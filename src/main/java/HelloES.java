import es.common.annotation.Chinese;
import es.model.POI;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class HelloES {

    public static void main(String[] args) throws Exception {
        POI poi = new POI();
        System.out.println(poi.getClass().getName());
        Class c = Class.forName(poi.getClass().getName());
        Field[] fields = c.getDeclaredFields();
        for(Field field:fields) {
            System.out.println(field.getGenericType().getTypeName());
            Annotation[] annotations = field.getAnnotations();
            for(Annotation annotation:annotations){
                System.out.println(annotation.annotationType().getName().endsWith(Chinese.class.getName()));
            }
        }
    }



}
