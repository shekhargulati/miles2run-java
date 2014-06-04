package org.miles2run.jaxrs.api.v1;

import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.ActivityService;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.TimelineService;
import org.miles2run.business.vo.ActivityDetails;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 05/06/14.
 */
@Path("/api/v1/timeline")
public class TimelineResource {

    @Inject
    private Logger logger;
    @Inject
    private TimelineService timelineService;
    @Inject
    private ProfileService profileService;

    @Path("/{username}/profile")
    @GET
    @Produces("application/json")
    public List<ActivityDetails> profileTimeline(@PathParam("username") String username, @QueryParam("page") int page, @QueryParam("count") int count) {
        Profile profile = profileService.findProfile(username);
        if (profile == null) {
            return Collections.emptyList();
        }
        page = page == 0 ? 1 : page;
        count = count == 0 || count > 50 ? 30 : count;
        return timelineService.getProfileTimeline(username, page, count);
    }
}
