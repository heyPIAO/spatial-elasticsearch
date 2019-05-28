package es.common.annotation;

import java.lang.annotation.*;

/**
 * @author Hu
 * @date 2019/5/28
 * es data mapping 的 ip 类
 * ipv4/ipv6
 **/
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IP {}
