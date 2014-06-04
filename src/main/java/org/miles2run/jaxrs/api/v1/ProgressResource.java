package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.Activity;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.ActivityService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.Progress;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shekhargulati on 15/03/14.
 */
@Path("/api/v1/profiles/{username}/progress")
public class ProgressResource {

    @Inject
    private ActivityService activityService;
    @Inject
    private ProfileService profileService;

    @GET
    @Produces("application/json")
    @LoggedIn
    public Response progress(@PathParam("username") String username) {
        Profile loggedInUser = profileService.findProfile(username);
        if (loggedInUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user exist with username " + username).build();
        }
        Progress progress = activityService.calculateUserProgress(loggedInUser);
        return Response.status(Response.Status.OK).entity(progress).build();
    }

    @Path("/timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response progressTimeline(@PathParam("username") String username) {
        Profile loggedInUser = profileService.findProfile(username);
        if (loggedInUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user exist with username " + username).build();
        }
        List<Activity> activities = activityService.findActivitiesWithTimeStamp(loggedInUser);
        Map<String, Long> progressTimeline = new LinkedHashMap<>();
        for (Activity activity : activities) {
            progressTimeline.put(String.valueOf(activity.getActivityDate().getTime() / 1000), activity.getDistanceCovered() / activity.getGoalUnit().getConversion());
        }
        return Response.status(Response.Status.OK).entity(progressTimeline).build();
    }

}
