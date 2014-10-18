package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.GoalJPAService;
import org.miles2run.shared.repositories.ProfileRepository;
import org.miles2run.business.services.redis.GoalAggregationService;
import org.miles2run.business.services.redis.TimelineService;

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
public class GoalAggregateResource {

    @Context
    private SecurityContext securityContext;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private GoalJPAService goalJPAService;
    @Inject
    private GoalAggregationService goalAggregationService;

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/distance_and_pace")
    public List<Object[]> getDistanceAndPaceOverTime(@PathParam("goalId") Long goalId, @QueryParam("interval") String interval, @QueryParam("days") int days, @QueryParam("months") int months, @CookieParam("timezoneoffset") int timezoneOffset) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalJPAService.findGoal(profile, goalId);
        days = days == 0 || days > 60 ? 60 : days;
        months = months == 0 || months > 12 ? 6 : months;
        interval = interval == null ? "day" : interval;
        switch (interval) {
            case "day":
                return goalAggregationService.distanceAndPaceOverNDays(profile.getUsername(), goal, days, timezoneOffset);
            case "month":
                return timelineService.distanceAndPaceOverNMonths(profile, goal, interval, months);
            default:
                return goalAggregationService.distanceAndPaceOverNDays(profile.getUsername(), goal, days, timezoneOffset);
        }
    }

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/distance_and_activity")
    public List<Object[]> getDistanceAndActivityCountOverTime(@PathParam("goalId") Long goalId, @QueryParam("months") int months) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalJPAService.findGoal(profile, goalId);
        months = months == 0 || months > 12 ? 6 : months;
        return timelineService.distanceAndActivityCountOverNMonths(profile, goal, months);
    }

    @Path("/activity_calendar")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response activityCalendar(@PathParam("goalId") Long goalId, @QueryParam("months") int nMonths) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalJPAService.findGoal(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        nMonths = nMonths == 0 || nMonths > 12 ? 3 : nMonths;
        Map<String, Double> data = goalAggregationService.getActivitiesPerformedInLastNMonthsForGoal(profile.getUsername(), goal, nMonths);
        return Response.status(Response.Status.OK).entity(data).build();
    }
}
