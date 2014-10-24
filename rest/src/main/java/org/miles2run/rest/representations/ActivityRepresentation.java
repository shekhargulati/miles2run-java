package org.miles2run.rest.representations;

import org.miles2run.domain.entities.Activity;
import org.miles2run.domain.entities.GoalUnit;
import org.miles2run.domain.entities.Profile;

import java.util.Date;

public class ActivityRepresentation {

    private Long goalId;
    private Long id;

    private String status;

    private double distanceCovered;

    private GoalUnit goalUnit;

    private Date activityDate;

    private String fullname;

    private String username;

    private String profilePic;

    private long duration;

    private Date postedAt;

    private String durationStr;


    public static ActivityRepresentation from(Activity activity) {
        ActivityRepresentation representation = new ActivityRepresentation();
        representation.id = activity.getId();
        representation.goalId = activity.getGoal().getId();
        representation.status = activity.getStatus();
        representation.goalUnit = activity.getGoalUnit();
        representation.distanceCovered = activity.getDistanceCovered() / activity.getGoalUnit().getConversion();
        representation.activityDate = activity.getActivityDate();
        Profile postedBy = activity.getPostedBy();
        representation.fullname = postedBy.getFullname();
        representation.username = postedBy.getUsername();
        representation.profilePic = postedBy.getProfilePic();
        representation.duration = activity.getDuration();
        representation.postedAt = activity.getCreatedAt();
        representation.durationStr = toDurationText(representation.duration);
        return representation;
    }

    private static String toDurationText(long duration) {
        long secondsInMinute = 60;
        long minutesInHour = 60;
        long secondsInHour = minutesInHour * secondsInMinute;
        long hours = duration / secondsInHour;
        long minutes = (duration - (hours * secondsInHour)) / secondsInMinute;
        long seconds = duration - (hours * secondsInHour) - (minutes * secondsInMinute);
        String hoursText = toText(hours);
        String minutesText = toText(minutes);
        String secondsText = toText(seconds);
        return hoursText + ":" + minutesText + ":" + secondsText;
    }

    private static String toText(long val) {
        return val < 10 ? "0" + val : String.valueOf(val);
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

    public Date getPostedAt() {
        return postedAt;
    }

    public String getDurationStr() {
        return durationStr;
    }
}
