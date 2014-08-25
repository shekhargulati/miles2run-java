package org.miles2run.business.services.redis;

import com.google.common.collect.Sets;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Tuple;

import java.util.*;

/**
 * Created by shekhargulati on 09/08/14.
 */
public class GoalRedisServiceTest {

    final private GoalRedisService goalRedisService = new GoalRedisService();

    @Test
    public void calculateTotalDays_IntervalBetween1stAugustAnd30thAugust_30Days() throws Exception {
        DateTime start = new DateTime(2014, 8, 1, 0, 0);
        DateTime end = new DateTime(2014, 8, 30, 23, 59);
        int totalDays = goalRedisService.calculateTotalDays(start, end);
        Assert.assertThat(totalDays, CoreMatchers.is(CoreMatchers.equalTo(30)));
    }

    @Test
    public void calculateTotalDays_IntervalBetween1stAugustAnd29thOctober2014_90Days() throws Exception {
        DateTime start = new DateTime(2014, 8, 1, 0, 0);
        DateTime end = new DateTime(2014, 10, 29, 23, 59);
        int totalDays = goalRedisService.calculateTotalDays(start, end);
        Assert.assertThat(totalDays, CoreMatchers.is(CoreMatchers.equalTo(90)));
    }

    @Test
    public void calculateRemainingDays_DaysBetween10thAugustAnd30thAugust_20Days() throws Exception {
        int remainingDays = goalRedisService.calculateRemainingDays(new LocalDate(2014, 8, 10), new LocalDate(2014, 8, 30));
        Assert.assertThat(remainingDays, CoreMatchers.is(CoreMatchers.equalTo(20)));
    }

    @Test
    public void calculateRemainingDays_StartDateGreaterThanEndDate_Return0() throws Exception {
        int remainingDays = goalRedisService.calculateRemainingDays(new LocalDate(2014, 8, 30), new LocalDate(2014, 8, 10));
        Assert.assertThat(remainingDays, CoreMatchers.is(CoreMatchers.equalTo(0)));
    }

