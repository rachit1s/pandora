package transmittal.com.tbitsGlobal.server;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.User;
import transbit.tbits.domain.User.UserColumn;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import transmittal.com.tbitsGlobal.server.cache.PostTrnFieldMapCache;
import transmittal.com.tbitsGlobal.server.cache.SrcTargetFieldMapCache;
import transmittal.com.tbitsGlobal.server.cacheObjects.SrcTargetFieldMapObject;

/**
 * @author lokesh
 * 
 */
public final class TransmittalUtils {

	private static final String REVISION_DEFAULT = "default";

	private static final String DATA_TYPE_ID = "data_type_id";

	private static final String FIELD_CONFIG = "field_config";

	private static final String FIELD_ID = "field_id";

	private static final String BIRT_TEMPLATE_HANDLER = "BirtTemplateHandler";

	private static final String DIR_TBITSREPORTS = "tbitsreports";

	static final String TRN_POST_TRANSMITTAL_FIELD_VALUES = "trn_post_transmittal_field_values";

	static final String TRN_SRC_TARGET_FIELD_MAPPING_TABLE = "trn_src_target_field_mapping";

	protected static final String SRC_SYS_ID = "src_sys_id";

	protected static final String DCR_SYS_ID = "dcr_sys_id";

	public static final String EMPTY_STRING = "";

	public static final String STRING_DASH = "-";

	public static final String DELIMETER_COMMA = ",";

	public static final String DELIMETER_HASH = "#";

	public static final String DELIMETER_SEMICOLON = ";";

	public static final String PDF = ".pdf";

	static final String REVISION = "Revision";

	public static final int TRANSMITTAL_BUSINESS_AREA = 1;
	public static final int DCR_BUSINESS_AREA = 2;
	public static final int DTR_BUSINESS_AREA = 3;
	public static final int OTHER_BUSINESS_AREA = 4;

	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	public static String DTR = "DTR";

	public static String IDTR = "IDTR";

	private static final String APP_PROPERTIES = "app.properties";

	protected static final String TRN_PROCESS_ID = "trn_process_id";

	public static final String DTN_HEADER_IMAGE = "DTNHeaderImage";

	public static final String DTN_FOOTER_IMAGE = "DTNFooterImage";

	public static final String LOGGER_IMAGE_PATH = "loggerImagePath";

	// TODO: User db query to fetch this info, instead of hard coding.
	public static final String[] userTableColumnNames = { "user_id",
			"user_login", "first_name", "last_name", "display_name", "email",
			"is_active", "user_type_id", "web_config", "windows_config",
			"is_on_vacation", "is_display", "cn", "distinguished_name", "name",
			"member_of", "member", "mail_nickname", "location", "extension",
			"mobile", "home_phone", "firm_code", "designation", "firm_address",
			"sex", "full_firm_name" };

