package org.miles2run.core.repositories.redis;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.miles2run.core.utils.DateUtils;
import org.miles2run.domain.entities.Goal;
import org.miles2run.domain.entities.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@ApplicationScoped
public class GoalAggregationRepository {

    private static final int SECONDS_IN_ONE_MINUTE = 60;
    
    private final Logger logger = LoggerFactory.getLogger(GoalAggregationRepository.class);

    @Inject
    JedisExecution jedisExecution;

    public List<Object[]> distanceAndPaceOverNDays(final String username, final Goal goal, final int daysBack, int timezoneOffsetInMinutes) {
        int timezoneOffsetInMillis = (-1) * timezoneOffsetInMinutes * 60 * 1000;
        DateTimeZone dateTimeZone = timezoneOffsetInMillis == 0 ? DateTimeZone.forID("UTC") : DateTimeZone.forOffsetMillis(timezoneOffsetInMillis);

        final Interval interval = DateUtils.toDateRangeInterval(daysBack, dateTimeZone);
        Set<Tuple> activityIdsInNDaysWithScores = findActivitiesWithScore(username, goal, interval);
        Map<LocalDate, List<String>> localdateAndTimestamp = toMapOfLocalDateAndActivities(activityIdsInNDaysWithScores, dateTimeZone);
        logger.info("localdateAndTimestamp : {}", localdateAndTimestamp);
        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<String>> entry : localdateAndTimestamp.entrySet()) {
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
            result.add(new Object[]{entry.getKey().toString(), totalDistanceRanOnADay, averagePaceOnADay});
        }
        return result;

    }

    List<String> getActivityFieldValues(final String activityId, final String[] fields) {
        return jedisExecution.execute(new JedisOperation<List<String>>() {
            @Override
            public List<String> perform(Jedis jedis) {
                return jedis.hmget(String.format(RedisKeyNames.ACTIVITY_S, activityId), fields);
            }
        });
    }

    Set<Tuple> findActivitiesWithScore(final String username, Goal goal, final Interval interval) {
        final String key = String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, username, goal.getId());
        return jedisExecution.execute(new JedisOperation<Set<Tuple>>() {
            @Override
            public Set<Tuple> perform(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, interval.getStartMillis(), interval.getEndMillis());
            }
        });
    }

    Map<LocalDate, List<String>> toMapOfLocalDateAndActivities(Set<Tuple> activityIdsInNDaysWithScores, DateTimeZone dateTimeZone) {
        Map<LocalDate, List<String>> timestampAndActivities = new HashMap<>();
        for (Tuple tuple : activityIdsInNDaysWithScores) {
            String activityId = tuple.getElement();
            LocalDate localDate = new LocalDate(Double.valueOf(tuple.getScore()).longValue(), dateTimeZone);
            if (timestampAndActivities.containsKey(localDate)) {
                List<String> activityIds = timestampAndActivities.get(localDate);
                activityIds.add(activityId);
            } else {
                List<String> activityIds = new ArrayList<>();
                activityIds.add(activityId);
                timestampAndActivities.put(localDate, activityIds);
            }
        }
        return timestampAndActivities;
    }

    public Map<String, Double> getActivitiesPerformedInLastNMonthsForGoal(final String username, final Goal goal, final int nMonths) {
        return jedisExecution.execute(new JedisOperation<Map<String, Double>>() {
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

    public List<Object[]> distanceAndPaceOverNMonths(final Profile profile, final Goal goal, final String interval, final int nMonths) {
        return jedisExecution.execute(new JedisOperation<List<Object[]>>() {
            @Override
            public List<Object[]> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -nMonths);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, profile.getUsername(), goal.getId()), nMonthsBack.getTime(), today.getTime());
                Map<String, Double> monthDistanceHash = new HashMap<>();
                Map<String, Double> monthPaceHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    logger.info(String.format("Activity Date : %s", activityDate));
                    String key = formatDateToYearAndMonth(activityDate);
                    logger.info(String.format("DateToYearAndMonth : %s", key));
                    double distance = Double.valueOf(values.get(0)) / goal.getGoalUnit().getConversion();
                    if (monthDistanceHash.containsKey(key)) {
                        Double value = monthDistanceHash.get(key);
                        monthDistanceHash.put(key, value + distance);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        double pace = durationInMinutes / distance;
                        Double currentAvgPace = monthPaceHash.get(key);
                        monthPaceHash.put(key, (currentAvgPace + pace) / 2);
                    } else {
                        monthDistanceHash.put(key, distance);
                        Double durationInSeconds = Double.valueOf(Long.valueOf(values.get(1)));
                        double durationInMinutes = durationInSeconds / 60;
                        double pace = durationInMinutes / distance;
                        monthPaceHash.put(key, pace);
                    }
                }
                List<Object[]> chartData = new ArrayList<>();
                Set<Map.Entry<String, Double>> entries = monthDistanceHash.entrySet();
                for (Map.Entry<String, Double> entry : entries) {
                    chartData.add(new Object[]{entry.getKey(), entry.getValue(), monthPaceHash.get(entry.getKey())});
                }
                Collections.reverse(chartData);
                return chartData;
            }
        });
    }

    public List<Object[]> distanceAndActivityCountOverNMonths(final Profile profile, final Goal goal, final int months) {
        return jedisExecution.execute(new JedisOperation<List<Object[]>>() {
            @Override
            public List<Object[]> perform(Jedis jedis) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -months);
                Date nMonthsBack = calendar.getTime();
                Set<Tuple> activityIdsInNDaysWithScores = jedis.zrangeByScoreWithScores(String.format(RedisKeyNames.PROFILE_S_GOAL_S_TIMELINE, profile.getUsername(), goal.getId()), nMonthsBack.getTime(), today.getTime());
                Map<String, Double> monthDistanceHash = new HashMap<>();
                Map<String, Long> monthActivityCountHash = new HashMap<>();
                for (Tuple activityIdTuple : activityIdsInNDaysWithScores) {
                    String activityId = activityIdTuple.getElement();
                    double activityTimestamp = activityIdTuple.getScore();
                    List<String> values = jedis.hmget(String.format("activity:%s", activityId), "distanceCovered", "duration");
                    Date activityDate = new Date(Double.valueOf(activityTimestamp).longValue());
                    logger.info(String.format("Activity Date : %s", activityDate));
                    String key = formatDateToYearAndMonth(activityDate);
                    logger.info(String.format("DateToYearAndMonth : %s", key));
                    double distance = Double.valueOf(values.get(0)) / goal.getGoalUnit().getConversion();
                    if (monthDistanceHash.containsKey(key)) {
                        Double distanceTillNow = monthDistanceHash.get(key);
                        monthDistanceHash.put(key, distanceTillNow + distance);
                        Long activityCountTillNow = monthActivityCountHash.get(key);
                        monthActivityCountHash.put(key, activityCountTillNow + 1L);
                    } else {
                        monthDistanceHash.put(key, distance);
                        monthActivityCountHash.put(key, 1L);
                    }
                }
                List<Object[]> chartData = new ArrayList<>();
                Set<Map.Entry<String, Double>> entries = monthDistanceHash.entrySet();
                for (Map.Entry<String, Double> entry : entries) {
                    chartData.add(new Object[]{entry.getKey(), entry.getValue(), monthActivityCountHash.get(entry.getKey())});
                }
                Collections.sort(chartData, (m1, m2) -> {
                    String month1 = (String) m1[0];
                    String month2 = (String) m2[0];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM");
                    try {
                        Date date1 = dateFormat.parse(month1);
                        Date date2 = dateFormat.parse(month2);
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                return chartData;
            }
        });
    }


    private String formatDateToYearAndMonth(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM");
        return dateFormat.format(date);
    }
}
