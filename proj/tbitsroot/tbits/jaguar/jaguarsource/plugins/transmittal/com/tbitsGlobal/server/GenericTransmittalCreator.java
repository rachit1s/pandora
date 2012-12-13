package transmittal.com.tbitsGlobal.server;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;

import com.google.gson.JsonArray;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.webapps.WebUtil;

public class GenericTransmittalCreator {

	private static final String NOTIFY = "notify";

	private static final String ACTUAL_NUMBER = "actualNumber";

	private static final String ACTUAL_DATE = "actualDate";

	private static final String ACCESS_TO = "AccessTo";

	private static final String APPROVED = "Approved";

	private static final String SUBMITTED_FOR_APPROVAL = "SubmittedForApproval";

	static final String TRN_APPROVAL_CYCLE_TRANSIENT_DATA_ATTACHMENTS = "trn_approval_cycle_transient_data_attachments";

	protected static final String IS_APPROVAL_CYCLE = "isApprovalCycle";

	private static final String UNIVERSAL_ID = "universal_id";

	public static final String KIND_ATTENTION = "kindAttention";

	static final String REQUEST_LIST = "requestList";

	private static final String SELECTED_ATTACHMENTS_TABLE = "SelectedAttachmentsTable";

	public static final String TRN_PROCESS_ID_PARAM = "trnProcessId";

	private static final String DISTRIBUTION_TABLE = "distributionTable";

	private static final String DTN_FIELD_ID = "dtnFieldId";

	private static final String DTN_LOGGER = "dtnLogger";

	private static final String IST = "IST";

	private static final String DELIVERABLE_FIELD_NAME = "deliverableFieldName";

	private static final String UNIQUE_TRANSMITTAL_NUMBER_FIELD_ID = "uniqueTransmittalNumberFieldId";

	public static final String TRANSMITTAL_DATE = "transmittalDate";

	private static final String DRAWING_TABLE = "drawingTable";

	private static final String IS_SUPERCEDE_DELIVERABLES = "isSupercedeDeliverables";

	private static final String TRANSMITTAL_TEMPLATE_NAME = "transmittal_template_name";

	private static final String RECIPIENT = "recipient";

	private static final String ORIGINATOR = "originator";

	public static final String IS_IMPORT_PROCESS = "isImportProcess";

	private static final String DTN_STATUS_TRANSMITTAL_COMPLETE = "TransmittalComplete";

	// Tag-replace strings in document selection window.
	private static final String EMAIL_BODY = "emailBody";
	private static final String TRANSMITTAL_SUBJECT = "transmittalSubject";

	// Miscellaneous
	private static final String KEYWORD_TRUE = "true";

	private static final String KEYWORD_FALSE = "false";

	private static final String TO_LIST = "toList";

	private static final String CC_LIST = "ccList";

	// Column names of db tables
	private static final String TARGET_SYS_ID = "target_sys_id";

	// Field names in transmittal business area
	private static final String DTN_NUMBER = "DTNNumber";

	private static final String DTN_NOTE_FIELD_ID = "DTNNoteFieldId";

	private static final String DTN_DEL_FIELD_ID_PROVIDED_IS_INVALID_MSG = "Field with id was not found or "
			+ "field id provided is invalid, provided in parameter: ";

	private static final String TRANSMITTED_FILE_FIELD_ID = "TransmittedFileFieldId";

	private static final String DTN_NO_DEL_FIELD_ID_PROVIDED_MSG = "No parameter was specified for Transmittal files field id or invalid"
			+ "field id provided in parameter: ";

	User user = null;

	private HttpServletRequest httpRequest;

	// Parameters required by the transmittal process.
	// private int deliverableFieldId;

	// LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	private static final String USER = "user";

	public static final String PDF = ".pdf";

	public static final String DOC = ".doc";

	public GenericTransmittalCreator(HttpServletRequest request) {
		this.httpRequest = request;
	}

	ArrayList<String[]> final_arr;

	public GenericTransmittalCreator() {
		final_arr = null;
	}

