package transmittal.com.tbitsGlobal.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.birt.report.engine.api.EngineException;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.client.TransmittalConstants;
import transmittal.com.tbitsGlobal.client.TransmittalService;
import transmittal.com.tbitsGlobal.client.models.AttachmentModel;
import transmittal.com.tbitsGlobal.client.models.Attachmentinfo;
import transmittal.com.tbitsGlobal.client.models.DrawinglistModel;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gwt.user.client.Window;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class TransmittalServiceImpl extends TbitsRemoteServiceServlet implements
		TransmittalService {
	// private static final String TRANSIENT_DATA_APPROVAL_ROLE_NAME_QUERY =
	// "SELECT * FROM trn_approval_cycle_transient_data " +
	// "WHERE sys_id=? and request_id=? and parameter=?";

	public static final String KEYFIELD = "keyfield";

	public static final String KEYFIELD_LIST = "keyfieldList";

	private static final String MAP_OF_REQUESTS = "mapOfRequests";

	private static final String TRN_EXTENDED_FIELDS = "trnExtendedFields";

	private static final String ATTACHMENT_TABLE_COLUMN_LIST = "attachmentTableColumnList";

	private static final String DISTRIBUTION_TABLE_COLUMNS_LIST = "distributionTableColumnsList";

	private static final String VALIDATION_RULES_LIST = "validationRulesList";

	private static final String TRANSMITTAL_PROCESS_PARAMS = "transmittalProcessParams";

	private static final String DROP_DOWN_ID = "dropDownId";

	private static final String PROCESS_ID = "processId";

	private static final String TRANSIENT_DATA_APPROVAL_ROLE_NAME_QUERY = "SELECT value FROM trn_process_parameters "
			+ "WHERE trn_process_id=(CONVERT (INT, CONVERT (VARCHAR, (SELECT value FROM trn_approval_cycle_transient_data "
			+ "WHERE sys_id=? AND request_id=? AND parameter=?)))) AND parameter=?";

	private static final String ATTACHMENTS = "attachments";

	private static final String FIELD_NAME = "fieldName";

	private static final String VALUE = "value";

	private static final String PARAMETER = "parameter";

	private static final String REQUEST_ID = "request_id";

	private static final String SYS_ID = "sys_id";

	private static final String TYPE_VALUE_SOURCE = "type_value_source";

	private static final String FIELD_CONFIG = "field_config";

	private static final String CC_LIST = "ccList";

	private static final String TO_LIST = "toList";

	private static final long serialVersionUID = 1L;

	protected static final String IS_ACTIVE_COLUMN = "is_active";

	protected static final String IS_EDITABLE_COLUMN = "is_editable";

	protected static final String DEFAULT_VALUE_COLUMN = "default_value";

	protected static final String DATA_TYPE_ID_COLUMN = "data_type_id";

	protected static final String FIELD_ID_COLUMN = "field_id";

	protected static final String NAME_COLUMN = "name";

	protected static final String TRN_PROCESS_ID_COLUMN = "trn_process_id";

	protected static final String COLUMN_ORDER = "column_order";

	protected static final String DISPLAY_NAME_COLUMN = "display_name";

	private static final String IS_INCLUDED = "is_included";

	protected static final String FIELD_ORDER = "field_order";

	protected static final String IS_DTN_NUMBER_PART = "is_dtn_number_part";

	private static final String SRC_REQUEST_ID = "src_request_id";

	public static final TBitsLogger LOG = TBitsLogger.getLogger("Transmittal");

	public void getTransmittalForm(String requestIds, String selectedDTNProcess) {
		System.out.println("Testing transmittal.");
	}

	public ArrayList<TbitsModelData> getTransmittalDropDownOptions(int aSystemId)
			throws TbitsExceptionClient {
		ArrayList<TbitsModelData> tmdList = new ArrayList<TbitsModelData>();
		ArrayList<TransmittalDropDownOption> ddOptionList;
		try {
			ddOptionList = TransmittalDropDownOption
					.lookupTransmittalDropDownOptionsBySystemId(aSystemId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while retrieving transmital types.");
		}
		if (ddOptionList != null) {
			for (TransmittalDropDownOption ddOption : ddOptionList) {
				TbitsModelData tmd = new TbitsModelData();
				tmd.set("trnDropDownId", String.valueOf(ddOption.getId()));
				tmd.set("name", ddOption.getName());
				tmd.set("dcrSystemId", String
						.valueOf(ddOption.getDcrSystemId()));
				tmd.set("sortOrder", String.valueOf(ddOption.getSortOrder()));
				tmdList.add(tmd);
			}

			Comparator<TbitsModelData> c = new Comparator<TbitsModelData>() {
				public int compare(TbitsModelData arg0, TbitsModelData arg1) {
					int diff = Integer.parseInt((String) arg0.get("sortOrder"))
							- Integer.parseInt((String) arg1.get("sortOrder"));
					if (diff > 0)
						return 1;
					else if (diff == 0)
						return 0;
					else
						return -1;
				}
			};
			Collections.sort((List<TbitsModelData>) tmdList, c);
		}
		return tmdList;
	}

	public ArrayList<String> getDCRBusinessAreas() throws TbitsExceptionClient {
		ArrayList<String> dcrBAList = new ArrayList<String>();
		try {
			ArrayList<Integer> dcrSysIds = TransmittalUtils
					.lookupAllDCRBusinessAreaIds();
			if (dcrSysIds != null)
				for (Integer dcrSysId : dcrSysIds) {
					BusinessArea ba = BusinessArea.lookupBySystemId(dcrSysId);
					if (ba != null) {
						dcrBAList.add(ba.getSystemPrefix());
					}
				}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while retrieving DCR business areas.");
		}
		return dcrBAList;
	}

	public TbitsModelData getTransmittalProcessParameters(int dcrSystemId,
			ArrayList<Integer> dcrRequestList, int transmittalDropDownId)
			throws TbitsExceptionClient {

		String RoleNameForDtnDate = "HistoricalDateDTN";
		TbitsModelData tpMD = new TbitsModelData();
		TransmittalProcess selectedTransmittalProcess = null;
		ArrayList<Request> dcrRequests = new ArrayList<Request>();
		BusinessArea dcrBA = null;
		Connection connection = null;
		try {
			dcrBA = BusinessArea.lookupBySystemId(dcrSystemId);
			ArrayList<TransmittalProcess> ttList = TransmittalProcess
					.lookupTransmittalTypeBySystemIdAndtrnDropDownId(
							dcrSystemId, transmittalDropDownId);
			if ((ttList != null) && (!ttList.isEmpty())) {
				ttList.trimToSize();
				selectedTransmittalProcess = ttList.get(0);
			} else
				throw new TbitsExceptionClient("Invalid transmittal type.");

			for (Integer requestId : dcrRequestList) {
				Request request = Request.lookupBySystemIdAndRequestId(
						dcrSystemId, requestId);
				dcrRequests.add(request);
			}
			dcrRequests.trimToSize();

			// Run controller plugin here and get the appropriate transmittal
			// process
			// If only one transmittal process exists, no need to run controller
			// to choose a transmittalTypeCombo
			// if (ttList.size() > 1){
			connection = DataSourcePool.getConnection();
			TransmittalDropDownOption ntp = TransmittalDropDownOption
					.lookupTransmittalDropDownBySystemIdAndDropdownId(
							dcrSystemId, transmittalDropDownId);
			runIPreControllers(connection, dcrBA, dcrRequests, ntp, ttList,
					selectedTransmittalProcess);
			// }
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while getting database connection.", sqle);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Could not parse one of the DCR request ids");
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving DCR requests.");
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e.getDescription(), e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(
							"Error occurred while closing database connection.",
							sqle);
				}
		}
		if (selectedTransmittalProcess == null) {
			throw new TbitsExceptionClient(
					"Invalid transmittal process type/no process exists for the  transmittal drop down id: "
							+ transmittalDropDownId);
		}

		try {
			Hashtable<String, String> tpp = TransmittalProcess
					.getTransmittalProcessParameters(dcrSystemId,
							selectedTransmittalProcess.getTrnProcessId());
			if (tpp != null)
				for (String property : tpp.keySet()) {
					String value = tpp.get(property);
					tpMD.set(property, value);
				}
			tpMD.set("trnProcessId", selectedTransmittalProcess
					.getTrnProcessId());
			tpMD.set("transmittalSerialKey", selectedTransmittalProcess
					.getTrnMaxSnKey());
			String roleName = tpMD.get("approvalRoleName");
			String statusOfFinishButton = "";

			Boolean statusOffinishButton = this.checkUserExistsInRole(
					dcrSystemId, roleName, getCurrentUser().getUserLogin());

			statusOfFinishButton = String.valueOf(statusOffinishButton);

			tpMD.set("isExistInRole", statusOfFinishButton);
			if (tpMD.get(KEYFIELD) != null) {

				ArrayList<TbitsModelData> keyFieldList = getTransmittalOptionForkeyFields(
						dcrSystemId, selectedTransmittalProcess
								.getTrnProcessId(), Integer
								.parseInt((String) tpMD.get(KEYFIELD)));
				tpMD.set(KEYFIELD, tpMD.get(KEYFIELD));
				tpMD.set(KEYFIELD_LIST, keyFieldList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while retrieving tramsittal type with name: "
							+ transmittalDropDownId);
		}

		return tpMD;
	}

	/*
	 * This methods runs various preLoader. It essentials is run just before
	 * rendering.
	 * 
	 * @param businessAreaType
	 * 
	 * @param aRequest
	 * 
	 * @param aResponse
	 * 
	 * @param tagTable
	 * 
	 * @throws TBitsException
	 */

	@SuppressWarnings("unchecked")
	private void runIPreControllers(Connection connection, BusinessArea dcrBA,
			ArrayList<Request> dcrRequestList, TransmittalDropDownOption ntp,
			ArrayList<TransmittalProcess> transmittalTypes,
			TransmittalProcess transmittalType) throws TBitsException {
		PluginManager pm = PluginManager.getInstance();
		ArrayList<Class> transmittalRuleClasses = pm
				.findPluginsByInterface(ITransmittalController.class.getName());
		ArrayList<ITransmittalController> preTransmittalRuleLoaders = new ArrayList<ITransmittalController>();
		if (transmittalRuleClasses != null) {
			for (Class preTransmittalRuleClass : transmittalRuleClasses) {
				ITransmittalController preTransmittalRule;
				try {
					preTransmittalRule = (ITransmittalController) preTransmittalRuleClass
							.newInstance();
					preTransmittalRuleLoaders.add(preTransmittalRule);
				} catch (InstantiationException e) {
					LOG
							.error("Could not instantiate the pre controller class: "
									+ preTransmittalRuleClass.getClass()
											.getName());
				} catch (IllegalAccessException e) {
					LOG.error("Could not access the controller class: "
							+ preTransmittalRuleClass.getClass().getName());
				} catch (Exception e) {
					throw new TBitsException(e);
				}
			}
		}

		Comparator<ITransmittalController> c = new Comparator<ITransmittalController>() {

			public int compare(ITransmittalController arg0,
					ITransmittalController arg1) {
				double diff = arg0.getSequence() - arg1.getSequence();
				if (diff > 0)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}
		};
		Collections.sort(preTransmittalRuleLoaders, c);

		for (ITransmittalController trnPreRuleLoader : preTransmittalRuleLoaders) {
			trnPreRuleLoader.process(connection, dcrBA, dcrRequestList, ntp,
					transmittalTypes, transmittalType);
		}
	}

	public Integer getIntegerValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHTMLTransmittalPreviewUsingBirt(
			HashMap<String, String> paramTable) throws TbitsExceptionClient {
		String htmlString = "";
		int transmittalMaxId = -1;
		String dtnNumber = paramTable
				.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
		String actualNumber = paramTable.get(TransmittalUtils.ACTUAL_NUMBER);

		if ((actualNumber != null) && (!actualNumber.trim().equals(""))) {
			dtnNumber = actualNumber;
		}

		String dtnTemplateDate = "";
		String actualDate = paramTable.get(TransmittalUtils.ACTUAL_DATE);
		try {
			dtnTemplateDate = TransmittalUtils.getDTNTemplateDate(actualDate);
			paramTable.put(TransmittalUtils.DTN_TEMPLATE_DATE, dtnTemplateDate);
			paramTable.put(TransmittalUtils.ACTUAL_DATE, dtnTemplateDate);

			if (dtnNumber == null)
				throw new TbitsExceptionClient(
						"Could not find \"transmittal_id_prefix\" in the transmittal process parameters.");

			if (dtnNumber.contains("{Likely}")) {
				int lIndex = dtnNumber.lastIndexOf("-");
				if (lIndex > 0) {
					dtnNumber = dtnNumber.substring(0, (lIndex + 1));
				}
			}
			if ((actualNumber == null) || (actualNumber.trim().equals(""))) {
				String dtnSerialKey = paramTable.get("transmittalSerialKey");
				// Fetch the max transmittal id and increment it by 1.
				transmittalMaxId = TransmittalUtils
						.getTransmittalMaxId(dtnSerialKey) + 1;
				if (transmittalMaxId != -1)
					dtnNumber = dtnNumber
							+ TransmittalUtils
									.getFormattedStringFromNumber(transmittalMaxId);
				else
					dtnNumber = dtnNumber + "XXXX";

				dtnNumber = dtnNumber + "{Likely}";
			}
		} catch (DatabaseException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while generating transmittal template. "
							+ e1.getMessage(), e1);
		} catch (TBitsException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while generating transmittal template. "
							+ e1.getMessage(), e1);
		}

		paramTable.put(TransmittalUtils.TRANSMITTAL_ID_PREFIX, dtnNumber);
		String toDisplayNameList = "";
		String ccDisplayNameList = "";
		HashMap<String, String> kAttnInfoMap = new HashMap<String, String>();
		TransmittalUtils.prefillWithUserColumns(kAttnInfoMap);
		try {
			String toList = paramTable.get(TO_LIST);
			String ccList = paramTable.get(CC_LIST);

			if (toList != null) {
				TransmittalUtils.getKindAttentionUserInfo(paramTable,
						kAttnInfoMap, toList);
				toDisplayNameList = TransmittalUtils
						.getUserDisplayNameList(toList);
				paramTable.put(TO_LIST, toDisplayNameList);
			}
			if (ccList != null) {
				ccDisplayNameList = TransmittalUtils
						.getUserDisplayNameList(ccList);
				paramTable.put(CC_LIST, ccDisplayNameList);
			}
		} catch (DatabaseException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while fetching display names of users in To/Cc list.",
					e1);
		}

		String drawingsTable = paramTable.get("drawingTable");
		ArrayList<String[]> drawingsList = TransmittalUtils
				.fetchDrawingListFromJsonArray(drawingsTable);// function
		// changed
		// slightly to
		// remove added
		// fields

		String distributionTable = paramTable.get("distributionTable");
		ArrayList<String[]> distributionList = TransmittalUtils
				.fetchDistributionListFromJsonArray(distributionTable);

		String selectedAttachmentTable = paramTable
				.get("SelectedAttachmentsTable");
		ArrayList<String[]> selectedAttachmentList = TransmittalUtils
				.fetchArrayListFromJsonArray(selectedAttachmentTable);

		HashMap<String, String> loggerInfo = null;

		String trnProcessId = paramTable.get("trnProcessId");
		String nullSignatoryAllow = TransmittalUtils.getDtnSignatory(Integer
				.valueOf(trnProcessId));

		/*
		 * Check if the value of dtnSignatory is allowed to be null or not
		 */
		if ((null == nullSignatoryAllow)
				|| (nullSignatoryAllow.trim().equals(""))) {
			try {
				String dtnSignatory = paramTable
						.get(TransmittalUtils.DTN_SIGNATORY);
				if ((dtnSignatory == null) || (dtnSignatory.trim().equals(""))) {
					String userLogin = paramTable.get("user");
					if (userLogin != null) {
						User user = User.lookupByUserLogin(userLogin);
						if (user == null)
							throw new TbitsExceptionClient(
									"No DTN signatory information provided "
											+ "in transmittal process parameters or is invalid.");
						else
							loggerInfo = TransmittalUtils.getUserInfoMap(user);
					} else
						throw new TbitsExceptionClient(
								"No DTN signatory information provided "
										+ "in transmittal process parameters or is invalid.");
				} else {
					User dtnSigUser = User.lookupByUserLogin(dtnSignatory);
					if (dtnSigUser == null)
						throw new TbitsExceptionClient(
								"DTN Signatory provided in transmittal process parameters does not "
										+ "exist as a user in tBits or is invalid: "
										+ dtnSignatory);
					else
						loggerInfo = TransmittalUtils
								.getUserInfoMap(dtnSigUser);
				}
			} catch (DatabaseException e1) {
				e1.printStackTrace();
				throw new TbitsExceptionClient(
						"Database error occurred while DTN signatory information.",
						e1);
			}
		}
		// ----------------When a valid value of dtn signatory is
		// present-----------------------------//
		else if ((null != nullSignatoryAllow)
				&& (!nullSignatoryAllow.equals("-1"))) {
			String dtnSignatory = paramTable
					.get(TransmittalUtils.DTN_SIGNATORY);
			User dtnSigUser;
			try {

				dtnSigUser = User.lookupByUserLogin(dtnSignatory);
				if (dtnSigUser == null)
					throw new TbitsExceptionClient(
							"DTN Signatory provided in transmittal process parameters does not "
									+ "exist as a user in tBits or is invalid: "
									+ dtnSignatory);
				else
					loggerInfo = TransmittalUtils.getUserInfoMap(dtnSigUser);
			} catch (DatabaseException e) {
				throw new TbitsExceptionClient(
						"Database error occurred while DTN signatory information.",
						e);
			}
		}
		// ----------------Requirement for IB(Allow empty dtnSignatory
		// field--------------------------//
		else if ((null != nullSignatoryAllow)
				&& (nullSignatoryAllow.trim().equals("-1"))) {
			try {
				String dtnSignatory = paramTable
						.get(TransmittalUtils.DTN_SIGNATORY);
				if ((null != dtnSignatory) && (!dtnSignatory.trim().equals(""))) {
					User dtnSigUser = User.lookupByUserLogin(dtnSignatory);
					if (null != dtnSigUser) {
						loggerInfo = TransmittalUtils
								.getUserInfoMap(dtnSigUser);
					}
				} else {
					String userLogin = paramTable.get("user");
					if ((!userLogin.trim().equals("")) || (null != userLogin)) {
						User user = User.lookupByUserLogin(userLogin);
						if (user == null)
							throw new TbitsExceptionClient(
									"No user exists in the userlist by the userlogin : "
											+ userLogin);
						else
							loggerInfo = TransmittalUtils.getUserInfoMap(user);
					}
				}
			} catch (Exception e) {

			}
		}

		/*
		 * try{ String dtnSignatory =
		 * paramTable.get(TransmittalUtils.DTN_SIGNATORY); if ((dtnSignatory ==
		 * null) || (dtnSignatory.trim().equals(""))) { String userLogin =
		 * paramTable.get("user"); if (userLogin != null) { User user =
		 * User.lookupByUserLogin(userLogin); if (user == null) throw new
		 * TbitsExceptionClient("No DTN signatory information provided " +
		 * "in transmittal process parameters or is invalid."); else loggerInfo
		 * = TransmittalUtils.getUserInfoMap(user); } else throw new
		 * TbitsExceptionClient("No DTN signatory information provided " +
		 * "in transmittal process parameters or is invalid."); } else { User
		 * dtnSigUser = User.lookupByUserLogin(dtnSignatory); if (dtnSigUser ==
		 * null) throw newTbitsExceptionClient(
		 * "DTN Signatory provided in transmittal process parameters does not "
		 * + "exist as a user in tBits or is invalid: " + dtnSignatory); else
		 * loggerInfo = TransmittalUtils.getUserInfoMap(dtnSigUser); }
		 * }catch(DatabaseException e1) { e1.printStackTrace(); throw new
		 * TbitsExceptionClient
		 * ("Database error occurred while DTN signatory information.", e1); }
		 */

		BirtTemplateHelper kth = new BirtTemplateHelper(paramTable,
				drawingsList, distributionList, selectedAttachmentList,
				loggerInfo, kAttnInfoMap);
		try {
			ByteArrayOutputStream htmlOS = null;
			String rptDesignFileName = paramTable
					.get("transmittal_template_name");
			if (rptDesignFileName == null)
				throw new TbitsExceptionClient(
						"Error occurred while generating transmittal template. "
								+ "RPT Design file name was not configured for this transmittal process.");
			try {
				htmlOS = TransmittalUtils.generateTransmittalNoteInHtml(
						rptDesignFileName, kth, this.getRequest()
								.getContextPath());
			} catch (IOException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(
						"Error occurred while generating transmittal template. "
								+ e.getMessage(), e);
			} catch (TBitsException e) {
				e.printStackTrace();
				throw new TbitsExceptionClient(e.getMessage());
			}
			htmlString = htmlOS.toString();
			htmlOS.close();
		} catch (EngineException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while generating transmittal template. "
							+ e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while generating transmittal template. "
							+ e.getMessage(), e);
		}
		paramTable.put(TransmittalUtils.ACTUAL_DATE, actualDate);
		return htmlString;
	}

	@Deprecated
	public String getHTMLTransmittalPreviewUsingBirt(int dcrSystemId,
			String requestList, String rptDesignFileName, String toAddress,
			String dtnNumber, String subject, String remarks,
			String kindAttentionString,
			ArrayList<String[]> refTransmittalNumbers,
			ArrayList<String[]> drawingsList, String[] approvalCategory,
			String[] documentType, ArrayList<String[]> distributionList,
			String[] loggerInfo, String emailBody, String yourReferenceNumber,
			String transmittalDate, String dtnSerialKey, String toList,
			String ccList) throws TbitsExceptionClient {
		return null;
	}

	public String createTransmittal(
			HashMap<String, String> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap)
			throws TbitsExceptionClient {
		// String contextPath = this.getRequest().getContextPath();
		String attachmentDetails = "*******************These are the documnets that have been selected for the transmittal*************** "
				+ "\n";

		for (Integer reqID : attachmentInfoClientsMap.keySet()) {
			HashMap<String, List<FileClient>> attachmentMap = attachmentInfoClientsMap
					.get(reqID);
			for (String attachmentFile : attachmentMap.keySet()) {
				attachmentDetails = attachmentDetails + attachmentFile + ":";
				List<FileClient> fc = attachmentMap.get(attachmentFile);
				String FileNames = "";
				for (FileClient fileclient : fc) {

					if (FileNames.equals("")) {
						FileNames = FileNames + fileclient.getFileName();

					} else {

						FileNames = FileNames + "," + fileclient.getFileName();
					}
				}
				attachmentDetails = attachmentDetails + FileNames + "\n";
			}

		}

		System.out.println(attachmentDetails);
		LOG.info(attachmentDetails);
		GenericTransmittalCreator gte = new GenericTransmittalCreator(this
				.getRequest());
		HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> attachmentsInfoMap = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();

		for (Integer requestId : attachmentInfoClientsMap.keySet()) {

			HashMap<String, List<FileClient>> attachmentInfoClients = attachmentInfoClientsMap
					.get(requestId);
			if (attachmentInfoClients != null) {
				HashMap<String, Collection<AttachmentInfo>> convertedAttachmentsMap = getConvertedAttachmentsMap(attachmentInfoClients);
				if (convertedAttachmentsMap != null)
					attachmentsInfoMap.put(requestId, convertedAttachmentsMap);
			}
		}
		try {
			String dtnNumber = gte.createTransmittal(paramTable,
					attachmentsInfoMap);
			if ((dtnNumber != null) && (!dtnNumber.trim().equals("")))
				return dtnNumber;
			else
				return null;
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (APIException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		}
	}

	public String createTransmittalPostApproval(
			HashMap<String, String> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap)
			throws TbitsExceptionClient {
		// String contextPath = this.getRequest().getContextPath();
		GenericTransmittalCreator gte = new GenericTransmittalCreator(this
				.getRequest());
		HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> attachmentsInfoMap = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();

		for (Integer requestId : attachmentInfoClientsMap.keySet()) {

			HashMap<String, List<FileClient>> attachmentInfoClients = attachmentInfoClientsMap
					.get(requestId);
			if (attachmentInfoClients != null) {
				HashMap<String, Collection<AttachmentInfo>> convertedAttachmentsMap = getConvertedAttachmentsMap(attachmentInfoClients);
				if (convertedAttachmentsMap != null)
					attachmentsInfoMap.put(requestId, convertedAttachmentsMap);
			}
		}
		try {
			String dtnNumber = gte.createTransmittal(paramTable,
					attachmentsInfoMap);
			if ((dtnNumber != null) && (!dtnNumber.trim().equals("")))
				return dtnNumber;
			else
				return null;
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (APIException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		}
	}

	static String getPrintStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	private HashMap<String, Collection<AttachmentInfo>> getConvertedAttachmentsMap(
			HashMap<String, List<FileClient>> attachmentInfoClients) {

		HashMap<String, Collection<AttachmentInfo>> attachments = new HashMap<String, Collection<AttachmentInfo>>();
		if (attachmentInfoClients != null) {
			for (String fieldName : attachmentInfoClients.keySet()) {
				Collection<AttachmentInfo> aiList = new ArrayList<AttachmentInfo>();
				ArrayList<FileClient> aicList = (ArrayList<FileClient>) attachmentInfoClients
						.get(fieldName);
				if (aicList != null) {
					for (FileClient aic : aicList) {
						if (aic != null) {
							AttachmentInfo ai = new AttachmentInfo();
							ai.name = aic.getFileName();
							ai.repoFileId = aic.getRepoFileId();
							ai.requestFileId = aic.getRequestFileId();
							ai.size = aic.getSize();
							aiList.add(ai);
						}
					}
				}
				attachments.put(fieldName, aiList);
			}
		}
		return attachments;
	}

	public ArrayList<TbitsModelData> getAttachmentSelectionTableColumns(
			int trnProcessId) throws TbitsExceptionClient {

		ArrayList<TbitsModelData> columnsList = null;
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			columnsList = getAttachmentSelectionTableColumns(connection,
					trnProcessId);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving the column details for creating"
							+ " attachment selection table in transmittal.", e1);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(
							"Error occurred while closing database connection.",
							e);
				}
		}
		return columnsList;
	}

	public static ArrayList<TbitsModelData> getAttachmentSelectionTableColumns(
			Connection connection, int trnProcessId) throws SQLException {
		ArrayList<TbitsModelData> columnsList = new ArrayList<TbitsModelData>();
		String queryString = "SELECT * FROM trn_attachment_selection_table_columns WHERE trn_process_id=?";
		PreparedStatement ps = connection.prepareStatement(queryString);
		ps.setInt(1, trnProcessId);
		ResultSet rs = ps.executeQuery();
		if (rs != null) {
			while (rs.next()) {
				TbitsModelData columnTMD = getAttachmentSelectionTableColumnTMD(rs);
				if (columnTMD != null)
					columnsList.add(columnTMD);
			}
		}
		rs.close();
		ps.close();
		return columnsList;
	}

	public static TbitsModelData getAttachmentSelectionTableColumnTMD(
			ResultSet rs) throws SQLException {
		TbitsModelData columnTMD = new TbitsModelData();
		columnTMD.set(TRN_PROCESS_ID_COLUMN, rs.getInt(TRN_PROCESS_ID_COLUMN)
				+ "");
		columnTMD.set(NAME_COLUMN, rs.getString(NAME_COLUMN));
		columnTMD.set(FIELD_ID_COLUMN, rs.getInt(FIELD_ID_COLUMN));
		columnTMD.set(DATA_TYPE_ID_COLUMN, rs.getInt(DATA_TYPE_ID_COLUMN));
		columnTMD.set(DEFAULT_VALUE_COLUMN, rs.getString(DEFAULT_VALUE_COLUMN));
		columnTMD.set(IS_EDITABLE_COLUMN, rs.getBoolean(IS_EDITABLE_COLUMN));
		columnTMD.set(IS_ACTIVE_COLUMN, rs.getBoolean(IS_ACTIVE_COLUMN));
		columnTMD.set(COLUMN_ORDER, rs.getInt(COLUMN_ORDER));
		columnTMD.set(TYPE_VALUE_SOURCE, rs.getInt(TYPE_VALUE_SOURCE));
		columnTMD.set(IS_INCLUDED, rs.getBoolean(IS_INCLUDED));
		return columnTMD;
	}

	public ArrayList<TbitsModelData> getAllTransmittalProcessParametersBySystemId(
			int dcrSystemId) throws TbitsExceptionClient {

		return null;
	}

	public ArrayList<TbitsModelData> getDistributionTableColumns(
			Integer trnProcessId) throws TbitsExceptionClient {
		Connection connection = null;
		ArrayList<TbitsModelData> columnsList = null;
		try {
			connection = DataSourcePool.getConnection();
			columnsList = getDistributionTableColumns(connection, trnProcessId);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving the column details for creating"
							+ " distribution table in transmittal.", e1);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving the column details for creating"
							+ " distribution table in transmittal.", e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(
							"Error occurred while closing database connection.",
							e);
				}
		}
		return columnsList;
	}

	public static ArrayList<TbitsModelData> getDistributionTableColumns(
			Connection connection, Integer trnProcessId)
			throws DatabaseException {

		ArrayList<TbitsModelData> columnsList = new ArrayList<TbitsModelData>();
		String queryString = "SELECT * FROM trn_distribution_table_column_config WHERE trn_process_id=?";
		try {
			PreparedStatement ps = connection.prepareStatement(queryString);
			ps.setInt(1, trnProcessId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					TbitsModelData columnTMD = getDistributionTableTMDList(rs);
					if (columnTMD != null)
						columnsList.add(columnTMD);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			throw new DatabaseException(
					"Error occurred while retrieving  distribution columns config.",
					sqle);
		}
		return columnsList;
	}

	public static TbitsModelData getDistributionTableTMDList(ResultSet rs)
			throws SQLException {
		TbitsModelData columnTMD = new TbitsModelData();
		columnTMD.set(TRN_PROCESS_ID_COLUMN, rs.getInt(TRN_PROCESS_ID_COLUMN)
				+ "");
		columnTMD.set(NAME_COLUMN, rs.getString(NAME_COLUMN));
		columnTMD.set(DISPLAY_NAME_COLUMN, rs.getString(DISPLAY_NAME_COLUMN));
		columnTMD.set(DATA_TYPE_ID_COLUMN, rs.getInt(DATA_TYPE_ID_COLUMN));
		columnTMD.set(FIELD_CONFIG, rs.getString(FIELD_CONFIG));
		columnTMD.set(IS_EDITABLE_COLUMN, rs.getBoolean(IS_EDITABLE_COLUMN));
		columnTMD.set(IS_ACTIVE_COLUMN, rs.getBoolean(IS_ACTIVE_COLUMN));
		columnTMD.set(COLUMN_ORDER, rs.getInt(COLUMN_ORDER));
		return columnTMD;
	}

	public ArrayList<TbitsModelData> getTrnExtendedFields(Integer trnProcessId)
			throws TbitsExceptionClient {
		Connection connection = null;
		ArrayList<TbitsModelData> columnsList = null;
		try {
			connection = DataSourcePool.getConnection();
			columnsList = getTrnExtendedFields(connection, trnProcessId);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving transmittal wizard extended fields.",
					e1);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving transmittal wizard extended fields.",
					e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(
							"Error occurred while closing database connection.",
							e);
				}
		}
		return columnsList;
	}

	public static ArrayList<TbitsModelData> getTrnExtendedFields(
			Connection connection, Integer trnProcessId)
			throws DatabaseException {
		ArrayList<TbitsModelData> columnsList = new ArrayList<TbitsModelData>();
		String queryString = "SELECT * FROM trn_wizard_fields WHERE trn_process_id=?";
		try {
			PreparedStatement ps = connection.prepareStatement(queryString);
			ps.setInt(1, trnProcessId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					TbitsModelData columnTMD = getTrnExtendedFieldsTMD(rs);
					if (columnTMD != null)
						columnsList.add(columnTMD);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			throw new DatabaseException(
					"Database error occurred while retrieving transmittal wizard extended fields.",
					sqle);
		}
		return columnsList;
	}

	public static TbitsModelData getTrnExtendedFieldsTMD(ResultSet rs)
			throws SQLException {
		TbitsModelData columnTMD = new TbitsModelData();
		columnTMD.set(TRN_PROCESS_ID_COLUMN, rs.getInt(TRN_PROCESS_ID_COLUMN)
				+ "");
		columnTMD.set(NAME_COLUMN, rs.getString(NAME_COLUMN));
		columnTMD.set(FIELD_ID_COLUMN, rs.getInt(FIELD_ID_COLUMN));
		columnTMD.set(FIELD_CONFIG, rs.getString(FIELD_CONFIG));
		columnTMD.set(IS_EDITABLE_COLUMN, rs.getBoolean(IS_EDITABLE_COLUMN));
		columnTMD.set(IS_ACTIVE_COLUMN, rs.getBoolean(IS_ACTIVE_COLUMN));
		columnTMD.set(FIELD_ORDER, rs.getInt(FIELD_ORDER));
		columnTMD.set(IS_DTN_NUMBER_PART, rs.getBoolean(IS_DTN_NUMBER_PART));
		return columnTMD;
	}

	public HashMap<String, String> getAppCycleTransientData(int currentSysId,
			Integer requestId) throws TbitsExceptionClient {

		HashMap<String, String> appCycleTransientDataMap = new HashMap<String, String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM trn_approval_cycle_transient_data "
							+ "WHERE sys_id=? and request_id=?");
			ps.setInt(1, currentSysId);
			ps.setInt(2, requestId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String param = rs.getString(PARAMETER);
					String value = rs.getString(VALUE);
					if (value == null)
						value = "";
					appCycleTransientDataMap.put(param, value);
				}
			}

		} catch (SQLException sqle) {
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving transmittal transient data for \"Approval\" id:"
							+ requestId + "\n" + sqle.getMessage(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(
							"Database error occurred while retrieving transmittal transient data for \"Approval\""
									+ " id:"
									+ requestId
									+ "\n"
									+ sqle.getMessage(), sqle);
				}
			}
		}

		return appCycleTransientDataMap;
	}

	public String getAppCycleRoleName(int currentSysId, Integer requestId)
			throws TbitsExceptionClient {
		String value = null;
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement(TRANSIENT_DATA_APPROVAL_ROLE_NAME_QUERY);
			ps.setInt(1, currentSysId);
			ps.setInt(2, requestId);
			ps.setString(3, "trnProcessId");
			ps.setString(4, "approvalRoleName");
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					value = rs.getString(VALUE);
				}
			}

		} catch (SQLException sqle) {
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving transmittal transient data for \"Approval\" id:"
							+ requestId + "\n" + sqle.getMessage(), sqle);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(
							"Database error occurred while retrieving transmittal transient data for "
									+ "\"Approval\" id:" + requestId + "\n"
									+ sqle.getMessage(), sqle);
				}
		}
		return value;
	}

	public ArrayList<TbitsModelData> getAppCycleTransientDataAttachments(
			int currentSysId, Integer requestId) throws TbitsExceptionClient {

		Connection connection = null;
		ArrayList<TbitsModelData> columnsList = new ArrayList<TbitsModelData>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM trn_approval_cycle_transient_data_attachments "
							+ "WHERE sys_id=? and request_id=?");
			ps.setInt(1, currentSysId);
			ps.setInt(2, requestId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					TbitsModelData columnTMD = getAppCycleTransientDataAttachmentsTMD(rs);
					if (columnTMD != null)
						columnsList.add(columnTMD);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while retrieving selected attachments info"
							+ " from transient data attachments.\n"
							+ sqle.getMessage(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(
							"Database error occurred while retrieving selected attachments info"
									+ " from transient data attachments.\n"
									+ sqle.getMessage(), sqle);
				}
			}
		}
		return columnsList;
	}

	public static TbitsModelData getAppCycleTransientDataAttachmentsTMD(
			ResultSet rs) throws SQLException {
		TbitsModelData columnTMD = new TbitsModelData();
		columnTMD.set(SYS_ID, rs.getInt(SYS_ID));
		columnTMD.set(REQUEST_ID, rs.getInt(REQUEST_ID));
		columnTMD.set(SRC_REQUEST_ID, rs.getInt(SRC_REQUEST_ID));
		columnTMD.set(FIELD_NAME, rs.getString(FIELD_NAME));
		columnTMD.set(ATTACHMENTS, rs.getString(ATTACHMENTS));
		return columnTMD;
	}

	public String createTransmittalPostApproval(
			int systemId,
			int requestId,
			HashMap<String, String> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap)
			throws TbitsExceptionClient {

		GenericTransmittalCreator gte = new GenericTransmittalCreator(this
				.getRequest());
		HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> attachmentsInfoMap = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();

		try {

			HashMap<String, String> appCycleTransientDataMap = getAppCycleTransientData(
					systemId, requestId);
			if (appCycleTransientDataMap == null)
				throw new TBitsException(
						"No transient data found for creating transmittal. Hence transmittal process failed. "
								+ "Please try approving it again by clicking \"Approved\" button or contact tBits team.");

			if (Boolean.parseBoolean(appCycleTransientDataMap
					.get(TransmittalUtils.IS_APPROVAL_CYCLE)))
				paramTable.put(TransmittalUtils.IS_APPROVAL_CYCLE, "false");

			paramTable.put(TransmittalUtils.IS_POST_APPROVAL_CYCLE, "true");
			/*
			 * ArrayList<TbitsModelData> appCycleTransientDataAttachments =
			 * getAppCycleTransientDataAttachments( systemId, requestId);
			 * 
			 * populateSelectedAttachments(attachmentsInfoMap,
			 * appCycleTransientDataAttachments);
			 */
			paramTable.put(TransmittalUtils.APPROVAL_CYCLE_REQUEST_ID, String
					.valueOf(requestId));

			for (Integer requestID : attachmentInfoClientsMap.keySet()) {

				HashMap<String, List<FileClient>> attachmentInfoClients = attachmentInfoClientsMap
						.get(requestID);
				if (attachmentInfoClients != null) {
					HashMap<String, Collection<AttachmentInfo>> convertedAttachmentsMap = getConvertedAttachmentsMap(attachmentInfoClients);
					if (convertedAttachmentsMap != null)
						attachmentsInfoMap.put(requestID,
								convertedAttachmentsMap);
				}
			}

			User user = WebUtil.validateUser(this.getRequest());
			if (user != null)
				paramTable.put("dtnLogger", user.getUserLogin());

			String dtnNumber = gte.createTransmittal(paramTable,
					attachmentsInfoMap);
			if ((dtnNumber != null) && (!dtnNumber.trim().equals("")))
				return dtnNumber;
			else
				return null;
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (APIException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(getPrintStackTrace(e));
		}
	}

	private void populateSelectedAttachments(
			HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> attachmentsInfoMap,
			ArrayList<TbitsModelData> appCycleTransientDataAttachments) {
		for (TbitsModelData tmd : appCycleTransientDataAttachments) {
			if (tmd != null) {
				Integer srcRequestId = (Integer) tmd.get(SRC_REQUEST_ID);
				HashMap<String, Collection<AttachmentInfo>> hashMap = attachmentsInfoMap
						.get(srcRequestId);
				if (hashMap == null) {
					hashMap = new HashMap<String, Collection<AttachmentInfo>>();
				}
				String fieldName = (String) tmd.get(FIELD_NAME);
				if (fieldName != null) {
					String attachments = (String) tmd.get(ATTACHMENTS);
					if (attachments != null) {
						Collection<AttachmentInfo> attCollection = AttachmentInfo
								.fromJson(attachments);
						if (attCollection != null) {
							hashMap.put(fieldName, attCollection);
							attachmentsInfoMap.put(srcRequestId, hashMap);
						}
					}
				}
			}
		}
	}

	public ArrayList<TbitsModelData> getValidationRules(
			int currentTransmittalProcessId) throws TbitsExceptionClient {
		ArrayList<TbitsModelData> rulesList = new ArrayList<TbitsModelData>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM trn_validation_rules"
							+ " WHERE trn_process_id=?");
			ps.setInt(1, currentTransmittalProcessId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					// int trnProcessId =
					// rs.getInt(TransmittalUtils.TRN_PROCESS_ID);
					int fieldId = rs.getInt("field_id");
					String value = rs.getString("value");
					TbitsModelData tmd = new TbitsModelData();
					tmd.set("field_id", fieldId);
					tmd.set("value", value);
					rulesList.add(tmd);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException sql) {
			sql.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while fetching validation rules.",
					sql);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException sql) {
					throw new TbitsExceptionClient(
							"Database exception occurred while fetching validation rules.\n"
									+ sql.getMessage());
				}
		}
		return rulesList;
	}

	public ArrayList<Integer> getAllApprovalBASysIds()
			throws TbitsExceptionClient {
		Connection connection = null;
		ArrayList<Integer> dtrSysIdList = new ArrayList<Integer>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("SELECT DISTINCT dtr_sys_id  FROM trn_processes");
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					int dtrSysId = rs.getInt("dtr_sys_id");
					dtrSysIdList.add(dtrSysId);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException sql) {
			sql.printStackTrace();
			throw new TbitsExceptionClient(
					"Database error occurred while fetching validation rules.",
					sql);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException sql) {
					throw new TbitsExceptionClient(
							"Database exception occurred while fetching validation rules.\n"
									+ sql.getMessage());
				}
		}
		return dtrSysIdList;
	}

	public boolean checkUserExistsInRole(int systemId, int requestId, int userId)
			throws TbitsExceptionClient {
		String roleName = getAppCycleRoleName(systemId, requestId);
		if (roleName != null) {
			ArrayList<String> roles = getUserRolesNamesBySysIdAndUserId(
					systemId, userId);
			if (roles != null) {
				for (String role : roles) {
					if ((role != null)
							&& (role.trim().equalsIgnoreCase(roleName.trim()))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean checkUserExistsInRole(int systemId, int userId)
			throws TbitsExceptionClient {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("SELECT role_name from trn_rolename_for_past_data_input_permission"
							+ " where sys_id=?");
			ps.setInt(1, systemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				if (rs.next()) {
					String roleName = rs.getString("role_name");
					if (roleName != null) {
						ArrayList<String> rolesList = getUserRolesNamesBySysIdAndUserId(
								systemId, userId);
						if (rolesList != null) {
							for (String role : rolesList) {
								if ((role != null)
										&& (role.trim()
												.equalsIgnoreCase(roleName
														.trim()))) {
									return true;
								}
							}
						}
					}
				}
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.error(e);
			throw new TbitsExceptionClient(
					"Database error occurred while fetching historical data"
							+ " input permissions for the user." + "\n"
							+ e.getMessage());
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					LOG.error(e);
					throw new TbitsExceptionClient(
							"Database error occurred while fetching historical data"
									+ " input permissions for the user.");
				}
		}
		return false;
	}

	public boolean checkUserExistsInRole(int systemId, String roleName,
			String userLogin) throws TbitsExceptionClient {
		if (roleName == null) {
			return false;
		}
		try {
			User user = User.lookupByUserLogin(userLogin);
			if (user != null) {
				ArrayList<String> roles = getUserRolesNamesBySysIdAndUserId(
						systemId, user.getUserId());
				if (roles != null) {
					for (String role : roles) {
						if ((role != null)
								&& (role.trim().equalsIgnoreCase(roleName
										.trim()))) {
							return true;
						}
					}
				}
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while checking user permissions for creating transmittal",
					e);
		}

		return false;
	}

	public ArrayList<String> getUserRolesNamesBySysIdAndUserId(int aSystemId,
			int aUserId) throws TbitsExceptionClient {
		ArrayList<String> arrayList = new ArrayList<String>();
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			CallableStatement cs = connection
					.prepareCall("{Call stp_admin_getUserRolesBySysIdAndUserIdIncludingMailingList ?,? }");
			// "{Call stp_admin_getUserRolesBySysIdAndUserId ?,? }");

			cs.setInt(1, aSystemId);
			cs.setInt(2, aUserId);

			ResultSet rs = cs.executeQuery();
			String roleName = null;
			int usrId = 0;

			if (rs != null) {
				while (rs.next()) {
					roleName = rs.getString("rolename");
					usrId = rs.getInt("user_id");
					if (usrId != -1) {
						arrayList.add(roleName);
					}

				}

				rs.close();
			}

			cs.close();
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("The application encountered an exception while ")
					.append("trying to retrieve the list of user Roles");

			throw new TbitsExceptionClient(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warn("Database exception while closing the connection:",
						sqle);
			}

			connection = null;
		}

		return arrayList;
	}

	public static void main(String[] args) throws TbitsExceptionClient {

		Connection connection = null;
		Request r = new Request();

		try {
			connection = DataSourcePool.getConnection();

			connection.setAutoCommit(false);

			Hashtable<String, String> hm = new Hashtable<String, String>();
			hm.put(Field.BUSINESS_AREA, "JPN_DCPL");
			hm.put(Field.USER, "root");
			hm.put(Field.REQUEST, "415");
			hm.put("Revision", "A");
			hm.put(Field.LOGGER, "root");

			UpdateRequest addRequest = new UpdateRequest();

			TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
			try {
				r = addRequest.updateRequest(connection, tbitsResMgr, hm);
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception e) {
		}

		System.out.println(r.getRequestId());
		System.out.println(r.getLoggedDate());
		System.out.println(r.getObject("Revision"));

		try {
			Request dcrRequest = Request.lookupBySystemIdAndRequestId(
					connection, 41, 415);

			System.out.println(dcrRequest.getObject("Revision"));
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Request dcrRequest;
		try {
			dcrRequest = Request.lookupBySystemIdAndRequestId(connection, 41,
					415);

			System.out.println(dcrRequest.getObject("Revision"));

		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(r.get("revision"));

	}

	public Long getTime(Integer trnProcessId, Integer dcrSysId,
			Integer targetSysId) {
		return null;
	}

	public TbitsModelData getTransmittalDropDownOption(int aSystemId,
			int trnProcessId) throws TbitsExceptionClient {

		TbitsModelData tmd = new TbitsModelData();
		TransmittalDropDownOption ddOption;
		try {
			ddOption = TransmittalDropDownOption
					.lookupTransmittalDropDownByProcessId(aSystemId,
							trnProcessId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while retrieving transmital types.");
		}
		if (ddOption != null) {

			tmd.set("trndropdownId", String.valueOf(ddOption.getId()));
			tmd.set("name", ddOption.getName());
			tmd.set("dcrSystemId", String.valueOf(ddOption.getDcrSystemId()));
			tmd.set("sortOrder", String.valueOf(ddOption.getSortOrder()));
		}
		return tmd;

	}

	/**
	 * Returns all the data corresponding to requestID in a
	 * DTRApprovalBusinessArea
	 * 
	 * @param requestId
	 * @param SystemId
	 * @returns a Hashmap consisting of the transient data corresponding to a
	 *          requestid,data corresponding to the transmission dropdown,id and
	 *          fields of the Sorce Business area,requestObjects for whom
	 *          transmittal is being created
	 * @see transmittal.com.tbitsGlobal.client.TransmittalService#getTansParams(int,
	 *      int)
	 */

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getTansParams(int requestid, int sysid)
			throws TbitsExceptionClient, NumberFormatException {

		HashMap<String, Object> hm = new HashMap<String, Object>();

		HashMap<String, String> HashMapForTransientData = new HashMap<String, String>();
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select parameter,value from trn_approval_cycle_transient_data where sys_id=? and request_id=?");

			ps.setInt(1, sysid);
			ps.setInt(2, requestid);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					String para = rs.getString("parameter");
					String val = rs.getString("value");

					HashMapForTransientData.put(para, val);

				}
			}

			String cc = HashMapForTransientData.get("ccList");
			String[] transientDataCC = cc.split(",");
			ArrayList<String> filteredCCList = new ArrayList<String>();
			ArrayList<User> usersToBeRemoved = new ArrayList<User>();

			String filteredCC = "";

			for (String eachCC : transientDataCC) {
				if (getNameForLogin(eachCC) != null) {
					if (filteredCC.equals("")) {
						filteredCC = filteredCC + eachCC;
					}

					else {
						filteredCC = filteredCC + "," + eachCC;
					}
					filteredCCList.add(eachCC);
				} else {
					User tobeDeletedUser = User.lookupAllByUserLogin(eachCC);
					usersToBeRemoved.add(tobeDeletedUser);
				}
			}

			HashMapForTransientData.put("ccList", filteredCC);

			hm.put("HashMapForTransientData", HashMapForTransientData);
			HashMap<String, String> tempHashMap = (HashMap<String, String>) hm
					.get("HashMapForTransientData");

			String roleName = tempHashMap.get("approvalRoleName");
			String trnProcessId = (String) tempHashMap.get("trnProcessId");// type
			int processid = Integer.parseInt(trnProcessId);
			TbitsModelData modelDataOfDropDown = getTransmittalDropDownOption(
					0, processid);
			String sysId = modelDataOfDropDown.get("dcrSystemId");// sys id of
			int originalSystemId = Integer.parseInt(sysId); // the
			// original
			// ba
			ArrayList<?> ddOptionList = getTransmittalDropDownOptions(Integer
					.parseInt((String) modelDataOfDropDown.get("dcrSystemId")));
			String requestList = (String) tempHashMap.get("requestList");
			String[] arrOfRequest = requestList.split(",");

			ArrayList<Integer> RequestList_Integer = new ArrayList<Integer>();
			for (String requestId : arrOfRequest) {
				int temp = Integer.parseInt(requestId);
				RequestList_Integer.add(temp);

			}

			BusinessArea ba;
			ba = BusinessArea.lookupBySystemId(Integer.parseInt(sysId));
			String sysPrefix = ba.getSystemPrefix();

			ArrayList<TbitsTreeRequestData> RequestData = new ArrayList<TbitsTreeRequestData>();
			HashMap<Integer, TbitsTreeRequestData> requestMap = new HashMap<Integer, TbitsTreeRequestData>();

			requestMap = getDataByRequestIds(sysPrefix, RequestList_Integer);

			for (TbitsTreeRequestData ttrd : requestMap.values()) {
				RequestData.add(ttrd);
			}

			String statusOfFinishButton = "";

			Boolean statusOffinishButton = this.checkUserExistsInRole(sysid,
					roleName, getCurrentUser().getUserLogin());

			statusOfFinishButton = String.valueOf(statusOffinishButton);

			Integer trnDropDownId = Integer
					.parseInt((String) modelDataOfDropDown.get("trndropdownId"));
			TbitsModelData processParams = this
					.getTransmittalProcessParameters(originalSystemId,
							RequestList_Integer, trnDropDownId);

			TbitsModelData modelDataForApprovalWizard = new TbitsModelData();
			for (String property : processParams.getPropertyNames()) {

				if (tempHashMap.containsKey(property)) {
					Object value = tempHashMap.get(property);

					modelDataForApprovalWizard.set(property, value);

				}
			}
			ArrayList<BAField> baFieldList = (ArrayList<BAField>) getFields(sysPrefix);

			String drawingsTable = (String) tempHashMap.get("drawingTable");

			HashMap<String, ArrayList<DrawinglistModel>> drawingTableMapForOldVersion = this
					.fetchDrawingListFromJsonArrayInPrevVersion(drawingsTable);

			HashMap<String, ArrayList<DrawinglistModel>> drawingTableMap = this
					.fetchDrawingListFromJsonArray(drawingsTable);

			String selectedAttachmentTable = (String) tempHashMap
					.get("SelectedAttachmentsTable");
			ArrayList<AttachmentModel> selectedAttachmentList = this
					.fetchAttachmentListFromJsonArray(selectedAttachmentTable);

			String distributionData = tempHashMap.get("distributionTable");
			ArrayList<String[]> distributionList = new ArrayList<String[]>();

			if (tempHashMap.containsKey("version")) {
				distributionList = fetchDistributionListFromJsonArray(
						distributionData, usersToBeRemoved);

			} else {
				distributionList = fetchArrayListFromJsonArray(distributionData);
				for (String[] a : distributionList) {
					for (int i = 0; i < a.length; i++) {
						if (!a[i].equals("-")) {
							if (a[i].contains(",")) {
								String[] splittedString = a[i].split(",");
								String a1 = splittedString[0];
								String b1 = splittedString[1];
								if (a1.equals("display_name")) {
									if (!usersToBeRemoved.contains(b1)) {
										String c = getLoginForName(b1);
										a[i] = a1 + "," + c;
									} else {
										a[i] = a1 + "," + "-";
									}
								}
							}
						}

					}
				}

			}

			ArrayList<TrnEditableColumns> dataFOrEditableColumns = getEditabeColumnsForTrnWizrad(
					Integer.parseInt(sysId), processid);

			ArrayList<TrnEditableColumns> dataFOrEditableAttchmentColumns = getEditabeAttachmentColumnsForTrnWizrad(
					Integer.parseInt(sysId), processid);

			String statusOfDTNDate = "";
			String RoleNameForDtnDate = "HistoricalDateDTN";
			Boolean statusOfDtnDate = this.checkUserExistsInRole(
					originalSystemId, RoleNameForDtnDate, getCurrentUser()
							.getUserLogin());
			statusOfDTNDate = String.valueOf(statusOfDtnDate);
			hm.put("StatusOfDtnDate", statusOfDTNDate);

			hm.put("statusOfFinishButton", statusOfFinishButton);

			ArrayList<TbitsModelData> attColumns = this
					.getAttachmentSelectionTableColumns(processid);
			ArrayList<TbitsModelData> distributionColumn = this
					.getDistributionTableColumns(processid);
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
			// them. So, that they maintain the sort order and
			// hence the column order in the table.
			Collections.sort(distributionColumn, comp);
			Collections.sort(attColumns, comp);
			hm.put(DISTRIBUTION_TABLE_COLUMNS_LIST, distributionColumn);
			hm.put(ATTACHMENT_TABLE_COLUMN_LIST, attColumns);

			hm.put(TRN_EXTENDED_FIELDS, this.getTrnExtendedFields(processid));
			hm.put("dropdownlist", ddOptionList);
			hm.put("selected_dropdown_id", modelDataOfDropDown
					.get("trndropdownId"));
			hm.put("selected_dropdown_name", modelDataOfDropDown.get("name"));
			hm.put("sysid", modelDataOfDropDown.get("dcrSystemId"));
			hm.put("transmittalProcessParams", modelDataForApprovalWizard);
			hm.put("TreeModeldataOfRequest", RequestData);
			hm.put(VALIDATION_RULES_LIST, this.getValidationRules(processid));
			hm.put(MAP_OF_REQUESTS, this.getDataByRequestIds(sysPrefix,
					RequestList_Integer));
			hm.put("RequestList_Integer", RequestList_Integer);
			hm.put("BAFields", baFieldList);
			if (tempHashMap.containsKey("Type_Value_Source_Supported"))
				hm.put("ConvertedDrawingList", drawingTableMap);
			else
				hm.put("ConvertedDrawingList", drawingTableMapForOldVersion);
			hm.put("ConvertedAttachmentTable", selectedAttachmentList);
			hm.put("ConvertedDistributionList", distributionList);
			hm.put("editableAttachmentColumnsList",
					dataFOrEditableAttchmentColumns);
			hm.put("editableColumnsList", dataFOrEditableColumns);
			hm.put("defaultProcessExists", String.valueOf("true"));
			hm.put(TransmittalConstants.DISTRIBUTION_TABLE, tempHashMap
					.get(TransmittalConstants.DISTRIBUTION_TABLE));
			// 9 is the data type id for combobox
			if (tempHashMap.containsKey("Type_Value_Source_Supported")) {
				for (Object reqId : drawingTableMap.keySet()) {
					String reqID = (String) reqId;

					ArrayList<DrawinglistModel> arr = (ArrayList<DrawinglistModel>) drawingTableMap
							.get(reqId);

					for (DrawinglistModel eachModel : arr) {
						if (eachModel.getTYPE_VAUE_SOURCE() != null) {
							String val = getValforType(Integer.parseInt(sysId),
									Integer.parseInt(eachModel.getFIELD_ID()),
									eachModel.getFIELD_VALUE(), eachModel
											.getTYPE_VAUE_SOURCE());

							eachModel.setFIELD_VALUE(val);
						}
					}

				}
			} else {
				for (Object reqId : drawingTableMapForOldVersion.keySet()) {
					String reqID = (String) reqId;

					ArrayList<DrawinglistModel> arr = (ArrayList<DrawinglistModel>) drawingTableMapForOldVersion
							.get(reqId);

					for (DrawinglistModel eachModel : arr) {
						{
							for (TrnEditableColumns tec : dataFOrEditableColumns) {

								if (tec.getDataTypeId() == 9
										&& tec.getFIELDID() == Integer
												.parseInt(eachModel
														.getFIELD_ID())) {
									String val = getValforTypeForOldVersion(
											Integer.parseInt(sysId), Integer
													.parseInt(eachModel
															.getFIELD_ID()),
											eachModel.getFIELD_VALUE());

									eachModel.setFIELD_VALUE(val);
								}

							}
						}

					}
				}
			}

			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return hm;
	}

	/*
	 * parses the json string of attachments
	 */
	private ArrayList<AttachmentModel> fetchAttachmentListFromJsonArray(
			String attachmentList) {

		AttachmentModel attachmentModel = new AttachmentModel();
		ArrayList<AttachmentModel> list = new ArrayList<AttachmentModel>();
		ArrayList<Attachmentinfo> TempList = new ArrayList<Attachmentinfo>();
		if (attachmentList == null || attachmentList.equals("")) {
			return null;
		}

		else {
			final JsonParser parseValue = new JsonParser();
			final JsonElement jElem = parseValue.parse(attachmentList);
			final JsonArray jsonArray = jElem.getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {

				if ((jsonArray.get(i) != null)
						&& (!jsonArray.get(i).isJsonNull())) {
					JsonArray jArray = null;
					if (!jsonArray.get(i).isJsonArray()) {
						return null;
					} else {
						jArray = jsonArray.get(i).getAsJsonArray();
					}
					attachmentModel = new AttachmentModel();
					attachmentModel.setREQUEST_ID(jArray.get(0).getAsString());
					attachmentModel.setSUBJECT(jArray.get(1).getAsString());
					HashMap<String, List<Attachmentinfo>> hm = new HashMap<String, List<Attachmentinfo>>();
					Attachmentinfo ai = new Attachmentinfo();
					TempList = new ArrayList<Attachmentinfo>();

					for (int j = 2; j < jArray.size(); j++) {

						JsonArray jArray1 = null;

						if (!jArray.get(j).isJsonArray()) {
						} else {

							jArray1 = jArray.get(j).getAsJsonArray();
							String prop = jArray1.get(0).getAsString();

							JsonArray jArray2 = null;
							for (int k = 1; k < jArray1.size(); k++) {
								TempList = new ArrayList<Attachmentinfo>();
								if (!jArray1.get(k).isJsonArray()) {
								} else {
									jArray2 = jArray1.get(k).getAsJsonArray();

									for (int l = 0; l < jArray2.size(); l++) {
										ai = new Attachmentinfo();
										JsonArray jArray3 = jArray2.get(l)
												.getAsJsonArray();
										ai.setFILE_NAME(jArray3.get(0)
												.getAsString());

										String reqFileId = jArray3.get(1)
												.getAsString();
										ai.setREQUEST_FILE_ID(Integer
												.parseInt(reqFileId));
										TempList.add(ai);
									}
								}
							}
							hm.put(prop, TempList);
							attachmentModel.setAttachmentDetails(hm);
						}
					}

					list.add(attachmentModel);

				}

			}
		}
		return list;
	}

	/**
	 * (non-Javadoc)Retruns The Request Object corresponding to a requestId
	 * 
	 * @param System
	 *            Prefix of a business area
	 * @param RequestId
	 * @throws tbitsExceptionClient
	 * @return a requestobject
	 * @see transmittal.com.tbitsGlobal.client.TransmittalService#getDataByRequestId(java.lang.String,
	 *      int)
	 */

	public TbitsTreeRequestData getDataByRequestId(String sysPrefix,
			int requestId) throws TbitsExceptionClient {
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return GWTServiceHelper.getDataByRequestId(user, ba, requestId);
	}

	/**
	 * 
	 * Method to get the field of a particular business area
	 * 
	 * @param SystemPrefix
	 * @throws TbitsExceptionClient
	 * @return ListOfFields
	 * @see transmittal.com.tbitsGlobal.client.TransmittalService#getFields(java.lang.String)
	 */
	public List<BAField> getFields(String sysPrefix)
			throws TbitsExceptionClient {
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return GWTServiceHelper.getFields(ba, user);
	}

	public ArrayList<TrnEditableColumns> getEditabeColumnsForTrnWizrad(
			int systemid, int processid) throws TbitsExceptionClient {
		HashMap<String, HashMap<String, String>> hm = new HashMap<String, HashMap<String, String>>();
		ArrayList<TrnEditableColumns> columnsList = new ArrayList<TrnEditableColumns>();

		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select a.name,a.data_type_id,a.column_order,b.name as property,a.field_id from trn_attachment_selection_table_columns a,fields b WHERE a.data_type_id!=11 and a.field_id=b.field_id and b.sys_id=? and TRN_PROCESS_ID=? and a.is_editable=1 and a.is_active=1");

			ps.setInt(1, systemid);
			ps.setInt(2, processid);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					String name = rs.getString("name");
					Integer order = rs.getInt("column_order");
					String property = rs.getString("property");
					Integer data_type_id = rs.getInt("data_type_id");
					Integer field_id = rs.getInt("field_id");

					TrnEditableColumns obj = new TrnEditableColumns();
					obj.setDataTypeId(data_type_id);
					obj.setORDER(order);
					obj.setPROPERTY(property);
					obj.setNAME(name);
					obj.setFIELDID(field_id);
					columnsList.add(obj);

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return columnsList;

	}

	public ArrayList<String[]> fetchArrayListFromJsonArray(String jsonArrayTable)
			throws JsonParseException {

		final ArrayList<String[]> drawingsList = new ArrayList<String[]>();
		if ((jsonArrayTable == null) || jsonArrayTable.trim().equals(""))
			return drawingsList;

		if (jsonArrayTable != null) {
			final JsonParser parseValue = new JsonParser();
			final JsonElement jElem = parseValue.parse(jsonArrayTable);
			final JsonArray jsonArray = jElem.getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				if ((jsonArray.get(i) != null)
						&& (!jsonArray.get(i).isJsonNull())) {
					JsonArray jArray = null;
					if (!jsonArray.get(i).isJsonArray()) {
						JsonParser jParser = new JsonParser();
						jArray = jParser.parse(jsonArray.get(i).getAsString())
								.getAsJsonArray();
					} else
						jArray = jsonArray.get(i).getAsJsonArray();
					String[] drwInfo = new String[jArray.size()];
					for (int j = 0; j < jArray.size(); j++) {
						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull()))
							drwInfo[j] = jArray.get(j).getAsString();
						else
							drwInfo[j] = "-";
					}
					drawingsList.add(drwInfo);
				}
			}
		}
		return drawingsList;
	}

	public HashMap<String, ArrayList<DrawinglistModel>> fetchDrawingListFromJsonArrayInPrevVersion(
			String drawingList) throws JsonParseException {

		HashMap<String, ArrayList<DrawinglistModel>> hm = new HashMap<String, ArrayList<DrawinglistModel>>();

		if ((drawingList == null) || drawingList.trim().equals(""))
			return null;

		if (drawingList != null) {
			final JsonParser parseValue = new JsonParser();
			final JsonElement jElem = parseValue.parse(drawingList);
			final JsonArray jsonArray = jElem.getAsJsonArray();

			for (int i = 0; i < jsonArray.size(); i++) {
				if ((jsonArray.get(i) != null)
						&& (!jsonArray.get(i).isJsonNull())) {
					JsonArray jArray = null;
					if (!jsonArray.get(i).isJsonArray()) {

						JsonParser jParser = new JsonParser();
						jArray = jParser.parse(jsonArray.get(i).getAsString())
								.getAsJsonArray();
					} else {
						jArray = jsonArray.get(i).getAsJsonArray();
					}
					String[] drwInfo = new String[jArray.size()];
					// jarray is the original 1

					ArrayList<DrawinglistModel> masterModel = new ArrayList<DrawinglistModel>();
					String keyOfHashMap = "";

					for (int j = 0; j < jArray.size(); j++)

					{
						DrawinglistModel temp = new DrawinglistModel();
						/*
						 * for model of each drawing list
						 */

						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull())) {

							if (!jArray.get(j).isJsonArray()) {

								if (jArray.get(j).getAsString().contains(","))

								{
									int index = jArray.get(j).getAsString()
											.indexOf(",");

									String val = jArray.get(j).getAsString();

									String[] dataToBeProcessed = val.split(",");

									temp.setFIELD_ID(dataToBeProcessed[0]);
									if (jArray.get(j).getAsString().substring(
											index + 1).equals("")) {
										temp.setFIELD_VALUE("");
									} else {

										temp
												.setFIELD_VALUE(dataToBeProcessed[1]);
									}
									masterModel.add(temp);

								} else {
									keyOfHashMap = jArray.get(j).getAsString();
								}

							}
							// bwosing around jarray

						}

						if (j == jArray.size() - 1) {
							hm.put(keyOfHashMap, masterModel);
						}

					}

				}
			}
		}
		return hm;
	}

	public HashMap<String, ArrayList<DrawinglistModel>> fetchDrawingListFromJsonArray(
			String drawingList) throws JsonParseException {

		HashMap<String, ArrayList<DrawinglistModel>> hm = new HashMap<String, ArrayList<DrawinglistModel>>();

		if ((drawingList == null) || drawingList.trim().equals(""))
			return null;

		if (drawingList != null) {
			final JsonParser parseValue = new JsonParser();
			final JsonElement jElem = parseValue.parse(drawingList);
			final JsonArray jsonArray = jElem.getAsJsonArray();

			for (int i = 0; i < jsonArray.size(); i++) {
				if ((jsonArray.get(i) != null)
						&& (!jsonArray.get(i).isJsonNull())) {
					JsonArray jArray = null;
					if (!jsonArray.get(i).isJsonArray()) {
						JsonParser jParser = new JsonParser();
						jArray = jParser.parse(jsonArray.get(i).getAsString())
								.getAsJsonArray();
					} else {
						jArray = jsonArray.get(i).getAsJsonArray();
					}
					String[] drwInfo = new String[jArray.size()];
					// jarray is the original 1

					ArrayList<DrawinglistModel> masterModel = new ArrayList<DrawinglistModel>();
					String keyOfHashMap = "";
					for (int j = 0; j < jArray.size(); j++)

					{
						/*
						 * for model of each drawing list
						 */

						JsonArray jArray1 = new JsonArray();

						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull())) {

							if (!jArray.get(j).isJsonArray()) {
								JsonParser jParser = new JsonParser();

								keyOfHashMap = jArray.get(j).getAsString();
								hm.put(keyOfHashMap,
										new ArrayList<DrawinglistModel>());
							}
							// bwosing around jarray
							else {
								DrawinglistModel temp = new DrawinglistModel();

								jArray1 = jArray.get(j).getAsJsonArray();

								// each elemnt consitn gof 2 ele
								for (int l = 0; l < jArray1.size(); l++)

								{

									JsonArray jArray2 = new JsonArray();

									if ((jArray1.get(l) != null)
											&& (!jArray1.get(l).isJsonNull())) {

										if (jArray1.get(l).isJsonArray()) {
											jArray2 = jArray1.get(l)
													.getAsJsonArray();

											for (int p = 0; p < jArray2.size(); p++)

											{
												if (p == 0) {
													String eachElement = jArray2
															.get(p)
															.getAsString();
													drwInfo[j] = eachElement;
													temp
															.setFIELD_VALUE(eachElement);
												} else if (p == 1)

												{
													temp
															.setTYPE_VAUE_SOURCE(jArray2
																	.get(1)
																	.getAsString());
												}
											}

										}

										else {

											if (l == 1)

											{
												String eachElement = jArray1
														.get(l).getAsString();
												temp.setTYPE_VAUE_SOURCE(null);
												drwInfo[j] = eachElement;
												temp
														.setFIELD_VALUE(eachElement);
											} else if (l == 0) {
												String id = jArray1.get(l)
														.getAsString();
												temp.setFIELD_ID(id);

											}
										}
									} else {
										// throw new
										drwInfo[j] = "-";
									}
									if (l == 1) {
										masterModel.add(temp);
									}
								}

							}

						}

						if (j == jArray.size() - 1) {
							hm.put(keyOfHashMap, masterModel);
						}

					}
					// drawingsList.add(drwInfo);
				}
			}
		}
		return hm;
	}

	public ArrayList<String[]> fetchDistributionListFromJsonArray(
			String jsonArrayTable, ArrayList<User> users)
			throws JsonParseException {

		String msg = "These are the users that are no longer active" + "\n";
		String result = "";
		final ArrayList<String[]> drawingsList = new ArrayList<String[]>();
		if ((jsonArrayTable == null) || jsonArrayTable.trim().equals(""))
			return drawingsList;

		if (jsonArrayTable != null) {
			final JsonParser parseValue = new JsonParser();
			final JsonElement jElem = parseValue.parse(jsonArrayTable);
			final JsonArray jsonArray = jElem.getAsJsonArray();

			for (int i = 0; i < jsonArray.size(); i++) {

				if ((jsonArray.get(i) != null)
						&& (!jsonArray.get(i).isJsonNull())) {
					JsonArray jArray = null;
					if (!jsonArray.get(i).isJsonArray()) {

						JsonParser jParser = new JsonParser();
						jArray = jParser.parse(jsonArray.get(i).getAsString())
								.getAsJsonArray();
					} else
						jArray = jsonArray.get(i).getAsJsonArray();
					
					String[] drwInfo = new String[jArray.size()];

					boolean flagIgnore = false;
					String tempName = "";

					for (int j = 0; j < jArray.size(); j++) {
						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull())) {

							String[] distributionData = jArray.get(j)
									.getAsString().split(",");

							String property = distributionData[0];
							String value = distributionData[1];

							if (property.equals("login")) {
								if (users.contains(value)) {
									flagIgnore = true;
								}

							}

							drwInfo[j] = jArray.get(j).getAsString();

						} else
							drwInfo[j] = "-";
					}
					if (!flagIgnore)
						drawingsList.add(drwInfo);
					else if (result.equals("")) {
						result = result + tempName;
					} else {
						result = result + "," + tempName;
					}

				}
			}
		}
		if (!result.equals("")) {
			LOG.info(msg + result);

		}
		return drawingsList;
	}

	public String getValforTypeForOldVersion(int sysid, int fieldid,
			String value) throws TbitsExceptionClient {

		String correspondingName = "";
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select name from types where sys_id=? and field_id=? and description=?");
			ps.setInt(1, sysid);
			ps.setInt(2, fieldid);
			ps.setString(3, value);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					correspondingName = rs.getString("name");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return correspondingName;
	}

	public String getValforType(int sysid, int fieldid, String value,
			String type_value_source) throws TbitsExceptionClient {

		String correspondingName = "";
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select name from types where sys_id=? and field_id=? and "
							+ type_value_source + "=?");

			// String query = "select name from types where " + filter +
			// " like '%" + value + "%' {escape '\\'}";

			ps.setInt(1, sysid);
			ps.setInt(2, fieldid);
			ps.setString(3, value);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					correspondingName = rs.getString("name");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return correspondingName;

	}

	public ArrayList<TrnEditableColumns> getEditabeAttachmentColumnsForTrnWizrad(
			int systemid, int processid) throws TbitsExceptionClient {
		HashMap<String, HashMap<String, String>> hm = new HashMap<String, HashMap<String, String>>();
		ArrayList<TrnEditableColumns> columnsList = new ArrayList<TrnEditableColumns>();

		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select a.name,a.data_type_id,a.column_order,b.name as property,a.field_id from trn_attachment_selection_table_columns a,fields b WHERE  a.data_type_id=11 and a.field_id=b.field_id and b.sys_id=? and TRN_PROCESS_ID=? and a.is_active=1 ORDER BY a.column_order");

			ps.setInt(1, systemid);
			ps.setInt(2, processid);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					String name = rs.getString("name");
					Integer order = rs.getInt("column_order");
					String property = rs.getString("property");
					Integer data_type_id = rs.getInt("data_type_id");
					Integer field_id = rs.getInt("field_id");

					TrnEditableColumns obj = new TrnEditableColumns();
					obj.setDataTypeId(data_type_id);
					obj.setORDER(order);
					obj.setPROPERTY(property);
					obj.setNAME(name);
					obj.setFIELDID(field_id);
					columnsList.add(obj);

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return columnsList;

	}

	public String getLoginForName(String name) throws TbitsExceptionClient {

		String login = "";
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select user_login from users  WHERE display_name=?");

			ps.setString(1, name);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					login = rs.getString("user_login");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return login;
	}

	public String getEmailForName(String name) throws TbitsExceptionClient {

		String login = "";
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select email from users  WHERE display_name=?");

			ps.setString(1, name);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					login = rs.getString("user_login");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return login;
	}

	public String getInactiveUserForNameAndEmail(String name, String email)
			throws TbitsExceptionClient {

		String in_active = null;
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select is_active from users  WHERE display_name=? and email=? and count(is_active)=1");

			ps.setString(1, name);
			ps.setString(1, email);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					in_active = rs.getString("user_login");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return in_active;
	}

	public HashMap<String, Object> getTansParamsBeforeTransmittal(
			String sysPrefix, int sysid, ArrayList<Integer> requetsIds)
			throws TbitsExceptionClient, NumberFormatException {

		HashMap<String, Object> hm = new HashMap<String, Object>();

		String statusOfDTNDate = "";
		String RoleNameForDtnDate = "HistoricalDateDTN";
		Boolean statusOfDtnDate = this.checkUserExistsInRole(sysid,
				RoleNameForDtnDate, getCurrentUser().getUserLogin());
		statusOfDTNDate = String.valueOf(statusOfDtnDate);
		hm.put("StatusOfDtnDate", statusOfDTNDate);

		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			// HashMap<String, Integer> defaultData =
			;
			HashMap<String, Integer> defaultData = this
					.getDefaultTransmittalProcessParams(sysid);
			if (defaultData != null && defaultData.size() > 0) {
				TbitsModelData processParams = this
						.getTransmittalProcessParameters(sysid, requetsIds,
								(Integer) defaultData.get(DROP_DOWN_ID));
				hm.put(TRANSMITTAL_PROCESS_PARAMS, processParams);
				hm.put(VALIDATION_RULES_LIST, this
						.getValidationRules((Integer) defaultData
								.get(PROCESS_ID)));

				ArrayList<TbitsModelData> distributionDataColumns = this
						.getDistributionTableColumns((Integer) defaultData
								.get(PROCESS_ID));

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
				// them. So, that they maintain the sort order and
				// hence the column order in the table.
				Collections.sort(distributionDataColumns, comp);

				hm
						.put(DISTRIBUTION_TABLE_COLUMNS_LIST,
								distributionDataColumns);

				ArrayList<TbitsModelData> AttachmentColumnList = this
						.getAttachmentSelectionTableColumns((Integer) defaultData
								.get(PROCESS_ID));

				Collections.sort(AttachmentColumnList, comp);

				hm.put(ATTACHMENT_TABLE_COLUMN_LIST, AttachmentColumnList);

				hm.put(TRN_EXTENDED_FIELDS, this
						.getTrnExtendedFields((Integer) defaultData
								.get(PROCESS_ID)));
				hm.put(MAP_OF_REQUESTS, this.getDataByRequestIds(sysPrefix,
						requetsIds));
				ArrayList<TbitsModelData> ddOptionList = getTransmittalDropDownOptions(sysid);
				hm.put("dropdownlist", this
						.getTransmittalDropDownOptions(sysid));

				String roleName = processParams.get("approvalRoleName");
				String statusOfFinishButton = "";

				Boolean statusOffinishButton = this.checkUserExistsInRole(
						sysid, roleName, getCurrentUser().getUserLogin());

				statusOfFinishButton = String.valueOf(statusOffinishButton);

				hm.put("statusOfFinishButton", statusOfFinishButton);

				TbitsModelData tmd = this.getTransmittalDropDownOption(sysid,
						defaultData.get(PROCESS_ID));
				hm.put("selected_dropdown_id", tmd.get("trndropdownId"));
				hm.put("selected_dropdown_name", tmd.get("name"));
				hm.put("defaultProcessExists", String.valueOf("true"));
				// hm.put(KEYFIELD_LIST,TransmittalDropDownOption.getTransmittalDropdownOtionsForkey(sysid,
				// ((Integer)
				// defaultData.get(PROCESS_ID)),((Integer)processParams.get(KEYFIELD))));

			} else {
				hm.put(MAP_OF_REQUESTS, this.getDataByRequestIds(sysPrefix,
						requetsIds));
				hm.put("defaultProcessExists", String.valueOf("false"));
				hm.put("dropdownlist", this
						.getTransmittalDropDownOptions(sysid));
			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return hm;

	}

	ArrayList<TbitsModelData> getTransmittalOptionForkeyFields(int sysid,
			int trnProcessId, int FieldId) throws SQLException {
		return TransmittalDropDownOption.getTransmittalDropdownOtionsForkey(
				sysid, trnProcessId, FieldId);
	}

	public HashMap<String, Integer> getDefaultTransmittalProcessParams(int sysid)
			throws TbitsExceptionClient, NumberFormatException {

		int count = 0;
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select trn_process_id,trn_dropdown_id from trn_processes where src_sys_id=? and is_default=1");

			ps.setInt(1, sysid);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					hm.put(PROCESS_ID, rs.getInt("trn_process_id"));
					hm.put(DROP_DOWN_ID, rs.getInt("trn_dropdown_id"));
					count++;
				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		if (count == 1) {
			return hm;
		} else {
			return null;
		}

	}

	/**
	 * (non-Javadoc)returns the hashmap of requestId and requestObject
	 * corresponding to a list of requestIds
	 * 
	 * @param SystemPrefix
	 *            of a BA
	 * @param ArrayList
	 *            of requestIds
	 * 
	 *            * @see transmittal.com.tbitsGlobal.client.TransmittalService#
	 *            getDataByRequestIds(java.lang.String, java.util.List)
	 */
	public HashMap<Integer, TbitsTreeRequestData> getDataByRequestIds(
			String sysPrefix, List<Integer> requestIds) {
		HashMap<Integer, TbitsTreeRequestData> resp = new HashMap<Integer, TbitsTreeRequestData>();

		String errorStringForMissingRequestId = "A few Requests have in deleted,Request ID's are: "
				+ "/n";
		for (int requestId : requestIds) {
			try {

				TbitsTreeRequestData model = getDataByRequestId(sysPrefix,
						requestId);
				if (model != null)
					resp.put(requestId, model);
				else
					errorStringForMissingRequestId = errorStringForMissingRequestId
							+ requestId + "" + "/n";
			} catch (TbitsExceptionClient e) {
				LOG.info(TBitsLogger.getStackTrace(e));
			}

		}

		if ((resp.size() != 0) && (errorStringForMissingRequestId.equals(""))) {
			Window.alert(errorStringForMissingRequestId);
		}
		return resp;
	}

	public UserClient getCurrentUser() throws TbitsExceptionClient {
		User user = null;
		try {
			user = WebUtil.validateUser(this.getRequest());

			if (user == null)
				return null;

			UserClient userClient = GWTServiceHelper.fromUser(user);

			userClient.setColPrefs(getColPreferences(user));

			return userClient;
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Get column preferences--column to be displayed and their size
	 * 
	 * @throws TbitsExceptionClient
	 * @throws SQLException
	 * @throws SQLException
	 */
	private HashMap<Integer, HashMap<Integer, List<ColPrefs>>> getColPreferences(
			User user) throws TbitsExceptionClient {
		HashMap<Integer, HashMap<Integer, List<ColPrefs>>> map = new HashMap<Integer, HashMap<Integer, List<ColPrefs>>>();

		int userId = user.getUserId();

		getColPreferences(userId, map, true);
		getColPreferences(0, map, false);

		return map;
	}

	private void getColPreferences(int userId,
			HashMap<Integer, HashMap<Integer, List<ColPrefs>>> map,
			boolean override) throws TbitsExceptionClient {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();

			String sql = "select distinct sys_id, view_id, field_id, max(col_size), \"order\" "
					+ "from user_grid_col_prefs "
					+ "WHERE user_id = ? "
					+ "group by sys_id, view_id, field_id, \"order\" "
					+ "order by \"order\" ASC";

			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);

			HashMap<Integer, HashMap<Integer, List<ColPrefs>>> localMap = new HashMap<Integer, HashMap<Integer, List<ColPrefs>>>();

			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Integer sysId = rs.getInt(1);
					Integer viewId = rs.getInt(2);

					Field field = Field.lookupBySystemIdAndFieldId(sysId, rs
							.getInt(3));
					if (field != null) {
						ColPrefs pref = new ColPrefs();
						pref.setName(field.getName());
						pref.setFieldId(rs.getInt(3));
						pref.set(ColPrefs.COLUMN_SIZE, rs.getInt(4));

						if (localMap.get(sysId) == null) {
							localMap.put(sysId,
									new HashMap<Integer, List<ColPrefs>>());
						}

						if (localMap.get(sysId).get(viewId) == null) {
							localMap.get(sysId).put(viewId,
									new ArrayList<ColPrefs>());
						}

						localMap.get(sysId).get(viewId).add(pref);
					}
				}
			}
			ps.close();

			for (int sysId : localMap.keySet()) {
				HashMap<Integer, List<ColPrefs>> viewMap = localMap.get(sysId);
				if (!map.containsKey(sysId)) {
					map.put(sysId, viewMap);
				} else {
					for (int viewId : viewMap.keySet()) {
						List<ColPrefs> prefs = viewMap.get(viewId);
						if (!map.get(sysId).containsKey(viewId)) {
							map.get(sysId).put(viewId, prefs);
						} else {
							if (override) {
								map.get(sysId).put(viewId, prefs);
							}
						}
					}
				}
			}

		} catch (SQLException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);

		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
	}

	public String getNameForLogin(String login) throws TbitsExceptionClient {

		String name = null;
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select display_name from users  WHERE user_login=? and is_active = 1");

			ps.setString(1, login);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					name = rs.getString("display_name");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return name;
	}

	public String getNameForInactiveLogin(String login)
			throws TbitsExceptionClient {

		String name = null;
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = connection
					.prepareStatement("select display_name from users  WHERE user_login=?");

			ps.setString(1, login);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					name = rs.getString("display_name");

				}

			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new TbitsExceptionClient(sqle);
				}
				connection = null;
			}
		}
		return name;
	}

	@Override
	public String createTransmittalPostApproval(int systemId, int requestId)
			throws TbitsExceptionClient {
		// TODO Auto-generated method stub
		return null;
	}
}
