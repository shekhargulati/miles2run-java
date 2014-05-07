package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;
import org.miles2run.business.services.ProfileService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 10/03/14.
 */
@Path("/home")
public class HomeView {

    @Inject
    private Logger logger;

    @GET
    @LoggedIn
    @Produces("text/plain")
    public String home() {
        logger.info("Welcome to home()..");
        return "Welcome to Home!";
    }
}