	protected String createTransmittal(
			HashMap<String, String> trnProcessParamTable, // method modifying
			// dtn 1 in sequence
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments)
			throws TBitsException, DatabaseException, APIException {
		BusinessArea dcrBA = null;

		int dcrSystemId = 0;
		int transmittalProcessId = 0;

		if (httpRequest != null)
			user = WebUtil.validateUser(httpRequest);
		// String userLogin = trnProcessParamTable.get(USER);
		// if (userLogin != null)
		// user = User.lookupByUserLogin(userLogin);

		String dcrRequestListStr = trnProcessParamTable.get(REQUEST_LIST);
		String[] dcrRequestList = null;
		if (dcrRequestListStr != null)
			dcrRequestList = dcrRequestListStr.split(",");
		else
			throw new TBitsException(
					"No requests selected for transmittal creation.");

		if (trnProcessParamTable.get(TRN_PROCESS_ID_PARAM) != null)
			transmittalProcessId = Integer.parseInt(trnProcessParamTable
					.get(TRN_PROCESS_ID_PARAM));

		TransmittalProcess selectedTransmittalProcess = null;
		try {
			selectedTransmittalProcess = TransmittalProcess
					.lookupTransmittalProcessByTransmittalProcessId(transmittalProcessId);
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw new DatabaseException(e2.getMessage(), e2);
		}

		if (selectedTransmittalProcess == null) {
			LOG
					.info("Invalid transmittal process type/no process exists for the  transmittal process id: "
							+ transmittalProcessId);
			throw new TBitsException(
					"Invalid transmittal process type/no process exists for the  transmittal process id: "
							+ transmittalProcessId);
		}

		dcrSystemId = selectedTransmittalProcess.getSystemId();
		dcrBA = BusinessArea.lookupBySystemId(dcrSystemId);

		int dtnSystemId = selectedTransmittalProcess.getDtnSysId();
		BusinessArea dtnBA = BusinessArea.lookupBySystemId(dtnSystemId);

		if (dtnBA == null) {
			LOG
					.error(dtnSystemId
							+ ": Invalid transmittal Business Area Id or business area does not exist");
			throw new TBitsException(
					dtnSystemId
							+ ": Invalid transmittal Business Area Id or business area does not exist");
		}

		String transmittalDate = trnProcessParamTable
				.get(TransmittalUtils.ACTUAL_DATE);
		if ((transmittalDate == null) || transmittalDate.trim().equals("")) {
			transmittalDate = "";
		}
		try {
			trnProcessParamTable.put(TRANSMITTAL_DATE,
					getLoggedDate(transmittalDate));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		String dtnTemplateDate = trnProcessParamTable
				.get(TransmittalUtils.DTN_TEMPLATE_DATE);
		if (dtnTemplateDate == null)
			dtnTemplateDate = TransmittalUtils
					.getDTNTemplateDate(transmittalDate);
		trnProcessParamTable.put(TransmittalUtils.ACTUAL_DATE, dtnTemplateDate);

		String transmittalNumber = "";
		String actualNumber = trnProcessParamTable.get(ACTUAL_NUMBER);
		if ((actualNumber != null) && (!actualNumber.trim().equals("")))
			transmittalNumber = actualNumber;

		Connection maxIdConn = null;
		TBitsResourceManager tBitsResMgr = new TBitsResourceManager();

		Connection connection = null;
		String dtnNumber = "";
		String contextPath = "";
		if (this.httpRequest != null)
			contextPath = this.httpRequest.getContextPath(); // GlobalConstants.CONTEXT_PATH;
		try {
			int transmittalMaxId = 0;

			// Create Connection.
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			String formattedTrnReqId = TransmittalUtils.EMPTY_STRING;

			if ((transmittalNumber == null)
					|| transmittalNumber.trim().equals("")) {
				String dtnSerialKey = selectedTransmittalProcess
						.getTrnMaxSnKey();
				boolean isApprovalCycle = Boolean.valueOf(trnProcessParamTable
						.get(IS_APPROVAL_CYCLE));
				if (!isApprovalCycle) {
					transmittalMaxId = TransmittalProcess
							.getMaxTransmittalNumber(connection, dtnBA
									.getSystemId(), dtnSerialKey);
					formattedTrnReqId = TransmittalUtils
							.getFormattedStringFromNumber(transmittalMaxId);
				} else {
					// Incrementing manually as it is a likely number for
					// approval cycle.
					transmittalMaxId = TransmittalUtils
							.getTransmittalMaxId(dtnSerialKey) + 1;
					formattedTrnReqId = TransmittalUtils
							.getFormattedStringFromNumber(transmittalMaxId)
							+ " {Likely}";
				}

				// Now append the formatted running number with the
				// transmittal_id_prefix.
				dtnNumber = trnProcessParamTable
						.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
				if (dtnNumber == null)
					dtnNumber = "";

				// When the documents are being approved from DTNApp BA instead
				// of directly from transmittal wizard,
				// then the transmittal process parameters are picked from the
				// approval transient data table and
				// transmittal_id_prefix will have likely number in it and hence
				// has to be removed before inserting
				// the running number.
				if (dtnNumber.contains("{Likely}")) {
					int lIndex = dtnNumber.lastIndexOf("-");
					if (lIndex > 0) {
						dtnNumber = dtnNumber.substring(0, (lIndex + 1));
					}
				}

				dtnNumber = dtnNumber + formattedTrnReqId;
			} else
				dtnNumber = transmittalNumber;

			trnProcessParamTable.put(TransmittalUtils.TRANSMITTAL_ID_PREFIX,
					dtnNumber);

			formattedTrnReqId = createTransmittal(connection, contextPath,
					tBitsResMgr, trnProcessParamTable, dcrBA,
					selectedTransmittalProcess, dtnBA, dcrRequestList,
					allAttachments);

			LOG
					.info("%%%%%%%%%%%%%%%%%%%%%%%%Finished updating all BA's&&&&&&&&&&&&&&&&&&&");

			connection.commit();
			tBitsResMgr.commit();
			return formattedTrnReqId;
		} catch (NumberFormatException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			LOG.error("Error occurred while creating transmittal: \n"
					+ e1.getMessage(), e1);
			throw new TBitsException(
					"Error occurred while creating transmittal: \n"
							+ e1.getMessage(), e1);
		} catch (FileNotFoundException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			LOG.error(
					"File not found exception occurred during tranmisttal process: "
							+ e1.getMessage(), e1);
			throw new TBitsException(
					"File not found exception occurred during tranmisttal process: "
							+ e1.getMessage(), e1);
		} catch (DatabaseException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			throw new TBitsException(e1.getDescription(), e1);
		} catch (TBitsException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			LOG.error(e1);
			throw e1;
		} catch (IOException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			LOG.error(e1);
			throw new TBitsException(e1);
		} catch (APIException e1) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			e1.printStackTrace();
			LOG.error(e1);
			throw e1;
		} catch (SQLException e) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database"));
			LOG.error(apie);
			throw apie;
		} catch (ParseException e) {
			e.printStackTrace();
			LOG.error(e);
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			throw new TBitsException(
					"Error occurred while creating transmittal: \n"
							+ e.getMessage(), e);
		} finally {
			try {
				if ((connection != null) && (!connection.isClosed()))
					connection.close();
				if ((maxIdConn != null) && (!maxIdConn.isClosed()))
					maxIdConn.close();
			} catch (SQLException e) {
				LOG.error("Unable to close the connection to the database.", e);
				throw new TBitsException(
						"Unable to get connection to the database: \n"
								+ e.getMessage(), e);
			}
		}
	}

	/*
	 * user=root, requestList=1,2, trnProcessId=57,
	 * transmittal_template_name=dtn_template_non_lnt_for_comments.rptdesign,
	 * drawingTable
	 * =[["1","sdadas","adsad","P0","I","SC","5","-"],["2","new","asdsad"
	 * ,"P0","I","SC","5","-"]],
	 * distributionTable=[[null,"V. Lekshmanan","","vl@localhost.com"
	 * ,"SC","11"]], SelectedAttachmentsTable=[["0",""],["0",""]],
	 * transmittalDate="",
	 */
	/**
	 * Entry point for creating transmittal by calling the api
	 */
	public String createTransmittal(
			Connection connection,
			TBitsResourceManager tBitsResMgr,
			HashMap<String, String> trnProcessParamTable,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments)
			throws TBitsException, DatabaseException, APIException {

		BusinessArea dcrBA = null;
		int dcrSystemId = 0;
		int transmittalProcessId = 0;

		boolean shouldGenerateDtnNumber;
		String dtno = trnProcessParamTable.get("transmittal_id_prefix");
		if (dtno == null) {
			shouldGenerateDtnNumber = true;
		} else {
			shouldGenerateDtnNumber = false;
		}

		if (trnProcessParamTable.get(TRN_PROCESS_ID_PARAM) != null)
			transmittalProcessId = Integer.parseInt(trnProcessParamTable
					.get(TRN_PROCESS_ID_PARAM));
		else
			throw new TBitsException(
					"Transmittal process id missing and hence cannot continue.");

		LOG
				.info("*************************************************************************");
		LOG.info("Transmittal starting for transmittal process id: "
				+ transmittalProcessId);

		String userLogin = trnProcessParamTable.get(USER);
		if (userLogin != null)
			user = User.lookupByUserLogin(userLogin);
		else {
			LOG
					.warn("Current user's \"user_login\" not provided. Hence assuming \"root\" for transmittal process id: "
							+ transmittalProcessId);
			user = User.lookupByUserLogin("root");
		}

		String dcrRequestListStr = trnProcessParamTable.get(REQUEST_LIST);
		String[] dcrRequestList = null;
		if (dcrRequestListStr != null)
			dcrRequestList = dcrRequestListStr.split(",");
		else
			throw new TBitsException(
					"No requests selected for transmittal creation.");

		TransmittalProcess selectedTransmittalProcess = null;
		try {
			selectedTransmittalProcess = TransmittalProcess
					.lookupTransmittalProcessByTransmittalProcessId(transmittalProcessId);
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw new DatabaseException(e2.getMessage(), e2);
		}

		if (selectedTransmittalProcess == null) {
			LOG
					.info("Invalid transmittal process id or no process exists for the transmittal process id: "
							+ transmittalProcessId);
			throw new TBitsException(
					"Invalid transmittal process type/no process exists for the transmittal process id: "
							+ transmittalProcessId);
		}

		dcrSystemId = selectedTransmittalProcess.getSystemId();
		dcrBA = BusinessArea.lookupBySystemId(dcrSystemId);

		int dtnSystemId = selectedTransmittalProcess.getDtnSysId();
		BusinessArea dtnBA = BusinessArea.lookupBySystemId(dtnSystemId);

		if (dtnBA == null) {
			LOG
					.error(dtnSystemId
							+ ": Invalid transmittal Business Area Id or business area does not exist");
			throw new TBitsException(
					dtnSystemId
							+ ": Invalid transmittal Business Area Id or business area does not exist");
		}

		try {
			Hashtable<String, String> tpp = TransmittalProcess
					.getTransmittalProcessParameters(connection, dcrSystemId,
							selectedTransmittalProcess.getTrnProcessId());
			if (tpp != null) {
				for (String key : tpp.keySet()) {
					if (trnProcessParamTable.get(key) == null)
						trnProcessParamTable.put(key, tpp.get(key));
				}
			} else {
				LOG
						.error("Transmittal process parameters not found. Hence cannot continue.");
				throw new TBitsException(
						"Transmittal process parameters not found. Hence cannot continue.");
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw new DatabaseException(
					"Error occurred while fetching transmittal process parameters",
					e2);
		}

		String transmittalDate = trnProcessParamTable.get(TRANSMITTAL_DATE);
		if (transmittalDate == null) {
			trnProcessParamTable.put(TRANSMITTAL_DATE, "");
		}
		try {
			trnProcessParamTable.put(TRANSMITTAL_DATE,
					getLoggedDate(transmittalDate));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		String dtnTemplateDate = trnProcessParamTable
				.get(TransmittalUtils.DTN_TEMPLATE_DATE);
		if (dtnTemplateDate == null)
			dtnTemplateDate = TransmittalUtils
					.getDTNTemplateDate(transmittalDate);
		trnProcessParamTable.put(TransmittalUtils.ACTUAL_DATE, dtnTemplateDate);

		String transmittalNumber = "";

		transmittalNumber = trnProcessParamTable
				.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);

		String dtnNumber = "";
		String contextPath = "";// GlobalConstants.CONTEXT_PATH;
		if (this.httpRequest != null)
			contextPath = httpRequest.getContextPath();

		String dtnLogger = trnProcessParamTable.get(DTN_LOGGER);
		if ((dtnLogger == null) || (dtnLogger.trim().equals(""))) {
			if (user != null)
				dtnLogger = user.getUserLogin();
			else
				throw new TBitsException(
						"Could not find, \"dtnLogger\", could not also set \"root\" as a user in tBits, "
								+ "hence cannot continue.");
		}

		/*
		 * transmittal_template_name=dtn_template_non_lnt_for_comments.rptdesign,
		 * drawingTable
		 * =[["1","sdadas","adsad","P0","I","SC","5","-"],["2","new",
		 * "asdsad","P0","I","SC","5","-"]],
		 * distributionTable=[[null,"V. Lekshmanan"
		 * ,"","vl@localhost.com","SC","11"]],
		 * SelectedAttachmentsTable=[["0",""],["0",""]],
		 */

		String templateName = trnProcessParamTable
				.get("transmittal_template_name");
		if (templateName == null)
			throw new TBitsException("DTN birt template name not provided.");

		ArrayList<Request> requestsList = new ArrayList<Request>();
		for (String dcrRequestIdStr : dcrRequestList) {
			if ((dcrRequestIdStr != null)
					&& (!dcrRequestIdStr.trim().equals(""))) {
				int dcrReqId = Integer.parseInt(dcrRequestIdStr);
				Request dcrRequest = Request.lookupBySystemIdAndRequestId(
						connection, dcrSystemId, dcrReqId);
				if (dcrRequest != null)
					requestsList.add(dcrRequest);
			}
		}

		ArrayList<TbitsModelData> attachmentSelectionTableColumns = null;
		String drawingTable = trnProcessParamTable.get(DRAWING_TABLE);
		if (drawingTable == null) {
			try {
				attachmentSelectionTableColumns = TransmittalServiceImpl
						.getAttachmentSelectionTableColumns(connection,
								transmittalProcessId);
				Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
					public int compare(TbitsModelData o1, TbitsModelData o2) {
						if ((o1 != null) && (o2 != null)) {
							int s1 = (Integer) o1.get("column_order");
							int s2 = (Integer) o2.get("column_order");
							if (s1 > s2)
								return 1;
							else if (s1 == s2)
								return 0;
							else if (s1 < s2)
								return -1;
						}
						return 0;
					}
				};
				// Sort the column info, before creating column configs out of
				// them. So,
				// that they maintain the sort order and
				// hence the column order in the table.
				Collections.sort(attachmentSelectionTableColumns, comp);

				drawingTable = TransmittalUtils.getDrawingTableJSON(connection,
						dcrSystemId, transmittalProcessId, requestsList,
						attachmentSelectionTableColumns);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException(
						"Error occurred while creating transmittal table", e);
			}
		}
		trnProcessParamTable.put(DRAWING_TABLE, drawingTable);

		String toList = trnProcessParamTable.get(TO_LIST);
		String ccList = trnProcessParamTable.get(CC_LIST);

		String distributionTable = trnProcessParamTable.get(DISTRIBUTION_TABLE);
		if (distributionTable == null) {
			// if ((toList == null) && (ccList == null))
			if (ccList == null)
				distributionTable = "[]";
			else {
				ArrayList<String> propertyList = new ArrayList<String>();
				ArrayList<TbitsModelData> distributionTableColumnsList = TransmittalServiceImpl
						.getDistributionTableColumns(connection,
								transmittalProcessId);

				Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
					public int compare(TbitsModelData o1, TbitsModelData o2) {
						if ((o1 != null) && (o2 != null)) {
							int s1 = (Integer) o1.get("column_order");
							int s2 = (Integer) o2.get("column_order");
							if (s1 > s2)
								return 1;
							else if (s1 == s2)
								return 0;
							else if (s1 < s2)
								return -1;
						}
						return 0;
					}
				};
				// Sort the column info, before creating column configs out of
				// them. So,
				// that they maintain the sort order and
				// hence the column order in the table.
				Collections.sort(distributionTableColumnsList, comp);

				for (TbitsModelData md : distributionTableColumnsList) {
					propertyList.add((String) md.get("name"));
				}
				ArrayList<TbitsModelData> distributionListData = TransmittalUtils
						.getDistributionListData(connection,
								transmittalProcessId, toList, ccList,
								distributionTableColumnsList);
				if (distributionListData != null) {
					JsonArray distributionListJsonStr = TransmittalUtils
							.getDistributionListJsonString(
									distributionListData, propertyList);
					if (distributionListJsonStr != null)
						distributionTable = distributionListJsonStr.toString();
				}
			}
			trnProcessParamTable.put(DISTRIBUTION_TABLE, distributionTable);
		}
		// here will write code for attachment list for dtn

		// String selectedAttachmentsTable =
		// trnProcessParamTable.get(SELECTED_ATTACHMENTS_TABLE);
		// ArrayList<String[]> selectedAttachmentsList =
		// TransmittalUtils.fetchArrayListFromJsonArray(selectedAttachmentsTable);

		ArrayList<TbitsModelData> attachmentSelectionTableColumns1 = null;
		String selectedAttachmentsTable = trnProcessParamTable
				.get(SELECTED_ATTACHMENTS_TABLE);

		if (selectedAttachmentsTable == null) {
			// trnProcessParamTable.put(SELECTED_ATTACHMENTS_TABLE, "[]");

			try {
				attachmentSelectionTableColumns1 = TransmittalServiceImpl
						.getAttachmentSelectionTableColumns(connection,
								transmittalProcessId);
				Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
					public int compare(TbitsModelData o1, TbitsModelData o2) {
						if ((o1 != null) && (o2 != null)) {
							int s1 = (Integer) o1.get("column_order");
							int s2 = (Integer) o2.get("column_order");
							if (s1 > s2)
								return 1;
							else if (s1 == s2)
								return 0;
							else if (s1 < s2)
								return -1;
						}
						return 0;
					}
				};
				// Sort the column info, before creating column configs out of
				// them. So,
				// that they maintain the sort order and
				// hence the column order in the table.
				Collections.sort(attachmentSelectionTableColumns1, comp);
				final_arr = this.getSelectedAttachmentslist(requestsList,
						trnProcessParamTable.get("sysprefix"),
						attachmentSelectionTableColumns1, Integer
								.parseInt(trnProcessParamTable
										.get(Field.BUSINESS_AREA)),
						allAttachments);

			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException(
						"Error occurred while creating transmittal table", e);
			}

		}

		// Accumulate all the field names of type attachment from use while
		// updating target BAs
		HashSet<String> deliverableNames = new HashSet<String>();
		if (allAttachments != null) {
			Collection<HashMap<String, Collection<AttachmentInfo>>> values = allAttachments
					.values();
			if (values != null) {
				for (HashMap<String, Collection<AttachmentInfo>> tMap : values) {
					if (tMap != null)
						deliverableNames.addAll(tMap.keySet());
				}
			}
		}

		if (deliverableNames != null) {
			String delNames = "";
			for (String delName : deliverableNames) {
				delNames = (delNames.equals("")) ? delName : delNames + ","
						+ delName;
			}
			trnProcessParamTable.put(DELIVERABLE_FIELD_NAME, delNames);
		}

		try {
			int transmittalMaxId = 0;

			String formattedTrnReqId = TransmittalUtils.EMPTY_STRING;
			String dtnSerialKey = selectedTransmittalProcess.getTrnMaxSnKey();

			if ((dtnSerialKey != null) && (dtnSerialKey.trim().isEmpty()))
				throw new TBitsException(
						"Invalid transmittal serial key provided for transmittal process: "
								+ selectedTransmittalProcess.getTrnProcessId());

			/*
			 * if (dtnNumber.contains("{Likely}")) { int lIndex =
			 * dtnNumber.lastIndexOf("-"); if (lIndex > 0) { dtnNumber =
			 * dtnNumber.substring(lIndex); } }
			 * 
			 * if ((transmittalNumber == null) ||
			 * transmittalNumber.trim().equals("")) { transmittalMaxId =
			 * TransmittalProcess.getMaxTransmittalNumber( connection,
			 * dtnBA.getSystemId(), dtnSerialKey); formattedTrnReqId =
			 * TransmittalUtils .getFormattedStringFromNumber(transmittalMaxId);
			 * 
			 * dtnNumber = trnProcessParamTable
			 * .get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
			 * 
			 * if (dtnNumber == null) dtnNumber = "";
			 * 
			 * dtnNumber = dtnNumber + formattedTrnReqId; } else dtnNumber =
			 * transmittalNumber;
			 */

			if (dtnNumber.contains("{Likely}")) {
				int lIndex = dtnNumber.lastIndexOf("-");
				if (lIndex > 0) {
					dtnNumber = dtnNumber.substring(lIndex);
				}
			}

			// if ((transmittalNumber == null) ||
			// transmittalNumber.trim().equals(""))
			if (shouldGenerateDtnNumber == true) {
				transmittalMaxId = TransmittalProcess.getMaxTransmittalNumber(
						connection, dtnBA.getSystemId(), dtnSerialKey);
				formattedTrnReqId = TransmittalUtils
						.getFormattedStringFromNumber(transmittalMaxId);

				dtnNumber = trnProcessParamTable
						.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);

				if (dtnNumber == null)
					dtnNumber = "";

				dtnNumber = dtnNumber + formattedTrnReqId;
			} else
				dtnNumber = transmittalNumber;

			trnProcessParamTable.put(TransmittalUtils.TRANSMITTAL_ID_PREFIX,
					dtnNumber);

			formattedTrnReqId = createTransmittal(connection, contextPath,
					tBitsResMgr, trnProcessParamTable, dcrBA,
					selectedTransmittalProcess, dtnBA, dcrRequestList,
					allAttachments);

			LOG
					.info("%%%%%%%%%%%%%%%%%%%%%%%%Finished updating all BA's&&&&&&&&&&&&&&&&&&&");

			return formattedTrnReqId;
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			LOG.error("Error occurred while creating transmittal: \n"
					+ e1.getMessage(), e1);
			throw new TBitsException(
					"Error occurred while creating transmittal: \n"
							+ e1.getMessage(), e1);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			LOG.error(
					"File not found exception occurred during tranmisttal process: "
							+ e1.getMessage(), e1);
			throw new TBitsException(
					"File not found exception occurred during transmittal process: "
							+ e1.getMessage(), e1);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
			throw new TBitsException(e1.getDescription(), e1);
		} catch (TBitsException e1) {
			e1.printStackTrace();
			LOG.error(e1);
			throw e1;
		} catch (IOException e1) {
			e1.printStackTrace();
			LOG.error(e1);
			throw new TBitsException(e1);
		} catch (APIException e1) {
			e1.printStackTrace();
			LOG.error(e1);
			throw e1;
		} catch (ParseException e) {
			e.printStackTrace();
			LOG.error(e);
			throw new TBitsException(
					"Error occurred while creating transmittal: \n"
							+ e.getMessage(), e);
		}
	}

	private String createTransmittal(
			Connection connection,
			String contextPath,// being called by 1 one
			TBitsResourceManager tBitsResMgr,
			HashMap<String, String> trnProcessParamTable,
			BusinessArea dcrBA,
			TransmittalProcess selectedTransmittalProcess,
			BusinessArea dtnBA,
			String[] dcrRequestList,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments)
			throws DatabaseException, TBitsException, APIException,
			ParseException, FileNotFoundException, IOException {

		String dtnAttachment = TransmittalUtils.EMPTY_STRING;

		String toDisplayNameList = "";
		String ccDisplayNameList = "";
		HashMap<String, String> kindAttentionInfo = new HashMap<String, String>();
		TransmittalUtils.prefillWithUserColumns(kindAttentionInfo);

		String toList = trnProcessParamTable.get(TO_LIST);
		String ccList = trnProcessParamTable.get(CC_LIST);
		try {
			if (toList != null) {
				TransmittalUtils.getKindAttentionUserInfo(trnProcessParamTable,
						kindAttentionInfo, toList);
				toDisplayNameList = TransmittalUtils
						.getUserDisplayNameList(toList);
			}

			if (ccList != null)
				ccDisplayNameList = TransmittalUtils
						.getUserDisplayNameList(ccList);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
			throw new TBitsException(
					"Database error occurred while fetching display names of users in To/Cc list.",
					e1);
		}

		trnProcessParamTable.put(TO_LIST, toDisplayNameList);
		trnProcessParamTable.put(CC_LIST, ccDisplayNameList);

		String distributionTable = trnProcessParamTable.get(DISTRIBUTION_TABLE);
		ArrayList<String[]> distributionList = TransmittalUtils
				.fetchDistributionListFromJsonArray(distributionTable);

		String drawingsTable = trnProcessParamTable.get(DRAWING_TABLE);
		ArrayList<String[]> drawingsList = TransmittalUtils
				.fetchDrawingListFromJsonArray(drawingsTable);

		String selectedAttachmentsTable = trnProcessParamTable
				.get(SELECTED_ATTACHMENTS_TABLE);
		ArrayList<String[]> selectedAttachmentsList;
		if (final_arr == null) {
			selectedAttachmentsList = TransmittalUtils
					.fetchArrayListFromJsonArray(selectedAttachmentsTable);
		} else {
			selectedAttachmentsList = final_arr;
		}
		HashMap<String, String> loggerInfo = null;
		String dtnNumber = trnProcessParamTable
				.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);

		String dtnLogger = trnProcessParamTable.get(DTN_LOGGER);
		if ((dtnLogger == null) || (dtnLogger.trim().equals(""))) {
			if (user == null)
				throw new TBitsException(
						"No user/dtnLogger provided or invalid value provided. Please contact tBits team.");
			else {
				dtnLogger = user.getUserLogin();

				trnProcessParamTable.put(DTN_LOGGER, dtnLogger);
			}
		}

		/**
		 * If the transmittal is created through excel import, then if the
		 * dtnlogger is given as null, then take the current user as dtnlogger
		 */
		if (trnProcessParamTable.containsKey(IS_IMPORT_PROCESS)
				&& trnProcessParamTable.get(IS_IMPORT_PROCESS).trim().equals(
						"true")) {
			dtnLogger = user.getUserLogin();
			trnProcessParamTable.put(DTN_LOGGER, dtnLogger);
		}

		String dtnSignatory = trnProcessParamTable
				.get(TransmittalUtils.DTN_SIGNATORY);
		if ((dtnSignatory == null) || (dtnSignatory.trim().equals(""))) {
			loggerInfo = TransmittalUtils.getUserInfoMap(user);
		} else {
			User dtnSigUser = User.lookupByUserLogin(dtnSignatory);
			if (dtnSigUser == null)
				throw new TBitsException(
						"No user/dtnSignatory provided or invalid value provided. Please contact tBits team.");
			else
				loggerInfo = TransmittalUtils.getUserInfoMap(dtnSigUser);
		}

		boolean isApprovalCycle = Boolean.valueOf(trnProcessParamTable
				.get(IS_APPROVAL_CYCLE));

		BusinessArea approvalBA = BusinessArea
				.lookupBySystemId(selectedTransmittalProcess.getDtrSysId());
		String curLoggerImagePath = loggerInfo.get("user_login");
		String curHeaderImagePath = trnProcessParamTable
				.get(TransmittalUtils.DTN_HEADER_IMAGE);
		String curFooterImagePath = trnProcessParamTable
				.get(TransmittalUtils.DTN_FOOTER_IMAGE);
		try {

			BirtTemplateHelper kth = new BirtTemplateHelper(
					trnProcessParamTable, drawingsList, distributionList,
					selectedAttachmentsList, loggerInfo, kindAttentionInfo);
			ArrayList<TemplateTuple> templates = new ArrayList<TemplateTuple>();
			TemplateTuple temp = null;

			String formatString = trnProcessParamTable.get("formats");
			if (formatString != null && !formatString.trim().equals("")) {

				String[] docnames = formatString.trim().split(";");

				for (String eachdoc : docnames) {
					String[] docFormat = eachdoc.trim().split(":");
					String docName = null;
					String format = null;
					if (docFormat.length == 2) {
						docName = docFormat[0].trim();
						format = docFormat[1].trim();
					} else if (docFormat.length == 1) {
						docName = docFormat[0].trim();
						format = PDF;
					} else if (docFormat.length == 0) {
						throw new TBitsException(
								"Please provide appropriate Json String in Format key ");
					} else {
						throw new TBitsException(
								"Please provide appropriate Json String in Format key ");
					}

					temp = new TemplateTuple(docName, format);
					templates.add(temp);

				}
			} else {
				String templateName = trnProcessParamTable
						.get(TRANSMITTAL_TEMPLATE_NAME);
				if ((templateName == null) || (templateName.trim().equals(""))) {
					throw new TBitsException(
							"Please provide appropriate DTN template for generating transmittal note.");

				}
				temp = new TemplateTuple(templateName, PDF);
				templates.add(temp);
			}

			String attachmentHint = "";
			if (isApprovalCycle)
				attachmentHint = approvalBA.getSystemPrefix();
			else
				attachmentHint = dtnBA.getSystemPrefix();
			// helper,templatename,outputfileName,hint

			dtnAttachment = getDTNAttachmentWithGivenFormat(kth, templates,
					dtnNumber, attachmentHint);

		} catch (EngineException e) {
			e.printStackTrace();
			throw new TBitsException(e);
		}

		if (isApprovalCycle) {
			if (approvalBA == null) {
				throw new TBitsException(
						"Invalid system id provide for approval business area in transmittal"
								+ " processes configuration: "
								+ selectedTransmittalProcess.getDtrSysId());
			}

			trnProcessParamTable.put(TransmittalUtils.LOGGER_IMAGE_PATH,
					(curLoggerImagePath == null) ? "" : curLoggerImagePath);
			trnProcessParamTable.put(TransmittalUtils.DTN_HEADER_IMAGE,
					(curHeaderImagePath == null) ? "" : curHeaderImagePath);
			trnProcessParamTable.put(TransmittalUtils.DTN_FOOTER_IMAGE,
					(curFooterImagePath == null) ? "" : curFooterImagePath);

			Request approvalRequest = logRequestInApprovalBA(connection,
					contextPath, tBitsResMgr, selectedTransmittalProcess,
					trnProcessParamTable, approvalBA, dtnAttachment,
					allAttachments, toList, ccList);
			updateSourceDCRBusinessAreaPostApproval(connection, tBitsResMgr,
					dcrRequestList, dcrBA, selectedTransmittalProcess,
					trnProcessParamTable, approvalBA, approvalRequest);
			return dtnNumber + " [" + approvalBA.getSystemPrefix() + "#"
					+ approvalRequest.getRequestId() + "]";
		} else {
			Request trnRequest = logRequestInTransmittalBA(connection,
					contextPath, tBitsResMgr, selectedTransmittalProcess,
					trnProcessParamTable, dtnBA, dtnAttachment, allAttachments,
					toList, ccList);

			ArrayList<Request> dcrRequests = updateSourceDCRBusinessArea(
					connection, tBitsResMgr, dcrRequestList, dcrBA,
					selectedTransmittalProcess, trnProcessParamTable, dtnBA,
					trnRequest);

			ArrayList<Integer> targetBAList = getTargetBusinessAreas(
					connection, selectedTransmittalProcess.getTrnProcessId());
			for (Integer targetBAId : targetBAList) {

				updateTargetBA(connection, trnRequest, dtnNumber, dcrBA,
						targetBAId, dcrRequests, selectedTransmittalProcess,
						tBitsResMgr, allAttachments, contextPath,
						trnProcessParamTable);
			}

			String dtnNumberWithLink = dtnNumber + " ["
					+ dtnBA.getSystemPrefix() + "#" + trnRequest.getRequestId()
					+ "]";
			String isPostApproval = trnProcessParamTable
					.get(TransmittalUtils.IS_POST_APPROVAL_CYCLE);
			if (isPostApproval != null) {
				String appCycleReqId = trnProcessParamTable
						.get(TransmittalUtils.APPROVAL_CYCLE_REQUEST_ID);
				if (appCycleReqId != null) {
					if (approvalBA == null) {
						throw new TBitsException(
								"Invalid system id provide for approval business area in transmittal"
										+ " processes configuration: "
										+ selectedTransmittalProcess
												.getDtrSysId());
					} else {
						Integer approvalSysId = Integer.valueOf(appCycleReqId);
						deleteApprovalTransientData(connection, approvalBA
								.getSystemId(), approvalSysId);
						deleteApprovalTransientDataAttachments(connection,
								approvalBA.getSystemId(), approvalSysId);
						UpdateRequest updateAppRequest = new UpdateRequest();
						updateAppRequest
								.setSource(TBitsConstants.SOURCE_CMDLINE);
						Hashtable<String, String> appParamTable = new Hashtable<String, String>();
						appParamTable.put(Field.BUSINESS_AREA, approvalBA
								.getSystemPrefix());
						appParamTable.put(Field.USER, user.getUserLogin());
						appParamTable.put(Field.REQUEST, appCycleReqId);
						appParamTable.put(Field.STATUS, APPROVED);
						appParamTable.put(Field.DESCRIPTION,
								"Approved the transmittal: "
										+ dtnNumberWithLink);
						updateAppRequest.updateRequest(appParamTable);
					}
				} else {
					throw new TBitsException(
							"No approval request was found for this transmittal.");
				}
			}

			return dtnNumberWithLink;
		}
	}

	protected static void deleteApprovalTransientData(Connection connection,
			int systemId, int approvalRequestId) throws DatabaseException {

		PreparedStatement ps = null;
		try {
			ps = connection
					.prepareStatement("DELETE FROM trn_approval_cycle_transient_data where sys_id="
							+ +systemId
							+ " and request_id="
							+ approvalRequestId);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while deleting transient data.", e);
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException(
							"Database error occurred while deleting transient data.",
							e);
				}
		}
	}

	protected static void deleteApprovalTransientDataAttachments(
			Connection connection, int systemId, int approvalRequestId)
			throws DatabaseException {

		PreparedStatement ps = null;
		try {
			ps = connection
					.prepareStatement("DELETE FROM trn_approval_cycle_transient_data_attachments where sys_id="
							+ +systemId
							+ " and request_id="
							+ approvalRequestId);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while deleting transient data attachments.",
					e);
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException(
							"Database error occurred while deleting transient data attachments.",
							e);
				}
		}
	}

	private ArrayList<Request> updateSourceDCRBusinessArea(
			Connection connection, TBitsResourceManager tBitsResMgr,
			String[] dcrRequestList, BusinessArea dcrBA,
			TransmittalProcess transmittalProcess,
			HashMap<String, String> trnProcessParamsTable, BusinessArea dtnBA,
			Request trnRequest) throws DatabaseException, TBitsException,
			APIException, ParseException {

		int dcrSysId = dcrBA.getSystemId();
		String outwardDTNFieldIdStr = trnProcessParamsTable.get(DTN_FIELD_ID);
		Field dcrDTNField = null;

		if ((outwardDTNFieldIdStr == null)
				|| outwardDTNFieldIdStr.trim().equals(""))
			LOG
					.warn("Skipped populating outgoing DTN field as the appropriate field id for the field was not found.");
		else {
			outwardDTNFieldIdStr = outwardDTNFieldIdStr.trim();
			try {
				dcrDTNField = Field.lookupBySystemIdAndFieldId(dcrSysId,
						Integer.parseInt(outwardDTNFieldIdStr));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				LOG
						.error(
								"Number format error occurred while fetching outward DTN field in originating DCR BA: "
										+ dcrBA.getSystemPrefix(), nfe);
				throw new TBitsException(nfe);
			}
		}

		String logDate = trnProcessParamsTable.get(TRANSMITTAL_DATE);

		ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(
				dcrSysId, true, DataType.ATTACHMENTS);
		ArrayList<Request> dcrRequests = new ArrayList<Request>(
				dcrRequestList.length);
		for (int index = 0; index < dcrRequestList.length; index++) {
			UpdateRequest dcrUpdateRequest = new UpdateRequest();

			// Update the drawings sent to transmittal by changing the status
			// and description.
			Hashtable<String, String> tempParamTable = new Hashtable<String, String>();

			tempParamTable.put(Field.BUSINESS_AREA, dcrSysId
					+ TransmittalUtils.EMPTY_STRING);
			tempParamTable.put(Field.REQUEST, dcrRequestList[index]);

			if ((logDate != null) && (!logDate.trim().equals(""))) {
				tempParamTable.put(Field.LASTUPDATED_DATE, logDate);
			}

			int dcrReqId = Integer.parseInt(dcrRequestList[index]);
			Request dcrRequest = Request.lookupBySystemIdAndRequestId(
					connection, dcrSysId, dcrReqId);
			if (dcrRequest == null) {
				throw new TBitsException(
						"Request id provided in request list is invalid/does not exist: "
								+ dcrReqId + " in business area: "
								+ dcrBA.getSystemPrefix());
			}

			// Retain existing attachments, during update.
			if (extAttachmentFieldList != null) {
				for (Field extAttachmentField : extAttachmentFieldList) {
					if (extAttachmentField != null) {
						String fieldName = extAttachmentField.getName();
						String fieldValue = dcrRequest.get(fieldName);
						if (fieldValue != null)
							tempParamTable.put(fieldName, fieldValue);
					}
				}
			}

			String dtnNumber = trnProcessParamsTable
					.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);

			String calendarName = trnProcessParamsTable
					.get(TransmittalUtils.CALENDAR);
			calendarName = ((calendarName == null) || (calendarName.trim()
					.isEmpty())) ? "" : calendarName.trim();
			trnProcessParamsTable.put(TransmittalUtils.CALENDAR, calendarName);

			updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest,
					dcrSysId, transmittalProcess.getTrnProcessId(), dtnNumber,
					dtnBA.getSystemPrefix(), trnRequest, tempParamTable,
					calendarName, false);

			if (dcrDTNField != null)
				tempParamTable.put(dcrDTNField.getName(), dtnNumber);

			runTransmittalRules(connection, trnRequest, dcrBA, dcrBA,
					dcrRequest, tempParamTable, transmittalProcess,
					trnProcessParamsTable, TransmittalUtils.DCR_BUSINESS_AREA,
					false);

			// Set notify field based on Transmittal wizard input.
			setNotifyField(trnProcessParamsTable, tempParamTable);

			try {
				Request updatedDcrRequest = dcrUpdateRequest.updateRequest(
						connection, tBitsResMgr, tempParamTable);
				dcrRequests.add(index, updatedDcrRequest);
			} catch (NullPointerException npe) {
				throw new TBitsException(
						"Null pointer exception occurred while updating originating DCR business area: "
								+ dcrBA.getSystemPrefix(), npe);
			}
		}

		dcrRequests.trimToSize();
		return dcrRequests;
	}

	private void updateSourceDCRBusinessAreaPostApproval(Connection connection,
			TBitsResourceManager tBitsResMgr, String[] dcrRequestList,
			BusinessArea dcrBA, TransmittalProcess transmittalProcess,
			HashMap<String, String> trnProcessParamsTable,
			BusinessArea approvalBA, Request approvalRequest)
			throws DatabaseException, TBitsException, APIException,
			ParseException {

		TransmittalDropDownOption trnDropdownOption = null;
		int dcrSysId = dcrBA.getSystemId();

		int trnDropDownId = transmittalProcess.getTrnDropDownId();
		try {
			trnDropdownOption = TransmittalDropDownOption
					.lookupTrnDropdownOptionByDropdownId(connection, dcrSysId,
							trnDropDownId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while fetching drop down option "
							+ "based on trnDropdownId: " + trnDropDownId, e);
		}

		ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(
				dcrSysId, true, DataType.ATTACHMENTS);

		String transmittalDate = trnProcessParamsTable.get(TRANSMITTAL_DATE);
		if (transmittalDate == null)
			transmittalDate = "";
		String logDate = getLoggedDate(transmittalDate);

		for (int index = 0; index < dcrRequestList.length; index++) {
			UpdateRequest dcrUpdateRequest = new UpdateRequest();

			// Update the drawings sent to transmittal by changing the status
			// and description.
			Hashtable<String, String> tempParamTable = new Hashtable<String, String>();
			tempParamTable.put(Field.BUSINESS_AREA, dcrSysId
					+ TransmittalUtils.EMPTY_STRING);
			tempParamTable.put(Field.REQUEST, dcrRequestList[index]);
			tempParamTable.put(Field.USER, approvalRequest.get(Field.LOGGER));

			if ((logDate != null) && (!logDate.trim().equals("")))
				tempParamTable.put(Field.LASTUPDATED_DATE, logDate);

			int dcrReqId = Integer.parseInt(dcrRequestList[index]);
			Request dcrRequest = Request.lookupBySystemIdAndRequestId(
					connection, dcrSysId, dcrReqId);

			// Retain existing attachments, during update.
			if (extAttachmentFieldList != null) {
				for (Field extAttachmentField : extAttachmentFieldList) {
					if (extAttachmentField != null) {
						String fieldName = extAttachmentField.getName();
						String fieldValue = dcrRequest.get(fieldName);
						if (fieldValue != null)
							tempParamTable.put(fieldName, fieldValue);
					}
				}
			}

			// String dtnNumber =
			// trnProcessParamsTable.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
			String calendarName = trnProcessParamsTable
					.get(TransmittalUtils.CALENDAR);
			calendarName = ((calendarName == null) || (calendarName.trim()
					.isEmpty())) ? "" : calendarName.trim();
			trnProcessParamsTable.put(TransmittalUtils.CALENDAR, calendarName);

			// if (dcrDTNField != null)
			// tempParamTable.put(dcrDTNField.getName(), dtnNumber);

			runTransmittalRules(connection, approvalRequest, dcrBA, dcrBA,
					dcrRequest, tempParamTable, transmittalProcess,
					trnProcessParamsTable, TransmittalUtils.DCR_BUSINESS_AREA,
					false);

			if (trnDropdownOption != null)
				tempParamTable.put(Field.DESCRIPTION,
						"Submitted for approval for the transmittal process: "
								+ trnDropdownOption.getName() + "["
								+ approvalBA.getSystemPrefix() + "#"
								+ approvalRequest.getRequestId() + "]");
			else
				tempParamTable.put(Field.DESCRIPTION, "Submitted for approval"
						+ "[" + approvalBA.getSystemPrefix() + "#"
						+ approvalRequest.getRequestId() + "].");

			// Set notify field based on Transmittal wizard input.
			setNotifyField(trnProcessParamsTable, tempParamTable);

			try {
				dcrUpdateRequest.updateRequest(connection, tBitsResMgr,
						tempParamTable);
			} catch (NullPointerException npe) {
				throw new TBitsException(
						"Null pointer exception occurred while updating originating DCR business area: "
								+ dcrBA.getSystemPrefix(), npe);
			}
		}
	}

	/**
	 * @param connection
	 * @param aRequest
	 * @param tBitsResMgr
	 * @param trnProcessParamTable
	 * @param transBA
	 * @param aParamTable
	 * @param trnIdPrefix
	 * @param toList
	 * @param ccList
	 * @param transmittalSubject
	 * @param emailBody
	 * @param formattedTrnReqId
	 * @param dtnAttachment
	 * @param linkedRequests
	 * @param transmittalNumber
	 * @param transmittalDate
	 * @return
	 * @throws TBitsException
	 * @throws NumberFormatException
	 * @throws DatabaseException
	 * @throws APIException
	 * @throws ParseException
	 */
	private Request logRequestInTransmittalBA(
			Connection connection,
			String contextPath,
			TBitsResourceManager tBitsResMgr,
			TransmittalProcess trnProcess,
			HashMap<String, String> trnProcessParamTable,
			BusinessArea transBA,
			String dtnAttachment,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments,
			String toList, String ccList) throws TBitsException,
			NumberFormatException, DatabaseException, APIException,
			ParseException {

		// Create new AddRequest object to add new transmittal.
		AddRequest addReq = new AddRequest();
		addReq.setContext(contextPath);
		addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> addReqParamTable = new Hashtable<String, String>();
		addReqParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
		int dtnSysId = transBA.getSystemId();

		String dtnLogger = trnProcessParamTable.get(DTN_LOGGER);
		if ((dtnLogger == null) || (dtnLogger.trim().equals(""))) {
			throw new TBitsException("Invalid DTN logger name.");
		}
		addReqParamTable.put(Field.USER, dtnLogger.trim());

		String dtnSignatory = trnProcessParamTable
				.get(TransmittalUtils.DTN_SIGNATORY);
		if (dtnSignatory != null) {
			if (!dtnLogger.trim().equalsIgnoreCase(dtnSignatory.trim()))
				ccList = ((ccList == null) || (ccList.trim().equals(""))) ? dtnSignatory
						: ccList + "," + dtnSignatory;
		}

		String accessTo = trnProcessParamTable.get(ACCESS_TO);
		if ((accessTo != null) && (!accessTo.trim().equals("")))
			addReqParamTable.put(ACCESS_TO, accessTo);

		addReqParamTable.put(Field.ASSIGNEE, toList);
		addReqParamTable.put(Field.SUBSCRIBER, ccList);
		addReqParamTable.put(Field.STATUS, DTN_STATUS_TRANSMITTAL_COMPLETE);

		String logDate = trnProcessParamTable.get(TRANSMITTAL_DATE);
		if ((logDate == null) || logDate.trim().equals(""))
			logDate = getLoggedDate(logDate);
		if ((logDate != null) && (!logDate.trim().equals(""))) {
			// addReqParamTable.put(Field.LOGGED_DATE, logDate);
			// addReqParamTable.put(Field.LASTUPDATED_DATE, logDate);
			addReqParamTable.put(TransmittalUtils.ACTUAL_TRANSMITTAL_DATE,
					logDate);
		}
		// trnProcessParamTable.put(TRANSMITTAL_DATE, logDate);

		// Fetch ids for the fields, DTNNoteFieldId, TransmittedFileFieldId
		String dtnDelFieldIdStr = trnProcessParamTable
				.get(TRANSMITTED_FILE_FIELD_ID);
		String dtnNoteFieldIdStr = trnProcessParamTable.get(DTN_NOTE_FIELD_ID);
		LOG.debug("Transmitted attachments field id: " + dtnDelFieldIdStr);
		LOG.debug("DTN Note attachment field id: " + dtnNoteFieldIdStr);

		Field dtnDeliverableField = getAttachmentFieldById(connection,
				dtnSysId, dtnDelFieldIdStr, DTN_NO_DEL_FIELD_ID_PROVIDED_MSG
						+ TRANSMITTED_FILE_FIELD_ID, TRANSMITTED_FILE_FIELD_ID
						+ " " + DTN_DEL_FIELD_ID_PROVIDED_IS_INVALID_MSG);

		if (allAttachments != null) {
			Collection<AttachmentInfo> exTempList = getAllAttachmentsFromRequestAttachmentMap(allAttachments);

			// TODO: Check if all the attachment types involved in this business
			// area are different or same.
			if (!exTempList.isEmpty())
				addReqParamTable.put(dtnDeliverableField.getName(),
						AttachmentInfo.toJson(exTempList));
		}

		Field dtnNoteField = getAttachmentFieldById(connection, dtnSysId,
				dtnNoteFieldIdStr, DTN_NO_DEL_FIELD_ID_PROVIDED_MSG
						+ DTN_NOTE_FIELD_ID,
				DTN_DEL_FIELD_ID_PROVIDED_IS_INVALID_MSG);
		addReqParamTable.put(dtnNoteField.getName(), dtnAttachment);

		addReqParamTable.put(Field.IS_PRIVATE, TransmittalUtils.TRUE);

		String dtnNumber = trnProcessParamTable
				.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
		if (dtnNumber == null)
			dtnNumber = "";

		// Set notify based on transmittal wizard input.
		setNotifyField(trnProcessParamTable, addReqParamTable);

		String subject = trnProcessParamTable.get(TRANSMITTAL_SUBJECT);
		if ((subject == null) || (subject.trim().isEmpty()))
			subject = dtnNumber;
		else
			subject = dtnNumber + " - " + subject;
		addReqParamTable.put(Field.SUBJECT, subject);

		addReqParamTable.put(DTN_NUMBER, dtnNumber);

		String originatingFirm = trnProcessParamTable.get(ORIGINATOR);
		if ((originatingFirm == null) || (originatingFirm.trim().equals(""))) {
			LOG
					.error("No originating firm provided in trn_process_parameters or invalid originating firm: "
							+ originatingFirm);
			throw new TBitsException("Invalid originating firm.");
		}
		addReqParamTable.put(Field.CATEGORY, originatingFirm);

		String recipientFirm = trnProcessParamTable.get(RECIPIENT);
		if ((recipientFirm == null) || (recipientFirm.trim().equals(""))) {
			LOG
					.error("No, recipient firm provided in trn_process_parameters or invalid recipient firm: "
							+ recipientFirm);
			throw new TBitsException("Invalid recipient firm.");
		}
		addReqParamTable.put(Field.REQUEST_TYPE, recipientFirm);

		/*
		 * 1. Connection Object 2. Transmittal Number 3. DCR business area 4.
		 * DCR httpRequest list
		 */
		// runPreTransmittalRules(connection, transReqId, aParamTable, tpParams,
		// dcrBA, dcrRequestList, trnTypeName, true);

		StringBuffer desSB = new StringBuffer();
		String emailBody = trnProcessParamTable.get(EMAIL_BODY);
		if (emailBody == null)
			emailBody = "";
		desSB.append(emailBody);

		addReqParamTable.put(Field.DESCRIPTION, desSB.toString());

		TransmittalUtils.getPostTrnBusinessAreaFieldsAndValues(connection,
				transBA, trnProcess, addReqParamTable, trnProcessParamTable,
				dtnNumber);

		runTransmittalRules(connection, null, transBA, null, null,
				addReqParamTable, trnProcess, trnProcessParamTable,
				TransmittalUtils.TRANSMITTAL_BUSINESS_AREA, true);

		Request trnRequest = addReq.addRequest(connection, tBitsResMgr,
				addReqParamTable);
		return trnRequest;
	}

	private static void setNotifyField(
			HashMap<String, String> trnProcessParamTable,
			Hashtable<String, String> addUpdateReqParamTable) {
		String notify = trnProcessParamTable.get(NOTIFY);
		if (notify == null)
			notify = KEYWORD_TRUE;
		else if (Boolean.parseBoolean(notify)) {
			addUpdateReqParamTable.put(Field.NOTIFY, KEYWORD_TRUE);
			addUpdateReqParamTable.put(Field.NOTIFY_LOGGERS, KEYWORD_TRUE);
		} else {
			addUpdateReqParamTable.put(Field.NOTIFY, KEYWORD_FALSE);
			addUpdateReqParamTable.put(Field.NOTIFY_LOGGERS, KEYWORD_FALSE);
		}
	}

	private Request logRequestInApprovalBA(
			Connection connection,
			String contextPath,
			TBitsResourceManager tBitsResMgr,
			TransmittalProcess trnProcess,
			HashMap<String, String> trnProcessParamTable,
			BusinessArea approvalBA,
			String dtnAttachment,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments,
			String toList, String ccList) throws TBitsException,
			NumberFormatException, DatabaseException, APIException,
			ParseException {

		// Create new AddRequest object to add new transmittal.
		AddRequest addReq = new AddRequest();
		addReq.setContext(contextPath);
		addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		aParamTable.put(Field.BUSINESS_AREA, approvalBA.getSystemPrefix());
		int dtnSysId = approvalBA.getSystemId();

		String dtnLogger = trnProcessParamTable.get(DTN_LOGGER);
		if ((dtnLogger == null) || (dtnLogger.trim().equals(""))) {
			throw new TBitsException("Invalid DTN logger name.");
		}
		aParamTable.put(Field.USER, user.getUserLogin());
		// aParamTable.put(Field.DESCRIPTION, "Transmittal for documents: " +
		// linkedRequests);

		String logDate = trnProcessParamTable.get(TRANSMITTAL_DATE);
		if ((logDate == null) || logDate.trim().equals(""))
			logDate = getLoggedDate(logDate);
		if ((logDate != null) && (!logDate.trim().equals(""))) {
			aParamTable.put(Field.LOGGED_DATE, logDate);
			aParamTable.put(Field.LASTUPDATED_DATE, logDate);
		}

		String dtnSignatory = trnProcessParamTable
				.get(TransmittalUtils.DTN_SIGNATORY);
		if (dtnSignatory != null)
			aParamTable.put(Field.ASSIGNEE, dtnSignatory);
		else
			aParamTable.put(Field.ASSIGNEE, dtnLogger.trim());

		aParamTable.put(Field.STATUS, SUBMITTED_FOR_APPROVAL);

		// Fetch ids for the fields, DTNNoteFieldId, TransmittedFileFieldId
		String dtnDelFieldIdStr = trnProcessParamTable
				.get(TRANSMITTED_FILE_FIELD_ID);
		String dtnNoteFieldIdStr = trnProcessParamTable.get(DTN_NOTE_FIELD_ID);
		LOG.debug("Transmitted attachments field id: " + dtnDelFieldIdStr);
		LOG.debug("DTN Note attachment field id: " + dtnNoteFieldIdStr);

		Field dtnDeliverableField = getAttachmentFieldById(connection,
				dtnSysId, dtnDelFieldIdStr, DTN_NO_DEL_FIELD_ID_PROVIDED_MSG
						+ TRANSMITTED_FILE_FIELD_ID,
				DTN_DEL_FIELD_ID_PROVIDED_IS_INVALID_MSG);

		if (allAttachments != null) {
			Collection<AttachmentInfo> exTempList = getAllAttachmentsFromRequestAttachmentMap(allAttachments);

			// TODO: Check if all the attachment types involved in this business
			// area are different or same.
			if (!exTempList.isEmpty())
				aParamTable.put(dtnDeliverableField.getName(), AttachmentInfo
						.toJson(exTempList));
		}

		Field dtnNoteField = getAttachmentFieldById(connection, dtnSysId,
				dtnNoteFieldIdStr, DTN_NO_DEL_FIELD_ID_PROVIDED_MSG
						+ DTN_NOTE_FIELD_ID,
				DTN_DEL_FIELD_ID_PROVIDED_IS_INVALID_MSG);
		aParamTable.put(dtnNoteField.getName(), dtnAttachment);

		aParamTable.put(Field.IS_PRIVATE, TransmittalUtils.TRUE);

		String dtnNumber = trnProcessParamTable
				.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
		if (dtnNumber == null)
			dtnNumber = "";

		// aParamTable.put(Field.NOTIFY, KEYWORD_TRUE);
		// aParamTable.put(Field.NOTIFY_LOGGERS, KEYWORD_TRUE);
		aParamTable.put(DTN_NUMBER, dtnNumber);

		String subject = trnProcessParamTable.get(TRANSMITTAL_SUBJECT);
		if (subject == null)
			subject = "";
		aParamTable.put(Field.SUBJECT, subject);

		String originatingFirm = trnProcessParamTable.get(ORIGINATOR);
		if ((originatingFirm == null) || (originatingFirm.trim().equals(""))) {
			LOG
					.error("No originating firm provided in trn_process_parameters or invalid originating firm: "
							+ originatingFirm);
			throw new TBitsException("Invalid originating firm.");
		}
		aParamTable.put(Field.CATEGORY, originatingFirm);

		String recipientFirm = trnProcessParamTable.get(RECIPIENT);
		if ((recipientFirm == null) || (recipientFirm.trim().equals(""))) {
			LOG
					.error("No, recipient firm provided in trn_process_parameters or invalid recipient firm: "
							+ recipientFirm);
			throw new TBitsException("Invalid recipient firm.");
		}
		aParamTable.put(Field.REQUEST_TYPE, recipientFirm);

		/*
		 * 1. Connection Object 2. Transmittal Number 3. DCR business area 4.
		 * DCR httpRequest list
		 */
		// runPreTransmittalRules(connection, transReqId, aParamTable, tpParams,
		// dcrBA, dcrRequestList, trnTypeName, true);

		StringBuffer desSB = new StringBuffer();
		String emailBody = trnProcessParamTable.get(EMAIL_BODY);
		if (emailBody == null)
			emailBody = "";
		desSB.append(emailBody);

		aParamTable.put(Field.DESCRIPTION, desSB.toString());

		TransmittalUtils.getPostTrnBusinessAreaFieldsAndValues(connection,
				approvalBA, trnProcess, aParamTable, trnProcessParamTable,
				dtnNumber);

		setNotifyField(trnProcessParamTable, aParamTable);

		Request approvalRequest = addReq.addRequest(connection, tBitsResMgr,
				aParamTable);

		trnProcessParamTable.put(TO_LIST, toList);
		trnProcessParamTable.put(CC_LIST, ccList);
		trnProcessParamTable.put(TransmittalUtils.ACTUAL_DATE, "");

		// Save the transient data to be used in the post approval to log
		// transmittal requests.
		ApprovalCycleUtils.insertTransientDataToDB(connection, approvalBA
				.getSystemId(), approvalRequest.getRequestId(),
				trnProcessParamTable);
		ApprovalCycleUtils.saveAttachementsStateToDB(connection, approvalBA
				.getSystemId(), approvalRequest.getRequestId(), true,
				allAttachments);

		return approvalRequest;
	}

	protected static Collection<AttachmentInfo> getAllAttachmentsFromRequestAttachmentMap(
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments) {

		Collection<AttachmentInfo> exTempList = new ArrayList<AttachmentInfo>();
		for (Integer requestId : allAttachments.keySet()) {
			HashMap<String, Collection<AttachmentInfo>> allDelMap = allAttachments
					.get(requestId);
			if (allDelMap != null)
				for (String fieldName : allDelMap.keySet()) {
					Collection<AttachmentInfo> tCollection = allDelMap
							.get(fieldName);
					if (tCollection != null)
						exTempList.addAll(tCollection);
				}
		}
		return exTempList;
	}

	/**
	 * @param transmittalDate
	 * @return
	 * @throws ParseException
	 */
	private static String getLoggedDate(String transmittalDate)
			throws ParseException {
		String logDate = "";
		if ((transmittalDate == null) || transmittalDate.trim().equals("")) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone(IST));
			Date d = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(
					TBitsConstants.API_DATE_FORMAT);
			logDate = sdf.format(d);
		} else {
			DateFormat df = new SimpleDateFormat(
					TBitsConstants.API_DATE_ONLY_FORMAT);
			Date d = df.parse(transmittalDate);
			logDate = Timestamp.toCustomFormat(d,
					TBitsConstants.API_DATE_FORMAT);
		}
		return logDate;
	}

	private void updateTargetBA(
			Connection connection,
			Request transmittalRequest,
			String formattedDtnNumber,
			BusinessArea dcrBA,
			int targetBASysId,
			ArrayList<Request> dcrRequests,
			TransmittalProcess transmittalType,
			TBitsResourceManager tBitsResMgr,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments,
			String contextPath, HashMap<String, String> tpParams)
			throws DatabaseException, TBitsException, APIException {

		boolean isMatched = false;
		int dcrSysId = dcrBA.getSystemId();
		int dtrSystemId = transmittalType.getDtrSysId();

		if (dcrSysId == targetBASysId)
			return;

		// Get the target BA.
		BusinessArea currentBA = BusinessArea.lookupBySystemId(targetBASysId);
		if (currentBA == null) {
			LOG.warn("Target business area with sys_id: " + targetBASysId
					+ ", does not exists,"
					+ " hence not updating in this business area.");
			return;
		}

		Field dNumField = null;
		String srcBANumFieldName = "";
		Field targetDrawingNumberField = null;
		String uniqueNumFieldIdStr = tpParams
				.get(UNIQUE_TRANSMITTAL_NUMBER_FIELD_ID);
		if ((uniqueNumFieldIdStr != null)
				&& (!uniqueNumFieldIdStr.trim().equals(""))) {
			int uniqueFieldId = Integer.parseInt(uniqueNumFieldIdStr);
			dNumField = Field.lookupBySystemIdAndFieldId(dcrSysId,
					uniqueFieldId);
			// Set the unique drawing number field name to fetch the unique
			// drawing number for originating DCR httpRequest,
			// using which the matching drawing/document is fetched in the
			// target business areas.
			if (dNumField != null) {
				srcBANumFieldName = dNumField.getName();
				targetDrawingNumberField = TransmittalUtils
						.getTargetBusinessAreaField(connection, transmittalType
								.getTrnProcessId(), dNumField.getFieldId(),
								targetBASysId);
			}
		}

		if (dNumField == null) {
			LOG.error("Invalid \"unqiue drawing number\", field id: "
					+ uniqueNumFieldIdStr
					+ " or the field is not active in BA: "
					+ dcrBA.getSystemPrefix());
			throw new TBitsException(
					"Invalid \"unqiue drawing number\", field id: "
							+ uniqueNumFieldIdStr
							+ " or the field is not active in BA: "
							+ dcrBA.getSystemPrefix());
		}
		if (targetDrawingNumberField == null) {
			LOG
					.error("Matching unique drawing/document number field not found in: "
							+ currentBA.getSystemPrefix()
							+ "for originating DCR BA field name: "
							+ srcBANumFieldName);
			throw new TBitsException(
					"Matching unique drawing/document number field not found in: "
							+ currentBA.getSystemPrefix()
							+ " for originating DCR BA field name: "
							+ srcBANumFieldName);
		}

		Field targetUIDField = Field.lookupBySystemIdAndFieldName(
				targetBASysId, UNIVERSAL_ID);

		// Get the deliverables list (treating other attachments too as
		// deliverables).
		String deliverableNames = tpParams.get(DELIVERABLE_FIELD_NAME);
		ArrayList<Field> deliverablesList = new ArrayList<Field>();
		if ((deliverableNames != null) && (!deliverableNames.trim().equals(""))) {
			for (String fieldName : deliverableNames.split(",")) {
				Field srcField = Field.lookupBySystemIdAndFieldName(dcrSysId,
						fieldName);
				deliverablesList.add(srcField);
			}
		}

		// Map deliverables with appropriate fields from source DCR.
		Hashtable<String, Field> srcTargetFieldMap = new Hashtable<String, Field>();
		if (deliverablesList.isEmpty())
			LOG
					.info("Did not find any deliverables, hence skipping copying any attachments.");
		else {
			for (Field field : deliverablesList) {
				Field targetField = TransmittalUtils
						.getTargetBusinessAreaField(connection, transmittalType
								.getTrnProcessId(), field.getFieldId(),
								targetBASysId);
				if (targetField != null) {
					srcTargetFieldMap.put(field.getName(), targetField);
				}
			}
		}

		String dtnSysPrefix = BusinessArea.lookupBySystemId(
				transmittalRequest.getSystemId()).getSystemPrefix();

		for (int i = 0; i < dcrRequests.size(); i++) {

			LOG.info("Updating target business area with Id: " + targetBASysId
					+ ", post transmittal.");
			isMatched = false;

			int baType = TransmittalUtils.OTHER_BUSINESS_AREA;
			if (dtrSystemId == targetBASysId) {
				baType = TransmittalUtils.DTR_BUSINESS_AREA;
			}

			Hashtable<String, String> aParamTable = new Hashtable<String, String>();
			Request dcrRequest = dcrRequests.get(i);
			String lastUpdatedDate = dcrRequest.get(Field.LASTUPDATED_DATE);
			String dNumValue = dcrRequest.get(srcBANumFieldName);
			Request matchedRequest = null;
			int srcUID = 0;

			try {
				if ((dNumValue == null) || (dNumValue.trim().equals(""))) {
					LOG
							.debug("Unique drawing number in originating DCR BA is empty or null.");
					try {
						String uidStr = dcrRequest.get(UNIVERSAL_ID);
						if (uidStr != null) {
							srcUID = Integer.valueOf(uidStr);
							if (srcUID > 0) {
								if (targetUIDField != null) {
									matchedRequest = getMatchingRequestInTargetBusinessArea(
											connection, targetBASysId,
											targetUIDField.getFieldId(), srcUID);
									LOG
											.debug("Since drawing number in originating DCR BA is empty or null, "
													+ "hence considering universal id: "
													+ srcUID);
								} else {
									LOG
											.debug("Field \"universal_id\" not found in this business area: "
													+ currentBA
															.getSystemPrefix());
									throw new TBitsException(
											"Field \"universal_id\" not found in this business area: "
													+ currentBA
															.getSystemPrefix()
													+ ". Hence transmittal process cannot continue.");
								}
							}
						}
					} catch (Exception e) {
						LOG.error("No UID found for request: "
								+ dcrRequest.getRequestId() + " in src BA: "
								+ dcrBA.getSystemPrefix());
					}
				} else {
					matchedRequest = getMatchingRequestInTargetBusinessArea(
							connection, targetBASysId, targetDrawingNumberField
									.getFieldId(), dNumValue);
				}

				ArrayList<Field> extAttachmentFieldList = Field
						.lookupBySystemId(targetBASysId, true,
								DataType.ATTACHMENTS);

				if (matchedRequest != null) {
					LOG.info("Found a matching request:"
							+ matchedRequest.getRequestId()
							+ ", in target business area: "
							+ currentBA.getSystemPrefix());
					isMatched = true;

					aParamTable.put(Field.BUSINESS_AREA, currentBA
							.getSystemPrefix());
					aParamTable.put(Field.REQUEST, matchedRequest
							.getRequestId()
							+ TransmittalUtils.EMPTY_STRING);
					aParamTable.put(Field.LASTUPDATED_DATE, lastUpdatedDate);

					// Retain existing attachments, during update.
					if (extAttachmentFieldList != null) {
						for (Field extAttachmentField : extAttachmentFieldList) {
							if ((extAttachmentField != null)
									&& (srcTargetFieldMap != null)
									&& (!srcTargetFieldMap.values().contains(
											extAttachmentField))) {
								String fieldName = extAttachmentField.getName();
								String fieldValue = matchedRequest
										.get(fieldName);
								if (fieldValue != null)
									aParamTable.put(fieldName, fieldValue);
							}
						}
					}

					// Superceding logic.
					String isSupercedeString = tpParams
							.get(IS_SUPERCEDE_DELIVERABLES);
					boolean isSupercede = true;
					if (isSupercedeString != null) {
						isSupercede = Boolean.parseBoolean(isSupercedeString);
					}

					HashMap<String, Collection<AttachmentInfo>> allDelAttachments = allAttachments
							.get(dcrRequest.getRequestId());
					if (allDelAttachments != null) {
						for (String fieldName : deliverableNames.split(",")) {
							// Get the deliverables
							Collection<AttachmentInfo> selectedDelAttachments = allDelAttachments
									.get(fieldName);
							if (selectedDelAttachments != null) {
								Field targetDelAttachmentField = srcTargetFieldMap
										.get(fieldName);
								String targetFieldName = "";
								if (targetDelAttachmentField == null)
									throw new TBitsException(
											"A matching target deliverable field id, for deliverable field: "
													+ fieldName
													+ "could not be found,"
													+ " for Business Area: "
													+ currentBA
															.getSystemPrefix());
								else
									targetFieldName = targetDelAttachmentField
											.getName();

								// Check if deliverables should be superseded.
								if (!isSupercede) {
									Collection<AttachmentInfo> prevDelAttachments = null;
									String delAttJSONStr = matchedRequest
											.get(targetFieldName);
									if (delAttJSONStr != null)
										prevDelAttachments = AttachmentInfo
												.fromJson(delAttJSONStr);

									TransmittalUtils.mergeAttachmentsLists(
											selectedDelAttachments,
											prevDelAttachments);
								}
								String delAttachments = "[]";
								if (selectedDelAttachments != null)
									delAttachments = AttachmentInfo
											.toJson(selectedDelAttachments);

								aParamTable
										.put(targetFieldName, delAttachments);
							}
						}
					}

					UpdateRequest updateRequest = new UpdateRequest();
					updateRequest.setContext(contextPath);
					updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

					if (targetUIDField != null)
						aParamTable.put(UNIVERSAL_ID, srcUID + "");

					updateFields(connection, transmittalType.getTrnProcessId(),
							dcrSysId, dcrRequest, targetBASysId, aParamTable);
					updatePostTransmittalFieldValues(connection, dcrSysId,
							dcrRequest, targetBASysId, transmittalType
									.getTrnProcessId(), formattedDtnNumber,
							dtnSysPrefix, transmittalRequest, aParamTable,
							tpParams.get(TransmittalUtils.CALENDAR), false);

					runTransmittalRules(connection, transmittalRequest,
							BusinessArea.lookupBySystemId(targetBASysId),
							dcrBA, dcrRequest, aParamTable, transmittalType,
							tpParams, baType, false);

					// Set notify based on the input from transmittal wizard
					setNotifyField(tpParams, aParamTable);

					try {
						updateRequest.updateRequest(connection, tBitsResMgr,
								aParamTable);
					} catch (APIException e) {
						e.printStackTrace();
						e.addException(new TBitsException(
								"The following exceptions occurred while updating target "
										+ "business area: "
										+ currentBA.getSystemPrefix()));
						LOG.error(e);
						throw e;
					}
				}
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder();

				message
						.append(
								"An exception occurred while retrieving a drawing with matching drawing number: ")
						.append(dNumValue).append(
								" for Business area: "
										+ currentBA.getSystemPrefix());

				LOG.error(message.toString(), sqle);

				throw new DatabaseException(message.toString(), sqle);
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				LOG.error("Could not update BA: " + currentBA.getSystemPrefix()
						+ "\n" + npe.getMessage(), npe);
				throw new TBitsException(
						"Could not update BA: " + currentBA.getSystemPrefix()
								+ "\n" + npe.getMessage(), npe);
			}

			if (!isMatched) {

				LOG.info("No matching request exist, hence adding new...... "
						+ i);
				// updateFields(connection, dcrSysId, dcrRequest, targetBASysId,
				// delAttachments, sAttachments, aParamTable);
				try {
					AddRequest addRequest = new AddRequest();
					addRequest.setContext(contextPath);
					addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

					aParamTable.put(Field.BUSINESS_AREA, currentBA
							.getSystemPrefix());
					aParamTable.put(Field.SUBJECT, dcrRequest.getSubject());
					aParamTable.put(Field.LASTUPDATED_DATE, lastUpdatedDate);
					aParamTable.put(Field.LOGGED_DATE, lastUpdatedDate);

					HashMap<String, Collection<AttachmentInfo>> selectedAttachments = allAttachments
							.get(dcrRequest.getRequestId());
					if (selectedAttachments != null) {
						for (String fieldName : deliverableNames.split(",")) {
							Collection<AttachmentInfo> tempAttachments = selectedAttachments
									.get(fieldName);
							if (tempAttachments != null) {
								Field targetAttField = srcTargetFieldMap
										.get(fieldName);
								String delAttachments = "[]";
								if (targetAttField != null) {
									delAttachments = AttachmentInfo
											.toJson(tempAttachments);
									aParamTable.put(targetAttField.getName(),
											delAttachments);
								} else
									throw new TBitsException(
											"A matching target deliverable field id, for deliverable field: "
													+ fieldName
													+ "could not be found,"
													+ " for Business Area: "
													+ currentBA
															.getSystemPrefix());
							}
						}
					}

					// Update universal_id from src BA.
					if (targetUIDField != null)
						aParamTable.put(UNIVERSAL_ID, srcUID + "");
					// Finally add the httpRequest
					updateFields(connection, transmittalType.getTrnProcessId(),
							dcrSysId, dcrRequest, targetBASysId, aParamTable);

					updatePostTransmittalFieldValues(connection, dcrSysId,
							dcrRequest, targetBASysId, transmittalType
									.getTrnProcessId(), formattedDtnNumber,
							dtnSysPrefix, transmittalRequest, aParamTable,
							tpParams.get(TransmittalUtils.CALENDAR), true);

					runTransmittalRules(connection, transmittalRequest,
							currentBA, dcrBA, dcrRequest, aParamTable,
							transmittalType, tpParams, baType, true);

					// aParamTable.put(Field.NOTIFY, "false");
					// Set notify based on transmittal wizard input.
					setNotifyField(tpParams, aParamTable);

					try {
						addRequest.addRequest(connection, tBitsResMgr,
								aParamTable);
					} catch (APIException e) {
						e.printStackTrace();
						e.addException(new TBitsException(
								"The following exceptions occurred while updating target business area"
										+ ": " + currentBA.getSystemPrefix()));
						LOG.error(e);
						throw e;
					}
					isMatched = false;
				} catch (NullPointerException npe) {
					npe.printStackTrace();
					LOG.error("Could not update BA: "
							+ currentBA.getSystemPrefix() + "\n"
							+ npe.getMessage(), npe);
					throw new TBitsException("Could not update BA: "
							+ currentBA.getSystemPrefix() + "\n"
							+ npe.getMessage(), npe);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Could not update BA: "
							+ currentBA.getSystemPrefix() + "\n"
							+ e.getMessage(), e);
					throw new TBitsException("Could not update BA: "
							+ currentBA.getSystemPrefix() + "\n"
							+ e.getMessage(), e);
				}
			}
		}
	}

	private Request getMatchingRequestInTargetBusinessArea(
			Connection connection, int targetBASysId, int targetFieldId,
			String drawingNumber) throws SQLException, DatabaseException {
		// String matchDrawingByDrawingNumberQuery =
		// "SELECT request_id from requests_ex where sys_id=? " +
		// "and field_id=? and varchar_value=?";
		Request matchingRequest = null;
		String matchDrawingByDrawingNumberQuery = "select r.* from requests r join requests_ex r_ex "
				+ "on r.sys_id=r_ex.sys_id and r.request_id = r_ex.request_id "
				+ "where r_ex.sys_id=?  and r_ex.field_id=? and r_ex.varchar_value=?";

		PreparedStatement cs = connection
				.prepareStatement(matchDrawingByDrawingNumberQuery);
		cs.setInt(1, targetBASysId);
		cs.setInt(2, targetFieldId);
		cs.setString(3, drawingNumber);
		ResultSet reqIdRS = cs.executeQuery();
		if ((reqIdRS != null) && (reqIdRS.next()))
			matchingRequest = Request.createFromResultSet(reqIdRS);
		return matchingRequest;
	}

	private Request getMatchingRequestInTargetBusinessArea(
			Connection connection, int targetBASysId, int targetFieldId, int uID)
			throws SQLException, DatabaseException {
		// String matchDrawingByDrawingNumberQuery =
		// "SELECT request_id from requests_ex where sys_id=? " +
		// "and field_id=? and varchar_value=?";
		Request matchingRequest = null;
		String matchDrawingByDrawingNumberQuery = "select r.* from requests r join requests_ex r_ex "
				+ "on r.sys_id=r_ex.sys_id and r.request_id = r_ex.request_id "
				+ "where r_ex.sys_id=?  and r_ex.field_id=? and r_ex.int_value=?";

		PreparedStatement cs = connection
				.prepareStatement(matchDrawingByDrawingNumberQuery);
		cs.setInt(1, targetBASysId);
		cs.setInt(2, targetFieldId);
		cs.setInt(3, uID);
		ResultSet reqIdRS = cs.executeQuery();
		if ((reqIdRS != null) && (reqIdRS.next()))
			matchingRequest = Request.createFromResultSet(reqIdRS);
		return matchingRequest;
	}

	private void updateFields(Connection connection, int trnProcessId,
			int dcrSystemId, Request dcrRequest, int targetSystemId,
			Hashtable<String, String> aParamTable) throws DatabaseException {
		Hashtable<String, String> targetBAFields = TransmittalUtils
				.getTargetBusinessAreaFieldsByTrnProcessIdAndSrcFieldId(
						connection, trnProcessId, dcrSystemId, targetSystemId);
		for (String fieldName : targetBAFields.keySet()) {
			String tFieldNameList = targetBAFields.get(fieldName);
			String dcrFieldValue = dcrRequest.get(fieldName);
			if ((tFieldNameList != null) && (!tFieldNameList.trim().equals(""))
					&& (dcrFieldValue != null)) {
				for (String tFieldName : tFieldNameList.split(","))
					if ((tFieldName != null) && (!tFieldName.trim().equals("")))
						aParamTable.put(tFieldName, dcrFieldValue);
			}
		}
	}

	private void updatePostTransmittalFieldValues(Connection connection,
			int dcrSystemId, Request dcrRequest, int targetSystemId,
			int transmittalProcessId, String formattedDTNNumber,
			String dtnSysPrefix, Request trnRequest,
			Hashtable<String, String> aParamTable, String calendarName,
			boolean isAddRequest) throws DatabaseException, TBitsException {
		TransmittalUtils.getTargetBusinessAreaFieldsAndValues(connection,
				dcrRequest, targetSystemId, transmittalProcessId,
				formattedDTNNumber, dtnSysPrefix, trnRequest, isAddRequest,
				aParamTable, calendarName);
	}

	/**
	 * @param connection
	 * @param maxIdConn
	 * @param tBitsResMgr
	 */
	private void rollbackAllOperations(Connection connection,
			Connection maxIdConn, TBitsResourceManager tBitsResMgr) {
		try {
			if (connection != null) {
				connection.rollback();
			}
			if (maxIdConn != null) {
				maxIdConn.rollback();
			}

			tBitsResMgr.rollback();

		} catch (SQLException e1) {
			e1.printStackTrace();
			LOG
					.error(
							"Error occurred while rolling back of transmittal process.",
							e1);
		}
	}

	private static String getDTNAttachment(BirtTemplateHelper kth,
			String templateName, String outputFileName, String attachmentHint)
			throws DatabaseException, FileNotFoundException, IOException,
			EngineException, TBitsException {

		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
		String pdfFilePath = "";
		try {
			pdfFilePath = TransmittalUtils.generateTransmittalNoteUsingBirt(
					templateName, kth, outputFileName);
		} catch (BirtException e) {
			e.printStackTrace();
		}
		File pdfFile = new File(pdfFilePath);
		Uploader uploader = new Uploader();
		uploader.setFolderHint(attachmentHint);
		AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
		trnAttCollection.add(trnNoteInfo);
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();
	}

	private static String getDTNAttachmentWithGivenFormat(
			BirtTemplateHelper kth, ArrayList<TemplateTuple> templates,
			String outputFileName, String attachmentHint)
			throws DatabaseException, FileNotFoundException, IOException,
			EngineException, TBitsException {

		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
		String FilePath = "";
		for (TemplateTuple eachtemplateName : templates) {
			try {
				FilePath = TransmittalUtils
						.generateTransmittalNoteUsingBirtForGivenFormat(
								eachtemplateName.getDocname(), kth,
								outputFileName, eachtemplateName.getFormat());
			} catch (BirtException e) {
				e.printStackTrace();
			}
			File pdfFile = new File(FilePath);
			Uploader uploader = new Uploader();
			uploader.setFolderHint(attachmentHint);
			AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
			trnAttCollection.add(trnNoteInfo);
		}
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();
	}

	@SuppressWarnings("unused")
	private static ArrayList<Integer> getTargetBusinessAreas(
			Connection connection, int dcrSystemId, int trnProcessId)
			throws DatabaseException {

		ArrayList<Integer> targetSysIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT DISTINCT target_sys_id FROM "
							+ TransmittalUtils.TRN_SRC_TARGET_FIELD_MAPPING_TABLE
							+ " where trn_process_id=? and src_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while (rs.next()) {
					targetSysIdList.add(rs.getInt(TARGET_SYS_ID));
				}
		} catch (SQLException sqle) {
			throw new DatabaseException(
					"Error while retrieving target business area ids.", sqle);
		}
		return targetSysIdList;
	}

	private static ArrayList<Integer> getTargetBusinessAreas(
			Connection connection, int trnProcessId) throws DatabaseException {

		ArrayList<Integer> targetSysIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT DISTINCT target_sys_id FROM "
							+ TransmittalUtils.TRN_SRC_TARGET_FIELD_MAPPING_TABLE
							+ " where trn_process_id=?");
			ps.setInt(1, trnProcessId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while (rs.next()) {
					targetSysIdList.add(rs.getInt(TARGET_SYS_ID));
				}
		} catch (SQLException sqle) {
			LOG.error("Error while retrieving target business area ids.", sqle);
			throw new DatabaseException(
					"Error while retrieving target business area ids.", sqle);
		}
		return targetSysIdList;
	}

	/**
	 * This runs ITransmittalRule based plugins which are run just before, a
	 * request is added to DTN BA, update source BA or add/update target BA
	 * requests.
	 * 
	 * @param businessAreaType
	 * @param aRequest
	 * @param aResponse
	 * @param tagTable
	 * @throws TBitsException
	 */
	@SuppressWarnings("unchecked")
	private void runTransmittalRules(Connection connection,
			Request transmittalRequest, BusinessArea currentBA,
			BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable,
			TransmittalProcess transmittalType,
			HashMap<String, String> transmittalParams, int businessAreaType,
			boolean isAddRequest) throws TBitsException {
		PluginManager pm = PluginManager.getInstance();
		ArrayList<Class> transmittalRuleClasses = pm
				.findPluginsByInterface(ITransmittalRule.class.getName());
		ArrayList<ITransmittalRule> transmittalRuleLoaders = new ArrayList<ITransmittalRule>();
		if (transmittalRuleClasses != null) {
			for (Class transmittalRuleClass : transmittalRuleClasses) {
				ITransmittalRule transmittalRule;
				try {
					transmittalRule = (ITransmittalRule) transmittalRuleClass
							.newInstance();
					transmittalRuleLoaders.add(transmittalRule);
				} catch (InstantiationException e) {
					LOG.error("Could not instantiate the pre renderer class: "
							+ transmittalRuleClass.getClass().getName());
					throw new TBitsException(
							"Could not instantiate the pre renderer class: "
									+ transmittalRuleClass.getClass().getName(),
							e);
				} catch (IllegalAccessException e) {
					LOG.error("Could not access the renderer class: "
							+ transmittalRuleClass.getClass().getName());
					throw new TBitsException(
							"Could not instantiate the pre renderer class: "
									+ transmittalRuleClass.getClass().getName(),
							e);
				}
			}
		}

		Comparator<ITransmittalRule> c = new Comparator<ITransmittalRule>() {

			public int compare(ITransmittalRule arg0, ITransmittalRule arg1) {
				double diff = arg0.getSequence() - arg1.getSequence();
				if (diff > 0)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}
		};
		Collections.sort(transmittalRuleLoaders, c);

		for (ITransmittalRule trnRuleLoader : transmittalRuleLoaders) {
			trnRuleLoader.process(connection, transmittalRequest, currentBA,
					dcrBA, dcrRequest, paramTable, transmittalType,
					transmittalParams, businessAreaType, isAddRequest);
		}
	}

	/**
	 * @param connection
	 * @param dtnSysId
	 * @param dtnDelFieldIdStr
	 * @return
	 * @throws TBitsException
	 * @throws NumberFormatException
	 * @throws DatabaseException
	 */
	private static Field getAttachmentFieldById(Connection connection,
			int dtnSysId, String dtnDelFieldIdStr, String noFieldIdProvidedMsg,
			String dtnFieldIdInvalidMsg) throws TBitsException,
			NumberFormatException, DatabaseException {
		Field dtnDeliverableField = null;
		if ((dtnDelFieldIdStr == null) || (dtnDelFieldIdStr.trim().equals(""))) {
			throw new TBitsException(noFieldIdProvidedMsg);
		} else {
			int dtnDelFieldId = Integer.parseInt(dtnDelFieldIdStr);
			dtnDeliverableField = Field.lookupBySystemIdAndFieldId(dtnSysId,
					dtnDelFieldId);
			if (dtnDeliverableField == null)
				throw new TBitsException(dtnFieldIdInvalidMsg
						+ dtnDelFieldIdStr);
		}
		return dtnDeliverableField;
	}

	public static void main(String[] args) throws TBitsException,
			DatabaseException, APIException, SQLException {
		// for (int i = 1 ; i < 3 ;i++ ){
		// RunnableThread thread3 = null;
		// if (i == 1)
		// thread3 = new RunnableThread(i, "Transmittal: " + i, 38);
		// else if (i == 2)
		// thread3 = new RunnableThread(i, "Transmittal: " + i, 31);
		// }

		// Connection connection = null;
		// connection = DataSourcePool.getConnection();
		// try{
		// GenericTransmittalCreator.deleteApprovalTransientDataAttachments(connection
		// , 95, 2);
		// }finally{
		// if (connection != null)
		// connection.close();
		// }

		// }
		long startTime = System.currentTimeMillis();
		GenericTransmittalCreator gtc = new GenericTransmittalCreator();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			TBitsResourceManager tBitsResMgr = new TBitsResourceManager();
			HashMap<String, String> paramTable = new HashMap<String, String>();
			paramTable.put(GenericTransmittalCreator.TRN_PROCESS_ID_PARAM,
					27 + "");
			paramTable.put(GenericTransmittalCreator.REQUEST_LIST, 1 + "2");
			paramTable.put("transmittalDate", "2010-10-01");
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();
			System.out.println("Finished trasmittal: "
					+ gtc.createTransmittal(connection, tBitsResMgr,
							paramTable, allAttachments));
			connection.commit();
		} catch (TBitsException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (APIException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			System.out.println("DONE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "
					+ ", Time: " + (System.currentTimeMillis() - startTime)
					/ 1000);
		}

	}

	class RunnableThread implements Runnable {

		Thread runner;
		int requestId;
		int trnProcessId;

		public RunnableThread() {
		}

		public RunnableThread(int requestId, String threadName) {
			runner = new Thread(this, threadName);
			this.requestId = requestId;
			System.out.println(runner.getName());
			runner.start();
		}

		public RunnableThread(int requestId, String threadName, int trnProcessId) {
			runner = new Thread(this, threadName);
			this.requestId = requestId;
			this.trnProcessId = trnProcessId;
			System.out.println(runner.getName());
			runner.start();
		}

		public void run() {

			long startTime = System.currentTimeMillis();
			System.out.println(Thread.currentThread());
			GenericTransmittalCreator gtc = new GenericTransmittalCreator();
			Connection connection = null;
			try {
				connection = DataSourcePool.getConnection();
				connection.setAutoCommit(false);
				TBitsResourceManager tBitsResMgr = new TBitsResourceManager();
				HashMap<String, String> paramTable = new HashMap<String, String>();
				paramTable.put(GenericTransmittalCreator.TRN_PROCESS_ID_PARAM,
						trnProcessId + "");
				paramTable.put(GenericTransmittalCreator.REQUEST_LIST,
						requestId + "");
				paramTable.put(TransmittalUtils.TRANSMITTAL_ID_PREFIX,
						requestId + "");
				paramTable.put("transmittalDate", "2010-10-01");
				HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();
				System.out.println("Finished trasmittal: "
						+ gtc.createTransmittal(connection, tBitsResMgr,
								paramTable, allAttachments));
				connection.commit();
			} catch (TBitsException e) {
				e.printStackTrace();
			} catch (DatabaseException e) {
				e.printStackTrace();
			} catch (APIException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (connection != null)
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				System.out.println("DONE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "
						+ requestId + ", Time: "
						+ (System.currentTimeMillis() - startTime) / 1000);
			}
		}
	}

	/**
	 * Returns the Json array for the attachment list
	 * 
	 * @param allAttachments
	 * 
	 * @param isMergeAllAttachments
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String[]> getSelectedAttachmentslist(
			ArrayList<Request> req,
			String sysPrefix,
			ArrayList<TbitsModelData> tmd,
			int sysid,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments) {

		ArrayList<String[]> arr = new ArrayList<String[]>();
		int count = 0;
		for (Request request : req) {

			try {
				arr.add(setJsonFieldValue(request, tmd, sysid, allAttachments));
			} catch (TbitsExceptionClient e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return arr;
	}

	/**
	 * Methods used to form json array for attachment list
	 * 
	 * @param trd
	 * @param allAttachments
	 * @return
	 * @throws TbitsExceptionClient
	 */
	@SuppressWarnings("unchecked")
	private String[] setJsonFieldValue(
			Request trd,
			ArrayList<TbitsModelData> attachmentSelectionColumns1,
			int aSystemId,
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> allAttachments)
			throws TbitsExceptionClient {

		String[] arr = new String[4];

		int reqId = trd.getRequestId();
		arr[1] = (String) trd.get("subject");

		int count1 = 2;
		ArrayList<TbitsModelData> attachmentSelectionColumns = attachmentSelectionColumns1;

		if (attachmentSelectionColumns != null) {
			for (TbitsModelData tmd : attachmentSelectionColumns) {

				Integer fieldId = (Integer) tmd.get("field_id");
				Integer data_id = (Integer) tmd.get("data_type_id");
				if (fieldId > 0) {

					if (11 == data_id) {

						Field field = null;
						try {
							field = Field.lookupBySystemIdAndFieldId(aSystemId,
									fieldId);
						} catch (DatabaseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String property = field.getName();

						Collection<AttachmentInfo> atts = allAttachments.get(
								reqId).get(property);
						/*
						 * POJO obj = trd.getAsPOJO(property);
						 * 
						 * if (obj == null || !(obj instanceof POJOAttachment))
						 * { obj = new POJOAttachment( new
						 * ArrayList<FileClient>()); }
						 * 
						 * List<FileClient> delAttachments = ((POJOAttachment)
						 * obj) .getValue();
						 */
						if (atts != null) {
							if (atts.size() == 0) {

								arr[count1] = "";
								++count1;
							} else {
								String attString = "";
								for (AttachmentInfo eachAttachMent : atts) {

									attString = eachAttachMent.getName() + "\n";

								}

								arr[count1] = attString;
								++count1;
							}

						} else {
							arr[count1] = "";
							++count1;
						}
					}

				}
			}
		}

		return arr;

	}

}
