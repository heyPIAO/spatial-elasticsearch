package es.common.annotation;

import java.lang.annotation.*;

/**
 * @author Hu
 * @date 2019/5/28
 **/
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Chinese {
  String desc() default "中文字段";
}
