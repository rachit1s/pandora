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
 * Wrapper.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;

//TBits Imports
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

/**
 * This class as a command-line tool to access the TBits Request System in
 * following ways.
 * <ul>
 *    <li> View a request in a given business area.
 *    <li> Add a request to a given business area.
 *    <li> Update a given request in given business area.
 * </ul>
 *
 * @author  : Vinod Gupta.
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class Wrapper implements TBitsConstants, TBitsPropEnum {
    public static final TBitsLogger          LOG     = TBitsLogger.getLogger(PKG_UTIL);
    private static Hashtable<String, String> argInfo = null;
    private static String                    ourDbLogin;
    private static String                    ourDbName;
    private static String                    ourDbPassword;
    private static String                    ourDbServer;
    private static String                    ourDriverName;
    private static String                    ourDriverTag;
    private static boolean					 exitOnFinished = false;
    //~--- static initializers ------------------------------------------------

    /*
     * Static Block to load the properties of the application.
     */
    static {
        try {

            // Read the database related properties.
            ourDbServer   = PropertiesHandler.getProperty(KEY_DB_SERVER);
            ourDbName     = PropertiesHandler.getProperty(KEY_DB_NAME);
            ourDbLogin    = PropertiesHandler.getProperty(KEY_DB_LOGIN);
            ourDbPassword = PropertiesHandler.getProperty(KEY_DB_PASSWORD);
            ourDriverName = PropertiesHandler.getProperty(KEY_DRIVER_NAME);
            ourDriverTag  = PropertiesHandler.getProperty(KEY_DRIVER_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method adds a request to the request-system processing the details
     * passed in the argument table.
     *
     * @param  aArgTable  Table of [command-line argument key, value]
     *
     * @exception Incase of any error.
     */
    public static void addRequest(Hashtable<String, String> aArgTable) throws Exception {
        try {

            // Process this argument table and get the request object.
            Hashtable<String, String> paramTable = getRequestAttributes(aArgTable);

            // Submit this to the AddRequest API.
            AddRequest addRequest = new AddRequest();
            addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
            Request request = addRequest.addRequest(paramTable);
        } catch (APIException de) {

            // Get the List of Exceptions that made the Add-Operation fail.
            LOG.severe("Error While inserting a request from the " + " Command Line Interface:\nDetails of command:\n" + argInfo.toString().replaceAll(", ", "\n") + de.toString()
                       + "",(de));

            return;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * This is the main method - entry point into the program.
     *
     * @param args Command Line Argument List.
     */
    public static void main(String args[]) {
        try {

            // Obtain the command-line options and their values
            // in the form of a table.
            argInfo = getArgumentInfo(args);

            //
            // Check if the table has "-help" command-line option or
            // If it is empty. Print Help in such cases.
            //
            if ((argInfo.get("-help") != null) || (argInfo.size() == 0)) {
                printHelp();

                return;
            }
        } catch (Exception e) {
            printHelp();
            LOG.info("",(e));
            return;
        }
        String exit_app_on_closed = "exit_app_on_closed_xxxxx";
        if(argInfo.get(exit_app_on_closed) != null)
        {
        	//HACK: just to exit on done. 
            //Because add request and update request call the garbage collector every time.
           	exitOnFinished = true;
           	argInfo.remove(exit_app_on_closed);
        }
        
        //
        // If the argInfo contains -add, then this is a request to
        // add request with given details.
        //
        if (argInfo.get("-add") != null) {
            try {
                // Then the argument list should not contain "-update"
                if (argInfo.get("-update") != null) {
                    printHelp();
                    return;
                }

                addRequest(argInfo);
            } catch (Exception e) {
                String msg = e.toString();

                msg = msg.substring(msg.indexOf(':') + 2, msg.length());
                LOG.info(msg);
            } finally {
            	if(exitOnFinished)
            		System.exit(0);
            	else
            		return;
            }
        }

        //
        // If the argInfo contains -update, then this is a request to
        // update a given request with given details.
        //
        else if (argInfo.get("-update") != null) {
            try {

                // Then the argument list should not contain "-add"
                if (argInfo.get("-add") != null) {
                    printHelp();

                    return;
                }

                updateRequest(argInfo);
            } catch (Exception e) {
                String msg = e.toString();

                LOG.info(msg);
                LOG.info("",(e));
            } finally {
            	if(exitOnFinished)
            		System.exit(0);
            	else
            		return;
            }
        }

        //
        // If the argInfo contains -n, then this is a request to
        // view the request details.
        //
        else {
            String sysPrefix    = argInfo.get("-ba");
            String strRequestId = argInfo.get("-n");
            String user = argInfo.get("-u");
            
            if (strRequestId == null) {
                strRequestId = argInfo.get("-request");
            }
            
            if(user == null)
            {
            	user = getCurrentUser();
            }
            try {
                if (sysPrefix == null) {
                    printHelp();

                    return;
                }

                int     requestId = Integer.parseInt(strRequestId);
                boolean order     = false;    // false - asc, true -  desc

                if (argInfo.get("-d") != null) {
                    order = true;
                }

                viewRequest(sysPrefix, requestId, order, user);

                if(exitOnFinished)
            		System.exit(0);
            	else
            		return;
            } catch (NumberFormatException nfe) {
                LOG.info(Messages.getMessage("INVALID_REQUEST_ID", strRequestId));
                LOG.info("",(nfe));

                return;
            } catch (Exception e) {
                String msg = e.toString();

                msg = msg.substring(msg.indexOf(':') + 2, msg.length());
                LOG.info(msg);
                LOG.info("",(e));

                return;
            } finally {
            	if(exitOnFinished)
            		System.exit(0);
            	else
            		return;
            }
        }
        
    }

    /**
     * This method prints the usage information of this class.
     */
    public static void printHelp() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Usage: ").append("").append(" \n\treq\n\t\t").append("-ba <System Prefix>\n\t\t").append("-n  <Request Id>\n\t\t").append(
            "-a  <Ascending Order> [Optional, Default]\n\t\t").append("-d  <Descending Order> [Optional]\n\n\t\t").append("-ba is optional if TBits_BA environment").append(
            " variable is defined.").append(" \n\n\t").append("").append(" req -add\n\t\t").append("-ba BusinessAreaPrefix\n\t\t").append("-category Category\n\t\t").append(
            "-severity Severity\n\t\t").append("-status Status\n\t\t").append("-requesttype RequestType\n\t\t").append("-logger Logger\n\t\t").append("-duedate yyyymmddhhMMss\n\t\t").append(
            "-subject Subject\n\t\t").append("-description Description\n\t\t").append("-duetime HHmm\n\t\t").append("-notify 1/0\n\n\t\t").append(
            " Business Area Prefix and Subject are mandatory\n\t\t").append(" \n\n\t").append("").append(" req -update\n\t\t").append("-request RequestID\n\t\t").append(
            "-ba BusinessAreaPrefix\n\t\t").append("-category Category\n\t\t").append("-severity Severity\n\t\t").append("-status Status\n\t\t").append("-requestType RequestType\n\t\t").append(
            "-logger Logger\n\t\t").append("-duedate yyyymmddhhMMss\n\t\t").append("-duetime HHmm\n\t\t").append("-subject Subject\n\t\t").append("-description Description\n\t\t").append(
            "-notify 1/0\n\n\t\t").append(" Business Area Prefix and RequestID are mandatory.\n\t\t").append("\n");
        LOG.info(buffer.toString());
    }

    /**
     * This method updates a request to the request-system from the details
     * passed in the argument table.
     *
     * @param  aArgTable  Table of [command-line argument key, value]
     *
     * @exception Incase of any error.
     */
    public static void updateRequest(Hashtable<String, String> aArgTable) throws Exception {
        Hashtable<String, String> paramTable = null;
        Request                   request    = null;

        try {

            // Process this argument table and get the request table.
            paramTable = getRequestAttributes(aArgTable);

            // Submit this to the AddRequest API.
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
            request = updateRequest.updateRequest(paramTable);
        } catch (APIException de) {

            // Get the List of Exceptions that made the Update-Operation fail.
            LOG.severe("Error While updating a request from the " + " Command Line Interface:\nDetails of command:\n" + argInfo.toString().replaceAll(", ", "\n") + de.toString()
                       + "",(de));

            return;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * This method prints the request details for the given Business Area
     * and request Id.
     *
     * @param aSysPrefix   Business Area Prefix.
     * @param aRequestId   Request Id whose details should be printed.
     * @param aOrder       Order in which the details should be printed.
     *
     */
    public static void viewRequest(String aSysPrefix, int aRequestId, boolean aOrder, String currentUser) throws Exception {
        User   user        = User.lookupByUserLogin(currentUser);

        if (user == null) {
            throw new Exception(Messages.getMessage("INVALID_USER", currentUser));
        }

        Connection        connection = null;
        
        try
        {
			connection = DataSourcePool.getConnection(ourDbServer, ourDbName, ourDbLogin,
					ourDbPassword, ourDriverName, ourDriverTag);
			CallableStatement cs = connection
					.prepareCall("stp_tbits_getCompleteAction ?, ?, ?, ?");

			cs.setString(1, aSysPrefix);
			cs.setInt(2, aRequestId);
			cs.setString(3, currentUser);
			cs.setBoolean(4, aOrder);

			// First ResultSet will be about the request's current details.
			ResultSet rsRequest = cs.executeQuery();

			if ((rsRequest == null) || (rsRequest.next() == false)) {
				throw new Exception(Messages.getMessage("INVALID_REQUEST_ID",
						Integer.toString(aRequestId)));
			}

			int systemId = rsRequest.getInt("sys_id");
			String prefixedId = rsRequest.getString("sys_prefix") + "#"
					+ rsRequest.getLong("request_id");
			String subject = rsRequest.getString("subject");
			String category = rsRequest.getString("category");
			String severity = rsRequest.getString("severity");
			String status = rsRequest.getString("status");
			String requestType = rsRequest.getString("request_type");
			String loggedDate = getDateInCurrentZoneInFormat(Timestamp
					.getTimestamp(rsRequest.getTimestamp("logged_datetime")),
					"MM/dd/yyyy hh:mm:ss zzz");
			String lastUpdatedDate = getDateInCurrentZoneInFormat(Timestamp
					.getTimestamp(rsRequest
							.getTimestamp("lastupdated_datetime")),
					"MM/dd/yyyy hh:mm:ss zzz");
			String dueDate = getDateInCurrentZoneInFormat(Timestamp
					.getTimestamp(rsRequest.getTimestamp("due_datetime")),
					"MM/dd/yyyy hh:mm:ss zzz");
			int isPrivate = rsRequest.getInt("is_private");
			String loggers = "";
			String assignees = "";
			String subscribers = "";
			String ccs = "";
			String tos = "";

			// The Next ResultSet is about the current request users.
			cs.getMoreResults();

			ResultSet rsRequestUsers = cs.getResultSet();

			if ((rsRequestUsers == null) || (rsRequestUsers.next() == false)) {

				// Error Handling.
			}

			do {
				int userType = rsRequestUsers.getInt("user_type_id");
				String displayName = rsRequestUsers.getString("display_name");
				String userLogin = rsRequestUsers.getString("user_login");
				String userName = ((displayName == null) || displayName
						.equals("")) ? userLogin : (displayName + " ("
						+ userLogin + ")");

				switch (userType) {
				case 2: // Logger
					if (!loggers.equals("")) {
						loggers = loggers + ", ";
					}

					loggers = loggers + userName;

					break;

				case 3: // Assignee
					if (!assignees.equals("")) {
						assignees = assignees + ", ";
					}

					assignees = assignees + userName;

					break;

				case 4: // Subscriber
					if (!subscribers.equals("")) {
						subscribers = subscribers + ", ";
					}

					subscribers = subscribers + userName;

					break;

				case 5: // To
					if (!tos.equals("")) {
						tos = tos + ", ";
					}

					tos = tos + userName;

					break;

				case 6: // Cc
					if (!ccs.equals("")) {
						ccs = ccs + ", ";
					}

					ccs = ccs + userName;

					break;
				}
			} while (rsRequestUsers.next() != false);

			Hashtable<String, Integer> permTable = RolePermission
					.getPermissionsBySystemIdAndRequestIdAndUserId(systemId,
							aRequestId, user.getUserId());
			boolean permission = false;

			cs.getMoreResults();

			ResultSet rsPermissions = cs.getResultSet();

			if ((rsPermissions != null) && (rsPermissions.next() != false)) {
				int perm = rsPermissions.getInt("permission");

				if ((perm & Permission.VIEW) != 0) {
					permission = true;
				}
			}

			StringBuffer actions = new StringBuffer();

			// The Next ResultSet is about the list of actions for this request.
			cs.getMoreResults();

			ResultSet rsActions = cs.getResultSet();

			if ((rsActions == null) || (rsActions.next() == false)) {

				// Error Handling.
			}

			do {
				long actionId = rsActions.getLong("action_id");
				String displayName = rsActions.getString("display_name");
				String userLogin = rsActions.getString("user_login");
				String userName = ((displayName == null) || displayName
						.equals("")) ? userLogin : (displayName + " ("
						+ userLogin + ")");
				String actionDate = getDateInCurrentZoneInFormat(Timestamp
						.getTimestamp(rsActions
								.getTimestamp("lastupdated_datetime")),
						"MM/dd/yyyy hh:mm:ss zzz");
				String description = rsActions.getString("description");
				String headerDescription = rsActions
						.getString("header_description");

				headerDescription = ActionHelper.formatActionLog(systemId,
						headerDescription, permTable, TEXT_FORMAT, false, "",
						"", 0, 0, 0, SOURCE_CMDLINE, NO_TOOLTIP);
				actions.append("\n").append(
						"*********************************************")
						.append("***********************************\n")
						.append(actionId).append(".  ").append(userName)
						.append("  ").append(actionDate).append("\n").append(
								"\n").append(description).append("\n").append(
								"\n").append(headerDescription).append("\n");
			} while (rsActions.next() != false);

			actions.append("*********************************************")
					.append("***********************************\n");

			StringBuffer details = new StringBuffer();

			details.append("Request ID  ").append(prefixedId).append("\n")
					.append("Subject     ").append(subject).append("\n")
					.append("Category    ").append(category).append("\n")
					.append("Logger      ").append(loggers).append("\n")
					.append("Assignee    ").append(assignees).append("\n")
					.append("Logged      ").append(loggedDate).append("\n")
					.append("Updated     ").append(lastUpdatedDate)
					.append("\n").append("Due by      ").append(dueDate)
					.append("\n").append("Type        ").append(requestType)
					.append("\n").append("Severity    ").append(severity)
					.append("\n").append("Status      ").append(status).append(
							"\n").append(actions);

			if ((isPrivate == 0) || ((isPrivate == 1) && (permission == true))) {
				LOG.info(details);
			} else {
				LOG.info(Messages.getMessage("PRIVATE_REQUEST_ERROR",
						"the TBits team"));
			}

		}
        catch(SQLException e)
        {
        	e.printStackTrace();
        	throw e;
        }
        finally
		{
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
       
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method reads the command line arguments list and produces a
     * hashtable of (key, value) pairs. [Imitating GetOpt::Long in Perl]
     *
     * @param args Command Line Argument List.
     *
     * @return Table of (key, value) pairs.
     * @exception Incase invalid argument list format is received.
     */
    public static Hashtable<String, String> getArgumentInfo(String args[]) throws Exception {
        Hashtable<String, String> table = new Hashtable<String, String>();

        // Return the empty table if the argument list is null or empty.
        if ((args == null) || (args.length == 0)) {
            return table;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }

            String arg = args[i].trim();

            //
            // These command line options are singlets and so let the value
            // be true if they are present in the list.
            //
            if (arg.equalsIgnoreCase("-add") || arg.equalsIgnoreCase("-update") || arg.equalsIgnoreCase("-d") 
				|| arg.equalsIgnoreCase("-a") || arg.equalsIgnoreCase("-help")) {
                table.put(arg, "true");
            }
            
            //
            // For Other tags, the argument that follows them in the list
            // is a value for this option. So take that value and move
            // the current pointer to the argument list by 1.
            //
            else {
                i++;

                if (i < args.length) {
                    table.put(arg, args[i]);

                    // We received argument list in invalid format.
                } else {
                    throw new Exception("Invalid format.");
                }
            }
        }

        LOG.info(table.toString());

        return table;
    }

    /**
     * This method returns the login-name of the current user.
     *
     * @return UserLogin of the current user.
     */
    public static String getCurrentUser() {
        return System.getProperty("user.name");
    }

    /**
     * This method returns the given GMTDate's equivalent in current zone
     * in required format.
     *
     * @param aGmtTime Gmt Time.
     * @param aFormat  Request Format.
     *
     * @return GMTTime's current zone equivalent in aFormat.
     */
    public static String getDateInCurrentZoneInFormat(Timestamp aGmtTime, String aFormat) {
        if (aGmtTime == null) {
            return "-";
        }

        try {

            // Site's Zone.
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a");

            format.setTimeZone(TimeZone.getDefault());

            // GMT Zone.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a");

            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            // GMT Time in Site Zone.
            Date   t  = new Date(aGmtTime.getTime());
            String dt = sdf.format(new Date(t.getTime()));
            Date   tt = t;
            Date   t2 = format.parse(dt);

            tt = new Date(t.getTime() + (t.getTime() - t2.getTime()));

            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(tt.getTime());

            SimpleDateFormat finale = new SimpleDateFormat(aFormat);

            return finale.format(cal.getTime());
        } catch (Exception e) {
            LOG.info("",(e));

            return aGmtTime.toDateMin();
        }
    }

    /**
     * A General entry in header-description is as follows
     * [FieldName]##[FieldId]##[Header]. This method removes the fieldName and
     * fieldId from the header entry.
     *
     * @param header uncleaned header in above specified format.
     *
     * @return Cleaned Header.
     */
    private static String getHeaderCleaned(String header) {
        StringBuffer    newHeader = new StringBuffer();
        StringTokenizer st        = new StringTokenizer(header, "\n");

        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            if (line.indexOf("[") >= 0) {
                newHeader.append(line.substring(line.indexOf("["))).append("\n");
            }
        }

        return newHeader.toString();
    }

    /**
     * This method builds the Request object from the arguments passed on the
     * Command-line which are arranged in a table.
     *
     * @param  aArgTable  Table of [command-line argument key, value]
     *
     * @exception Incase of any error.
     */
    public static Hashtable<String, String> getRequestAttributes(Hashtable<String, String> aArgTable) throws Exception {
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        try {
			//why tmpArgTable?
        	//First put every argument into tmpArgTable
        	//Keep preparing the paramTable and remove that element from tmpArgTable.
        	//Put everything from tmpArgTable into paramTable
        	
			Hashtable<String, String> tmpArgTable = new Hashtable<String, String>();
			for(String key:aArgTable.keySet())
			{
				tmpArgTable.put(key, aArgTable.get(key));
			}

			// Get the BA Prefix. This is mandatory to move further.
            String sysPrefix = (String) aArgTable.get("-ba");

            if (sysPrefix == null) {
                throw new IllegalArgumentException("Business Area Prefix is mandatory.");
            }
			else
			{
				tmpArgTable.remove("-ba");				
			}

            BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

            if (ba == null) {
                throw new IllegalArgumentException(Messages.getMessage("INVALID_BUSINESS_AREA", sysPrefix));
            }


            int systemId = ba.getSystemId();

            paramTable.put(Field.BUSINESS_AREA, Integer.toString(systemId));

            // Check if this is a request to update a ticket.
            if (aArgTable.get("-update") != null) {
                String strRequestId = (String) aArgTable.get("-request");

                if ((strRequestId == null) || strRequestId.trim().equals("")) {
                    throw new IllegalArgumentException("Request ID is mandatory while updating a request.");
                }

				tmpArgTable.remove("-update");				
				tmpArgTable.remove("-request");

                int requestId = 0;

                try {
                    requestId = Integer.parseInt(strRequestId);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(Messages.getMessage("INVALID_REQUEST_ID", strRequestId));
                }

                paramTable.put(Field.REQUEST, Integer.toString(requestId));
            }

            // Process if the category is present.
            String category = (String) aArgTable.get("-category");

            if (category != null) {
                paramTable.put(Field.CATEGORY, category);
				tmpArgTable.remove("-category");				
            }

            // Process if the status is present.
            String status = (String) aArgTable.get("-status");

            if (status != null) {
                paramTable.put(Field.STATUS, status);
				tmpArgTable.remove("-status");				
            }

            // Process if the severity is present.
            String severity = (String) aArgTable.get("-severity");

            if (severity != null) {
                paramTable.put(Field.SEVERITY, severity);
				tmpArgTable.remove("-severity");				
            }

            // Process if the requestType is present.
            String requestType = (String) aArgTable.get("-requestType");

            if (requestType != null) {
                paramTable.put(Field.REQUEST_TYPE, requestType);
				tmpArgTable.remove("-requestType");				
            }

            // Process subject.
            String subject = (String) aArgTable.get("-subject");

            if ((subject != null) &&!subject.trim().equals("")) {
                paramTable.put(Field.SUBJECT, subject);
				tmpArgTable.remove("-subject");				
            }

            // Get the Description if any.
            String description = (String) aArgTable.get("-description");

            description = (description == null)
                          ? ""
                          : description.trim();
			if(description != null)
				tmpArgTable.remove("-description");				
            paramTable.put(Field.DESCRIPTION, description);

            // Current User is the appender.
            String currentUser = getCurrentUser();

            paramTable.put(Field.USER, currentUser);

            // Handle the due date if specified.
            if (aArgTable.get("-duedate") != null) {
                String strDueDate = (String) aArgTable.get("-duedate");

                paramTable.put(Field.DUE_DATE, strDueDate);
				tmpArgTable.remove("-duedate");				
            }

            // Check if duetime is specified.
            else if (aArgTable.get("-duetime") != null) {
                try {
                    String   strDueTime = (String) aArgTable.get("-duetime");
                    Calendar today      = Calendar.getInstance();
                    int      year       = today.get(Calendar.YEAR);
                    int      month      = today.get(Calendar.MONTH) + 1;
                    int      day        = today.get(Calendar.DAY_OF_MONTH);
                    String   strDueDate = year + "" + ((month < 10)
                                                       ? ("0" + month)
                                                       : ("" + month)) + "" + ((day < 10)
                            ? ("0" + day)
                            : ("" + day)) + strDueTime + "00";

                    LOG.info("Due Date (Time specified): " + strDueDate);
                    paramTable.put(Field.DUE_DATE, strDueTime);
					tmpArgTable.remove("-duetime");				
                } catch (Exception e) {
                    throw new IllegalArgumentException("Specify due date in yyyymmddhhMMss format.");
                }
            }

            // else
            // {
            // The API will calcuate the default duedatetime for this BA.
            // So leave it null.
            // }
            if (aArgTable.get("-notify") != null) {
                paramTable.put(Field.NOTIFY, aArgTable.get("-notify"));
				tmpArgTable.remove("-notify");				
            }

            // Get the logger information.
            ArrayList loggerList = new ArrayList();
            String    logger     = (String) aArgTable.get("-logger");

            if (logger != null) {
                paramTable.put(Field.LOGGER, logger);
				tmpArgTable.remove("-logger");				
            }
            //comma separated list of attachements
			String attachments = (String) tmpArgTable.get(Field.ATTACHMENTS);
			//TODO: Need to copy the attachments to temp location 
			if (attachments != null){
                //convert a,b,c -> a\tname-of-a\nb\tname-of-b\nc\tname-of-c
				StringBuilder newAttachements = new StringBuilder(); 
				boolean isFirst = true;
                for(String fileName : attachments.split(","))
                {
                	if((fileName == null) ||(fileName.length() == 0))
                	{
                		continue;
                	}
                	File file = new File(fileName);
                	if(!file.exists())
                	{
                		throw new FileNotFoundException("The attachment doesn't exist: " + fileName);
                	}
                	if(isFirst)
                		isFirst = false;
                	else
                		newAttachements.append("\n");
                	newAttachements.append(file.getAbsolutePath()).append("\t").append(file.getName());
                }
                //put it back
                tmpArgTable.put(Field.ATTACHMENTS, newAttachements.toString());
            }
			for(String key:tmpArgTable.keySet())
			{
				paramTable.put(key, tmpArgTable.get(key));
			}
            return paramTable;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
