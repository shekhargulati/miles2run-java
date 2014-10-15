package org.miles2run.jaxrs.api.v1;

import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.ActivityJPAService;
import org.miles2run.business.services.jpa.GoalJPAService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.services.redis.TimelineService;
import org.miles2run.business.vo.ActivityDetails;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
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
    private GoalJPAService goalJPAService;
    @Inject
    private ActivityJPAService activityJPAService;

    @Path("/goal_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Map<String, Object> goalTimeline(@PathParam("goalId") Long goalId, @QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileService.findProfile(loggedInUser);
        Goal goal = goalJPAService.findGoal(profile, goalId);
        if (goal == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        Set<String> timelineIds = timelineService.getGoalTimelineIds(loggedInUser, goal, page, count);
        if (timelineIds == null || timelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(loggedInUser, goal, timelineIds);
    }

    Map<String, Object> toTimelineResponse(String loggedInUser, Goal goal, Set<String> homeTimelineIds) {
        List<Long> activityIds = new ArrayList<>();
        for (String homeTimelineId : homeTimelineIds) {
            activityIds.add(Long.valueOf(homeTimelineId));
        }
        List<ActivityDetails> homeTimeline = ActivityDetails.toListOfHumanReadable(activityJPAService.findAllActivitiesByIds(activityIds));
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", homeTimeline);
        response.put("totalItems", timelineService.totalActivitiesForGoal(loggedInUser, goal));
        return response;
    }

    private Map<String, Object> emptyResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", Collections.emptyList());
        response.put("totalItems", 0L);
        return response;

    }

    @Path("/user_goal_timeline")
    @GET
    @Produces("application/json")
    public Map<String, Object> getUserGoalTimeline(@PathParam("goalId") Long goalId, @QueryParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileService.findProfile(username);
        Goal goal = goalJPAService.findGoal(profile, goalId);
        if (goal == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        Set<String> timelineIds = timelineService.getGoalTimelineIds(username, goal, page, count);
        if (timelineIds == null || timelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(username, goal, timelineIds);
    }
}
