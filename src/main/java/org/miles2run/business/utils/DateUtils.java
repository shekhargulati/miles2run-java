package org.miles2run.business.utils;

import org.joda.time.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by shekhargulati on 03/07/14.
 */
public abstract class DateUtils {

    public static Interval toDateRangeInterval(int daysBack, DateTimeZone dateTimeZone) {
        DateTime currentDateTime = new DateTime(dateTimeZone);
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

    public static Date toDate(String text) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(text);
        } catch (ParseException e) {
            return null;

        }
    }

    public static Date toUTCDate(Date date) {
        try {
            DateFormat df = DateFormat.getInstance();
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date utcDate = df.parse(df.format(date));
            return utcDate;
        } catch (ParseException e) {
            return date;
        }
    }

}
