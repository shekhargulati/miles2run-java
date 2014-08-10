package org.miles2run.jaxrs.views;

import org.jug.filters.InjectPrincipal;
import org.jug.view.View;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.ActivityJPAService;
import org.miles2run.business.services.jpa.ProfileService;
import org.miles2run.business.vo.ActivityDetails;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Created by shekhargulati on 15/05/14.
 */
@Path("/profiles/{username}/activities")
public class ActivityView {

    private final Logger logger = LoggerFactory.getLogger(ActivityView.class);

    @Inject
    private ActivityJPAService activityJPAService;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private ProfileService profileService;

    @GET
    @Path("/{activityId}")
    @Produces("text/html")
    @InjectPrincipal
    @InjectProfile
    public View viewActivity(@PathParam("username") String username, @PathParam("activityId") Long activityId) {
        Profile profile = profileService.findProfile(username);
        ActivityDetails activityDetails = activityJPAService.findByUsernameAndId(profile, activityId);
        if (activityDetails == null) {
            throw new ViewResourceNotFoundException(String.format("User %s has not posted any activity with id %d", username, activityId), templateEngine);
        }
        return View.of("/activity", templateEngine).withModel("activity", ActivityDetails.toHumanReadable(activityDetails));
    }
}
