package org.miles2run.jaxrs.api.v1;

import org.hibernate.validator.constraints.NotBlank;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.ActivityJPAService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.services.redis.TimelineService;
import org.miles2run.business.vo.ActivityDetails;
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

/**
 * Created by shekhargulati on 05/06/14.
 */
@Path("/api/v1/activities")
public class TimelineResource {

    private Logger logger = LoggerFactory.getLogger(TimelineResource.class);

    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ActivityJPAService activityJPAService;

    @Path("/user_timeline")
    @GET
    @Produces("application/json")
    public Map<String, Object> userTimeline(@NotBlank @QueryParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileService.findProfile(username);
        if (profile == null) {
            return Collections.emptyMap();
        }
        page = page == 0 ? 1 : page;
        count = (count == 0 || count > 10) ? 10 : count;
        Set<String> timelineIds = timelineService.getProfileTimelineIds(username, page, count);
        if (timelineIds == null || timelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(username, timelineIds);
    }

    @Path("/home_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Map<String, Object> homeTimeline(@QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 10 ? 10 : count;
        Set<String> homeTimelineIds = timelineService.getHomeTimelineIds(loggedInUser, page, count);
        if (homeTimelineIds == null || homeTimelineIds.isEmpty()) {
            return emptyResponse();
        }
        return toTimelineResponse(loggedInUser, homeTimelineIds);
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
        List<ActivityDetails> homeTimeline = ActivityDetails.toListOfHumanReadable(activityJPAService.findAllActivitiesByIds(activityIds));
        logger.info("Found {} activities : {}", homeTimeline.size(), homeTimeline);
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", homeTimeline);
        response.put("totalItems", timelineService.totalItems(loggedInUser));
        return response;
    }


}
