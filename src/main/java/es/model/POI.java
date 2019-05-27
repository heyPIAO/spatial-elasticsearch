package es.model;

// TODO 添加是否为中文字段的标签支持
// TODO 添加是否是空间字段的标签支持
// TODO 实现AOP, 在数据入库时自动获取这些标签, 用于入库的mapping配置

import lombok.Getter;
import lombok.Setter;

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
    private String vadminCode;
    private String chainCode;
    private String priorAuth;
    private int poiFlag;
    private int rating;
    private String poiName; // 中文
    private String address; // 中文
    private String shape;

}
