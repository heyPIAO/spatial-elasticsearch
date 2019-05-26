package es.loader;

import es.exception.LoaderException;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * 数据装载类的基类
 */
public class Loader {


    // TODO 改成全局单例
    RestHighLevelClient client;
    String indexName;

    boolean isOpen = false;

    /**
     * 配置当前Loader
     */
    Loader configure() {
        return this;
    };

    /**
     * 配置索引名称
     * @return
     */
    Loader name(String name) {
        this.indexName = name;
        return this;
    };

    boolean check() {
        if (!this.isOpen) {
            throw new LoaderException("Loader not open yet");
        }
        if (this.indexName == null){
            throw new LoaderException("Not set index name yet");
        }
        return true;
    }


}
