package org.miles2run.domain.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
@Table(name = "social_connection", indexes = {
        @Index(unique = true, columnList = "connectionId"),
        @Index(unique = true, columnList = "handle")
})
public class SocialConnection extends BaseEntity {

    @NotNull
    private String accessToken;

    private String accessSecret;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SocialProvider provider;

    private String handle;

    @NotNull
    private String connectionId;

    @ManyToOne
    private Profile profile;

    protected SocialConnection() {

    }

    private SocialConnection(String accessToken, String accessSecret, SocialProvider provider, String handle, String connectionId) {
        this.accessToken = accessToken;
        this.accessSecret = accessSecret;
        this.provider = provider;
        this.handle = handle;
        this.connectionId = connectionId;
    }

    static SocialConnection createSocialConnection(String accessToken, String accessSecret, SocialProvider provider, String handle, String connectionId) {
        return new SocialConnection(accessToken, accessSecret, provider, handle, connectionId);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public SocialProvider getProvider() {
        return provider;
    }

    public String getHandle() {
        return handle;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "SocialConnection{" +
                "id='" + id + '\'' +
                "accessToken='" + accessToken + '\'' +
                ", accessSecret='" + accessSecret + '\'' +
                ", provider=" + provider +
                ", handle='" + handle + '\'' +
                ", connectionId='" + connectionId + '\'' +
                '}';
    }
}
