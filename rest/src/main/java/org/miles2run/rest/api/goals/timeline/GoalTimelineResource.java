package org.miles2run.rest.api.goals.timeline;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.core.vo.ActivityDetails;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.logging.Logger;

@Path("goals/{goalId}/activities")
public class GoalTimelineResource {

    @Inject
    private Logger logger;
    @Inject
    private TimelineRepository timelineRepository;
    @Inject
    private ProfileRepository profileRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private GoalRepository goalRepository;
    @Inject
    private ActivityRepository activityRepository;

    @Path("/goal_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Map<String, Object> goalTimeline(@PathParam("goalId") Long goalId, @QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findProfile(loggedInUser);
        Goal goal = goalRepository.findGoal(profile, goalId);
        if (goal == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        Set<String> timelineIds = timelineRepository.getGoalTimelineIds(loggedInUser, goal, page, count);
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
        List<ActivityDetails> homeTimeline = ActivityDetails.toListOfHumanReadable(activityRepository.findAllActivitiesWithIds(activityIds));
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", homeTimeline);
        response.put("totalItems", timelineRepository.totalActivitiesForGoal(loggedInUser, goal));
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
        Profile profile = profileRepository.findProfile(username);
        Goal goal = goalRepository.findGoal(profile, goalId);
        if (goal == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        Set<String> timelineIds = timelineRepository.getGoalTimelineIds(username, goal, page, count);
        if (timelineIds == null || timelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(username, goal, timelineIds);
    }
}
