package org.miles2run.jaxrs.views;

import org.jug.view.View;
import org.miles2run.business.domain.jpa.SocialConnection;
import org.miles2run.business.domain.jpa.SocialProvider;
import org.miles2run.business.services.SocialConnectionService;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

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
 * Created by shekhargulati on 05/05/14.
 */
@Path("/")
public class TwitterCallbackView {

    @Inject
    private TwitterFactory twitterFactory;
    @Inject
    private Logger logger;
    @Inject
    private SocialConnectionService socialConnectionService;
    @Context
    private HttpServletRequest request;

    @Path("/twitter/callback")
    @GET
    @Produces("text/html")
    public View callback(@QueryParam("oauth_token") String oauthToken, @QueryParam("oauth_verifier") String oauthVerifier) throws Exception {
        RequestToken requestToken = new RequestToken(oauthToken, oauthVerifier);
        Twitter twitter = twitterFactory.getInstance();
        AccessToken oAuthAccessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
        logger.info(String.format("OAuthAccess Token created : %s ", oAuthAccessToken));
        String connectionId = String.valueOf(twitter.getId());
        SocialConnection existingSocialConnection = socialConnectionService.findByConnectionId(connectionId);
        logger.info("SocialConnection " + existingSocialConnection);
        if (existingSocialConnection != null) {
            if (existingSocialConnection.getProfile() == null) {
                logger.info("Profile was null. So redirecting to new profile creation.");
                return View.of("/profiles/new?connectionId=" + connectionId, true);
            } else {
                String username = existingSocialConnection.getProfile().getUsername();
                logger.info(String.format("User %s already had authenticated with twitter. So redirecting to home.", username));
                HttpSession session = request.getSession();
                logger.info("Using Session with id " + session.getId());
                session.setAttribute("principal", username);
                return View.of("/", true);
            }
        }
        SocialConnection socialConnection = new SocialConnection(oAuthAccessToken.getToken(), oAuthAccessToken.getTokenSecret(), SocialProvider.TWITTER, oAuthAccessToken.getScreenName(), connectionId);
        socialConnectionService.save(socialConnection);
        logger.info(String.format("Saved new SocialConnection with id %s", connectionId));
        return View.of("/profiles/new?connectionId=" + connectionId, true);
    }
}
