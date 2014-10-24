package org.miles2run.rest.api.activities.timeline;

import org.hibernate.validator.constraints.NotBlank;
import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Profile;
import org.miles2run.rest.representations.TimelineRepresentation;

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
        Set<String> timelineIds = timelineRepository.getUserTimeline(username, page, count);
        if (timelineIds.isEmpty()) {
            return TimelineRepresentation.empty();
        }
        return toTimelineRepresentation(username, timelineIds);
    }

    private TimelineRepresentation toTimelineRepresentation(String loggedInUser, Set<String> homeTimelineIds) {
        List<Long> activityIds = homeTimelineIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Activity> activities = activityRepository.findAllActivitiesWithIds(activityIds);
        Long activityCount = timelineRepository.totalItems(loggedInUser);
        return TimelineRepresentation.with(activityCount, activities);
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
        return toTimelineRepresentation(loggedInUser, homeTimelineIds);
    }

}
