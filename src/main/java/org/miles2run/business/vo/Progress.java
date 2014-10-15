package org.miles2run.business.vo;

import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.GoalType;
import org.miles2run.business.domain.jpa.GoalUnit;

/**
 * Created by shekhargulati on 06/03/14.
 */
public class Progress {

    private GoalUnit goalUnit;
    private long goal;
    private double totalDistanceCovered;
    private double percentage;
    private long activityCount;
    private double averagePace;
    private GoalType goalType;

    public Progress(long goal, GoalUnit goalUnit, double totalDistanceCovered, long activityCount, long totalDurationInSecs, GoalType goalType) {
        this.goalUnit = goalUnit;
        this.goal = goal / this.goalUnit.getConversion();
        this.totalDistanceCovered = totalDistanceCovered / this.goalUnit.getConversion();
        this.activityCount = activityCount;
        this.goalType = goalType;
        if (goal != 0) {
            this.percentage = (totalDistanceCovered * 100) / goal;
            this.percentage = this.percentage > 100 ? 100 : this.percentage;
        }
        double totalDurationInMins = Double.valueOf(totalDurationInSecs) / 60;
        if (this.totalDistanceCovered != 0) {
            this.averagePace = totalDurationInMins / this.totalDistanceCovered;
        }
    }

    public Progress() {
        this.goal = 0;
        this.totalDistanceCovered = 0;
        this.percentage = 0;
        this.activityCount = 0;
    }

    public Progress(Goal goal) {
        this.goal = goal.getDistance() / goal.getGoalUnit().getConversion();
        this.percentage = 0;
        this.totalDistanceCovered = 0;
        this.averagePace = 0;
        this.activityCount = 0;
        this.goalUnit = goal.getGoalUnit();
        this.goalType = goal.getGoalType();
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    public double getTotalDistanceCovered() {
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
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

    public GoalType getGoalType() {
        return goalType;
    }

}

