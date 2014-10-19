package org.miles2run.rest.api.goals;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;
import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.GoalStatsRepository;
import org.miles2run.core.vo.Progress;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.GoalType;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.GoalDetails;
import org.miles2run.representations.GoalDetailsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Path("goals")
public class GoalResource {

    private Logger logger = LoggerFactory.getLogger(GoalResource.class);

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private GoalRepository goalRepository;
    @Inject
    private GoalStatsRepository goalStatsRepository;
    @Inject
    private ActivityRepository activityRepository;

    @GET
    @Produces("application/json")
    @LoggedIn
    public Response allGoal(@QueryParam("archived") boolean archived, @QueryParam("groupByType") Boolean groupByType) {
        Profile profile = getProfile();
        List<Goal> goals = goalRepository.findAllGoals(profile, archived);
        if (groupByType != null && groupByType) {
            return groupGoalsByType(goals);
        }
        return Response.status(Response.Status.OK).entity(goals).build();
    }

    private Response groupGoalsByType(List<Goal> goals) {
        Map<GoalType, List<Object>> goalsByType = new HashMap<>();
        for (Goal goal : goals) {
            GoalType goalType = goal.getGoalType();
            double totalDistanceCoveredForGoal = goalStatsRepository.distanceCovered(goal.getId());
            GoalDetails specificGoal = GoalDetailsFactory.toGoalType(goal, totalDistanceCoveredForGoal);
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

    private Profile getProfile() {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        return profileRepository.findProfile(loggedInUser);
    }

    @Path("{goalId}")
    @GET
    @Produces("application/json")
    @LoggedIn
    public GoalDetails goal(@PathParam("goalId") Long goalId) {
        Profile profile = getProfile();
        Goal goal = goalRepository.findGoal(profile, goalId);
        double distanceCovered = goalStatsRepository.distanceCovered(goal.getId());
        return GoalDetailsFactory.toGoalType(goal, distanceCovered);
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response createGoal(@Valid Goal goal) {
        try {
            logger.info("Goal : " + goal);
            Profile profile = getProfile();
            goal.setDistance(goal.getDistance() * goal.getGoalUnit().getConversion());
            Goal createdGoal = goalRepository.save(goal, profile);
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
        Goal existingGoal = goalRepository.find(goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        if (!StringUtils.equals(loggedInUser, existingGoal.getProfile().getUsername())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        goal.setDistance(goal.getDistance() * goal.getGoalUnit().getConversion());
        goalRepository.update(goal, goalId);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{goalId}/archive")
    @PUT
    @Produces("application/json")
    @LoggedIn
    public Response archiveGoal(@PathParam("goalId") Long goalId, @QueryParam("archived") boolean archived) {
        Profile profile = getProfile();
        Goal existingGoal = goalRepository.findGoal(profile, goalId);
        if (existingGoal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        goalRepository.updatedArchiveStatus(goalId, archived);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{goalId}/progress")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response progress(@PathParam("goalId") Long goalId, @CookieParam("timezoneoffset") int timezoneOffset) {
        String username = securityContext.getUserPrincipal().getName();
        Profile loggedInUser = profileRepository.findProfile(username);
        Goal goal = goalRepository.findGoal(loggedInUser, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        Progress progress = activityRepository.calculateUserProgressForGoal(loggedInUser, goal);
        if (goal.getGoalType() == GoalType.DISTANCE_GOAL) {
            return Response.status(Response.Status.OK).entity(progress).build();
        }
        Map<String, Object> goalProgress = goalStatsRepository.getDurationGoalProgress(username, goalId, new Interval(goal.getStartDate().getTime(), goal.getEndDate().getTime()), timezoneOffset);
        goalProgress.put("activityCount", progress.getActivityCount());
        goalProgress.put("totalDistanceCovered", progress.getTotalDistanceCovered());
        goalProgress.put("goalUnit", progress.getGoalUnit());
        return Response.status(Response.Status.OK).entity(goalProgress).build();
    }

}
