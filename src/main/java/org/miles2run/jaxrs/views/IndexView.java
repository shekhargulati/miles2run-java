package org.miles2run.jaxrs.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.miles2run.business.services.CounterService;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.miles2run.business.domain.Counter;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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

    @GET
    @EnableSession
    @InjectProfile
    public View index() {
        logger.info("In the IndexView index() ... ");
        return View.of("/index", templateEngine).withModel("counter", counterService.currentCounter());
    }
}
