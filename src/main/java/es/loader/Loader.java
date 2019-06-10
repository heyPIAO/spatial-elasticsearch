package es.loader;

import es.common.annotation.Geometry;
import es.exception.LoaderException;
import es.model.BaseEntity;
import es.util.FileUtils;
import es.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static es.common.config.ESConfig.*;


/**
 * 数据装载类的基类
 */
@Getter
@Setter
public class Loader<T extends BaseEntity> implements LoadProcedure<T> {

    private static final Logger logger = LoggerFactory.getLogger(Loader.class);


    // TODO 改成全局单例
    private TransportClient client;
    private String indexName;
    private boolean isOpen = false;
    private String fileName;

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

    public boolean createIndex() {
        if (this.indexExist()) {
            System.out.println("Index Already Exists");
            return true;
        }
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        CreateIndexResponse response = indicesAdminClient.prepareCreate(this.indexName)
          .setSettings(Settings.builder()
            .put("index.number_of_shards", 1)
            .put("index.number_of_replicas", 0)).get();
        if (!response.isAcknowledged()) {
            throw new LoaderException("Create index fail");
        }
        // 用put接口实现mapping设置
        PutMappingResponse putResponse = indicesAdminClient.preparePutMapping(this.indexName)
          .setType(this.indexName)
          .setSource(getT().toESMapStr(getTClass().getName())).get();
        if(!putResponse.isAcknowledged()){
            throw new LoaderException("Put mapping to index" + this.indexName + " failed");
        }
        return true;
    }

    private boolean indexExist() {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        IndicesExistsResponse response = indicesAdminClient.prepareExists(this.indexName).get();
        return response.isExists();
    }


    public void load(List<T> data) {
        for(T t: data) {
            String json = t.toJson();
            IndexResponse response = client.prepareIndex(this.indexName, this.getTClass().getSimpleName(), t.getId())
              .setSource(json, XContentType.JSON).get();
            if (!response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                throw new LoaderException(response.getResult().toString());
            }
        }
    }

    public void bulkLoad(List<T> data) {
        // todo
    }


    public boolean open() {
        Settings settings = Settings.builder()
          .put("cluster.name", "es").build();
        try {
            this.client = new PreBuiltTransportClient(settings)
              .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(HOST_NAME), TRANSPORT_PORT))
              .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(HOST_NAME_2), TRANSPORT_PORT));
            this.isOpen = true;
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new LoaderException("Unvalid host name with [" + HOST_NAME + "," + HOST_NAME_2 + "]");
        }
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

    public boolean close() {
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
                    if(i < 0){
                        logger.error("No value for field " + field.getName());
                        continue;
                    }
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

    /**
     * 获取泛型 T 的真实类
     * @return
     */
    private Class<T> getTClass() {
        return (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 获取泛型 T 实例
     * @return
     */
    private T getT() {
        Class<T> c = this.getTClass();
        try {
            Constructor<T> con = c.getConstructor();
            T t = con.newInstance();
            return t;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new LoaderException("No no argument construction for class " + this.getTClass().getName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 迭代获取泛型 T 所有字段（包括父类所有字段）
     * @return
     */
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
