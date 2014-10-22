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

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
