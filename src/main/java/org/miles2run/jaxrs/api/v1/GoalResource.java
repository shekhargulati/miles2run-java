package org.miles2run.jaxrs.api.v1;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalType;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.ActivityService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.services.jpa.GoalJPAService;
import org.miles2run.business.services.redis.GoalRedisService;
import org.miles2run.business.vo.Progress;
import org.miles2run.jaxrs.vo.CommunityRunGoalDetails;
import org.miles2run.jaxrs.vo.DistanceGoalDetails;
import org.miles2run.jaxrs.vo.DurationGoalDetails;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 12/06/14.
 */
@Path("/api/v1/goals")
public class GoalResource {

    @Inject
    private Logger logger;

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;
    @Inject
    private GoalJPAService goalJPAService;
    @Inject
    private GoalRedisService goalRedisService;
    @Inject
    private ActivityService activityService;


    @GET
    @Produces("application/json")
    @LoggedIn
    public Response allGoal(@QueryParam("archived") boolean archived, @QueryParam("groupByType") Boolean groupByType) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        List<Goal> goals = goalJPAService.findAllGoals(loggedInUser, archived);
        if (groupByType != null && groupByType == true) {
            Map<GoalType, List<Object>> goalsByType = new HashMap<>();
            for (Goal goal : goals) {
                GoalType goalType = goal.getGoalType();
                Object specificGoal = toGoalType(goal);
                if (goalsByType.containsKey(goalType)) {
                    List<Object> goalsForType = goalsByType.get(goalType);
                    goalsForType.add(specificGoal);
                } else {
                    List<Object> goalsForType = new ArrayList<>();
                    goalsForType.add(specificGoal);
                    goalsByType.put(goalType, goalsForType);
                }
            }
            return Response.status(Response.Status.OK).entity(goalsByType).build();
        }
        return Response.status(Response.Status.OK).entity(goals).build();
    }

    private Object toGoalType(Goal goal) {
        switch (goal.getGoalType()) {
            case DURATION_GOAL:
                return toDurationGoal(goal);
            case DISTANCE_GOAL:
                return toDistanceGoal(goal);
            case COMMUNITY_RUN_GOAL:
                return toCommunityRunGoal(goal);
            default:
                return null;
        }
    }

    private CommunityRunGoalDetails toCommunityRunGoal(Goal goal) {
        return new CommunityRunGoalDetails(goal);
    }

    private DurationGoalDetails toDurationGoal(Goal goal) {
        return new DurationGoalDetails(goal);
    }

    private DistanceGoalDetails toDistanceGoal(Goal goal) {
        double percentageCompleted = percentageGoalCompleted(goal);
        return new DistanceGoalDetails(goal, percentageCompleted);
    }

    @Path("{goalId}")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Object goal(@PathParam("goalId") Long goalId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Goal goal = goalJPAService.findGoal(loggedInUser, goalId);
        return toGoalType(goal);
    }


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response createGoal(@Valid Goal goal) {
        try {
            logger.info("Goal : " + goal);
            String loggedInUser = securityContext.getUserPrincipal().getName();
            Profile profile = profileService.findProfile(loggedInUser);
            goal.setDistance(goal.getDistance() * goal.getGoalUnit().getConversion());
            Goal createdGoal = goalJPAService.save(goal, profile);
            return Response.status(Response.Status.CREATED).entity(createdGoal).build();
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
        Goal existingGoal = goalJPAService.find(goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        if (!StringUtils.equals(loggedInUser, existingGoal.getProfile().getUsername())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        goal.setDistance(goal.getDistance() * goal.getGoalUnit().getConversion());
        goalJPAService.update(goal, goalId);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{goalId}/archive")
    @PUT
    @Produces("application/json")
    @LoggedIn
    public Response archiveGoal(@PathParam("goalId") Long goalId, @QueryParam("archived") boolean archived) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal existingGoal = goalJPAService.findGoal(profile, goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        goalJPAService.updatedArchiveStatus(goalId, archived);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{goalId}/progress")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response progress(@PathParam("goalId") Long goalId) {
        String username = securityContext.getUserPrincipal().getName();
        Profile loggedInUser = profileService.findProfile(username);
        Goal goal = goalJPAService.find(goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        if (goal.getGoalType() == GoalType.DISTANCE_GOAL) {
            Progress progress = activityService.calculateUserProgressForGoal(loggedInUser, goal);
            return Response.status(Response.Status.OK).entity(progress).build();
        }
        Map<String, Object> progress = goalRedisService.getDurationGoalProgress(username, goalId, new Interval(goal.getStartDate().getTime(), goal.getEndDate().getTime()));
        return Response.status(Response.Status.OK).entity(progress).build();
    }

    public Response deleteGoal(@PathParam("goalId") Long goalId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal existingGoal = goalJPAService.find(goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        if (!StringUtils.equals(loggedInUser, existingGoal.getProfile().getUsername())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        goalJPAService.delete(goalId);
        return Response.status(Response.Status.OK).build();
    }

    private double percentageGoalCompleted(Goal goal) {
        double totalDistanceCoveredForGoal = goalRedisService.totalDistanceCoveredForGoal(goal.getId());
        double percentageCompleted = (Double.valueOf(totalDistanceCoveredForGoal) * 100 / goal.getDistance());
        return percentageCompleted > 100 ? 100 : percentageCompleted;
    }
}
