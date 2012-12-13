/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * Timestamp.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Imports from the current package.
import transbit.tbits.common.TBitsLogger;

//~--- JDK imports ------------------------------------------------------------

//Java related imports.
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

/**
 * This class is a subclass of <code>java.util.Date</code> with additional
 * methods that are generally used during manipulation of date fields.
 *
 * @author  : Vaibhav
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class Timestamp extends Date implements Comparable<Date>, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// The Logger.
    private static final TBitsLogger LOG         = TBitsLogger.getLogger("transbit.tbits.common");
    private static final Calendar    ourCalendar = Calendar.getInstance();

    /** Number of milliseconds in one second. */
    public static final int ONE_SECOND = 1000;

    /** Number of milliseconds in one minute. */
    public static final int ONE_MINUTE = 60 * ONE_SECOND;

    /** Number of milliseconds in one hour. */
    public static final int ONE_HOUR = 60 * ONE_MINUTE;

    /** Number of milliseconds in one day. */
    public static final int ONE_DAY = 24 * ONE_HOUR;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor sets the Timestamp to the current time.
     */
    public Timestamp() {
        super();
    }

    /**
     * Construct a Timestamp from a Date.
     *
     * @param d   Date object from which the timestamp object should be created.
     */
    private Timestamp(Date d) {
        super(d.getTime());
    }

    /**
     * Forming a Timestamp from SQL Timestamp.
     *
     * @param ts SQL Timestamp.
     */
    private Timestamp(java.sql.Timestamp ts) {
        super(ts.getTime());
    }

    /**
     * Constructor which initializes the Timestamp according to the given
     * millisecond value.
     *
     * @param millis time in milliseconds.
     */
    public Timestamp(long millis) {
        super(millis);
    }

    /**
     * Constructor which sets the Timestamp to the given date String. The date
     * should be specified in yyyyMMddHHmmss format.
     *
     * @param aDateStr the date string
     * @throws ParseException if date string is not in the required format
     */
    public Timestamp(String aDateStr) throws ParseException {
        this(aDateStr, "yyyyMMddHHmmss");
    }

    /**
     * Copy constructor.
     *
     * @param ts Timestamp object from which the current one should be created.
     */
    private Timestamp(Timestamp ts) {
        super(ts.getTime());
    }

    /**
     * Constructor which sets the Timestamp to the given date String in a
     * specified format.
     *
     * @param aDateStr the date string
     * @param aFormat the date string format
     * @throws ParseException if date string is not in the specified format
     */
    public Timestamp(String aDateStr, String aFormat) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(aFormat);
        Date             d   = sdf.parse(aDateStr);

        if (d == null) {
            throw new ParseException(aDateStr + ": bad timestamp or" + " not in specified format " + aFormat, 0);
        }

        setTime(d.getTime());
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Compare to method to compare two Timestamp objects.
     *
     * @param aObj Object to which this should be compared.
     *
     * @return 0 if equal, 1 if this is greater, -1 if this is lesser.
     */
    public int compareTo(Timestamp aObj) {
        Long l1 = new Long(this.getTime());
        Long l2 = new Long(aObj.getTime());

        return l1.compareTo(l2);
    }

    /**
     * Test method.
     *
     * @param args Command-line arguments (none supported).
     */
    public static void main(String[] args) {
        Timestamp ts = new Timestamp();

        LOG.info("toString()          " + ts.toString());
    }

    /*
     *  Returns the TimeStamp in the desired format.
     *
     */
    public String toCustomFormat(String format) {
        DateFormat df = new SimpleDateFormat(format);

        df.setLenient(false);

        return df.format(this);
    }

    /*
     *  Returns the Date in the desired format.
     *
     */
    public static String toCustomFormat(Date aDate, String format) {
        DateFormat df = new SimpleDateFormat(format);

        df.setLenient(false);

        return df.format(aDate);
    }

    public String toDateMin() {
    	TimeZone   zone   = TimeZone.getDefault();
    	 Calendar cal = Calendar.getInstance(zone);

         cal.setTime(new Timestamp(this.getTime()));

         int    year       = cal.get(Calendar.YEAR);
         int    month      = cal.get(Calendar.MONTH) + 1;
         int    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
         int    hourOfDay  = cal.get(Calendar.HOUR_OF_DAY);
         int    minute     = cal.get(Calendar.MINUTE);
         String s          = "";

         // LOG.info(zone.getID());
         if (zone.getID().trim().equals("America/New_York") || (zone.getID().trim().equals("US/Eastern"))) {
             s = "EST";
         } else {
             s = "IST";
         }

         return twoDigitPad(month) + "/" + twoDigitPad(dayOfMonth) + "/" + year + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ' ' + s;
     }
    
    public static String toDateMin(Date date) 
    {
    	 if( null == date )
    		 return null ;
    	 
    	 TimeZone   zone   = TimeZone.getDefault();
    	 Calendar cal = Calendar.getInstance(zone);

         cal.setTime(date);

         int    year       = cal.get(Calendar.YEAR);
         int    month      = cal.get(Calendar.MONTH) + 1;
         int    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
         int    hourOfDay  = cal.get(Calendar.HOUR_OF_DAY);
         int    minute     = cal.get(Calendar.MINUTE);
         String s          = "";

         // LOG.info(zone.getID());
         if (zone.getID().trim().equals("America/New_York") || (zone.getID().trim().equals("US/Eastern"))) {
             s = "EST";
         } else {
             s = "IST";
         }

         return twoDigitPad(month) + "/" + twoDigitPad(dayOfMonth) + "/" + year + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ' ' + s;
     }

    /**
     * Returns the Sql date in (MM/dd/yyyy HH:mm) format
     */
    public String toDateMinold() {
        TimeZone   zone   = TimeZone.getDefault();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        DateFormat formatGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        formatGMT.setTimeZone(TimeZone.getDefault());

        Date   t  = new Date(this.getTime());
        String dt = format.format(new Date(t.getTime()));
        Date   tt = t;

        try {
            Date t2 = formatGMT.parse(dt);

            if (t.getTime() >= t2.getTime()) {
                tt = new Date(t.getTime() + (t.getTime() - t2.getTime()));
            } else {
                tt = new Date(t.getTime() - (t2.getTime() - t.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar cal = (Calendar) ourCalendar.clone();

        cal.setTime(new Timestamp(tt.getTime()));

        int    year       = cal.get(Calendar.YEAR);
        int    month      = cal.get(Calendar.MONTH) + 1;
        int    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int    hourOfDay  = cal.get(Calendar.HOUR_OF_DAY);
        int    minute     = cal.get(Calendar.MINUTE);
        int    second     = cal.get(Calendar.SECOND);
        String s          = "";

        // LOG.info(zone.getID());
        if (zone.getID().trim().equals("America/New_York") || (zone.getID().trim().equals("US/Eastern"))) {
            s = "EST";
        } else {
            s = "IST";
        }

        return twoDigitPad(month) + "/" + twoDigitPad(dayOfMonth) + "/" + year + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ' ' + s;
    }

    /**
     * Returns a GMT equivalent local time.
     * Example : if Timestamp has 03/26/2003 16:30 IST
     *           function returns 03/36/2003 11:00 IST
     *           which is the GMT equivalent in local time.
     */
    public Timestamp toGmtTimestamp() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        String     dt        = format.format(new Date(getTime()));
        Date       t         = (Date) this;
        DateFormat formatGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        formatGMT.setTimeZone(TimeZone.getDefault());

        try {
            t = formatGMT.parse(dt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Timestamp(t.getTime());
    }

    /**
     * This method takes the GMT Timestamp in Site's Zone and then returns the
     * local date in preferred zone.
     *
     * @param aGmtTime  Gmt Time in Site's Zone.
     * @param aTimeZone Preferred Time Zone.
     *
     * @return local Date time inspecified zone.
     */
    public static Date toPreferredZone(Timestamp aGmtTime, String aTimeZone) {
        try {

            // GMT Zone.
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            // User Zone.
            SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            userFormat.setTimeZone(TimeZone.getTimeZone(aTimeZone));

            // Site Zone.
            SimpleDateFormat siteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            siteFormat.setTimeZone(TimeZone.getDefault());

            // GMT Time in Site Zone.
            Date sTime       = new Date(aGmtTime.getTime());
            Date gTime       = siteFormat.parse(gmtFormat.format(sTime));
            Date uTime       = siteFormat.parse(userFormat.format(sTime));
            long gmtUserDiff = gTime.getTime() - uTime.getTime();
            Date tt          = new Date(sTime.getTime() - gmtUserDiff);

            return tt;
        } catch (Exception e) {
            LOG.info("",(e));

            return new Date(aGmtTime.getTime());
        }
    }

    /**
     * This method takes the GMT Timestamp in Site's Zone and then returns the
     * local date in preferred zone.
     *
     * @param aGmtTime  Gmt Time in Site's Zone.
     * @param aTimeZone Preferred Time Zone.
     *
     * @return local Date time inspecified zone.
     */
    public static Date toPreferredZone(Date aGmtTime, String aTimeZone) {
        try {

            // GMT Zone.
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            // User Zone.
            SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            userFormat.setTimeZone(TimeZone.getTimeZone(aTimeZone));

            // Site Zone.
            SimpleDateFormat siteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            siteFormat.setTimeZone(TimeZone.getDefault());

            // GMT Time in Site Zone.
            Date sTime       = new Date(aGmtTime.getTime());
            Date gTime       = siteFormat.parse(gmtFormat.format(sTime));
            Date uTime       = siteFormat.parse(userFormat.format(sTime));
            long gmtUserDiff = gTime.getTime() - uTime.getTime();
            Date tt          = new Date(sTime.getTime() - gmtUserDiff);

            return tt;
        } catch (Exception e) {
            LOG.info("",(e));

            return new Date(aGmtTime.getTime());
        }
    }
    
    /**
     * Returns the date as site Timestamp
     */
    public Timestamp toSiteTimestamp() {
        TimeZone   zone   = TimeZone.getDefault();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        DateFormat formatGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        formatGMT.setTimeZone(TimeZone.getDefault());

        Date   t  = new Date(this.getTime());
        String dt = format.format(new Date(t.getTime()));
        Date   tt = t;

        try {
            Date t2 = formatGMT.parse(dt);

            if (t.getTime() >= t2.getTime()) {
                tt = new Date(t.getTime() + (t.getTime() - t2.getTime()));
            } else {
                tt = new Date(t.getTime() - (t2.getTime() - t.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar cal = (Calendar) ourCalendar.clone();

        cal.setTime(new Timestamp(tt.getTime()));

        int           year        = cal.get(Calendar.YEAR);
        int           month       = cal.get(Calendar.MONTH) + 1;
        int           dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH);
        int           hourOfDay   = cal.get(Calendar.HOUR_OF_DAY);
        int           minute      = cal.get(Calendar.MINUTE);
        int           second      = cal.get(Calendar.SECOND);
        int           milliSecond = cal.get(Calendar.MILLISECOND);
        String        str         = twoDigitPad(month) + "/" + twoDigitPad(dayOfMonth) + "/" + year + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ':' + twoDigitPad(second) + "."
                                    + milliSecond;
        ParsePosition pos         = new ParsePosition(0);
        DateFormat    df          = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        Date          d           = df.parse(str, pos);

        return new Timestamp(d.getTime());
    }
    
    /**
     * Returns the date as site Timestamp
     */
    public static Date toSiteTimestamp( Date date) {
        TimeZone   zone   = TimeZone.getDefault();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        DateFormat formatGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

        formatGMT.setTimeZone(TimeZone.getDefault());

        Date   t  = date;
        String dt = format.format(new Date(t.getTime()));
        Date   tt = t;

        try {
            Date t2 = formatGMT.parse(dt);

            if (t.getTime() >= t2.getTime()) {
                tt = new Date(t.getTime() + (t.getTime() - t2.getTime()));
            } else {
                tt = new Date(t.getTime() - (t2.getTime() - t.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar cal = (Calendar) ourCalendar.clone();

        cal.setTime(new Timestamp(tt.getTime()));

        int           year        = cal.get(Calendar.YEAR);
        int           month       = cal.get(Calendar.MONTH) + 1;
        int           dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH);
        int           hourOfDay   = cal.get(Calendar.HOUR_OF_DAY);
        int           minute      = cal.get(Calendar.MINUTE);
        int           second      = cal.get(Calendar.SECOND);
        int           milliSecond = cal.get(Calendar.MILLISECOND);
        String        str         = twoDigitPad(month) + "/" + twoDigitPad(dayOfMonth) + "/" + year + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ':' + twoDigitPad(second) + "."
                                    + milliSecond;
        ParsePosition pos         = new ParsePosition(0);
        DateFormat    df          = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        Date          d           = df.parse(str, pos);

        return d;
    }

    /**
     * Returns the Sql date in (yyyyMMdd HH:mm:ss) format
     */
    public String toSqlDate() {
        Calendar cal = (Calendar) ourCalendar.clone();

        cal.setTime(this);

        int      year       = cal.get(Calendar.YEAR);
        int      month      = cal.get(Calendar.MONTH) + 1;
        int      dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int      hourOfDay  = cal.get(Calendar.HOUR_OF_DAY);
        int      minute     = cal.get(Calendar.MINUTE);
        int      second     = cal.get(Calendar.SECOND);
        TimeZone zone       = cal.getTimeZone();

        return year + twoDigitPad(month) + twoDigitPad(dayOfMonth) + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ':' + twoDigitPad(second);
    }

    public java.sql.Timestamp toSqlTimestamp() {
        return new java.sql.Timestamp(getTime());
    }

    public static java.sql.Timestamp toSqlTimestamp(Date date)
    {
    	java.sql.Timestamp ts = null ;
    	if( null != date )
    		ts = new java.sql.Timestamp(date.getTime());
    	return ts ;
    }
    /**
     * Return a string representation of the Timestamp.
     */
    public String toString() {
        DateFormat df = getFormat();

        return df.format(this);
    }

    /**
     * Given an integer, return a two-digit String to represent that
     * integer, left-padding with zeroes if necessary.
     *
     * @param num   The number to process.
     * @return      The String which represents num.
     */
    private static String twoDigitPad(int num) {
        if (num < 10) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    /**
     * Returns the date of this Timstamp as an integer, in yyyyMMdd format.
     */
    public int yyyymmdd() {
        Calendar cal = (Calendar) ourCalendar.clone();

        cal.setTime(this);

        int myYear  = cal.get(Calendar.YEAR);
        int myMonth = cal.get(Calendar.MONTH) + 1;
        int myDay   = cal.get(Calendar.DAY_OF_MONTH);

        return (myYear * 10000) + (myMonth * 100) + (myDay);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Create a format object for default formatting.
     */
    private DateFormat getFormat() {
        return new SimpleDateFormat("yyyyMMdd@HHmmssSSS@z");
    }

    /**
     * This method returns the current time in GMT.
     *
     * @return Current Time in GMT.
     */
    public static Timestamp getGMTNow() {
        Timestamp date = new Timestamp();
        return date;//.toGmtTimestamp();
    }

    /**
     * Return the current time as milliseconds since 1/1/1970.
     */
    public static final long getMillisSinceStart(TimeZone zone) {

        // This will set the time in the calendar as current.
        Calendar cal = Calendar.getInstance(zone);

        return cal.getTimeInMillis();
    }

    /**
     * Return the current time as milliseconds since the start of the day.
     */
    public static final int getMillisSinceStartOfDay(TimeZone zone) {

        // This will set the time in the calendar as current.
        Calendar cal = Calendar.getInstance(zone);

        return cal.get(Calendar.HOUR_OF_DAY) * ONE_HOUR + cal.get(Calendar.MINUTE) * ONE_MINUTE + cal.get(Calendar.SECOND) * ONE_SECOND + cal.get(Calendar.MILLISECOND);
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID. Returns null if not
     * found.
     * @param timeZone the ID for a <code>TimeZone</code>, either an
     * abbreviation such as "EST", a full name such as "America/New_York",
     * or a custom ID such as "GMT-5:00".
     * @return the specified <code>TimeZone</code>, or null if the given ID
     * cannot be understood.
     */
    public static final TimeZone getTimeZone(String timeZone) {
        TimeZone zone = TimeZone.getTimeZone(timeZone);

        if (!timeZone.equals("GMT") && zone.getID().equals("GMT")) {
            zone = null;
        }

        return zone;
    }

    /**
     * Factory method to get Timestamp object from a given date object.
     *
     * @param d   Date object from which the timestamp object should be created.
     */
    public static Timestamp getTimestamp(Date d) {
        if (d != null) {
            return new Timestamp(d);
        }

        return null;
    }

    /**
     * Factory method to get Timestamp object from SQL Timestamp.
     *
     * @param ts SQL Timestamp.
     */
    public static Timestamp getTimestamp(java.sql.Timestamp ts) {
        if (ts != null) {
            return new Timestamp(ts);
        }

        return null;
    }

    /**
     * Factory method to get Timestamp object from another one.
     *
     * @param ts Timestamp object from which the current one should be created.
     */
    public static Timestamp getTimestamp(Timestamp ts) {
        if (ts != null) {
            return new Timestamp(ts);
        }

        return null;
    }

    /**
     * Test if it is a "null" Timestamp.
     */
    public boolean isNull() {
        return getTime() == 0L;
    }

    /**
     * Returns true if this timestamp shares the same date as the
     * given timestamp in the default locale.
     */
    public boolean isSameDate(Timestamp ts) {
        return yyyymmdd() == ts.yyyymmdd();
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Set this Timestamp to represent the same time as the given time.
     *
     * @param ts Timestamp.
     */
    public void set(Timestamp ts) {
        if (ts != null) {
            setTime(ts.getTime());
        }
    }
}
