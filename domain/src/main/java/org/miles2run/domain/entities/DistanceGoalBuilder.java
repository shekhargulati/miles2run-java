package org.miles2run.domain.entities;

public class DistanceGoalBuilder {
    private String purpose;
    private Duration duration;
    private GoalUnit goalUnit;
    private boolean archived;
    private Profile profile;
    private long distance;

    public DistanceGoalBuilder setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public DistanceGoalBuilder setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public DistanceGoalBuilder setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
        return this;
    }

    public DistanceGoalBuilder setArchived(boolean archived) {
        this.archived = archived;
        return this;
    }

    public DistanceGoalBuilder setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public DistanceGoalBuilder setDistance(long distance) {
        this.distance = distance;
        return this;
    }

    public DistanceGoal createDistanceGoal() {
        return DistanceGoal.createDistanceGoal(purpose, duration, goalUnit, archived, profile, distance);
    }
}