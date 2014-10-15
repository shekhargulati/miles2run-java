package org.miles2run.business.services.jpa;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Stateless
public class GoalJPAService {

    private Logger logger = LoggerFactory.getLogger(GoalJPAService.class);

    @Inject
    private EntityManager entityManager;

    public List<Goal> findAllGoals(Profile profile, boolean archived) {
        TypedQuery<Goal> query = entityManager.createNamedQuery("Goal.findAllWithProfileAndArchive", Goal.class);
        query.setParameter("profile", profile);
        query.setParameter("archived", archived);
        return query.getResultList();
    }

    public Goal save(Goal goal, Profile profile) {
        goal.setProfile(profile);
        entityManager.persist(goal);
        return findGoal(profile, goal.getId());
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

    public void update(Goal goal, Long goalId) {
        Goal existingGoal = this.find(goalId);
        existingGoal.setDistance(goal.getDistance());
        existingGoal.setStartDate(goal.getStartDate());
        existingGoal.setEndDate(goal.getEndDate());
        existingGoal.setArchived(goal.isArchived());
        existingGoal.setGoalUnit(goal.getGoalUnit());
        existingGoal.setPurpose(goal.getPurpose());
        entityManager.persist(existingGoal);
    }

    public Goal find(Long goalId) {
        return entityManager.find(Goal.class, goalId);
    }

    public void delete(Long goalId) {
        entityManager.remove(this.find(goalId));
    }

    public void updatedArchiveStatus(Long goalId, boolean archived) {
        Goal goal = this.find(goalId);
        goal.setArchived(archived);
        entityManager.persist(goal);
    }

    public Goal findLatestCreatedGoal(Profile profile) {
        TypedQuery<Goal> query = entityManager.createNamedQuery("Goal.findLastedCreatedGoal", Goal.class);
        query.setParameter("profile", profile);
        query.setMaxResults(1);
        List<Goal> goals = query.getResultList();
        return goals.isEmpty() ? null : goals.get(0);
    }

    public Long findGoalIdWithCommunityRunAndProfile(CommunityRun communityRun, Profile profile) {
        List<Long> list = entityManager.createQuery("SELECT g.id FROM Goal g where g.communityRun =:communityRun  and g.profile =:profile and g.archived is FALSE", Long.class).setParameter("communityRun", communityRun).setParameter("profile", profile).getResultList();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void archiveGoalWithCommunityRun(CommunityRun communityRun, Profile profile) {
        Goal goal = entityManager.createQuery("SELECT g from Goal g where g.communityRun =:communityRun and g.profile =:profile and g.archived is FALSE", Goal.class).setParameter("communityRun", communityRun).setParameter("profile", profile).getSingleResult();
        goal.setArchived(true);
        entityManager.merge(goal);
        entityManager.flush();
    }

    public long countOfActiveGoalCreatedByUser(@NotNull Profile profile) {
        return entityManager.createQuery("SELECT count(g) FROM Goal g where g.profile =:profile and g.archived is FALSE", Long.class).setParameter("profile", profile).getSingleResult();
    }
}
