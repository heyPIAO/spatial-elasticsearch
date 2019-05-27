package es.model;

import com.google.gson.Gson;
import es.exception.LoaderException;
import lombok.Getter;
import lombok.Setter;

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
        Map<String, Object> message = new HashMap<>();
        Class c = Class.forName(className);
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            // todo 反射获取annotation，若为中文，设置中文标签
            // todo 反射获取annotation，若有范围类型，则设置范围
            String fieldType = field.getGenericType().getTypeName();
            String fieldName = field.getName();
            switch (fieldType) {
                case "int":
                    message.put(fieldName, "integer");
                    break;
                case "float":
                    message.put(fieldName, "float");
                    break;
                case "double":
                    message.put(fieldName, "double");
                    break;
                case "java.lang.String":
                    message.put(fieldName, "text");
                    break;
                case "java.util.Date":
                    message.put(fieldName, "date");
                    break;
                case "boolean":
                    message.put(fieldName, "boolean");
                    break;
                default:
                    throw new LoaderException("Unsupported Field Type");
            }
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put("message", message);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        return mapping;
    }

}
