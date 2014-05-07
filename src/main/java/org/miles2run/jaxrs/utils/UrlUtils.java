package org.miles2run.jaxrs.utils;

/**
 * Created by shekhargulati on 10/03/14.
 */
public abstract class UrlUtils {

    public static String removeProtocol(String url) {
        if (url != null) {
            return url.replace("http:", "");
        }
        return null;
    }
}
