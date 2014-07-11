package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.services.CommunityRunService;
import org.miles2run.business.services.GoalService;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by shekhargulati on 10/07/14.
 */
@Path("/community_runs")
public class CommunityRunView {

    private Logger logger = LoggerFactory.getLogger(CommunityRunView.class);

    @Inject
    private CommunityRunService communityRunService;

    @Inject
    private TemplateEngine templateEngine;

    @Context
    private SecurityContext securityContext;

    @Inject
    private GoalService goalService;

    @GET
    @Produces("text/html")
    @InjectProfile
    public View allCommunityRuns() {
        List<CommunityRun> runs = communityRunService.findAllActiveRaces();
        return View.of("/community_runs", templateEngine).withModel("runs", runs);
    }

    @Path("/{slug}")
    @GET
    @Produces("text/html")
    @InjectProfile
    public View findCommunityRun(@NotNull @PathParam("slug") String slug) {
        CommunityRun run = communityRunService.findBySlug(slug);
        return View.of("/community_run", templateEngine).withModel("run", run);
    }

    @Path("/{slug}/join")
    @POST
    @Produces("text/html")
    @LoggedIn
    public View joinCommunityRun(@NotNull @PathParam("slug") String slug) {
        String principal = securityContext.getUserPrincipal().getName();
        CommunityRun run = communityRunService.findBySlug(slug);
        if (run == null) {
            return View.of("/community_runs", true);
        }
        if (communityRunService.isUserAlreadyPartOfRun(slug, principal)) {
            return View.of("/community_runs/" + slug, true);
        }
        Goal goal = Goal.newCommunityRunGoal(run);
        Long goalId = goalService.save(goal, principal);
        communityRunService.addGoalToCommunityRun(slug, goalId);
        communityRunService.addRunnerToCommunityRun(slug, principal);
        return View.of("/goals/" + goalId, true);
    }
}
