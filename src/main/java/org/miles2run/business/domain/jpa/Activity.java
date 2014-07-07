package org.miles2run.business.domain.jpa;

import org.miles2run.business.vo.ActivityDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by shekhargulati on 04/03/14.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Activity.findAll", query = "SELECT NEW org.miles2run.business.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.share,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt) FROM Activity a WHERE a.postedBy =:postedBy ORDER BY a.activityDate DESC"),
        @NamedQuery(name = "Activity.findById", query = "SELECT new org.miles2run.business.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.share,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt) from Activity a where a.id =:id"),
        @NamedQuery(name = "Activity.countByProfile", query = "SELECT COUNT(a) FROM Activity a WHERE a.postedBy =:profile"),
        @NamedQuery(name = "Activity.countByProfileAndGoal", query = "SELECT COUNT(a) FROM Activity a WHERE a.postedBy =:profile and a.goal =:goal"),
        @NamedQuery(name = "Activity.userGoalProgress", query = "SELECT new org.miles2run.business.vo.Progress(a.goal.distance,a.goal.goalUnit,SUM(a.distanceCovered),COUNT(a), SUM(a.duration) ) from Activity a WHERE a.postedBy =:postedBy and a.goal =:goal"),
        @NamedQuery(name = "Activity.findByUsernameAndId", query = "SELECT new org.miles2run.business.vo.ActivityDetails(a.id,a.status,a.distanceCovered,a.goalUnit,a.activityDate,a.share,a.postedBy.fullname,a.duration,a.postedBy.username,a.postedBy.profilePic,a.createdAt) from Activity a where a.id =:activityId and a.postedBy =:profile")

})
@Access(AccessType.FIELD)
@Table(name = "activity")
public class Activity extends BaseEntity{

    @Size(max = 1000)
    private String status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit;

    @NotNull
    private double distanceCovered;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date activityDate;

    @ManyToOne
    private Profile postedBy;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Share share;

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

    public Activity(ActivityDetails activityDetails) {
        this.id = activityDetails.getId();
        this.goalUnit = activityDetails.getGoalUnit();
        this.distanceCovered = activityDetails.getDistanceCovered();
        this.activityDate = activityDetails.getActivityDate();
        this.duration = activityDetails.getDuration();
    }

    public String getStatus() {
        return status;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public double getDistanceCovered() {
        return distanceCovered;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public Profile getPostedBy() {
        return postedBy;
    }

    public Share getShare() {
        return share;
    }

    public long getDuration() {
        return duration;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public void setDistanceCovered(double distanceCovered) {
        this.distanceCovered = distanceCovered;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    public void setPostedBy(Profile postedBy) {
        this.postedBy = postedBy;
    }

    public void setShare(Share share) {
        this.share = share;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }
}
