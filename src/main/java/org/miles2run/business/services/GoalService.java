package org.miles2run.business.services;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.domain.jpa.Profile;
import org.miles2run.business.services.jpa.CommunityRunJPAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * Created by shekhargulati on 11/06/14.
 */
@Stateless
public class GoalService {

    private Logger logger = LoggerFactory.getLogger(GoalService.class);

    @Inject
    private EntityManager entityManager;
    @Inject
    private ProfileService profileService;
    @Inject
    private JedisExecutionService jedisExecutionService;
    @Inject
    private CommunityRunJPAService communityRunJPAService;

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

    public Long save(Goal goal, String loggedInUser) {
        Profile profile = profileService.findProfile(loggedInUser);
        goal.setProfile(profile);
        entityManager.persist(goal);
        return goal.getId();
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
        existingGoal.setStartDate(goal.getStartDate());
        existingGoal.setEndDate(goal.getEndDate());
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

    public void updateTotalDistanceCoveredForAGoal(final Long goalId, final double distanceCovered) {
        logger.info("Updating goal with id {} with distance {}", goalId, distanceCovered);
        jedisExecutionService.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String key = String.format("goal:%s:progress", goalId);
                jedis.incrByFloat(key, distanceCovered);
                return null;
            }
        });
    }

    public double totalDistanceCoveredForGoal(final Long goalId) {
        return jedisExecutionService.execute(new JedisOperation<Double>() {
            @Override
            public Double perform(Jedis jedis) {
                String key = String.format("goal:%s:progress", goalId);
                String value = jedis.get(key);
                return value == null ? Double.valueOf(0) : Double.valueOf(value);
            }
        });
    }

    public Long findLatestGoalWithActivity(final String username) {
        return jedisExecutionService.execute(new JedisOperation<Long>() {
            @Override
            public Long perform(Jedis jedis) {
                Set<String> activities = jedis.zrevrange(String.format(RedisKeyNames.PROFILE_S_TIMELINE_LATEST, username), 0, -1);
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

    public Map<String, Object> getDurationGoalProgress(final String username, final Long goalId, final Interval interval) {
        return jedisExecutionService.execute(new JedisOperation<Map<String, Object>>() {
            @Override
            public Map<String, Object> perform(Jedis jedis) {
                String key = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goalId);
                DateTime dateTime = new DateTime(interval.getStartMillis()).minusDays(1);
                Set<Tuple> activityIdsInNMonthWithScores = jedis.zrangeByScoreWithScores(key, dateTime.getMillis(), interval.getEndMillis());
                Set<LocalDate> activityDates = new HashSet<>();
                for (Tuple activityIdsInNMonthWithScore : activityIdsInNMonthWithScores) {
                    activityDates.add(new LocalDate(Double.valueOf(activityIdsInNMonthWithScore.getScore()).longValue()));
                }
                int totalDays = Days.daysBetween(new LocalDate(interval.getStartMillis()), new LocalDate(interval.getEndMillis())).getDays() + 1;
                int performedDays = activityDates.size();
                int remainingDays = Days.daysBetween(new LocalDate(), new LocalDate(interval.getEndMillis())).getDays() + 1;
                List<LocalDate> dates = new ArrayList<>();
                int daysTillToday = Days.daysIn(new Interval(interval.getStartMillis(), new DateTime().getMillis())).getDays();
                for (int i = 0; i < daysTillToday; i++) {
                    dates.add(interval.getStart().toLocalDate().plusDays(i));
                }
                int missedDays = 0;
                for (LocalDate date : dates) {
                    if (!activityDates.contains(date)) {
                        missedDays += 1;
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("totalDays", totalDays);
                data.put("performedDays", performedDays);
                data.put("missedDays", missedDays);
                data.put("remainingDays", remainingDays);
                double percentage = (Double.valueOf(performedDays) * 100) / totalDays;
                data.put("percentage", percentage);
                return data;
            }
        });
    }

    public Long findGoalIdWithCommunityRunAndProfile(String slug, Profile profile) {
        CommunityRun communityRun = communityRunJPAService.find(slug);
        List<Long> list = entityManager.createQuery("SELECT g.id FROM Goal g where g.communityRun =:communityRun  and g.profile =:profile and g.archived is FALSE", Long.class).setParameter("communityRun", communityRun).setParameter("profile", profile).getResultList();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void archiveGoalWithCommunityRun(CommunityRun communityRun) {
        Goal goal = entityManager.createQuery("SELECT g from Goal g where g.communityRun =:communityRun and g.archived is FALSE", Goal.class).setParameter("communityRun", communityRun).getSingleResult();
        goal.setArchived(true);
        entityManager.merge(goal);
        entityManager.flush();
    }
}
