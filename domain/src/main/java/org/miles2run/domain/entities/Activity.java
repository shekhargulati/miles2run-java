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

    public Activity() {
    }

    public Activity(String status, double distanceCovered, Profile postedBy) {
        this.status = status;
        this.distanceCovered = distanceCovered;
        this.postedBy = postedBy;
    }

    public Activity(Long id, String status, double distanceCovered) {
        this.id = id;
        this.status = status;
        this.distanceCovered = distanceCovered;
    }

    public Activity(Long id, String status, double distanceCovered, Date postedAt) {
        this.id = id;
        this.status = status;
        this.distanceCovered = distanceCovered;
        this.createdAt = postedAt;
    }

    public Activity(Date activityDate, double distanceCovered, GoalUnit goalUnit) {
        this.activityDate = activityDate;
        this.distanceCovered = distanceCovered;
        this.goalUnit = goalUnit;
    }

    public Activity(String status, double distanceCovered, GoalUnit goalUnit, long duration, Date activityDate) {
        this.status = status;
        this.distanceCovered = distanceCovered;
        this.goalUnit = goalUnit;
        this.duration = duration;
        this.activityDate = activityDate;

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
        final StringBuilder sb = new StringBuilder("Activity{");
        sb.append("activityDate=").append(activityDate);
        sb.append(", distanceCovered=").append(distanceCovered);
        sb.append(", goalUnit=").append(goalUnit);
        sb.append('}');
        return sb.toString();
    }
}
