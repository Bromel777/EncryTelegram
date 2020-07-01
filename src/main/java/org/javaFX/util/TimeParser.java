package org.javaFX.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeParser {

    public static String parseDataString(String timeStamp){
        Long timeMillisecondsValue = Long.parseLong(timeStamp)*1000 ;
        Timestamp ts = new Timestamp( timeMillisecondsValue );
        Date date = new Date(ts.getTime());
        SimpleDateFormat dateFormat;
        Calendar messageTime = new GregorianCalendar();
        messageTime.setTime(date);
        Calendar currentTime =  Calendar.getInstance();
        if( ( currentTime.getTimeInMillis() - messageTime.getTimeInMillis() ) < 1000*60*60*24 ){
            dateFormat = new SimpleDateFormat("HH:mm");
        }
        else if (currentTime.getWeekYear() == messageTime.getWeekYear() ){
            dateFormat = new SimpleDateFormat("E");
        }
        else {
            dateFormat = new SimpleDateFormat("dd:MM:yy");
        }
        return dateFormat.format(date);
    }
}
