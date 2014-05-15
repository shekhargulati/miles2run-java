package org.miles2run.jaxrs.views;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import org.jug.view.View;
import org.miles2run.business.utils.UrlUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 09/05/14.
 */
@Path("/facebook/signin")
public class FacebookSigninView {

    @Inject
    private FacebookFactory facebookFactory;

    @Inject
    private Logger logger;

    @GET
    @Produces("text/html")
    public View signin(@Context HttpServletRequest request) {
        try {
            Facebook facebook = facebookFactory.getInstance();
            String redirectUrl = UrlUtils.absoluteUrlForResourceMethod(request, FacebookCallbackView.class, "callback");
            logger.info(String.format("Facebook redirectUrl %s", redirectUrl));
            return View.of(facebook.getOAuthAuthorizationURL(redirectUrl), true).withAbsoluteUrl();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get Facebook AuthorizationURL. Exception is: " + e);
        }
    }
}
