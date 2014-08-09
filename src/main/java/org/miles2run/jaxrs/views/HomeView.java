package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.services.jpa.GoalJPAService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
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
    @Inject
    private ProfileService profileService;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private GoalJPAService goalJPAService;

    @GET
    @Produces("text/html")
    @LoggedIn
    @InjectProfile
    public View showAllGoals() {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        List<Goal> goals = goalJPAService.findAllGoals(loggedInUser, false);
        return View.of("/home", templateEngine).withModel("goals", goals);
    }


}
