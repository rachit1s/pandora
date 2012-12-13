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
 * TBitsFileAppender.java
 *
 * $Header: $
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Log4j imports
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

//Static imports.
//Current Package imports
import transbit.tbits.common.PropertiesHandler;

//~--- classes ----------------------------------------------------------------

/**
 * This class extends the DailyRollingFileAppender of
 * <a href="http://logging.apache.org/log4j" target="_blank">Log4java</a>
 * and overrides the append method to send out emails.
 *
 *
 * @author : Vinod Gupta
 * @version : $Id: $
 */
public class TBitsFileAppender extends DailyRollingFileAppender {

    // Property Key whose value is the level from which mails should be send.
    private static final String MAIL_LEVEL = "log4j.maillevel";
    private static Level        ourMailingLevel;

    //~--- static initializers ------------------------------------------------

    static {
        try {

            // Get the Mail Logging Level.
            String aLevel = PropertiesHandler.getLoggingProperty(MAIL_LEVEL);

            ourMailingLevel = Level.toLevel(aLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //~--- constructors -------------------------------------------------------

    /**
     * Default constructor.
     */
    public TBitsFileAppender() {
        super();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Overridden to send mails if the level is greater than or equals to the
     * specified level.
     *
     * @param  aEvent Logging Event object.
     */
    public void append(LoggingEvent aEvent) {
        super.append(aEvent);

        if (aEvent.getLevel().isGreaterOrEqual(ourMailingLevel)) {
            TBitsAppender.sendMail(aEvent);
        }
    }
}
