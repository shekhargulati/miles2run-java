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

    public CommunityRun getCommunityRun() {
        return communityRun;
    }

    public void setCommunityRun(CommunityRun communityRun) {
        this.communityRun = communityRun;
    }
}
