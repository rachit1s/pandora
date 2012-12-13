package zipuploader.com.tbitsGlobal.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.server.GenericTransmittalCreator;
import zipuploader.com.tbitsGlobal.client.DocNumberFileTuple;
import zipuploader.com.tbitsGlobal.client.HolcimAttachmentInfo;
import zipuploader.com.tbitsGlobal.client.HolcimJagService;
import zipuploader.com.tbitsGlobal.shared.HolcimPluginConstants;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class HolcimJagServiceImpl extends TbitsRemoteServiceServlet implements
		HolcimJagService {

	public static String FILE_PATH = "filepath";

	public static String REQUEST_ID = "requestId";

	public static String SUBJECT = "subject";

	public static String FILE_NAME_TO_BE_USED = "filenameToBeUsed";

	public static String IS_REGEX_CORRECT = "isRegexCorrect";

	public static final TBitsLogger LOG = TBitsLogger.getLogger("Holcim");
	static String regex;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * regex deliverableFieldID TRN_PROCESS_ID docNoField
	 * 
	 * @param aSystemId
	 * @return
	 * @throws SQLException
	 * 
	 */
	HashMap<String, String> fetchConfigData(int aSystemId) throws SQLException {
		Connection connection = null;
		HashMap<String, String> holcimConfig = new HashMap<String, String>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select * from holcim_config where sys_id=?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					holcimConfig.put(rs.getString("field_name"), rs
							.getString("field_value"));

					if (rs.getString("field_name").equalsIgnoreCase("regex")) {
						regex = rs.getString("field_value");

					} else if (rs.getString("field_name").equalsIgnoreCase(
							"trn_process_id")) {
						HolcimPluginConstants.setTRN_PROCESS_ID(Integer
								.parseInt(rs.getString("field_value")));

					} else if (rs.getString("field_name").equalsIgnoreCase(
							"docNoField")) {
						HolcimPluginConstants.setDocField(rs
								.getString("field_value"));

					} else if (rs.getString("field_name").equalsIgnoreCase(
							"deliverableFieldID")) {
						HolcimPluginConstants.setDeliverableFieldID(Integer
								.parseInt(rs.getString("field_value")));

					}
				}
			}
			rs.close();
			ps.close();

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
		return holcimConfig;
	}

	/**
	 * regex deliverableFieldID TRN_PROCESS_ID docNoField
	 * 
	 * sys_id regex_order field_name is_editable 51 1 DrawingNo 0 51 2 Revision
	 * 1
	 * 
	 * 
	 * @param aSystemId
	 * @return
	 * @throws TbitsExceptionClient
	 * @throws SQLException
	 * @throws SQLException
	 */
	public ArrayList<TbitsModelData> fetchFieldsdata(int aSystemId)
			throws SQLException {
		Connection connection = null;
		ArrayList<TbitsModelData> tmd = new ArrayList<TbitsModelData>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select * from holcim_fields where sys_id=?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					TbitsModelData model = new TbitsModelData();
					Boolean is_editable = rs.getBoolean("is_editable");
					int order = rs.getInt("regex_order");
					String field_name = rs.getString("field_name");
					model.set("is_editable", is_editable);
					model.set("name", field_name);
					model.set("regex_order", order);
					tmd.add(model);
				}
			}
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			throw new SQLException();
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
		return tmd;
	}

	@Override
	public HashMap<String, Object> processRepoFileId(int repoFileId, int sysId,
			UserClient currentUser) throws TbitsExceptionClient {
		// TODO Auto-generated method stub

		HashMap<String, Object> foundData = new HashMap<String, Object>();
		ArrayList<DocNumberFileTuple> missingDocs = new ArrayList<DocNumberFileTuple>();
		ArrayList<DocNumberFileTuple> presentDocs = new ArrayList<DocNumberFileTuple>();
		String path = null;
		ArrayList<Integer> listOfPresentRequests = new ArrayList<Integer>();

		try {
			/*
			 * HolcimPluginConstants.fields = fetchFieldsdata(sysId);
			 * 
			 * HashMap<String, String > hm1 = fetchConfigData(sysId);
			 * Comparator<TbitsModelData> cmp = new Comparator<TbitsModelData>()
			 * {
			 * 
			 * @Override public int compare(TbitsModelData o1, TbitsModelData
			 * o2) {
			 * 
			 * int s1 = (Integer) o1.get("regex_order"); int s2 = (Integer)
			 * o2.get("regex_order"); if (s1 > s2) return 1; else if (s1 == s2)
			 * return 0; else if (s1 < s2) return -1; return 0;
			 * 
			 * } };
			 * 
			 * Collections.sort(HolcimPluginConstants.fields, cmp);
			 */
			path = Uploader.getFileLocation(repoFileId);

			ArrayList<String> listOfDocuments = new ArrayList<String>();

			File attachmentBase;
			attachmentBase = new File(APIUtil.getAttachmentLocation());

			String exactpath = attachmentBase.getPath();
			exactpath = exactpath + "/" + path;

			HashMap<String, String> hm = unzipMyZip(exactpath, listOfDocuments);

			ArrayList<DocNumberFileTuple> FileMetaData = new ArrayList<DocNumberFileTuple>();

			if (hm != null) {

				for (String fileName : hm.keySet()) {

					String FilePath = hm.get(fileName);
					DocNumberFileTuple processedData = getFiles(fileName,
							FilePath, HolcimPluginConstants.fields);
					if (processedData != null)
						FileMetaData.add(processedData);
				}

				if (FileMetaData != null && FileMetaData.size() > 0) {

					this.search(FileMetaData, sysId, HolcimPluginConstants
							.getDocField());
					for (DocNumberFileTuple metadata : FileMetaData) {
						if (metadata.getRequestID() != -1) {
							listOfPresentRequests.add(metadata.getRequestID());
							presentDocs.add(metadata);
						} else {
							missingDocs.add(metadata);
						}
					}

				}
			}

			String ErrorMsg = "These are the documents for which no request is present"
					+ "\n";
			boolean flag = false;
			for (DocNumberFileTuple data : missingDocs) {
				flag = true;
				// ErrorMsg = ErrorMsg + "Document  No =   "+
				// data.getDOC_NUMBER();
				ErrorMsg = ErrorMsg + "/n";
			}
			if (flag) {

				LOG.error(ErrorMsg);
				System.out.println(ErrorMsg);
			}

			foundData.put("NMap", missingDocs);
			foundData.put("PathMap", presentDocs);
			// foundData.put("fields", HolcimPluginConstants.fields);
			// foundData.put("config", hm1);

		} catch (APIException e) {

			e.printStackTrace();
			throw new TbitsExceptionClient("Some Error occurred ");
		} catch (DatabaseException e) {

			e.printStackTrace();
			throw new TbitsExceptionClient(
					"Error occurred while DataBase Connectivity");

		}

		return foundData;
	}

	public static HashMap<String, String> unzipMyZip(String zipFileName,
			ArrayList<String> listOfDocuments) {

		Enumeration entriesEnum;
		HashMap<String, String> hm = new HashMap<String, String>();
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(zipFileName);
			entriesEnum = zipFile.entries();

			/**
			 * Check if the directory to extract to exists
			 */

			while (entriesEnum.hasMoreElements()) {
				try {
					ZipEntry entry = (ZipEntry) entriesEnum.nextElement();

					if (entry.isDirectory()) {
						/**
						 * Currently not unzipping the directory structure. All
						 * the files will be unzipped in a Directory
						 * 
						 **/
					} else {

						System.err.println("Extracting file: "
								+ entry.getName());
						/**
						 * The following logic will just extract the file name
						 * and discard the directory
						 */
						int index = 0;
						String name = entry.getName();
						index = entry.getName().lastIndexOf("/");
						if (index > 0 && index != name.length())
							name = entry.getName().substring(index + 1);
						listOfDocuments.add(name);
						System.out.println(name);

						String suffix = "";
						int index1 = name.lastIndexOf('.');
						File tempFile;

						if (index1 > 0) {
							suffix = name.substring(index1 + 1);
							tempFile = File.createTempFile(name.substring(0,
									index1), suffix);
							hm.put(name, tempFile.getPath());
						} else {
							tempFile = File.createTempFile("hello", null);
							hm.put(name, tempFile.getPath());
						}
						writeFile(zipFile.getInputStream(entry),
								new BufferedOutputStream(new FileOutputStream(
										tempFile)));

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Some Exception Occurred:");
			ioe.printStackTrace();
			return null;
		}
		return hm;
	}

	/**
	 * This method --Reads an input stream --Writes the value to the output
	 * stream --Uses 1KB buffer.
	 */
	public static final void writeFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	/*
	 * parses the file names and generates model data
	 */
	public DocNumberFileTuple getFiles(String fileName, String filePath,
			ArrayList<TbitsModelData> fields) throws TbitsExceptionClient {

		DocNumberFileTuple fileData = new DocNumberFileTuple();

		String name1 = fileName;
		String path = filePath;
		// PC01 xxx yyy 001_PC02-GA02-2413P1-001_B.xls
		String regex = HolcimJagServiceImpl.regex;
		System.out
				.println(regex
						.equals("[^_]+_([A-Za-z0-9-]+)_([A-Za-z0-9][A-Za-z0-9]?)\\.[a-zA-Z]{3}"));
		// "[^_]+_([A-Za-z0-9-]+)_([A-Za-z0-9][A-Za-z0-9]?)\\.[a-zA-Z]{3}";
		// HolcimJagServiceImpl.regex;
		Pattern pattern = Pattern.compile(regex);

		int size = fields.size();
		Matcher m1 = pattern.matcher(name1);
		if (m1.matches()) {

			DocNumberFileTuple temp = new DocNumberFileTuple();

			for (int i = 1; i <= size; i++) {
				try {
					String str = m1.group(i);
					String name = fields.get(i - 1).get("name");
					temp.set(name, str);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(
							"please provide data compatible with the regex");
				}

			}

			temp.setFILE_PATH(path);
			temp.setfilenameToBeUsed(fileName);
			temp.setIs_Regex_Correct(true);

			fileData = temp;
			return fileData;
		} else {

			DocNumberFileTuple temp = new DocNumberFileTuple();
			temp.setIs_Regex_Correct(false);
			temp.setfilenameToBeUsed(fileName);
			temp.setFILE_PATH(path);
			return temp;

		}

	}

	/*
	 * parses the file names and generates model data
	 */
	public DocNumberFileTuple getFilesAfterKnowingSubject(int sysid,
			String fileName, String filePath, String subject)
			throws TbitsExceptionClient {

		// /---------------------//
		DocNumberFileTuple fileData = new DocNumberFileTuple();
		String name1 = fileName;
		String path = filePath;

		String regex = HolcimJagServiceImpl.regex;
		Pattern pattern = Pattern.compile(regex);

		int size = HolcimPluginConstants.fields.size();
		Matcher m1 = pattern.matcher(name1);
		if (m1.matches()) {

			DocNumberFileTuple temp = new DocNumberFileTuple();

			for (int i = 1; i <= size; i++) {
				try {
					String str = m1.group(i);
					String name = HolcimPluginConstants.fields.get(i - 1).get(
							"name");
					temp.set(name, str);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(
							"please provide data compatible with the regex");
				}

			}
			temp.setFILE_PATH(path);
			temp.setfilenameToBeUsed(fileName);
			temp.setIs_Regex_Correct(true);
			if (subject != null)
				temp.setSubject(subject);
			fileData = temp;
			return fileData;

		} else {

			DocNumberFileTuple temp = new DocNumberFileTuple();
			temp.setIs_Regex_Correct(false);
			temp.setfilenameToBeUsed(fileName);
			temp.setFILE_PATH(path);
			if (subject != null)
				temp.setSubject(subject);
			return temp;

		}

	}

	/**
	 *@param model
	 */
	@SuppressWarnings("static-access")
	void search(ArrayList<DocNumberFileTuple> docNumberFileTuples, int sysid,
			String docNumberFieldName) throws DatabaseException, APIException {

		// BusinessArea.lookupBySystemId(sysid);

		int baId = sysid;
		Connection conn = null;
		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
		AddRequest addRequest = new AddRequest();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			for (DocNumberFileTuple data : docNumberFileTuples) {
				/*
				 * if (data.getREVISION() != null) { if
				 * (data.getREVISION().equals("00")) { data.setREVISION("R0"); }
				 * else if (data.getREVISION().equals("02")) {
				 * data.setREVISION("R2"); } else if
				 * (data.getREVISION().equals("03")) { data.setREVISION("R1"); }
				 * else { data.setREVISION("R0"); } }
				 */
				String docno = data
						.get(HolcimPluginConstants.DocumentFieldNameToBeUsed);
				if (docno != null) {
					int requestId = this.searchRequestEx(conn, baId,
							docNumberFieldName, docno);

					TbitsTreeRequestData trd = getDataByRequestId(BusinessArea
							.lookupBySystemId(sysid).getSystemPrefix(),
							requestId);
					if (requestId == 0) {

						LOG.info("Request Doesnot Exist in the BA for  "
								+ docno + " Please create the request First");
						data.setRequestID(-1);
						data.setSubject(null);

					} else {

						LOG.info("Request Present in the BA " + docno);

						data.setRequestID(requestId);
						data.setSubject((String) trd
								.get(HolcimPluginConstants.TITLE));

					}
				} else {
					LOG.info("Request Doesnot Exist in the BA for  " + docno
							+ " Please create the request First");
					data.setRequestID(-1);
					data.setSubject(null);

				}

			}

			conn.commit();
			tbitsResMgr.commit();
		} catch (SQLException e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				e.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database", e));
			throw apie;
		} catch (Exception ex) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				ex.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
				LOG.error(e);
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(ex));
			throw apie;
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Unable to close the connection to the database.");
			}
		}

	}

	public static int searchRequestEx(Connection conn, int sysId,
			String fieldName, String fieldValue) throws DatabaseException,
			SQLException {
		int requestId = 0;

		Field field = Field.lookupBySystemIdAndFieldName(sysId, fieldName);
		if (field == null)
			return 0;
		String sql = "SELECT request_id from requests_ex "
				+ "where sys_id = ? " + "and field_id = ? "
				+ "and varchar_value = ?";

		PreparedStatement ps = conn.prepareStatement(sql);

		ps.setInt(1, sysId);
		ps.setInt(2, field.getFieldId());
		ps.setString(3, fieldValue);

		ResultSet rs = ps.executeQuery();

		if (null != rs) {
			if (rs.next()) {
				requestId = rs.getInt(1);
			}
			rs.close();
		}
		ps.close();
		ps = null;

		return requestId;
	}

	@SuppressWarnings("unchecked")
	public void AddRequest(String docNumberFieldName, int deliverableFieldID,
			ArrayList<DocNumberFileTuple> modelDataForAddition,
			UserClient currentUser, int sysId,
			HashMap<String, Object> dataObject) {
		/**
		 * TODO
		 */
		AddRequest addRequest = new AddRequest();
		Connection conn = null;

		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();

		HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>> attachmentInfo = new HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>>();
		Hashtable<Integer, Collection<AttachmentInfo>> deliverableAttachments = new Hashtable<Integer, Collection<AttachmentInfo>>();
		List<Integer> dcrRequestList = new ArrayList<Integer>();
		HashMap<String, Object> tempHashMap = new HashMap<String, Object>();

		try {

			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			for (DocNumberFileTuple eachModelForAddition : modelDataForAddition) {

				eachModelForAddition.set("op", "add");
				Hashtable<String, String> aParamTable = new Hashtable<String, String>();
				try {
					aParamTable.put(Field.BUSINESS_AREA, BusinessArea
							.lookupBySystemId(sysId).getSystemPrefix());
				} catch (DatabaseException e) {

					e.printStackTrace();
				}

				aParamTable.put(Field.USER, currentUser.getUserLogin());
				// aParamTable.put("Revision",
				// eachModelForAddition.getREVISION());

				for (TbitsModelData tmd : HolcimPluginConstants.fields) {
					if ((eachModelForAddition.get((String) tmd.get("name")) != null)) {
						aParamTable.put((String) tmd.get("name"),
								(String) eachModelForAddition.get((String) tmd
										.get("name")));
					}
				}
				if (eachModelForAddition.getSubject() != null
						&& (!eachModelForAddition.getSubject().trim()
								.equals("")))
					aParamTable.put(HolcimPluginConstants.TITLE,
							eachModelForAddition.getSubject());

				// aParamTable.put(docNumberFieldName,
				// eachModelForAddition.getDOC_NUMBER());

				Field deliverableField = null;
				try {
					deliverableField = Field.lookupBySystemIdAndFieldId(sysId,
							deliverableFieldID);
				} catch (DatabaseException e) {

					e.printStackTrace();
				}

				ArrayList<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();

				if (eachModelForAddition.getFILE_PATH() != null) {
					File file1 = new File(eachModelForAddition.getFILE_PATH());

					if (!file1.exists()) {// inner if
						/**
						 * TODO: flag an error
						 */
					} else {// inner else
						if (!file1.isDirectory()) {// 2-inner if
							// AttachmentInfo att1 = uploader.upload(file1);
							AttachmentInfo att1 = new Uploader()
									.copyIntoRepository(file1,
											eachModelForAddition
													.getfilenameToBeUsed());
							if (att1 == null) {// 3 inner if
								/**
								 * Flag error
								 */
							}
							atts.add(att1);
						}// 2 inner if
						else {// 2-inner else
							// atts.addAll(uploadFolder(file, errorWriter,
							// uploader, trnProcessId, action));
						}
					}// inner else
				}// if

				String fieldName = deliverableField.getName();

				String jsonString = AttachmentInfo.toJson(atts);

				aParamTable.put(fieldName, jsonString);

				Request r = null;
				try {
					r = addRequest.addRequest(conn, tbitsResMgr, aParamTable);

					dcrRequestList.add(r.getRequestId());
					deliverableAttachments.put(r.getRequestId(),
							(Collection<AttachmentInfo>) r
									.getObject(deliverableField));

					if (!attachmentInfo.containsKey(r.getRequestId())) {
						attachmentInfo
								.put(
										r.getRequestId(),
										new HashMap<String, Collection<HolcimAttachmentInfo>>());
					}

					Collection<AttachmentInfo> attahmentInfoOfRequest = (Collection<AttachmentInfo>) r
							.getObject(deliverableField);
					HolcimAttachmentInfo tempHolcimAttInfo = new HolcimAttachmentInfo();
					ArrayList<HolcimAttachmentInfo> tempCollectionOfHolcimInfo = new ArrayList<HolcimAttachmentInfo>();

					for (AttachmentInfo eachInfo : attahmentInfoOfRequest) {

						tempHolcimAttInfo.setName(eachInfo.getName());
						tempHolcimAttInfo.setSIZE(eachInfo.getSize());
						tempHolcimAttInfo.setRequestFileID(eachInfo
								.getRequestFileId());
						tempHolcimAttInfo.setRepotFileID(eachInfo
								.getRepoFileId());

						tempCollectionOfHolcimInfo.add(tempHolcimAttInfo);

						attachmentInfo.get(r.getRequestId()).put(
								deliverableField.getName(),
								tempCollectionOfHolcimInfo);
						eachModelForAddition.setRequestID(r.getRequestId());
						eachModelForAddition.set("is_added", "true");

					}

				} catch (APIException e) {

					eachModelForAddition.set("is_added", "false");
					e.printStackTrace();
					LOG.error("Error occurred while adding  request: \n"
							+ e.getMessage(), e);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Error occurred while adding  request: \n"
							+ e.getMessage(), e);
				}

			}
			conn.commit();

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out
						.println("Unable to close the connection to the database.");
			}
		}
		tempHashMap.put("attachmentInfo", attachmentInfo);
		tempHashMap.put("dcrRequestList", dcrRequestList);

		dataObject.put("dataObjcet", tempHashMap);
	}

	@SuppressWarnings("unchecked")
	private boolean updateRequest(String docNumberFieldName,
			int deliverableFieldID, int SysId,
			ArrayList<DocNumberFileTuple> docNumberFileTuple,
			UserClient CurrentUser, HashMap<String, Object> dataObject)
			throws SQLException, DatabaseException {

		HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>> attachmentInfo = new HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>>();
		Hashtable<Integer, Collection<AttachmentInfo>> deliverableAttachments = new Hashtable<Integer, Collection<AttachmentInfo>>();
		List<Integer> dcrRequestList = new ArrayList<Integer>();
		HashMap<String, Object> tempHashMap = new HashMap<String, Object>();

		int userid = CurrentUser.getUserId();
		User user = User.lookupByUserId(userid);
		BusinessArea ba = BusinessArea.lookupBySystemId(SysId);
		int baId = SysId;

		int deliverableFieldId = deliverableFieldID;
		Connection conn = null;
		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
		conn = DataSourcePool.getConnection();
		conn.setAutoCommit(false);

		Field deliverableField = Field.lookupBySystemIdAndFieldId(SysId,
				deliverableFieldId);

		try

		{
			for (DocNumberFileTuple data : docNumberFileTuple) {
				if (data.getRequestID() != -1) {

					data.set("op", "up");
					UpdateRequest updateRequest = new UpdateRequest();
					Hashtable<String, String> aParamTable = new Hashtable<String, String>();

					aParamTable.put(Field.BUSINESS_AREA, ((Integer) baId)
							.toString());
					aParamTable.put(Field.USER, user.getUserLogin());
					aParamTable.put(Field.REQUEST, ((Integer) data
							.getRequestID()).toString());
					// aParamTable.put("Revision", data.getREVISION());
					// aParamTable.put("VendorNo", data.getvendorNo());
					for (TbitsModelData tmd : HolcimPluginConstants.fields) {
						if ((data.get((String) tmd.get("name")) != null)) {
							aParamTable
									.put((String) tmd.get("name"),
											(String) data.get((String) tmd
													.get("name")));
						}
					}

					if (data.getSubject() != null
							&& (!data.getSubject().trim().equals("")))
						aParamTable.put(HolcimPluginConstants.TITLE, data
								.getSubject());

					ArrayList<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();
					FileWriter errorWriter = null;
					try {
						errorWriter = new FileWriter("attachment_err.txt", true);
					} catch (IOException e1) {

						e1.printStackTrace();
					}

					if (data.getFILE_PATH() != null) {
						File file1 = new File(data.getFILE_PATH());

						if (!file1.exists()) {// inner if
							/**
							 * TODO: flag an error
							 */
						} else {// inner else
							if (!file1.isDirectory()) {// 2-inner if
								// AttachmentInfo att1 = uploader.upload(file1);
								AttachmentInfo att1 = new Uploader()
										.copyIntoRepository(file1, data
												.getfilenameToBeUsed());
								if (att1 == null) {// 3 inner if
									/**
									 * Flag error
									 */
								}
								atts.add(att1);
							}// 2 inner if
							else {// 2-inner else
								// atts.addAll(uploadFolder(file, errorWriter,
								// uploader, trnProcessId, action));
							}
						}// inner else
					}// if
					try {
						errorWriter.close();
					} catch (IOException e) {

						e.printStackTrace();
					}

					// --------------------------------------------------------------------//
					String fieldName = deliverableField.getName();
					String jsonString = AttachmentInfo.toJson(atts);
					aParamTable.put(fieldName, jsonString);
					Request updatedRequest;
					try {
						updatedRequest = updateRequest.updateRequest(conn,
								tbitsResMgr, aParamTable);
						data.set("is_updated", "true");

						dcrRequestList.add(updatedRequest.getRequestId());
						deliverableAttachments.put(updatedRequest
								.getRequestId(),
								(Collection<AttachmentInfo>) updatedRequest
										.getObject(deliverableField));

						if (!attachmentInfo.containsKey(updatedRequest
								.getRequestId())) {
							attachmentInfo
									.put(
											updatedRequest.getRequestId(),
											new HashMap<String, Collection<HolcimAttachmentInfo>>());
						}

						Collection<AttachmentInfo> attahmentInfoOfRequest = (Collection<AttachmentInfo>) updatedRequest
								.getObject(deliverableField);
						HolcimAttachmentInfo tempHolcimAttInfo = new HolcimAttachmentInfo();
						ArrayList<HolcimAttachmentInfo> tempCollectionOfHolcimInfo = new ArrayList<HolcimAttachmentInfo>();

						for (AttachmentInfo eachInfo : attahmentInfoOfRequest) {

							tempHolcimAttInfo.setName(eachInfo.getName());
							tempHolcimAttInfo.setSIZE(eachInfo.getSize());
							tempHolcimAttInfo.setRequestFileID(eachInfo
									.getRequestFileId());
							tempHolcimAttInfo.setRepotFileID(eachInfo
									.getRepoFileId());

							tempCollectionOfHolcimInfo.add(tempHolcimAttInfo);
						}

						attachmentInfo.get(updatedRequest.getRequestId()).put(
								deliverableField.getName(),
								tempCollectionOfHolcimInfo);

					} catch (TBitsException e) {

						e.printStackTrace();

						data.set("is_updated", "false");
						LOG.error("Error occurred while Updating  request: \n"
								+ e.getMessage(), e);
					} catch (APIException e) {

						e.printStackTrace();
						data.set("is_updated", "false");
						LOG.error("Error occurred while Updating  request: \n"
								+ e.getMessage(), e);
					}
				}
			}
			conn.commit();
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out
						.println("Unable to close the connection to the database.");
				return false;
			}
		}
		HashMap<String, Object> hm = (HashMap<String, Object>) dataObject
				.get("dataObjcet");

		HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>> attachmentInfo1 = (HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>>) hm
				.get("attachmentInfo");
		attachmentInfo1.putAll(attachmentInfo);
		List<Integer> dcrRequestList1 = (List<Integer>) hm
				.get("dcrRequestList");
		dcrRequestList1.addAll(dcrRequestList);

		tempHashMap.put("attachmentInfo", attachmentInfo1);
		tempHashMap.put("dcrRequestList", dcrRequestList1);

		dataObject.put("dataObjcet", tempHashMap);

		return true;

	}

	public DocNumberFileTuple GetObject(DocNumberFileTuple a) {
		// TODO Auto-generated method stub
		return a;
	}

	public TbitsTreeRequestData GetObject(TbitsTreeRequestData a) {
		// TODO Auto-generated method stub
		return a;
	}

	public HashMap<String, Object> processGridData(
			ArrayList<DocNumberFileTuple> models, int sysid)
			throws TbitsExceptionClient {

		ArrayList<DocNumberFileTuple> missingDocs = new ArrayList<DocNumberFileTuple>();
		ArrayList<DocNumberFileTuple> presentDocs = new ArrayList<DocNumberFileTuple>();
		ArrayList<DocNumberFileTuple> masterDocList = new ArrayList<DocNumberFileTuple>();

		ArrayList<Integer> listOfPresentRequests = new ArrayList<Integer>();
		HashMap<String, Object> dataToBereturned = new HashMap<String, Object>();

		String docName = HolcimPluginConstants.DocumentFieldNameToBeUsed;

		for (DocNumberFileTuple eachMetaData : models) {
			masterDocList.add(this.getFilesAfterKnowingSubject(sysid,
					eachMetaData.getfilenameToBeUsed(), eachMetaData
							.getFILE_PATH(), eachMetaData.getSubject()));
		}

		try {
			searchWithSubjectKnown(masterDocList, sysid, docName);
		} catch (DatabaseException e) {

			e.printStackTrace();
		} catch (APIException e) {

			e.printStackTrace();
		}

		Boolean resultOfvalidaion = true;

		for (DocNumberFileTuple metadata : masterDocList) {
			if (metadata.getRequestID() != -1) {
				listOfPresentRequests.add(metadata.getRequestID());
				presentDocs.add(metadata);
			} else {
				if (!metadata.getIs_Regex_Correct()) {
					resultOfvalidaion = false;
				}
				missingDocs.add(metadata);
			}
		}
		/*
		 * if (missingDocs.size() == 0) { resultOfvalidaion = true; }
		 */
		dataToBereturned.put("result", resultOfvalidaion);
		dataToBereturned.put("NMap", missingDocs);
		dataToBereturned.put("PathMap", presentDocs);

		return dataToBereturned;
	}

	/*
	 * params String docNumberFieldName, int deliverableFieldID = 34, int SysId,
	 * ArrayList<DocNumberFileTuple> docNumberFileTuple, UserClient CurrentUser
	 */
	public HashMap<String, Object> AddAndUpdateRequest(int deliverableFieldID,
			int sysId, ArrayList<DocNumberFileTuple> modelData,
			UserClient CurrentUser) {

		HashMap<String, Object> dataObject = new HashMap<String, Object>();
		ArrayList<DocNumberFileTuple> modelDataForUpdation = new ArrayList<DocNumberFileTuple>();
		ArrayList<DocNumberFileTuple> modelDataForAddition = new ArrayList<DocNumberFileTuple>();

		try {

			for (DocNumberFileTuple eachGridData : modelData) {
				if (eachGridData.getRequestID() == -1) {
					modelDataForAddition.add(eachGridData);

				} else {

					modelDataForUpdation.add(eachGridData);
				}

			}

			this.AddRequest(HolcimPluginConstants.DocumentFieldNameToBeUsed,
					deliverableFieldID, modelDataForAddition, CurrentUser,
					sysId, dataObject);

			this.updateRequest(HolcimPluginConstants.DocumentFieldNameToBeUsed,
					deliverableFieldID, sysId, modelDataForUpdation,
					CurrentUser, dataObject);

			dataObject.put("modelData", modelData);

			return dataObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/*
	 * attachmentInfo; dcrRequestList
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> createDtn(int txnId, UserClient user,
			HashMap<String, Object> dtnDataMap) {

		HashMap<String, String> deliverableParams = new HashMap<String, String>();
		deliverableParams.put(Field.BUSINESS_AREA, (String) dtnDataMap
				.get(Field.BUSINESS_AREA));
		List<Integer> dcrRequestList = new ArrayList<Integer>();
		GenericTransmittalCreator plugin = new GenericTransmittalCreator();
		HashMap<String, Object> finalMapToBeReturned = new HashMap<String, Object>();

		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
		HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>> HolcimattachmentInfo = new HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>>();
		HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>> ActualattachmentInfo = new HashMap<Integer, HashMap<String, Collection<AttachmentInfo>>>();

		boolean SuccessOfDtnProcess;
		Connection conn = null;
		try {
			LOG.info("*************************************" + "/n"
					+ "Creation of dtn process started" + "/n"
					+ "******************************" + "/n");
			dcrRequestList = (List<Integer>) dtnDataMap.get("dcrRequestList");
			HolcimattachmentInfo = (HashMap<Integer, HashMap<String, Collection<HolcimAttachmentInfo>>>) dtnDataMap
					.get("attachmentInfo");

			for (Integer reqId : HolcimattachmentInfo.keySet()) {

				ActualattachmentInfo.put(reqId,
						new HashMap<String, Collection<AttachmentInfo>>());
				HashMap<String, Collection<HolcimAttachmentInfo>> tempHashMap = HolcimattachmentInfo
						.get(reqId);

				for (String eachDeliverableField : tempHashMap.keySet()) {
					ActualattachmentInfo.get(reqId).put(eachDeliverableField,
							new ArrayList<AttachmentInfo>());

					ArrayList<HolcimAttachmentInfo> attInfo = (ArrayList<HolcimAttachmentInfo>) HolcimattachmentInfo
							.get(reqId).get(eachDeliverableField);

					ArrayList<AttachmentInfo> FinalAttInfo = new ArrayList<AttachmentInfo>();

					for (HolcimAttachmentInfo eachHolcimAttachment : attInfo) {

						AttachmentInfo tempFinalAttInfo = new AttachmentInfo();
						tempFinalAttInfo
								.setSize(eachHolcimAttachment.getSIZE());
						tempFinalAttInfo.setRepoFileId(eachHolcimAttachment
								.getRepoFileID());
						tempFinalAttInfo
								.setName(eachHolcimAttachment.getNAME());
						tempFinalAttInfo.setRequestFileId(eachHolcimAttachment
								.getRequestFileID());

						FinalAttInfo.add(tempFinalAttInfo);

					}

					ActualattachmentInfo.get(reqId).get(eachDeliverableField)
							.addAll(FinalAttInfo);

				}

			}

			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			deliverableParams.put("user", user.getUserLogin());
			String requestList = "";

			for (int i = 0; i < dcrRequestList.size(); i++) {
				requestList += dcrRequestList.get(i) + ",";
			}

			deliverableParams.put("requestList", requestList);
			deliverableParams.put(
					GenericTransmittalCreator.TRN_PROCESS_ID_PARAM, Integer
							.toString(txnId));

			if (dcrRequestList.size() > 0) {
				String retValue = plugin.createTransmittal(conn, tbitsResMgr,
						deliverableParams, ActualattachmentInfo);
				if (retValue.equals("") || retValue.equals(null)) {

					SuccessOfDtnProcess = false;
					LOG.info("*************************" + "/n"
							+ "Dtn has not been created with this id" + "/n"
							+ "************************" + "/n");
					throw new TBitsException("Transmittal process id : "
							+ txnId + " failed.");

				} else {
					System.out.println("Process Completed");
					SuccessOfDtnProcess = true;
					finalMapToBeReturned.put("success", true);
					finalMapToBeReturned.put("ResultOfDtnProcess", retValue);
					LOG.info("*************************" + "/n"
							+ "Dtn has been created with this id" + "/n"
							+ "************************" + "/n");

				}
			} else {
				System.out
						.println("REQUEST LIST EMPTY    ++++++++++++++++++++++++++++++++++++");
				LOG
						.error("REQUEST LIST EMPTY    ++++++++++++++++++++++++++++++++++++");
				finalMapToBeReturned.put("success", false);

			}
			tbitsResMgr.commit();
			conn.commit();
			
		} catch (APIException apiException) {
			try {
				finalMapToBeReturned.put("success", false);
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();

				apiException.printStackTrace();
			} catch (SQLException e) {
				finalMapToBeReturned.put("success", false);
				e.printStackTrace();
			}

		} catch (SQLException e) {
			try {
				finalMapToBeReturned.put("success", false);
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database", e));

		} catch (TBitsException tbitsException) {
			finalMapToBeReturned.put("success", false);
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				tbitsException.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (Exception ex) {
			finalMapToBeReturned.put("success", false);
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				ex.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (Throwable ex) {
			finalMapToBeReturned.put("success", false);
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				ex.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		finally {
			try {
				if ((conn != null) && !conn.isClosed()) {

					conn.close();
				}
			} catch (SQLException e) {
				System.out
						.println("Unable to close the connection to the database.");

			}
		}

		return finalMapToBeReturned;

	}

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
	 *@param model
	 */
	@SuppressWarnings("static-access")
	void searchWithSubjectKnown(
			ArrayList<DocNumberFileTuple> docNumberFileTuples, int sysid,
			String docNumberFieldName) throws DatabaseException, APIException {

		// BusinessArea.lookupBySystemId(sysid);

		int baId = sysid;
		Connection conn = null;
		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
		AddRequest addRequest = new AddRequest();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			for (DocNumberFileTuple data : docNumberFileTuples) {
				/*
				 * if (data.getREVISION() != null) { if
				 * (data.getREVISION().equals("00")) { data.setREVISION("R0"); }
				 * else if (data.getREVISION().equals("02")) {
				 * data.setREVISION("R2"); } else if
				 * (data.getREVISION().equals("03")) { data.setREVISION("R1"); }
				 * else { data.setREVISION("R0"); } }
				 */
				String docno = data
						.get(HolcimPluginConstants.DocumentFieldNameToBeUsed);
				if (docno != null) {
					int requestId = this.searchRequestEx(conn, baId,
							docNumberFieldName, docno);

					TbitsTreeRequestData trd = getDataByRequestId(BusinessArea
							.lookupBySystemId(sysid).getSystemPrefix(),
							requestId);
					if (requestId == 0) {

						LOG.info("Request Doesnot Exist in the BA for  "
								+ docno + " Please create the request First");
						data.setRequestID(-1);

					} else {

						LOG.info("Request Present in the BA " + docno);

						data.setRequestID(requestId);

					}
				} else {
					LOG.info("Request Doesnot Exist in the BA for  " + docno
							+ " Please create the request First");
					data.setRequestID(-1);

				}

			}

			conn.commit();
			tbitsResMgr.commit();
		} catch (SQLException e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				e.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database", e));
			throw apie;
		} catch (Exception ex) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
				ex.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
				LOG.error(e);
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(ex));
			throw apie;
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Unable to close the connection to the database.");
			}
		}

	}

	@Override
	public HashMap<String, Object> fetchConstants(int systemId)
			throws TbitsExceptionClient {

		HashMap<String, Object> hm = new HashMap<String, Object>();
		try {
			HolcimPluginConstants.fields = fetchFieldsdata(systemId);

			Comparator<TbitsModelData> cmp = new Comparator<TbitsModelData>() {

				@Override
				public int compare(TbitsModelData o1, TbitsModelData o2) {

					int s1 = (Integer) o1.get("regex_order");
					int s2 = (Integer) o2.get("regex_order");
					if (s1 > s2)
						return 1;
					else if (s1 == s2)
						return 0;
					else if (s1 < s2)
						return -1;
					return 0;

				}
			};

			Collections.sort(HolcimPluginConstants.fields, cmp);

			HashMap<String, String> hm1 = fetchConfigData(systemId);

			hm.put("fields", HolcimPluginConstants.fields);
			hm.put("config", hm1);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}
		return hm;
	}
}
