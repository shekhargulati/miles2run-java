package org.miles2run.jaxrs.views;

import org.jug.filters.InjectPrincipal;
import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.CommunityRunJPAService;
import org.miles2run.business.services.redis.CommunityRunRedisService;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.miles2run.jaxrs.vo.CommunityRunDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shekhargulati on 10/07/14.
 */
@Path("/community_runs")
public class CommunityRunView {

    private Logger logger = LoggerFactory.getLogger(CommunityRunView.class);

    @Inject
    private CommunityRunRedisService communityRunRedisService;
    @Inject
    private CommunityRunJPAService communityRunJPAService;

    @Inject
    private TemplateEngine templateEngine;

    @Context
    private SecurityContext securityContext;

    @Inject
    private GoalService goalService;

    @Inject
    private ProfileService profileService;

    @GET
    @Produces("text/html")
    @InjectProfile
    public View allCommunityRuns() {
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveRaces();
        List<CommunityRunDetails> runs = new ArrayList<>();
        for (CommunityRun communityRun : communityRuns) {
            runs.add(new CommunityRunDetails(communityRun, communityRunRedisService.currentStats(communityRun)));
        }
        return View.of("/community_runs", templateEngine).withModel("runs", runs);
    }

    @Path("/{slug}")
    @GET
    @Produces("text/html")
    @InjectProfile
    @InjectPrincipal
    public View viewCommunityRun(@NotNull @PathParam("slug") String slug) {
        CommunityRun communityRun = communityRunJPAService.findBySlug(slug);
        if (communityRun == null) {
            throw new ViewResourceNotFoundException(String.format("No community run exists with name %s", slug), templateEngine);
        }
        CommunityRunDetails communityRunDetails = new CommunityRunDetails(communityRun, communityRunRedisService.currentStats(communityRun));
        if (securityContext.getUserPrincipal() != null) {
            String principal = securityContext.getUserPrincipal().getName();
            if (communityRunRedisService.isUserAlreadyPartOfRun(slug, principal)) {
                Long goalId = goalService.findGoalIdWithCommunityRunAndProfile(slug, profileService.findProfile(principal));
                return View.of("/community_run", templateEngine).withModel("run", communityRunDetails).withModel("userAlreadyJoined", true).withModel("goalId", goalId);
            }
        }
        return View.of("/community_run", templateEngine).withModel("run", communityRunDetails).withModel("userAlreadyJoined", false);
    }

    @Path("/{slug}/join")
    @POST
    @Produces("text/html")
    @LoggedIn
    public View joinCommunityRun(@NotNull @PathParam("slug") final String slug) {
        String principal = securityContext.getUserPrincipal().getName();
        if (communityRunRedisService.isUserAlreadyPartOfRun(slug, principal)) {
            return View.of("/community_runs/" + slug, true);
        }
        if (!communityRunRedisService.communityRunExists(slug)) {
            return View.of("/community_runs", true);
        }
        Profile profile = profileService.findProfile(principal);
        logger.info("Adding profile {} to community run ", principal, slug);
        CommunityRun communityRun = communityRunJPAService.addRunnerToCommunityRun(slug, profile);

        Goal goal = Goal.newCommunityRunGoal(communityRun);
        logger.info("Creating a CommunityRun goal for profile {}", principal);
        Goal savedGoal = goalService.save(goal, profile);
        logger.info("Created a new goal with id {}", savedGoal.getId());

        communityRunRedisService.addGoalToCommunityRun(slug, savedGoal.getId());
        communityRunRedisService.addRunnerToCommunityRun(slug, profile);
        return View.of("/goals/" + savedGoal.getId(), true);
    }
}
