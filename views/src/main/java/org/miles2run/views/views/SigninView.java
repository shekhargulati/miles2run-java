package org.miles2run.views.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/signin")
public class SigninView {

    private final Logger logger = LoggerFactory.getLogger(SigninView.class);

    @Inject
    private TemplateEngine templateEngine;

    @GET
    @Produces("text/html")
    @EnableSession
    public View signin() throws ViewException {
        logger.info("In signin().. ");
        return View.of("/signin", templateEngine);
    }

}
