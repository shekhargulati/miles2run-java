package org.miles2run.rest.api.goals.timeline;

import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.TimelineRepresentation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("goals/{goalId}/activities")
public class GoalTimelineResource {

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
    public TimelineRepresentation goalTimeline(@PathParam("goalId") Long goalId, @QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        Profile profile = profileRepository.findByUsername(loggedInUser);
        Goal goal = goalRepository.find(profile, goalId);
        if (goal == null) {
            return TimelineRepresentation.empty();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        Set<String> timelineIds = timelineRepository.getGoalTimelineIds(loggedInUser, goal, page, count);
        if (timelineIds.isEmpty()) {
            return TimelineRepresentation.empty();
        }
        return toTimelineRepresentation(loggedInUser, timelineIds, goal);
    }


    private TimelineRepresentation toTimelineRepresentation(String loggedInUser, Set<String> homeTimelineIds, Goal goal) {
        List<Long> activityIds = homeTimelineIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Activity> activities = activityRepository.findAllActivitiesWithIds(activityIds);
        Long activityCount = timelineRepository.totalActivitiesForGoal(loggedInUser, goal);
        return TimelineRepresentation.with(activityCount, activities);
    }

    @Path("/user_goal_timeline")
    @GET
    @Produces("application/json")
    public TimelineRepresentation getUserGoalTimeline(@PathParam("goalId") Long goalId, @QueryParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileRepository.findByUsername(username);
        Goal goal = goalRepository.find(profile, goalId);
        if (goal == null) {
            return TimelineRepresentation.empty();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        Set<String> timelineIds = timelineRepository.getGoalTimelineIds(username, goal, page, count);
        if (timelineIds.isEmpty()) {
            return TimelineRepresentation.empty();
        }
        return toTimelineRepresentation(username, timelineIds, goal);
    }

}
