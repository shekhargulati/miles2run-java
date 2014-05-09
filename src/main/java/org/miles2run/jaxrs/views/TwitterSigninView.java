package org.miles2run.jaxrs.views;

import org.jug.view.View;
import org.miles2run.jaxrs.utils.UrlUtils;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 05/05/14.
 */
@Path("/twitter/signin")
public class TwitterSigninView {

    @Inject
    private TwitterFactory twitterFactory;

    @Inject
    private Logger logger;

    @GET
    @Produces("text/html")
    public View signin(@Context HttpServletRequest request) {
        Twitter twitter = twitterFactory.getInstance();
        try {
            RequestToken requestToken = twitter.getOAuthRequestToken(UrlUtils.absoluteUrlFor(request, TwitterCallbackView.class, "callback"));
            return View.of(requestToken.getAuthenticationURL(), true).withAbsoluteUrl();
        } catch (TwitterException e) {
            throw new RuntimeException("Unable to get Twitter Authentication Url. Exception is: " + e);
        }
    }
}
