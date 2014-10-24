package org.miles2run.views.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.CommunityRunGoal;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.UserRepresentation;
import org.miles2run.views.filters.InjectProfile;
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

@Path("/goals")
public class GoalView {

    private final Logger logger = LoggerFactory.getLogger(GoalView.class);

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private GoalRepository goalRepository;
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
            Profile profile = profileRepository.findByUsername(username);
            Goal goal = goalRepository.find(profile, goalId);
            if (goal == null) {
                logger.info("No Goal found for id {}", goalId);
                throw new ViewResourceNotFoundException(String.format("There is no goal with id %s associated with user %s", goalId, username), templateEngine);
            }
            Map<String, Object> model = new HashMap<>();
            if (goal instanceof CommunityRunGoal) {
                CommunityRun communityRun = ((CommunityRunGoal) goal).getCommunityRun();
                model.put("communityRun", communityRun);
            }
            Profile profileWithSocialConnections = profileRepository.findWithSocialConnections(username);
            model.put("activeProfile", UserRepresentation.from(profileWithSocialConnections));
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
