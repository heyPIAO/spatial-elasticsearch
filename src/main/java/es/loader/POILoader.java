package es.loader;

// todo 添加log

import es.exception.LoaderException;
import es.model.POI;
import es.util.FileUtils;
import es.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static es.common.config.ESConfig.*;

/**
 * 装载POI数据到ES中
 */
@Getter
@Setter
public class POILoader extends Loader implements LoadProcedure<POI> {


    public boolean open() {
        this.client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(HOST_NAME, HTTP_PORT, "http")));
        this.isOpen = true;
        return true;
    }

    public POILoader configure() {
        return this;
    }

    public boolean execute(List<POI> data) throws Exception {
        this.check();
        this.createIndex();
        // this.load(data);
        return true;
    }

    public void createIndex() throws Exception {
        if (this.indexExist()) {
            System.out.println("Index Already Exists");
            return;
        }
        // todo 写成全局单例, 统一配置
        CreateIndexRequest request = new CreateIndexRequest(this.indexName);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1)
        );
        request.mapping(POI.toESMap(POI.class.getName()));
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
            throw new LoaderException("Create index fail");
        }
    }

    public void load(List<POI> data) throws IOException {
        // todo change to bulk load
        for(POI poi: data) {
            IndexRequest request = new IndexRequest(this.indexName);
            request.id(poi.getId());
            String json = poi.toJson();
            request.source(json, XContentType.JSON);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            if (!response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                throw new LoaderException(response.getResult().toString());
            }
        }
    }

    public boolean close() throws IOException {
        if (!this.isOpen) {
            // todo
            return true;
        }
        this.client.close();
        return true;
    }

    private boolean indexExist() throws IOException {
        GetIndexRequest request = new GetIndexRequest(this.indexName);
        return this.client.indices().exists(request, RequestOptions.DEFAULT);
    }

    public static void main(String[] args) throws Exception {
        // 字段名文件中为dash，model类为驼峰
        System.out.println("=== START LOAD POI DATA ===");
        POILoader loader = new POILoader();
        String fileName = "D:\\Work\\工作\\Part 1\\地理大数据\\数据示例\\POI_bj_out.csv";
        List<String> data = FileUtils.readByLine(fileName, 100, true);
        Class c = POI.class;
        Constructor<?> con = c.getConstructor();
        List<String> heads = Arrays.asList(FileUtils.readByLine(fileName, 1, false).get(0).split(","));
        List<POI> pois = data.stream().map((x)->{
            try {
                Object obj = con.newInstance();
                String[] values = x.split(",");
                Field[] fields = POI.class.getDeclaredFields();
                for(Field field:fields){
                    int i = heads.indexOf(StringUtil.unCamelCase(field.getName()));
                    field.setAccessible(true);
                    switch (field.getGenericType().getTypeName()){
                        case "int":
                            if (values[i]!=null && values[i].trim().length()>0)
                                field.set(obj, Integer.valueOf(values[i]));
                            break;
                        case "java.lang.String":
                            field.set(obj, values[i]);
                            break;
                        case "float":
                            if (values[i]!=null && values[i].trim().length()>0)
                                field.set(obj, Float.valueOf(values[i]));
                            break;
                        default: throw new LoaderException("Unsupported Field Type for Data Model");
                    }
                }
                return (POI)obj;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        loader.name("poi");
        loader.open();
        loader.execute(pois);
        loader.close();
        System.out.println("=== END LOADING ===");
    }

}
