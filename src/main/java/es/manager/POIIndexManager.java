package es.manager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class POIIndexManager extends IndexManager {

    public POIIndexManager() {
        this.name = "poi";
    }

}
