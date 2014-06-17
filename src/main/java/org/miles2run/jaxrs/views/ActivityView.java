package org.miles2run.jaxrs.views;

import org.jug.filters.InjectPrincipal;
import org.jug.view.View;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.business.services.ActivityService;
import org.miles2run.business.vo.ActivityDetails;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 15/05/14.
 */
@Path("/profiles/{username}/activities")
public class ActivityView {

    @Inject
    private ActivityService activityService;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private Logger logger;

    @GET
    @Path("/{activityId}")
    @Produces("text/html")
    @InjectPrincipal
    @InjectProfile
    public View viewActivity(@PathParam("username") String username, @PathParam("activityId") Long activityId) {
        logger.info("Inside ActivityView. viewActivity method");
        ActivityDetails activityDetails = activityService.findByUsernameAndId(username, activityId);
        if (activityDetails == null) {
            throw new ViewResourceNotFoundException(String.format("User %s has not posted any activity with id %d", username, activityId), templateEngine);
        }
        return View.of("/activity", templateEngine).withModel("activity", activityDetails);
    }
}
