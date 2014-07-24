package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
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
            Goal goal = goalService.findGoal(username, goalId);
            if (goal == null) {
                throw new ViewResourceNotFoundException("There is no goal with id : " + goalId, templateEngine);
            }
            ProfileSocialConnectionDetails activeProfileWithSocialConnections = profileService.findProfileWithSocialConnections(username);
            return View.of("/goal", templateEngine).withModel("activeProfile", activeProfileWithSocialConnections).withModel("goal", goal);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.log(Level.SEVERE, "Unable to load home page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

}
