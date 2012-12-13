package invitationLetterWizard.com.tbitsGlobal.server;

import invitationLetterWizard.com.tbitsGlobal.client.ILService;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;



import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.IActivator;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

public class Activator extends TbitsRemoteServiceServlet implements ILService, TBitsPropEnum, IActivator, IFixedFields {

	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(
				ILService.class.getName(), Activator.class);
	}
	
	public static TBitsLogger LOG = TBitsLogger.getLogger("invitationLetterWizard.com.tbitsGlobal.server");

	private static final long serialVersionUID = 1L;

	private static final String INVITATION_LETTERS = "InvitationLetters";
	private static final String REF = "ref";
	private static final String PASSPORT_NO = "PassportNo";
	private static final String INVITATION_LETTER_NO = "InvitationLetterNo";
	
	private static final String PROPERTY_IL_BA = "il_prefix";
	private static final String PROPERTY_INVITE_BA = "invite_prefix";
	private static final String PROPERTY_IL_BUSINESS_TEMPLATE = "il_business_template";
	private static final String PROPERTY_IL_EMPLOYEMENT_TEMPLATE = "il_employment_template";
	private static final String PROPERTY_IL_EMPLOYEMENT_CERTIFICATION_TEMPLATE = "il_employment_certification_template";
	private static final String PROPERTY_IL_EMBASSY_FIELD = "il_embassy_field";
	private static final String PROPERTY_IL_PROJECT_FIELD = "il_project_field";
	private static final String PROPERTY_ROLES = "roles";

	public boolean createTransmittal(String sysPrefix, HashMap<Integer, TbitsTreeRequestData> users, ArrayList<String[]> scheduleList, HashMap<String, String> paramTable) throws TbitsExceptionClient {
		Connection conn = null;
		Request IlReq = null;

		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();

		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);

			User u = WebUtil.validateUser(this.getRequest());

			BusinessArea edBa = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if(edBa == null){
				throw new TbitsExceptionClient("Business Area with prefix : " + sysPrefix + " was not found");
			}
			
			int maxId = 0; 
			String rptType = paramTable.get(REQUEST_TYPE);
			if(rptType != null && !rptType.equals("Business")){
				maxId = APIUtil.getMaxId(sysPrefix + "_ed");
			}
			List<File> filesPDF = generatePreview(edBa.getSystemPrefix(), users, scheduleList, paramTable, "pdf", maxId + "");
			
			Hashtable<String, String> aParamTable = new Hashtable<String, String>();

			String ilPrefix = getPropValue(sysPrefix, PROPERTY_IL_BA);
			BusinessArea ilBA = BusinessArea.lookupBySystemPrefix(ilPrefix);
			if(ilBA == null){
				throw new TbitsExceptionClient("Business Area with prefix : " + ilPrefix + " was not found");
			}

			HashSet<String> agencySet = new HashSet<String>();
			for (Integer id : users.keySet()) {
				Request user = Request.lookupBySystemIdAndRequestId(edBa
						.getSystemId(), id);
				if (user.get(CATEGORY) != null) {
					agencySet.add(user.get(CATEGORY));
				}
			}
			if(agencySet.size()==1)
			{
				aParamTable.put(CATEGORY, (String) agencySet.toArray()[0]);
			}
			aParamTable.put(BUSINESS_AREA, ilBA.getSystemId() + "");
			aParamTable.put(USER, u.getUserLogin());
			aParamTable.put(DESCRIPTION, "No:of People included in invitation letter = " + users.size());
			for (String s : paramTable.keySet()) {
				aParamTable.put(s, paramTable.get(s));
			}
			aParamTable.put(INVITATION_LETTER_NO, paramTable.get(REF));
			aParamTable.put(SUBJECT, paramTable.get(REF));
			
			Uploader uploader = new Uploader();
			uploader.setFolderHint(ilPrefix);
			ArrayList<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>();
			if (filesPDF != null) {
				for (File outPDFFile : filesPDF) {
					AttachmentInfo att = uploader.moveIntoRepository(outPDFFile);
					attachments.add(att);
				}
			}

			String attJson = AttachmentInfo.toJson(attachments);
			aParamTable.put(INVITATION_LETTERS, attJson);

			AddRequest addRequest = new AddRequest();
			IlReq = addRequest.addRequest(conn, tbitsResMgr, aParamTable);

			String invitePrefix = getPropValue(sysPrefix, PROPERTY_INVITE_BA);
			BusinessArea inviteBA = BusinessArea.lookupBySystemPrefix(invitePrefix);
			if(inviteBA == null){
				throw new TbitsExceptionClient("Business Area with prefix : " + invitePrefix + " was not found");
			}
			for (Integer id : users.keySet()) {
				Request user = Request.lookupBySystemIdAndRequestId(edBa.getSystemId(), id);
				aParamTable = new Hashtable<String, String>();
				if(user.get(CATEGORY) != null)
					aParamTable.put(CATEGORY, user.get(CATEGORY));
				
				if(user.get("Name") != null)
					aParamTable.put("name", user.get("Name"));

				if(user.get("DOB") != null)
					aParamTable.put("dob", user.get("DOB"));

				if(user.get("Designation") != null)
					aParamTable.put("designation", user.get("Designation"));

				if(user.get("PassportNo") != null)
					aParamTable.put("passport#", user.get("PassportNo"));

				if(user.get("PPExpiryDate") != null)
					aParamTable.put("expirydate", user.get("PPExpiryDate"));

				if(user.get("bloodgroup") != null)
					aParamTable.put("bloodgroup", user.get("bloodgroup"));

				if(user.get("emergencycontact#") != null)
					aParamTable.put("emergencycontact#", user.get("emergencycontact#"));

				if(user.get("placeofissue") != null)
					aParamTable.put("placeofissue", user.get("placeofissue"));

				if(user.get("issuedate") != null)
					aParamTable.put("issuedate", user.get("issuedate"));

				if(paramTable.containsKey(REF))
					aParamTable.put("InvitationLetterNo", paramTable.get(REF));

				if(user.get("gender") != null)
					aParamTable.put("gender", user.get("gender"));
				
				aParamTable.put("BatchNo", maxId + "");

				aParamTable.put(REQUEST_TYPE, paramTable.get(Field.REQUEST_TYPE));
				aParamTable.put(BUSINESS_AREA, inviteBA.getSystemId() + "");
				aParamTable.put(USER, u.getUserLogin() + "");
				aParamTable.put(DESCRIPTION, "The complete details of the applicant are available at "
										+ edBa.getSystemPrefix() + "#" + id + " and " + "The complete invitation letter details are available at "
										+ ilPrefix + "#" + IlReq.getRequestId());

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 15);
				Date date = cal.getTime();
				String dateVal = GWTServiceHelper.formatDate(date, TBitsConstants.API_DATE_FORMAT);
				aParamTable.put("ILExpiry", dateVal);

				aParamTable.put(SUBJECT, user.get("Name"));
				addRequest = new AddRequest();
				addRequest.addRequest(conn, tbitsResMgr, aParamTable);

				UpdateRequest update = new UpdateRequest();
				aParamTable = new Hashtable<String, String>();

				aParamTable.put(BUSINESS_AREA, edBa.getSystemId() + "");
				aParamTable.put(REQUEST, id + "");
				aParamTable.put(USER, u.getUserLogin() + "");
				aParamTable.put(DESCRIPTION, IlReq.get(SUBJECT) + " [" + ilPrefix + "#" + IlReq.getRequestId() + "] ");
				update.updateRequest(conn, tbitsResMgr, aParamTable);
			}

			conn.commit();
			tbitsResMgr.commit();
		} catch (DatabaseException e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (APIException e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (SQLException e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}

		return true;
	}

	public ArrayList<String> getPdfPreviewPath(String sysPrefix, 
			HashMap<Integer, TbitsTreeRequestData> users,
			ArrayList<String[]> scheduleList,
			HashMap<String, String> paramTable, boolean hasToBePublic,
			String outputFormat) throws TbitsExceptionClient {
		ArrayList<String> outFilePaths = new ArrayList<String>();
		
		try {
			List<File> files = generatePreview(sysPrefix, users, scheduleList, paramTable, outputFormat, "XXXX");
			if(files != null){
				for(File file : files){
					outFilePaths.add("/download-delete?saveAs=true&file=" + file.getName());
				}
			}
		} catch (IOException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return outFilePaths;
	}
	
	private List<File> generatePreview(String sysPrefix, HashMap<Integer, TbitsTreeRequestData> users,
			ArrayList<String[]> scheduleList,
			HashMap<String, String> paramTable,
			String outputFormat, String batchNo) throws IOException, TBitsException, SQLException, TbitsExceptionClient{
		List<File> outFiles = new ArrayList<File>();
		
		String outputFileName = "";

		String ref = paramTable.get(REF);
		if (ref != null && !ref.trim().equals(""))
			outputFileName = ref;
		String address = "The Visa Officer,<br>Indian Embassy,<br>Beijing, CHINA";
		String subject = paramTable.get("pdf_subject");
		String body1 = paramTable.get("body1");
		String body2 = paramTable.get("body2");

		ArrayList<String[]> userList = new ArrayList<String[]>();
		for (Integer id : users.keySet()) {
			TbitsTreeRequestData user = users.get(id);
			String[] arr = new String[5];
			arr[0] = user.getAsString("Name");

			String gender = user.getAsString("gender");
			if (gender != null && gender.toLowerCase().equals("male"))
				gender = "Male";
			if (gender != null && gender.toLowerCase().equals("female"))
				gender = "Female";
			arr[1] = gender;

			try {
				Date d = (Date) user.getAsPOJO("DOB").getValue();
				arr[2] = GWTServiceHelper.formatDate(d, "yyyy-MM-dd");
			} catch (Exception e) {
				arr[2] = "";
			}

			arr[3] = user.getAsString("Designation");
			arr[4] = user.getAsString("PassportNo");
			userList.add(arr);
		}
		
		InvitationTemplateHelper ith = new InvitationTemplateHelper(ref,
				subject, address, body1, body2, null, userList, scheduleList);
		
		String embassyFieldName = getPropValue(sysPrefix, PROPERTY_IL_EMBASSY_FIELD);
		if(embassyFieldName != null){
			String embassy = paramTable.get(embassyFieldName);
			if(embassy != null)
				ith.setEmbassy(embassy);
		}
		String projectFieldName = getPropValue(sysPrefix, PROPERTY_IL_PROJECT_FIELD);
		if(projectFieldName != null){
			String project = paramTable.get(projectFieldName);
			if(project != null)
				ith.setProject(project);
		}
		
		String rptType = paramTable.get(REQUEST_TYPE);
		if (rptType.equals("Business")) {
			String template = getPropValue(sysPrefix, PROPERTY_IL_BUSINESS_TEMPLATE);
			if(template != null){
				// "ksk_business_invitation_letter.rptdesign"
				File outFile = generateReport(template, ith, outputFileName, outputFormat);
				outFiles.add(outFile);
			}
		} else {
			String template = getPropValue(sysPrefix, PROPERTY_IL_EMPLOYEMENT_TEMPLATE);
			if(template != null){
				// "ksk_employment_invitation_letter.rptdesign"
				File outFile = generateReport(template, ith, outputFileName + "IL", outputFormat);
				outFiles.add(outFile);
			}
			
			ith.setBatchNo(batchNo);
			ith.setBatchRef(paramTable.get("batch_ref"));
			ith.setApplicants(paramTable.get("applicants"));
			
			template = getPropValue(sysPrefix, PROPERTY_IL_EMPLOYEMENT_CERTIFICATION_TEMPLATE);
			if(template != null){
				// "Ksk_employment_certification_letter.rptdesign"
				File outFile = generateReport(template, ith, outputFileName + "CONF", outputFormat);
				outFiles.add(outFile);
			}
		}
		
		return outFiles;
	}
	
	private File generateReport(String rptDesignFileName,
			InvitationTemplateHelper ith, String outputFileName,
			String outputFormat) throws IOException, TBitsException {
		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		
		HashMap<Object, Object> reportVariables = new HashMap<Object, Object>();
		reportVariables.put("KSKInvitationHandler", ith);
		
		outputFileName = outputFileName.replaceAll("/", "_");
		if (outputFileName.contains("/")) {
			int index = outputFileName.lastIndexOf('/');
			outputFileName = outputFileName.substring(index + 1);
		}
		
		String outputDir = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
		
		File outputFile = findUniqueFile(outputFormat, outputDir, outputFileName);
		outputFile = tBitsEngine.generatePDFFile(rptDesignFileName, reportVariables, null, outputFile);
		
		return outputFile;
	}
	
	private File findUniqueFile(String fileExt, String tmpOutputLoc, String prefix) throws IOException {
		File of;
		String outFileName = prefix;
		int i = 0;
		do {
			Random r = new Random();
			outFileName += r.nextInt(1000) + "-" + i++ + "." + fileExt;
			outFileName = outFileName.replaceAll(":", "_");
			of = new File(tmpOutputLoc + "/" + outFileName);
		} while (of.exists());
		of.createNewFile();
		return of;
	}
	
	private String getPropValue(String sysPrefix, String property) throws TbitsExceptionClient{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			String sql = "SELECT value " +
			"FROM il_props " +
			"where sys_prefix = ? and property = ?";
			
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, sysPrefix);
			statement.setString(2, property);
			
			ResultSet rs = statement.executeQuery();
			if(rs != null){
				if(rs.next()) {
					return rs.getString(1);
				}else{
					sql = "SELECT value " +
					"FROM il_props " +
					"where sys_prefix = '-' and property = ?";
					statement = conn.prepareStatement(sql);
					statement.setString(1, property);
					
					rs = statement.executeQuery();
					if(rs != null && rs.next()) {
						return rs.getString(1);
					}
				}
			}
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		
		return null;
	}

	/**
	 * Rule - When the person clicks on the create invitation letter, then based
	 * on PAssport Number, check in the invite BA, if the said person has the
	 * invite type = IL being created and status = Invitation letter issued,
	 * Visa Applied - Pending Decision or Visa Granted.
	 * @throws TbitsExceptionClient 
	 */
	public String verifyEmployees(String sysPrefix, String inviType, HashMap<Integer, TbitsTreeRequestData> employees) throws TbitsExceptionClient {
		String response = "";
		try {
			BusinessArea edBA = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if (edBA == null)
				return "ED Business Area does not exists";
			int edSysId = edBA.getSystemId();

			String invitePrefix = getPropValue(sysPrefix, PROPERTY_INVITE_BA);
			BusinessArea inviteBA = BusinessArea.lookupBySystemPrefix(invitePrefix);
			if(inviteBA == null){
				throw new TbitsExceptionClient("Business Area with prefix : " + invitePrefix + " was not found");
			}

			Field passField = Field.lookupBySystemIdAndFieldName(inviteBA.getSystemId(), "passport#");
			if (passField == null)
				return "Passport No. field does not exist in Invite BA";

			Connection connection = null;
			try {
				connection = DataSourcePool.getConnection();

				for (Integer id : employees.keySet()) {
					Request edReq = Request.lookupBySystemIdAndRequestId(edSysId, id);
					String passNo = edReq.get(PASSPORT_NO);
					String name = edReq.get("Name");

					String sql = "SELECT request_id from requests_ex "
							+ "where sys_id = ? " + "and field_id = ? "
							+ "and varchar_value = ? ";

					PreparedStatement ps = connection.prepareStatement(sql);

					ps.setInt(1, inviteBA.getSystemId());
					ps.setInt(2, passField.getFieldId());
					ps.setString(3, passNo);

					ResultSet rs = ps.executeQuery();
					if (null != rs) {
						while (rs.next()) {
							int requestId = rs.getInt(1);
							Request inviteReq = Request.lookupBySystemIdAndRequestId(inviteBA.getSystemId(), requestId);
							String status = inviteReq.get(Field.STATUS);
							String ILType = inviteReq.get(Field.REQUEST_TYPE);
							if (ILType.equals(inviType)) {
								if (status.equals("InvitationLetterIssued"))
									response += name + "(Passport No. " + passNo + ")"
											+ " has been issued an Invitation Letter\n";
								if (status.equals("VisaAppliedPendingDecision"))
									response += name + "(Passport No. " + passNo + ")"
											+ " has applied for visa and decision is pending\n";
								if (status.equals("VisaGranted"))
									response += name + "(Passport No. " + passNo + ")" + " has been granted a visa\n";
							}
						}
						rs.close();
					}
					ps.close();
					ps = null;
				}
			} catch (SQLException sqle) {
				LOG.info(TBitsLogger.getStackTrace(sqle));
				throw new TbitsExceptionClient(sqle);
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException sqle) {
						LOG.warn("Exception while closing the connection:", sqle);
						throw new TbitsExceptionClient(sqle);
					}

					connection = null;
				}
			}
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		if (response.equals(""))
			return "OK";
		else
			return response;
	}
	
	public HashMap<String, String> getTextStrings(String sysPrefix) throws TbitsExceptionClient{
		HashMap<String, String> response = new HashMap<String, String>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			String sql = "SELECT property, value " +
			"FROM il_content " +
			"where sys_prefix = ?";
			
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, sysPrefix);
			
			ResultSet rs = statement.executeQuery();
			
			if(rs != null){
				
				while(rs.next()) {
					String property = rs.getString(1);
					String value = rs.getString(2);
					response.put(property, value);
				}
			}
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		
		return response;
	}

	public List<String> getValidBAList() throws TbitsExceptionClient {
		List<String> validBas = new ArrayList<String>();
		try {
			User user = WebUtil.validateUser(this.getRequest());
			if(user != null){
				String commaSeparatedPrefixes = PropertiesHandler.getProperty("IL_PREFIXES");
				if(commaSeparatedPrefixes != null && !commaSeparatedPrefixes.equals("")){
					String[] prefixes = commaSeparatedPrefixes.split(",");
					for(String prefix : prefixes){
						BusinessArea ba = BusinessArea.lookupBySystemPrefix(prefix);
						if(ba != null){
							boolean canAddBa = true;
							String rolesString = this.getPropValue(prefix, PROPERTY_ROLES);
							if(rolesString != null){
								canAddBa = false;
								String[] roleIds = rolesString.split(",");
								List<Role> roles = Role.lookupRolesBySystemIdAndUserId(ba.getSystemId(), user.getUserId());
								List<Integer> userRoleIds = new ArrayList<Integer>();
								for(Role role : roles){
									userRoleIds.add(role.getRoleId());
								}
								
								for(String roleIdString : roleIds){
									try{
										int roleId = Integer.parseInt(roleIdString.trim());
										if(userRoleIds.contains(roleId)){
											canAddBa = true;
										}
									}catch(Exception e){}
								}
							}
							if(canAddBa)
								validBas.add(prefix.trim());
						}
					}
				}
			}
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return validBas;
	}

	public List<BAField> getPage1Fields(String sysPrefix) throws TbitsExceptionClient {
		String ilPrefix = getPropValue(sysPrefix, PROPERTY_IL_BA);
		if(ilPrefix != null){
			try {
				List<BAField> fields = new ArrayList<BAField>();
				User user = WebUtil.validateUser(this.getRequest());
				BusinessArea ilBA = BusinessArea.lookupBySystemPrefix(ilPrefix);
				if(user != null && ilBA != null){
					String embassyFieldName = getPropValue(sysPrefix, PROPERTY_IL_EMBASSY_FIELD);
					if(embassyFieldName != null){
						Field embassyField = Field.lookupBySystemIdAndFieldName(ilBA.getSystemId(), embassyFieldName);
						if(embassyField != null){
							BAField embassyBAField = GWTServiceHelper.fromField(ilBA, embassyField, user);
							if(embassyBAField != null){
								fields.add(embassyBAField);
							}
						}
					}
					
					String projectFieldName = getPropValue(sysPrefix, PROPERTY_IL_PROJECT_FIELD);
					if(projectFieldName != null){
						Field projectField = Field.lookupBySystemIdAndFieldName(ilBA.getSystemId(), projectFieldName);
						if(projectField != null){
							BAField projectBAField = GWTServiceHelper.fromField(ilBA, projectField, user);
							if(projectBAField != null){
								fields.add(projectBAField);
							}
						}
					}
				}
				return fields;
			} catch (DatabaseException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			} catch (TBitsException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}
		return null;
	}
}
