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
 * DateTimeParser.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//Java Imports
import transbit.tbits.common.TBitsLogger;

//Static Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class parses the text to form a valid Timestamp object.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class DateTimeParser {
    public static final TBitsLogger            LOG                  = TBitsLogger.getLogger(PKG_UTIL);
    private static String[]                    DATE_FORMATS         = {
        "MM dd yy", "yyyy MM dd", "yyyyMMdd", "MMM d yy", "d MMM yy", "yy MMM d", "EEE MMM d yy"
    };
    private static String[]                    SHORT_DATE_FORMATS   = { "MM dd" };
    private static String[]                    TIME_FORMATS_LENIENT = {
        "h:mm a z", "h a z", "h:mm z", "h z", "H:mm a z", "H a z", "H:mm z", "H z", "k:mm a z", "k a z", "k:mm z", "k z"
    };
    private static String[]                    TIME_FORMATS         = {
        "h:mm a", "h a", "h:mm", "H:mm a", "H a", "H:mm", "k:mm a", "k a", "k:mm"
    };
    private static ArrayList<SimpleDateFormat> myDateFormats;
    private static ArrayList<SimpleDateFormat> myDateTimeFormats;
    private static ArrayList<SimpleDateFormat> myShortDateFormats;
    private static ArrayList<SimpleDateFormat> myShortDateTimeFormats;
    private static ArrayList<SimpleDateFormat> myTimeFormats;

    //~--- static initializers ------------------------------------------------

    static {
        myDateFormats          = new ArrayList<SimpleDateFormat>();
        myShortDateFormats     = new ArrayList<SimpleDateFormat>();
        myTimeFormats          = new ArrayList<SimpleDateFormat>();
        myDateTimeFormats      = new ArrayList<SimpleDateFormat>();
        myShortDateTimeFormats = new ArrayList<SimpleDateFormat>();

        SimpleDateFormat sdf = null;

        for (int i = 0; i < DATE_FORMATS.length; i++) {
            sdf = new SimpleDateFormat(DATE_FORMATS[i]);
            sdf.setLenient(false);
            myDateFormats.add(sdf);
        }

        for (int i = 0; i < SHORT_DATE_FORMATS.length; i++) {
            sdf = new SimpleDateFormat(SHORT_DATE_FORMATS[i]);
            sdf.setLenient(false);
            myShortDateFormats.add(sdf);
        }

        for (int i = 0; i < TIME_FORMATS_LENIENT.length; i++) {
            sdf = new SimpleDateFormat(TIME_FORMATS_LENIENT[i]);
            sdf.setLenient(true);
            myTimeFormats.add(sdf);
        }

        for (int i = 0; i < TIME_FORMATS.length; i++) {
            sdf = new SimpleDateFormat(TIME_FORMATS[i]);
            sdf.setLenient(false);
            myTimeFormats.add(sdf);
        }

        for (int i = 0; i < DATE_FORMATS.length; i++) {
            for (int j = 0; j < TIME_FORMATS_LENIENT.length; j++) {
                sdf = new SimpleDateFormat(DATE_FORMATS[i] + " " + TIME_FORMATS_LENIENT[j]);
                sdf.setLenient(true);
                myDateTimeFormats.add(sdf);
            }

            for (int j = 0; j < TIME_FORMATS.length; j++) {
                sdf = new SimpleDateFormat(DATE_FORMATS[i] + " " + TIME_FORMATS[j]);
                sdf.setLenient(false);
                myDateTimeFormats.add(sdf);
            }
        }

        for (int i = 0; i < SHORT_DATE_FORMATS.length; i++) {
            for (int j = 0; j < TIME_FORMATS_LENIENT.length; j++) {
                sdf = new SimpleDateFormat(SHORT_DATE_FORMATS[i] + " " + TIME_FORMATS_LENIENT[j]);
                sdf.setLenient(true);
                myShortDateTimeFormats.add(sdf);
            }

            for (int j = 0; j < TIME_FORMATS.length; j++) {
                sdf = new SimpleDateFormat(SHORT_DATE_FORMATS[i] + " " + TIME_FORMATS[j]);
                sdf.setLenient(false);
                myShortDateTimeFormats.add(sdf);
            }
        }
    }

    //~--- methods ------------------------------------------------------------

    public static void main(String[] args) {
        LOG.info(DateTimeParser.parse(args[0]));
    }

    public static Hashtable<String, Date> parse(String aStr) {

        //
        // Convert string to uppercase
        //
        aStr = aStr.toUpperCase();

        //
        // Do proper padding for year in dates like 5/5/5
        //
        aStr = aStr.replaceAll("([/|.|-])([0-9])([^0-9/.-]+|$)", "$10$2$3");

        //
        // replace all chars other than [alphanumeric,:,/, ]  by space
        //
        aStr = aStr.replaceAll("[^A-Z0-9: ]", " ");

        //
        // replace all multiple spaces by single space.
        //
        aStr = aStr.replaceAll(" ( +)", " ");

        //
        // add space between numeric and chars.
        //
        aStr = aStr.replaceAll("([0-9])([A-Z])", "$1 $2");
        aStr = aStr.replaceAll("([A-Z])([0-9])", "$1 $2");

        //
        // add space between am/pm and zone strings.
        //
        aStr = aStr.replaceAll("(AM|PM)([A-Z])", "$1 $2");

        //
        // replace "sept" by "sep", if specified.
        //
        aStr = aStr.replace("SEPT", "SEP");

        Hashtable<String, Date> dateWithFormat = parseDateTime(aStr);

        if (dateWithFormat == null) {
            dateWithFormat = parseDate(aStr);
        }

        if (dateWithFormat == null) {
            dateWithFormat = parseTime(aStr);
        }

        return dateWithFormat;
    }

    public static Hashtable<String, Date> parseDate(String aStr) {
        Hashtable<String, Date> dateWithFormat = null;
        Date                    date           = null;

        for (SimpleDateFormat sdf : myDateFormats) {
            try {
                date = sdf.parse(aStr);

                if (date != null) {

                    //
                    // "MM dd" format is getting matched to "yyyyMMdd"
                    // putting this check to avoid that match.
                    //
                    if ((aStr.indexOf(" ") != -1) && (sdf.toPattern().indexOf(" ") == -1)) {
                        date = null;

                        throw new Exception();
                    }

                    dateWithFormat = new Hashtable<String, Date>();
                    dateWithFormat.put("DATE", date);

                    return dateWithFormat;
                }
            } catch (Exception e) {}
        }

        if (date == null) {
            for (SimpleDateFormat sdf : myShortDateFormats) {
                try {
                    date = sdf.parse(aStr);

                    if (date != null) {
                        Calendar cal  = Calendar.getInstance();
                        int      year = cal.get(Calendar.YEAR);

                        cal.setTime(date);
                        cal.set(Calendar.YEAR, year);
                        dateWithFormat = new Hashtable<String, Date>();
                        dateWithFormat.put("SHORT_DATE", cal.getTime());

                        return dateWithFormat;
                    }
                } catch (Exception e) {}
            }
        }

        return dateWithFormat;
    }

    public static Hashtable<String, Date> parseDateTime(String aStr) {
        Hashtable<String, Date> dateWithFormat = null;
        Date                    date           = null;

        for (SimpleDateFormat sdf : myDateTimeFormats) {
            try {
                date = sdf.parse(aStr);

                if (date != null) {
                    dateWithFormat = new Hashtable<String, Date>();
                    dateWithFormat.put("DATE_TIME", date);

                    return dateWithFormat;
                }
            } catch (Exception e) {}
        }

        for (SimpleDateFormat sdf : myShortDateTimeFormats) {
            try {
                date = sdf.parse(aStr);

                if (date != null) {
                    Calendar cal  = Calendar.getInstance();
                    int      year = cal.get(Calendar.YEAR);

                    cal.setTime(date);
                    cal.set(Calendar.YEAR, year);
                    dateWithFormat = new Hashtable<String, Date>();
                    dateWithFormat.put("SHORT_DATE_TIME", cal.getTime());

                    return dateWithFormat;
                }
            } catch (Exception e) {}
        }

        return dateWithFormat;
    }

    public static Hashtable<String, Date> parseTime(String aStr) {
        Hashtable<String, Date> dateWithFormat = null;
        Date                    date           = null;

        for (SimpleDateFormat sdf : myTimeFormats) {
            try {
                date = sdf.parse(aStr);

                if (date != null) {
                    Calendar cal   = Calendar.getInstance();
                    int      year  = cal.get(Calendar.YEAR);
                    int      month = cal.get(Calendar.MONTH);
                    int      day   = cal.get(Calendar.DAY_OF_MONTH);

                    cal.setTime(date);
                    LOG.info(cal.getTime());

                    int year1  = cal.get(Calendar.YEAR);
                    int month1 = cal.get(Calendar.MONTH);
                    int day1   = cal.get(Calendar.DAY_OF_MONTH);

                    cal.set(Calendar.YEAR, year1 + (year - 1970));
                    cal.set(Calendar.MONTH, month1 + (month - 0));
                    cal.set(Calendar.DAY_OF_MONTH, day1 + (day - 1));
                    dateWithFormat = new Hashtable<String, Date>();
                    dateWithFormat.put("TIME", cal.getTime());

                    return dateWithFormat;
                }
            } catch (Exception e) {}
        }

        return dateWithFormat;
    }
}
