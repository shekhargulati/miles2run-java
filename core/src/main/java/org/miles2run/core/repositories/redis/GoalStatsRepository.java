package org.miles2run.core.repositories.redis;

import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class GoalStatsRepository {

    private final Logger logger = LoggerFactory.getLogger(GoalStatsRepository.class);

    @Inject
    JedisExecution jedisExecution;

    public void updateTotalDistanceCoveredForAGoal(final Long goalId, final double distanceCovered) {
        logger.info("Updating goal with id {} with distance {}", goalId, distanceCovered);
        jedisExecution.execute(new JedisOperation<Void>() {
            @Override
            public Void perform(Jedis jedis) {
                String key = String.format("goal:%s:progress", goalId);
                jedis.incrByFloat(key, distanceCovered);
                return null;
            }
        });
    }

    public double distanceCovered(final Long goalId) {
        return jedisExecution.execute(new JedisOperation<Double>() {
            @Override
            public Double perform(Jedis jedis) {
                String key = String.format("goal:%s:progress", goalId);
                String value = jedis.get(key);
                return value == null ? Double.valueOf(0) : Double.valueOf(value);
            }
        });
    }

    public Map<String, Object> getDurationGoalProgress(final String username, final Long goalId, final Interval goalInterval, int timezoneOffsetInMinutes) {
        int timezoneOffsetInMillis = (-1) * timezoneOffsetInMinutes * 60 * 1000;
        DateTimeZone dateTimeZone = timezoneOffsetInMillis == 0 ? DateTimeZone.forID("UTC") : DateTimeZone.forOffsetMillis(timezoneOffsetInMillis);
        logger.info("User {} with DateTimeZone {}", username, dateTimeZone.toTimeZone());
        final DateTime startDateTimeInUserTimezone = goalInterval.getStart().toDateTime(dateTimeZone);
        final DateTime endDateTimeInUserTimezone = goalInterval.getEnd().toDateTime(dateTimeZone);
        logger.info("Calculating Goal-{} progress between {} and {}", goalId, startDateTimeInUserTimezone, endDateTimeInUserTimezone);

        final int totalDays = calculateTotalDays(startDateTimeInUserTimezone, endDateTimeInUserTimezone);
        logger.info("For {}-goal-{} totalDays {}", username, goalId, totalDays);

        DateTime current = today(dateTimeZone);
        if (current.isBefore(startDateTimeInUserTimezone)) {
            logger.info("For {}-goal-{} current_datetime {} is before startdatetime {} so returning empty progress", username, goalId, current, startDateTimeInUserTimezone);
            final Map<String, Object> goalProgress = new HashMap<>();
            goalProgress.put("totalDays", totalDays);
            goalProgress.put("performedDays", 0);
            goalProgress.put("missedDays", 0);
            goalProgress.put("remainingDays", calculateRemainingDaysWithOffset(startDateTimeInUserTimezone.toLocalDate(), endDateTimeInUserTimezone.toLocalDate()));
            goalProgress.put("percentage", 0.0d);
            return goalProgress;
        }
        final Set<Tuple> activitiesPerformed = activitiesPerformedWithinAGoalInterval(username, goalId, goalInterval);
        logger.info("{}-goal-{} TotalActivitiesPerformed : {}", username, goalId, activitiesPerformed.size());
        final Set<LocalDate> performedActivityDates = toCollectionOfPerformedActivityDates(activitiesPerformed, dateTimeZone);
        final int performedDays = performedActivityDates.size();
        logger.info("{}-goal-{} ActivityPerformedDays :{}", username, goalId, performedDays);

        boolean activityPerformedTodayExists = isActivityPerformedToday(performedActivityDates, dateTimeZone);
        DateTime today = today(activityPerformedTodayExists, dateTimeZone);
        final int remainingDays = calculateRemainingDays(today.toLocalDate(), endDateTimeInUserTimezone.toLocalDate());
        logger.info("{}-goal-{} Remaining days between start {} and end {} is {}", username, goalId, today.toLocalDate(), endDateTimeInUserTimezone.toLocalDate(), remainingDays);
        final int missedDays = totalDays - (performedDays + remainingDays);
        logger.info("{}-goal-{} Missing Days '{} - ({} + {}) == {}'", username, goalId, totalDays, performedDays, remainingDays, missedDays);

        final Map<String, Object> goalProgress = new HashMap<>();
        goalProgress.put("totalDays", totalDays);
        goalProgress.put("performedDays", performedDays);
        goalProgress.put("missedDays", missedDays);
        goalProgress.put("remainingDays", remainingDays);
        double percentage = ((double) performedDays * 100) / totalDays;
        goalProgress.put("percentage", percentage);
        return goalProgress;
    }

    private boolean isActivityPerformedToday(Set<LocalDate> performedActivityDates, DateTimeZone dateTimeZone) {
        LocalDate today = new LocalDate(dateTimeZone);
        for (LocalDate performedActivityDate : performedActivityDates) {
            if (today.equals(performedActivityDate)) {
                return true;
            }
        }
        return false;
    }

    private int calculateRemainingDaysWithOffset(LocalDate start, LocalDate end) {
        int remainingDays = Days.daysBetween(start, end).getDays();
        return remainingDays < 0 ? 0 : remainingDays + 1;
    }

    DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
        DateTime today = new DateTime(dateTimeZone);
        return activityPerformedTodayExists ? today : today.minusDays(1);
    }

    DateTime today(DateTimeZone dateTimeZone) {
        return new DateTime(dateTimeZone);
    }

    int calculateRemainingDays(LocalDate startDate, LocalDate endDate) {
        int remainingDays = Days.daysBetween(startDate, endDate).getDays();
        return remainingDays < 0 ? 0 : remainingDays;
    }

    int calculateTotalDays(final DateTime startDateTime, final DateTime endDateTime) {
        return Days.daysBetween(startDateTime.toLocalDate(), endDateTime.toLocalDate()).getDays() + 1;
    }

    Set<LocalDate> toCollectionOfPerformedActivityDates(Set<Tuple> activitiesPerformed, DateTimeZone dateTimeZone) {
        return activitiesPerformed.stream().map(activityIdAndScore -> new LocalDate(Double.valueOf(activityIdAndScore.getScore()).longValue(), dateTimeZone)).collect(Collectors.toSet());
    }

    Set<Tuple> activitiesPerformedWithinAGoalInterval(final String username, final Long goalId, final Interval goalInterval) {
        return jedisExecution.execute(new JedisOperation<Set<Tuple>>() {
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
