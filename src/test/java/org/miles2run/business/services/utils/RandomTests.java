package org.miles2run.business.services.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by shekhargulati on 19/08/14.
 */
public class RandomTests {
    @Test
    public void testLocalDate() throws Exception {
        String[] availableIDs = TimeZone.getAvailableIDs();
        TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"));
        System.out.printf("Timezeone %s", TimeZone.getDefault().getDisplayName());
        LocalDate localDate = new LocalDate(1408453526000L);
        System.out.printf("Date %s ", localDate.toString());
    }

    @Test
    public void testName() throws Exception {
        DateTimeZone dateTimeZone = DateTimeZone.forID("UTC");
        System.out.println("DateTimeZone :" + dateTimeZone);
        DateTime dateTime = new DateTime(dateTimeZone);
        System.out.println(dateTime);
    }

    @Test
    public void testParse() throws Exception {
        String str = "[2014-07-07, 2014-07-18, 2014-08-01, 2014-07-29, 2014-08-12, 2014-07-01, 2014-07-30, 2014-08-13, 2014-07-02, 2014-08-24, 2014-06-30, 2014-07-13, 2014-08-18, 2014-07-24, 2014-08-07, 2014-07-14, 2014-07-25, 2014-08-08, 2014-08-19, 2014-07-19, 2014-08-02, 2014-07-08, 2014-08-20, 2014-07-09, 2014-07-31, 2014-08-14, 2014-07-20, 2014-08-03, 2014-07-03, 2014-08-25, 2014-07-21, 2014-08-04, 2014-08-15, 2014-07-15, 2014-07-04, 2014-07-26, 2014-08-09, 2014-07-05, 2014-07-27, 2014-08-10, 2014-07-16, 2014-08-21, 2014-06-27, 2014-07-10, 2014-08-22, 2014-07-28, 2014-08-11, 2014-06-28, 2014-07-11, 2014-07-22, 2014-08-05, 2014-08-16, 2014-07-23, 2014-08-06, 2014-06-29, 2014-07-12, 2014-08-17, 2014-07-06, 2014-07-17]";
        String[] strs = str.split("[,]");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Date> dates = new ArrayList<>();
        for (String s : strs) {
            s = s.replace("[", "");
            s = s.replace("]", "");

            Date date = dateFormat.parse(s);
            dates.add(date);
        }

        Collections.sort(dates);
        for (Date date : dates) {
            System.out.println(dateFormat.format(date));
        }
    }
}
