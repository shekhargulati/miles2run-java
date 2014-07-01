package org.miles2run.business.services;

import org.miles2run.business.domain.Goal;
import org.miles2run.business.domain.Profile;
import redis.clients.jedis.Jedis;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Stateless
public class GoalService {

    @Inject
    private EntityManager entityManager;
    @Inject
    private ProfileService profileService;
    @Inject
    private JedisExecutionService jedisExecutionService;
    @Inject
    private Logger logger;

    public List<Goal> findAllGoals(String loggedInuser, boolean archived) {
        Profile profile = profileService.findProfile(loggedInuser);
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

    public void update(Goal goal, Long goalId) {
        Goal existingGoal = this.find(goalId);
        existingGoal.setDistance(goal.getDistance());
        existingGoal.setTargetDate(goal.getTargetDate());
        existingGoal.setArchived(goal.isArchived());
        existingGoal.setGoalUnit(goal.getGoalUnit());
        existingGoal.setPurpose(goal.getPurpose());
        entityManager.persist(existingGoal);
    }

    public void delete(Long goalId) {
        entityManager.remove(this.find(goalId));
    }

    public void updatedArchiveStatus(Long goalId, boolean archived) {
        Goal goal = this.find(goalId);
        goal.setArchived(archived);
        entityManager.persist(goal);
    }

    public void updateTotalDistanceCoveredForAGoal(final Long goalId, final long distanceCovered) {
        logger.info(String.format("Updating goal with id %d with distance %d", goalId, distanceCovered));
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String key = String.format("goal:%s:progress", goalId);
                jedis.incrBy(key, distanceCovered);
                return null;
            }
        });
    }

    public long totalDistanceCoveredForGoal(final Long goalId) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                String key = String.format("goal:%s:progress", goalId);
                String value = jedis.get(key);
                return value == null ? Long.valueOf(0) : Long.valueOf(value);
            }
        });
    }

    public Long findLatestGoalWithActivity(final String username) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                Set<String> activities = jedis.zrevrange(String.format(TimelineService.PROFILE_S_TIMELINE_LATEST, username), 0, -1);
                if (activities != null && !activities.isEmpty()) {
                    String latestActivityId = activities.iterator().next();
                    String goalId = jedis.hget(String.format("activity:%s", latestActivityId), "goalId");
                    return Long.valueOf(goalId);
                }
                return null;
            }
        });
    }

    public Goal findLatestCreatedGoal(String username) {
        Profile profile = profileService.findProfile(username);
        TypedQuery<Goal> query = entityManager.createNamedQuery("Goal.findLastedCreatedGoal", Goal.class);
        query.setParameter("profile", profile);
        query.setMaxResults(1);
        List<Goal> goals = query.getResultList();
        return goals.isEmpty() ? null : goals.get(0);
    }
}
