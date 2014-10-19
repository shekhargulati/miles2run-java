package org.miles2run.views.views;

import org.jug.view.View;
import org.jug.view.ViewException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static org.miles2run.core.utils.UrlUtils.absoluteUrlForResourceMethod;

@Path("/twitter/signin")
public class TwitterSigninView {

    private final Logger logger = LoggerFactory.getLogger(TwitterSigninView.class);

    @Inject
    private TwitterFactory twitterFactory;
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
            RequestToken requestToken = twitter.getOAuthRequestToken(absoluteUrlForResourceMethod(request, TwitterCallbackView.class, "callback"));
            return View.of(requestToken.getAuthenticationURL(), true).withAbsoluteUrl();
        } catch (TwitterException e) {
            throw new ViewException("Unable to get Twitter Authentication Url. Exception is: " + e.getMessage(), e, templateEngine);
        }
    }
}
