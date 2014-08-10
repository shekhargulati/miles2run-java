package org.miles2run.business.services.redis;

import org.joda.time.Interval;
import org.miles2run.business.domain.jpa.Goal;
import org.miles2run.business.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Created by shekhargulati on 03/07/14.
 */
@ApplicationScoped
public class GoalAggregationService {

    public static final int SECONDS_IN_ONE_MINUTE = 60;

    private Logger logger = LoggerFactory.getLogger(GoalAggregationService.class);

    @Inject
    JedisExecutionService jedisExecutionService;


    public List<Object[]> distanceAndPaceOverNDays(final String username, final Goal goal, final int daysBack) {
        final Interval interval = DateUtils.toDateRangeInterval(daysBack);
        Set<Tuple> activityIdsInNDaysWithScores = findActivitiesWithScore(username, goal, interval);
        Map<Long, List<String>> timestampAndActivities = toMapOfTimestampAndActivities(activityIdsInNDaysWithScores);
        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<Long, List<String>> entry : timestampAndActivities.entrySet()) {
            List<String> activityIds = entry.getValue();
            double totalDistanceRanOnADay = 0;
            double totalPaceOnADay = 0.0;
            for (String activityId : activityIds) {
                List<String> values = getActivityFieldValues(activityId, new String[]{"distanceCovered", "duration"});
                Double distance = Double.valueOf(values.get(0));
                double distanceWithUnitConversion = distance / goal.getGoalUnit().getConversion();
                Long duration = Long.valueOf(values.get(1));
                double durationInMinutes = Double.valueOf(duration) / SECONDS_IN_ONE_MINUTE;
                double pace = durationInMinutes / distanceWithUnitConversion;
                totalDistanceRanOnADay += distanceWithUnitConversion;
                totalPaceOnADay += pace;
            }
            double averagePaceOnADay = totalPaceOnADay / activityIds.size();
            result.add(new Object[]{entry.getKey(), totalDistanceRanOnADay, averagePaceOnADay});
        }
        return result;

    }

    List<String> getActivityFieldValues(final String activityId, final String[] fields) {
        return jedisExecutionService.execute(new JedisOperation<List<String>>() {
            @Override
            public List<String> perform(Jedis jedis) {
                return jedis.hmget(String.format(RedisKeyNames.ACTIVITY_S, activityId), fields);
            }
        });
    }

    Set<Tuple> findActivitiesWithScore(final String username, Goal goal, final Interval interval) {
        final String key = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goal.getId());
        return jedisExecutionService.execute(new JedisOperation<Set<Tuple>>() {
            @Override
            public Set<Tuple> perform(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, interval.getStartMillis(), interval.getEndMillis());
            }
        });
    }


    public Map<String, Double> getActivitiesPerformedInLastNMonthsForGoal(final String username, final Goal goal, final int nMonths) {
        return jedisExecutionService.execute(new JedisOperation<Map<String, Double>>() {
            @Override
            public Map<String, Double> perform(Jedis jedis) {
                Interval interval = DateUtils.toDateRangeIntervalInMonths(nMonths);
                String key = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goal.getId());
                Set<Tuple> activityIdsInNMonthWithScores = jedis.zrangeByScoreWithScores(key, interval.getStartMillis(), interval.getEndMillis());
                Map<Long, List<String>> timestampAndActivities = toMapOfTimestampAndActivities(activityIdsInNMonthWithScores);
                logger.debug("for key {} : timestampAndActivities {}", key, timestampAndActivities);
                Map<String, Double> data = new LinkedHashMap<>();
                for (Map.Entry<Long, List<String>> entry : timestampAndActivities.entrySet()) {
                    List<String> activityIds = entry.getValue();
                    double totalDistanceRanOnADay = 0;
                    for (String activityId : activityIds) {
                        Double distance = Double.valueOf(jedis.hget(String.format("activity:%s", activityId), "distanceCovered"));
                        double distanceWithUnitConversion = distance / goal.getGoalUnit().getConversion();
                        totalDistanceRanOnADay += distanceWithUnitConversion;
                    }
                    long timestamp = entry.getKey() / 1000;
                    data.put(String.valueOf(timestamp), totalDistanceRanOnADay);
                }
                return data;
            }
        });
    }

    Map<Long, List<String>> toMapOfTimestampAndActivities(Set<Tuple> activityIdsInNDaysWithScores) {
        Map<Long, List<String>> timestampAndActivities = new HashMap<>();
        for (Tuple tuple : activityIdsInNDaysWithScores) {
            String activityId = tuple.getElement();
            Long timestamp = Double.valueOf(tuple.getScore()).longValue();
            if (timestampAndActivities.containsKey(timestamp)) {
                List<String> activityIds = timestampAndActivities.get(timestamp);
                activityIds.add(activityId);
            } else {
                List<String> activityIds = new ArrayList<>();
                activityIds.add(activityId);
                timestampAndActivities.put(timestamp, activityIds);
            }
        }
        return timestampAndActivities;
    }


}
