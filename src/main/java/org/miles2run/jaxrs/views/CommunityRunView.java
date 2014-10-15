package org.miles2run.jaxrs.views;

import org.jug.filters.InjectPrincipal;
import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.CommunityRunJPAService;
import org.miles2run.business.services.jpa.GoalJPAService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.services.redis.CommunityRunRedisService;
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
    private GoalJPAService goalJPAService;

    @Inject
    private ProfileService profileService;

    @GET
    @Produces("text/html")
    @InjectProfile
    public View allCommunityRuns() {
        List<CommunityRun> communityRuns = communityRunJPAService.findAllActiveCommunityRuns();
        List<CommunityRunDetails> runs = new ArrayList<>();
        for (CommunityRun communityRun : communityRuns) {
            runs.add(CommunityRunDetails.fromCommunityRun(communityRun).addStats(communityRunRedisService.getCurrentStatsForCommunityRun(communityRun.getSlug())));
        }
        return View.of("/community_runs", templateEngine).withModel("communityRuns", runs);
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
        CommunityRunDetails communityRunDetails = CommunityRunDetails.fromCommunityRun(communityRun).addStats(communityRunRedisService.getCurrentStatsForCommunityRun(communityRun.getSlug()));
        if (securityContext.getUserPrincipal() != null) {
            String principal = securityContext.getUserPrincipal().getName();
            if (communityRunRedisService.isUserAlreadyPartOfRun(slug, principal)) {
                Long goalId = goalJPAService.findGoalIdWithCommunityRunAndProfile(communityRunJPAService.find(slug), profileService.findProfile(principal));
                communityRunDetails.addParticipationDetails(true);
                return View.of("/community_run", templateEngine).withModel("communityRun", communityRunDetails).withModel("goalId", goalId);
            }
        }
        return View.of("/community_run", templateEngine).withModel("communityRun", communityRunDetails);
    }

    @Path("/{slug}/join")
    @POST
    @Produces("text/html")
    @LoggedIn
    public View joinCommunityRun(@NotNull @PathParam("slug") final String slug) {
        if (!communityRunRedisService.communityRunExists(slug)) {
            return View.of("/community_runs", true);
        }
        String principal = securityContext.getUserPrincipal().getName();
        if (communityRunRedisService.isUserAlreadyPartOfRun(slug, principal)) {
            return View.of("/community_runs/" + slug, true);
        }

        Profile profile = profileService.findProfile(principal);
        logger.info("Adding profile {} to community run ", principal, slug);
        CommunityRun communityRun = communityRunJPAService.addRunnerToCommunityRun(slug, profile);

        Goal goal = Goal.newCommunityRunGoal(communityRun);
        logger.info("Creating a CommunityRun goal for profile {}", principal);
        Goal savedGoal = goalJPAService.save(goal, profile);
        logger.info("Created a new goal with id {}", savedGoal.getId());

        communityRunRedisService.addGoalToCommunityRun(slug, savedGoal.getId());
        communityRunRedisService.addRunnerToCommunityRun(slug, profile);
        return View.of("/goals/" + savedGoal.getId(), true);
    }

    @Path("/{slug}/leave")
    @POST
    @Produces("text/html")
    @LoggedIn
    public View leaveCommunityRun(@NotNull @PathParam("slug") final String slug) {
        if (!communityRunRedisService.communityRunExists(slug)) {
            return View.of("/community_runs", true);
        }
        String principal = securityContext.getUserPrincipal().getName();
        if (!communityRunRedisService.isUserAlreadyPartOfRun(slug, principal)) {
            return View.of("/community_runs/" + slug, true);
        }
        Profile profile = profileService.findProfile(principal);
        logger.info("User {} leaving community run {}", principal, slug);
        communityRunJPAService.leaveCommunityRun(slug, profile);
        goalJPAService.archiveGoalWithCommunityRun(communityRunJPAService.find(slug), profile);
        communityRunRedisService.removeRunnerFromCommunityRun(slug, principal);
        return View.of("/community_runs/" + slug, true);
    }
}
