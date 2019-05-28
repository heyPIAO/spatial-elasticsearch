package es.model;

import es.common.annotation.Chinese;
import es.common.annotation.Geometry;
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

    @Chinese
    private String poiName;

    @Chinese
    private String address;
    
    @Geometry(type = Geometry.Type.POINT)
    private String shape;

}
