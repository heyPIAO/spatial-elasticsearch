package es.util;


public class StringUtil {
    /**
     * i_am_camel => iAmCamel
     */
    public static String camelCase(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '_') {
                i++;
                if (i < value.length())
                    sb.append((char) (value.charAt(i) - 'a' + 'A'));
            } else
                sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * iAmCamel => i_am_camel
     */
    public static String unCamelCase(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                sb.append('_').append((char) (ch - 'A' + 'a'));
            } else
                sb.append(ch);
        }
        return sb.toString();
    }
}
