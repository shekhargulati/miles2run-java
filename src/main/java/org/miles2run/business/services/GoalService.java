package org.miles2run.business.services;

import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import org.miles2run.business.vo.ActivityDetails;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Stateless
public class GoalService {

    @Inject
    private EntityManager entityManager;
    @Inject
    private ProfileService profileService;

    public List<Goal> findAllGoalsForProfile(String loggedInuser) {
        Profile profile = profileService.findProfile(loggedInuser);
        TypedQuery<Goal> query = entityManager.createNamedQuery("Goal.findAllForProfile", Goal.class);
        query.setParameter("profile", profile);
        return query.getResultList();
    }


    public Goal save(Goal goal, Profile profile) {
        goal.setProfile(profile);
        entityManager.persist(goal);
        return goal;
    }

    public Goal findGoal(String loggedInuser, Long goalId) {
        try {
            Profile profile = profileService.findProfile(loggedInuser);
            TypedQuery<Goal> query = entityManager.createNamedQuery("Goal.findGoalWithIdAndProfile", Goal.class);
            query.setParameter("profile", profile);
            query.setParameter("goalId", goalId);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Goal findGoal(Profile profile, Long goalId) {
        try {
            TypedQuery<Goal> query = entityManager.createNamedQuery("Goal.findGoalWithIdAndProfile", Goal.class);
            query.setParameter("profile", profile);
            query.setParameter("goalId", goalId);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Goal find(Long goalId) {
        return entityManager.find(Goal.class, goalId);
    }
}
