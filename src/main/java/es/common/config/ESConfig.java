package es.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ESConfig {

    public static String HOST_NAME = "10.183.69.7";
    public static int TRANSPORT_PORT = 9300;
    public static int HTTP_PORT = 9200;

}
