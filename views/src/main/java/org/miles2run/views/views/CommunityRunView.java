package org.miles2run.views.views;

import org.jug.filters.InjectPrincipal;
import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.core.repositories.jpa.CommunityRunRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.CommunityRunStatsRepository;
import org.miles2run.domain.entities.*;
import org.miles2run.representations.CommunityRunRepresentation;
import org.miles2run.views.filters.InjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Path("/community_runs")
public class CommunityRunView {

    private Logger logger = LoggerFactory.getLogger(CommunityRunView.class);

    @Inject
    private CommunityRunStatsRepository communityRunStatsRepository;
    @Inject
    private CommunityRunRepository communityRunRepository;

    @Inject
    private TemplateEngine templateEngine;

    @Context
    private SecurityContext securityContext;

    @Inject
    private GoalRepository goalRepository;

    @Inject
    private ProfileRepository profileRepository;

    @GET
    @Produces("text/html")
    @InjectProfile
    public View allCommunityRuns() {
        List<CommunityRun> communityRuns = communityRunRepository.findAllActiveCommunityRuns();
        List<CommunityRunRepresentation> runs = communityRuns.stream().map(communityRun -> CommunityRunRepresentation.from(communityRun).addStats(communityRunStatsRepository.getCurrentStatsForCommunityRun(communityRun.getSlug()))).collect(Collectors.toList());
        return View.of("/community_runs", templateEngine).withModel("communityRuns", runs);
    }

    @Path("/{slug}")
    @GET
    @Produces("text/html")
    @InjectProfile
    @InjectPrincipal
    public View viewCommunityRun(@NotNull @PathParam("slug") String slug) {
        CommunityRun communityRun = communityRunRepository.findBySlug(slug);
        if (communityRun == null) {
            throw new ViewResourceNotFoundException(String.format("No community run exists with name %s", slug), templateEngine);
        }
        CommunityRunRepresentation communityRunDetails = CommunityRunRepresentation.from(communityRun).addStats(communityRunStatsRepository.getCurrentStatsForCommunityRun(communityRun.getSlug()));
        if (securityContext.getUserPrincipal() != null) {
            String principal = securityContext.getUserPrincipal().getName();
            if (communityRunStatsRepository.isUserAlreadyPartOfRun(slug, principal)) {
                Long goalId = goalRepository.findGoalIdWithCommunityRunAndProfile(communityRunRepository.find(slug), profileRepository.findByUsername(principal));
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
        if (!communityRunStatsRepository.communityRunExists(slug)) {
            return View.of("/community_runs/", true);
        }
        String principal = securityContext.getUserPrincipal().getName();
        if (communityRunStatsRepository.isUserAlreadyPartOfRun(slug, principal)) {
            return View.of("/community_runs/" + slug, true);
        }
        Profile profile = profileRepository.findByUsername(principal);
        logger.info("Adding profile {} to community run {}", principal, slug);
        CommunityRun communityRun = communityRunRepository.addRunnerToCommunityRun(slug, profile);

        CommunityRunGoal goal = new CommunityRunGoalBuilder()
                .setCommunityRun(communityRun)
                .setDuration(new Duration(new Date(), communityRun.getDuration().getEndDate()))
                .setPurpose(communityRun.getName())
                .setProfile(profile)
                .createCommunityRunGoal();
        logger.info("Creating a CommunityRun goal for profile {}", principal);
        CommunityRunGoal savedGoal = goalRepository.save(goal);
        logger.info("Created a new goal with id {}", savedGoal.getId());

        communityRunStatsRepository.addGoalToCommunityRun(slug, savedGoal.getId());
        communityRunStatsRepository.addRunnerToCommunityRun(slug, profile);
        return View.of("/goals/" + savedGoal.getId(), true);
    }

    @Path("/{slug}/leave")
    @POST
    @Produces("text/html")
    @LoggedIn
    public View leaveCommunityRun(@NotNull @PathParam("slug") final String slug) {
        if (!communityRunStatsRepository.communityRunExists(slug)) {
            return View.of("/community_runs", true);
        }
        String principal = securityContext.getUserPrincipal().getName();
        if (!communityRunStatsRepository.isUserAlreadyPartOfRun(slug, principal)) {
            return View.of("/community_runs/" + slug, true);
        }
        Profile profile = profileRepository.findByUsername(principal);
        logger.info("User {} leaving community run {}", principal, slug);
        communityRunRepository.leaveCommunityRun(slug, profile);
        goalRepository.archiveGoalWithCommunityRun(communityRunRepository.find(slug), profile);
        communityRunStatsRepository.removeRunnerFromCommunityRun(slug, principal);
        return View.of("/community_runs/" + slug, true);
    }
}
