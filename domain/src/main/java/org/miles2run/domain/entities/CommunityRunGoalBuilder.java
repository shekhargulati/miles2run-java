package org.miles2run.domain.entities;

public class CommunityRunGoalBuilder {
    private String purpose;
    private Duration duration;
    private GoalUnit goalUnit;
    private boolean archived;
    private Profile profile;
    private CommunityRun communityRun;

    public CommunityRunGoalBuilder setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public CommunityRunGoalBuilder setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public CommunityRunGoalBuilder setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
        return this;
    }

    public CommunityRunGoalBuilder setArchived(boolean archived) {
        this.archived = archived;
        return this;
    }

    public CommunityRunGoalBuilder setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public CommunityRunGoalBuilder setCommunityRun(CommunityRun communityRun) {
        this.communityRun = communityRun;
        return this;
    }

    public CommunityRunGoal createCommunityRunGoal() {
        return CommunityRunGoal.createCommunityRunGoal(purpose, duration, goalUnit, archived, profile, communityRun);
    }
}