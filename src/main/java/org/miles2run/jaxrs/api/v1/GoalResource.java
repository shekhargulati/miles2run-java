package org.miles2run.jaxrs.api.v1;

import org.apache.commons.lang3.StringUtils;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.ActivityService;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.Progress;
import org.miles2run.jaxrs.vo.GoalDetails;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 12/06/14.
 */
@Path("/api/v1/goals")
public class GoalResource {

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;
    @Inject
    private GoalService goalService;
    @Inject
    private Logger logger;
    @Inject
    private ActivityService activityService;


    @GET
    @Produces("application/json")
    @LoggedIn
    public List<GoalDetails> allGoal(@QueryParam("archived") boolean archived) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        List<Goal> goals = goalService.findAllGoals(loggedInUser, archived);

        List<GoalDetails> goalsDetails = new ArrayList<>();
        for (Goal goal : goals) {
            long percentageCompleted = percentageGoalCompleted(goal);
            goalsDetails.add(new GoalDetails(goal, percentageCompleted));
        }
        return goalsDetails;
    }

    @Path("{goalId}")
    @GET
    @Produces("application/json")
    @LoggedIn
    public GoalDetails goal(@PathParam("goalId") Long goalId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Goal goal = goalService.findGoal(loggedInUser, goalId);
        long percentageCompleted = percentageGoalCompleted(goal);
        return new GoalDetails(goal, percentageCompleted);
    }


    @POST
    @Produces("application/json")
    @LoggedIn
    public Response createGoal(@Valid Goal goal) {
        try {
            logger.info("Goal : " + goal);
            String loggedInUser = securityContext.getUserPrincipal().getName();
            Profile profile = profileService.findProfile(loggedInUser);
            goal.setGoal(goal.getGoal() * goal.getGoalUnit().getConversion());
            goalService.save(goal, profile);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @Path("{goalId}")
    @PUT
    @Produces("application/json")
    @LoggedIn
    public Response updateGoal(@PathParam("goalId") Long goalId, @Valid Goal goal) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal existingGoal = goalService.find(goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        if (!StringUtils.equals(loggedInUser, existingGoal.getProfile().getUsername())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        goal.setGoal(goal.getGoal() * goal.getGoalUnit().getConversion());
        goalService.update(goal, goalId);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{goalId}/archive")
    @PUT
    @Produces("application/json")
    @LoggedIn
    public Response archiveGoal(@PathParam("goalId") Long goalId, @QueryParam("archived") boolean archived) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal existingGoal = goalService.findGoal(profile, goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        goalService.updatedArchiveStatus(goalId, archived);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{goalId}/progress")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response progress(@PathParam("goalId") Long goalId) {
        String username = securityContext.getUserPrincipal().getName();
        Profile loggedInUser = profileService.findProfile(username);
        Progress progress = activityService.calculateUserProgressForGoal(loggedInUser, goalId);
        return Response.status(Response.Status.OK).entity(progress).build();
    }

    public Response deleteGoal(@PathParam("goalId") Long goalId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal existingGoal = goalService.find(goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        if (!StringUtils.equals(loggedInUser, existingGoal.getProfile().getUsername())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        goalService.delete(goalId);
        return Response.status(Response.Status.OK).build();
    }

    private long percentageGoalCompleted(Goal goal) {
        long totalDistanceCoveredForGoal = goalService.totalDistanceCoveredForGoal(goal.getId());
        double percentageCompleted = (Double.valueOf(totalDistanceCoveredForGoal) * 100 / goal.getGoal());
        long percentage = Double.valueOf(percentageCompleted).longValue();
        percentage = percentage > 100 ? 100 : percentage;
        return percentage;
    }
}
