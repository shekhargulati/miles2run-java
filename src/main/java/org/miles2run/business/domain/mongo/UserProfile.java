package org.miles2run.business.domain.mongo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shekhargulati on 25/03/14.
 */
public class UserProfile {

    private String username;
    private List<String> following = new ArrayList<>();
    private List<String> followers = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFollowing() {
        return following;
    }

    public List<String> getFollowers() {
        return followers;
    }
}
