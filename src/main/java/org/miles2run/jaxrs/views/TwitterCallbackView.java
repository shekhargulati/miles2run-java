package org.miles2run.jaxrs.views;

import org.apache.commons.lang3.StringUtils;
import org.jug.view.View;
import org.miles2run.business.domain.jpa.SocialConnection;
import org.miles2run.business.domain.jpa.SocialProvider;
import org.miles2run.business.services.jpa.SocialConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Created by shekhargulati on 05/05/14.
 */
@Path("/")
public class TwitterCallbackView {

    private Logger logger = LoggerFactory.getLogger(TwitterCallbackView.class);

    @Inject
    private TwitterFactory twitterFactory;
    @Inject
    private SocialConnectionService socialConnectionService;
    @Context
    private HttpServletRequest request;

    @Path("/twitter/callback")
    @GET
    @Produces("text/html")
    public View callback(@QueryParam("oauth_token") String oauthToken, @QueryParam("oauth_verifier") String oauthVerifier, @QueryParam("denied") String deniedCode) throws Exception {
        if (StringUtils.isNotBlank(deniedCode)) {
            return View.of("/", true);
        }
        RequestToken requestToken = new RequestToken(oauthToken, oauthVerifier);
        Twitter twitter = twitterFactory.getInstance();
        AccessToken oAuthAccessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
        logger.info("OAuthAccessToken : {} Token :{} TokenSecret : {} ", oAuthAccessToken, oAuthAccessToken.getToken(), oAuthAccessToken.getTokenSecret());
        String connectionId = String.valueOf(twitter.getId());
        SocialConnection existingSocialConnection = socialConnectionService.findByConnectionId(connectionId);
        logger.info("SocialConnection " + existingSocialConnection);
        if (existingSocialConnection != null) {
            if (isTokenAndSecretEqual(oAuthAccessToken, existingSocialConnection) == true) {
                logger.info("Request token and database stored in token are equal.");
                return getRedirectView(connectionId, existingSocialConnection);
            } else {
                logger.info("Request token and token stored in database are not equal. So updating database with new token.");
                socialConnectionService.update(existingSocialConnection.getId(), oAuthAccessToken.getToken(), oAuthAccessToken.getTokenSecret());
                return getRedirectView(connectionId, socialConnectionService.findByConnectionId(connectionId));
            }
        }
        SocialConnection socialConnection = new SocialConnection(oAuthAccessToken.getToken(), oAuthAccessToken.getTokenSecret(), SocialProvider.TWITTER, oAuthAccessToken.getScreenName(), connectionId);
        socialConnectionService.save(socialConnection);
        logger.info("Saved new SocialConnection with id {}", connectionId);
        return View.of("/profiles/new?connectionId=" + connectionId, true);
    }

    View getRedirectView(String connectionId, SocialConnection existingSocialConnection) {
        if (existingSocialConnection.getProfile() == null) {
            logger.info("Profile was null. So redirecting to new profile creation.");
            return View.of("/profiles/new?connectionId=" + connectionId, true);
        } else {
            String username = existingSocialConnection.getProfile().getUsername();
            logger.info("User {} already had authenticated with twitter. So redirecting to home.", username);
            HttpSession session = request.getSession();
            logger.info("Using Session with id {}", session.getId());
            session.setAttribute("principal", username);
            return View.of("/", true);
        }
    }

    private boolean isTokenAndSecretEqual(AccessToken oAuthAccessToken, SocialConnection existingSocialConnection) {
        return StringUtils.equals(oAuthAccessToken.getToken(), existingSocialConnection.getAccessToken()) && StringUtils.equals(oAuthAccessToken.getTokenSecret(), existingSocialConnection.getAccessSecret());
    }
}
