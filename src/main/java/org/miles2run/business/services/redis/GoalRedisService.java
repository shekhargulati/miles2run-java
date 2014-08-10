package org.miles2run.business.services.redis;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Created by shekhargulati on 11/06/14.
 */
@ApplicationScoped
public class GoalRedisService {

    private Logger logger = LoggerFactory.getLogger(GoalRedisService.class);

    @Inject
    JedisExecutionService jedisExecutionService;

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

    public Map<String, Object> getDurationGoalProgress(final String username, final Long goalId, final Interval goalInterval) {
        final int totalDays = calculateTotalDays(goalInterval);
        DateTime today = today();
        if (today.isBefore(goalInterval.getStart())) {
            final Map<String, Object> goalProgress = new HashMap<>();
            goalProgress.put("totalDays", totalDays);
            goalProgress.put("performedDays", 0);
            goalProgress.put("missedDays", 0);
            goalProgress.put("remainingDays", calculateRemainingDaysWithOffset(goalInterval.getStart().toLocalDate(), goalInterval.getEnd().toLocalDate()));
            goalProgress.put("percentage", 0.0d);
            return goalProgress;
        }
        final int remainingDays = calculateRemainingDays(today.toLocalDate(), goalInterval.getEnd().toLocalDate());
        final Set<Tuple> activitiesPerformed = activitiesPerformedWithinAGoalInterval(username, goalId, goalInterval);
        final Set<LocalDate> performedActivityDates = toCollectionOfPerformedActivityDates(activitiesPerformed);
        final int performedDays = performedActivityDates.size();
        final Set<LocalDate> datesTillToday = allDatesWithin(goalInterval.getStart(), today);
        final int missedDays = calculateMissedDays(performedActivityDates, datesTillToday);

        final Map<String, Object> goalProgress = new HashMap<>();
        goalProgress.put("totalDays", totalDays);
        goalProgress.put("performedDays", performedDays);
        goalProgress.put("missedDays", missedDays);
        goalProgress.put("remainingDays", remainingDays);
        double percentage = (Double.valueOf(performedDays) * 100) / totalDays;
        goalProgress.put("percentage", percentage);
        return goalProgress;
    }

    private int calculateRemainingDaysWithOffset(LocalDate start, LocalDate end) {
        int remainingDays = Days.daysBetween(start, end).getDays();
        return remainingDays < 0 ? 0 : remainingDays + 1;
    }

    DateTime today() {
        return new DateTime();
    }

    int calculateMissedDays(Set<LocalDate> performedActivityDates, Set<LocalDate> allDates) {
        int missedDays = 0;
        for (LocalDate date : allDates) {
            if (!performedActivityDates.contains(date)) {
                missedDays += 1;
            }
        }
        return missedDays;
    }

    Set<LocalDate> allDatesWithin(DateTime start, DateTime end) {
        int numberOfDays = Days.daysBetween(start.toLocalDate(), end.toLocalDate()).getDays();
        if (numberOfDays < 0) {
            return Collections.emptySet();
        }
        int numberOfDaysWithOffset = numberOfDays + 1;
        Set<LocalDate> datesTillToday = new LinkedHashSet<>();
        for (int i = 0; i < numberOfDaysWithOffset; i++) {
            LocalDate ithDate = start.toLocalDate().plusDays(i);
            datesTillToday.add(ithDate);
        }
        return datesTillToday;
    }

    int calculateRemainingDays(LocalDate startDate, LocalDate endDate) {
        int remainingDays = Days.daysBetween(startDate, endDate).getDays();
        return remainingDays < 0 ? 0 : remainingDays;
    }

    int calculateTotalDays(Interval goalInterval) {
        return Days.daysBetween(goalInterval.getStart().toLocalDate(), goalInterval.getEnd().toLocalDate()).getDays() + 1;
    }

    Set<LocalDate> toCollectionOfPerformedActivityDates(Set<Tuple> activitiesPerformed) {
        Set<LocalDate> performedActivityDates = new HashSet<>();
        for (Tuple activityIdAndScore : activitiesPerformed) {
            performedActivityDates.add(new LocalDate(Double.valueOf(activityIdAndScore.getScore()).longValue()));
        }
        return performedActivityDates;
    }

    Set<Tuple> activitiesPerformedWithinAGoalInterval(final String username, final Long goalId, final Interval goalInterval) {
        return jedisExecutionService.execute(new JedisOperation<Set<Tuple>>() {
            @Override
            public Set<Tuple> perform(Jedis jedis) {
                String profileGoalTimelineKey = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goalId);
                DateTime goalStartDateWithOneDayOffset = new DateTime(goalInterval.getStartMillis()).minusDays(1);
                Set<Tuple> activitiesPerformed = jedis.zrangeByScoreWithScores(profileGoalTimelineKey, goalStartDateWithOneDayOffset.getMillis(), goalInterval.getEndMillis());
                return activitiesPerformed;
            }
        });
    }

}
