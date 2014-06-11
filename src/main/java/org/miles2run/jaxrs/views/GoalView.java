package org.miles2run.jaxrs.views;

import org.jboss.resteasy.annotations.Form;
import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.miles2run.jaxrs.forms.ProfileForm;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.logging.Level;
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

    @Path("/{goalId}")
    @GET
    @LoggedIn
    @Produces("text/html")
    @InjectProfile
    public View viewGoal(@PathParam("goalId") Long goalId) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            logger.info(String.format("Rendering home page for user %s ", username));
            ProfileSocialConnectionDetails activeProfileWithSocialConnections = profileService.findProfileWithSocialConnections(username);
            Goal goal = goalService.findGoal(username, goalId);
            if (goal == null) {
                throw new ViewResourceNotFoundException("There is no goal with id : " + goalId, templateEngine);
            }
            return View.of("/goal", templateEngine).withModel("activeProfile", activeProfileWithSocialConnections).withModel("goal", goal);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load home page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
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
        try {
            logger.info("Goal : " + goal);
            String loggedInUser = securityContext.getUserPrincipal().getName();
            Profile profile = profileService.findProfile(loggedInUser);
            goal.setTargetDate(goal.getTargetDateFromString());
            goal.setGoal(goal.getGoal() * goal.getGoalUnit().getConversion());
            goalService.save(goal, profile);
            return View.of("/home", true);
        } catch (Exception e) {
            logger.info("createGoal() Exception class " + e.getClass().getCanonicalName());
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                return constraintVoilationView(goal, (ConstraintViolationException) cause);
            }
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return View.of("/goals/create", templateEngine).withModel("profile", goal).withModel("errors", errors);
        }
    }

    private View constraintVoilationView(Goal goal, ConstraintViolationException constraintViolationException) {
        List<String> errors = new ArrayList<>();
        Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            errors.add(String.format("Field '%s' with value '%s' is invalid. %s", constraintViolation.getPropertyPath(), constraintViolation.getInvalidValue(), constraintViolation.getMessage()));
        }
        return View.of("/goals/create", templateEngine).withModel("profile", goal).withModel("errors", errors);
    }
}
