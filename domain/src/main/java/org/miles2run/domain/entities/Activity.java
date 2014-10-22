package org.miles2run.domain.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Access(AccessType.FIELD)
@Table(name = "activity")
public class Activity extends BaseEntity {

    @Size(max = 1000)
    private String status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit;

    @NotNull
    private double distanceCovered;

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date activityDate;

    @ManyToOne
    private Profile postedBy;

    private long duration;

    @ManyToOne
    private Goal goal;

    protected Activity() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public double getDistanceCovered() {
        return distanceCovered;
    }

    public void setDistanceCovered(double distanceCovered) {
        this.distanceCovered = distanceCovered;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    public Profile getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(Profile postedBy) {
        this.postedBy = postedBy;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    @Override
    public String toString() {
        return "Activity{" + "activityDate=" + activityDate + ", distanceCovered=" + distanceCovered + ", goalUnit=" + goalUnit + '}';
    }
}
