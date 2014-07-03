package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.ChartService;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.TimelineService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

/**
 * Created by shekhargulati on 06/06/14.
 */
@Path("/api/v1/goal_aggregate/{goalId}")
public class DashboardResource {

    @Context
    private SecurityContext securityContext;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;
    @Inject
    private GoalService goalService;
    @Inject
    private ChartService chartService;

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/distance_and_pace")
    public List<Object[]> getDistanceAndPaceOverTime(@PathParam("goalId") Long goalId, @QueryParam("interval") String interval, @QueryParam("days") int days, @QueryParam("months") int months) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        days = days == 0 || days > 60 ? 60 : days;
        months = months == 0 || months > 12 ? 6 : months;
        interval = interval == null ? "day" : interval;
        switch (interval) {
            case "day":
                return chartService.distanceAndPaceOverNDays(profile.getUsername(), goal, days);
            case "month":
                return timelineService.distanceAndPaceOverNMonths(profile, goal, interval, months);
            default:
                return chartService.distanceAndPaceOverNDays(profile.getUsername(), goal, days);
        }
    }


    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/distance_and_activity")
    public List<Object[]> getDistanceAndActivityCountOverTime(@PathParam("goalId") Long goalId, @QueryParam("months") int months) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        months = months == 0 || months > 12 ? 6 : months;
        return timelineService.distanceAndActivityCountOverNMonths(profile, goal, months);
    }

    @Path("/activity_calendar")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response activityCalendar(@PathParam("goalId") Long goalId, @QueryParam("months") int nMonths) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalService.findGoal(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        nMonths = nMonths == 0 || nMonths > 12 ? 3 : nMonths;
        Map<String, Double> data = chartService.getActivitiesPerformedInLastNMonthsForGoal(profile.getUsername(), goal, nMonths);
        return Response.status(Response.Status.OK).entity(data).build();
    }
}
