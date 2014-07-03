package org.miles2run.business.utils;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Date;

/**
 * Created by shekhargulati on 03/07/14.
 */
public abstract class DateUtils {

    public static Interval toDateRangeInterval(int daysBack) {
        DateTime currentDateTime = new DateTime();
        DateTime nDaysBack = currentDateTime.minusDays(daysBack);
        return new Interval(nDaysBack.getMillis(), currentDateTime.getMillis());
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
}
