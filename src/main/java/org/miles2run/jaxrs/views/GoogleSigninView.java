package org.miles2run.jaxrs.views;

import org.jug.view.View;
import org.miles2run.business.services.GoogleService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @GET
    @Produces("text/html")
    public View signin(){
        logger.info("Inside GoogleSigninView signin()..");
        return View.of(googleService.buildLoginUrl(),true).withAbsoluteUrl();
    }



}
