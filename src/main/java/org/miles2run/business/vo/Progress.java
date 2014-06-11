package org.miles2run.business.vo;

import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.GoalUnit;
import org.miles2run.business.domain.Profile;

/**
 * Created by shekhargulati on 06/03/14.
 */
public class Progress {
    private GoalUnit goalUnit;
    private long goal;
    private long totalDistanceCovered;
    private long percentage;
    private long activityCount;
    private long totalDurationInSecs;
    private long totalDurationInMins;
    private double averagePace;

    public Progress(long goal, GoalUnit goalUnit, long totalDistanceCovered, long activityCount, long totalDurationInSecs) {
        this.goalUnit = goalUnit;
        this.goal = goal / this.goalUnit.getConversion();
        this.totalDistanceCovered = totalDistanceCovered / this.goalUnit.getConversion();
        this.activityCount = activityCount;
        if (goal != 0) {
            double percentageInDouble = ((double) (totalDistanceCovered * 100) / goal);
            this.percentage = Double.valueOf(percentageInDouble).longValue();
            this.percentage = this.percentage > 100 ? 100 : this.percentage;
        }
        this.totalDurationInSecs = totalDurationInSecs;
        this.totalDurationInMins = this.totalDurationInSecs / 60;
        if (this.totalDistanceCovered != 0) {
            this.averagePace = Double.valueOf(this.totalDurationInMins) / this.totalDistanceCovered;
        }
    }

    public Progress() {
        this.goal = 0;
        this.totalDistanceCovered = 0;
        this.percentage = 0;
        this.activityCount = 0;
    }

    public Progress(Goal goal) {
        this.goal = goal.getGoal() / goal.getGoalUnit().getConversion();
        this.percentage = 0;
        this.totalDistanceCovered = 0;
        this.averagePace = 0;
        this.activityCount = 0;
        this.goalUnit = goal.getGoalUnit();
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    public long getTotalDistanceCovered() {
        return totalDistanceCovered;
    }

    public void setTotalDistanceCovered(long totalDistanceCovered) {
        this.totalDistanceCovered = totalDistanceCovered;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public long getPercentage() {
        return percentage;
    }

    public void setPercentage(long percentage) {
        this.percentage = percentage;
    }

    public long getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(long activityCount) {
        this.activityCount = activityCount;
    }

    public double getAveragePace() {
        return this.averagePace;
    }

    public static Progress format(Progress progress, Goal goal) {
        return null;
    }
}

