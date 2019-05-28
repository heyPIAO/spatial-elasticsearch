package es.model;

import com.google.gson.Gson;
import es.common.annotation.Chinese;
import es.common.annotation.Geometry;
import es.common.annotation.IP;
import es.common.annotation.Keyword;
import es.exception.LoaderException;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class BaseEntity {

    private String id;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    public static Map<String, Object> toESMap(String className) throws ClassNotFoundException {
        Map<String, Object> properties = new HashMap<>();
        Class c = Class.forName(className);
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            // todo 反射获取annotation，若有范围类型，则设置范围
            String fieldType = field.getGenericType().getTypeName();
            String fieldName = field.getName();
            Map<String, String> fieldMap = new HashMap<>();
            // 基于字段类型设置字段mapping
            switch (fieldType) {
                case "int":
                    fieldMap.put("type", "integer");
                    properties.put(fieldName, fieldMap);
                    break;
                case "float":
                    fieldMap.put("type", "float");
                    properties.put(fieldName, fieldMap);
                    break;
                case "double":
                    fieldMap.put("type", "double");
                    properties.put(fieldName, fieldMap);
                    break;
                case "java.lang.String":
                    fieldMap.put("type", "text");
                    properties.put(fieldName, fieldMap);
                    break;
                case "java.util.Date":
                    fieldMap.put("type", "date");
                    properties.put(fieldName, fieldMap);
                    break;
                case "boolean":
                    fieldMap.put("type", "boolean");
                    properties.put(fieldName, fieldMap);
                    break;
                default:
                    throw new LoaderException("Unsupported Field Type");
            }

            // 基于标签设置字段mapping
            Annotation[] annotations = field.getDeclaredAnnotations();
            for(Annotation annotation:annotations) {
                if(annotation.annotationType().getName().equals(Chinese.class.getName())) {
                    fieldMap.put("analyzer", "ik_max_word");
                } else if (annotation.annotationType().getName().equals(Geometry.class.getName())) {
                    Geometry geoAnno = (Geometry) annotation;
                    switch (geoAnno.type()) {
                        case POINT:
                            fieldMap.put("type", "geo_point");
                            break;
                        default: fieldMap.put("type", "geo_shape");
                    }
                } else if (annotation.annotationType().getName().equals(IP.class.getName())) {
                    fieldMap.put("type", "ip");
                } else if (annotation.annotationType().getName().equals(Keyword.class.getName())) {
                    fieldMap.put("type", "keyword");
                }
            }

        }
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        return mapping;
    }

}
