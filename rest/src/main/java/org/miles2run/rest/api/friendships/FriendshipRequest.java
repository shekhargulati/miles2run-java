package org.miles2run.rest.api.friendships;

public class FriendshipRequest {

    private String userToFollow;

    public String getUserToFollow() {
        return userToFollow;
    }

    public void setUserToFollow(String userToFollow) {
        this.userToFollow = userToFollow;
    }
}
