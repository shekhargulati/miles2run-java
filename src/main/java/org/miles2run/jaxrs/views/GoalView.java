package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalType;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.GoalJPAService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Path("/goals")
public class GoalView {

    private Logger logger = LoggerFactory.getLogger(GoalView.class);

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;
    @Inject
    private GoalJPAService goalJPAService;
    @Inject
    private TemplateEngine templateEngine;


    @Path("/{goalId}")
    @GET
    @LoggedIn
    @Produces("text/html")
    @InjectProfile
    public View viewGoal(@PathParam("goalId") Long goalId) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            logger.info("Rendering Goal page for user {} ", username);
            Profile profile = profileService.findProfile(username);
            Goal goal = goalJPAService.findGoal(profile, goalId);
            if (goal == null) {
                logger.info("No Goal found for id {}", goalId);
                throw new ViewResourceNotFoundException(String.format("There is no goal with id %s associated with user %s", goalId, username), templateEngine);
            }
            Map<String, Object> model = new HashMap<>();
            if (goal.getGoalType() == GoalType.COMMUNITY_RUN_GOAL) {
                CommunityRun communityRun = goal.getCommunityRun();
                model.put("communityRun", communityRun);
            }
            ProfileSocialConnectionDetails activeProfileWithSocialConnections = profileService.findProfileWithSocialConnections(username);
            model.put("activeProfile", activeProfileWithSocialConnections);
            model.put("goal", goal);
            return View.of("/goal", templateEngine).withModel(model);
        } catch (Exception e) {
            if (e instanceof ViewResourceNotFoundException) {
                throw e;
            }
            logger.error("Unable to load home page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }

}
