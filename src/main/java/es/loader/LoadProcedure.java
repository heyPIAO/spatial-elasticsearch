package es.loader;
import java.io.IOException;
import java.util.List;

public interface LoadProcedure<T> {

    /**
     * 检查当前参数是否合适
     * @return
     */
    boolean check();

    /**
     * 打开client
     * @return
     */
    boolean open();

    boolean createIndex();


    void load(List<T> data) throws Exception;

    void bulkLoad(List<T> data);

    /**
     * 触发数据装载操作
     */
    boolean execute() throws Exception ;

    /**
     * 关闭client
     * @return
     */
    boolean close();


}
