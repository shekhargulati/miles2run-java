package org.miles2run.views.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.miles2run.core.repositories.jpa.GoalRepository;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.miles2run.core.repositories.redis.CounterStatsRepository;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.miles2run.views.filters.InjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;

@Path("/")
public class IndexView {

    private final Logger logger = LoggerFactory.getLogger(IndexView.class);

    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private CounterStatsRepository counterService;
    @Context
    private HttpServletRequest request;
    @Inject
    private GoalRepository goalJPAService;
    @Inject
    private ProfileRepository profileRepository;

    @GET
    @EnableSession
    @InjectProfile
    public View index() {
        logger.info("In the IndexView index() ... ");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            String loggedInUser = (String) session.getAttribute("principal");
            Profile profile = profileRepository.findProfile(loggedInUser);
            List<Goal> goals = goalJPAService.findAllGoals(profile, false);
            return View.of("/home", templateEngine).withModel("goals", goals);
        } else {
            return View.of("/index", templateEngine).withModel("counter", counterService.currentCounter());
        }
    }
}
