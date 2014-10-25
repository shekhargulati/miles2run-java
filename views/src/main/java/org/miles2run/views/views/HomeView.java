package org.miles2run.views.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.miles2run.views.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/home")
public class HomeView {

    @Inject
    private TemplateEngine templateEngine;

    @GET
    @Produces("text/html")
    @LoggedIn
    @InjectProfile
    public View showAllGoals() {
        return View.of("/home", templateEngine);
    }

}
