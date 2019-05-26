package es.model;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseEntity {

    private String id;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    public String toESMap(){
        return null;
    }

}
