package org.miles2run.jaxrs.views;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import org.jug.view.View;
import org.miles2run.business.domain.SocialConnection;
import org.miles2run.business.domain.SocialProvider;
import org.miles2run.business.services.ProfileService;
import org.miles2run.business.services.SocialConnectionService;
import org.miles2run.business.utils.UrlUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 09/05/14.
 */
@Path("/")
public class FacebookCallbackView {


    @Inject
    private Logger logger;
    @Inject
    private SocialConnectionService socialConnectionService;
    @Inject
    private ProfileService profileService;
    @Inject
    private FacebookFactory facebookFactory;
    @Context
    private HttpServletRequest request;


    @Path("/facebook/callback")
    @GET
    @Produces("text/html")
    public View callback(@QueryParam("code") String oauthCode) throws Exception {
        logger.info(String.format("Facebook Oauth code : %s", oauthCode));
        Facebook facebook = facebookFactory.getInstance();
        facebook.setOAuthCallbackURL(UrlUtils.absoluteUrlFor(request, FacebookCallbackView.class, "callback"));
        AccessToken oAuthAccessToken = facebook.getOAuthAccessToken(oauthCode);
        String connectionId = facebook.getId();
        SocialConnection existingSocialConnection = socialConnectionService.findByConnectionId(connectionId);
        logger.info("SocialConnection " + existingSocialConnection);
        if (existingSocialConnection != null) {
            if (existingSocialConnection.getProfile() == null) {
                logger.info("Profile was null. So redirecting to new profile creation.");
                return View.of("/profiles/new?connectionId=" + connectionId, true);
            } else {
                String username = existingSocialConnection.getProfile().getUsername();
                logger.info(String.format("User %s already had authenticated with facebook. So redirecting to home.", username));
                HttpSession session = request.getSession();
                logger.info("Using Session with id " + session.getId());
                session.setAttribute("principal", username);
                return View.of("/home", true);
            }
        }
        SocialConnection socialConnection = new SocialConnection(oAuthAccessToken.getToken(), null, SocialProvider.FACEBOOK, facebook.users().getMe().getUsername(), connectionId);
        socialConnectionService.save(socialConnection);
        return View.of("/profiles/new?connectionId=" + connectionId, true);
    }
}
