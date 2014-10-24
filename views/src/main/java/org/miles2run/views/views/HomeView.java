package org.miles2run.views.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.views.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("/home")
public class HomeView {

    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileRepository profileRepository;
    @Inject
    private TemplateEngine templateEngine;
    @Inject
    private GoalRepository goalRepository;

    @GET
    @Produces("text/html")
    @LoggedIn
    @InjectProfile
    public View showAllGoals() {
        String loggedInUser = securityContext.getUserPrincipal().getName();
//        Profile profile = profileRepository.findByUsername(loggedInUser);
//        List<Goal> goals = goalRepository.findAll(profile, false);
        return View.of("/home", templateEngine);
    }

}
