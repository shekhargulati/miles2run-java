package org.miles2run.business.services;

import org.miles2run.business.domain.SocialConnection;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 11/03/14.
 */
@Stateless
public class TwitterService {

    @Inject
    private SocialConnectionService socialConnectionService;
    @Inject
    private TwitterFactory twitterFactory;
    @Inject
    private Logger logger;

    @Asynchronous
    public void postStatus(String status, SocialConnection connection) {
        Twitter twitter = twitterFactory.getInstance(new AccessToken(connection.getAccessToken(), connection.getAccessSecret()));
        try {
            twitter.updateStatus(status);
        } catch (TwitterException e) {
            logger.warning("Not able to send tweet because " + e.getMessage());
        }
    }
}
