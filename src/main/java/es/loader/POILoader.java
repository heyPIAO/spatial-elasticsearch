package es.loader;

// todo 添加log

import es.model.POI;
import lombok.Getter;
import lombok.Setter;

/**
 * 装载POI数据到ES中
 */
@Getter
@Setter
public class POILoader extends Loader<POI> {

    public static void main(String[] args) throws Exception {
        // 字段名文件中为dash，model类为驼峰
        System.out.println("=== START LOAD POI DATA ===");
        POILoader loader = new POILoader();
        String fileName = "D:\\Work\\工作\\Part 1\\地理大数据\\数据示例\\POI_bj_out.csv";
        loader.name("poi").fileName(fileName);
        loader.open();
        loader.execute();
        loader.close();
        System.out.println("=== END LOADING ===");
    }

}
