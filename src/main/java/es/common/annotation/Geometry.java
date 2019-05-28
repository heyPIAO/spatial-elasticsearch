package es.common.annotation;

import java.lang.annotation.*;

/**
 * @author Hu
 * @date 2019/5/28
 **/
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Geometry {

  public enum Type {
    POINT, POLYLINE, POLYGON, MULTIPOINT, MULTIPOLYLINE, MULTIPOLYGON
  }

  // 设置默认值
  Type type() default Type.POINT;

}
