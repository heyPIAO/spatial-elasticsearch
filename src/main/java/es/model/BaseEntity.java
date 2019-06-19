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
import java.util.*;

@Getter
@Setter
public class BaseEntity {

    private String id;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toESMapStr(String clazz) {
        Map<String, Object> map = this.toESMap(clazz);
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    public Map<String, Object> toESMap(String className) {
        Map<String, Object> properties = new HashMap<>();
        Class c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new LoaderException("No valid class " + className);
        }
        List<Field> fields = new ArrayList<>();
        while(c!=null){
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        for (Field field : fields) {
            // todo 反射获取annotation，若有范围类型，则设置范围
            String fieldType = field.getGenericType().getTypeName();
            String fieldName = field.getName();
            Map<String, String> fieldMap = new HashMap<>();
            // 基于字段类型设置字段mapping
            if(fieldType.equals("int") || fieldType.equals("java.lang.Integer")){
                fieldMap.put("type", "integer");
            } else if(fieldType.equals("float") || fieldType.equals("java.lang.Float")){
                fieldMap.put("type", "float");
            } else if(fieldType.equals("double") || fieldType.equals("java.lang.Double")){
                fieldMap.put("type", "double");
            } else if(fieldType.equals("java.lang.String")){
                fieldMap.put("type", "text");
            } else if(fieldType.equals("java.util.Data")){
                fieldMap.put("type", "date");
            } else if(fieldType.equals("boolean") || fieldType.equals("java.lang.Boolean")){
                fieldMap.put("type", "boolean");
            } else {
                throw new LoaderException("Unsupported Field Type");
            }

            // 基于标签设置字段mapping
            Annotation[] annotations = field.getDeclaredAnnotations();
            for(Annotation annotation:annotations) {
                if(annotation.annotationType().getName().equals(Chinese.class.getName())) {
                    fieldMap.put("analyzer", "ik_max_word");
                    fieldMap.put("search_analyzer", "ik_smart");
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

            properties.put(fieldName, fieldMap);
        }
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        return mapping;
    }

}
