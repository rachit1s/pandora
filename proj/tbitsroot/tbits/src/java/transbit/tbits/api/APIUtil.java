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
 * APIUtil.java
 *
 * $Header:
 */
package transbit.tbits.api;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.UserType.INTERNAL_MAILINGLIST;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.TVN.FileAction;
import transbit.tbits.TVN.WebdavConstants;
import transbit.tbits.TVN.WebdavUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.config.BusinessRule.Operator;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BAMailAccount;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.ExclusionList;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FileRepoIndexObject;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.mail.TBitsMailer;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class contains the static methods which are basically utilities used
 * during request processing in the API.
 * 
 * @author : Vaibhav.
 * @version : $Id: $
 */
public class APIUtil implements TBitsConstants {

	// Application Logger.
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

	// Insert Related Request Header.
	public static final String INSERT_RELATED_HEADER = "X-TBits-Insert-RELATED";

	// Location of attachments.
	// public static String ourAttachmentLocation;

	// Location of attachments prior to processing.
	// public static String ourTmpLocation;

	// ~--- static initializers ------------------------------------------------

	public static String getAttachmentLocation() {
		String attDirProp = PropertiesHandler
				.getProperty(TBitsPropEnum.KEY_ATTACHMENTDIR);
		if (null == attDirProp)
			return "";

		return Configuration.findAbsolutePath(attDirProp);
	}

	public static String getTMPDir() {
		String tmpDir = PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR);
		if (null == tmpDir)
			return null;

