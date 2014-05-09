package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 10/03/14.
 */
@Path("/home")
public class HomeView {

    @Inject
    private Logger logger;
    @Context
    private SecurityContext securityContext;

    @GET
    @LoggedIn
    @Produces("text/html")
    public String home() {
        logger.info("Welcome to home().." + securityContext.getUserPrincipal().getName());
        return "Welcome to Home " + securityContext.getUserPrincipal().getName();
    }
}
