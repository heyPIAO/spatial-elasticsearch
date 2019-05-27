package es.loader;

import java.io.IOException;
import java.util.List;

public interface LoadProcedure<T> {

    /**
     * 打开client
     * @return
     */
    boolean open();

    /**
     * 出发数据装载操作
     */
    boolean execute(List<T> data) throws Exception;

    /**
     * 关闭client
     * @return
     */
    boolean close() throws IOException;

    void createIndex() throws Exception;

}
