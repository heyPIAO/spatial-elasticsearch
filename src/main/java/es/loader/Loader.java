package es.loader;

import es.common.annotation.Geometry;
import es.exception.LoaderException;
import es.model.BaseEntity;
import es.util.FileUtils;
import es.util.StringUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static es.common.config.ESConfig.HOST_NAME;
import static es.common.config.ESConfig.HTTP_PORT;


/**
 * 数据装载类的基类
 */
public class Loader<T extends BaseEntity> implements LoadProcedure<T> {


    // TODO 改成全局单例
    RestHighLevelClient client;
    String indexName;
    boolean isOpen = false;
    String fileName;

    /**
     * 配置索引名称
     * @return
     */
    Loader name(String name) {
        this.indexName = name;
        return this;
    };

    Loader fileName(String name) {
        this.fileName = name;
        return this;
    }

    public boolean createIndex() throws Exception {
        if (this.indexExist()) {
            System.out.println("Index Already Exists");
            return true;
        }
        CreateIndexRequest request = new CreateIndexRequest(this.indexName);
        request.settings(Settings.builder()
          .put("index.number_of_shards", 1)
          .put("index.number_of_replicas", 0)
        );
        Class <T> entityClass = getTClass();
        request.mapping(T.toESMap(entityClass.getName()));
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
            throw new LoaderException("Create index fail");
        }
        return true;
    }

    private boolean indexExist() throws IOException {
        GetIndexRequest request = new GetIndexRequest(this.indexName);
        return this.client.indices().exists(request, RequestOptions.DEFAULT);
    }


    public void load(List<T> data) throws Exception {
        for(T t: data) {
            IndexRequest request = new IndexRequest(this.indexName);
            request.id(t.getId());
            String json = t.toJson();
            request.source(json, XContentType.JSON);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            if (!response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                throw new LoaderException(response.getResult().toString());
            }
        }
    }

    public void bulkLoad(List<T> data) throws Exception {
        // todo
    }


    public boolean open() {
        this.client = new RestHighLevelClient(RestClient.builder(
          new HttpHost(HOST_NAME, HTTP_PORT, "http"),
          new HttpHost(HOST_NAME, 9201, "http")));
        this.isOpen = true;
        return true;
    }

    /**
     * 执行数据装载流程
     * @return
     * @throws Exception
     * todo 利用bulkload装载所有数据
     */
    public boolean execute() throws Exception {
        this.check();
        this.createIndex();
        List<T> preparedData = this.prepareData(this.fileName); // 默认前100条
        this.load(preparedData);
        return true;
    }

    public boolean close() throws IOException {
        if (!this.isOpen) {
            // todo 写一个 warning
            return true;
        }
        this.client.close();
        return true;
    }

    public boolean check() {
        if (!this.isOpen) {
            throw new LoaderException("Loader not open yet");
        }
        if (this.indexName == null){
            throw new LoaderException("Not set index name yet");
        }
        return true;
    }

    /**
     * 准备数据
     * @param filename
     * @param size
     * @param offsize
     * @return
     * @throws Exception
     */
    private List<T> prepareData(String filename, int size, int offsize) throws Exception {
        List<String> data = FileUtils.readByLine(filename, size, true);
        Constructor<?> con = getTClass().getConstructor();
        // todo 默认文件有头
        List<String> heads = Arrays.asList(FileUtils.readByLine(filename, 1, false).get(0).split(","));
        List<T> ts = data.stream().map((x)->{
            try {
                Object obj = con.newInstance();
                String[] values = x.split(",");
                List<Field> fields = this.getFields();
                for(Field field:fields) {
                    int i = heads.indexOf(StringUtil.unCamelCase(field.getName()));
                    if (values[i]==null || values[i].trim().length()==0) continue;
                    field.setAccessible(true);
                    if(field.getGenericType().getTypeName().equals("int")||field.getGenericType().getTypeName().equals("Integer")){
                        field.set(obj, Integer.valueOf(values[i]));
                    } else if(field.getGenericType().getTypeName().equals("java.lang.String")){
                        Geometry geoAnno = field.getAnnotation(Geometry.class);
                        if(geoAnno != null){
                            WKTReader reader = new WKTReader();
                            org.locationtech.jts.geom.Geometry geometry = reader.read(values[i]);
                            switch (geoAnno.type()){
                                case POINT:
                                    Point point = (Point)geometry;
                                    field.set(obj, point.getY() + "," + point.getX());
                                    continue;
                                default:
                                    throw new LoaderException("Unsupport shape field type");
                            }
                        } else {
                            field.set(obj, values[i]);
                        }
                    } else if(field.getGenericType().getTypeName().equals("float")||field.getGenericType().getTypeName().equals("Float")){
                        field.set(obj, Float.valueOf(values[i]));
                    } else {
                        throw new LoaderException("Unsupported Field Type for Data Model");
                    }
                }
                return (T)obj;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        return ts;
    }

    /**
     * 默认为前100条
     * @param filename
     * @return
     * @throws Exception
     */
    private List<T> prepareData(String filename) throws Exception {
        return this.prepareData(filename, 100, 0);
    }

    private Class<T> getTClass() {
        return (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private List<Field> getFields(){
        List<Field> fields = new ArrayList<>();
        Class <T> c = getTClass();
        // 迭代获取类及父类中的所有字段
        while(c!=null){
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = (Class <T>)c.getSuperclass();
        }
        return fields;
    }

}
