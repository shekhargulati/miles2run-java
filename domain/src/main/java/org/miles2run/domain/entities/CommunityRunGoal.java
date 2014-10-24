package org.miles2run.domain.entities;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = GoalType.Constants.COMMUNITY_RUN)
@Access(AccessType.FIELD)
public class CommunityRunGoal extends Goal {

    @ManyToOne
    @JoinColumn(name = "cr_id")
    private CommunityRun communityRun;

    protected CommunityRunGoal() {
    }

    private CommunityRunGoal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile, CommunityRun communityRun) {
        super(purpose, duration, goalUnit, archived, profile);
        this.communityRun = communityRun;
    }

    static CommunityRunGoal createCommunityRunGoal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile, CommunityRun communityRun) {
        return new CommunityRunGoal(purpose, duration, goalUnit, archived, profile, communityRun);
    }

    public CommunityRun getCommunityRun() {
        return communityRun;
    }

    public void setCommunityRun(CommunityRun communityRun) {
        this.communityRun = communityRun;
    }
}
