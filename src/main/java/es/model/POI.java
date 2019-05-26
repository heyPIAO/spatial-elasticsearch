package es.model;

// TODO 添加是否为中文字段的标签支持
// TODO 添加是否是空间字段的标签支持
// TODO 实现AOP, 在数据入库时自动获取这些标签, 用于入库的mapping配置

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 北京POI数据的model类
 */
@Getter
@Setter
public class POI extends BaseEntity {

    private String mapId;
    private String kind;
    private String zipCode;
    private String telephone;
    private String adminCode;
    private float lon;
    private float lat;
    private String poiId;
    private int importance;
    private String yadminCode;
    private String chainCode;
    private String priorAuth;
    private int open24h;
    private int poiFlag;
    private int rating;
    private String poiName; // 中文
    private String address; // 中文
    private String shape;

    // todo 用反射写到基类
    @Override
    public String toESMap() {
        Gson message = new Gson();
        Gson type = new Gson();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("mapId", "text");


        return super.toESMap();
    }
}
