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

    private DistanceGoal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile, long distance) {
        super(purpose, duration, goalUnit, archived, profile);
        this.distance = distance;
    }

    static DistanceGoal createDistanceGoal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile, long distance) {
        return new DistanceGoal(purpose, duration, goalUnit, archived, profile, distance);
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
