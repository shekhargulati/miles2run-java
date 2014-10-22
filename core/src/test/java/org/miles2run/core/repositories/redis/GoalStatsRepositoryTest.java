package org.miles2run.core.repositories.redis;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Tuple;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.hasEntry;

public class GoalStatsRepositoryTest {

    final private GoalStatsRepository goalStatsRepository = new GoalStatsRepository();

    @Test
    public void calculateTotalDays_IntervalBetween1stAugustAnd30thAugust_30Days() throws Exception {
        DateTime start = new DateTime(2014, 8, 1, 0, 0);
        DateTime end = new DateTime(2014, 8, 30, 23, 59);
        int totalDays = goalStatsRepository.calculateTotalDays(start, end);
        Assert.assertThat(totalDays, CoreMatchers.is(CoreMatchers.equalTo(30)));
    }

    @Test
    public void calculateTotalDays_IntervalBetween1stAugustAnd29thOctober2014_90Days() throws Exception {
        DateTime start = new DateTime(2014, 8, 1, 0, 0);
        DateTime end = new DateTime(2014, 10, 29, 23, 59);
        int totalDays = goalStatsRepository.calculateTotalDays(start, end);
        Assert.assertThat(totalDays, CoreMatchers.is(CoreMatchers.equalTo(90)));
    }

    @Test
    public void calculateRemainingDays_DaysBetween10thAugustAnd30thAugust_20Days() throws Exception {
        int remainingDays = goalStatsRepository.calculateRemainingDays(new LocalDate(2014, 8, 10), new LocalDate(2014, 8, 30));
        Assert.assertThat(remainingDays, CoreMatchers.is(CoreMatchers.equalTo(20)));
    }

    @Test
    public void calculateRemainingDays_StartDateGreaterThanEndDate_Return0() throws Exception {
        int remainingDays = goalStatsRepository.calculateRemainingDays(new LocalDate(2014, 8, 30), new LocalDate(2014, 8, 10));
        Assert.assertThat(remainingDays, CoreMatchers.is(CoreMatchers.equalTo(0)));
    }

    @Test
    public void toCollectionOfPerformedActivityDates_ActivityPerformedOnThreeDifferentDays_SetWithSizeEqualTo3() throws Exception {
        Tuple tuple1 = new Tuple("1", (double) new DateTime(2014, 8, 10, 15, 30, 30).getMillis());
        Tuple tuple2 = new Tuple("2", (double) new DateTime(2014, 8, 11, 17, 30, 30).getMillis());
        Tuple tuple3 = new Tuple("3", (double) new DateTime(2014, 8, 12, 16, 30, 30).getMillis());
        Set<Tuple> activitiesPerformed = newHashSet(tuple1, tuple2, tuple3);
        Set<LocalDate> activityPerformedDates = goalStatsRepository.toCollectionOfPerformedActivityDates(activitiesPerformed, null);
        Assert.assertThat(activityPerformedDates, Matchers.hasSize(3));
    }

    @Test
    public void toCollectionOfPerformedActivityDates_ThreeActivitiesWithTwoPerformedOnSameDay_SetWithSizeEqualTo2() throws Exception {
        Tuple tuple1 = new Tuple("1", (double) new DateTime(2014, 8, 10, 15, 30, 30).getMillis());
        Tuple tuple2 = new Tuple("2", (double) new DateTime(2014, 8, 10, 17, 30, 30).getMillis());
        Tuple tuple3 = new Tuple("3", (double) new DateTime(2014, 8, 12, 16, 30, 30).getMillis());
        Set<Tuple> activitiesPerformed = newHashSet(tuple1, tuple2, tuple3);
        Set<LocalDate> activityPerformedDates = goalStatsRepository.toCollectionOfPerformedActivityDates(activitiesPerformed, null);
        Assert.assertThat(activityPerformedDates, Matchers.hasSize(2));
    }

