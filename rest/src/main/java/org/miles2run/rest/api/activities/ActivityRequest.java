package org.miles2run.rest.api.activities;

import org.miles2run.domain.entities.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

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

    public Activity toActivity(Profile profile, Goal goal) {
        return new ActivityBuilder().setPostedBy(profile).setStatus(status).setDistanceCovered(distanceCovered).setActivityDate(activityDate).setGoal(goal).setGoalUnit(goalUnit).setDuration(duration).createActivity();
    }
}

