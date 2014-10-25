package org.miles2run.views.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.miles2run.core.repositories.redis.CounterStatsRepository;
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

@Path("/")
public class IndexView {

    private final Logger logger = LoggerFactory.getLogger(IndexView.class);

    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private CounterStatsRepository counterStatsRepository;

    @Context
    private HttpServletRequest request;


    @GET
    @EnableSession
    @InjectProfile
    public View index() {
        logger.info("In the IndexView index() ... ");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            return View.of("/home", templateEngine);
        } else {
            return View.of("/index", templateEngine).withModel("counter", counterStatsRepository.currentCounter());
        }
    }
}
