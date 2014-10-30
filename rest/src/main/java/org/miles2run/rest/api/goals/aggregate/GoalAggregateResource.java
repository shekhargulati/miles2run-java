package org.miles2run.rest.api.goals.aggregate;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.GoalAggregationRepository;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

@Path("goal_aggregate/{goalId}")
public class GoalAggregateResource {

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private GoalRepository goalRepository;
    @Inject
    private GoalAggregationRepository goalAggregationRepository;

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/distance_and_pace")
    public List<Object[]> getDistanceAndPaceOverTime(@PathParam("goalId") Long goalId, @QueryParam("interval") String interval, @QueryParam("days") int days, @QueryParam("months") int months, @CookieParam("timezoneoffset") int timezoneOffset) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        days = days == 0 || days > 60 ? 60 : days;
        months = months == 0 || months > 12 ? 6 : months;
        interval = interval == null ? "day" : interval;
        switch (interval) {
            case "day":
                return goalAggregationRepository.distanceAndPaceOverNDays(profile.getUsername(), goal, days, timezoneOffset);
            case "month":
                return goalAggregationRepository.distanceAndPaceOverNMonths(profile, goal, interval, months);
            default:
                return goalAggregationRepository.distanceAndPaceOverNDays(profile.getUsername(), goal, days, timezoneOffset);
        }
    }

    @GET
    @LoggedIn
    @Produces("application/json")
    @Path("/distance_and_activity")
    public List<Object[]> getDistanceAndActivityCountOverTime(@PathParam("goalId") Long goalId, @QueryParam("months") int months) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        months = months == 0 || months > 12 ? 6 : months;
        return goalAggregationRepository.distanceAndActivityCountOverNMonths(profile, goal, months);
    }

    @Path("/activity_calendar")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Response activityCalendar(@PathParam("goalId") Long goalId, @QueryParam("months") int nMonths) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        if (goal == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No goal exists with id " + goalId).build();
        }
        nMonths = nMonths == 0 || nMonths > 12 ? 3 : nMonths;
        Map<String, Double> data = goalAggregationRepository.getActivitiesPerformedInLastNMonthsForGoal(profile.getUsername(), goal, nMonths);
        return Response.status(Response.Status.OK).entity(data).build();
    }
}
