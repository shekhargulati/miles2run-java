package org.miles2run.rest.api.friendships;

public class UnfollowRequest {

    private String userToUnfollow;

    public String getUserToUnfollow() {
        return userToUnfollow;
    }

    public void setUserToUnfollow(String userToUnfollow) {
        this.userToUnfollow = userToUnfollow;
    }
}
