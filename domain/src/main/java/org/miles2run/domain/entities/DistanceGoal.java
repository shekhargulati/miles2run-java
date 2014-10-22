package org.miles2run.domain.entities;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = GoalType.Constants.DISTANCE_GOAL)
@Access(AccessType.FIELD)
public class DistanceGoal extends Goal {

    private long distance = 0;

    protected DistanceGoal() {
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
