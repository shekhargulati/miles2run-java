package org.miles2run.business.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "Goal.findAllForProfile", query = "SELECT new Goal(g.id,g.purpose,g.targetDate,g.goal, g.goalUnit, g.active) FROM Goal g where g.profile =:profile"),
                @NamedQuery(name = "Goal.findGoalWithIdAndProfile", query = "SELECT new Goal(g.id,g.purpose,g.targetDate,g.goal, g.goalUnit, g.active) FROM Goal g where g.profile =:profile and g.id =:goalId")
        }
)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @FormParam("purpose")
    private String purpose;

    @FormParam("targetDate")
    @Transient
    private String targetDateStr;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date targetDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private final Date createdAt = new Date();

    @ManyToOne
    @NotNull
    private Profile profile;

    @NotNull
    @FormParam("goal")
    private long goal = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @FormParam("goalUnit")
    private GoalUnit goalUnit = GoalUnit.KMS;

    private boolean active = false;

    public Goal() {
    }

    public Goal(Long id, String purpose, Date targetDate, long goal, GoalUnit goalUnit, boolean active) {
        this.id = id;
        this.purpose = purpose;
        this.targetDate = targetDate;
        this.goalUnit = goalUnit;
        this.goal = goal / this.goalUnit.getConversion();
        this.active = active;
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

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    public GoalUnit getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(GoalUnit goalUnit) {
        this.goalUnit = goalUnit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "purpose='" + purpose + '\'' +
                ", targetDate=" + targetDate +
                ", goal=" + goal +
                ", goalUnit=" + goalUnit +
                ", active=" + active +
                '}';
    }

    public Date getTargetDateFromString() {
        if (this.targetDateStr == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(this.targetDateStr);
        } catch (ParseException e) {
            return null;
        }
    }

}
