package es.common.config;

// TODO change to ES Configuration

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ESConfig {

    public static String HOST_NAME = "localhost";
    public static int TRANSPORT_PORT = 9300;
    public static int HTTP_PORT = 9200;

}
