package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.services.GoalService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.TimelineService;
import org.miles2run.business.vo.ActivityDetails;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Path("/api/v1/goals/{goalId}/activities")
public class GoalTimelineResource {

    @Inject
    private Logger logger;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;
    @Context
    private SecurityContext securityContext;

    @Inject
    private GoalService goalService;

    @Path("/goal_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Map<String, Object> goalTimeline(@PathParam("goalId") Long goalId, @QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Goal goal = goalService.findGoal(loggedInUser, goalId);
        if (goal == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        List<ActivityDetails> goalTimeline = timelineService.getGoalTimeline(loggedInUser, goal, page, count);
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", goalTimeline);
        response.put("totalItems", timelineService.totalActivitiesForGoal(loggedInUser, goal));
        return response;
    }

    @Path("/user_goal_timeline")
    @GET
    @Produces("application/json")
    public Map<String, Object> getUserGoalTimeline(@PathParam("goalId") Long goalId, @QueryParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Goal goal = goalService.findGoal(username, goalId);
        if (goal == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        List<ActivityDetails> goalTimeline = timelineService.getGoalTimeline(username, goal, page, count);
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", goalTimeline);
        response.put("totalItems", timelineService.totalActivitiesForGoal(username, goal));
        return response;
    }
}
