package com.buhtum.twr;

import org.joda.time.DateTime;

public class Test {
    public static void main(String[] args) {
        final DateTime start = DateTime.now().withHourOfDay(10).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        System.out.printf(start.toString());
    }
}
