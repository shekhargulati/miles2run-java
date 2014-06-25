package org.miles2run.jaxrs.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.miles2run.business.domain.Goal;
import org.miles2run.business.services.CounterService;
import org.miles2run.business.services.GoalService;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.miles2run.business.domain.Counter;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 19/03/14.
 */
@Path("/")
public class IndexView {

    @Inject
    private Logger logger;

    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private CounterService counterService;
    @Context
    private HttpServletRequest request;
    @Inject
    private GoalService goalService;

    @GET
    @EnableSession
    @InjectProfile
    public View index() {
        logger.info("In the IndexView index() ... ");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            String loggedInUser = (String) session.getAttribute("principal");
            List<Goal> goals = goalService.findAllGoals(loggedInUser, false);
            return View.of("/home", templateEngine).withModel("goals", goals);
        } else {
            return View.of("/index", templateEngine).withModel("counter", counterService.currentCounter());
        }
    }
}
