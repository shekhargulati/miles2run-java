package org.miles2run.views.views;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.jug.view.View;
import org.miles2run.core.repositories.jpa.SocialConnectionRepository;
import org.miles2run.core.utils.UrlUtils;
import org.miles2run.domain.entities.SocialConnection;
import org.miles2run.domain.entities.SocialProvider;
import org.miles2run.social.Google;
import org.miles2run.social.GoogleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/google/callback")
public class GoogleCallbackView {

    private final Logger logger = LoggerFactory.getLogger(GoogleCallbackView.class);

    @Context
    private HttpServletRequest request;
    @Inject
    private GoogleService googleService;
    @Inject
    private SocialConnectionRepository socialConnectionRepository;

    @GET
    @Produces("text/html")
    public View callback(@QueryParam("state") String state, @QueryParam("code") String authCode) throws Exception {
        logger.info("Inside GoogleCallbackView callback()...");
        logger.info(String.format("Code %s State %s", authCode, state));
        GoogleTokenResponse oauthToken = googleService.getOauthToken(UrlUtils.getBaseUrl(request), authCode);
        Google user = googleService.getUser(oauthToken);
        String connectionId = user.getId();
        SocialConnection existingSocialConnection = socialConnectionRepository.findByConnectionId(connectionId);
        logger.info("SocialConnection " + existingSocialConnection);
        if (existingSocialConnection != null) {
            if (existingSocialConnection.getProfile() == null) {
                logger.info("Profile was null. So redirecting to new profile creation.");
                return View.of("/profiles/new?connectionId=" + connectionId, true);
            } else {
                String username = existingSocialConnection.getProfile().getUsername();
                logger.info(String.format("User %s already had authenticated with Google+. So redirecting to home.", username));
                HttpSession session = request.getSession();
                logger.info("Using Session with id " + session.getId());
                session.setAttribute("principal", username);
                return View.of("/", true);
            }
        }
        SocialConnection socialConnection = new SocialConnection(oauthToken.getAccessToken(), null, SocialProvider.GOOGLE_PLUS, user.getName(), connectionId);
        socialConnectionRepository.save(socialConnection);
        return View.of("/users/new?connectionId=" + connectionId, true);
    }

}
