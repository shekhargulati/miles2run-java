package org.miles2run.views.views;

import org.jug.view.View;
import org.miles2run.core.utils.UrlUtils;
import org.miles2run.social.GoogleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/google/signin")
public class GoogleSigninView {

    private final Logger logger = LoggerFactory.getLogger(GoogleSigninView.class);

    @Inject
    private GoogleService googleService;
    @Context
    private HttpServletRequest request;

    @GET
    @Produces("text/html")
    public View signin() {
        logger.info("Inside GoogleSigninView signin()..");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            return View.of("/", true);
        }
        return View.of(googleService.buildLoginUrl(UrlUtils.getBaseUrl(request)), true).withAbsoluteUrl();
    }

}
