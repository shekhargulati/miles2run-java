package org.miles2run.jaxrs.api.v1;

import org.apache.commons.lang3.StringUtils;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.*;
import org.miles2run.business.services.*;
import org.miles2run.business.utils.UrlUtils;
import org.miles2run.business.vo.ActivityDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by shekhargulati on 15/03/14.
 */
@Path("/api/v1/goals/{goalId}/activities")
public class ActivityResource {

    private Logger logger = LoggerFactory.getLogger(ActivityResource.class);

    @Inject
    private ActivityService activityService;
    @Inject
    private ProfileService profileService;
    @Inject
    private TwitterService twitterService;
    @Inject
    private FacebookService facebookService;
    @Inject
    private CounterService counterService;
    @Context
    private HttpServletRequest request;
    @Inject
    private GoogleService googleService;
    @Inject
    private TimelineService timelineService;
    @Context
    private SecurityContext securityContext;
    @Inject
    private GoalService goalService;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response postActivity(@PathParam("goalId") Long goalId, @Valid final Activity activity) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        logger.debug("Found goal {}", goal);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        double distanceCovered = activity.getDistanceCovered() * activity.getGoalUnit().getConversion();
        activity.setDistanceCovered(distanceCovered);
        activity.setPostedBy(profile);
        activity.setGoal(goal);
        ActivityDetails savedActivity = activityService.save(activity);
        counterService.updateDistanceCount(distanceCovered);
        counterService.updateActivitySecondsCount(activity.getDuration());
        goalService.updateTotalDistanceCoveredForAGoal(goal.getId(), savedActivity.getDistanceCovered());
        timelineService.postActivityToTimeline(savedActivity, profile, goal);
        Share share = activity.getShare();
        String message = toActivityMessage(activity, profile);
        shareActivity(message, profile, share);
        return Response.status(Response.Status.CREATED).entity(ActivityDetails.toHumanReadable(savedActivity)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public ActivityDetails get(@NotNull @PathParam("id") Long id) {
        return ActivityDetails.toHumanReadable(activityService.findById(id));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @LoggedIn
    public Response updateActivity(@PathParam("goalId") Long goalId, @PathParam("id") Long id, @Valid Activity activity) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }

        ActivityDetails existingActivity = activityService.findById(id);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        double distanceCovered = activity.getDistanceCovered() * activity.getGoalUnit().getConversion();
        activity.setDistanceCovered(distanceCovered);
        ActivityDetails updatedActivity = activityService.update(existingActivity, activity);
        double activityPreviousDistanceCovered = existingActivity.getDistanceCovered();
        double updatedDistanceCovered = distanceCovered - activityPreviousDistanceCovered;
        timelineService.updateActivity(updatedActivity, profile, goal);
        counterService.updateDistanceCount(updatedDistanceCovered);
        counterService.updateActivitySecondsCount(activity.getDuration());
        goalService.updateTotalDistanceCoveredForAGoal(goalId, updatedDistanceCovered);
        return Response.status(Response.Status.OK).entity(ActivityDetails.toHumanReadable(updatedActivity)).build();
    }

    @DELETE
    @Path("/{activityId}")
    @LoggedIn
    public Response deleteActivity(@PathParam("goalId") Long goalId, @PathParam("activityId") Long activityId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        ActivityDetails existingActivity = activityService.findById(activityId);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        activityService.delete(activityId);
        timelineService.deleteActivityFromTimeline(loggedInUser, activityId, goal);
        double activityPreviousDistanceCovered = existingActivity.getDistanceCovered();
        goalService.updateTotalDistanceCoveredForAGoal(goalId, (-1) * activityPreviousDistanceCovered);
        return Response.noContent().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/share")
    @LoggedIn
    public Response shareActivity(@PathParam("id") Long id, Activity activity) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Share share = activity.getShare();
        shareActivity(toActivityMessage(activity, profile), profile, share);
        return Response.ok().build();
    }

    private String toActivityMessage(Activity activity, Profile profile) {
        String activityUrl = UrlUtils.absoluteUrlForResourceUri(request, "/activities/{activityId}", profile.getUsername(), activity.getId());
        return new StringBuilder(profile.getFullname()).append(" ran ").append(activity.getDistanceCovered() / activity.getGoalUnit().getConversion()).append(" " + activity.getGoalUnit().toString()).append(" via @miles2runorg.").append(" Read full status here ").append(activityUrl).toString();
    }


    private void shareActivity(String message, Profile profile, Share share) {
        logger.info("in shareActivity() .. " + share);
        if (share != null) {

            for (SocialConnection socialConnection : profile.getSocialConnections()) {
                if (share.isTwitter() && socialConnection.getProvider() == SocialProvider.TWITTER) {
                    logger.info(String.format("Tweeting message : %s", message));
                    twitterService.postStatus(message, socialConnection);
                }
                if (share.isFacebook() && socialConnection.getProvider() == SocialProvider.FACEBOOK) {
                    logger.info(String.format("Posting message on Facebook wall : %s", message));
                    facebookService.postStatus(message, socialConnection);
                }

                if (share.isGooglePlus() && socialConnection.getProvider() == SocialProvider.GOOGLE_PLUS) {
                    logger.info(String.format("Posting message on G+ : %s", message));
                    googleService.postStatus(message, socialConnection);
                }
            }

        }
    }
}
