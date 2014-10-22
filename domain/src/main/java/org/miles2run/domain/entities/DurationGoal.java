package org.miles2run.domain.entities;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = GoalType.Constants.DURATION_GOAL)
@Access(AccessType.FIELD)
public class DurationGoal extends Goal {

    private int days;

    protected DurationGoal() {
    }

    private DurationGoal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile, int days) {
        super(purpose, duration, goalUnit, archived, profile);
        this.days = days;
    }

    static DurationGoal createDurationGoal(String purpose, Duration duration, GoalUnit goalUnit, boolean archived, Profile profile, int days) {
        return new DurationGoal(purpose, duration, goalUnit, archived, profile, days);
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
