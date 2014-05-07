package org.miles2run.jaxrs.views;

import org.jug.filters.EnableSession;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.miles2run.jaxrs.vo.Counter;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
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

    @GET
    @EnableSession
    public View index() {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("counter", new Counter(10L, 5L, 100L));
            return new View("/index", model).setTemplateEngine(templateEngine);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load index page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }
}
