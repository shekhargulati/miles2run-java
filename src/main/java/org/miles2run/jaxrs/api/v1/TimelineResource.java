package org.miles2run.jaxrs.api.v1;

import org.hibernate.validator.constraints.NotBlank;
import org.jug.filters.LoggedIn;
import org.miles2run.business.domain.jpa.Profile;
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
 * Created by shekhargulati on 05/06/14.
 */
@Path("/api/v1/activities")
public class TimelineResource {

    @Inject
    private Logger logger;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;
    @Context
    private SecurityContext securityContext;

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
        List<ActivityDetails> homeTimeline = timelineService.getProfileTimeline(username, page, count);
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", homeTimeline);
        response.put("totalItems", timelineService.totalItems(username));
        return response;
    }

    @Path("/home_timeline")
    @GET
    @Produces("application/json")
    @LoggedIn
    public Map<String, Object> homeTimeline(@QueryParam("page") int page, @QueryParam("count") int count) {
        String loggedInUser = securityContext.getUserPrincipal().getName();
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 10 : count;
        List<ActivityDetails> homeTimeline = timelineService.getHomeTimeline(loggedInUser, page, count);
        Map<String, Object> response = new HashMap<>();
        response.put("timeline", homeTimeline);
        response.put("totalItems", timelineService.totalItems(loggedInUser));
        return response;
    }


}
