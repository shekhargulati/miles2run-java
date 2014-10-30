package org.miles2run.rest.api.activities.timeline;

import org.hibernate.validator.constraints.NotBlank;
import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.TimelineRepresentation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/activities")
public class TimelineResource {

    @Inject
    private TimelineRepository timelineRepository;
    @Inject
    private ProfileRepository profileRepository;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ActivityRepository activityRepository;

    @Path("/user_timeline")
    @GET
    @Produces("application/json")
    public TimelineRepresentation userTimeline(@NotBlank @QueryParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileRepository.findByUsername(username);
        if (profile == null) {
            return TimelineRepresentation.empty();
        }
        page = page == 0 ? 1 : page;
        count = (count == 0 || count > 10) ? 10 : count;
        Set<String> timelineIds = timelineRepository.getUserTimelineIds(username, page, count);
        if (timelineIds.isEmpty()) {
            return TimelineRepresentation.empty();
        }
        List<Activity> activities = getActivities(timelineIds);
        Long activityCount = timelineRepository.userTimelineActivityCount(username);
        return TimelineRepresentation.with(activityCount, activities);
    }

    private List<Activity> getActivities(Set<String> homeTimelineIds) {
        List<Long> activityIds = homeTimelineIds.stream().map(Long::valueOf).collect(Collectors.toList());
        return activityRepository.findAllActivitiesWithIds(activityIds);
    }

    @Path("/home_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public TimelineRepresentation homeTimeline(@QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 10 ? 10 : count;
        Set<String> homeTimelineIds = timelineRepository.getHomeTimelineIds(loggedInUser, page, count);
        if (homeTimelineIds.isEmpty()) {
            return TimelineRepresentation.empty();
        }
        List<Activity> activities = getActivities(homeTimelineIds);
        Long activityCount = timelineRepository.homeTimelineActivityCount(loggedInUser);
        return TimelineRepresentation.with(activityCount, activities);
    }

}
