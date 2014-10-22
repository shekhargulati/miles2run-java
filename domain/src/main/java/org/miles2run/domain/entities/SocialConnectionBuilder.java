package org.miles2run.domain.entities;

public class SocialConnectionBuilder {
    private String accessToken;
    private String accessSecret;
    private SocialProvider provider;
    private String handle;
    private String connectionId;

    public SocialConnectionBuilder setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public SocialConnectionBuilder setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
        return this;
    }

    public SocialConnectionBuilder setProvider(SocialProvider provider) {
        this.provider = provider;
        return this;
    }

    public SocialConnectionBuilder setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public SocialConnectionBuilder setConnectionId(String connectionId) {
        this.connectionId = connectionId;
        return this;
    }

    public SocialConnection createSocialConnection() {
        return SocialConnection.createSocialConnection(accessToken, accessSecret, provider, handle, connectionId);
    }
}