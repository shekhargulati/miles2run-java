package org.miles2run.business.services;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import org.miles2run.business.domain.SocialConnection;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 27/03/14.
 */
@Stateless
public class FacebookService {

    @Inject
    FacebookFactory facebookFactory;
    @Inject
    private Logger logger;

    @Asynchronous
    public void postStatus(String status, SocialConnection socialConnection) {
        Facebook facebook = facebookFactory.getInstance(new AccessToken(socialConnection.getAccessToken(), null));
        try {
            facebook.postStatusMessage(status);
        } catch (FacebookException e) {
            throw new RuntimeException(e);
        }
    }
}