    @Test
    public void toCollectionOfPerformedActivityDates_ActivityPerformedOnThreeDifferentDays_SetWithSizeEqualTo3() throws Exception {
        Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime(2014, 8, 10, 15, 30, 30).getMillis()));
        Tuple tuple2 = new Tuple("2", Double.valueOf(new DateTime(2014, 8, 11, 17, 30, 30).getMillis()));
        Tuple tuple3 = new Tuple("3", Double.valueOf(new DateTime(2014, 8, 12, 16, 30, 30).getMillis()));
        Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1, tuple2, tuple3);
        Set<LocalDate> activityPerformedDates = goalRedisService.toCollectionOfPerformedActivityDates(activitiesPerformed, null);
        Assert.assertThat(activityPerformedDates, Matchers.hasSize(3));
    }

    @Test
    public void toCollectionOfPerformedActivityDates_ThreeActivitiesWithTwoPerformedOnSameDay_SetWithSizeEqualTo2() throws Exception {
        Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime(2014, 8, 10, 15, 30, 30).getMillis()));
        Tuple tuple2 = new Tuple("2", Double.valueOf(new DateTime(2014, 8, 10, 17, 30, 30).getMillis()));
        Tuple tuple3 = new Tuple("3", Double.valueOf(new DateTime(2014, 8, 12, 16, 30, 30).getMillis()));
        Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1, tuple2, tuple3);
        Set<LocalDate> activityPerformedDates = goalRedisService.toCollectionOfPerformedActivityDates(activitiesPerformed, null);
        Assert.assertThat(activityPerformedDates, Matchers.hasSize(2));
    }

    @Test
    public void allDatesWithin_DatesBetween10thAugustAnd12thAugust_SetWithSize3() throws Exception {
        DateTime start = new DateTime(2014, 8, 10, 0, 0);
        DateTime end = new DateTime(2014, 8, 12, 23, 59);
        Set<LocalDate> dates = goalRedisService.allDatesWithin(start, end);
        Assert.assertThat(dates, Matchers.hasSize(3));
    }


    @Test
    public void allDatesWithin_StartDateGreaterThanEndDate_ReturnsEmptySet() throws Exception {
        DateTime start = new DateTime(2014, 8, 12, 0, 0);
        DateTime end = new DateTime(2014, 8, 1, 23, 59);
        Set<LocalDate> dates = goalRedisService.allDatesWithin(start, end);
        Assert.assertThat(dates, IsCollectionWithSize.hasSize(0));
    }

    @Test
    public void allDatesWithin_StartDateAndEndDateAreSame_SetWithOneDate() throws Exception {
        DateTime start = new DateTime(2014, 8, 1, 13, 0);
        DateTime end = new DateTime(2014, 8, 1, 15, 59);
        Set<LocalDate> dates = goalRedisService.allDatesWithin(start, end);
        Assert.assertThat(dates, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void calculateMissedDays_ActivityPerformed3DaysOf5DaysInTotal_2Days() throws Exception {
        Set<LocalDate> activityPerformedDates = Sets.newHashSet(new LocalDate(2014, 8, 10), new LocalDate(2014, 8, 11), new LocalDate(2014, 8, 12));
        Set<LocalDate> allDates = Sets.newLinkedHashSet(Arrays.asList(new LocalDate(2014, 8, 9), new LocalDate(2014, 8, 10), new LocalDate(2014, 8, 11), new LocalDate(2014, 8, 12), new LocalDate(2014, 8, 13)));
        int missedDays = goalRedisService.calculateMissedDays(activityPerformedDates, allDates);
        Assert.assertThat(missedDays, Is.is(2));
    }

    @Test
    public void getDurationGoalProgress_WithValidData_MapWithProgressData() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime(2014, 8, 10, 15, 30, 30).getMillis()));
                Tuple tuple2 = new Tuple("2", Double.valueOf(new DateTime(2014, 8, 11, 17, 30, 30).getMillis()));
                Tuple tuple3 = new Tuple("3", Double.valueOf(new DateTime(2014, 8, 12, 16, 30, 30).getMillis()));
                Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1, tuple2, tuple3);
                return activitiesPerformed;
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 15, 15, 30, 30, dateTimeZone);
            }

        };
        DateTime start = new DateTime(2014, 8, 1, 0, 0);
        DateTime end = new DateTime(2014, 8, 30, 23, 59);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 3)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 12)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 15)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("percentage"), Is.is((Object) 10.0d)));
    }

    @Test
    public void getDurationGoalProgress_ActivityPerformedToday_MapWithProgressData() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime(2014, 8, 10, 15, 30, 30).getMillis()));
                Tuple tuple2 = new Tuple("2", Double.valueOf(new DateTime(2014, 8, 11, 17, 30, 30).getMillis()));
                Tuple tuple3 = new Tuple("3", Double.valueOf(new DateTime(2014, 8, 12, 16, 30, 30).getMillis()));
                Tuple tuple4 = new Tuple("4", Double.valueOf(new DateTime(2014, 8, 15, 15, 30, 30).getMillis()));
                Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1, tuple2, tuple3, tuple4);
                return activitiesPerformed;
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 15, 15, 30, 30, dateTimeZone);
            }
        };
        DateTime start = new DateTime(2014, 8, 1, 0, 0);
        DateTime end = new DateTime(2014, 8, 30, 23, 59);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 4)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 11)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 15)));
    }

    @Test
    public void getDurationGoalProgress_IntervalStartDateGreaterThanTodayDate_PartialCalculatedProgressDataRestEmpty() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                return Collections.emptySet();
            }

            @Override
            DateTime today(boolean activityPerformedTodayExists, DateTimeZone dateTimeZone) {
                return new DateTime(2014, 8, 15, 15, 30, 30, dateTimeZone);
            }
        };
        DateTime start = new DateTime(2014, 8, 16, 0, 0);
        DateTime end = new DateTime(2014, 8, 30, 23, 59);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 15)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 15)));
    }

    @Test
    public void getDurationGoalProgress_CurrentDateLessThanGoalStartDate_RemainingDaysShouldBe30() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
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
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 30)));
    }


    @Test
    public void getDurationGoalProgress_StartDate9AugEndDate7SeptActivityPerformedOneDay_MissedDays1AndRemainingDays28() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime(2014, 8, 9, 15, 30, 30).getMillis()));
                Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1);
                return activitiesPerformed;
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
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 1)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 1)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 28)));
    }

    @Test
    public void allDatesWithin_TwoConsecutiveDates_ShouldBeAColletionOfSize2() throws Exception {
        DateTime start = new DateTime(1407604407000L);
        DateTime end = new DateTime(1407660461744L);
        Set<LocalDate> dates = goalRedisService.allDatesWithin(start, end);
        Assert.assertThat(dates, IsCollectionWithSize.hasSize(2));
    }

    @Test
    public void getDurationGoalProgress_StartDateTodayAnyEndDateNoActivityPerformed_TodayDateShouldNotBeShownInMissedDays() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                return Collections.emptySet();
            }

        };
        DateTime start = new DateTime();
        DateTime end = start.plusDays(29);
        System.out.printf("Start %s End %s", start, end);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 30)));
    }

    @Test
    public void getDurationGoalProgress_StartDateTodayAnyEndDateActivityPerformed_Missed0Completed1() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime().getMillis()));
                Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1);
                return activitiesPerformed;
            }

        };
        DateTime start = new DateTime();
        DateTime end = start.plusDays(29);
        System.out.printf("Start %s End %s", start, end);
        Interval goalInterval = new Interval(start, end);

        Map<String, Object> progress = goalRedisService.getDurationGoalProgress("test_user", 1L, goalInterval, 0);
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 1)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 0)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 29)));
    }


    @Test
    public void getDurationGoalProgress_StartDate9AugEndDate7SeptActivityPerformedOneDayTimezoneOffset420_MissedDays1AndRemainingDays28() throws Exception {
        GoalRedisService goalRedisService = new GoalRedisService() {
            @Override
            Set<Tuple> activitiesPerformedWithinAGoalInterval(String username, Long goalId, Interval goalInterval) {
                Tuple tuple1 = new Tuple("1", Double.valueOf(new DateTime(2014, 8, 9, 15, 30, 30).getMillis()));
                Set<Tuple> activitiesPerformed = Sets.newHashSet(tuple1);
                return activitiesPerformed;
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
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("totalDays"), Is.is((Object) 30)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("performedDays"), Is.is((Object) 1)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("missedDays"), Is.is((Object) 1)));
        Assert.assertThat(progress, Matchers.hasEntry(Is.is("remainingDays"), Is.is((Object) 28)));
    }

}
