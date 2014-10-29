package org.miles2run.domain.kv_aggregates;

import org.miles2run.domain.entities.GoalUnit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ActivityAggregate {
    private final Long goalId;
    private final Long id;

    private final String status;

    private final double distanceCovered;

    private final GoalUnit goalUnit;

    private final Date activityDate;

    private final String fullname;

    private final String username;

    private final String profilePic;

    private final long duration;

    public ActivityAggregate(Map<String, String> activity) {
        this.id = Long.valueOf(activity.get("id"));
        this.username = activity.get("username");
        this.activityDate = toDate(activity.get("activityDate"));
        this.goalUnit = GoalUnit.fromStringToGoalUnit(activity.get("goalUnit"));
        this.distanceCovered = Double.valueOf(activity.get("distanceCovered")) / this.goalUnit.getConversion();
        this.fullname = activity.get("fullname");
        this.profilePic = activity.get("profilePic");
        this.status = activity.get("status");
        this.goalId = Long.valueOf(activity.get("goalId"));
        this.duration = Long.valueOf(activity.get("duration"));
    }

    private static Date toDate(String text) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(text);
        } catch (ParseException e) {
            return null;

        }
    }

    public Long getGoalId() {
        return goalId;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public double getDistanceCovered() {
        return distanceCovered;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public String getFullname() {
        return fullname;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public long getDuration() {
        return duration;
    }
}
