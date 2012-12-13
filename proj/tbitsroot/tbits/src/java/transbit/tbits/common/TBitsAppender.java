/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * TBitsLogger.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Log4j imports
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

//Current Package imports
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;

import static transbit.tbits.common.TBitsLogger.SOURCE_CMDLINE;
import static transbit.tbits.common.TBitsLogger.SOURCE_EMAIL;
import static transbit.tbits.common.TBitsLogger.SOURCE_WEB;

//Static imports.
import static transbit.tbits.common.TBitsLogger.ourSource;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.PrintWriter;
import java.io.StringWriter;

//~--- classes ----------------------------------------------------------------

/**
 *
 * <p>
 * This class extends the <a href=http://logging.apache.org/log4j/"
 * target="_blank">ConsoleAppender</a> and overrides the append method
 * to send out emails depending on the level of the log message and the
 * level configured for sending mails.
 * </p>
 *
 *
 * @author : Vaibhav
 * @version : $Id: $
 */
public class TBitsAppender extends ConsoleAppender implements PropertiesEnum {

    // Property Key whose value is the level from which mails should be send.
    private static final String MAIL_LEVEL = "log4j.maillevel";

    // Mail Related Properties.
    private static String ourApplicationName      = "";
    private static String ourApplicationVersion   = "";
    private static String ourFailureFromAddress   = "";
    private static String ourFailureNotifyAddress = "";
    private static Level  ourMailingLevel;
    private static String ourSourceStr;

    //~--- static initializers ------------------------------------------------

    static {
        try {

            // Get the Application Name.
            ourApplicationName = PropertiesHandler.getProperty(KEY_APP_NAME);

            // Get the Application Version.
            ourApplicationVersion = PropertiesHandler.getProperty(KEY_APP_VERSION);

            // Get the From address on the Envelope of Log mails.
            ourFailureFromAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);

            // Get the Recipients address of log mails.
            ourFailureNotifyAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_TO);

            // Get the Mail Logging Level.
            String aLevel = PropertiesHandler.getLoggingProperty(MAIL_LEVEL);

            ourMailingLevel = Level.toLevel(aLevel);
        } catch (Exception e) {}

        switch (ourSource) {
        case SOURCE_WEB :
            ourSourceStr = "Web";

            break;

        case SOURCE_EMAIL :
            ourSourceStr = "Email";

            break;

        case SOURCE_CMDLINE :
            ourSourceStr = "Command Line";

            break;

        default :
            ourSourceStr = "Web";

            break;
        }
    }

    //~--- constructors -------------------------------------------------------

    /**
     * Default constructor.
     */
    public TBitsAppender() {
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
            sendMail(aEvent);
        }
    }

    /**
     * This method formats the Logging Event object and prepares the mail
     * message to be sent out.
     *
     * @param  aEvent Logging Event object.
     */
    public static void sendMail(LoggingEvent aEvent) {

        // Format of the subject is
        // [ServerName]:[ApplicationName]:[LogLevel]: Message.
        String hostName  = (String) MDC.get("HOST_NAME");
        String hostAddr  = (String) MDC.get("HOST_ADDR");
        String userName  = (String) MDC.get("USER_NAME");
        String sysPrefix = (String) MDC.get("SYS_PREFIX");

        sysPrefix = (sysPrefix == null)
                    ? "N/A"
                    : sysPrefix.trim();

        String requestId = (String) MDC.get("REQUEST_ID");

        requestId = (requestId == null)
                    ? "N/A"
                    : requestId.trim();

        String serverName = (String) MDC.get("SERVER_NAME");

        if (serverName != null) {
            serverName = serverName.replaceAll(".transbittech.com", "");
        } else {
            serverName = "N/A";
        }

        String subject = (new StringBuilder().append("[").append(serverName).append("] [").append(ourApplicationName).append(" ").append(ourApplicationVersion).append("] [").append(
                             aEvent.getLevel().toString()).append("]: Log Message").toString());
        String content = (new StringBuilder().append("Host Name: ").append(hostName).append("\nHost Addr: ").append(hostAddr).append("\nUser: ").append(userName).append("\nBusinessArea: ").append(
                             sysPrefix).append("\nRequestId: ").append(requestId).append("\n\n").toString());
        String               notUsed = ourSourceStr;
        ThrowableInformation ti      = aEvent.getThrowableInformation();

        if (ti != null) {
            StringWriter sw         = new StringWriter();
            PrintWriter  pw         = new PrintWriter(sw);
            Throwable    aThrowable = ti.getThrowable();

            aThrowable.printStackTrace(pw);
            content = content + sw.toString();
        } else {
            content = content + (String) aEvent.getMessage();
        }

        Mail.send(ourFailureNotifyAddress, ourFailureFromAddress, subject, content);
    }
}
