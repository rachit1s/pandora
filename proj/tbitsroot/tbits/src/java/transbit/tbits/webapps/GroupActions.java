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
 * GroupActions.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//Log4Java Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;

import transbit.tbits.Helper.IUCValidator;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to act on a group of requests.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class GroupActions extends HttpServlet {

	// Logger to log Information/Error messages to the Application Log.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	private static final String GROUP_ACTION_FILE = "web/tbits-group-action.htm";

	private static final int GA_UNREAD = 5;

	private static final int GA_READ = 4;

	private static final int GA_PUBLIC = 3;

	private static final int GA_PRIVATE = 2;

	private static final int GA_CLOSE = 1;

	//~--- methods ------------------------------------------------------------

	/**
	 * This method sends a list of requests to database procedure to act on
	 * the givegn list of actions specified.
	 * @param aRequest TODO
	 * @param aSystemId      Id of the Business Area.
	 * @param aRequestList   List of requests.
	 * @param aGroupAction   Action to be taken on the list.
	 * @param aDescription   Description to be appended during the action.
	 * @param aUserId        User who does this group action.
	 *
	 * @return List of rejected Ids if any.
	 */
	private String actOnList(HttpServletRequest aRequest, int aSystemId,
			String aRequestList, int aGroupAction, String aDescription, int aUserId) {

		StringBuilder rejectedList = new StringBuilder();
		Hashtable<Field, String> aIUCMetaData = new Hashtable<Field, String>();
		ArrayList<String> aFailedEUCs = new ArrayList<String>();
		ArrayList<String> aAllEUCs = new ArrayList<String>();
		ArrayList<String> aBadDescriptorEUCs = new ArrayList<String>();
		ArrayList<String> aNotAllowedEUCs = new ArrayList<String>();
		ArrayList<String> aNotAllowedPrivateEUCs = new ArrayList<String>();
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemId(aSystemId);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return e1.getMessage();
		}
		try {
			aDescription = IUCValidator.parseEUCMetaData(aDescription,
					aIUCMetaData, false, false, false, ba, aFailedEUCs,
					aAllEUCs, aBadDescriptorEUCs, aNotAllowedEUCs,
					aNotAllowedPrivateEUCs);

		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}
		for (String requestStr : aRequestList.split(",")) {
			if (requestStr.length() <= 0)
				continue;

			Hashtable<String, String> requestParams = new Hashtable<String, String>();
			requestParams.put(Field.REQUEST, requestStr);

			requestParams.put(Field.BUSINESS_AREA, Integer.toString(aSystemId));

			//TODO: IUC Validations

			//requestParams.put(Field.STATUS, Integer.toString(aGroupAction));
			//System.out.println("Field.STATUS"+Integer.toString(aGroupAction));
			switch (aGroupAction) {
			case GA_CLOSE:
				requestParams.put(Field.STATUS, "close");
				break;
			case GA_PRIVATE:
				requestParams.put(Field.IS_PRIVATE, "1");
				break;

			case GA_PUBLIC:
				requestParams.put(Field.IS_PRIVATE, "0");
				break;

			default:
				break;
			}

			requestParams.put(Field.DESCRIPTION, aDescription);

			requestParams.put(Field.USER, Integer.toString(aUserId));

			for (Field f : aIUCMetaData.keySet()) {

				String name = f.getName();
				System.out.println();
				requestParams.put(name, aIUCMetaData.get(f));
				System.out.println("Inside IUC processing :" + name + ">"
						+ aIUCMetaData.get(f));
			}
			try {
				UpdateRequest updateRequest = new UpdateRequest();
				updateRequest.setSource(TBitsConstants.SOURCE_WEB);
				updateRequest.setContext(aRequest.getContextPath());
				updateRequest.updateRequest(requestParams);

			} catch (APIException e) {
				rejectedList.append(requestStr + "-" + e.toString())
						.append(";");
			} catch (Exception e) {
				rejectedList.append(requestStr + "-" + e.toString())
						.append(";");
			}
		}
		return rejectedList.toString();

	}

	/**
	 * This method sends a list of requests to database procedure to act on
	 * the given list of actions specified.
	 * @param aRequest TODO
	 * @param aSystemId      Id of the Business Area.
	 * @param aRequestList   List of requests.
	 * @param aIsPrivate     Value of private property.
	 * @param aCategory      New category.
	 * @param aStatus        New status.
	 * @param aSeverity      New severity.
	 * @param aRequestType   New request type.
	 * @param aDescription   Description to be appended during the action.
	 * @param aUserId        User who does this group action.
	 *
	 * @return List of rejected Ids if any.
	 */

	private String actOnList(HttpServletRequest aRequest, int aSystemId,
			String aRequestList, String aIsPrivate, String aCategory,
			String aStatus, String aSeverity, String aRequestType,
			String aDescription, int aUserId) {
		StringBuilder rejectedList = new StringBuilder();
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemId(aSystemId);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return e1.getMessage();
		}
		Hashtable<Field, String> aIUCMetaData = new Hashtable<Field, String>();
		ArrayList<String> aFailedEUCs = new ArrayList<String>();
		ArrayList<String> aAllEUCs = new ArrayList<String>();
		ArrayList<String> aBadDescriptorEUCs = new ArrayList<String>();
		ArrayList<String> aNotAllowedEUCs = new ArrayList<String>();
		ArrayList<String> aNotAllowedPrivateEUCs = new ArrayList<String>();

		try {
			aDescription = IUCValidator.parseEUCMetaData(aDescription,
					aIUCMetaData, false, false, false, ba, aFailedEUCs,
					aAllEUCs, aBadDescriptorEUCs, aNotAllowedEUCs,
					aNotAllowedPrivateEUCs);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}

		for (String requestStr : aRequestList.split(",")) {
			if (requestStr.length() <= 0)
				continue;
			System.out.println("Trying '" + requestStr + "'");
			Hashtable<String, String> requestParams = new Hashtable<String, String>();
			requestParams.put(Field.REQUEST, requestStr);
			requestParams.put(Field.BUSINESS_AREA, Integer.toString(aSystemId));
			requestParams.put(Field.IS_PRIVATE, aIsPrivate);
			requestParams.put(Field.CATEGORY, aCategory);
			requestParams.put(Field.STATUS, aStatus);
			requestParams.put(Field.SEVERITY, aSeverity);
			requestParams.put(Field.REQUEST_TYPE, aRequestType);
			//IUC Validations
			requestParams.put(Field.DESCRIPTION, aDescription);
			requestParams.put(Field.USER, Integer.toString(aUserId));
			for (Field f : aIUCMetaData.keySet()) {
				String name = f.getName();

				requestParams.put(name, aIUCMetaData.get(f));

				System.out.println(name);
			}
			try {
				UpdateRequest updateRequest = new UpdateRequest();
				updateRequest.setSource(TBitsConstants.SOURCE_WEB);
				updateRequest.setContext(aRequest.getContextPath());
				
				updateRequest.updateRequest(requestParams);

			} catch (APIException e) {
				rejectedList.append(requestStr + "-" + e.toString())
						.append(";");
			} catch (Exception e) {
				rejectedList.append(requestStr + "-" + e.toString())
						.append(";");
			}
		}

		return rejectedList.toString();
	}

	/**
	 * This method sends a list of requests to database procedure to change
	 * the read status of those requests for the specified user.
	 *
	 * @param aSystemId      Id of the Business Area.
	 * @param aRequestList   List of requests.
	 * @param aGroupAction   Action to be taken on the list.
	 * @param aUserId        User who does this group action.
	 *
	 * @return List of rejected Ids if any.
	 */

	private String changeReadStatus(int aSystemId, String aRequestList,
			int aGroupAction, int aUserId) {
		String rejectedList = "";
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			CallableStatement cs = connection
					.prepareCall("stp_tbits_changeReadStatus ?, ?, ?, ?, ?");

			cs.setInt(1, aSystemId);
			cs.setString(2, aRequestList);
			cs.setInt(3, aGroupAction);
			cs.setInt(4, aUserId);
			cs.registerOutParameter(5, Types.VARCHAR);
			cs.execute();
			rejectedList = cs.getString(5);
			cs.close();
			connection.commit();
		} catch (Exception e) {
			try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			rejectedList = "Exception:" + e.toString();
		} finally {
			if( null != connection )
			{
				try {			
					connection.close();
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}

		return rejectedList;
	}

	/**
	 * This method services the Http-GET requests to this servlet.
	 *
	 * @param  aRequest          the HttpServlet Request Object
	 * @param  aResponse         the HttpServlet Response Object
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		PrintWriter out = aResponse.getWriter();

		Utilities.registerMDCParams(aRequest);

		try {
			handleGetRequest(aRequest, aResponse);
		} catch (Exception e) {
			LOG.info(e.toString());
			out.println("Operation could not be completed." + e.toString());

			return;
		} finally {
			Utilities.clearMDCParams();
		}
	}

	/**
	 * This method services the Http-POST requests to this servlet.
	 *
	 * @param  aRequest          the HttpServlet Request Object
	 * @param  aResponse         the HttpServlet Response Object
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		PrintWriter out = aResponse.getWriter();

		Utilities.registerMDCParams(aRequest);

		try {

			handlePostRequest(aRequest, aResponse);
		} catch (Exception e) {
			LOG.info(e.toString());
			out.println("Operation could not be completed." + e.toString());

			return;
		} finally {
			Utilities.clearMDCParams();
		}
	}

	/**
	 * This method actually services the Http-GET requests to this servlet.
	 *
	 * @param  aRequest          the HttpServlet Request Object
	 * @param  aResponse         the HttpServlet Response Object
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	public void handleGetRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException,
			IOException, DatabaseException, TBitsException {
		PrintWriter out = aResponse.getWriter();

		aResponse.setContentType("text/html");

		// Validate the user.
		User user = WebUtil.validateUser(aRequest);
		int userId = user.getUserId();

		// Get the Business Area ID.
		String sysPrefix = aRequest.getParameter("sysPrefix");

		if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
			out.println("Invalid Business Area.");

			return;
		} else {
			sysPrefix = sysPrefix.trim();
		}

		BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

		if (ba == null) {
			out.println("Invalid Business Area.");

			return;
		}

		int systemId = ba.getSystemId();

		sysPrefix = ba.getSystemPrefix();

		SysConfig sc = ba.getSysConfigObject();

		MDC.put("SYS_PREFIX", sysPrefix);

		String requestList = aRequest.getParameter("requestList");

		if ((requestList == null) || requestList.trim().equals("")) {
			out.println("Empty Requests List.");

			return;
		}

		requestList = requestList.trim();
		LOG.info("Attempt to perform bulk request update: " + "\nUser: "
				+ user.getUserLogin() + "\nBA: " + sysPrefix
				+ "\nRequestList: " + requestList);

		Hashtable<String, Integer> permTable = RolePermission
				.getPermissionsBySystemIdAndUserId(systemId, userId);
		Integer temp = null;
		int permission = 0;
		boolean isPrivate = WebUtil.getIsPrivate(permTable);
		DTagReplacer hp = new DTagReplacer(GROUP_ACTION_FILE);

		hp.replace("sysPrefix", sysPrefix);
		hp.replace("requestList", requestList);
		hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(),
				sysPrefix, false));

		if (WebUtil.canChange(permTable, Field.CATEGORY) == true) {
			hp.replace("category_disabled", "");
		} else {
			hp.replace("category_disabled", " DISABLED ");
		}

		hp.replace("categoryList", getTypeList(systemId, Field.CATEGORY,
				isPrivate));

		if (WebUtil.canChange(permTable, Field.STATUS) == true) {
			hp.replace("status_disabled", "");
		} else {
			hp.replace("status_disabled", " DISABLED ");
		}

		hp
				.replace("statusList", getTypeList(systemId, Field.STATUS,
						isPrivate));

		if (WebUtil.canChange(permTable, Field.SEVERITY) == true) {
			hp.replace("severity_disabled", "");
		} else {
			hp.replace("severity_disabled", " DISABLED ");
		}

		hp.replace("severityList", getTypeList(systemId, Field.SEVERITY,
				isPrivate));

		if (WebUtil.canChange(permTable, Field.REQUEST_TYPE) == true) {
			hp.replace("requestType_disabled", "");
		} else {
			hp.replace("requestType_disabled", " DISABLED ");
		}

		if (WebUtil.canAdd(permTable, Field.DESCRIPTION) == true) {
			hp.replace("description_disabled", "");
		} else {
			hp.replace("description_disabled", " DISABLED ");
		}

		hp.replace("requestTypeList", getTypeList(systemId, Field.REQUEST_TYPE,
				isPrivate));
		out.println(hp.parse(systemId));

		return;
	}

	/**
	 * This method actually services the Http-POST requests to this servlet.
	 *
	 * @param  aRequest          the HttpServlet Request Object
	 * @param  aResponse         the HttpServlet Response Object
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	public void handlePostRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException,
			IOException, DatabaseException, TBitsException {
		PrintWriter out = aResponse.getWriter();

		// Validate the user.
		User user = WebUtil.validateUser(aRequest);
		int userId = user.getUserId();

		// Get the Business Area ID.
		String sysPrefix = aRequest.getParameter("sysPrefix");

		if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
			LOG.info("Invalid Business Area: " + sysPrefix);
			out.println("Invalid Business Area: " + sysPrefix);

			return;
		} else {
			sysPrefix = sysPrefix.trim();
		}

		BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

		if (ba == null) {
			LOG.info("Invalid Business Area: " + sysPrefix);
			out.println("Invalid Business Area: " + sysPrefix);

			return;
		}

		int systemId = ba.getSystemId();

		MDC.put("SYS_PREFIX", sysPrefix);

		// Check the groupActionType
		String grpActionType = aRequest.getParameter("groupActionType");

		if ((grpActionType == null) || (grpActionType.equals("") == true)) {
			LOG.info("Invalid group action: " + grpActionType);
			out.println("Invalid group action.");

			return;
		}

		// Get the list of requests to act on.
		String requestList = aRequest.getParameter("requestList");

		if ((requestList == null) || requestList.trim().equals("")) {
			LOG.info("Empty Requests List.");
			out.println("Empty Requests List.");

			return;
		}

		LOG.info("Bulk Request Update: " + "\nUser: " + user.getUserLogin()
				+ "\nBA: " + sysPrefix + "\nRequestList: " + requestList);

		//INFO: 
		// 1. Update the read/unread status (changeReadStatus())
		// 2. Update each request with the given parameters
		// 		a. Extract IUCs from description
		//		b. Prepare the hashtable containing the new values
		//		c. For each Request, update the request and gather the outcome of each. UpdateRequest.updateRequest(hashTable)
		// 		d. Return the collective outcome to user.

		//
		// Get the table that contains the field name and the permissions
		// the user obtained by virtue of his association with the business
		// area.
		//
		Hashtable<String, Integer> permTable = RolePermission
				.getPermissionsBySystemIdAndUserId(systemId, userId);

		grpActionType = grpActionType.trim().toLowerCase();

		if (grpActionType.equals("simple") == true) {

			// Get what action should be taken on the list.
			String strGroupAction = aRequest.getParameter("groupAction");
			System.out.println("This is strGroupAction " + strGroupAction);
			if ((strGroupAction == null) || strGroupAction.trim().equals("")) {
				LOG.info("Group Action not specified.");
				out.println("Group Action not specified.");

				return;
			}

			int groupAction = 0;

			try {
				groupAction = Integer.parseInt(strGroupAction);
			} catch (Exception e) {
				LOG.info("Invalid Group Action specified.");
				out.println("Invalid Group Action specified.");

				return;
			}

			// Get the Description.
			String description = aRequest.getParameter("description");

			if (description == null) {
				description = "";
			} else {
				description = description.trim();
			}

			switch (groupAction) {
			case GA_CLOSE:

			//
			// Closing a list of requests.
			// Does the user has change permission on Status?
			//
			{
				Integer temp = permTable.get(Field.STATUS);

				if (temp == null) {
					LOG.info("you do not have sufficient permissions.");
					out.println("you do not have sufficient permissions.");

					return;
				}

				int permission = temp.intValue();

				if ((permission & Permission.CHANGE) == 0) {
					LOG.info("you do not have sufficient permissions.");
					out.println("you do not have sufficient permissions.");

					return;
				}

				// We can proceed with changing the state.
			}

				break;

			case GA_PRIVATE:
			case GA_PUBLIC:

			//
			// Changing the confidential property of the tickets.
			// Does the user has change permission on Is Private?
			//
			{
				Integer temp = permTable.get(Field.IS_PRIVATE);

				if (temp == null) {
					LOG.info("Insufficient Permissions.");
					out.println("you do not have sufficient permissions.");

					return;
				}

				int permission = temp.intValue();

				if ((permission & Permission.CHANGE) == 0) {
					LOG.info("Insufficient Permissions.");
					out.println("you do not have sufficient permissions.");

					return;
				}
			}

				break;

			case GA_READ:
			case GA_UNREAD:

				// As of now, no permission checks are required for these.
				break;
			}

			if ((groupAction == GA_CLOSE) || (groupAction == GA_PUBLIC)
					|| (groupAction == GA_PRIVATE)) {

				// Now we can proceed with our action on the list.

				System.out.println("Reached just before actOnList"
						+ groupAction);
				String rejectedList = actOnList(aRequest, systemId,
						requestList, groupAction, description, userId);

				if ((rejectedList != null) && rejectedList.trim().equals("")) {
					out.println(true);
				} else {
					out.println("Rejected:" + rejectedList);
				}
			} else if ((groupAction == GA_READ) || (groupAction == GA_UNREAD)) {
				String rejectedList = changeReadStatus(systemId, requestList,
						groupAction, userId);

				if ((rejectedList != null) && rejectedList.trim().equals("")) {
					out.println(true);
				} else {
					out.println("Rejected:" + rejectedList);
				}
			}
		} else if (grpActionType.equals("advanced") == true) {
			String strPrivate = aRequest.getParameter("isPrivate");

			strPrivate = (strPrivate == null) ? "none" : strPrivate.trim();

			String category = aRequest.getParameter("category");

			category = (category == null) ? "" : category.trim();

			String status = aRequest.getParameter("status");

			status = (status == null) ? "" : status.trim();

			String severity = aRequest.getParameter("severity");

			severity = (severity == null) ? "" : severity.trim();

			String requestType = aRequest.getParameter("requestType");

			requestType = (requestType == null) ? "" : requestType.trim();

			String description = aRequest.getParameter("description");

			description = (description == null) ? "" : description.trim();
			LOG.info("\nPrivate:     " + strPrivate + "\nCategory:    "
					+ category + "\nStatus:      " + status + "\nSeverity:    "
					+ severity + "\nRequestType: " + requestType
					+ "\nDescription: " + description);

			// Now we can proceed with our action on the list.
			String rejectedList = actOnList(aRequest, systemId, requestList,
					strPrivate, category, status, severity, requestType,
					description, userId);

			if ((rejectedList != null) && rejectedList.trim().equals("")) {
				out.println(true);
			} else {
				out.println("Rejected:" + rejectedList);
			}
		}

		return;
	}

	//~--- get methods --------------------------------------------------------

	/**
	 * This method returns the HTML required to render the types for the given
	 * field.
	 *
	 * @param aSystemId  Business Area Id.
	 * @param aFieldName Name of the field.
	 * @param aIsPrivate User's permission on isprivate field.
	 *
	 * @return HTML for rendering the types.
	 */
	private String getTypeList(int aSystemId, String aFieldName,
			boolean aIsPrivate) throws DatabaseException {
		StringBuffer buffer = new StringBuffer();
		ArrayList<Type> typeList = Type.lookupBySystemIdAndFieldName(aSystemId,
				aFieldName);

		if (typeList != null) {

			// Sort the fields by the ordering field.
			Type.setSortParams(Type.ORDERING, 1);
			typeList = Type.sort(typeList);

			for (Type type : typeList) {

				// Skip the nulls.
				if (type == null) {
					continue;
				}

				// Skip the inactive types.
				if (type.getIsActive() == false) {
					continue;
				}

				// Skip the private types if user does not have permission.
				if ((type.getIsPrivate() == true) && (aIsPrivate == false)) {
					continue;
				}

				String displayName = Utilities
						.htmlEncode(type.getDisplayName());

				buffer.append("\n<OPTION value='").append(displayName).append(
						"'>").append(displayName).append("</OPTION>");
			}
		}

		return buffer.toString();
	}
}
