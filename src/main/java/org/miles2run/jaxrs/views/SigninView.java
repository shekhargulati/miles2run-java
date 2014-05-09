package org.miles2run.jaxrs.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 11/03/14.
 */
@Path("/signin")
public class SigninView {

    @Inject
    private Logger logger;

    @Inject
    private TemplateEngine templateEngine;

    @GET
    @Produces("text/html")
    @EnableSession
    public View signin() throws ViewException {
        logger.info("In signin().. ");
        return new View("/signin").setTemplateEngine(templateEngine);
    }


}
