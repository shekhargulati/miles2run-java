package org.miles2run.business.services.utils;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;
import java.util.TimeZone;

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
}
