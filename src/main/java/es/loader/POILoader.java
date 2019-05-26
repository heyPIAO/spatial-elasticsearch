package es.loader;

// todo 添加log

import es.exception.LoaderException;
import es.model.POI;
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
import java.util.List;

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

    public boolean execute(List<POI> data) throws IOException {
        this.check();
        this.createIndex();
        this.load(data);
        return false;
    }

    public void createIndex() throws IOException {
        if (this.indexExist()) return;
        // todo 写成全局单例, 统一配置
        CreateIndexRequest request = new CreateIndexRequest(this.indexName);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1)
        );
        // todo 配置mapping

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

}
