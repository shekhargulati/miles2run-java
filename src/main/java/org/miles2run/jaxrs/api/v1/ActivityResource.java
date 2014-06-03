package org.miles2run.jaxrs.api.v1;

import org.apache.commons.lang3.StringUtils;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.*;
import org.miles2run.business.services.*;
import org.miles2run.business.utils.UrlUtils;
import org.miles2run.business.vo.ActivityDetails;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 15/03/14.
 */
@Path("/api/v1/profiles/{username}/activities")
public class ActivityResource {

    @Inject
    private Logger logger;

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

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @LoggedIn
    public Response postActivity(@PathParam("username") String username, @Valid final Activity activity) {
        Profile profile = profileService.findProfile(username);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user exists with username " + username).build();
        }
        long distanceCovered = activity.getDistanceCovered() * activity.getGoalUnit().getConversion();
        activity.setDistanceCovered(distanceCovered);
        Activity savedActivity = activityService.save(activity, profile);
        counterService.updateRunCounter(distanceCovered);
        timelineService.postActivityToTimeline(savedActivity, profile);
        Share share = activity.getShare();
        String message = toActivityMessage(activity, profile);
        shareActivity(message, profile, share);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Produces("application/json")
    @LoggedIn
    public List<ActivityDetails> homeTimeline(@PathParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileService.findProfile(username);
        if (profile == null) {
            return Collections.emptyList();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 30 : count;
        return timelineService.getHomeTimeline(username, page, count);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @LoggedIn
    public ActivityDetails get(@NotNull @PathParam("id") Long id) {
        return activityService.findById(id);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @LoggedIn
    public Response updateActivity(@PathParam("id") Long id, @Valid Activity activity) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        ActivityDetails existingActivity = activityService.findById(id);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        long distanceCovered = activity.getDistanceCovered() * activity.getGoalUnit().getConversion();
        long activityPreviousDistanceCovered = existingActivity.getDistanceCovered();
        long updatedRunCounter = distanceCovered - activityPreviousDistanceCovered;
        activity.setDistanceCovered(distanceCovered);
        ActivityDetails updatedActivity = activityService.update(existingActivity, activity);
        timelineService.updateActivity(updatedActivity);
        counterService.updateRunCounter(updatedRunCounter);
        return Response.status(Response.Status.OK).entity(updatedActivity).build();
    }

    @DELETE
    @Path("/{activityId}")
    @LoggedIn
    public Response deleteActivity(@PathParam("activityId") Long activityId) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        ActivityDetails existingActivity = activityService.findById(activityId);
        if (existingActivity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String activityBy = existingActivity.getUsername();
        if (!StringUtils.equals(loggedInUser, activityBy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        activityService.delete(activityId);
        timelineService.deleteActivityFromTimeline(loggedInUser, activityId);
        return Response.noContent().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/share")
    @LoggedIn
    public Response shareActivity(@PathParam("username") String username, @PathParam("id") Long id, Activity activity) {
        Profile profile = profileService.findProfile(username);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user exists with username " + username).build();
        }
        Share share = activity.getShare();
        shareActivity(toActivityMessage(activity, profile), profile, share);
        return Response.ok().build();
    }

    private String toActivityMessage(Activity activity, Profile profile) {
        String activityUrl = UrlUtils.absoluteUrlForResourceUri(request, "/profiles/{username}/activities/{activityId}", profile.getUsername(), activity.getId());
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
