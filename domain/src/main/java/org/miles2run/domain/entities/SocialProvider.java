package org.miles2run.domain.entities;

public enum SocialProvider {

    TWITTER("twitter"), FACEBOOK("facebook"), GOOGLE_PLUS("google-plus");

    private final String provider;

    SocialProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
