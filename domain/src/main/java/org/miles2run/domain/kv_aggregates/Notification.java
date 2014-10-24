package org.miles2run.domain.kv_aggregates;

public class Notification {

    private String userToNotify;
    private String userTookAction;
    private long timestamp;
    private Action action;

    public Notification(String userToNotify, String userTookAction, Action action, long timestamp) {
        this.userToNotify = userToNotify;
        this.userTookAction = userTookAction;
        this.action = action;
        this.timestamp = timestamp;
    }


    public String getUserToNotify() {
        return userToNotify;
    }

    public String getUserTookAction() {
        return userTookAction;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Action getAction() {
        return action;
    }

}