		return Configuration.findAbsolutePath(tmpDir);
	}

	/**
	 * This creates a the temp directory if not already present and returns that
	 * File object.
	 * 
	 * @return
	 */
	public static File getTBitsTMPDir() {
		String tmpDir = PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR);
		if (null == tmpDir)
			return null;
		File file = new File(Configuration.findAbsolutePath(tmpDir));
		if (!file.exists())
			file.mkdirs();
		return file;
	}

	// static {
	// try {
	// ourTmpLocation =
	// } catch (IllegalArgumentException e) {
	// LOG.severe(e.toString(), e);
	// }

	// try {
	// ourAttachmentLocation =
	// } catch (IllegalArgumentException e) {
	// LOG.severe(e.toString(), e);
	// }
	// catch(Exception e)
	// {
	// LOG.severe("The attachment directory not found.");
	// }
	// }

	// ~--- methods ------------------------------------------------------------

	/**
	 * This method checks if there is a directory existing under the attachment
	 * location with the given sys prefix. If the directory is not present, it
	 * is created.
	 * 
	 * @param aSysPrefix
	 *            Prefix of the BA.
	 * 
	 */
	private static void checkDir(String aSysPrefix) {
		File file = new File(getAttachmentLocation() + "/" + aSysPrefix);

		if (file.exists() == false) {
			file.mkdirs();
		}
	}

	/*
	 * A value may be another variable. If it is a variable it would be starting
	 * with $. Replace it with its value.
	 */
	public static String getVarValue(Request aRequest, Field aField,
			String aValue) {
		int systemId = aRequest.getSystemId();

		/*
		 * Check if the value starts with $ in which case, the user wants us to
		 * get the value of that field and user it here.
		 */
		if (aValue.startsWith("$") == true) {
			String sourceFieldName = aValue.substring(1);
			Field sourceField = null;

			try {
				sourceField = Field.lookupBySystemIdAndFieldName(systemId,
						sourceFieldName);
			} catch (Exception e) {

				// Log this as info message and return.
				LOG.info("",(e));
				return aValue;
			}

			if (sourceField == null) {
				LOG.info("Invalid field: " + sourceFieldName);

				return aValue;
			}

			/*
			 * Check if both the fields are of same data type.
			 */
			if (aField.getDataTypeId() != sourceField.getDataTypeId()) {
				return aValue;
			}

			aValue = aRequest.get(sourceField.getName());
		}

		return aValue;
	}

	/**
	 * This method compares two boolean values presented as strings depending on
	 * the operator.
	 * 
	 * @param optr
	 *            Operator.
	 * @param opnd1
	 *            Operand 1.
	 * @param opnd2
	 *            Operand 2.
	 * 
	 * @return Truth value of comparison.
	 */
	public static boolean compareBoolean(Operator optr, String opnd1,
			String opnd2) {
		boolean arg1 = parseBoolean(opnd1);
		boolean arg2 = parseBoolean(opnd2);
		boolean flag = false;

		switch (optr) {
		case EQ:
			flag = (arg1 == arg2);

			break;

		case NE:
			flag = (arg1 != arg2);

			break;

		/*
		 * All the other operators does not make sense for a boolean field
		 * return false;
		 */
		case LT:
		case LE:
		case GT:
		case GE:
		case IN:
		default:
			flag = false;

			break;
		}

		return flag;
	}

	/**
	 * This method compares two date values presented as strings depending on
	 * the operator.
	 * 
	 * @param optr
	 *            Operator.
	 * @param opnd1
	 *            Operand 1.
	 * @param opnd2
	 *            Operand 2.
	 * 
	 * @return Truth value of comparison.
	 */
	public static boolean compareDate(Operator optr, String opnd1, String opnd2) {
		Timestamp arg1 = null;
		Timestamp arg2 = null;

		try {
			arg1 = parseDateTime(opnd1);
		} catch (Exception e) {
			LOG.info("",(e));

			return false;
		}

		try {
			arg2 = parseDateTime(opnd2);
		} catch (Exception e) {
			LOG.info("",(e));

			return false;
		}

		boolean flag = false;

		switch (optr) {
		case EQ: {
			if (arg1.equals(arg2)) {
				flag = true;
			} else {
				flag = false;
			}
		}

			break;

		case NE: {
			if (arg1.equals(arg2)) {
				flag = false;
			} else {
				flag = true;
			}
		}

			break;

		case LT: {
			if (arg1.compareTo(arg2) < 0) {
				flag = true;
			} else {
				flag = false;
			}
		}

			break;

		case LE: {
			if (arg1.compareTo(arg2) <= 0) {
				flag = true;
			} else {
				flag = false;
			}
		}

			break;

		case GT: {
			if (arg1.compareTo(arg2) > 0) {
				flag = true;
			} else {
				flag = false;
			}
		}

			break;

		case GE: {
			if (arg1.compareTo(arg2) >= 0) {
				flag = true;
			} else {
				flag = false;
			}
		}

			break;

		case LIKE:
			if ((opnd1 != null) && (opnd2 != null))
				flag = opnd1.matches(opnd2);
			break;

		// All the other operators does not make sense for a date field
		// return false;
		case IN:
		default:
			flag = false;

			break;
		}

		return flag;
	}

	/**
	 * This method compares two integer values presented as strings depending on
	 * the operator.
	 * 
	 * @param optr
	 *            Operator.
	 * @param opnd1
	 *            Operand 1.
	 * @param opnd2
	 *            Operand 2.
	 * 
	 * @return Truth value of comparison.
	 */
	public static boolean compareInteger(Operator optr, String opnd1,
			String opnd2) {
		int arg1 = 0;
		int arg2 = 0;

		try {
			arg1 = Integer.parseInt(opnd1.trim());
		} catch (Exception nfe) {
			LOG.info("",(nfe));

			return false;
		}

		try {
			arg2 = Integer.parseInt(opnd2.trim());
		} catch (Exception nfe) {
			LOG.info("",(nfe));

			return false;
		}

		boolean flag = false;

		switch (optr) {
		case EQ:
			flag = (arg1 == arg2);

			break;

		case NE:
			flag = (arg1 != arg2);

			break;

		case LT:
			flag = (arg1 < arg2);

			break;

		case LE:
			flag = (arg1 <= arg2);

			break;

		case GT:
			flag = (arg1 > arg2);

			break;

		case GE:
			flag = (arg1 >= arg2);

			break;

		case LIKE:
			if ((opnd1 != null) && (opnd2 != null))
				flag = opnd1.matches(opnd2);
			break;
		// All the other operators does not make sense for an integer field
		// return false;
		case IN:
		default:
			flag = false;

			break;
		}

		return flag;
	}

	/**
	 * This method compares two UserList values presented as strings depending
	 * on the operator.
	 * 
	 * @param optr
	 *            Operator.
	 * @param opnd1
	 *            Operand 1.
	 * @param opnd2
	 *            Operand 2.
	 * 
	 * @return Truth value of comparison.
	 */
	public static boolean compareMultiValue(Operator optr, String opnd1,
			String opnd2) {
		boolean flag = false;
		ArrayList<User> arg1 = toUserList(opnd1, true);
		ArrayList<User> arg2 = toUserList(opnd2, true);

		switch (optr) {
		case EQ:
			flag = equals(arg1, arg2);

			break;

		case NE:
			flag = !(equals(arg1, arg2));

			break;

		case IN:
			flag = in(arg1, arg2);

			break;
		case LIKE:
			if ((opnd1 != null) && (opnd2 != null))
				flag = opnd1.matches(opnd2);
			break;
		// These operators does not make sense for a multivalue field.
		case LT:
		case LE:
		case GT:
		case GE:
		default:
			flag = false;
		}

		return flag;
	}

	/**
	 * This method compares two real values presented as strings depending on
	 * the operator.
	 * 
	 * @param optr
	 *            Operator.
	 * @param opnd1
	 *            Operand 1.
	 * @param opnd2
	 *            Operand 2.
	 * 
	 * @return Truth value of comparison.
	 */
	public static boolean compareReal(Operator optr, String opnd1, String opnd2) {
		double arg1 = 0;
		double arg2 = 0;

		try {
			arg1 = Double.parseDouble(opnd1.trim());
		} catch (Exception nfe) {
			LOG.info("",(nfe));

			return false;
		}

		try {
			arg2 = Double.parseDouble(opnd2.trim());
		} catch (Exception nfe) {
			LOG.info("",(nfe));

			return false;
		}

		boolean flag = false;

		switch (optr) {
		case EQ:
			flag = (arg1 == arg2);

			break;

		case NE:
			flag = (arg1 != arg2);

			break;

		case LT:
			flag = (arg1 < arg2);

			break;

		case LE:
			flag = (arg1 <= arg2);

			break;

		case GT:
			flag = (arg1 > arg2);

			break;

		case GE:
			flag = (arg1 >= arg2);

			break;

		case LIKE:
			if ((opnd1 != null) && (opnd2 != null))
				flag = opnd1.matches(opnd2);
			break;

		// All the other operators does not make sense for an integer field
		// return false;
		case IN:
		default:
			flag = false;

			break;
		}

		return flag;
	}

	/**
	 * This method compares two strings depending on the operator.
	 * 
	 * @param optr
	 *            Operator.
	 * @param opnd1
	 *            Operand 1.
	 * @param opnd2
	 *            Operand 2.
	 * 
	 * @return Truth value of comparison.
	 */
	public static boolean compareString(Operator optr, String opnd1,
			String opnd2) {
		String arg1 = (opnd1 == null) ? "" : opnd1.trim();
		String arg2 = (opnd2 == null) ? "" : opnd2.trim();
		boolean flag = false;

		switch (optr) {
		case EQ:
			flag = (arg1.equalsIgnoreCase(arg2));

			break;

		case NE:
			flag = (!(arg1.equalsIgnoreCase(arg2)));

			break;

		case IN: {
			if (arg1.toLowerCase().indexOf(arg2.toLowerCase()) >= 0) {
				flag = true;
			} else {
				flag = false;
			}
		}

			break;
		case LIKE:
			if ((arg1 != null) && (arg1 != null))
				flag = arg1.matches(arg2);
			break;
		// All the other operators does not make sense for a String field
		// return false;
		case LT:
		case LE:
		case GT:
		case GE:
		default:
			flag = false;

			break;
		}

		return flag;
	}

	/**
	 * This method checks if two users lists are equal.
	 * 
	 * @param list1
	 *            Operand 1.
	 * @param list2
	 *            Operand 2.
	 * 
	 * @return True if both are equal.
	 */
	public static boolean equals(ArrayList<User> list1, ArrayList<User> list2) {

		// Return true if both are null.
		if ((list1 == null) && (list2 == null)) {
			return true;
		}

		// Return false if one of them is not null.
		if ((list1 == null) || (list2 == null)) {
			return false;
		}

		// Return true if both are empty
		if ((list1.size() == 0) && (list2.size() == 0)) {
			return true;
		}

		// Return false if the sizes are different.
		if (list1.size() != list2.size()) {
			return false;
		}

		int size = list1.size();

		for (int i = 0; i < size; i++) {
			User user1 = list1.get(i);
			User user2 = list2.get(i);

			if (user1.getUserId() != user2.getUserId()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * This method formats the given integer to represent the size of a file.
	 * 
	 * @param aLength
	 *            Size.
	 * @param aPrecision
	 *            Flag to tell if precision is required.
	 * 
	 * @return Formatted string.
	 * 
	 */
	public static String formatLength(long aLength, boolean aPrecision) {
		int ctr = 0;
		long den = 0;

		while ((aLength >= 1024) && (ctr < 4)) {
			ctr++;
			den = aLength % 1024;
			aLength = aLength / 1024;
		}

		StringBuilder buffer = new StringBuilder();

		buffer.append(aLength);

		// Precision is maintained for sizes greater than 1MB.
		if ((aPrecision == true) && (den != 0) && (ctr > 1)) {
			buffer.append(".");

			// Since we want precision upto 2 digits, truncate the last one.
			if (den > 100) {
				den = den / 10;
				buffer.append(den);
			} else {
				den = den / 10;
				buffer.append("0").append(den).append(" ");
			}
		}

		switch (ctr) {
		case 0:
			buffer.append("B");

			break;

		case 1:
			buffer.append("KB");

			break;

		case 2:
			buffer.append("MB");

			break;

		case 3:
			buffer.append("GB");

			break;

		case 4:
			buffer.append("TB");

			break;
		}

		return buffer.toString();
	}

	/**
	 * This method checks if all the elements of list1 are present in list2.
	 * 
	 * @param list1
	 *            Operand 1.
	 * @param list2
	 *            Operand 2.
	 * 
	 * @return True if list2 contains all elements of list1.
	 */
	public static boolean in(ArrayList<User> list1, ArrayList<User> list2) {

		// Return true if both are null.
		if ((list1 == null) && (list2 == null)) {
			return true;
		}

		// Return false if atleast one of them is null.
		if ((list1 == null) || (list2 == null)) {
			return false;
		}

		// Return true if both are empty
		if ((list1.size() == 0) && (list2.size() == 0)) {
			return true;
		}

		// Return false if source list is lesser in size than the member list.
		if (list1.size() > list2.size()) {
			return false;
		}

		for (User user : list1) {
			if (!list2.contains(user)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method inserts the related requests.
	 * 
	 * @param aRequest
	 * @param aMailIds
	 * @param aRelatedRequests
	 * 
	 * @return Returns the request object furnishing the related request info.
	 * 
	 * @throws Exception
	 */
	public static Request insertRelatedRequests(Request aRequest,
			Hashtable<String, String> aMailIds, String aRelatedRequests)
			throws Exception {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			Request request = insertRelatedRequests(connection, aRequest,
					aMailIds, aRelatedRequests);
			connection.commit();
			return request;
		} catch (SQLException sqle) {
			if (null != connection) {
				try {
					connection.rollback();
				} catch (SQLException sqle1) {
					sqle1.printStackTrace();
				}
			}
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					if (connection != null)
						connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}
			connection = null;
		}
	}

	public static Request insertRelatedRequests(Connection connection,
			Request aRequest, Hashtable<String, String> aMailIds,
			String aRelatedRequests) throws Exception {
		if (aRequest == null) {
			return null;
		}

		StringBuilder batch = new StringBuilder();
		StringBuilder actionBatch = new StringBuilder();
		Hashtable<String, String> relatedRequestsHash = new Hashtable<String, String>();

		//
		// Get distinct related requests by looking for
		// requests Cc'ed to other Bas.
		//
		ArrayList<String> relatedRequestByMail = getRelatedRequestsFromMailsIds(
				connection, aRequest, aMailIds);

		//
		// If no related Requests, do nothing and return.
		//
		if ((aRelatedRequests == null) && (relatedRequestByMail.size() == 0)) {
			aRequest.setRelatedRequests(relatedRequestsHash);

			return aRequest;
		}

		//
		// get all related requests as arraylist of distict strings.
		//
		ArrayList<String> relRequests = new ArrayList<String>(
				relatedRequestByMail);

		if ((aRelatedRequests != null)
				&& (aRelatedRequests.trim().equals("") == false)) {
			StringTokenizer st = new StringTokenizer(aRelatedRequests, ",;");

			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim().toUpperCase();

				if (relRequests.contains(token) == false) {
					relRequests.add(token);
				}
			}
		}

		//
		// Look up for sys_prefix and requestId of the request.
		//
		BusinessArea ba = BusinessArea.lookupBySystemId(aRequest.getSystemId());
		int primarySystemId = aRequest.getSystemId();
		String primaryPrefix = ba.getSystemPrefix();
		int primaryRequestId = aRequest.getRequestId();

		//
		// Store Other Instances one BA Emails to send this information to
		// incase cross site requests are related.
		//
		Hashtable<String, String> crossSiteBaEmail = new Hashtable<String, String>();

		// this stores the Ba location of this primary request.
		String baLocation = ba.getLocation().trim().toUpperCase();

		//
		// Before inserting, delete all records for the request.
		//
		batch.append("delete from related_requests")
				.append(" where (primary_sys_id = '").append(primarySystemId)
				.append("' and primary_request_id = ").append(primaryRequestId)
				.append(")\n");

		//
		// This format will be passed to cross-site as mail and
		// will be processed their to form the batch string.
		//
		actionBatch.append("/delete : ").append(primaryPrefix).append("#")
				.append(primaryRequestId).append("\n");

		//
		// Check each string in list for sysPrefix#NNN pattern.
		//
		Pattern p = Pattern.compile("(([a-zA-Z0-9_]+)#)?([0-9]+)(#([0-9]+))?",
				Pattern.CASE_INSENSITIVE);
		Matcher m = null;

		for (String str : relRequests) {
			m = p.matcher(str);

			//
			// If string matches pattern sysPrefix#NNN
			//
			if (m.matches() == true) {
				String prefix = (m.group(1) == null) ? primaryPrefix : m
						.group(2);
				String loc = "";
				String email = "";

				//
				// Check if its a valid Ba prefix.
				//
				if (primaryPrefix.equals(prefix) == false) {
					ba = BusinessArea.lookupBySystemPrefix(prefix);

					if (ba == null) {
						if (SysPrefixes.getPrefix(prefix) == null) {

							// should this be reported?
							continue;
						} else {
							prefix = SysPrefixes.getPrefix(prefix);
							loc = SysPrefixes.getLocation(prefix);
							email = SysPrefixes.getEmail(prefix);

							if (crossSiteBaEmail.get(loc) == null) {
								crossSiteBaEmail.put(loc, email);
							}
						}
					} else {
						prefix = ba.getSystemPrefix();
					}
				}

				int requestId = Integer.parseInt(m.group(3));
				int actionId = (m.group(4) == null) ? 0 : Integer.parseInt(m
						.group(5));

				//
				// Insert only if not relating to the same request
				//
				if (str.equalsIgnoreCase(primaryPrefix + "#" + primaryRequestId) == false) {
					if (relatedRequestsHash.get(prefix + "#" + requestId) == null) {
						BusinessArea secba = BusinessArea
								.lookupBySystemPrefix(prefix);

						batch.append("Insert into related_requests values")
								.append("(").append(primarySystemId)
								.append(",").append(primaryRequestId)
								.append(",").append(0).append(",")
								.append(secba.getSystemId()).append(",")
								.append(requestId).append(",").append(actionId)
								.append(")\n");

						//
						// This format will be passed to cross-site as mail and
						// will be processed their to form the batch string.
						//
						actionBatch.append("/insert : ").append("('")
								.append(primarySystemId).append("',")
								.append(primaryRequestId).append(",").append(0)
								.append(",'").append(secba.getSystemId())
								.append("',").append(requestId).append(",")
								.append(actionId).append(")\n");
						relatedRequestsHash.put(prefix + "#" + requestId, "");
					}
				}
			}
		}

		if (batch.length() > 0) {
			final String batchStr = batch.toString();
			final String actionBatchStr = actionBatch.toString();
			final String reqLink = primaryPrefix + "#" + primaryRequestId + "#"
					+ aRequest.getMaxActionId();

			LOG.info("inserting linked requests for: " + reqLink);
			Request.insertRelatedRequests(connection, batchStr);

			if (crossSiteBaEmail.size() > 0) {
				final String fromAddress = User.lookupByUserId(
						aRequest.getUserId()).getEmail();
				final Enumeration<String> toAddresses = crossSiteBaEmail
						.elements();

				new Thread() {
					public void run() {
						try {
							String targetEmails = "";
							boolean first = true;

							while (toAddresses.hasMoreElements()) {
								if (first == false) {
									targetEmails = targetEmails + ",";
								}

								targetEmails = targetEmails
										+ toAddresses.nextElement();
								first = false;
							}

							LOG.info("generating related request "
									+ "sync mail for: " + reqLink);
							sendRelatedRequestMail(fromAddress, targetEmails,
									actionBatchStr);
						} catch (RuntimeException e) {
							LOG.severe("error sending related request"
									+ "sync mail for: " + reqLink + "\n"
									+ "",(e));
						} catch (Exception e) {
							LOG.severe("error sending related request"
									+ "sync mail for: " + reqLink + "\n"
									+ "",(e));
						}
					}
				}.start();
			}
		}

		aRequest.setRelatedRequests(relatedRequestsHash);

		return aRequest;
	}

	/**
	 * Main method primarily used for testing.
	 */
	public static void main(String arg[]) {
		class A implements Cloneable {
			public int a;

			public A(int a) {
				this.a = a;
			}

			public String toString() {
				return a + "";
			}

			public A clone() {
				A na = new A(this.a);
				return na;
			}

			public int hashCode() {
				return this.a;
			}

			public boolean equals(Object o) {
				if (!(o instanceof A))
					return false;

				else {
					A ao = (A) o;
					if (this.a == ao.a)
						return true;
				}

				return false;
			}
		}

		try {

			Hashtable<A, A> htaa = new Hashtable<A, A>();
			htaa.put(new A(1), new A(4));
			htaa.put(new A(2), new A(5));
			htaa.put(new A(3), new A(6));
			System.out.println("htaa : " + htaa);

			Hashtable<A, A> chtaa = new Hashtable<A, A>();

			cloneHashtable(htaa, chtaa);

			htaa.get(new A(1)).a = 10;

			System.out.println("chtaa : " + chtaa);
			System.out.println("htaa : " + htaa);
			// ArrayList<A> ala = new ArrayList<A>() ;
			// ala.add(new A(1));
			// ala.add(new A(2));
			// ala.add(new A(3));
			//
			// System.out.println("ala : " + ala);
			//
			// ArrayList<A> cala = new ArrayList<A>() ;
			// APIUtil.cloneCollection(ala, cala);
			//
			// ala.get(0).a = 10 ;
			//
			// System.out.println("cala : " + cala );
			// System.out.println("ala : " + ala );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method modifies the value of a boolean field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyBoolean(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		LOG.info("Modify Boolean " + "[ RequestId= " + aRequest.getRequestId()
				+ ", FieldName= " + aField.getName() + ", New Value= " + aValue
				+ ", aOperator= " + aOperator);

		if (aOperator != Operator.SET) {
			return;
		}
		aValue = getVarValue(aRequest, aField, aValue);

		boolean arg = false;

		if (aValue != null) {
			if (aValue.trim().equals("1") || aValue.trim().equals("true")
					|| aValue.trim().equals("yes")
					|| aValue.trim().equals("private")
					|| aValue.trim().equals("conf")) {
				arg = true;
			}
		}
		String fieldName = aField.getName();
		boolean extended = aField.getIsExtended();

		if (extended == false) {
			if (fieldName.equals(Field.IS_PRIVATE)) {
				aRequest.setIsPrivate(arg);
			} else if (fieldName.equals(Field.NOTIFY)) {
				aRequest.setNotify(arg);
			} else if (fieldName.equals(Field.NOTIFY_LOGGERS)) {
				aRequest.setNotifyLoggers(arg);
			}
		} else {
			if (aReqEx != null) {
				aReqEx.setBitValue(arg);
			}
		}

		return;
	}

	/**
	 * This method modifies the value of a date field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyDate(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		aValue = getVarValue(aRequest, aField, aValue);

		// TODO: implement the modifications on a date field.
		String fieldName = aField.getName();
		boolean isExtended = aField.getIsExtended();
		Timestamp value = null;
		try {
			value = new Timestamp(aValue);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (!isExtended) {
			if (value != null) {
				if (fieldName.equals(Field.DUE_DATE)) {
					aRequest.setDueDate(value);
				} else if (fieldName.equals(Field.LOGGED_DATE)) {
					aRequest.setLoggedDate(value);
				} else if (fieldName.equals(Field.LASTUPDATED_DATE)) {
					aRequest.setLastUpdatedDate(value);
				}
			}
		}
	}

	/**
	 * This method modifies the value of an integer field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyInteger(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		LOG.info("Modify Integer " + "[ RequestId= " + aRequest.getRequestId()
				+ ", FieldName= " + aField.getName() + ", New Value= " + aValue
				+ ", aOperator= " + aOperator);
		// / Supported operators are EQ/SET/ADD/MINUS/REPLACE
		if ((aOperator != Operator.EQ) && (aOperator != Operator.SET)
				&& (aOperator != Operator.ADD) && (aOperator != Operator.MINUS)
				&& (aOperator != Operator.REPLACE)) {
			return;
		}

		// Check the value.
		aValue = getVarValue(aRequest, aField, aValue);

		int value = 1;

		try {
			value = Integer.parseInt(aValue);
		} catch (Exception e) {
			LOG.info("",(e));

			return;
		}

		String fieldName = aField.getName();
		boolean extended = aField.getIsExtended();

		// if (fieldName.equals(Field.NOTIFY)) {
		// aRequest.setNotify(value);
		// }

		if (extended) {

			if (aReqEx != null) {
				if (aOperator == Operator.SET) {
					aReqEx.setIntValue(value);
				}
			}
		} else {
			if (fieldName.equals(Field.PARENT_REQUEST_ID)) {
				aRequest.setParentRequestId(value);
			} else if (fieldName.equals(Field.BUSINESS_AREA)) {
				aRequest.setSystemId(value);
			} else if (fieldName.equals(Field.REQUEST)) {
				aRequest.setRequestId(value);
			} else if (fieldName.equals(Field.REPLIED_TO_ACTION)) {
				aRequest.setRepliedToAction(value);
			} else if (fieldName.equals(Field.MAX_ACTION_ID)) {
				aRequest.setMaxActionId(value);
			} else if (fieldName.equals(Field.APPEND_INTERFACE)) {
				aRequest.setAppendInterface(value);
			}
		}
		// TODO: Implement the modifications on integer extended fields.

		return;
	}

	/**
	 * This method modifies the value of a multi value field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyUserType(Request aRequest, Field aField,
			String aValue, Operator aOperator) {
		LOG.info("Modify Multi Value " + "[ RequestId= "
				+ aRequest.getRequestId() + ", FieldName= " + aField.getName()
				+ ", New Value= " + aValue + ", aOperator= " + aOperator);

		// / Supported operator is BR_APPEND
		if ((aOperator != Operator.APPEND) && (aOperator != Operator.SET)) {
			return;
		}

		aValue = getVarValue(aRequest, aField, aValue);

		int systemId = aRequest.getSystemId();
		int requestId = aRequest.getRequestId();
		String fieldName = aField.getName();

		// We do not allow any operations on User Field.
		if (fieldName.equals(Field.USER)) {
			return;
		}

		ArrayList<RequestUser> reqUserList = new ArrayList<RequestUser>();
		// int userType = 0;

		if (fieldName.equals(Field.LOGGER)) {
			reqUserList.addAll(aRequest.getLoggers());
			// userType = LOGGER;
		} else if (fieldName.equals(Field.ASSIGNEE)) {
			reqUserList.addAll(aRequest.getAssignees());
			// userType = ASSIGNEE;
		} else if (fieldName.equals(Field.SUBSCRIBER)) {
			reqUserList.addAll(aRequest.getSubscribers());
			// userType = SUBSCRIBER;
		} else if (fieldName.equals(Field.TO)) {
			reqUserList.addAll(aRequest.getTos());
			// userType = TO;
		} else if (fieldName.equals(Field.CC)) {
			reqUserList.addAll(aRequest.getCcs());
			// userType = CC;
		}

		LOG.info("\nfieldName: " + fieldName + "\nBefore: \n"
				+ reqUserList.toString());

		/*
		 * The value corresponds to a comma separated list of users. So get the
		 * list.
		 */
		ArrayList<User> userList = toUserList(aValue, false);

		if (aOperator == Operator.SET) {

			/*
			 * We have a new set of values to be used, so clear the old list.
			 */
			reqUserList = new ArrayList<RequestUser>();
		} else {

			/*
			 * Otherwise just append the userList to the existing list of
			 * request users.
			 */
		}

		int ordering = reqUserList.size();

		toReqUserList(systemId, requestId, aField.getFieldId(), ordering,
				userList, reqUserList);

		if (fieldName.equals(Field.LOGGER)) {
			aRequest.setLoggers(reqUserList);

		} else if (fieldName.equals(Field.ASSIGNEE)) {
			aRequest.setAssignees(reqUserList);

		} else if (fieldName.equals(Field.SUBSCRIBER)) {
			aRequest.setSubscribers(reqUserList);

		} else if (fieldName.equals(Field.TO)) {
			aRequest.setTos(reqUserList);

		} else if (fieldName.equals(Field.CC)) {
			aRequest.setCcs(reqUserList);

		}

		LOG.info("\nfield: " + fieldName + "\nAfter: \n"
				+ reqUserList.toString());

		return;
	}

	/**
	 * This method modifies the value of a double field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyReal(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		LOG.info("Modify Real " + "[ RequestId= " + aRequest.getRequestId()
				+ ", FieldName= " + aField.getName() + ", New Value= " + aValue
				+ ", aOperator= " + aOperator);

		// / Supported operators are ADD/MINUS/REPLACE
		if ((aOperator != Operator.ADD) && (aOperator != Operator.MINUS)
				&& (aOperator != Operator.REPLACE)) {
			return;
		}
		boolean extended = aField.getIsExtended();
		aValue = getVarValue(aRequest, aField, aValue);
		double value = Double.parseDouble(aValue);
		if (extended) {
			aReqEx.setRealValue(value);
		}

		// TODO: Implement the modifications on real-extended fields.
	}

	/**
	 * This method modifies the value of a string field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyString(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		LOG.info("Modify String " + "[ RequestId= " + aRequest.getRequestId()
				+ ", FieldName= " + aField.getName() + ", New Value= " + aValue
				+ ", aOperator= " + aOperator);

		// / Supported operators are PREPEND and APPEND
		if ((aOperator != Operator.PREPEND) && (aOperator != Operator.APPEND)) {
			return;
		}

		String fieldName = aField.getName();
		boolean extended = aField.getIsExtended();
		aValue = getVarValue(aRequest, aField, aValue);
		if (extended == false) {
			if (fieldName.equals(Field.RELATED_REQUESTS)) {
				aRequest.setRelatedRequests(aValue);
			} else if (fieldName.equals(Field.SUBJECT)) {
				aRequest.setSubject(aValue);
			}

		} else {
			aReqEx.setTextValue(aValue);
		}

		// TODO: Implement modifications on string fields.
	}

	/**
	 * This method modifies the value of a text field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyText(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		LOG.info("Modify Text " + "[ RequestId= " + aRequest.getRequestId()
				+ ", FieldName= " + aField.getName() + ", New Value= " + aValue
				+ ", aOperator= " + aOperator);
		// / Supported operators are PREPEND and APPEND
		if ((aOperator != Operator.PREPEND) && (aOperator != Operator.APPEND)) {
			return;
		}

		aValue = getVarValue(aRequest, aField, aValue);
		String fieldName = aField.getName();
		boolean extended = aField.getIsExtended();
		aValue = getVarValue(aRequest, aField, aValue);
		if (extended == false) {

			if (fieldName.equals(Field.DESCRIPTION)) {
				aRequest.setDescription(aValue);
			} else if (fieldName.equals(Field.HEADER_DESCRIPTION)) {
				aRequest.setHeaderDescription(aValue);
			}
			// else if (fieldName.equals(Field.ATTACHMENTS)) {
			// aRequest.setAttachments(aValue);
			// }
			else if (fieldName.equals(Field.SUMMARY)) {
				aRequest.setSummary(aValue);
			} else if (fieldName.equals(Field.MEMO)) {
				aRequest.setMemo(aValue);
			}

		} else {
			aReqEx.setTextValue(aValue);
		}

		// TODO: Implement modifications on text fields.
	}

	/**
	 * This method modifies the value of a type field in the request.
	 * 
	 * @param aRequest
	 *            Request Object.
	 * @param aReqEx
	 *            Request Ex Object.
	 * @param aField
	 *            Field whose value should be changed.
	 * @param aValue
	 *            Target value.
	 * @param aOperator
	 *            Operator.
	 * 
	 */
	public static void modifyType(Request aRequest, RequestEx aReqEx,
			Field aField, String aValue, Operator aOperator) {
		LOG.info("Modify Type " + "[ RequestId= " + aRequest.getRequestId()
				+ ", FieldName= " + aField.getName() + ", New Value= " + aValue
				+ ", aOperator= " + aOperator);

		// / Supported operators are SET and REPLACE
		if ((aOperator != Operator.SET) && (aOperator != Operator.REPLACE)) {
			return;
		}

		aValue = getVarValue(aRequest, aField, aValue);

		int systemId = aRequest.getSystemId();
		int fieldId = aField.getFieldId();
		String fieldName = aField.getName();
		boolean isExtended = aField.getIsExtended();

		try {
			Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId,
					fieldName, aValue);

			if (type != null) {
				if (fieldName.equals(Field.CATEGORY)) {
					aRequest.setCategoryId(type);
				} else if (fieldName.equals(Field.STATUS)) {
					aRequest.setStatusId(type);
				} else if (fieldName.equals(Field.SEVERITY)) {
					aRequest.setSeverityId(type);
				} else if (fieldName.equals(Field.REQUEST_TYPE)) {
					aRequest.setRequestTypeId(type);
				} else if (isExtended == true) {
					if (aReqEx != null) {
						aReqEx.setTypeValue(type.getTypeId());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Handle the exception in a better way.
		}
	}

	/**
	 * This method decides the boolean value of the string passed.
	 * 
	 * @param str
	 * @return ...
	 */
	private static boolean parseBoolean(String str) {
		if (str == null) {
			return false;
		}

		str = str.trim().toLowerCase();

		/*
		 * false/public/0 is Boolean.FALSE
		 */
		if (str.equals("false") || str.equals("public") || str.equals("0")) {
			return false;
		}

		/*
		 * Anything else is true.
		 */
		return true;
	}

	/**
	 * This method returns the GMT value of the given date string.
	 * 
	 * 
	 * @param aValue
	 *            Date/Time/Datetime value.
	 * @return Timestamp object corresponding to the given value.
	 * @exception TBitsException
	 *                in case of any parse exceptions.
	 */
	public static Timestamp parseDateTime(String aValue) throws TBitsException {
		if ((aValue == null) || (aValue.trim().equals("") == true)) {
			throw new TBitsException("Date should be specified in "
					+ API_DATE_FORMAT + " format.");
		}

		Timestamp value = null;
		Matcher m = DATETIME_PATTERN.matcher(aValue);

		if (m.matches() == true) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);

				value = Timestamp.getTimestamp(sdf.parse(aValue));

				return value.toGmtTimestamp();
			} catch (Exception e) {
				throw new TBitsException("Date should be specified in "
						+ API_DATE_FORMAT + " format. " + e.toString());
			}
		}

		return value;
	}

	public static String printDate(long l) {
		StringBuilder sb = new StringBuilder("The long is: " + l).append("\n");
		SimpleDateFormat sdf = new SimpleDateFormat();
		sb.append(
				"In '" + sdf.getTimeZone().getDisplayName() + "' : "
						+ sdf.format(new Date(l))).append("\n");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		sb.append("In GMT : " + sdf.format(new Date(l))).append("\n");
		return sb.toString();
	}

	/**
	 * This method processes the attachments present in the tmp.dir and returns
	 * an array list of attachment names after moving them to the business area
	 * directory and renaming them with corresponding
	 * requestid-actionid-AttachmentName-Size-CFactor.
	 * 
	 * 
	 * @param aSysPrefix
	 *            Business area prefix.
	 * @param aRequestId
	 *            Request Id.
	 * @param aActionId
	 *            Action Id.
	 * @param aUser
	 *            User
	 * @param aAttachments
	 *            Comma separated list of attachment names.
	 * @param aBmpToPng
	 *            Flag to say if conversion should happen.
	 * @param aPrecision
	 *            Flag to say if precision is required.
	 * @return XML to represent the stored attachments.
	 * @throws TBitsException
	 * @exception TBitsException
	 *                Incase of any errors.
	 */
	/*
	 * public static String processAttachments(String aSysPrefix, int
	 * aRequestId, int aActionId, User aUser, String aAttachments, boolean
	 * aBmpToPng, boolean aPrecision,StringBuilder headerAppenderAtt,
	 * Hashtable<String,String> fileActions) throws TBitsException{
	 * FileResourceManager fileResMgr = new FileResourceManager(); Connection
	 * connection = null; try { connection = DataSourcePool.getConnection();
	 * return processAttachments(connection, aSysPrefix, aRequestId, aActionId,
	 * aUser, aAttachments, aBmpToPng, aPrecision, headerAppenderAtt,
	 * fileActions, fileResMgr); } catch (SQLException e1) { // TODO
	 * Auto-generated catch block e1.printStackTrace(); throw new
	 * TBitsException("Unable to get the connection to database.", e1); } catch
	 * (TBitsException e) { // TODO Auto-generated catch block
	 * fileResMgr.rollback(); throw e; }finally { if(connection != null) { try {
	 * connection.close(); } catch(SQLException sqle) {
	 * LOG.warn("Error while closeing the connection", sqle); } } } }
	 */

	/**
	 * This method processes the attachment list. generates the requestFileId
	 * for the newly added attachments
	 * 
	 * @param aSysPrefix
	 *            Business area prefix.
	 * @param aRequestId
	 *            Request Id.
	 * @param aActionId
	 *            Action Id.
	 * @param aUser
	 *            User
	 * @param myAttachments
	 *            Comma separated list of attachment names.
	 * @param aBmpToPng
	 *            Flag to say if conversion should happen.
	 * @param aPrecision
	 *            Flag to say if precision is required.
	 * @param fileResMgr
	 *            The resource manager which can help rolling back the file move
	 *            operation.
	 * @return XML to represent the stored attachments.
	 * @throws TBitsException
	 * @exception TBitsException
	 *                Incase of any errors.
	 */
	@Deprecated
	// Nitiraj : Will always throw null pointer exception.
	// public static String processAttachments(Connection connection, String
	// aSysPrefix, int aRequestId, int aActionId,
	// User aUser, Collection<AttachmentInfo> myAttachments, boolean aBmpToPng,
	// boolean aPrecision,StringBuilder headerAppenderAtt,
	// Hashtable<String,String> fileActions, FileResourceManager fileResMgr)
	// throws TBitsException {
	//
	//
	// System.out.println(myAttachments);
	// String userLogin = aUser.getUserLogin();
	//
	// if ((aUser.getUserTypeId() == UserType.INTERNAL_MAILINGLIST) ||
	// (aUser.getUserTypeId() == UserType.INTERNAL_HIDDEN_LIST) ||
	// (aUser.getUserTypeId() == UserType.EXTERNAL_USER)
	// || (aUser.getUserTypeId() == UserType.INTERNAL_CONTACT)) {
	// int index = userLogin.indexOf('@');
	//
	// //TODO: what happens when index == 0?
	// if (index > 0) {
	// userLogin = userLogin.substring(0, index);
	// }
	// }
	//
	// // Convert the prefix to lowercase.
	// aSysPrefix = aSysPrefix.toLowerCase();
	//
	// StringBuilder message = new StringBuilder();
	//
	// // Check if the directory for this Business area is existing. If not
	// create.
	// checkDir(aSysPrefix);
	//
	// //
	// // aAttachments will be a string of new-line separated entries of
	// // attachment information. And each entry is a tab separated list
	// // of stored name and display name.
	// //
	// ArrayList<String> attachmentList =
	// null;//Utilities.toArrayList(myAttachments, "\n");
	// ArrayList<Attachment> attachList = new ArrayList<Attachment>();
	// boolean first = true;
	// int counter = 0;
	//
	// for (String attachment : attachmentList) {
	// boolean converted = false;
	// boolean extracted = false;
	//
	// // Each entry is a tab-separated list of stored and display names.
	// String[] list = attachment.split("\t");
	//
	// // Get the stored name.
	// String pStored = list[0];
	//
	// // Get the display name.
	// String nDisplay = list[1];
	//
	// String fileAction = null;
	// //get the file action (Added, Modified or Deleted)
	// if(list.length > 2)
	// fileAction = list[2];
	// else
	// {
	// //If the File action isn't specified by the caller. This can happen from
	// web/email etc.
	// //Check the previous state of the file. If there is no action containing
	// the file in this request or
	// //the file was deleted in the most recent update, mark it added.
	// fileAction = getTVNFileAction(connection, nDisplay, aSysPrefix,
	// aRequestId);
	// }
	// fileActions.put(nDisplay, fileAction);
	//
	// //if fileAction is Deleted, then no need to go further for this
	// attachment
	// //just append this information to the header
	// if(fileAction.equals(WebdavConstants.FILE_DELETED)) {
	// headerAppenderAtt.append("['"+ nDisplay +"'" +
	// " was Deleted ]\n");
	// System.out.println("#######################");
	// System.out.println(headerAppenderAtt.toString());
	// System.out.println("###################");
	// continue;
	// }
	// // form the stored path and get a file object to it.
	// //String pStored = nStored;//ourTmpLocation + "/" + userLogin + "-" +
	// nStored;
	// File fStored = new File(pStored);
	//
	// //To check,whether this attachment is different from its previous version
	// or not
	// // if fileAction is null, this request must be through web based
	// interface
	// // in such case, checkforUpdateInAttachment will put a valid value in
	// //
	// boolean isDifferent = true;
	//
	// try
	// {
	// isDifferent = checkForUpdateInAttachment(connection, nDisplay,pStored,
	// aSysPrefix,aRequestId);
	// }
	// catch(Exception e)
	// {
	// LOG.error("Error in checking the update in the attachment. Assuming it to be different.",
	// e);
	// }
	//
	// if(!isDifferent) {
	// if(null != headerAppenderAtt)
	// headerAppenderAtt.append("['"+ nDisplay +"'" +
	// " was not uploaded because No Changes were Made since " +
	// "last commit ]\n" );
	// fileActions.remove(nDisplay);
	// continue;
	// }
	//
	//
	// if (fStored == null) {
	// LOG.warn("Attachment with display name '"+ nDisplay +"' not found: " +
	// pStored);
	//
	// continue;
	// }
	//
	//
	// //
	// // Check if the attachment is a BMP. In such case, perform the
	// // conversion step.
	// //
	// //TODO: need to be conditional based on a configurable prop
	// tbits.convertbmptopng=true
	// // and need to check if sam2p exists
	// if (pStored.toLowerCase().endsWith(".bmp") == true) {
	// try {
	// //String bmpName = pStored;
	// String pngPath = pStored.substring(0, pStored.length() - 4) + ".png";
	// //String pngDisplayName = nDisplay.substring(0, pStored.length() - 4) +
	// ".png";
	// //String bmpPath = ourTmpLocation + "/" + userLogin + "-" + bmpName;
	// //String pngPath = ourTmpLocation + "/" + userLogin + "-" + pngName;
	// //TODO: to be replaced configurable path
	// String command = "/usr/local/bin/sam2p \'" + pStored + "\' PNG: " +
	// pngPath;
	// Runtime runtime = Runtime.getRuntime();
	// Process process = runtime.exec(command);
	// StreamGobbler sg = new StreamGobbler(process.getErrorStream());
	//
	// sg.start();
	//
	// InputStreamReader ipstream = new
	// InputStreamReader(process.getInputStream());
	// int chr = ipstream.read();
	//
	// while (chr != -1) {
	// chr = ipstream.read();
	// }
	//
	// String errorMessage = sg.getMessage();
	//
	// if ((errorMessage != null) &&!errorMessage.trim().equals("")) {}
	//
	// LOG.info("sam2p Command: " + command);
	// LOG.info("sam2p Error: " + sg.getMessage());
	//
	// File fBmp = new File(pStored);
	// File fPng = new File(pngPath);
	//
	// // Check if the conversion is successful.
	// if ((fPng != null) && (fPng.exists() == true)) {
	//
	// // Delete the bmp version.
	// fBmp.delete();
	//
	// // Set the name of the attachment to the pngName.
	// //nStored = pngPath;
	// pStored = pngPath;
	// fStored = fPng;
	// nDisplay = nDisplay.substring(0, nDisplay.length() - 4) + ".png";
	// converted = true;
	// }
	// } catch (Exception e) {
	// LOG.info("Error while converting from BMP to PNG: " +
	// "",(e));
	// converted = false;
	// }
	// }
	//
	// //
	// // Check if file is extracted from an msg.If yes, mark extracted
	// // as true and remove "Msg_" prefixed before the file names.
	// //
	// if (nDisplay.startsWith("Msg_Message")) {
	// extracted = true;
	// //nStored = nStored.substring(4);
	// nDisplay = nDisplay.substring(4);
	// }
	// // Commented out by Abhishek To make it compatible with TVN
	// // nDisplay = nDisplay.replaceAll("[^a-zA-Z0-9._]", "_");
	//
	// String proposedfileName = aRequestId + "-" + aActionId + "-"
	// + nDisplay.replaceAll("[^a-zA-Z0-9._]", "_");
	//
	// int dotIndex = proposedfileName.lastIndexOf(".");
	// String proposedPrefix = null;
	// String proposedSuffix = ".tmp";
	// if(dotIndex > -1)
	// {
	// proposedPrefix = proposedfileName.substring(0, dotIndex);
	// proposedSuffix = proposedfileName.substring(dotIndex + 1);
	// }
	// else
	// {
	// proposedPrefix = proposedfileName;
	// }
	//
	//
	// File fTarget;
	// try {
	// fTarget = Utilities.createTempFile(proposedPrefix, proposedSuffix,
	// new File(ourAttachmentLocation + "/" + aSysPrefix));
	// } catch (IOException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// throw new
	// TBitsException("Unable to create file in the attachments folder. Please check if you have permission.",
	// e1);
	// }
	//
	// if(fTarget == null)
	// {
	// throw new
	// TBitsException("Unable to create file in the attachments folder. Please check if you have permission.");
	// }
	//
	// //It should be copy.
	// //First Copy
	// try {
	// Utilities.copyFile(fStored, fTarget);
	// fileResMgr.addTuple(fStored, fTarget);
	// } catch (Exception e) {
	// LOG.severe("Unable to copy the file from '" + fStored.getAbsolutePath()
	// + "' to '" + fTarget.getAbsolutePath() + "'", e);
	// continue;
	// }
	//
	// if (!fStored.delete()) {
	// LOG.error("Could not delete the temporary file '" + fStored + "'.");
	// }
	//
	// long fileSize = fTarget.length();
	//
	// LOG.info("FileSize: " + fileSize);
	// counter = counter + 1;
	//
	// String attId = aRequestId + "_" + aActionId + "_" + counter;
	// Attachment obj = new Attachment();
	//
	// obj.setId(attId);
	// obj.setName(fTarget.getName());
	// obj.setDisplayName(nDisplay);
	// obj.setSizeInBytes(fileSize);
	// obj.setSize(formatLength(fileSize, true));
	// obj.setIsConverted(converted);
	// obj.setIsExtracted(extracted);
	// attachList.add(obj);
	// }
	//
	// if (message.toString().trim().equals("") == false) {
	// throw new TBitsException(message.toString());
	// }
	//
	// String xml = Attachment.xmlSerialize(attachList);
	// System.out.println(xml);
	// LOG.debug("Returning:\n" + xml);
	//
	// return xml;
	// }
	//
	// /**
	// * @throws DatabaseException
	// * @throws TBitsException
	// * @throws SQLException
	// *
	// *
	// */
	//
	// public static boolean checkForUpdateInAttachment(Connection connection,
	// String attName,String attLocation,
	// String systemPrefix,int requestId)
	// throws TBitsException, SQLException {
	//
	//
	// /**
	// * This portion was added by Abhishek
	// * if (the given attachment exists in previous version and
	// * user is trying to upload it without making any changes) {
	// * do not add the attachment to attachList;
	// * continue;
	// * }
	// * else {
	// * proceed normally
	// * }
	// *
	// */
	// try {
	//
	// BusinessArea myBusinessArea =
	// BusinessArea.lookupBySystemPrefix(systemPrefix);
	// int aSystemId = myBusinessArea.getSystemId();
	// int verNum = WebdavUtil.getHeadRevision(connection, systemPrefix);
	// Hashtable<String, Object> params =
	// WebdavUtil.getActionAndFileActions(connection, aSystemId,
	// requestId,verNum,attName);
	// Action action = (Action)params.get(Field.ACTION);
	// String fileAction = (String)params.get(WebdavConstants.FILE_ACTION);
	//
	// if(null == action
	// || fileAction.equals(WebdavConstants.FILE_DELETED)){
	// return true;
	// }
	//
	//
	// AttachmentInfo att = WebdavUtil.getAttachment(action, attName);
	// //TODO: what if the the att is null
	// // This can happen when the attachments are correctly added in version
	// table but not in request.
	// if(att == null)
	// return true;
	// String oldLocation = ourAttachmentLocation + "/" +
	// Uploader.getFileLocation(att.repoFileId);
	//
	// String newChecksum = DeltaUtil.computeChecksum(attLocation);
	// String oldChecksum = DeltaUtil.computeChecksum(oldLocation);
	// System.out.println("version is: " + verNum);
	// System.out.println("new checksum is :" + newChecksum);
	// System.out.println("old checksum is : " + oldChecksum);
	// if(newChecksum.equals(oldChecksum))
	// return false;
	// else
	// return true;
	// } catch (DatabaseException e) {
	// e.printStackTrace();
	// throw new TBitsException(e);
	// }
	//
	// }
	public static String getTVNFileAction(Connection connection,
			String attName, String systemPrefix, int requestId)
			throws TBitsException {

		try {
			BusinessArea myBusinessArea = BusinessArea
					.lookupBySystemPrefix(systemPrefix);
			int aSystemId = myBusinessArea.getSystemId();
			int verNum = WebdavUtil.getHeadRevision(connection, systemPrefix);
			Hashtable<String, Object> params = WebdavUtil
					.getActionAndFileActions(connection, aSystemId, requestId,
							verNum, attName);
			Action action = (Action) params.get(Field.ACTION);
			String fileAction = (String) params
					.get(WebdavConstants.FILE_ACTION);

			return (null == action || fileAction
					.equals(WebdavConstants.FILE_DELETED)) ? WebdavConstants.FILE_ADDED
					: WebdavConstants.FILE_MODIFIED;
		} catch (DatabaseException e) {
			throw new TBitsException("Error in obtaining the business area", e);
		}
	}

	public static void sendRelatedRequestMail(String aFromAddress,
			String aToAddresses, String aDescription) throws Exception {

		Session session = Mail.getSession();
		// Parameters needed while constructing the message.
		// From address
		InternetAddress fromAddress = new InternetAddress(aFromAddress);

		// Reply To Address
		InternetAddress[] replyTo = new InternetAddress[1];

		replyTo[0] = fromAddress;

		// List of users who appear in to-list.
		InternetAddress[] toList = InternetAddress.parse(aToAddresses);

		// The only header we want to pass.
		String headerKey = INSERT_RELATED_HEADER;
		String headerValue = "insert";

		// create properties and get the default Session.
		MimeMessage message = new MimeMessage(session);

		message.setFrom(fromAddress);
		message.setReplyTo(replyTo);
		message.setRecipients(Message.RecipientType.TO, toList);
		message.setSubject("Insert related Requests");
		message.setText(aDescription);
		message.setHeader(headerKey, headerValue);

		//
		// finally handover the message to the sendMail running on this
		// Machine for queuing this message for further processing.
		//
		Mail.postMessageToSMTPServer(session, message, aFromAddress,
				aToAddresses);
	}

	/**
	 * This method returns a comma separated list of userlogins given the
	 * ArrayList of Request users.
	 * 
	 * @param aList
	 *            List of request users.
	 * 
	 * @return Comma separated list of user logins.
	 * 
	 */
	public static String toLoginList(Collection<RequestUser> aList) {
		return toLoginList(aList, ",");
	}

	/**
	 * This method returns a comma separated list of userlogins given the
	 * ArrayList of Request users.
	 * 
	 * @param aList
	 *            List of request users.
	 * 
	 * @return Comma separated list of user logins.
	 * 
	 */
	public static String toLoginList(Collection<RequestUser> aList,
			String separator) {
		StringBuilder buffer = new StringBuilder();

		if (aList == null) {
			return buffer.toString();
		}

		boolean first = true;

		// creating another ArrayList to sort the request-user
		ArrayList<RequestUser> reqUsers = new ArrayList<RequestUser>(aList);
		Collections.sort(reqUsers, new Comparator<RequestUser>() {

			public int compare(RequestUser ru, RequestUser ru1) {
				if (ru.getOrdering() < ru1.getOrdering())
					return -1;
				else if (ru.getOrdering() > ru1.getOrdering())
					return 1;
				else
					return 0;
			}
		});

		for (RequestUser reqUser : reqUsers) {
			String userLogin = "";

			try {
				User user = reqUser.getUser();

				if (user == null) {
					LOG.severe("No user object found corresponding to one of "
							+ "the request users:" + "[ BA ID: "
							+ reqUser.getSystemId() + ", Request: "
							+ reqUser.getRequestId() + "]");

					continue;
				}

				userLogin = user.getUserLogin()
						.replace(".transbittech.com", "");

				if (reqUser.getIsPrimary() == true) {
					userLogin = "*" + userLogin;
				}
			} catch (DatabaseException de) {
				LOG.warn(de.toString(), de);

				continue;
			}

			if (first == false) {
				buffer.append(separator);
			} else {
				first = false;
			}

			buffer.append(userLogin);
		}

		return buffer.toString();
	}

	/**
	 * gives you a sysPrefix#RequestId[#ActionId] format of strings for the
	 * requests in the requests
	 * 
	 * @param requests
	 *            : the collection of requests
	 * @return
	 */
	public static String getRequestsString(Collection<RequestDataType> requests) {
		StringBuilder relatedRequests = new StringBuilder();
		boolean first = true;

		for (RequestDataType req : requests) {
			if (first == false) {
				relatedRequests.append(",");
			}

			BusinessArea ba = null;
			try {
				ba = BusinessArea.lookupBySystemId(req.getSysId());
			} catch (DatabaseException e) {
				LOG.info("",(e));
			}

			if (null == ba)
				throw new IllegalArgumentException("BusinessArea with sysId = "
						+ req.getSysId() + " not found.");

			String reqStr = ba.getSystemPrefix();

			int reqId = req.getRequestId();
			reqStr += "#" + reqId;

			int actionId = req.getActionid();
			if (actionId != 0)
				reqStr += "#" + actionId;

			relatedRequests.append(reqStr);

			first = false;
		}

		return relatedRequests.toString();
	}

	/**
	 * gives you a collection of RequestDataType objects from the string the
	 * format of the string should be
	 * <sysPrefix>#<RequestId>[#<ActionId>][,<sysPrefix
	 * >#<RequestId>[#<ActionId>]] the separator ";" is also supported for
	 * separting two requests but should be avoided. Although the method returns
	 * the HashSet concrete type but one should treat returned object to be a
	 * Collection only and not any specific type.
	 * 
	 * @param requests
	 * @return null if the requests is null else the collection of
	 *         RequestDataType which can be empty
	 */
	public static Collection<RequestDataType> getRequestCollection(
			String requests) {
		if (null == requests)
			return null;

		HashSet<RequestDataType> hashSet = new HashSet<RequestDataType>();
		for (String req : requests.split("[,;]")) {
			String[] reqElements = req.split("#");
			if (reqElements.length == 2 || reqElements.length == 3) {
				String sysPrefix = reqElements[0].trim();
				BusinessArea ba = null;
				try {
					ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				} catch (DatabaseException e) {
					LOG.info("",(e));
				}

				if (null == ba)
					throw new IllegalArgumentException(
							"The BusinessArea with sys-prefix = " + sysPrefix
									+ " not found for the string : " + req);

				Integer reqId = null;
				try {
					reqId = Integer.parseInt(reqElements[1].trim());
				} catch (NumberFormatException nfe) {
					LOG.info("",(nfe));
					throw new IllegalArgumentException("Illegal RequestId="
							+ reqElements[1].trim() + " in the string " + req);
				}

				Integer actionId = 0;
				if (reqElements.length == 3) {
					try {
						actionId = Integer.parseInt(reqElements[2].trim());
					} catch (NumberFormatException nfe) {
						LOG.info("",(nfe));
						throw new IllegalArgumentException("Illegal ActionId="
								+ reqElements[2].trim() + " in the string "
								+ req);
					}
				}
				RequestDataType reqData = new RequestDataType(ba.getSystemId(),
						reqId, actionId);
				hashSet.add(reqData);
			} else
				throw new IllegalArgumentException(
						"The Requests string "
								+ req
								+ " was not of format <sysPrefix>#<RequestId>[#<ActionId>] ");
		}

		return hashSet;
	}

	/**
	 * gives you a collection of RequestDataType objects from the hashtable the
	 * format of the keys in hashtable should be
	 * <sysPrefix>#<RequestId>[#<ActionId>] Although the method returns the
	 * HashSet concrete type but one should treat returned object to be a
	 * Collection only and not any specific type.
	 * 
	 * @param aRelatedRequests
	 * @return
	 */
	public static Collection<RequestDataType> getRequestCollection(
			Hashtable<String, String> requests) {
		if (null == requests)
			return null;

		HashSet<RequestDataType> hashSet = new HashSet<RequestDataType>();

		for (Enumeration<String> keys = requests.keys(); keys.hasMoreElements();) {
			String req = keys.nextElement();
			String[] reqElements = req.split("#");
			if (reqElements.length == 2 || reqElements.length == 3) {
				String sysPrefix = reqElements[0].trim();
				BusinessArea ba = null;
				try {
					ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				} catch (DatabaseException e) {
					LOG.info("",(e));
				}

				if (null == ba)
					throw new IllegalArgumentException(
							"The BusinessArea with sys-prefix = " + sysPrefix
									+ " not found for the string : " + req);

				Integer reqId = null;
				try {
					reqId = Integer.parseInt(reqElements[1].trim());
				} catch (NumberFormatException nfe) {
					LOG.info("",(nfe));
					throw new IllegalArgumentException("Illegal RequestId="
							+ reqElements[1].trim() + " in the string " + req);
				}

				Integer actionId = 0;
				if (reqElements.length == 3) {
					try {
						actionId = Integer.parseInt(reqElements[2].trim());
					} catch (NumberFormatException nfe) {
						LOG.info("",(nfe));
						throw new IllegalArgumentException("Illegal ActionId="
								+ reqElements[2].trim() + " in the string "
								+ req);
					}
				}
				RequestDataType reqData = new RequestDataType(ba.getSystemId(),
						reqId, actionId);
				hashSet.add(reqData);
			} else
				throw new IllegalArgumentException(
						"The Requests string "
								+ req
								+ " was not of format <sysPrefix>#<RequestId>[#<ActionId>] ");
		}

		return hashSet;
	}

	/**
	 * This method returns the RequestUser list with the given User List and
	 * other required information.
	 * 
	 * @param aSystemId
	 *            BA Id.
	 * @param aRequestId
	 *            Request Id.
	 * @param aUserTypeId
	 *            User Type Id.
	 * @param aOrdering
	 *            Ordering information.
	 * @param aList
	 *            UserList.
	 * @param aReqUserList
	 *            Corresponding RequestUser list.
	 */
	public static void toReqUserList(int aSystemId, int aRequestId,
			int fieldId, int aOrdering, ArrayList<User> aList,
			ArrayList<RequestUser> aReqUserList) {
		for (User user : aList) {
			RequestUser ru = new RequestUser(aSystemId, aRequestId,
					user.getUserId(), ++aOrdering, false, fieldId);

			if (aReqUserList.contains(ru) == false) {
				aReqUserList.add(ru);
			}
		}
	}

	/**
	 * This method returns the list of user objects corresponding to the comma
	 * separated list of user logins in the given list.
	 * 
	 * @param cssList
	 *            Comma separated list of user logins.
	 * 
	 * @return ArrayList of user objects.
	 */
	public static ArrayList<User> toUserList(String cssList, boolean expand) {
		ArrayList<User> list = new ArrayList<User>();

		if ((cssList == null) || cssList.trim().equals("")) {
			return list;
		}

		StringTokenizer st = new StringTokenizer(cssList, ";,");

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			boolean localExpand = false;

			token = (token == null) ? "" : token.trim();

			if (token.equals("") == true) {
				continue;
			}

			// Check if the token starts with membersof/memberof
			if (token.startsWith("membersof:")) {
				token = token.substring("membersof:".length());
				localExpand = true;
			} else if (token.startsWith("memberof")) {
				token = token.substring("memberof:".length());
				localExpand = true;
			}

			User user = null;

			try {
				user = User.lookupAllByUserLogin(token);

				if (user == null) {
					continue;
				}

				int userId = user.getUserId();

				// Check if this user object corresponds to a mailing list.
				if (user.getUserTypeId() == INTERNAL_MAILINGLIST) {

					// Check if this should be expanded.
					if ((expand == false) && (localExpand == false)) {
						list.add(user);

						continue;
					}

					/*
					 * Otherwise expand the mailing list to get the resultant
					 * users.
					 */
					ArrayList<User> mList = MailListUser.getMemberUsers(userId);

					if ((mList != null) && (mList.size() != 0)) {
						for (User tmp : mList) {
							if (list.contains(tmp) == false) {
								list.add(tmp);
							}
						}
					}
				} else {

					/*
					 * Include non-mailing list users if they are not already
					 * present
					 */
					if (list.contains(user) == false) {
						list.add(user);
					}
				}
			} catch (Exception e) {
				LOG.info(e.toString());

				continue;
			}

			if (user == null) {
				continue;
			}
		}

		return list;
	}

	// ~--- get methods --------------------------------------------------------

	/**
	 * This method returns distinct related requests by looking for requests
	 * Cc'ed to other Bas.
	 */
	public static ArrayList<String> getRelatedRequestsFromMailsIds(
			Connection connection, Request aRequest,
			Hashtable<String, String> aMailIds) throws Exception {
		ArrayList<String> relatedRequests = new ArrayList<String>();

		if ((aRequest == null) || (aMailIds == null)) {
			return relatedRequests;
		}

		String mailIds = aMailIds.get(Field.CC);

		if ((mailIds != null) && (mailIds.trim().equals("") == false)) {
			ArrayList<String> mailList = Utilities.toArrayList(mailIds);

			for (String email : mailList) {
				BusinessArea ba = BusinessArea.lookupByEmail(email);

				//
				// If Ba exist in other site, then also Ba will be null.
				//
				if (ba == null) {
					continue;
				}

				int requestId = Request.lookupBySystemIdAndRequestData(
						connection, ba.getSystemId(), aRequest);

				if (requestId == -1) {
					continue;
				}

				String relReq = ba.getSystemPrefix() + "#" + requestId;

				relReq = relReq.toUpperCase();

				if (relatedRequests.contains(relReq) == false) {
					relatedRequests.add(relReq);
				}
			}
		}

		mailIds = aMailIds.get(Field.TO);

		if ((mailIds != null) && (mailIds.trim().equals("") == false)) {
			ArrayList<String> mailList = Utilities.toArrayList(mailIds);

			for (String email : mailList) {
				BusinessArea ba = BusinessArea.lookupByEmail(email);

				//
				// If Ba exist in other site, then also Ba will be null.
				//
				if (ba == null) {
					continue;
				}

				int requestId = Request.lookupBySystemIdAndRequestData(
						connection, ba.getSystemId(), aRequest);

				//
				// If Ba exist in other site, then also Request will be null.
				//
				if (requestId == -1) {
					continue;
				}

				String relReq = ba.getSystemPrefix() + "#" + requestId;

				relReq = relReq.toUpperCase();

				if (relatedRequests.contains(relReq) == false) {
					relatedRequests.add(relReq);
				}
			}
		}

		return relatedRequests;
	}

	/**
	 * This method returns a valid list of volunteers for the specified list of
	 * possible volunteers and the business area.
	 * 
	 * @param aVolunteersList
	 * @param aSystemId
	 * 
	 * @return List of valid volunteers.
	 * 
	 * @throws DatabaseException
	 *             Incase of any database related errors.
	 */
	public static ArrayList<TypeUser> getValidVolunteersList(
			ArrayList<TypeUser> aVolunteersList, int aSystemId)
			throws DatabaseException {
		ArrayList<TypeUser> validVolunteers = new ArrayList<TypeUser>();

		for (TypeUser typeUser : aVolunteersList) {

			//
			// If user not on vacation , add to the valid volunteers list
			//
			if (isOnVacation(typeUser.getUser(), aSystemId) == false) {
				validVolunteers.add(typeUser);
			}
		}

		return validVolunteers;
	}

	/**
	 * This method obtains the volunteer for the given systemid and the given
	 * category id.
	 * 
	 * @param aSystemId
	 *            Business Area Id.
	 * @param aCategoryId
	 *            Id of the category.
	 * 
	 * @param aMethod
	 *            Method to be used to select the volunteer from list.
	 * 
	 * @return UserLogin of the volunteer if found comma seperated by next RR
	 *         volunteer for Roundrobin scheduling.
	 */
	public static String getVolunteer(int aSystemId, int aCategoryId,
			int aMethod) throws DatabaseException {
		if (aMethod == NO_VOLUNTEER) {
			LOG.warn("This BA is not configured for volunteer" + "generation: "
					+ aSystemId);

			return "";
		}

		Field field = Field.lookupBySystemIdAndFieldName(aSystemId,
				Field.CATEGORY);

		//
		// Get Volunteers for the specified category
		//
		ArrayList<TypeUser> AllVolunteers = TypeUser
				.lookupVolunteersBySystemIdAndFieldIdAndTypeId(aSystemId,
						field.getFieldId(), aCategoryId);

		//
		// If no Volunteers available, return
		//
		if (AllVolunteers.size() == 0) {
			return "";
		}

		//
		// Remove vacanatiers from the valid volunteers list
		//
		ArrayList<TypeUser> validVolunteers = getValidVolunteersList(
				AllVolunteers, aSystemId);

		//
		// If all volunteers are on vacation, return
		//
		if (validVolunteers.size() == 0) {
			return "";
		}

		//
		// if Round Robin Scheduling
		//
		if (aMethod == RR_VOLUNTEER) {

			//
			// Get category user with RR Volunteer set to true
			// as auto-assignee and set next Category user in the list to be
			// next RR volunteer
			//
			int nextRRIndex = 0;
			TypeUser rrVolunteer = null;

			for (int i = 0; i < validVolunteers.size(); i++) {
				rrVolunteer = validVolunteers.get(i);

				if (rrVolunteer.getRRVolunteer() == true) {
					nextRRIndex = ((i == (validVolunteers.size() - 1)) ? 0
							: i + 1);

					break;
				}
			}

			//
			// If no RR volunteer set by default, take the last as
			// auto-assignee and first as next RR volunteer
			//
			return rrVolunteer.getUser().getUserLogin() + ","
					+ validVolunteers.get(nextRRIndex).getUser().getUserLogin();
		}

		//
		// else if Random Scheduling
		//
		else if (aMethod == RANDOM_VOLUNTEER) {

			//
			// generate a random index to pick from volunteers list
			//
			int randomNum = (int) Math.floor(Math.random()
					* validVolunteers.size());

			return validVolunteers.get(randomNum).getUser().getUserLogin();
		}

		return "";
	}

	/**
	 * This method returns true if the specified email is globally excluded for
	 * email communication from TBits System.
	 * 
	 * @param aEmail
	 *            Email to be checked.
	 * 
	 * @return true if this is gloablly excluded, false otherwise.
	 * 
	 * @exception DatabaseException
	 */
	public static boolean isBAEmail(String aEmail) throws DatabaseException {

		if ((aEmail == null) || (aEmail.trim().length() == 0))
			return false;

		boolean flag = false;
		ArrayList<User> baList = ExclusionList.lookupBySystemIdAndUserTypeId(0,
				-1);
		User user = User.lookupByEmail(aEmail);

		if (BusinessArea.lookupByEmail(aEmail) != null) {
			return true;
		} else if (BAMailAccount.lookupByEmail(aEmail) != null) {
			return true;
		} else if ((user != null) && (baList.contains(user) == true)) {
			return true;
		}

		return false;
	}

	/**
	 * This method returns true if the specified user in on vacation for the
	 * given business area.
	 * 
	 * @param aUser
	 * @param aSystemId
	 * 
	 * @return True if the user is on vacation. False otherwise.
	 * 
	 * @throws DatabaseException
	 *             In case of any database related errors.
	 */
	public static boolean isOnVacation(User aUser, int aSystemId)
			throws DatabaseException {
		if (aUser == null) {
			return false;
		}

		if (aUser.getIsOnVacation() == true) {
			return true;
		}

		WebConfig userConfig = aUser.getWebConfigObject();

		if (userConfig == null) {
			return false;
		}

		BusinessArea ba = BusinessArea.lookupBySystemId(aSystemId);
		BAConfig config = userConfig.getBAConfig(ba.getSystemPrefix());

		if (config == null) {
			return false;
		}

		if (config.getVacation() == true) {
			return true;
		}

		return false;
	}

	/**
	 * This method checks if the parent is a transferred request.
	 * 
	 * @return True if request is already transferred.
	 */
	public static boolean isTransferred(String sysPrefix, int requestId)
			throws DatabaseException {
		boolean flag = false;
		TransferredRequest tr = TransferredRequest
				.lookupBySourcePrefixAndRequestId(sysPrefix, requestId);

		if (tr != null) {
			flag = true;
		} else {
			flag = false;
		}

		return flag;
	}

	public static boolean compareFieldValueAndRuleValue(int dataType,
			String fieldName, String fieldValue, String ruleValue,
			Operator operator) {
		boolean flag = false;
		// Field field = fieldsHashTable.get(fieldName);

		// if (field == null) {
		// return false;
		// }

		switch (dataType) {
		case DataType.BOOLEAN:
			flag = compareBoolean(operator, fieldValue, ruleValue);

			break;

		case DataType.TIME:
		case DataType.DATE:
		case DataType.DATETIME:
			flag = compareDate(operator, fieldValue, ruleValue);

			break;

		case DataType.INT:
			flag = compareInteger(operator, fieldValue, ruleValue);

			break;

		case DataType.REAL:
			flag = compareReal(operator, fieldValue, ruleValue);

			break;

		case DataType.STRING:
			flag = compareString(operator, fieldValue, ruleValue);

			break;

		case DataType.TEXT:
			flag = compareString(operator, fieldValue, ruleValue);

			break;

		case DataType.TYPE:
			flag = compareString(operator, fieldValue, ruleValue);

			break;

		case DataType.USERTYPE:
			flag = compareMultiValue(operator, fieldValue, ruleValue);

			break;
		}

		return flag;
	}

	/**
	 * This method compares the following values of a field using the specified
	 * operator. - Value of the field in the request. - Value of the field
	 * specified in the rule.
	 * 
	 * @param fieldsHashTable
	 *            The table containing the fields definition.
	 * @param fieldName
	 *            Name of the field.
	 * @param ruleValue
	 *            Value specified by the rule.
	 * @param operator
	 *            Operator to be applied.
	 * 
	 * @return true if the comparison is successful.
	 * 
	 */
	public static boolean compareFieldValueAndRuleValue(
			Hashtable<String, Field> fieldsHashTable, String fieldName,
			String fieldValue, String ruleValue, Operator operator) {
		Field field = fieldsHashTable.get(fieldName);
		int dataType = field.getDataTypeId();
		return compareFieldValueAndRuleValue(dataType, fieldName, fieldValue,
				ruleValue, operator);
	}

	/**
	 * This method modifies the current value of a field using the specified
	 * operator.
	 * 
	 * @param fieldName
	 *            Name of the field.
	 * @param value
	 *            Value specified by the rule.
	 * @param operator
	 *            Operator to be applied.
	 * @param myFieldTable
	 *            A hashmap fieldName-Field of all the field of a BA
	 * @param myExtendedFields
	 * @param myRequest
	 * 
	 */
	public static void modify(String fieldName, String value,
			Operator operator, Hashtable<String, Field> myFieldTable,
			Hashtable<Field, RequestEx> myExtendedFields, Request myRequest) {
		Field field = myFieldTable.get(fieldName);

		if (field == null) {
			return;
		}

		RequestEx reqEx = null;

		if (field.getIsExtended() == true) {
			reqEx = myExtendedFields.get(field);
			if (reqEx == null) {
				return;
			}
		}

		int dataType = field.getDataTypeId();

		LOG.info(fieldName + ", " + value + ", " + operator + ", " + dataType);

		switch (dataType) {
		case BOOLEAN:
			APIUtil.modifyBoolean(myRequest, reqEx, field, value, operator);
			break;

		case TIME:
		case DATE:
		case DATETIME:
			APIUtil.modifyDate(myRequest, reqEx, field, value, operator);

			break;

		case INT:
			APIUtil.modifyInteger(myRequest, reqEx, field, value, operator);

			break;

		case REAL:
			APIUtil.modifyReal(myRequest, reqEx, field, value, operator);

			break;

		case STRING:
			APIUtil.modifyString(myRequest, reqEx, field, value, operator);

			break;

		case TEXT:
			APIUtil.modifyText(myRequest, reqEx, field, value, operator);

			break;

		case TYPE:
			APIUtil.modifyType(myRequest, reqEx, field, value, operator);

			break;

		case USERTYPE:
			APIUtil.modifyUserType(myRequest, field, value, operator);

			break;
		}

		if (reqEx != null) {
			myExtendedFields.put(field, reqEx);
		}
	}

	public static int getAndCreateRequestFileId(int systemId, int requestId)
			throws DatabaseException {
		// TODO Auto-generated method stub
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			CallableStatement cs = connection
					.prepareCall("stp_request_getAndIncrRequestFileMaxrId ?, ?");
			cs.setInt(1, systemId);
			cs.setInt(2, requestId);

			ResultSet rs = cs.executeQuery();
			if (rs.next()) {
				int n = rs.getInt(1);
				rs.close();
				cs.close();
				return n;
			} else {
				throw new DatabaseException("Unable to get the id.", null);
			}
		} catch (SQLException sqle) {
			throw new DatabaseException("Unable to get the id.", sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					// TODO: handle exception
					LOG.warn("Unable to close the connection while getting and incrementing the requestid");
					e.printStackTrace();
				}
			}
		}
	}

	public static ArrayList<FileAction> getAttachmentDiff(int fieldId,
			Collection<AttachmentInfo> newAttachments,
			Collection<AttachmentInfo> aOldAttachments) {
		// First, copy because we are going to manipulate.
		Collection<AttachmentInfo> oldAttachments = new ArrayList<AttachmentInfo>();
		oldAttachments.addAll(aOldAttachments);

		ArrayList<FileAction> fileActions = new ArrayList<FileAction>();
		// Locate the added
		for (AttachmentInfo ai : newAttachments) {
			// Check whether it was modified or removed
			boolean isFound = false;
			AttachmentInfo toBeRemoved = null;
			for (AttachmentInfo oi : oldAttachments) {
				if ((oi.requestFileId == ai.requestFileId)) {
					if (oi.repoFileId != ai.repoFileId) {
						fileActions.add(new FileAction(ai,
								WebdavConstants.FILE_MODIFIED, fieldId));
					}
					// Condition for renaming of file
					else if (!oi.name.equals(ai.name)) {
						fileActions.add(new FileAction(oi,
								WebdavConstants.FILE_DELETED, fieldId));
						fileActions.add(new FileAction(ai,
								WebdavConstants.FILE_ADDED, fieldId));
					}
					toBeRemoved = oi;
					break;
				}
			}
			if (toBeRemoved != null) {
				oldAttachments.remove(toBeRemoved);
			} else {
				fileActions.add(new FileAction(ai, WebdavConstants.FILE_ADDED,
						fieldId));
			}
		}
		for (AttachmentInfo oi : oldAttachments) {
			fileActions.add(new FileAction(oi, WebdavConstants.FILE_DELETED,
					fieldId));
		}
		return fileActions;
	}

	/**
	 * This method can be used to clone the Collection for one level of objects
	 * which are not container type Example it can do deep clone of
	 * Collection<A>, or Collection<RequestUser> but for objects like
	 * Collection<ArrayList<RequestUser>> this will not do a deep clone. for
	 * such Collections you can create methods which uses this method multiple
	 * times for each Collection
	 * 
	 * @param src
	 *            : source Collection cannot be null
	 * @param dest
	 *            : destination Collection cannot be null
	 * @throws TBitsException
	 *             : in case where src or dest is null or clone is not
	 *             implemented in the Collection objects or other problem.
	 */
	public static void cloneCollection(Collection src, Collection dest)
			throws TBitsException {
		if (src == null)
			throw new TBitsException("The src Collection cannot be null.");
		if (dest == null)
			throw new TBitsException("The dest Collection cannot be null.");
		try {
			dest.clear();
			for (Object o : src) {
				Class klass = o.getClass();
				Method meth = klass.getMethod("clone", null);
				Object dupl = meth.invoke(o, null);
				dest.add(dupl);
			}
		} catch (Exception e) {
			LOG.info("",(e));
			throw new TBitsException(
					"Cannot generate Clone because of previous exception.");
		}
	}

	/**
	 * This method can be used to clone the Hashtable for one level of objects
	 * which are not container type, i.e. both key and value cannot be container
	 * type Example it can do deep clone of Hashtable<Integer,Field>, or
	 * Collection<String,Field> but for objects like
	 * Hashtable<Integer,ArrayList<RequestUser>> this will NOT do a deep
	 * clone.As it will call the ArrayList.clone() for the value objects for
	 * such Hashtable you can create methods which uses this method and
	 * cloneCollection() for each Collection values.
	 * 
	 * @param src
	 *            : source Hashtable cannot be null
	 * @param dest
	 *            : destination Hashtable cannot be null
	 * @throws TBitsException
	 *             : in case where src or dest is null or clone is not
	 *             implemented in the Hashtable key, values .. or other problem.
	 */
	public static void cloneHashtable(Hashtable src, Hashtable dest)
			throws TBitsException {
		if (src == null)
			throw new TBitsException("The src Hashtable cannot be null.");
		if (dest == null)
			throw new TBitsException("The dest Hashtable cannot be null.");

		try {
			dest.clear();

			for (Enumeration allKeys = src.keys(); allKeys.hasMoreElements();) {
				Object key = allKeys.nextElement();
				Class keyClass = key.getClass();
				Method keyCloneMethod = keyClass.getMethod("clone", null);
				Object dupKey = keyCloneMethod.invoke(key, null);

				Object value = src.get(key);
				Class valueClass = value.getClass();
				Method valueCloneMethod = valueClass.getMethod("clone", null);
				Object dupValue = valueCloneMethod.invoke(value, null);

				dest.put(dupKey, dupValue);
			}
		} catch (Exception e) {
			LOG.info("",(e));
			throw new TBitsException(
					"Cannot generate Clone because of previous exception.");
		}

	}

	public static int getMaxId(String key) throws SQLException {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			CallableStatement stmt = conn.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, key);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	/**
	 */
	public static HashSet<User> resolveMailList(User aUser) throws Exception {
		if (aUser == null) {
			return null;
		}

		HashSet<User> finalUsersSet = new HashSet<User>();
		HashSet<User> iterationSet = new HashSet<User>();
		HashSet<User> alreadyIteratedMailingLists = new HashSet<User>();

		if (aUser.getUserTypeId() == UserType.INTERNAL_MAILINGLIST) {
			iterationSet.add(aUser);
			TBitsMailer.LOG.info("Trying to find resolved users for : "
					+ aUser.getUserLogin());
			while (iterationSet.isEmpty() == false) {
				Iterator<User> mailListIterator = iterationSet.iterator();

				// get iteration element.
				User mailList = mailListIterator.next();

				// dereference this mailing list
				ArrayList<User> resolvedList = User.getMailListUsers(mailList
						.getUserId());
				HashSet<User> resolvedSet = new HashSet<User>();
				if (resolvedList != null)
					resolvedSet.addAll(resolvedList);

				// add user to iteration set or to the finalUserSet
				for (User user : resolvedSet) {
					if (user == null)
						continue;

					if (user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST) {
						if (!alreadyIteratedMailingLists.contains(user))
							iterationSet.add(user);
					} else {
						finalUsersSet.add(user);
					}
				}

				// remove this iteration element
				iterationSet.remove(mailList);

				// add into already iterated set
				alreadyIteratedMailingLists.add(mailList);
			}

			return finalUsersSet;
		} else {
			return null;
		}
	}

	public static void performRegExpChecksOnFieldValues(Request myRequest)
			throws TBitsException {

		ArrayList<Field> fields = null;
		try {
			fields = Field.lookupBySystemId(myRequest.getSystemId());
			for (Field field : fields) {
				String value = myRequest.get(field.getName());
			
				if(DataType.TEXT == field.getDataTypeId())
				{
					if(value != null)
					{
					value = value.replaceAll("\\<.*?>","");
				    value = value.replaceAll("\n", "");
					}
				}
				String regEx = field.getRegex();

				String error = "";
				if ((regEx != null) && (regEx.trim().length() > 0)) {
                   if (value == null) {
                	   error = field.getError();
						if ((error != null) && (error.trim().length() > 0)) {
							throw new TBitsException(" '" + error + "' ");
						} else {
							throw new TBitsException("The value of the field '"
									+ field.getDisplayName()
									+ "' did not match the regex '" + regEx
									+ "'");

						}
					}
               	//Handling attachments
					int dataType = field.getDataTypeId();
					if (DataType.ATTACHMENTS == dataType) {
						if (value.equalsIgnoreCase("[]")) {
							error = field.getError();
							if ((error != null) && (error.trim().length() > 0)) {
								throw new TBitsException(" '" + error + "' ");
							} else {
								throw new TBitsException(
										"The value of the field '"
												+ field.getDisplayName()
												+ "' cannot be empty. ");
							}
						}
					}
					
					if ((!value.trim().matches(regEx))) {
						error = field.getError();
						if ((error != null) && (error.trim().length() > 0)) {
							throw new TBitsException(" '" + error + "' ");
						} else {
							throw new TBitsException("The value of the field '"
									+ field.getDisplayName()
									+ "' did not match the regex '" + regEx
									+ "'");

						}
					}
				
				
				/*	if (value.equalsIgnoreCase("")) {
						throw new TBitsException("The value of the field '"
								+ field.getDisplayName()
								+ "' cannot be empty. ");
					}*/

				}

			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/*
	  public static void performRegExChecksOnFieldValues( Hashtable<String,
	  String> paramTable, Hashtable<String, Field> fieldsTable, boolean
	  isAdRequest, User user) throws TBitsException { try { Field fieldSysId =
	  fieldsTable.get("sys_id");
	  
	  Hashtable<String, Integer> myPermTable = RolePermission
	  .getPermissionsBySystemIdAndUserId( fieldSysId.getSystemId(),
	  user.getUserId());
	  
	  for (String fieldName : paramTable.keySet()) { if
	  (fieldsTable.containsKey(fieldName)) { Field field =
	  fieldsTable.get(fieldName); String regEx = field.getRegex(); if ((regEx
	  != null) && (regEx.trim().length() > 0)) { String value =
	  paramTable.get(fieldName); if (value != null) { if
	 (!value.trim().matches(regEx)) { String error = field.getError(); if
	  ((error != null) && (error.trim().length() > 0)) { throw new
	  TBitsException(" '" + error + "' "); } else { throw new TBitsException(
	  "The value of the field '" + field.getDisplayName() +
	  "' did not match the regex '" + regEx + "'"); } }
	  
	  }
	  
	  } } } if (isAddRequest) { for (Field field : fieldsTable.values()) {
	  
	  int perm = myPermTable.get(field.getName()); if ((((perm &
	  Permission.ADD) & (field.getPermission() & Permission.ADD)) != 0)) {
	  String regEx = field.getRegex(); if ((regEx != null) &&
	  (regEx.trim().length() > 0)) { String fieldName = field.getName();
	  
	  if (!paramTable.containsKey(fieldName)) { String error =
	  field.getError(); if ((error != null) && (error.trim().length() > 0)) {
	  throw new TBitsException(" '" + error + "' "); } else { throw new
	  TBitsException( "The value of the field '" + field.getDisplayName() +
	  "' did not match the regex '" + regEx + "'"); } } else { int dataType =
	  field.getDataTypeId(); if (DataType.ATTACHMENTS == dataType) { String
	  value = paramTable.get(fieldName); if (value.equalsIgnoreCase("[]")) {
	  String error = field.getError(); if ((error != null) &&
	  (error.trim().length() > 0)) { throw new TBitsException(" '" + error +
	  "' "); } else { throw new TBitsException( "The value of the field '" +
	  field.getDisplayName() + "' did not match the regex '" + regEx + "'"); }
	  } } } } } } } else{ for (Field field : fieldsTable.values()) {
	  
	  int perm = myPermTable.get(field.getName()); if ((((perm &
	  Permission.CHANGE) & (field .getPermission() & Permission.CHANGE)) != 0))
	  { String regEx = field.getRegex(); if ((regEx != null) &&
	  (regEx.trim().length() > 0)) { String fieldName = field.getName();
	  
	  if (!paramTable.containsKey(fieldName)) { String error =
	  field.getError(); if ((error != null) && (error.trim().length() > 0)) {
	  throw new TBitsException(" '" + error + "' "); } else { throw new
	  TBitsException( "The value of the field '" + field.getDisplayName() +
	  "' did not match the regex '" + regEx + "'"); } } else { int dataType =
	  field.getDataTypeId(); if (DataType.ATTACHMENTS == dataType) { String
	  value = paramTable.get(fieldName); if (value.equalsIgnoreCase("[]")) {
	  String error = field.getError(); if ((error != null) &&
	  (error.trim().length() > 0)) { throw new TBitsException(" '" + error +
	  "' "); } else { throw new TBitsException( "The value of the field '" +
	  field.getDisplayName() + "' did not match the regex '" + regEx + "'"); }
	  } } } } } }
	  
	  } } catch (DatabaseException e) { // TODO Auto-generated catch block
	  e.printStackTrace(); }
	  
	  }
	 */

	public static void performUniquenessChecksOnFieldValues(
			Connection connection, Hashtable<String, Field> fieldsTable,
			boolean isUpdate, Request myRequest) throws TBitsException {
		// TODO Auto-generated method stub
		int sysId = myRequest.getSystemId();
		int requestId = myRequest.getRequestId();
		for (String fieldName : fieldsTable.keySet()) {

			Field field = fieldsTable.get(fieldName);
			boolean isRequestUnique = ((field.getPermission() & Permission.IS_REQUEST_UNIQUE) != 0);
			boolean isActionUnique = ((field.getPermission() & Permission.IS_ACTION_UNIQUE) != 0);

			String tableName;
			if (isActionUnique)
				tableName = "actions";
			else
				tableName = "requests";

			boolean isExtended = field.getIsExtended();

			if (isRequestUnique || isActionUnique) {
				String value = myRequest.get(fieldName);
				if ((value == null) || (value.length() == 0)) {
					LOG.info("The uniqueness is not validated for datatype id : "
							+ field.getDataTypeId()
							+ " on field '"
							+ field.getDisplayName()
							+ "' because the value is either null or empty.");
					continue;
				}
				boolean isUnique = false;
				String extColName = "";
				Object valueObject = null;
				switch (field.getDataTypeId()) {
				case DataType.INT:
					try {
						valueObject = new Integer(Integer.parseInt(value));
					} catch (NumberFormatException nfe) {
						throw new TBitsException("The value '" + value
								+ "' of field '" + field.getDisplayName()
								+ "' should integer value.");
					}
					extColName = "int_value";
					break;
				case DataType.REAL:
					try {
						valueObject = new Double(Double.parseDouble(value));
					} catch (NumberFormatException nfe) {
						throw new TBitsException("The value '" + value
								+ "' of field '" + field.getDisplayName()
								+ "' should decimal value.");
					}
					extColName = "real_value";
					break;
				case DataType.STRING:
					valueObject = value;
					extColName = "varchar_value";
					break;
				default:
					LOG.warn("The uniqueness is not validated for datatype id : "
							+ field.getDataTypeId()
							+ " on field '"
							+ field.getDisplayName() + "'.");
					return;
				}

				// if(isRequestUnique)
				// {
				if (isExtended)
					isUnique = isExtendedFieldValueUnique(connection, tableName
							+ "_ex", extColName, valueObject, field, isUpdate,
							isRequestUnique, sysId, requestId);
				else
					isUnique = isFixedFieldValueUnique(connection, tableName,
							field.getName(), valueObject, field, isUpdate,
							isRequestUnique, sysId, requestId);
				// }

				if (!isUnique) {
					throw new TBitsException("The value '" + value
							+ "' of field '" + field.getDisplayName()
							+ "' already exists. Please input a unique value.");
				}
			}

		}
	}

	public static boolean isFixedFieldValueUnique(Connection connection,
			String tableName, String name, Object valueObject, Field field,
			boolean isUpdate, boolean isRequestLevelUniqueness, int sysId,
			int requestId) throws TBitsException {
		try {
			String query = "select count(*) from " + tableName + " where "
					+ name + " = ? " + " and sys_id = " + sysId;
			if (isRequestLevelUniqueness && isUpdate)
				query += " and request_id != " + requestId;
			PreparedStatement ps = connection.prepareStatement(query);
			switch (field.getDataTypeId()) {
			case DataType.INT:
				ps.setInt(1, ((Integer) valueObject).intValue());
				break;
			case DataType.REAL:
				ps.setDouble(1, ((Double) valueObject).doubleValue());
				break;
			case DataType.STRING:
				ps.setString(1, (String) valueObject);
				break;
			default:
				LOG.warn("The uniqueness is not supported on field type '"
						+ field.getDataTypeId() + "' marked on '"
						+ field.getDisplayName() + "'. and sys_id '" + sysId
						+ "' ");
				return true;
			}

			ResultSet rs = ps.executeQuery();
			boolean retValue = true;
			if (rs.next()) {
				int numberOfRecords = rs.getInt(1);
				if (numberOfRecords > 0)
					retValue = false;
			}

			rs.close();
			ps.close();

			return retValue;

		} catch (Exception exp) {
			LOG.error("Error while checking uniqueness in fixed field value.",
					exp);
			throw new TBitsException("Unknown database error has occurred", exp);
		}
	}

	public static boolean isExtendedFieldValueUnique(Connection connection,
			String tableName, String colName, Object valueObject, Field field,
			boolean isUpdate, boolean isRequestLevelUniqueness, int sysId,
			int requestId) throws TBitsException {
		try {
			String query = "";

			PreparedStatement ps = null;
			switch (field.getDataTypeId()) {
			case DataType.INT:
				query = "select count(*) from " + tableName + " where "
						+ colName + " = ? " + " and sys_id = " + sysId
						+ " and field_id = " + field.getFieldId();
				if (isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;

				ps = connection.prepareStatement(query);
				ps.setInt(1, ((Integer) valueObject).intValue());
				break;

			case DataType.REAL:
				// rather than using abs(rvalue - value) < 0.0001, using this
				// approach so that table scan can be avoided
				query = "select count(*) from " + tableName + " where "
						+ colName + " < (? + 0.0001) and " + colName
						+ " > (? - 0.0001) " + " and sys_id = " + sysId
						+ " and field_id = " + field.getFieldId();
				if (isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;

				ps = connection.prepareStatement(query);
				ps.setDouble(1, ((Double) valueObject).doubleValue());
				ps.setDouble(2, ((Double) valueObject).doubleValue());

				break;
			case DataType.STRING:
				query = "select count(*) from " + tableName + " where "
						+ colName + " = ? " + " and sys_id = " + sysId
						+ " and field_id = " + field.getFieldId();
				if (isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;

				ps = connection.prepareStatement(query);
				ps.setString(1, (String) valueObject);
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported datatype for Uniqueness");
			}

			boolean retValue = true;

			if (ps != null) {
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					int numberOfRecords = rs.getInt(1);
					if (numberOfRecords > 0)
						retValue = false;
				}

				rs.close();
				ps.close();
			}
			return retValue;

		} catch (Exception exp) {
			LOG.error(
					"Error while checking uniqueness in extended field value.",
					exp);
			throw new TBitsException("Unknown database error has occurred", exp);
		}
	}

	public static User getFileLocker(Connection conn, int systemId,
			int requestId, int fieldId, int requestFileId) throws SQLException,
			DatabaseException {

		User locker = null;

		String query = "select owner from locks where sys_id=? and request_id=? and field_id=? and request_file_id=?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setInt(1, systemId);
		ps.setInt(2, requestId);
		ps.setInt(3, fieldId);
		ps.setInt(4, requestFileId);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			locker = User.lookupByUserLogin(rs.getString(1));
		}

		return locker;
	}

	public static void removeFileLock(Connection conn, int systemId,
			Integer requestId, int fieldId, int requestFileId)
			throws SQLException {
		String query = "delete from locks where sys_id=? and request_id=? and field_id=? and request_file_id=?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setInt(1, systemId);
		ps.setInt(2, requestId);
		ps.setInt(3, fieldId);
		ps.setInt(4, requestFileId);
		ps.executeUpdate();
	}

	public static Hashtable<Integer, FileRepoIndexObject> getFileRepoIndexObjectList(
			Collection<Integer> repoIdList) throws Exception {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			return getFileRepoIndexObjectList(conn, repoIdList);
		} catch (Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			if (null != conn && conn.isClosed() == false)
				conn.close();
		}

	}

	public static FileRepoIndexObject getFileRepoIndexObject(Connection conn,
			int repoId) throws Exception {
		String sql = "select * from file_repo_index where id = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, repoId);
		ResultSet result = ps.executeQuery();
		if (null != result && result.next() != false) {
			// you will always get single result as id is primary key
			int id;
			String location;
			String name;
			Date createDate;
			long size;
			String hash;
			int securityCode;

			id = result.getInt("id");
			location = result.getString("location");
			name = result.getString("name");
			createDate = result.getDate("create_date");
			size = result.getLong("size");
			hash = result.getString("hash");
			securityCode = result.getInt("security_code");

			return new FileRepoIndexObject(id, location, name, createDate,
					size, hash, securityCode);
		}

		return null;
	}

	public static FileRepoIndexObject getFileRepoIndexObject(int repoId)
			throws Exception {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			FileRepoIndexObject frio = getFileRepoIndexObject(conn, repoId);
			return frio;
		} catch (Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			if (null != conn && conn.isClosed() == false)
				conn.close();
		}

	}

	public static String toCommaSeparatedList(Collection col) {
		if (null == col)
			return null;
		StringBuffer sb = new StringBuffer();
		Iterator iter = col.iterator();

		if (iter.hasNext()) {
			sb.append(iter.next() + "");
		}

		for (; iter.hasNext();) {
			sb.append("," + iter.next());
		}

		return sb.toString();
	}

	public static Hashtable<Integer, FileRepoIndexObject> getFileRepoIndexObjectList(
			Connection conn, Collection<Integer> repoIdList) throws Exception {
		Hashtable<Integer, FileRepoIndexObject> map = new Hashtable<Integer, FileRepoIndexObject>();

		if (repoIdList == null || repoIdList.size() == 0)
			return null;

		// Array array = repoIdList.toArray();
		// String repoIds = "";
		String sql = "select * from file_repo_index where id in ("
				+ toCommaSeparatedList(repoIdList) + ")";
		Statement ps = conn.createStatement();

		ResultSet result = ps.executeQuery(sql);
		if (null != result) {
			while (result.next()) {
				int id;
				String location;
				String name;
				Date createDate;
				long size;
				String hash;
				int securityCode;

				id = result.getInt("id");
				location = result.getString("location");
				name = result.getString("name");
				createDate = result.getDate("create_date");
				size = result.getLong("size");
				hash = result.getString("hash");
				securityCode = result.getInt("security_code");

				map.put(id, new FileRepoIndexObject(id, location, name,
						createDate, size, hash, securityCode));
			}

			return map;
		}

		return null;
	}

}
