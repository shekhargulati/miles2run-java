package org.miles2run.business.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Created by shekhargulati on 03/07/14.
 */
public abstract class DateUtils {

    public static Interval toDateRangeInterval(int daysBack) {
        DateTime currentDateTime = new DateTime();
        DateTime nDaysBack = currentDateTime.minusDays(daysBack);
        return new Interval(nDaysBack.getMillis(), currentDateTime.getMillis());
    }

    public static Interval toForwardDateRangeInterval(int daysAhead) {
        DateTime currentDateTime = new DateTime();
        DateTime nDaysAhead = currentDateTime.plusDays(daysAhead);
        return new Interval(currentDateTime.getMillis(), nDaysAhead.getMillis());
    }

    public static long timestamp(int daysBack) {
        DateTime currentDateTime = new DateTime();
        return currentDateTime.minusDays(daysBack).getMillis();
    }

    public static double timestampInDouble(int daysBack) {
        DateTime currentDateTime = new DateTime();
        return Double.valueOf(currentDateTime.minusDays(daysBack).getMillis());
    }

    public static Interval toDateRangeIntervalInMonths(int nMonths) {
        DateTime currentDateTime = new DateTime();
        DateTime nDaysBack = currentDateTime.minusMonths(nMonths);
        return new Interval(nDaysBack.getMillis(), currentDateTime.getMillis());
    }

    public static Map<String, Integer> calc(Interval interval, Set<LocalDate> activityDates) {
        int totalDays = Days.daysIn(interval).getDays();
        int performedDays = activityDates.size();
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            dates.add(interval.getStart().toLocalDate().plusDays(i));
        }
        int missedDays = 0;
        for (LocalDate date : dates) {
            if (!activityDates.contains(date)) {
                missedDays += 1;
            }
        }
        Map<String, Integer> data = new HashMap<>();
        data.put("totalDays", totalDays);
        data.put("performedDays", performedDays);
        data.put("missedDays", missedDays);
        return data;
    }

}
