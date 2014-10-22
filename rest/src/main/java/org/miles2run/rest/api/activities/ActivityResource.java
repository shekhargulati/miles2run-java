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
import org.miles2run.core.vo.ActivityDetails;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.GoalType;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.ActivityRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger logger = LoggerFactory.getLogger(ActivityResource.class);

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
        logger.info("Posting Activity {}", activityRequest);
        Activity activity = activityRequest.toActivity();
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalRepository.findGoal(profile, goalId);
        logger.debug("Found goal {}", goal);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        activity.setPostedBy(profile);
        activity.setGoal(goal);
        Long persistedActivityId = activityRepository.save(activity);
        ActivityDetails savedActivity = activityRepository.findById(persistedActivityId);
        counterStatsRepository.updateDistanceCount(activity.getDistanceCovered());
        counterStatsRepository.updateActivitySecondsCount(activity.getDuration());
        goalStatsRepository.updateTotalDistanceCoveredForAGoal(goal.getId(), savedActivity.getDistanceCovered());
        timelineRepository.postActivityToTimeline(savedActivity, profile, goal);
        if (goal.getGoalType() == GoalType.COMMUNITY_RUN_GOAL) {
            communityRunStatsRepository.updateCommunityRunStats(loggedInUser, goal, activity);
        }
        return Response.status(Response.Status.CREATED).entity(ActivityDetails.toHumanReadable(savedActivity)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public ActivityDetails get(@NotNull @PathParam("id") Long id) {
        return ActivityDetails.toHumanReadable(activityRepository.findById(id));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @LoggedIn
    public Response updateActivity(@PathParam("goalId") Long goalId, @PathParam("id") Long id, @Valid Activity activity) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalRepository.findGoal(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }

        ActivityDetails existingActivity = activityRepository.findById(id);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        double distanceCovered = activity.getDistanceCovered() * activity.getGoalUnit().getConversion();
        activity.setDistanceCovered(distanceCovered);
        ActivityDetails updatedActivity = activityRepository.update(existingActivity, activity);
        timelineRepository.updateActivity(updatedActivity, profile, goal);
        updateStats(goal, existingActivity.getDistanceCovered(), existingActivity.getDuration(), activity.getDistanceCovered(), activity.getDuration());
        return Response.status(Response.Status.OK).entity(ActivityDetails.toHumanReadable(updatedActivity)).build();
    }

    void updateStats(Goal goal, double existingDistanceCovered, long existingDuration, double distanceCovered, long duration) {
        Long goalId = goal.getId();
        double updatedDistanceCovered = distanceCovered - existingDistanceCovered;
        counterStatsRepository.updateDistanceCount(updatedDistanceCovered);
        long updatedDuration = duration - existingDuration;
        counterStatsRepository.updateActivitySecondsCount(updatedDuration);
        goalStatsRepository.updateTotalDistanceCoveredForAGoal(goalId, updatedDistanceCovered);
        if (goal.getGoalType() == GoalType.COMMUNITY_RUN_GOAL) {
            communityRunStatsRepository.updateCommunityRunDistanceAndDurationStats(goal.getCommunityRun().getSlug(), updatedDistanceCovered, updatedDuration);
        }
    }

    @DELETE
    @Path("/{activityId}")
    @LoggedIn
    public Response deleteActivity(@PathParam("goalId") Long goalId, @PathParam("activityId") Long activityId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalRepository.findGoal(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        ActivityDetails existingActivity = activityRepository.findById(activityId);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        activityRepository.delete(activityId);
        timelineRepository.deleteActivityFromTimeline(loggedInUser, activityId, goal);
        updateStats(goal, existingActivity.getDistanceCovered(), existingActivity.getDuration(), 0.0d, 0L);
        return Response.noContent().build();
    }

}
