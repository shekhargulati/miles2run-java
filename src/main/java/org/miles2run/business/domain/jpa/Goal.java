package org.miles2run.business.domain.jpa;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "Goal.findAllWithProfileAndArchive", query = "SELECT new Goal(g.id,g.version, g.purpose,g.startDate,g.endDate,g.distance, g.goalUnit, g.archived,g.goalType) FROM Goal g where g.profile =:profile and g.archived =:archived"),
                @NamedQuery(name = "Goal.findGoalWithIdAndProfile", query = "SELECT new Goal(g.id,g.version,g.purpose,g.startDate,g.endDate,g.distance, g.goalUnit, g.archived,g.goalType,g.communityRun) FROM Goal g where g.profile =:profile and g.id =:goalId"),
                @NamedQuery(name = "Goal.findLastedCreatedGoal", query = "SELECT new Goal(g.id,g.version,g.purpose,g.startDate,g.endDate,g.distance, g.goalUnit, g.archived,g.goalType) from Goal g where g.profile =:profile order by g.createdAt desc")
        }
)
@Access(AccessType.FIELD)
@Table(name = "goal")
public class Goal extends BaseEntity {

    @NotNull
    private String purpose;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @ManyToOne
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "communityRun_Id")
    private CommunityRun communityRun;

    private long distance = 0;

    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit = GoalUnit.KM;

    @Enumerated(EnumType.STRING)
    @NotNull
    private GoalType goalType;

    private boolean archived = false;

    public Goal() {
    }

    public Goal(Long id, Long version, String purpose, Date startDate, Date endDate, long distance, GoalUnit goalUnit, boolean archived, GoalType goalType, CommunityRun communityRun) {
        this.id = id;
        this.version = version;
        this.purpose = purpose;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalUnit = goalUnit;
        this.distance = distance;
        this.archived = archived;
        this.goalType = goalType;
        this.communityRun = communityRun;
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

    public String getPurpose() {
        return purpose;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public long getDistance() {
        return distance;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setEndDate(Date targetDate) {
        this.endDate = targetDate;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
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

    public static Goal of(Long id, long distance, GoalUnit goalUnit) {
        Goal goal = new Goal();
        goal.id = id;
        goal.distance = distance;
        goal.goalUnit = goalUnit;
        return goal;
    }

    public static Goal newCommunityRunGoal(CommunityRun run) {
        Goal goal = new Goal();
        goal.purpose = run.getName() + " Community Run";
        goal.startDate = new Date();
        goal.endDate = run.getEndDate();
        goal.goalType = GoalType.COMMUNITY_RUN_GOAL;
        goal.setCommunityRun(run);
        return goal;
    }
}
