package org.miles2run.jaxrs.views;

import org.jug.filters.LoggedIn;
import org.jug.view.View;
import org.jug.view.ViewException;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.vo.ProfileSocialConnectionDetails;
import org.miles2run.jaxrs.filters.InjectProfile;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 10/03/14.
 */
@Path("/home")
public class HomeView {

    @Inject
    private Logger logger;
    @Context
    private SecurityContext securityContext;
    @Inject
    private ProfileService profileService;
    @Inject
    private TemplateEngine templateEngine;

    @GET
    @LoggedIn
    @Produces("text/html")
    @InjectProfile
    public View home() {
        try {
            String username = securityContext.getUserPrincipal().getName();
            logger.info(String.format("Rendering home page for user %s ", username));
            Map<String, Object> model = new HashMap<>();
            ProfileSocialConnectionDetails activeProfileWithSocialConnections = profileService.findProfileWithSocialConnections(username);
            model.put("activeProfile", activeProfileWithSocialConnections);
            return new View("/home", model, "model").setTemplateEngine(templateEngine);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load home page.", e);
            throw new ViewException(e.getMessage(), e, templateEngine);
        }
    }
}