    @Test
    public void getDurationGoalProgress_WithValidData_MapWithProgressData() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", (double) new DateTime(2014, 8, 10, 15, 30, 30).getMillis());
                Tuple tuple2 = new Tuple("2", (double) new DateTime(2014, 8, 11, 17, 30, 30).getMillis());
                Tuple tuple3 = new Tuple("3", (double) new DateTime(2014, 8, 12, 16, 30, 30).getMillis());
                return newHashSet(tuple1, tuple2, tuple3);
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 15, 15, 30, 30, dateTimeZone);
            }

        };
        DateTimeZone utc = DateTimeZone.forID("UTC");
        DateTime start = new DateTime(2014, 8, 1, 0, 0, utc);
        DateTime end = new DateTime(2014, 8, 30, 23, 59, utc);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 3));
        Assert.assertThat(progress, hasEntry("missedDays", 12));
        Assert.assertThat(progress, hasEntry("remainingDays", 15));
        Assert.assertThat(progress, hasEntry("percentage", 10.0d));
    }

    @Test
    public void getDurationGoalProgress_ActivityPerformedToday_MapWithProgressData() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", (double) new DateTime(2014, 8, 10, 15, 30, 30).getMillis());
                Tuple tuple2 = new Tuple("2", (double) new DateTime(2014, 8, 11, 17, 30, 30).getMillis());
                Tuple tuple3 = new Tuple("3", (double) new DateTime(2014, 8, 12, 16, 30, 30).getMillis());
                Tuple tuple4 = new Tuple("4", (double) new DateTime(2014, 8, 15, 15, 30, 30).getMillis());
                return newHashSet(tuple1, tuple2, tuple3, tuple4);
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 15, 15, 30, 30, dateTimeZone);
            }
        };
        DateTimeZone utc = DateTimeZone.forID("UTC");
        DateTime start = new DateTime(2014, 8, 1, 0, 0, utc);
        DateTime end = new DateTime(2014, 8, 30, 23, 59, utc);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 4));
        Assert.assertThat(progress, hasEntry("missedDays", 11));
        Assert.assertThat(progress, hasEntry("remainingDays", 15));
    }

    @Test
    public void getDurationGoalProgress_IntervalStartDateGreaterThanTodayDate_PartialCalculatedProgressDataRestEmpty() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                return Collections.emptySet();
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 15, 15, 30, 30, dateTimeZone);
            }
        };
        DateTimeZone utc = DateTimeZone.forID("UTC");
        DateTime start = new DateTime(2014, 8, 16, 0, 0, utc);
        DateTime end = new DateTime(2014, 8, 30, 23, 59, utc);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 15));
        Assert.assertThat(progress, hasEntry("performedDays", 0));
        Assert.assertThat(progress, hasEntry("missedDays", 0));
        Assert.assertThat(progress, hasEntry("remainingDays", 15));
    }

    @Test
    public void getDurationGoalProgress_CurrentDateLessThanGoalStartDate_RemainingDaysShouldBe30() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                return Collections.emptySet();
            }

            @Override
            DateTime today(DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 9, 19, 10, 30, dateTimeZone);
            }

        };
        DateTime start = new DateTime(2014, 8, 16, 18, 4);
        DateTime end = new DateTime(2014, 9, 14, 18, 4);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 0));
        Assert.assertThat(progress, hasEntry("missedDays", 0));
        Assert.assertThat(progress, hasEntry("remainingDays", 30));
    }

    @Test
    public void getDurationGoalProgress_StartDate9AugEndDate7SeptActivityPerformedOneDay_MissedDays1AndRemainingDays28() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", (double) new DateTime(2014, 8, 9, 15, 30, 30).getMillis());
                return newHashSet(tuple1);
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 10, 14, 10, 30, dateTimeZone);
            }

        };
        DateTime start = new DateTime(1407604407000L);
        DateTime end = new DateTime(2014, 9, 7, 23, 59);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 1));
        Assert.assertThat(progress, hasEntry("missedDays", 1));
        Assert.assertThat(progress, hasEntry("remainingDays", 28));
    }


    @Test
    public void getDurationGoalProgress_StartDateTodayAnyEndDateNoActivityPerformed_TodayDateShouldNotBeShownInMissedDays() throws Exception {
        GoalStatsRepository statsRepository = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                return Collections.emptySet();
            }

        };
        DateTime start = new DateTime();
        DateTime end = start.plusDays(29);
        System.out.printf("Start %s End %s", start, end);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = statsRepository.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 0));
        Assert.assertThat(progress, hasEntry("missedDays", 0));
        Assert.assertThat(progress, hasEntry("remainingDays", 30));
    }

    @Test
    public void getDurationGoalProgress_StartDateTodayAnyEndDateActivityPerformed_Missed0Completed1() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", (double) new DateTime().getMillis());
                return newHashSet(tuple1);
            }

        };
        DateTime start = new DateTime();
        DateTime end = start.plusDays(29);
        System.out.printf("Start %s End %s", start, end);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 1));
        Assert.assertThat(progress, hasEntry("missedDays", 0));
        Assert.assertThat(progress, hasEntry("remainingDays", 29));
    }

    @Test
    public void getDurationGoalProgress_StartDate9AugEndDate7SeptActivityPerformedOneDayTimezoneOffset420_MissedDays1AndRemainingDays28() throws Exception {
        GoalStatsRepository goalRedisService = new GoalStatsRepository() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", (double) new DateTime(2014, 8, 9, 15, 30, 30).getMillis());
                return newHashSet(tuple1);
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 10, 14, 10, 30, dateTimeZone);
            }

        };
        DateTime start = new DateTime(1407604407000L);
        DateTime end = new DateTime(2014, 9, 7, 23, 59);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 420);
        Assert.assertThat(progress, hasEntry("totalDays", 30));
        Assert.assertThat(progress, hasEntry("performedDays", 1));
        Assert.assertThat(progress, hasEntry("missedDays", 1));
        Assert.assertThat(progress, hasEntry("remainingDays", 28));
    }


}