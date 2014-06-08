package org.miles2run.jaxrs.views;

import org.jug.view.View;
import org.miles2run.business.services.GoogleService;
import org.miles2run.business.utils.UrlUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 13/05/14.
 */
@Path("/google/signin")
public class GoogleSigninView {

    @Inject
    private Logger logger;

    @Inject
    private GoogleService googleService;
    @Context
    private HttpServletRequest request;

    @GET
    @Produces("text/html")
    public View signin() {
        logger.info("Inside GoogleSigninView signin()..");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            return View.of("/home", true);
        }
        return View.of(googleService.buildLoginUrl(UrlUtils.getBaseUrl(request)), true).withAbsoluteUrl();
    }


}
