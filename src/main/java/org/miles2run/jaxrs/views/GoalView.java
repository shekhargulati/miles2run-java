package org.miles2run.jaxrs.views;

import org.jboss.resteasy.annotations.Form;
import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Path("/goals")
public class GoalView {

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;
    @Inject
    private GoalService goalService;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private Logger logger;

    @GET
    @Produces("text/html")
    @LoggedIn
    @InjectProfile
    public View showAllGoals() {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        List<Goal> goals = goalService.findAllGoalsForProfile(loggedInUser);
        return View.of("/goals", templateEngine).withModel("goals", goals);
    }

    @Path("/create")
    @GET
    @Produces("text/html")
    @LoggedIn
    @InjectProfile
    public View createGoalForm() {
        return View.of("/createGoal", templateEngine).withModel("goal", new Goal());
    }


    @Path("/create")
    @POST
    @Produces("text/html")
    @LoggedIn
    @InjectProfile
    public View createGoal(@Form Goal goal) {
        logger.info("Goal : " + goal);
        goal.setTargetDate(goal.getTargetDateFromString());
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        goalService.save(goal, profile);
        return View.of("/goals", true);
    }
}
