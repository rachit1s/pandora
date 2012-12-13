/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



package transbit.tbits.common;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//~--- classes ----------------------------------------------------------------

/**
 * This class is used to gobble up (read) the error stream of a process
 * in a separate thread.
 *
 * <pre>
 * <code>
 * Usage:
 *      ...
 *      ...
 *      ...
 *      String cmd = "my OS command";
 *      Process process = Runtime.getRuntime().exec(cmd);
 *      StreamGobbler sg = new StreamGobbler(process.getErrorStream());
 *      sg.start();
 *      ...
 *      ... // Handle the input and output streams.
 *      ...
 *      String errorMessage = sg.getMessage();
 *      ...
 *      ...
 * </code>
 * </pre>
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class StreamGobbler extends Thread {
    String      message = new String();
    InputStream is;

    //~--- constructors -------------------------------------------------------

    /**
     * Constructor that takes the Inputstream to be read.
     * @param is Inputstream to be read.
     */
    public StreamGobbler(InputStream is) {
        this.is = is;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Thie method reads the input stream and is called by the system.
     */
    public void run() {
        try {
            InputStreamReader isr  = new InputStreamReader(is);
            BufferedReader    br   = new BufferedReader(isr);
            String            line = null;
            StringBuffer      sb   = new StringBuffer();

            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            message = sb.toString();
        } catch (IOException ioe) {}
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the message read from the input stream.
     *
     * @return Text read from the input stream.
     */
    public String getMessage() {
        return message;
    }
}
