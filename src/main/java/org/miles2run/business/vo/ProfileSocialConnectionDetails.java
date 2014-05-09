package org.miles2run.business.vo;

import org.miles2run.business.domain.GoalUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shekhargulati on 12/03/14.
 */
public class ProfileSocialConnectionDetails {
    private Long id;
    private String username;
    private final List<String> providers = new ArrayList<>();
    private long goal;
    private GoalUnit goalUnit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getProviders() {
        return providers;
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
}
