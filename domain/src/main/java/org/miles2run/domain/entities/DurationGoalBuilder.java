package org.miles2run.domain.entities;

public class DurationGoalBuilder {
    private String purpose;
    private Duration duration;
    private GoalUnit goalUnit;
    private boolean archived;
    private Profile profile;
    private int days;

    public DurationGoalBuilder setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public DurationGoalBuilder setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public DurationGoalBuilder setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
        return this;
    }

    public DurationGoalBuilder setArchived(boolean archived) {
        this.archived = archived;
        return this;
    }

    public DurationGoalBuilder setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public DurationGoalBuilder setDays(int days) {
        this.days = days;
        return this;
    }

    public DurationGoal createDurationGoal() {
        return DurationGoal.createDurationGoal(purpose, duration, goalUnit, archived, profile, days);
    }
}