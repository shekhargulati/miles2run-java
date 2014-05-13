package org.miles2run.business.utils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by shekhargulati on 10/03/14.
 */
public abstract class UrlUtils {

    public static String removeProtocol(String url) {
        if (url != null && url.startsWith("http:")) {
            return url.replace("http:", "");
        }else if(url != null && url.startsWith("https:")){
            return url.replace("https:", "");
        }
        return null;
    }

    public static String absoluteUrlFor(HttpServletRequest request, Class resourceClass, String method) {
        String requestUrl = request.getRequestURL().toString();
        String baseURL = requestUrl.substring(0, requestUrl.length() - request.getRequestURI().length()) + request.getContextPath();
        URI callbackResourceUri = UriBuilder.fromMethod(resourceClass, method).build();
        String url = new StringBuilder(baseURL).append(callbackResourceUri.toString()).toString();
        return url;
    }
}
