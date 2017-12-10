package nju.edu.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by SuperSY on 2017/12/10.
 */
public class DateFormatter extends ThreadLocal<SimpleDateFormat> {
    public final static String longFormat = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public final static String webFormat = "yyyy-MM-dd";

    //  SimpleDateFormat is not thread safe
    protected SimpleDateFormat initialValue() {
        // Formats into HTTP date format (RFC 822/1123).
        SimpleDateFormat f = new SimpleDateFormat(longFormat, Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        return f;
    }

    private static final DateFormatter FORMATTER = new DateFormatter();

    public static String getDate() {
        return FORMATTER.get().format(new Date());
    }
    public static String format(Date date,String formt){
        if(date==null){
            return null ;
        }
        return new SimpleDateFormat(formt).format(date) ;
    }
}
