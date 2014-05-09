package org.miles2run.business.producers;

import facebook4j.FacebookFactory;
import facebook4j.conf.ConfigurationBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by shekhargulati on 26/03/14.
 */
@ApplicationScoped
public class FacebookFactoryProducer {

    @Produces
    public FacebookFactory facebookFactory() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setOAuthAppId(System.getenv("FACEBOOK_OAUTH_ID"))
                .setOAuthAppSecret(System.getenv("FACEBOOK_OAUTH_SECRET"))
                .setOAuthPermissions("email,publish_stream,user_photos");
        FacebookFactory facebookFactory = new FacebookFactory(builder.build());
        return facebookFactory;
    }
}
