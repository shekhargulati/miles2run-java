package org.miles2run.jaxrs.requests;

import org.miles2run.business.domain.jpa.Activity;
import org.miles2run.business.domain.jpa.GoalUnit;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by shekhargulati on 11/08/14.
 */
public class ActivityRequest {

    @Size(max = 1000)
    private String status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit;

    @NotNull
    @Min(value = 1)
    @Max(value = 20)
    private double distanceCovered;

    @NotNull
    private Date activityDate;

    @NotNull
    private long duration;

    public ActivityRequest() {
    }

    public ActivityRequest(String status, GoalUnit goalUnit, double distanceCovered, Date activityDate, long duration) {
        this.status = status;
        this.goalUnit = goalUnit;
        this.distanceCovered = distanceCovered;
        this.activityDate = activityDate;
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public double getDistanceCovered() {
        return distanceCovered;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public long getDuration() {
        return duration;
    }

    public Activity toActivity() {
        double distanceCovered = this.distanceCovered * this.goalUnit.getConversion();
        Activity activity = new Activity(this.status, distanceCovered, this.goalUnit, this.duration, this.activityDate);
        return activity;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActivityRequest{");
        sb.append("status='").append(status).append('\'');
        sb.append(", goalUnit=").append(goalUnit);
        sb.append(", distanceCovered=").append(distanceCovered);
        sb.append(", activityDate='").append(activityDate).append('\'');
        sb.append(", duration=").append(duration);
        sb.append('}');
        return sb.toString();
    }
}