	/**
	 * Get the value of dtn signatory for the given transaction process id and
	 * given system id
	 * 
	 * @param trnProcessId
	 * @return DtnSignatory for the given process
	 */
	public static String getDtnSignatory(Integer trnProcessId) {
		String dtnSignatory = null;

		Connection conn = null;

		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			PreparedStatement ps = conn
					.prepareStatement("SELECT value from trn_process_parameters"
							+ " WHERE trn_process_id=? and parameter = 'dtnSignatory'");
			ps.setInt(1, trnProcessId);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				if (rs.next()) {
					dtnSignatory = rs.getString(1);
				}
				rs.close();
			}
			ps.close();
			ps = null;
			conn.close();

		} catch (SQLException e) {
			// TODO: Log it --SG
			System.out.println("Could not get dtnSignatory from database....");
			e.printStackTrace();
			e.printStackTrace();
		}

		return dtnSignatory;
	}

	public static File getResourceFile(String filePath) {
		URL url = TransmittalUtils.class.getResource(filePath);
		String file = url.getFile();
		File f = new File(file);
		return f;
	}

	public static String getProperty(String propertyName) {
		URL url = TransmittalUtils.class.getResource(APP_PROPERTIES);
		String file = url.getFile();
		File f = new File(file);
		if (f.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(f));
				String baPrefix = props.getProperty(propertyName);
				return baPrefix;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			LOG.error(f.getAbsolutePath()
					+ " file is missing. Please check if it exists.");
		}
		return null;
	}

	public static String generateTransmittalNoteUsingBirt(
			String rptDesignFileName, BirtTemplateHelper kth,
			String outputFileName) throws BirtException, TBitsException {

		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		String tempDir = Configuration.findAbsolutePath(PropertiesHandler
				.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		outputFileName = outputFileName.replaceAll("[^A-Za-z0-9]+", "_");
		String pdfFilePath = tempDir + File.separator + outputFileName + PDF;
		File outFile = new File(pdfFilePath);
		reportVariables.put(BIRT_TEMPLATE_HANDLER, kth);
		File generatedPDFFile = tBitsEngine.generatePDFFile(rptDesignFileName,
				reportVariables, reportParams, outFile);
		return generatedPDFFile.getAbsolutePath();
	}

	public static String generateTransmittalNoteUsingBirtForGivenFormat(
			String rptDesignFileName, BirtTemplateHelper kth,
			String outputFileName, String format) throws BirtException,
			TBitsException {

		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		String tempDir = Configuration.findAbsolutePath(PropertiesHandler
				.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		outputFileName = outputFileName.replaceAll("[^A-Za-z0-9]+", "_");
		String FilePath = tempDir + File.separator + outputFileName + format;
		File outFile = new File(FilePath);
		reportVariables.put(BIRT_TEMPLATE_HANDLER, kth);
		File generatedFile = tBitsEngine.generateReportFile(rptDesignFileName,
				reportVariables, reportParams, outFile, format.substring(1));
		return generatedFile.getAbsolutePath();
	}

	public static ByteArrayOutputStream generateTransmittalNoteInHtml(
			String rptDesignFileName, BirtTemplateHelper kth, String contextPath)
			throws EngineException, IOException, TBitsException {

		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		String tempDir = Configuration.findAbsolutePath(PropertiesHandler
				.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		reportVariables.put(BIRT_TEMPLATE_HANDLER, kth);

		// Report Design
		LOG.info("Opening RPTDesign file for preview.");
		LOG.info("Rendering...");

		// Setup rendering to HTML
		HTMLRenderOption options = new HTMLRenderOption();
		options.setImageHandler(new HTMLServerImageHandler());

		ByteArrayOutputStream htmlOS = new ByteArrayOutputStream();
		options.setOutputStream(htmlOS);
		options.setOutputFormat("html");

		options.setBaseImageURL(contextPath + "/web/images/dashboard_images");
		options.setImageDirectory(tempDir
				+ "/../webapps/web/images/dashboard_images");

		// Setting this to true removes html and body tags
		options.setEmbeddable(true);
		tBitsEngine.generateReportFile(rptDesignFileName, reportVariables,
				reportParams, options);
		return htmlOS;
	}

	/**
	 * Looks up the MailingList membership of a user using his loginName/emailId
	 * 
	 * @param userName
	 * @return ArrayList<User> representing a list of mailing lists.
	 */
	public static ArrayList<User> getMailList(String userName) {
		User user = null;
		ArrayList<User> mailList = null;
		try {
			user = User.lookupByEmail(userName);
			if (user == null)
				user = User.lookupAllByUserLogin(userName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		if (user != null) {
			mailList = MailListUser.getMailListsByDirectMembership(user
					.getUserId());
		}
		return mailList;
	}

	public static boolean isUserExists(ArrayList<RequestUser> ruList,
			String userName) {
		for (RequestUser ru : ruList) {
			try {
				User usr = ru.getUser();
				if ((usr.getUserLogin().equals(userName) || (usr.getEmail()
						.equals(userName)))) {
					return true;
				} else
					return false;
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param ba
	 * @param requestId
	 * @return
	 */
	public static String getLinkedRequests(BusinessArea ba, String[] requestId) {
		String linkedRequests = "";
		for (int index = 0; index < requestId.length; index++) {
			if (index == 0)
				linkedRequests = ba.getSystemPrefix() + DELIMETER_HASH
						+ requestId[index];
			else
				linkedRequests = linkedRequests
						+ TransmittalUtils.DELIMETER_COMMA
						+ ba.getSystemPrefix() + DELIMETER_HASH
						+ requestId[index];
		}
		return linkedRequests;
	}

	/**
	 * @param transId
	 *            Transmittal Id.
	 * @return returns a String representing transmittal request id
	 */
	public static String getFormattedStringFromNumber(int transmittalId) {
		String ref;
		NumberFormat formatter = new DecimalFormat("00000");
		ref = formatter.format(transmittalId);
		return ref;
	}

	/**
	 * Merges two Collections. To be used when adding new attachments into a
	 * request. This method, checks if an attachments with the same name existed
	 * previously and modifies the new attachments collection.
	 * 
	 * @param newAttachments
	 * @param prevAttachments
	 */
	public static void mergeAttachmentsLists(
			Collection<AttachmentInfo> newAttachments,
			Collection<AttachmentInfo> prevAttachments) {
		// If no previous attachments were found return without adding anything
		// to newAttachments.
		if ((prevAttachments == null) || prevAttachments.isEmpty())
			return;

		// If no new attachments are there, add previous attachments to the new
		// attachments collection,
		// so they are retained in the request.
		if ((newAttachments == null) || newAttachments.isEmpty())
			newAttachments = prevAttachments;
		else {
			Collection<AttachmentInfo> oldAI = new ArrayList<AttachmentInfo>();
			if ((newAttachments != null) && (!newAttachments.isEmpty())) {
				for (AttachmentInfo ai : prevAttachments) {
					boolean isFound = false;
					for (AttachmentInfo cAI : newAttachments) {
						if (ai.name.equals(cAI.name)) {
							cAI.requestFileId = ai.requestFileId;
							isFound = true;
							break;
						} else
							isFound = false;
					}
					if (!isFound)
						oldAI.add(ai);
				}
				newAttachments.addAll(oldAI);
			}
		}
	}

	public static ArrayList<String> getLatestBAAssigneeList(
			Hashtable<String, String> userListMapping) {
		ArrayList<String> assingneeList = new ArrayList<String>();
		Set<String> keySet = userListMapping.keySet();
		for (String key : keySet) {
			assingneeList.add(key.trim());
		}
		return assingneeList;
	}

	/**
	 * Retrieves the target business area fields for updation after transmittal.
	 * 
	 * @param connection
	 * @param dcrSysId
	 * @param targetSysId
	 * @return
	 * @throws DatabaseException
	 */
	public static Hashtable<String, String> getTargetBusinessAreaFields(
			Connection connection, int trnProcessId, int dcrSysId,
			int targetSysId) throws DatabaseException {
		Hashtable<String, String> fieldMap = new Hashtable<String, String>();
		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT src_field_id, target_field_id FROM "
							+ TRN_SRC_TARGET_FIELD_MAPPING_TABLE
							+ " WHERE trn_process_id=? and src_sys_id=? and target_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrSysId);
			ps.setInt(3, targetSysId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					int dcrFieldId = rs.getInt("src_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					Field dcrField = Field.lookupBySystemIdAndFieldId(dcrSysId,
							dcrFieldId);
					Field targetField = Field.lookupBySystemIdAndFieldId(
							targetSysId, targetFieldId);
					if (dcrField == null)
						LOG
								.warn("Skipping copying the value of field with DCR field Id: "
										+ dcrFieldId
										+ ", for business area with id: "
										+ dcrSysId);
					else if (targetField == null)
						LOG
								.warn("Skipping copying the value of field with Target BA field Id: "
										+ targetFieldId
										+ ", for business area with id: "
										+ targetSysId);
					else if ((dcrField != null) && (targetField != null)) {
						if (dcrField.getDataTypeId() != DataType.ATTACHMENTS)
							fieldMap.put(dcrField.getName(), targetField
									.getName());
					}
				}
			}
			return fieldMap;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while retrieving mapped fields.",
					sqle);
		}
	}

	/**
	 * CACHE ENABLED.................
	 */
	public static Hashtable<String, String> getTargetBusinessAreaFieldsByTrnProcessIdAndSrcFieldId(
			Connection connection, int trnProcessId, int dcrSysId,
			int targetSysId) throws DatabaseException {
		Hashtable<String, String> fieldMap = new Hashtable<String, String>();

		SrcTargetFieldMapObject keyObject = new SrcTargetFieldMapObject();
		keyObject.setTrnProcessId(Integer.valueOf(trnProcessId));
		keyObject.setTargetSysId(Integer.valueOf(targetSysId));
		keyObject.setSrcSysId(Integer.valueOf(dcrSysId));

		fieldMap.putAll(SrcTargetFieldMapCache.getInstance()
				.getSrcTargetFieldMap(keyObject));
		return fieldMap;
		// fieldMap.putAll(CacheHandler.getInstance().getSrcTargetFieldMap(trnProcessId,
		// dcrSysId, targetSysId));
		// return fieldMap;
		// try {
		// PreparedStatement ps =
		// connection.prepareStatement("SELECT src_field_id, target_field_id FROM "
		// + TRN_SRC_TARGET_FIELD_MAPPING_TABLE
		// + " WHERE  trn_process_id=? and target_sys_id=?");
		// ps.setInt(1, trnProcessId);
		// ps.setInt(2, targetSysId);
		// ResultSet rs = ps.executeQuery();
		// if(rs != null){
		// while (rs.next()){
		// int dcrFieldId = rs.getInt("src_field_id");
		// int targetFieldId = rs.getInt("target_field_id");
		// Field dcrField = Field.lookupBySystemIdAndFieldId( dcrSysId,
		// dcrFieldId);
		// Field targetField = Field.lookupBySystemIdAndFieldId(targetSysId,
		// targetFieldId);
		// if (dcrField == null)
		// LOG.warn("Skipping copying the value of field with DCR field Id: " +
		// dcrFieldId
		// + ", for business area with id: " + dcrSysId);
		// else if (targetField == null)
		// LOG.warn("Skipping copying the value of field with Target BA field Id: "
		// + targetFieldId
		// + ", for business area with id: " + targetSysId);
		// else if ((dcrField != null) && (targetField != null)){
		// if (dcrField.getDataTypeId() != DataType.ATTACHMENTS){
		// String targetFieldNameList = fieldMap.get(dcrField.getName());
		// if ((targetFieldNameList == null) ||
		// (targetFieldNameList.trim().equals("")))
		// fieldMap.put(dcrField.getName(), targetField.getName());
		// else
		// fieldMap.put(dcrField.getName(), targetFieldNameList + "," +
		// targetField.getName());
		// }
		// }
		// }
		// }
		// return fieldMap;
		// } catch (SQLException sqle) {
		// sqle.printStackTrace();
		// throw new
		// DatabaseException("Database error occurred while retrieving mapped fields.",
		// sqle);
		// }
	}

	/**
	 * Retrieves the target business area fields for updation after transmittal.
	 * 
	 * @param connection
	 * @param dcrSysId
	 * @param targetSysId
	 * @return
	 * @throws DatabaseException
	 */
	public static Field getTargetBusinessAreaField(Connection connection,
			int trnProcessId, int dcrSysId, int dcrFieldId, int targetSysId)
			throws DatabaseException {
		Field targetField = null;
		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT src_field_id, target_field_id FROM "
							+ TRN_SRC_TARGET_FIELD_MAPPING_TABLE
							+ " WHERE trn_process_id=? and "
							+ "and src_field_id=? and target_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrSysId);
			ps.setInt(3, dcrFieldId);
			ps.setInt(4, targetSysId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					// int dcrFieldId = rs.getInt("dcr_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					targetField = Field.lookupBySystemIdAndFieldId(targetSysId,
							targetFieldId);
				}
			}
			return targetField;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while retrieving mapped fields.",
					sqle);
		}
	}

	public static Field getTargetBusinessAreaField(Connection connection,
			int trnProcessId, int dcrFieldId, int targetSysId)
			throws DatabaseException {
		Field targetField = null;
		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT src_field_id, target_field_id FROM "
							+ TRN_SRC_TARGET_FIELD_MAPPING_TABLE
							+ " WHERE trn_process_id=? and src_field_id=? and target_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrFieldId);
			ps.setInt(3, targetSysId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					// int dcrFieldId = rs.getInt("dcr_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					targetField = Field.lookupBySystemIdAndFieldId(targetSysId,
							targetFieldId);
				}
			}
			return targetField;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while retrieving mapped fields.",
					sqle);
		}
	}

	/**
	 * Retrieves the target business area fields for updation after transmittal.
	 * 
	 * @param connection
	 * @param dcrSysId
	 * @param targetSysId
	 * @return
	 * @throws DatabaseException
	 * @throws TBitsException
	 * @deprecated
	 */
	public static void getTargetBusinessAreaFieldsAndValues(
			Connection connection, int dcrSysId, Request dcrRequest,
			int targetSysId, int transmittalTypeId, String formattedDTNNumber,
			String dtnSysPrefix, int dtnRequestId, boolean isAddRequest,
			Hashtable<String, String> paramTable) throws DatabaseException,
			TBitsException {

		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ TRN_POST_TRANSMITTAL_FIELD_VALUES + " WHERE "
					+ SRC_SYS_ID + "=? and target_sys_id=? and "
					+ TRN_PROCESS_ID + "=?");
			ps.setInt(1, dcrSysId);
			ps.setInt(2, targetSysId);
			ps.setInt(3, transmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					int targetFieldId = rs.getInt("target_field_id");
					String fieldValue = rs.getString("target_field_value");
					Field targetField = Field.lookupBySystemIdAndFieldId(
							targetSysId, targetFieldId);
					if (targetField != null) {
						switch (targetField.getDataTypeId()) {
						// ((targetField.getDataTypeId() == ) ||
						// (targetField.getDataTypeId() == DataType.DATETIME)){
						case DataType.DATE:
						case DataType.DATETIME: {
							if (fieldValue != null) {
								int slideOffset = 0;
								if (fieldValue.trim().equals(""))
									paramTable.put(targetField.getName(), "");
								else {
									String[] dateOffset = fieldValue.split(",");
									if (dateOffset.length == 1) {
										slideOffset = Integer
												.parseInt(dateOffset[0]);
									} else if (dateOffset.length == 2) {
										int firstRevisionOffset = Integer
												.parseInt(dateOffset[0]);
										int subsequentRevisionOffset = Integer
												.parseInt(dateOffset[1]);
										if (firstRevisionOffset == subsequentRevisionOffset)
											slideOffset = Integer
													.parseInt(dateOffset[0]);
										else {
											slideOffset = TransmittalUtils
													.getResponseDateOffset(
															connection,
															targetSysId,
															dcrRequest,
															paramTable,
															firstRevisionOffset,
															subsequentRevisionOffset,
															isAddRequest);
										}
									}
									if (slideOffset != 0) {
										Timestamp gmtDueDate = TransmittalUtils
												.getSlidedDueDate(slideOffset);
										paramTable
												.put(
														targetField.getName(),
														gmtDueDate
																.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
									}
								}
							} else
								LOG
										.warn("No value provided for field: "
												+ targetField.getDisplayName()
												+ ", hence ignoring it in post transmittal update.");
							break;
						}
						case DataType.TEXT:
						case DataType.STRING: {
							if (targetField.getName().equals(Field.DESCRIPTION)) {
								String dtnNumberAndLinkString = formattedDTNNumber
										+ "["
										+ dtnSysPrefix
										+ "#"
										+ dtnRequestId + "]";
								if ((fieldValue == null)
										|| fieldValue.trim().equals("")) {
									paramTable.put(Field.DESCRIPTION,
											"Document submitted via transmittal number: "
													+ dtnNumberAndLinkString);
								} else {
									paramTable
											.put(Field.DESCRIPTION, fieldValue
													+ dtnNumberAndLinkString);
								}
							} else {
								if (fieldValue == null)
									LOG
											.warn("No value provided for field: "
													+ targetField
															.getDisplayName()
													+ ", hence ignoring it in post transmittal update.");
								else
									paramTable.put(targetField.getName(),
											fieldValue);
							}
							break;
						}
						default:
							paramTable.put(targetField.getName(), fieldValue);
						}
					} else
						LOG
								.warn("Skipped setting the value of field with field-id: "
										+ targetFieldId
										+ ", for business area with system id: "
										+ targetSysId);
				}
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			throw new TBitsException(
					"Error occurred while parsing date-offset of a date field.",
					nfe);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while retrieving mapped fields.",
					sqle);
		}
	}

	/**
	 * Retrieves the target business area fields for updation after transmittal.
	 * 
	 * @param connection
	 * @param dcrSysId
	 * @param targetSysId
	 * @return
	 * @throws DatabaseException
	 * @throws TBitsException
	 */
	public static void getTargetBusinessAreaFieldsAndValues(
			Connection connection, Request dcrRequest, int targetSysId,
			int transmittalTypeId, String formattedDTNNumber,
			String dtnSysPrefix, Request trnRequest, boolean isAddRequest,
			Hashtable<String, String> paramTable, String calendarName)
			throws DatabaseException, TBitsException {

		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ TRN_POST_TRANSMITTAL_FIELD_VALUES
					+ " WHERE target_sys_id=? and " + TRN_PROCESS_ID + "=?");
			ps.setInt(1, targetSysId);
			ps.setInt(2, transmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					int targetFieldId = rs.getInt("target_field_id");
					String fieldValue = rs.getString("target_field_value");
					Field targetField = Field.lookupBySystemIdAndFieldId(
							targetSysId, targetFieldId);
					if (targetField != null) {
						switch (targetField.getDataTypeId()) {
						// ((targetField.getDataTypeId() == ) ||
						// (targetField.getDataTypeId() == DataType.DATETIME)){
						case DataType.DATE:
						case DataType.DATETIME: {
							if (fieldValue != null) {
								int slideOffset = 0;
								if (fieldValue.trim().equals(""))
									paramTable.put(targetField.getName(), "");
								else {
									try {
										slideOffset = TransmittalUtils
												.getSlideOffsetBasedOnRevisionFieldAndType(
														connection, dcrRequest,
														targetSysId, fieldValue
																.trim());
										if (slideOffset != -1
												&& slideOffset != 0) {
											Date dcrRequestLoggedDate = null;
											if ((trnRequest
													.getObject(ACTUAL_TRANSMITTAL_DATE) != null)) {
												dcrRequestLoggedDate = (Date) trnRequest
														.getObject(ACTUAL_TRANSMITTAL_DATE);
											} else
												dcrRequestLoggedDate = Calendar
														.getInstance(
																TimeZone
																		.getTimeZone("IST"))
														.getTime();

											Timestamp gmtDueDate = TransmittalUtils
													.getSlidedDueDate(
															dcrRequestLoggedDate,
															slideOffset,
															calendarName);
											// TODO: Historical date input
											// accommodation if required.
											// Timestamp gmtDueDate =
											// TransmittalUtils.getSlidedDueDate(slideOffset,
											// calendarName);
											paramTable
													.put(
															targetField
																	.getName(),
															gmtDueDate
																	.toCustomFormat(TBitsConstants.API_DATE_FORMAT));
										}
										if (slideOffset == -1) {
											paramTable.put(targetField
													.getName(), "");

										}
									} catch (JsonParseException jpe) {
										jpe.printStackTrace();
										throw new TBitsException(
												"Error occurred while setting date-offset for date field: "
														+ targetField.getName()
														+ ", in BA: "
														+ targetSysId
														+ ". Invalid (JSON)configuration in post transmittal field values table.",
												jpe);
									} catch (IllegalStateException ise) {
										ise.printStackTrace();
										throw new TBitsException(
												"Error occurred while setting date-offset for date field: "
														+ targetField.getName()
														+ ", in BA: "
														+ targetSysId, ise);
									}
								}
							} else
								LOG
										.warn("No value provided for field: "
												+ targetField.getDisplayName()
												+ ", hence ignoring it in post transmittal update.");
							break;
						}
						case DataType.TEXT:
						case DataType.STRING: {
							if (targetField.getName().equals(Field.DESCRIPTION)) {
								String dtnNumberAndLinkString = formattedDTNNumber
										+ "["
										+ dtnSysPrefix
										+ "#"
										+ trnRequest.getRequestId() + "]";
								if ((fieldValue == null)
										|| fieldValue.trim().equals("")) {
									paramTable.put(Field.DESCRIPTION,
											"Document submitted via transmittal number: "
													+ dtnNumberAndLinkString);
								} else {
									paramTable
											.put(Field.DESCRIPTION, fieldValue
													+ dtnNumberAndLinkString);
								}
							} else {
								if (fieldValue == null)
									LOG
											.warn("No value provided for field: "
													+ targetField
															.getDisplayName()
													+ ", hence ignoring it in post transmittal update.");
								else
									paramTable.put(targetField.getName(),
											fieldValue);
							}
							break;
						}
						default:
							paramTable.put(targetField.getName(), fieldValue);
						}
					} else
						LOG
								.warn("Skipped setting the value of field with field-id: "
										+ targetFieldId
										+ ", for business area with system id: "
										+ targetSysId);
				}
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			throw new TBitsException(
					"Error occurred while parsing date-offset of a date field.",
					nfe);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while retrieving mapped fields.",
					sqle);
		}
	}

	public static void getPostTrnBusinessAreaFieldsAndValues(
			Connection connection, BusinessArea dtnBA,
			TransmittalProcess trnProcess,
			Hashtable<String, String> paramTable,
			HashMap<String, String> trnProcessParamTable, String dtnNumber)
			throws DatabaseException, TBitsException {

		/**
		 * TODO: CHeck again
		 */
		PostTrnFieldMapCache.getInstance()
				.getPostTrnBusinessAreaFieldsAndValues(paramTable, dtnBA,
						trnProcess);

		// if ((dtnBA.getSystemId() == trnProcess.getDtnSysId()) ||
		// (dtnBA.getSystemId() == trnProcess.getDtrSysId())){
		//		
		// try {
		// PreparedStatement ps = connection.prepareStatement("SELECT * FROM " +
		// TRN_POST_TRANSMITTAL_FIELD_VALUES +
		// " WHERE target_sys_id=? and " + TRN_PROCESS_ID + "=?");
		// ps.setInt(1, dtnBA.getSystemId());
		// ps.setInt(2, trnProcess.getTrnProcessId());
		// ResultSet rs = ps.executeQuery();
		// if(rs != null){
		// while (rs.next()){
		// int targetFieldId = rs.getInt("target_field_id");
		// String fieldValue = rs.getString("target_field_value");
		// Field targetField =
		// Field.lookupBySystemIdAndFieldId(dtnBA.getSystemId(), targetFieldId);
		// if (targetField != null){
		// switch (targetField.getDataTypeId()){
		// // ((targetField.getDataTypeId() == ) || (targetField.getDataTypeId()
		// == DataType.DATETIME)){
		// case DataType.DATE:
		// case DataType.DATETIME:{
		// if (fieldValue != null){
		// int slideOffset = 0;
		// if (fieldValue.trim().equals(""))
		// paramTable.put(targetField.getName(), "");
		// else{
		// // TODO: Not handling as of now. If a logic is required will be added
		// as per requirement.
		// }
		// }
		// else
		// LOG.warn("No value provided for field: " +
		// targetField.getDisplayName() +
		// ", hence ignoring it in post transmittal update.");
		// break;
		// }
		// case DataType.TEXT:
		// case DataType.STRING:{
		// if (targetField.getName().equals(Field.DESCRIPTION)){
		// // TODO: Not handling as of now. If a logic is required will be added
		// as per requirement.
		// }
		// else{
		// if (fieldValue == null)
		// LOG.warn("No value provided for field: " +
		// targetField.getDisplayName() +
		// ", hence ignoring it in post transmittal update.");
		// else
		// paramTable.put(targetField.getName(), fieldValue);
		// }
		// break;
		// }
		// default:
		// paramTable.put(targetField.getName(), fieldValue);
		// }
		// }
		// else
		// LOG.warn("Skipped setting the value of field with field-id: " +
		// targetFieldId
		// + ", for business area with system id: " + dtnBA.getSystemPrefix());
		// }
		// }
		// }catch (NumberFormatException nfe){
		// nfe.printStackTrace();
		// throw new
		// TBitsException("Error occurred while parsing date-offset of a date field.",
		// nfe);
		// }catch (SQLException sqle) {
		// sqle.printStackTrace();
		// throw new
		// DatabaseException("Database error occurred while retrieving mapped fields.",
		// sqle);
		// }
		// }
	}

	public static int getResponseDateOffset(Connection connection,
			int currentBASystemId, Request dcrRequest,
			Hashtable<String, String> paramTable, int addRequestOffset,
			int updateRequestOffset, boolean isAddRequest)
			throws TBitsException {
		int slideOffset = 0;
		// If the request is being added for the first time, then revision is
		// assumed to be 'R0'
		String curRevision = dcrRequest.get(REVISION).trim();
		if (isAddRequest) {
			if (curRevision.equals("R0"))
				slideOffset = addRequestOffset;
			else
				slideOffset = updateRequestOffset;
		} else if (curRevision.equals("R0")) {
			slideOffset = addRequestOffset;
		} else if (curRevision.equals("R1"))
			slideOffset = updateRequestOffset;
		return slideOffset;
	}

	/**
	 * This method returns the BusinessArea object corresponding to the given
	 * System Id.
	 * 
	 * @param aSystemId
	 *            Business Area Id.
	 * 
	 * @return BusinessArea object corresponding to this SystemId
	 * 
	 * @exception DatabaseException
	 *                incase of any error while interacting with the database.
	 */
	public static BusinessArea lookupBySystemId(Connection connection,
			int aSystemId) throws DatabaseException {
		BusinessArea ba = null;
		/*
		 * // Look in the mapper first. String key =
		 * Integer.toString(aSystemId);
		 * 
		 * if (ourBAMap != null) { ba = ourBAMap.get(key);
		 * 
		 * return ba; }
		 * 
		 * // else try to get the BA record from the database. // Connection
		 * connection = null;
		 */
		try {
			// connection = DataSourcePool.getConnection();

			CallableStatement cs = connection
					.prepareCall("stp_ba_lookupBySystemId ?");

			cs.setInt(1, aSystemId);

			ResultSet rs = cs.executeQuery();

			if (rs != null) {
				if (rs.next() != false) {
					ba = BusinessArea.createFromResultSet(rs);
				}

				// Close the result set.
				rs.close();
			}

			// Close the statement.
			cs.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occurred while retrieving the ")
					.append("BusinessArea Object.").append("\nSystem Id: ")
					.append(aSystemId).append("\n");

			throw new DatabaseException(message.toString(), sqle);
		}

		return ba;
	}

	public static Field lookupBySystemIdAndFieldId(Connection connection,
			int aSystemId, int aFieldId) throws DatabaseException {
		Field field = null;

		try {
			CallableStatement cs = connection
					.prepareCall("stp_field_lookupBySystemIdAndFieldId ?, ?");

			cs.setInt(1, aSystemId);
			cs.setInt(2, aFieldId);

			ResultSet rs = cs.executeQuery();

			if (rs != null) {
				if (rs.next() != false) {
					field = Field.createFromResultSet(rs);
				}

				// Close the result set.
				rs.close();
			}

			// Close the statement.
			cs.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occured while retrieving the field.")
					.append("\nSystem Id: ").append(aSystemId).append(
							"\nField Id : ").append(aFieldId).append("\n");

			throw new DatabaseException(message.toString(), sqle);
		}

		return field;
	}

	public static Field lookupBySystemIdAndFieldName(Connection connection,
			int aSystemId, String aFieldName) throws DatabaseException {
		Field field = null;

		try {
			CallableStatement cs = connection
					.prepareCall("stp_field_lookupBySystemIdAndFieldName ?, ?");

			cs.setInt(1, aSystemId);
			cs.setString(2, aFieldName);

			ResultSet rs = cs.executeQuery();

			if (rs != null) {
				if (rs.next() != false) {
					field = Field.createFromResultSet(rs);
				}

				// Close the result set.
				rs.close();
			}

			// Close the statement.
			cs.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occured while retrieving the field.")
					.append("\nSystem Id : ").append(aSystemId).append(
							"\nField Name: ").append(aFieldName).append("\n");

			throw new DatabaseException(message.toString(), sqle);
		}

		return field;
	}

	/**
	 * This method prepares the data requires by the transmittal
	 * template(.rptdesign) for creating a transmittal template.
	 * 
	 * @param dcrSystemId
	 * @param requestList
	 * @param approvalCategoryList
	 * @param documentList
	 * @param quantityList
	 * @param distributionList
	 * @param transmittalId
	 * @param transmittalSubject
	 * @param user
	 *            - Logger object
	 * @param inwardDTNFieldName
	 *            - List of all reference DTNs through which the transmittal of
	 *            drawings had occurred previously from the other firm.
	 * @param transmittalDate
	 * @param transmittalProcessParameters
	 * @return
	 * @throws NumberFormatException
	 * @throws DatabaseException
	 * @throws TBitsException
	 * 
	 */
	@Deprecated
	public static BirtTemplateHelper getBirtTemplateHelper(int dcrSystemId,
			String[] dcrRequestList, String approvalCategoryList,
			String documentTypeList, String quantityList, String summaryList,
			String toList, String ccList, String transmittalId,
			String transmittalSubject, String remarks, User user,
			String inwardDTNFieldName, String toAddress, String emailBody,
			String yourReference, Hashtable<String, String> transmittalParams,
			String transmittalDate)// , String reference)
			throws NumberFormatException, DatabaseException, TBitsException {

		return null;
	}

	/**
	 * @param userLoginList
	 * @return
	 * @throws DatabaseException
	 */
	protected static ArrayList<String[]> getUserInfoList(String userLoginList)
			throws DatabaseException {
		ArrayList<String[]> userInfoList = new ArrayList<String[]>();
		if ((userLoginList == null) || (userLoginList.isEmpty()))
			return userInfoList;
		String[] userLogins = userLoginList.split(",");
		for (int index = 0; index < userLogins.length; index++) {
			User recipient = User.lookupByUserLogin(userLogins[index]);
			if (recipient == null)
				recipient = User.lookupByEmail(userLogins[index]);

			if (recipient != null) {
				if (!isExistsInDistributionList(userInfoList, userLogins[index])) {
					int slNumber = index + 1;
					String rPhone = recipient.getMobile();
					if ((rPhone == null) || rPhone.trim().equals(EMPTY_STRING))
						rPhone = STRING_DASH;

					String organization = "-";
					organization = recipient.getFirmCode();
					LOG.info("!!!!!!!!!!Organization of " + recipient.getName()
							+ ":" + organization);
					if ((organization == null)
							|| organization.trim().equals(""))
						organization = STRING_DASH;

					// System.out.println("DistList:\n" + slNumber + "," +
					// recipient.getDisplayName() + "," + organization +
					// "," + recipient.getEmail() + "," + recipient.getMobile()
					// + "," + STRING_DASH + "," + STRING_DASH + ","
					// + recipient.getUserLogin());
					userInfoList.add(new String[] { slNumber + "",
							recipient.getDisplayName(), organization,
							recipient.getEmail(), rPhone, STRING_DASH,
							STRING_DASH, recipient.getUserLogin() });
				} else
					continue;
			} else
				LOG
						.info("User with login: \""
								+ userLogins[index]
								+ "\", could not be found in tBits. So, igonring him from the list.");
		}
		return userInfoList;
	}

	/**
	 * @param userLoginList
	 * @return
	 * @throws DatabaseException
	 */
	protected static String getUserDisplayNameList(String userLoginList)
			throws DatabaseException {
		String userDisplayNameList = "";
		if ((userLoginList == null) || (userLoginList.isEmpty()))
			return userDisplayNameList;
		String[] userLogins = userLoginList.split(",");
		for (int index = 0; index < userLogins.length; index++) {
			User recipient = User.lookupByUserLogin(userLogins[index]);
			if (recipient == null) {
				recipient = User.lookupByEmail(userLogins[index]);
			}

			if (recipient != null) {
				userDisplayNameList = (userDisplayNameList == "") ? recipient
						.getDisplayName() : userDisplayNameList + ","
						+ recipient.getDisplayName();
			} else
				LOG
						.info("User with login: \""
								+ userLogins[index]
								+ "\", could not be found in tBits. So, igonring him from the list.");

		}
		return userDisplayNameList;
	}

	private static boolean isExistsInDistributionList(
			ArrayList<String[]> distributionUserList, String userName) {
		boolean isExists = false;
		userName = userName.trim();

		for (String[] userInfo : distributionUserList) {
			// Matches email or login.
			if (userInfo[3].equals(userName) || userInfo[7].equals(userName)) {
				isExists = true;
				return isExists;
			} else
				continue;
		}
		return isExists;
	}

	public static ArrayList<Integer> lookupAllDCRBusinessAreaIds()
			throws DatabaseException {
		ArrayList<Integer> dcrBAIdList;
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			dcrBAIdList = lookupAllDCRBusinessAreaIds(connection);
		} catch (SQLException sqle) {
			throw new DatabaseException(
					"Exception occurred while retrieving DCR business areas.",
					sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}

		return dcrBAIdList;
	}

	public static ArrayList<Integer> lookupAllDCRBusinessAreaIds(
			Connection connection) throws DatabaseException {
		ArrayList<Integer> dcrBAIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection
					.prepareStatement("Select DISTINCT " + SRC_SYS_ID
							+ " from trn_processes");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while (rs.next()) {
					int dcrSysId = rs.getInt(SRC_SYS_ID);
					dcrBAIdList.add(dcrSysId);
				}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException(
					"Exception occurred while retrieving DCR business areas.",
					sqle);
		}
		return dcrBAIdList;
	}

	public static boolean isExistsInDCRBAList(int systemId) {
		ArrayList<Integer> baIds = null;
		try {
			baIds = lookupAllDCRBusinessAreaIds();
			for (Integer baId : baIds) {
				if (baId.intValue() == systemId)
					return true;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static Timestamp getSlidedDueDate(int slideOffset) {
		Calendar cal = Calendar.getInstance();
		Date slidedDate = CalenderUtils.slideDate(cal.getTime(), slideOffset,
				new MyHolidayCalendar());
		Timestamp gmtDate = Timestamp.getTimestamp(slidedDate);
		return gmtDate;
	}

	public static Timestamp getSlidedDueDate(int slideOffset,
			String calendarName) {
		Calendar cal = Calendar.getInstance();
		Date slidedDate = CalenderUtils.slideDate(cal.getTime(), slideOffset,
				new GenericHolidayCalendar(calendarName));
		Timestamp gmtDate = Timestamp.getTimestamp(slidedDate);
		return gmtDate;
	}

	public static Timestamp getSlidedDueDate(Date date, int slideOffset,
			String calendarName) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Date slidedDate = CalenderUtils.slideDate(cal.getTime(), slideOffset,
				new GenericHolidayCalendar(calendarName));
		Timestamp gmtDate = Timestamp.getTimestamp(slidedDate);
		return gmtDate;
	}

	/**
	 * 
	 * @param aSystemId
	 * @param aParentId
	 *            of a particular request.
	 * @return sub-request count
	 * @throws SQLException
	 */
	public static int getSubRequestCountBySysIdReqId(int aSystemId,
			int aParentId) throws SQLException {
		int count = 0;
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			CallableStatement cs = connection
					.prepareCall("stp_request_lookupBySystemIdAndParentId ?, ?");

			cs.setInt(1, aSystemId);
			cs.setInt(2, aParentId);

			ResultSet rs = cs.executeQuery();

			if (rs != null) {
				if (rs.next() != false) {
					count = rs.getInt(1);
				}

				rs.close();
			}

			cs.close();
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}

		return count;
	}

	/*
	 * Takes comma separated string and a compare string. Checks if the compare
	 * string exists in the comma separated string.
	 */
	public static boolean isExistsInString(String parentString,
			String childString) {
		String[] strArray = parentString.split(",");
		for (String str : strArray) {
			if (str.trim().equals(childString.trim()))
				return true;
			else
				continue;
		}
		return false;
	}

	public static final String STATUS_PENDING_SUBMISSION = "PendingSubmission";

	// Miscellaneous
	public static final String FALSE = "false";

	public static final String TRUE = "true";

	public static final String FIELD_INCOMING_TRANSMITTAL_NO = "IncomingTransmittalNo";

	public static final String TRANSMITTAL_ID_PREFIX = "transmittal_id_prefix";

	public static final String STRING_NONE = "None";

	static final String BR_B_OR_B_BR = "<br><b>OR</b><br>";

	static final String ALL_DRAWINGS_SAME_CATEGORY_MSG = "All the drawings selected must belong to one of the following categories:<br>";

	/**
	 * @param jsonArrayTable
	 * @return
	 * @throws JsonParseException
	 */
	public static ArrayList<String[]> fetchArrayListFromJsonArray(
			String jsonArrayTable) throws JsonParseException {

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
					} else {
						jArray = jsonArray.get(i).getAsJsonArray();
					}
					//

					String[] drwInfo = new String[jArray.size()];
					StringBuffer sb = new StringBuffer();
					for (int j = 0; j < jArray.size(); j++) {
						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull())) {
							JsonArray jarray = null;
							if (jArray.get(j).isJsonArray()) {
								JsonParser jParser = new JsonParser();
								jarray = jArray.get(j).getAsJsonArray();

								for (int l = 0; l < jarray.size(); l++) {
									if ((jarray.get(l) != null)
											&& (!jarray.get(l).isJsonNull())) {
										JsonArray jarray1 = null;
										if (jarray.get(l).isJsonArray()) {

											jarray1 = jarray.get(l)
													.getAsJsonArray();
											String doc = "";
											for (int m = 0; m < jarray1.size(); m++) {
												if ((jarray1.get(m) != null)
														&& (!jarray1.get(m)
																.isJsonNull())) {

													JsonArray jArray2 = null;
													if (jarray1.get(m)
															.isJsonArray()) {

														jArray2 = jarray1
																.get(m)
																.getAsJsonArray();

														doc = doc
																+ jArray2
																		.get(0)
																		.getAsString()
																+ "\n";

													}

												}
												drwInfo[j] = doc;
											}
										}

									}
								}

							} else {
								String a = jArray.get(j).getAsString();

								if (!a.equals("")) {

									drwInfo[j] = a;
								} else {
									drwInfo[j] = "-";
								}

							}

						}
					}

					drawingsList.add(drwInfo);
				}
			}
		}

		return drawingsList;
	}

	/**
	 * @param args
	 * @throws DatabaseException
	 * @throws TbitsExceptionClient
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws TbitsExceptionClient,
			DatabaseException {

		// JsonObject jobj = new JsonObject();
		// JsonObject jObj1 = new JsonObject();
		// jObj1.add("A,B,C", new JsonPrimitive(10));
		// jObj1.add("R1,R0", new JsonPrimitive("12"));
		// jObj1.add("default", new JsonPrimitive("8"));
		// jobj.add("revFieldName", jObj1);
		// System.out.println("String: " + jobj.toString());
		// System.out.println("****************************************");
		// String testString = jobj.toString();
		// System.out.println("String: " + testString);
		// //{"revFieldName":{"A,B,C":10,"R1,R0":"10"}}
		// String fieldValue = "R2";
		// ArrayList<String> roles =
		// TransmittalServiceImpl.getUserRolesNamesBySysIdAndUserId(31, 82);
		// System.out.println("Roles: " + roles.toString());
		// System.out.println("SIGNATORY.... : " + getDtnSignatory(46, 31));
		System.out.println("****************************************");
		System.out.println(Role.getUserRolesBySysIdAndUserId(84, 546));

	}

	public static HashMap<String, String> getUserInfoMap(User user) {
		HashMap<String, String> userInfoTable = null;
		UserColumn[] userColumn = User.UserColumn.values();
		if (user != null) {
			userInfoTable = new HashMap<String, String>();
			for (int i = 0; i < userTableColumnNames.length; i++) {
				String value = user.get(userColumn[i]);
				if (value == null)
					value = "";
				userInfoTable.put(userTableColumnNames[i].trim(), value);
			}
		}
		return userInfoTable;
	}

	public static ArrayList<TbitsModelData> getDistributionListData(
			Connection connection, int trnProcessId, String toList,
			String ccList,
			ArrayList<TbitsModelData> distributionTableColumnsList)
			throws DatabaseException {
		ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();
		try {
			// toList = (toList == null) ? "" : toList.trim();
			ccList = (ccList == null) ? "" : ccList.trim();

			String distributionList = ccList;
			// Create unique list
			HashSet<String> userSet = new HashSet<String>();
			for (String userLogin : distributionList.trim().split(",")) {
				if (!userLogin.trim().equals("")) {
					userSet.add(userLogin);
				}
			}
			HashMap<String, Integer> userTableColumns = getUserTableColumnNames(connection);

			for (String userLogin : userSet) {
				if (!userLogin.trim().equals("")) {
					User uc = User.lookupByUserLogin(userLogin);
					if (uc != null) {
						TbitsModelData tempData = new TbitsModelData();
						if (distributionTableColumnsList == null)
							return distributionListModelData;

						UserColumn[] userColumns = User.UserColumn.values();
						for (TbitsModelData distColumnTmd : distributionTableColumnsList) {
							String property = (String) distColumnTmd
									.get("name");
							if (property != null) {
								Integer userColumnId = userTableColumns
										.get(property);
								UserColumn userColumn = null;
								if (userColumnId != null)
									userColumn = userColumns[userColumnId];
								// Check if its a user property if not, fetch
								// default values from db.

								String value = "";
								if (userColumn != null) {
									value = (String) uc.get(userColumn);
								} else {
									value = distColumnTmd.get(FIELD_CONFIG);
									if ((Integer) distColumnTmd
											.get(DATA_TYPE_ID) == DataType.TYPE) {
										HashMap<String, String> typesMap = fetchKeyValuePairsfromJsonString(value);
										value = typesMap.keySet().iterator()
												.next();
									}
								}
								if ((value == null)
										|| (value.trim().equals("")))
									value = "-";
								tempData.set(property, value);
							}
						}
						distributionListModelData.add(tempData);
					}
				}
			}
		} catch (SQLException sqle) {
			throw new DatabaseException(
					"Error occurred which fetching distribution table.", sqle);
		}
		return distributionListModelData;
	}

	/**
	 * @param jsonMap
	 * @param jsonString
	 */
	protected static HashMap<String, String> fetchKeyValuePairsfromJsonString(
			String jsonString) throws JsonParseException {
		HashMap<String, String> jsonMap = new HashMap<String, String>();
		if (jsonString != null) {
			// jsonMap.put("-", "-");
			JsonParser jsonParser = new JsonParser();
			JsonElement parsedJson = jsonParser.parse(jsonString);
			if (parsedJson != null) {
				JsonArray jsonArray = parsedJson.getAsJsonArray();
				if (jsonArray != null) {
					for (int i = 0; i < jsonArray.size(); i++) {
						JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
						if (jsonObj != null) {
							String name = jsonObj.get("name").getAsString();
							String value = jsonObj.get("value").getAsString();
							if ((name != null) && (value != null))
								jsonMap.put(value, name);
						}
					}
				}
			}
		}
		return jsonMap;
	}

	/**
	 * @param model
	 * @param fieldNames
	 * @return
	 */
	public static JsonArray getDistributionListJsonString(
			List<TbitsModelData> models, ArrayList<String> fieldNames) {
		JsonArray tableJson = new JsonArray();
		int count = 1;
		for (TbitsModelData model : models) {
			JsonArray drawingListValues = new JsonArray();
			JsonElement serialJsonElem = new JsonPrimitive(count);
			drawingListValues.add(serialJsonElem);
			for (int i = 0; i < fieldNames.size(); i++) {
				String fValue = String.valueOf(model.get(fieldNames.get(i)));
				if (fValue != null) {
					JsonElement jsonElem = new JsonPrimitive(fValue);
					drawingListValues.add(jsonElem);
				} else {
					JsonElement jsonElem = new JsonPrimitive("-");
					drawingListValues.add(jsonElem);
				}
			}
			tableJson.add(drawingListValues);
			count++;
		}
		return tableJson;
	}

	public static HashMap<String, Integer> getUserTableColumnNames(
			Connection connection) throws SQLException {
		HashMap<String, Integer> columnNameList = new HashMap<String, Integer>();
		String userTableColumnNamesQuery = "Select column_name 'Columns' from information_schema.columns"
				+ " where table_name='users'";
		PreparedStatement ps = connection
				.prepareStatement(userTableColumnNamesQuery);
		ResultSet rs = ps.executeQuery();
		if (rs != null) {
			int count = 0;
			while (rs.next()) {
				String columnName = rs.getString(1);
				if (columnName != null) {
					columnNameList.put(columnName, count++);
				}
			}
		}
		rs.close();
		ps.close();
		return columnNameList;
	}

	protected static final String DTN_SIGNATORY = "dtnSignatory";

	/**
	 * @param connection
	 * @param dcrSystemId
	 * @param trnProcessId
	 * @param attachmentSelectionTableColumns
	 * @throws TbitsExceptionClient
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	protected static ArrayList<Field> getTrnInvolvedFields(
			Connection connection, int dcrSystemId, int trnProcessId,
			ArrayList<TbitsModelData> attachmentSelectionTableColumns)
			throws SQLException, DatabaseException {
		ArrayList<Field> baFields = new ArrayList<Field>();

		if (attachmentSelectionTableColumns != null) {
			Collections.sort(attachmentSelectionTableColumns, c);
			for (TbitsModelData md : attachmentSelectionTableColumns) {
				int dataTypeId = (Integer) md.get(DATA_TYPE_ID);
				if (dataTypeId != DataType.ATTACHMENTS) {
					Integer fieldId = (Integer) md.get(FIELD_ID);
					if (fieldId != null) {
						Field baField = Field.lookupBySystemIdAndFieldId(
								dcrSystemId, fieldId);
						if (baField != null) {
							baFields.add(baField);
						}
					}
				}
			}
		}
		return baFields;
	}

	static Comparator<TbitsModelData> c = new Comparator<TbitsModelData>() {
		public int compare(TbitsModelData arg0, TbitsModelData arg1) {
			int diff = (Integer) arg0.get("column_order")
					- (Integer) arg1.get("column_order");
			if (diff > 0)
				return 1;
			else if (diff == 0)
				return 0;
			else
				return -1;
		}
	};

	protected static final String TYPE_VALUE_SOURCE = "type_value_source";

	protected static String getValueFromAppropriateSourceForTypeField(
			Field field,
			ArrayList<TbitsModelData> attachmentSelectionTableColumns,
			Request request) throws DatabaseException, TBitsException {

		if ((field != null) && (field.getDataTypeId() == DataType.TYPE)
				&& (attachmentSelectionTableColumns != null)
				&& (request != null)) {
			for (TbitsModelData attColumn : attachmentSelectionTableColumns) {
				if (attColumn != null) {
					Integer tFieldId = (Integer) attColumn.get(FIELD_ID);
					if ((tFieldId != null) && (tFieldId == field.getFieldId())) {
						transbit.tbits.domain.Type typeVal = (transbit.tbits.domain.Type) request
								.getObject(field.getName());
						if (typeVal == null) {
							ArrayList<transbit.tbits.domain.Type> typesList = transbit.tbits.domain.Type
									.lookupBySystemIdAndFieldName(field
											.getSystemId(), field.getName());
							if (typesList != null) {
								for (transbit.tbits.domain.Type fType : typesList) {
									if ((fType != null)
											&& (fType.getIsDefault())) {
										typeVal = fType;
										break;
									}
								}
								if (typeVal == null)
									throw new TBitsException(
											"No default value found for Type field: "
													+ field.getName()
													+ ", in BA: "
													+ field.getSystemId());
							}
						}
						switch ((Integer) attColumn.get(TYPE_VALUE_SOURCE)) {
						case 0:
						case 1:
							return typeVal.getName();
						case 2:
							return typeVal.getDisplayName();
						case 3:
							return typeVal.getDescription();
						}
					}
				}
			}
		}
		return "";
	}

	static String getDrawingTableJSON(Connection connection, int dcrSystemId,
			int trnProcessId, String[] dcrRequestList,
			ArrayList<TbitsModelData> attachmentSelectionTableColumns)
			throws DatabaseException, SQLException, TBitsException {
		ArrayList<String> drwTable = new ArrayList<String>();
		java.lang.reflect.Type arrayListType = new TypeToken<ArrayList<String>>() {
		}.getType();
		Gson gson = new Gson();
		if (attachmentSelectionTableColumns != null) {
			ArrayList<Field> fields = getTrnInvolvedFields(connection,
					dcrSystemId, trnProcessId, attachmentSelectionTableColumns);
			for (String reqIdStr : dcrRequestList) {
				int requestId = Integer.parseInt(reqIdStr);
				ArrayList<String> fieldValues = new ArrayList<String>();
				Request request = Request.lookupBySystemIdAndRequestId(
						connection, dcrSystemId, requestId);
				if (request != null) {
					fieldValues.add(request.get(Field.REQUEST));
					for (int i = 0; i < fields.size(); i++) {
						Field field = fields.get(i);
						String value = request.get(field.getName());
						if (field.getDataTypeId() == DataType.TYPE) {
							value = getValueFromAppropriateSourceForTypeField(
									field, attachmentSelectionTableColumns,
									request);
						}
						if ((value == null) || (value.trim().equals("")))
							value = "-";
						fieldValues.add(value);
					}
					String fieldValuesJson = gson.toJson(fieldValues,
							arrayListType);
					drwTable.add(fieldValuesJson);
				}
			}
		}
		return gson.toJson(drwTable, arrayListType);
	}

	static String getDrawingTableJSON(Connection connection, int dcrSystemId,
			int trnProcessId, ArrayList<Request> requestsList,
			ArrayList<TbitsModelData> attachmentSelectionTableColumns)
			throws DatabaseException, SQLException, TBitsException {
		ArrayList<String> drwTable = new ArrayList<String>();
		java.lang.reflect.Type arrayListType = new TypeToken<ArrayList<String>>() {
		}.getType();
		Gson gson = new Gson();
		if (attachmentSelectionTableColumns != null) {
			ArrayList<Field> fields = getTrnInvolvedFields(connection,
					dcrSystemId, trnProcessId, attachmentSelectionTableColumns);
			for (Request request : requestsList) {
				int requestId = request.getRequestId();
				ArrayList<String> fieldValues = new ArrayList<String>();
				// Request request =
				// Request.lookupBySystemIdAndRequestId(connection, dcrSystemId,
				// requestId);
				if (request != null) {
					fieldValues.add(requestId + "");
					for (int i = 0; i < fields.size(); i++) {
						Field field = fields.get(i);
						String value = request.get(field.getName());
						if (field.getDataTypeId() == DataType.TYPE) {
							value = getValueFromAppropriateSourceForTypeField(
									field, attachmentSelectionTableColumns,
									request);
						} else {
							for (TbitsModelData tmd : attachmentSelectionTableColumns) {
								if (tmd.get("field_id").equals(
										field.getFieldId())) {
									if (field.getName().equals("DTNConfigQty")) {
										value = "1";
										break;
									}
									if (value.equals("")) {
										value = tmd.get("default_value");
										break;
									}
								}
							}
						}
						if ((value == null) || (value.trim().equals("")))
							value = "-";
						fieldValues.add(value);
					}
					String fieldValuesJson = gson.toJson(fieldValues,
							arrayListType);
					drwTable.add(fieldValuesJson);
				}
			}
		}
		return gson.toJson(drwTable, arrayListType);
	}

	public static final String KIND_ATTENTION = "kindAttention";

	/**
	 * @param paramTable
	 * @param kAttnInfoMap
	 * @param toList
	 * @param kAttnInfoMap
	 * @return
	 * @throws DatabaseException
	 */
	protected static void getKindAttentionUserInfo(
			HashMap<String, String> paramTable,
			HashMap<String, String> kAttnInfoMap, String toList)
			throws DatabaseException {
		String kindAttention = paramTable.get(KIND_ATTENTION);
		if ((kindAttention == null) || kindAttention.trim().equals("")) {
			String[] toUserNames = toList.split(",");
			if ((toUserNames != null) && (!toUserNames[0].trim().equals(""))) {
				User kindAttentionUser = User.lookupByUserLogin(toUserNames[0]);

				if (kindAttentionUser != null) {
					kAttnInfoMap.putAll(getUserInfoMap(kindAttentionUser));
					paramTable.put(KIND_ATTENTION, kindAttentionUser
							.getDisplayName());
				} else {
					paramTable.put(KIND_ATTENTION, "");
				}
			}
		}
	}

	static void prefillWithUserColumns(HashMap<String, String> kAttnInfoMap) {
		for (String colName : userTableColumnNames) {
			kAttnInfoMap.put(colName, "");
		}
	}

	/**
	 * @param connection
	 * @param jsonString
	 *            - of the form {"revFieldName":{"A,B,C":10,"R1,R0":"12"}}
	 * @param curSysPrefix
	 *            - current business area system prefix.
	 * @param srcRequest
	 *            - date offset based on revision number(of the revision field
	 *            in DCR)
	 * @return
	 */
	static int getSlideOffsetBasedOnRevisionFieldAndType(Connection connection,
			Request srcRequest, int targetSysId, String jsonString)
			throws JsonParseException {
		// int slideOffset = 0;
		int defaultOffset = 0;
		JsonParser jsonParser = new JsonParser();
		try {
			JsonElement jsonElement = jsonParser.parse(jsonString);
			if (jsonElement != null) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				if (jsonObject.isJsonObject()) {
					Set<Entry<String, JsonElement>> entrySet = jsonObject
							.entrySet();
					Iterator<Entry<String, JsonElement>> iterator = entrySet
							.iterator();
					if (iterator.hasNext()) {
						Entry<String, JsonElement> next = iterator.next();
						String fieldName = next.getKey();
						if ((fieldName != null)
								&& (!fieldName.trim().equals(""))) {
							String fieldValue = srcRequest.get(fieldName);
							JsonElement jsonValue = next.getValue();
							if (jsonValue != null) {
								JsonObject typesOffsetJsonObj = jsonValue
										.getAsJsonObject();
								if (typesOffsetJsonObj.isJsonObject()) {
									Set<Entry<String, JsonElement>> typesOffsetEntry = typesOffsetJsonObj
											.entrySet();
									Iterator<Entry<String, JsonElement>> offsetIterator = typesOffsetEntry
											.iterator();
									while (offsetIterator.hasNext()) {
										Entry<String, JsonElement> typesOffset = offsetIterator
												.next();
										String typesString = typesOffset
												.getKey();// typeString
										String val = typesOffset.getValue()
												.getAsString();// value of the
										// offset

										int offset = 0;
										// if value of the offset is not given,
										// we will make that particular field
										// empty
										if (!val.trim().equals("")) {
											offset = Integer
													.valueOf(typesOffset
															.getValue()
															.getAsString());
										} else {
											offset = -1;
										}

										if ((typesString != null)
												&& (!typesString.trim().equals(
														""))) {
											String[] revisionType = typesString
													.split(",");
											if (revisionType.length > 0) {

												for (String typeName : revisionType) {
													if (typeName
															.contentEquals(REVISION_DEFAULT)) {
														defaultOffset = offset;
														break;
													}
													if (fieldValue
															.equals(typeName
																	.trim())) {
														LOG
																.info("Offset selected for field: "
																		+ fieldName
																		+ ", in business area "
																		+ "with system id: "
																		+ targetSysId
																		+ " is : "
																		+ offset);
														return offset;
													}
												}
											}
										}

									}
								} else {
									LOG
											.info("Invalid value provided in trn_field_mapping for field: "
													+ fieldName
													+ "in business area system id: "
													+ targetSysId);
								}
							}
						}

					}
				}
			}
		} catch (JsonParseException jpe) {
			throw jpe;
		} catch (IllegalStateException ise) {
			throw ise;
		}

		return defaultOffset;
	}

	public static final String CALENDAR = "calendar";

	/**
	 * @param transmittalType
	 * @return
	 * @throws DatabaseException
	 * @throws TBitsException
	 */
	protected static int getTransmittalMaxId(String dtnSerialKey)
			throws DatabaseException, TBitsException {
		Connection connection = null;
		int transmittalMaxId = 0;
		try {
			connection = DataSourcePool.getConnection();
			String queryString = "SELECT id FROM max_ids WHERE name=?";
			PreparedStatement ps = connection.prepareStatement(queryString);
			ps.setString(1, dtnSerialKey);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				transmittalMaxId = rs.getInt("id");
			}
			rs.close();
			ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseException(
					"Database error occurred while retrieving the max_id for preview.",
					e1);
		} catch (NumberFormatException nfe) {
			throw new TBitsException("Illegal transmittal number.", nfe);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException(
							"Error occurred while closing database connection.",
							e);
				}
		}
		return transmittalMaxId;
	}

	protected static final String IS_APPROVAL_CYCLE = "isApprovalCycle";

	protected static final String IS_POST_APPROVAL_CYCLE = "isPostApprovalCycle";

	protected static final String APPROVAL_CYCLE_REQUEST_ID = "approvalCycleRequestId";

	protected static final String ACTUAL_DATE = "actualDate";

	protected static final String ACTUAL_NUMBER = "actualNumber";

	static final String DTN_TEMPLATE_DATE = "dtnDate";

	protected static String getDTNTemplateDate(String actualDate)
			throws TBitsException {
		Date aDate;
		String dtnTemplateDate = "";
		SimpleDateFormat sdfForDTN = new SimpleDateFormat("dd MMM yyyy");
		if ((actualDate != null) && (!actualDate.trim().equals(""))) {
			SimpleDateFormat sdf = new SimpleDateFormat(
					TBitsConstants.API_DATE_FORMAT);
			try {
				aDate = sdf.parse(actualDate);
				if (aDate != null) {
					dtnTemplateDate = sdfForDTN.format(aDate);
				}
			} catch (ParseException e) {
				SimpleDateFormat onlyDateSDF = new SimpleDateFormat(
						TBitsConstants.API_DATE_ONLY_FORMAT);
				try {
					aDate = onlyDateSDF.parse(actualDate);
					if (aDate != null) {
						dtnTemplateDate = sdfForDTN.format(aDate);
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
					throw new TBitsException(
							"Actual date(for transmittal) provided by the user is invalid: "
									+ actualDate
									+ ", should be of the format, "
									+ TBitsConstants.API_DATE_ONLY_FORMAT
									+ " or " + TBitsConstants.API_DATE_FORMAT);
				}
			}
		} else {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));
			aDate = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
			dtnTemplateDate = sdf.format(aDate);
		}
		return dtnTemplateDate;
	}

	protected static final String ACTUAL_TRANSMITTAL_DATE = "ActualTransmittalDate";

	/**
	 * Get the businessareaclient object for a given sysId
	 * 
	 * @param sysId
	 * @return
	 */
	public static BusinessAreaClient getBAforSysId(int sysId) {
		BusinessArea ba;

		try {
			ba = BusinessArea.lookupBySystemId(sysId);
			if (null == ba)
				return null;

			BusinessAreaClient baClient = new BusinessAreaClient();
			GWTServiceHelper.setValuesInDomainObject(ba, baClient);
			return baClient;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static ArrayList<String[]> fetchDistributionListFromJsonArray(
			String jsonArrayTable) throws JsonParseException {
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
					} else {
						jArray = jsonArray.get(i).getAsJsonArray();
					}
					String[] drwInfo = new String[jArray.size()];
					for (int j = 1; j < jArray.size(); j++) {
						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull())) {
							String eachElement = jArray.get(j).getAsString();
							int positionOfSeprator = eachElement.indexOf(',');
							if (positionOfSeprator != -1) {
								drwInfo[j] = eachElement
										.substring(positionOfSeprator + 1);
							} else {
								drwInfo[j] = eachElement;
							}
						} else
							drwInfo[j] = "-";
					}

					drawingsList.add(drwInfo);
				}
			}
		}
		return drawingsList;

	}

	public static ArrayList<String[]> fetchDrawingListFromJsonArray(
			String jsonArrayTable) throws JsonParseException {

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
					} else {
						jArray = jsonArray.get(i).getAsJsonArray();
					}
					String[] drwInfo = new String[jArray.size()];
					// jarray is the original 1
					for (int j = 0; j < jArray.size(); j++)

					{

						JsonArray jArray1 = new JsonArray();

						if ((jArray.get(j) != null)
								&& (!jArray.get(j).isJsonNull())) {

							if (!jArray.get(j).isJsonArray()) {
								JsonParser jParser = new JsonParser();

								drwInfo[j] = jArray.get(j).getAsString();
							} // bwosing around jarray
							else {
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
												}
											}

										}

										else {

											if (l == 1)

											{
												String eachElement = jArray1
														.get(l).getAsString();
												drwInfo[j] = eachElement;
											}
										}
									} else
										drwInfo[j] = "-";
								}

							}

						}
					}
					drawingsList.add(drwInfo);
				}
			}
		}

		return drawingsList;
	}

}
