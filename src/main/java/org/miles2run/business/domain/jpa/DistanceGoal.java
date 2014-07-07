package org.miles2run.business.domain.jpa;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by shekhargulati on 07/07/14.
 */
@Entity
@Table(name = "distance_goal")
public class DistanceGoal extends NewGoal {

    @NotNull
    private long distance;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit = GoalUnit.KM;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DistanceGoal{");
        sb.append("id=").append(this.id);
        sb.append(", purpose=").append(this.purpose);
        sb.append(", distance=").append(distance);
        sb.append(", goalUnit=").append(goalUnit);
        sb.append(", endDate=").append(endDate);
        sb.append('}');
        return sb.toString();
    }
}
