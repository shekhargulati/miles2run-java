package org.miles2run.core.repositories.jpa.vo;

import org.miles2run.domain.entities.*;

public class Progress {

    private GoalUnit goalUnit;
    private long goal;
    private double totalDistanceCovered;
    private double percentage;
    private long activityCount;
    private double averagePace;

    public Progress(Goal goal, double totalDistanceCovered, long activityCount, long totalDurationInSecs) {
        this.goalUnit = goal.getGoalUnit();

        this.totalDistanceCovered = totalDistanceCovered / this.goalUnit.getConversion();
        this.activityCount = activityCount;
        if (goalType(goal) == GoalType.DISTANCE_GOAL) {
            DistanceGoal distanceGoal = (DistanceGoal) goal;
            this.goal = distanceGoal.getDistance() / this.goalUnit.getConversion();
            this.percentage = (totalDistanceCovered * 100) / this.goal;
            this.percentage = this.percentage > 100 ? 100 : this.percentage;
        }
        double totalDurationInMins = Double.valueOf(totalDurationInSecs) / 60;
        if (this.totalDistanceCovered != 0) {
            this.averagePace = totalDurationInMins / this.totalDistanceCovered;
        }
    }

    private GoalType goalType(Goal goal) {
        if (goal instanceof DistanceGoal) {
            return GoalType.DISTANCE_GOAL;
        } else if (goal instanceof DurationGoal) {
            return GoalType.DURATION_GOAL;
        }
        return GoalType.COMMUNITY_RUN_GOAL;
    }

    public Progress(Goal goal) {
        if (goalType(goal) == GoalType.DISTANCE_GOAL) {
            DistanceGoal distanceGoal = (DistanceGoal) goal;
            this.goal = distanceGoal.getDistance() / this.goalUnit.getConversion();
        }
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

}

