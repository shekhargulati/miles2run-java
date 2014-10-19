package org.miles2run.core.vo;

import org.miles2run.core.utils.DateUtils;
import org.miles2run.domain.entities.GoalUnit;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityDetails {
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

    private ActivityDetails() {

    }

    public ActivityDetails(Long id, String status, double distanceCovered, GoalUnit goalUnit, Date activityDate, String fullname, long duration, String username, String profilePic, Date postedAt, Long goalId) {
        this.id = id;
        this.status = status;
        this.goalUnit = goalUnit;
        this.distanceCovered = distanceCovered;
        this.activityDate = activityDate;
        this.fullname = fullname;
        this.username = username;
        this.profilePic = profilePic;
        this.duration = duration;
        this.postedAt = postedAt;
        this.goalId = goalId;
    }

    public ActivityDetails(Map<String, String> hash) {
        this.id = Long.valueOf(hash.get("id"));
        this.username = hash.get("username");
        this.activityDate = DateUtils.toDate(hash.get("activityDate"));
        this.goalUnit = GoalUnit.fromStringToGoalUnit(hash.get("goalUnit"));
        this.distanceCovered = Double.valueOf(hash.get("distanceCovered")) / this.goalUnit.getConversion();
        this.fullname = hash.get("fullname");
        this.profilePic = hash.get("profilePic");
        this.status = hash.get("status");
        this.goalId = Long.valueOf(hash.get("goalId"));
        this.duration = Long.valueOf(hash.get("duration"));
    }

    public static List<ActivityDetails> toListOfHumanReadable(List<ActivityDetails> activityDetailsList) {
        List<ActivityDetails> activityDetailsReadableList = activityDetailsList.stream().map(ActivityDetails::toHumanReadable).collect(Collectors.toList());
        return activityDetailsReadableList;
    }

    public static ActivityDetails toHumanReadable(ActivityDetails activityDetails) {
        ActivityDetails hr = new ActivityDetails();
        hr.id = activityDetails.getId();
        hr.goalId = activityDetails.goalId;
        hr.status = activityDetails.status;
        hr.goalUnit = activityDetails.goalUnit;
        hr.distanceCovered = activityDetails.distanceCovered / hr.goalUnit.getConversion();
        hr.activityDate = activityDetails.activityDate;
        hr.fullname = activityDetails.fullname;
        hr.username = activityDetails.username;
        hr.profilePic = activityDetails.profilePic;
        hr.duration = activityDetails.duration;
        hr.postedAt = activityDetails.postedAt;
        hr.durationStr = toDurationText(activityDetails.duration);
        return hr;
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
        return new StringBuilder(hoursText).append(":")
                .append(minutesText).append(":")
                .append(secondsText)
                .toString();
    }

    private static String toText(long val) {
        return val < 10 ? "0" + val : String.valueOf(val);
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

    public Long getGoalId() {
        return goalId;
    }

    public Date getPostedAt() {
        return postedAt;
    }

    public String getDurationStr() {
        return durationStr;
    }

    @Override
    public String toString() {
        return "ActivityDetails{" +
                "id=" + id +
                ", activityDate=" + activityDate +
                ", timestamp=" + activityDate.getTime() +
                ", username='" + username + '\'' +
                '}';
    }

}
