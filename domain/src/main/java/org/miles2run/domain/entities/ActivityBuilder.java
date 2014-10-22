package org.miles2run.domain.entities;

import java.util.Date;

public class ActivityBuilder {
    private String status;
    private GoalUnit goalUnit;
    private double distanceCovered;
    private Date activityDate;
    private Profile postedBy;
    private long duration;
    private Goal goal;

    public ActivityBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public ActivityBuilder setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
        return this;
    }

    public ActivityBuilder setDistanceCovered(double distanceCovered) {
        this.distanceCovered = distanceCovered;
        return this;
    }

    public ActivityBuilder setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
        return this;
    }

    public ActivityBuilder setPostedBy(Profile postedBy) {
        this.postedBy = postedBy;
        return this;
    }

    public ActivityBuilder setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public ActivityBuilder setGoal(Goal goal) {
        this.goal = goal;
        return this;
    }

    public Activity createActivity() {
        return Activity.createActivity(status, goalUnit, distanceCovered, activityDate, postedBy, duration, goal);
    }
}