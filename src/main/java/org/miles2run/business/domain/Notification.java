package org.miles2run.business.domain;

import com.google.gson.Gson;

/**
 * Created by shekhargulati on 26/03/14.
 */
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

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Notification toNotification(String notification) {
        Gson gson = new Gson();
        return gson.fromJson(notification, Notification.class);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "userToNotify='" + userToNotify + '\'' +
                ", userTookAction='" + userTookAction + '\'' +
                ", timestamp=" + timestamp +
                ", action=" + action +
                '}';
    }
}

