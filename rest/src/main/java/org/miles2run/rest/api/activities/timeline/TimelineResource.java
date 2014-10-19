package org.miles2run.rest.api.activities.timeline;

import org.hibernate.validator.constraints.NotBlank;
import org.jug.filters.LoggedIn;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.TimelineRepository;
import org.miles2run.core.vo.ActivityDetails;
import org.miles2run.domain.entities.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/activities")
public class TimelineResource {

    private Logger logger = LoggerFactory.getLogger(TimelineResource.class);

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
    public Map<String, Object> userTimeline(@NotBlank @QueryParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileRepository.findProfile(username);
        if (profile == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = (count == 0 || count > 10) ? 10 : count;
        Set<String> timelineIds = timelineRepository.getProfileTimelineIds(username, page, count);
        if (timelineIds == null || timelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(username, timelineIds);
    }

    private Map<String, Object> emptyResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", Collections.emptyList());
        response.put("totalItems", 0L);
        return response;

    }

    Map<String, Object> toTimelineResponse(String loggedInUser, Set<String> homeTimelineIds) {
        List<Long> activityIds = new ArrayList<>();
        for (String homeTimelineId : homeTimelineIds) {
            activityIds.add(Long.valueOf(homeTimelineId));
        }
        List<ActivityDetails> homeTimeline = ActivityDetails.toListOfHumanReadable(activityRepository.findAllActivitiesWithIds(activityIds));
        logger.info("Found {} activities : {}", homeTimeline.size(), homeTimeline);
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", homeTimeline);
        response.put("totalItems", timelineRepository.totalItems(loggedInUser));
        return response;
    }

    @Path("/home_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Map<String, Object> homeTimeline(@QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 10 ? 10 : count;
        Set<String> homeTimelineIds = timelineRepository.getHomeTimelineIds(loggedInUser, page, count);
        if (homeTimelineIds == null || homeTimelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(loggedInUser, homeTimelineIds);
    }

}
