package org.javaFX.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Clock {
    public static void printTime(){
        Calendar calendar = new GregorianCalendar();
        System.out.println(calendar.get(Calendar.HOUR_OF_DAY)+":"+ calendar.get(Calendar.MINUTE)+ ":"+calendar.get(Calendar.SECOND));
    }
}
