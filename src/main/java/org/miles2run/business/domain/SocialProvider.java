package org.miles2run.business.domain;

/**
 * Created by shekhargulati on 05/03/14.
 */
public enum SocialProvider {

    TWITTER("twitter"), FACEBOOK("facebook"), GOOGLE_PLUS("google+");

    private final String provider;

    SocialProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
