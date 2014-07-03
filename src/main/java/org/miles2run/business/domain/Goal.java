package org.miles2run.business.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "Goal.findAllWithProfileAndArchive", query = "SELECT new Goal(g.id,g.purpose,g.targetDate,g.distance, g.goalUnit, g.archived) FROM Goal g where g.profile =:profile and g.archived =:archived"),
                @NamedQuery(name = "Goal.findGoalWithIdAndProfile", query = "SELECT new Goal(g.id,g.purpose,g.targetDate,g.distance, g.goalUnit, g.archived) FROM Goal g where g.profile =:profile and g.id =:goalId"),
                @NamedQuery(name = "Goal.findLastedCreatedGoal", query = "SELECT new Goal(g.id,g.purpose,g.targetDate,g.distance, g.goalUnit, g.archived) from Goal g where g.profile =:profile order by g.createdAt desc")
        }
)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String purpose;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date targetDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private final Date createdAt = new Date();

    @ManyToOne
    private Profile profile;

    @NotNull
    private long distance = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalUnit goalUnit = GoalUnit.KM;

    private boolean archived = false;

    public Goal() {
    }

    public Goal(Long id, String purpose, Date targetDate, long distance, GoalUnit goalUnit, boolean archived) {
        this.id = id;
        this.purpose = purpose;
        this.targetDate = targetDate;
        this.goalUnit = goalUnit;
        this.distance = distance;
        this.archived = archived;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }

    public Date getCreatedAt() {
        return createdAt;
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

    public void setDistance(long goal) {
        this.distance = goal;
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

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", purpose='" + purpose + '\'' +
                ", targetDate=" + targetDate +
                ", createdAt=" + createdAt +
                ", goal=" + distance +
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
}
