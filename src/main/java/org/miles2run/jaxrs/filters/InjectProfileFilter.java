package org.miles2run.jaxrs.filters;

import org.jug.view.View;
import org.miles2run.business.services.ProfileService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 09/05/14.
 */
@Provider
@InjectProfile
public class InjectProfileFilter implements ContainerResponseFilter {

    @Inject
    private ProfileService profileService;
    @Context
    private HttpServletRequest request;
    @Inject
    private Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        HttpSession session = request.getSession(false);
        logger.info("Inside InjectProfileFilter filter() with session " + session);
        if (session != null && session.getAttribute("principal") != null && responseContext.hasEntity()) {
            View view = (View) responseContext.getEntity();
            Map<String, Object> model = view.getModel();
            Object principal = session.getAttribute("principal");
            logger.info("Setting profile for Principal " + principal);
            model.put("profile", profileService.findProfileByUsername(principal.toString()));
        }
    }
}
