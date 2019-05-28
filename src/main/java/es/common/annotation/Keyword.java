package es.common.annotation;

import java.lang.annotation.*;

/**
 * @author Hu
 * @date 2019/5/28
 * es data mapping 的 keyword 类
 **/
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Keyword {}
