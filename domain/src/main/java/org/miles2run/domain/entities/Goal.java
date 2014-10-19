package org.miles2run.domain.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Access(AccessType.FIELD)
@Table(name = "goal")
public class Goal extends BaseEntity {

    @NotNull
    private String purpose;

    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date startDate;

    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date endDate;

    @ManyToOne
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "communityRun_Id")
    private CommunityRun communityRun;

    private long distance = 0;

    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit = GoalUnit.MI;

    @Enumerated(EnumType.STRING)
    @NotNull
    private GoalType goalType;

    private boolean archived = false;

    public Goal() {
    }

    public Goal(Goal goal) {
        this.id = goal.id;
        this.version = goal.version;
        this.purpose = goal.purpose;
        this.startDate = goal.startDate;
        this.endDate = goal.endDate;
        this.goalUnit = goal.goalUnit;
        this.distance = goal.distance;
        this.archived = goal.archived;
        this.goalType = goal.goalType;
        if (goal.communityRun != null) {
            this.communityRun = new CommunityRun(goal.communityRun);
        }
    }

    public Goal(Long id, Long version, String purpose, Date startDate, Date endDate, long distance, GoalUnit goalUnit, boolean archived, GoalType goalType) {
        this.id = id;
        this.version = version;
        this.purpose = purpose;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalUnit = goalUnit;
        this.distance = distance;
        this.archived = archived;
        this.goalType = goalType;
    }

    public static Goal of(Long id, long distance, GoalUnit goalUnit) {
        Goal goal = new Goal();
        goal.id = id;
        goal.distance = distance;
        goal.goalUnit = goalUnit;
        return goal;
    }

    public static Goal newCommunityRunGoal(CommunityRun communityRun) {
        Goal goal = new Goal();
        goal.purpose = communityRun.getName() + " Community Run";
        goal.startDate = new Date();
        goal.endDate = communityRun.getEndDate();
        goal.goalType = GoalType.COMMUNITY_RUN_GOAL;
        goal.communityRun = communityRun;
        return goal;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date targetDate) {
        this.endDate = targetDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public CommunityRun getCommunityRun() {
        return communityRun;
    }

    public void setCommunityRun(CommunityRun communityRun) {
        this.communityRun = communityRun;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", purpose='" + purpose + '\'' +
                ", targetDate=" + endDate +
                ", createdAt=" + createdAt +
                ", distance=" + distance +
                ", goalUnit=" + goalUnit +
                ", archived=" + archived +
                '}';
    }
}
