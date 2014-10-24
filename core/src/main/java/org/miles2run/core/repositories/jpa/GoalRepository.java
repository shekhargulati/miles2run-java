package org.miles2run.core.repositories.jpa;

import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.List;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GoalRepository {

    private Logger logger = LoggerFactory.getLogger(GoalRepository.class);

    @Inject
    private EntityManager entityManager;

    public <T extends Goal> T save(final T goal) {
        entityManager.persist(goal);
        return goal;
    }

    public List<Goal> findAll(final Profile profile, final boolean archived) {
        final String findByProfileAndArchiveQuery = "SELECT g FROM Goal g where g.profile =:profile and g.archived =:archived";
        return entityManager.createQuery(findByProfileAndArchiveQuery, Goal.class)
                .setParameter("profile", profile)
                .setParameter("archived", archived)
                .getResultList();
    }

    public Goal find(final Profile profile, final Long goalId) {
        try {
            final String goalByProfileAndGoalIdQuery = "SELECT g FROM Goal g where g.profile =:profile and g.id =:goalId";
            return entityManager.createQuery(goalByProfileAndGoalIdQuery, Goal.class).setParameter("profile", profile).setParameter("goalId", goalId).getSingleResult();
        } catch (NoResultException e) {
            logger.warn("No goal exists for profile {} and goalId {}", profile.getUsername(), goalId);
            return null;
        }
    }

    public Goal find(Long goalId) {
        return entityManager.find(Goal.class, goalId);
    }

    public <T extends Goal> T find(Class<T> type, Long goalId) {
        return entityManager.find(type, goalId);
    }

    public <T extends Goal> void update(T goal) {
        T merged = entityManager.merge(goal);
        entityManager.persist(merged);
    }

    public Goal findLatestCreatedGoal(Profile profile) {
        final String lastCreatedGoalQuery = "SELECT g from Goal g where g.profile =:profile order by g.createdAt desc";
        TypedQuery<Goal> query = entityManager.createQuery(lastCreatedGoalQuery, Goal.class);
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
