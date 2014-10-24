package org.miles2run.views.views;

import org.jug.filters.InjectPrincipal;
import org.jug.view.View;
import org.jug.view.ViewResourceNotFoundException;
import org.miles2run.core.repositories.jpa.ActivityRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.Profile;
import org.miles2run.representations.ActivityRepresentation;
import org.miles2run.views.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/profiles/{username}/activities")
public class ActivityView {

    @Inject
    private ActivityRepository activityRepository;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private ProfileRepository profileRepository;

    @GET
    @Path("/{activityId}")
    @Produces("text/html")
    @InjectPrincipal
    @InjectProfile
    public View viewActivity(@PathParam("username") String username, @PathParam("activityId") Long activityId) {
        Profile profile = profileRepository.findByUsername(username);
        Activity activity = activityRepository.findByProfileAndId(profile, activityId);
        if (activity == null) {
            throw new ViewResourceNotFoundException(String.format("User %s has not posted any activity with id %d", username, activityId), templateEngine);
        }
        return View.of("/activity", templateEngine).withModel("activity", ActivityRepresentation.from(activity));
    }
}
