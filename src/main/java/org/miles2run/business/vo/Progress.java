package org.miles2run.business.vo;

import org.miles2run.business.domain.GoalUnit;

/**
 * Created by shekhargulati on 06/03/14.
 */
public class Progress {
    private GoalUnit goalUnit;
    private long goal;
    private long totalDistanceCovered;
    private long percentage;
    private long activityCount;

    public Progress(long goal, GoalUnit goalUnit, long totalDistanceCovered, long activityCount) {
        this.goalUnit = goalUnit;
        this.goal = goal / this.goalUnit.getConversion();
        this.totalDistanceCovered = totalDistanceCovered / this.goalUnit.getConversion();
        this.activityCount = activityCount;
        if (goal != 0) {
            double percentageInDouble = ((double) totalDistanceCovered / goal) * 100;
            this.percentage = Double.valueOf(Math.ceil(percentageInDouble)).longValue();
            this.percentage = this.percentage > 100 ? 100 : this.percentage;
        }
    }


    public Progress() {
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
}

