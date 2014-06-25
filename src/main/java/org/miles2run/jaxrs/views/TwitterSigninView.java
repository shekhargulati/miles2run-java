package org.miles2run.jaxrs.views;

import org.jug.view.View;
import org.jug.view.ViewException;
import org.miles2run.business.utils.UrlUtils;
import org.thymeleaf.TemplateEngine;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
    @Inject
    private TemplateEngine templateEngine;

    @GET
    @Produces("text/html")
    public View signin(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            return View.of("/", true);
        }
        Twitter twitter = twitterFactory.getInstance();
        try {
            RequestToken requestToken = twitter.getOAuthRequestToken(UrlUtils.absoluteUrlForResourceMethod(request, TwitterCallbackView.class, "callback"));
            return View.of(requestToken.getAuthenticationURL(), true).withAbsoluteUrl();
        } catch (TwitterException e) {
            throw new ViewException("Unable to get Twitter Authentication Url. Exception is: " + e.getMessage(), e, templateEngine);
        }
    }
}
