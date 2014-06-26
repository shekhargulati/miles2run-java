package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.TimelineService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

/**
 * Created by shekhargulati on 06/06/14.
 */
@Path("/api/v1/goals/{goalId}/dashboard")
public class DashboardResource {

    @Context
    private SecurityContext securityContext;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;
    @Inject
    private GoalService goalService;


    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/charts/distance")
    public List<Map<String, Object>> getDataForDistanceCovered(@PathParam("goalId") Long goalId, @QueryParam("interval") String interval, @QueryParam("days") int days, @QueryParam("months") int months) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        days = days == 0 || days > 60 ? 30 : days;
        months = months == 0 || months > 12 ? 6 : months;
        switch (interval) {
            case "day":
                return timelineService.distanceAndPaceOverLastNDays(profile, goal, interval, days);
            case "month":
                return timelineService.distanceAndPaceOverLastNMonths(profile, goal, interval, months);
            default:
                return timelineService.distanceAndPaceOverLastNDays(profile, goal, interval, days);
        }
    }

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/charts/distanceandpace")
    public List<Object[]> getDistanceAndPaceOverTime(@PathParam("goalId") Long goalId, @QueryParam("interval") String interval, @QueryParam("days") int days, @QueryParam("months") int months) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        days = days == 0 || days > 60 ? 60 : days;
        months = months == 0 || months > 12 ? 6 : months;
        interval = interval == null ? "day" : interval;
        switch (interval) {
            case "day":
                return timelineService.distanceAndPaceOverNDays(profile, goal, interval, days);
            case "month":
                return timelineService.distanceAndPaceOverNMonths(profile, goal, interval, months);
            default:
                return timelineService.distanceAndPaceOverNDays(profile, goal, interval, days);
        }


    }

}
