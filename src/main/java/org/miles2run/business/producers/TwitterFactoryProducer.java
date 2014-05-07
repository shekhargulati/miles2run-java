package org.miles2run.business.producers;

import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by shekhargulati on 04/03/14.
 */
@ApplicationScoped
public class TwitterFactoryProducer {

    @Produces
    public TwitterFactory twitterFactory() {
        Configuration configuration = new ConfigurationBuilder().
                setOAuthConsumerKey(System.getenv("TWITTER_CONSUMER_KEY")).
                setOAuthConsumerSecret(System.getenv("TWITTER_CONSUMER_SECRET")).
                setDebugEnabled(true).
                build();
        TwitterFactory twitterFactory = new TwitterFactory(configuration);
        return twitterFactory;
    }
}
