package org.miles2run.views.filters;

import org.jug.view.View;
import org.miles2run.core.repositories.jpa.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Map;

@Provider
@InjectProfile
public class InjectProfileFilter implements ContainerResponseFilter {

    private Logger logger = LoggerFactory.getLogger(InjectProfileFilter.class);

    @Inject
    private ProfileRepository profileRepository;
    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        HttpSession session = request.getSession(false);
        logger.info("Inside InjectProfileFilter filter() with session " + session);
        if (session != null && session.getAttribute("principal") != null && responseContext.hasEntity()) {
            View view = (View) responseContext.getEntity();
            Map<String, Object> model = view.getModel();
            Object principal = session.getAttribute("principal");
            logger.info("Setting profile for Principal " + principal);
            model.put("profile", profileRepository.findProfileByUsername(principal.toString()));
        }
    }
}
