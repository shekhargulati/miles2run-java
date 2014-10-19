package org.miles2run.views.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/contact")
public class ContactView {

    @Inject
    private TemplateEngine templateEngine;

    @GET
    @Produces("text/html")
    @EnableSession
    public View about() throws ViewException {
        return View.of("/contact", templateEngine);
    }

}
