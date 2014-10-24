package org.miles2run.rest.api.activities;

import org.apache.commons.lang3.StringUtils;
import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.CommunityRunStatsRepository;
import org.miles2run.core.repositories.redis.CounterStatsRepository;
import org.miles2run.core.repositories.redis.GoalStatsRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.CommunityRunGoal;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.ActivityRepresentation;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("goals/{goalId}/activities")
public class ActivityResource {

    @Inject
    private ActivityRepository activityRepository;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private CounterStatsRepository counterStatsRepository;
    @Inject
    private TimelineRepository timelineRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private GoalRepository goalRepository;
    @Inject
    private CommunityRunStatsRepository communityRunStatsRepository;
    @Inject
    private GoalStatsRepository goalStatsRepository;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response postActivity(@PathParam("goalId") Long goalId, @Valid final ActivityRequest activityRequest) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        Activity activity = activityRequest.toActivity(profile, goal);
        Activity savedActivity = activityRepository.save(activity);
        updateStats(profile, goal, activity, savedActivity);
        return Response.status(Response.Status.CREATED).entity(ActivityRepresentation.from(savedActivity)).build();
    }

    private void updateStats(Profile profile, Goal goal, Activity activity, Activity savedActivity) {
        counterStatsRepository.updateDistanceCount(activity.getDistanceCovered());
        counterStatsRepository.updateActivitySecondsCount(activity.getDuration());
        goalStatsRepository.updateTotalDistanceCoveredForAGoal(goal.getId(), savedActivity.getDistanceCovered());
        timelineRepository.postActivityToTimeline(savedActivity, profile, goal);
        if (goal instanceof CommunityRunGoal) {
            communityRunStatsRepository.updateCommunityRunStats(profile.getUsername(), (CommunityRunGoal) goal, activity);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public ActivityRepresentation get(@NotNull @PathParam("id") Long id) {
        return ActivityRepresentation.from(activityRepository.get(id));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @LoggedIn
    public Response updateActivity(@PathParam("goalId") Long goalId, @PathParam("id") Long id, @Valid ActivityRequest activityRequest) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }

        Activity existingActivity = activityRepository.get(id);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getPostedBy().getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        double oldDistanceCovered = existingActivity.getDistanceCovered();
        long oldDuration = existingActivity.getDuration();
        updateExistingActivity(existingActivity, activityRequest);
        Activity updatedActivity = activityRepository.update(existingActivity);
        timelineRepository.updateActivity(updatedActivity, profile, goal);
        updateStats(goal, updatedActivity.getDistanceCovered(), updatedActivity.getDuration(), oldDistanceCovered, oldDuration);
        return Response.status(Response.Status.OK).entity(ActivityRepresentation.from(updatedActivity)).build();
    }

    private void updateExistingActivity(Activity existingActivity, ActivityRequest activityRequest) {
        double distanceCovered = activityRequest.getDistanceCovered() * activityRequest.getGoalUnit().getConversion();
        existingActivity.setGoalUnit(activityRequest.getGoalUnit());
        existingActivity.setDistanceCovered(distanceCovered);
        existingActivity.setActivityDate(activityRequest.getActivityDate());
        existingActivity.setDuration(activityRequest.getDuration());
        existingActivity.setStatus(activityRequest.getStatus());
    }

    void updateStats(Goal goal, double existingDistanceCovered, long existingDuration, double distanceCovered, long duration) {
        Long goalId = goal.getId();
        double updatedDistanceCovered = distanceCovered - existingDistanceCovered;
        counterStatsRepository.updateDistanceCount(updatedDistanceCovered);
        long updatedDuration = duration - existingDuration;
        counterStatsRepository.updateActivitySecondsCount(updatedDuration);
        goalStatsRepository.updateTotalDistanceCoveredForAGoal(goalId, updatedDistanceCovered);
        if (goal instanceof CommunityRunGoal) {
            communityRunStatsRepository.updateCommunityRunDistanceAndDurationStats(((CommunityRunGoal) goal).getCommunityRun().getSlug(), updatedDistanceCovered, updatedDuration);
        }
    }

    @DELETE
    @Path("/{activityId}")
    @LoggedIn
    public Response deleteActivity(@PathParam("goalId") Long goalId, @PathParam("activityId") Long activityId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        Activity existingActivity = activityRepository.get(activityId);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getPostedBy().getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        activityRepository.delete(activityId);
        timelineRepository.deleteActivityFromTimeline(loggedInUser, activityId, goal);
        updateStats(goal, existingActivity.getDistanceCovered(), existingActivity.getDuration(), 0.0d, 0L);
        return Response.noContent().build();
    }

}
