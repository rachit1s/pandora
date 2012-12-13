/**
 * 
 */
package ksk;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;

import com.ibm.icu.text.SimpleDateFormat;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;

/**
 * @author lokesh
 *
 */
public final class KSKUtils {
	
	public static final String ZUBERI = "ZUBERI";

	public static final String RWIS = "RWIS";

	public static final String RWIS_CEC = "RWIS_ERC";
	
	public static final Object RWIS_SITE = "RWIS_SITE";

	private static final int DTN_NUMBER_FIELD_ID = 35;

	private static final String ORGANIZATION_KMPCL = "KMPCL";

	private static final String ORGANIZATION_WPCL = "WPCL";

	public static final String STRING_DASH = "-";

	protected static final String DCR_SYS_ID = "dcr_sys_id";
	
	private static final String TO_ADDRESS = "toAddress";
	public static final String EMPTY_STRING = "";
	public static final String DELIMETER_COMMA = ",";
	public static final String DELIMETER_HASH = "#";
	public static final String DELIMETER_SEMICOLON = ";";
	public static final String PDF = ".pdf";
	
	public static final String FIELD_DRAWING_NO = "DrawingNumber";
	static final String REVISION = "Revision";
	
	static final String documentTypes = "CD - Compact Disk, SC - Soft Copy by e-Mail, RT - Reproducible Tracing, PR - Paper Prints";
	static final String approvalCategory = "PL - Preliminary issue,IN - For Information,FA - For Apporval,AP - Approved Drg./Doc,RC - Released For Construction,AB - As Built Drawing";
	
	public static final int TRANSMITTAL_BUSINESS_AREA = 1;
	public static final int DCR_BUSINESS_AREA = 2;
	public static final int DTR_BUSINESS_AREA = 3;
	public static final int OTHER_BUSINESS_AREA = 4;
	
	public static final String IDCBALIST = "SEPCO,PHO,CEC,DCPL";
	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	public static final String ELECON_CEC = "ELECON_CEC";

	public static final String ELECON_PHO = "ELECON_PHO";

	public static final String ELECON = "ELECON";
	
	public static final String ELECON_SITE = "ELECON_SITE";

	public static File getResourceFile(String filePath){
		URL url = KSKUtils.class.getResource(filePath);
		String file = url.getFile();
		File f = new File(file);
		return f;
	}
	
	public static String generateTransmittalNoteUsingBirt(String rptDesignFileName, KskTemplateHelper kth, String outputFileName)
	throws EngineException {
		TBitsReportEngine tBitsEngine = new TBitsReportEngine();
		IReportEngine engine = tBitsEngine.getEngine();
		engine.getConfig().getAppContext().put("KSKTemplateHandler", kth);

		engine.changeLogLevel(Level.WARNING);

		// Report Design
		System.out.println("opening design.");
		String tempDir = Configuration.findAbsolutePath (PropertiesHandler.getProperty(
				transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		IReportRunnable design = engine.openReportDesign(tempDir + "/../tbitsreports/" + rptDesignFileName); //TSR_ScriptedDataReport.rptdesign");

		IRunTask task = engine.createRunTask(design);
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, KskTemplateHelper.class.getClassLoader()); 

		String rptdoc = tempDir + "/output.rptdocument";
		task.run(rptdoc);
		task.close();

		IReportDocument iReportDocument = engine.openReportDocument(rptdoc);
		IRenderTask rendertask = engine.createRenderTask(iReportDocument);

		//Set parent classloader report engine
		rendertask.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, KskTemplateHelper.class.getClassLoader());			
		System.out.println("Rendering...");


		String pdfFilePath = tempDir + File.separator + outputFileName + PDF;

		// Setup rendering to HTML
		PDFRenderOption options = new PDFRenderOption();
		options.setOutputFileName(pdfFilePath);
		options.setOutputFormat("pdf");
		rendertask.setRenderOption(options);

		// run and render report
		rendertask.render();
		if(rendertask.getStatus() != IRenderTask.STATUS_SUCCEEDED)
			throw new EngineException("The task didnt succeed.");

		System.out.println("Finished rendering and hence closing.");
		rendertask.close();
		return pdfFilePath;
	}

	public static ByteArrayOutputStream generateTransmittalNoteInHtml(HttpServletRequest request , String rptDesignFileName, KskTemplateHelper kth)
	throws EngineException{
		TBitsReportEngine tBitsEngine = new TBitsReportEngine();
		IReportEngine engine = tBitsEngine.getEngine();
		engine.getConfig().getAppContext().put("KSKTemplateHandler", kth);

		engine.changeLogLevel(Level.WARNING);

		// Report Design
		System.out.println("opening design.");
		String tempDir = Configuration.findAbsolutePath (PropertiesHandler.getProperty(
				transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		IReportRunnable design = engine.openReportDesign(tempDir + "/../tbitsreports/" + rptDesignFileName); //TSR_ScriptedDataReport.rptdesign");

		IRunTask task = engine.createRunTask(design);
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, KskTemplateHelper.class.getClassLoader()); 

		String rptdoc = tempDir + "/output.rptdocument";
		task.run(rptdoc);
		task.close();

		IReportDocument iReportDocument = engine.openReportDocument(rptdoc);
		IRenderTask rendertask = engine.createRenderTask(iReportDocument);

		//Set parent classloader report engine
		rendertask.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, KskTemplateHelper.class.getClassLoader());			
		System.out.println("Rendering...");

		// Setup rendering to HTML
		HTMLRenderOption options = new HTMLRenderOption();
		options.setImageHandler(new HTMLServerImageHandler());
		
		ByteArrayOutputStream htmlOS = new ByteArrayOutputStream();		
		options.setOutputStream( htmlOS);
		options.setOutputFormat("html");
		
		ServletContext sc = request.getSession().getServletContext();
		options.setBaseImageURL(request.getContextPath()+"/web/images/dashboard_images");
		options.setImageDirectory(sc.getRealPath("/web/images/dashboard_images"));

		// Setting this to true removes html and body tags
		options.setEmbeddable(true);
		rendertask.setRenderOption(options);

		// run and render report
		rendertask.render();
		/*if(rendertask.getStatus() != IRenderTask.STATUS_SUCCEEDED)
			throw new EngineException("The task didnt succeed.");*/

		System.out.println("Finished rendering and hence closing.");
		rendertask.close();
		
		return htmlOS;
	}
	
	/**
	 * Looks up the MailingList membership of a user using his loginName/emailId
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
		if (user != null){
			mailList = MailListUser.getMailListsByDirectMembership(user.getUserId());
		}
		return mailList;
	}
	
	public static boolean isUserExists(ArrayList<RequestUser> ruList, String userName){
		for (RequestUser ru : ruList){
			try {
				User usr = ru.getUser();				
				if ((usr.getUserLogin().equals(userName) || (usr.getEmail().equals(userName)))){
					return true;
				}
				else 
					return false;
			} catch (DatabaseException e) {				
				e.printStackTrace();
			}
		}
		return false;
	}
	
	//Get DTN system Id for a particular DCR system Id.
	public static int[] getTransmittalSystemId (int aDCRSystemId) throws SQLException{
		int trnSysIds[] = {0,0};
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_dcr_dtn_ba_map WHERE dcr_sys_id=?");
			ps.setInt(1, aDCRSystemId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next()){
				trnSysIds[0] = rs.getInt(2);
				trnSysIds[1] = rs.getInt(3);
			}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return trnSysIds;
	}	
	
	/**
	 * Returns Transmittal BA id and DTR BA id as an array of integer with int[0] containing the former while int[1] containing the later.
	 * @param connection
	 * @param aDCRSystemId
	 * @return
	 * @throws SQLException
	 */
	public static int[] getTransmittalSystemId (Connection connection, int aDCRSystemId) throws SQLException{
		int trnSysIds[] = {0,0};
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_dcr_dtn_ba_map WHERE dcr_sys_id=?");
			ps.setInt(1, aDCRSystemId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next()){
				trnSysIds[0] = rs.getInt(2);
				trnSysIds[1] = rs.getInt(3);
			}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			throw sqle;
		} 
		return trnSysIds;
	}
	
	/**
	 * @param ba
	 * @param requestId
	 * @return
	 */
	public static String getLinkedRequests(BusinessArea ba, String[] requestId) {
		String linkedRequests = "";
		for (int index=0; index<requestId.length; index++){
			if (index == 0)
				linkedRequests = ba.getSystemPrefix() + DELIMETER_HASH + requestId[index];
			else
				linkedRequests=linkedRequests + KSKUtils.DELIMETER_COMMA + ba.getSystemPrefix() + DELIMETER_HASH + requestId[index];
		}
		return linkedRequests;
	}

	
	/**
	 * @param transId	Transmittal Id.
	 * @return			returns a String representing transmittal request id
	 */
	public static String getFormattedStringFromNumber(int transmittalId) {
		String ref;
		NumberFormat formatter = new DecimalFormat("00000");
		ref = formatter.format(transmittalId);
		return ref;
	}	
	
	/**
	 * Get selected deliverable attachments for a particular request in JSON format.
	 * @param ba
	 * @param request
	 * @param fileNames
	 * @param index
	 * @return
	 */
	public static String getSelectedAttachmentsJSONString(BusinessArea ba, Request request, String[] fileNames,int index){
		Collection<AttachmentInfo> attachmentCollection = KSKUtils.getSelectedAttachments(request, fileNames, index);
		return AttachmentInfo.toJson(attachmentCollection).toString();
	}

	/**
	 * Gets a Collection of deliverable attachments selected for transmittal for a particular request .
	 * @param request
	 * @param fileNames
	 * @param index
	 * @return
	 */
	public static Collection<AttachmentInfo> getSelectedAttachments(Request request, String[] fileNames, int index) {
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>(); 
		
		if(fileNames != null){
			if (fileNames.length == 0)
				return trnAttCollection;
			String reqFileList = fileNames[index];
			if (reqFileList.trim().equals(EMPTY_STRING))
				return trnAttCollection;
			else{
				for (String reqFileInfo : reqFileList.split("<br3>")){//DELIMETER_SEMICOLON)){
					//Splits the String using semicolon because selected attachments information is passed 
					//as attachmentName:repoFieldId:attachmentSize in the URL query.
					String[] reqAttInfo = reqFileInfo.split("<br1>");//:");
					String attName = reqAttInfo[0];
					int repoFileId = Integer.parseInt(reqAttInfo[1]);
					int attSize = Integer.parseInt(reqAttInfo[2]);

					//for (AttachmentInfo ai : reqAttachments){
					//if (ai.name.equals(attName) && (ai.size == attSize) && (ai.repoFileId == repoFileId)){
					AttachmentInfo tAI = new AttachmentInfo();
					tAI.name = attName;
					tAI.size = attSize;
					tAI.repoFileId = repoFileId;
					tAI.requestFileId = 0;
					trnAttCollection.add(tAI);
				}	
			}
		}
		else{
			LOG.warn("No attachments found.");
			System.out.println("Selected attachments is null, hence ignoring it.");
		}
		return trnAttCollection;
	}

	/**
	 * Merges two Collections. To be used when adding new attachments into a request. This method, checks
	 * if an attachments with the same name existed previously and modifies the new attachments collection.
	 * @param newAttachments
	 * @param prevAttachments
	 */
	public static void mergeAttachmentsLists(Collection<AttachmentInfo> newAttachments,
			Collection<AttachmentInfo> prevAttachments) {
		//If no previous attachments were found return without adding anything to newAttachments.
		if ((prevAttachments == null) || prevAttachments.isEmpty())
			return;
		
		//If no new attachments are there, add previous attachments to the new attachments collection, so they are retained in the request.
		if ((newAttachments == null) || newAttachments.isEmpty())
			newAttachments.addAll(prevAttachments);
		else{
			Collection<AttachmentInfo> oldAI = new ArrayList<AttachmentInfo>();
			if ((newAttachments != null) && (!newAttachments.isEmpty())){			
				for(AttachmentInfo ai : prevAttachments){
					boolean isFound = false;
					for(AttachmentInfo cAI : newAttachments){
						if(ai.name.equals(cAI.name)){
							cAI.requestFileId = ai.requestFileId;
							isFound = true;
							break;
						}
						else
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
		for (String key : keySet){
			assingneeList.add(key.trim());
		}
		return assingneeList;
	}

	/**
	 * Retrieves the target business area fields for updation after transmittal.
	 * @param connection
	 * @param dcrSysId
	 * @param targetSysId
	 * @return
	 * @throws DatabaseException
	 */
	public static Hashtable<String, String> getTargetBusinessAreaFields(Connection connection, int dcrSysId, int targetSysId) throws DatabaseException{
		Hashtable<String, String> fieldMap = new Hashtable<String, String>();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT dcr_field_id, target_field_id FROM transmittal_dcr_target_ba_fields_map WHERE dcr_sys_id=? and target_sys_id=?");
			ps.setInt(1, dcrSysId);
			ps.setInt(2, targetSysId);
			ResultSet rs = ps.executeQuery();
			if(rs != null){
				while (rs.next()){
					int dcrFieldId = rs.getInt("dcr_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					Field dcrField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, dcrFieldId);
					Field targetField = KSKUtils.lookupBySystemIdAndFieldId(connection, targetSysId, targetFieldId);
					if ((targetField != null) && (dcrField != null))
						fieldMap.put(dcrField.getName(), targetField.getName());
				}
			}
			return fieldMap;			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving mapped fields.", sqle);
		}		
	}
	
	public static Field getTargetBusinessAreaField(Connection connection, int dcrSysId, int targetSysId) 
			throws DatabaseException{
		Field targetField = null;
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT dcr_field_id, target_field_id FROM " +
					"transmittal_dcr_target_ba_fields_map WHERE dcr_sys_id=? and dcr_field_id=? and target_sys_id=?");
			ps.setInt(1, dcrSysId);
			ps.setInt(2, dcrSysId);
			ps.setInt(3, targetSysId);
			ResultSet rs = ps.executeQuery();
			if(rs != null){
				if (rs.next()){
					int dcrFieldId = rs.getInt("dcr_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					Field dcrField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, dcrFieldId);
					targetField = KSKUtils.lookupBySystemIdAndFieldId(connection, targetSysId, targetFieldId);
					if ((targetField != null) && (dcrField != null))
						return targetField;
				}
			}
			return targetField;			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving mapped fields.", sqle);
		}		
	}
	
	public static Hashtable<String,Integer> getInwardDTNMapping(int dcrSystemId, String[] requestList,
																	String inwardDTNFieldName) throws DatabaseException{
		
		Hashtable<String, Integer> inwardDTNTable = new Hashtable<String, Integer>();
		// Iterate for each request and retrieve inward DTN Number.
		for(String reqIdStr : requestList){
			int requestId = Integer.parseInt(reqIdStr);
			Request request = Request.lookupBySystemIdAndRequestId(dcrSystemId, requestId);
			
			//Retrieve inward DTN number.
			String inwardDTNNumber = request.get(inwardDTNFieldName);
			if ((inwardDTNNumber == null) ||inwardDTNNumber.trim().equals(""))
				continue;
			else			
				if (inwardDTNTable.get(inwardDTNNumber) == null){
					int index = inwardDTNTable.size();
					inwardDTNTable.put(inwardDTNFieldName, index + 1);
				}
				else
					continue;
		}
		
		return inwardDTNTable;
	}

	/**
	 * Takes in a hash table containing the mapping of inward-dtn-number and its assigned serial number.
	 * @param inwardDTNTable
	 * @return
	 */
	private static ArrayList<String[]> getInwardDTNList(Hashtable<String, Integer> inwardDTNTable) {
		ArrayList<String[]> referenceNumberList  = new ArrayList<String[]>(); 
		
		if (inwardDTNTable.isEmpty() || inwardDTNTable == null){
			referenceNumberList.add(new String[]{STRING_DASH, STRING_DASH, STRING_DASH});
			return referenceNumberList;
		}
		
		for(String dtnNumber : inwardDTNTable.keySet()){
			Integer index = inwardDTNTable.get(dtnNumber);
			String[] referenceNumberRow = new String[]{String.valueOf(index), dtnNumber, ""};
			referenceNumberList.add(referenceNumberRow);
		}
		
		return referenceNumberList;
	}
	
	/**
     * This method returns the BusinessArea object corresponding to the given
     * System Id.
     *
     * @param aSystemId Business Area Id.
     *
     * @return BusinessArea object corresponding to this SystemId
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static BusinessArea lookupBySystemId(Connection connection, int aSystemId) throws DatabaseException {
        BusinessArea ba = null;
        /*
        // Look in the mapper first.
        String key = Integer.toString(aSystemId);

        if (ourBAMap != null) {
            ba = ourBAMap.get(key);

            return ba;
        }

        // else try to get the BA record from the database.
       // Connection connection = null;
         */
        try {
            //connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_lookupBySystemId ?");

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

            message.append("An exception occurred while retrieving the ").append("BusinessArea Object.").append("\nSystem Id: ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        }

        return ba;
    }
	
    
    public static Field lookupBySystemIdAndFieldId(Connection connection, int aSystemId, int aFieldId) throws DatabaseException {
        Field field = null;
        
        try {
            CallableStatement cs = connection.prepareCall("stp_field_lookupBySystemIdAndFieldId ?, ?");

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

            message.append("An exception occured while retrieving the field.").append("\nSystem Id: ").append(aSystemId).append("\nField Id : ").append(aFieldId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } 
        
        return field;
    }

    public static Field lookupBySystemIdAndFieldName(Connection connection, int aSystemId, String aFieldName) throws DatabaseException {
        Field field = null;

        try {
            CallableStatement cs = connection.prepareCall("stp_field_lookupBySystemIdAndFieldName ?, ?");

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

            message.append("An exception occured while retrieving the field.").append("\nSystem Id : ").append(aSystemId).append("\nField Name: ").append(aFieldName).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } 

        return field;
    }
    
    
	/**
	 * This method prepares the data requires by the transmittal template(.rptdesign) for creating a transmittal template.
	 * 
	 * @param dcrSystemId
	 * @param dtnSystemId 
	 * @param requestList
	 * @param approvalCategoryList
	 * @param documentList
	 * @param quantityList
	 * @param distributionList
	 * @param transmittalId
	 * @param transmittalSubject
	 * @param user - Logger object
	 * @param inwardDTNFieldName - List of all reference DTNs through which the transmittal of drawings had occurred previously from the other firm.
	 * @param transmittalDate 
	 * @param transmittalProcessParameters 
	 * @return
	 * @throws NumberFormatException
	 * @throws DatabaseException
	 * @throws TBitsException 
	 */
	public static KskTemplateHelper getKskTemplateHelper(int dcrSystemId, int dtnSystemId, String requestList, String revisionList, 
			String approvalCategoryList, String documentList, String quantityList, String summaryList, String toList,
			String ccList, String transmittalId, String transmittalSubject, String remarks, User user,
			String inwardDTNFieldName, Hashtable<String,String> transmittalParams, String transmittalDate,
			String draftedBy) throws NumberFormatException, DatabaseException, TBitsException{
		
		String[] requestId = requestList.split(",");
		String[] revision = revisionList.split(",");
		String[] appCategories = approvalCategoryList.split(",");
		String[] docTypes = documentList.split(",");
		String[] quantity = quantityList.split(",");
		String[] summary = summaryList.split("<br,>");
		String distributionList = toList + "," + ccList;
		String kindAttentionString = "";
		
		//Prepare "Kind Attn." string.
		for (String userLogin : toList.split(",")){
			User kaUser = User.lookupByUserLogin(userLogin);
			if (kaUser == null)
				kaUser = User.lookupByEmail(userLogin);
			
			if (kaUser != null){
				kindAttentionString = (kindAttentionString.trim().equals(""))? kaUser.getDisplayName():
																					kindAttentionString + ", " + kaUser.getDisplayName();
			}
		}
		
		//Create the list of inward DTN numbers for the reference DTN numbers table(inwardDTNList).
		//Hashtable<String, Integer> inwardDTNMapping = getInwardDTNMapping(dcrSystemId, requestId, inwardDTNFieldName);
		ArrayList<String[]> inwardDTNList = new ArrayList<String[]>();//;getInwardDTNList(inwardDTNMapping);
		
		//Create the list of drawing details for the drawings being transmitted.
		ArrayList<String[]> drawingsList = new ArrayList<String[]>();
		
		for (int i= 0 ; i < requestId.length; i++){			
			//Get the corresponding reference id for the inward DTN number.
			String dtnRefId = STRING_DASH;
			Request tRequest = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestId[i]));
			String inDTNNumber = tRequest.get(inwardDTNFieldName);
			//Integer refId = inwardDTNMapping.get(inDTNNumber);
			
			if (inDTNNumber != null){
				dtnRefId = inDTNNumber;
				if (!dtnRefId.equals("")){
					String curReqId = requestId[i];
					Date refDTNLoggedDate = getLoggedDateByInwardDTNNumber(dtnSystemId, inDTNNumber,
							curReqId);					
					
					if (refDTNLoggedDate != null){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String refLoggedDateStr = sdf.format(refDTNLoggedDate);
						inwardDTNList.add(new String[]{i + "", inDTNNumber, refLoggedDateStr});			
						dtnRefId = inDTNNumber + "\t\t Dated: " + refLoggedDateStr;
					}
				}
			}
			
			//Create the drawing info.
			String[] drawing = new String[]{String.valueOf(i + 1), dtnRefId, tRequest.getSubject(), tRequest.get(FIELD_DRAWING_NO), 
					/*tRequest.get(REVISION)*/revision[i], quantity[i], appCategories[i], docTypes[i], summary[i],
					tRequest.get("SEPCODocumentNumber")};//tRequest.getSummary()};
			drawingsList.add(drawing);
		}
		
		//Create distribution list 
		ArrayList<String[]> distributionUserList = new ArrayList<String[]>();
		String[] userLogins = distributionList.split(",");
		String prevUserOrganization = "";
		for (int index=0; index < userLogins.length; index++){
			User recipient = User.lookupByUserLogin(userLogins[index]);
			if (recipient == null)
				recipient = User.lookupByEmail(userLogins[index]);
			
			if (recipient != null){
				if (!isExistsInDistributionList(distributionUserList, userLogins[index])){
					int slNumber = index + 1;
					String rPhone = recipient.getMobile();
					if ((rPhone == null) || rPhone.trim().equals(EMPTY_STRING))
						rPhone = STRING_DASH;
					
					String organization = recipient.getFirmCode();
					if ((organization == null) || organization.trim().equals(""))
						organization = STRING_DASH;
					else if (organization.toUpperCase().trim().equals(ORGANIZATION_WPCL))
						organization = ORGANIZATION_KMPCL;
					
					// TODO: Check why the firm is missing if the next person is also from the same "firm".
					//The following is a hack to avoid this problem. This happens in Birt while generating the 
					//transmittal note using a .rptdesign template.					
					if (organization.equals(prevUserOrganization)){						
						organization = organization + " ";
					}			
					
					prevUserOrganization = organization;
															
					//slnumber, name, organization, email, phoneNumber
					distributionUserList.add(new String[]{slNumber+"", recipient.getDisplayName(), organization, recipient.getEmail(), 
							rPhone, STRING_DASH, STRING_DASH, recipient.getUserLogin()});
				}
				else 
					continue;
			}
			else
				LOG.info("User with login: \"" + userLogins[index] + "\", could not be found in tBits. So, igonring him from the list." );
		}
		
		//Create the logger info String array for filling user info who is creating the transmittal.
		String loggerPhone = user.getMobile();
		if ((loggerPhone == null) || loggerPhone.trim().equals(EMPTY_STRING))
			loggerPhone = STRING_DASH;
		String[] loggerInfo = new String[]{user.getDisplayName(), loggerPhone, user.getEmail()};
		
		String toAddress = transmittalParams.get(TO_ADDRESS);
		String[] approvalCatList = approvalCategory.split(",");
		String[] docTypeList= documentTypes.split(",");
		
		
		//Parameters for TemplateHelper
		//String dtnNumber, String subject, String kindAttentionString, ArrayList<String[]> refTransmittalNumbers,
		//ArrayList<String[]> drawingsList, ArrayList<String[]> distributionList, String[] loggerInfo
		KskTemplateHelper kth = new KskTemplateHelper(toAddress, transmittalId, transmittalSubject, remarks, kindAttentionString,
				inwardDTNList, drawingsList, approvalCatList, docTypeList, distributionUserList, loggerInfo, transmittalDate, 
				draftedBy);		
		
		return kth;
	}

	/**
	 * @param dtnSystemId
	 * @param inwardDTNNumber
	 * @param dcrRequestId
	 * @throws DatabaseException
	 */
	protected static Date getLoggedDateByInwardDTNNumber(int dtnSystemId,
			String inwardDTNNumber, String dcrRequestId)
			throws DatabaseException {
		Connection connection = null;
		Date refDTNLoggedDate = null;
		try {			
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("select logged_datetime from requests r join " +
					"requests_ex re on r.request_id=re.request_id and r.sys_id=re.sys_id where r.sys_id=? and " +
					"re.field_id=? and re.varchar_value =?");
			ps.setInt(1, dtnSystemId);
			ps.setInt(2, DTN_NUMBER_FIELD_ID);
			ps.setString(3, inwardDTNNumber);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				refDTNLoggedDate = rs.getDate(1);
			else
				LOG.warn("Logged Date not found for reference DTN number: " + inwardDTNNumber);											
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving reference DTN number for request_id: " 
					+ dcrRequestId, sqle);				
		}finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException("Error occurred while retrieving reference DTN number for request_id: " 
							+ dcrRequestId, e);
				}							
			}
		}
		return refDTNLoggedDate;
	}
			
	public static String replaceStrangeDash( String str ) 
	{
		String out = "" ;
		if( null == str ) 
			return out ;
		
		char strangeDash1 = (char)150 ;
		char strangeDash2 = (char)8211 ;
		char validDash = '-' ;
		
		for( int i = 0 ; i < str.length() ; i++ )
		{
			if( str.charAt(i) == strangeDash1 || str.charAt(i) == strangeDash2 )
				out += validDash ;
			else
				out += str.charAt(i) ;
		}
		
		return out ;
	}
	
	public static String removeRedundantDashes(String corrNo )
	{
		if( null == corrNo || corrNo.equalsIgnoreCase("") ) 
			return "" ;
		
		String ncn = "" ;
		boolean gotD = false ;
		 
		for( int i = 0 ; i < corrNo.length() ; i++ )
		{
			if(  '-' == corrNo.charAt(i) )
			{
				if( true == gotD  )
					continue ;
				else 
				{
					ncn += corrNo.charAt(i) ;
					gotD = true ;
				}
			}
			else
			{
				ncn += corrNo.charAt(i) ;
				gotD = false ;
			}
		}
		
		if(ncn.length() == 0 )
			return ncn ;
		else
		{
			// remove trailing -es
			if( '-' == ncn.charAt(0) ) // first character
			{
				if( ncn.length() > 1 )
				{
					ncn = ncn.substring(1) ;				
				}
				else return "" ;
			}
			
			if('-' == ncn.charAt(ncn.length()-1)) // last character
			{
				if( ncn.length() > 1 )					
				{
					ncn = ncn.substring(0, ncn.length()-1) ;
				}
				else return "" ;
			}
		}
		
		return ncn ;
	}
	
	public static final String TABLE_NAME = "ksk_user_info" ;
	
	public static final String USER_ID = "user_id" ; // int (primary) NOT NULL
	public static final String FIRM = "firm" ; // varchar(100) NOT NULL // short name of the firm 
	public static final String LOCATION = "location" ; // varchar(2) NULL 
	public static final String DESIGNATION = "designation" ; // varchar(50) NULL  
	public static final String ADDRESS = "address" ; // varchar(500) NULL 
	public static final String SEX = "sex" ;// ENUM('F','M') NULL
	public static final String FULL_FIRM_NAME = "full_firm_name" ; // varchar(200) NULL // full name of firm
	public static final String USER_INFO = "SELECT " + USER_ID +" , " + FIRM + " , " + LOCATION + " , " + DESIGNATION + " , " + ADDRESS + " , " + SEX + " , " + FULL_FIRM_NAME +
	   " FROM " + TABLE_NAME +
	   " WHERE " + USER_ID + "=?" ;
	private static final String ERR_DB_ACCESS = "Exception while accessing the database table the database";
	private static final String ERR_CON_ONCLOSE = "Exception while closing the connection object.";
	
	/**
	 * 
	 * @param user_id
	 * @return null if user doesnot exist, else a Hashtable containing following key-value pairs 
	 * firm = String
	 * location = String
	 * designation = String
	 * address = String 
	 * sex = String ( F/M )
	 * @throws TBitsException , IllegalArgumentException
	 */
	public static Hashtable<String, String> getUserInfo( int user_id ) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(USER_INFO) ;
			pstmt.setInt(1, user_id ) ;
			
			Hashtable<String, String > info = new Hashtable<String,String>() ;
			
			ResultSet rs = pstmt.executeQuery() ;
			if( ! rs.next() )
			 {
				LOG.info("User does not exist int database table: " + TABLE_NAME) ;
				throw new IllegalArgumentException("User does not exist in database table: " + TABLE_NAME) ; 
			 }
			
			// otherwise extract data 
			String firm = rs.getString(FIRM) ;
			if( null == firm ) firm = "" ;
			String location = rs.getString(LOCATION) ;
			if(null == location ) location = "" ;
			String designation = rs.getString(DESIGNATION); 
			if( null == designation ) designation = "" ;
			String address = rs.getString(ADDRESS) ;
			if( null == address) address = "" ;
			String sex = rs.getString(SEX) ;
			if( null == sex ) sex = "" ;
			String full_firm_name = rs.getString(FULL_FIRM_NAME); 
			if( null == full_firm_name ) full_firm_name = "" ;
					
			info.put(FIRM, firm ) ;
			info.put(LOCATION, location ) ;
			info.put( DESIGNATION , designation ) ;
			info.put(ADDRESS , address ) ; 
			info.put(SEX, sex ) ;
			info.put(FULL_FIRM_NAME, full_firm_name) ;
			
			return info ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;					
				}
			}
		}
		
	}
	
	private static boolean isExistsInDistributionList(ArrayList<String[]> distributionUserList, String userName){
		boolean isExists = false;
		userName = userName.trim();
		
		for (String[] userInfo : distributionUserList){
			//Matches email or login.
			if(userInfo[3].equals(userName) || userInfo[7].equals(userName)){
				isExists = true;
				return isExists;
			}
			else
				continue;
		}
		return isExists;
	}
	
	public static ArrayList<Integer> lookupAllDCRBusinessAreaIds() throws DatabaseException{
		ArrayList<Integer> dcrBAIdList;
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			dcrBAIdList = lookupAllDCRBusinessAreaIds(connection);
		}catch (SQLException sqle) {
			throw new DatabaseException("Exception occurred while retrieving DCR business areas.", sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		
		return dcrBAIdList;
	}
	
	
	public static ArrayList<Integer> lookupAllDCRBusinessAreaIds(Connection connection) throws DatabaseException {
		ArrayList<Integer> dcrBAIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection.prepareStatement("Select DISTINCT sys_id from transmittal_types");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					int dcrSysId = rs.getInt("sys_id");
					dcrBAIdList.add(dcrSysId);
				}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Exception occurred while retrieving DCR business areas.", sqle);
		}
		return dcrBAIdList;
	}

	public static boolean isExistsInDCRBAList(int systemId) {
		ArrayList<Integer> baIds = null;
		try {
			baIds = lookupAllDCRBusinessAreaIds();
			for(Integer baId : baIds){
				if (baId.intValue() == systemId)
					return true;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();			
			return false;
		}
		return false;
	}
	
	
	public static boolean isSEPCODCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("SEPCO"))
			return true;
		return false;			
	}
	
	public static boolean isODSEPCODCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("ODSEPCO"))
			return true;
		return false;			
	}
	
	public static boolean isPHODCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("PHO"))
			return true;
		return false;			
	}
	
	public static boolean isODPHODCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("ODPHO"))
			return true;
		return false;			
	}
	
	public static boolean isSiteDCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("SITE"))
			return true;
		return false;			
	}
	
	public static boolean isCECDCR(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("CEC"))
			return true;
		return false;
	}
	
	public static boolean isODCECDCR(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("ODCEC"))
			return true;
		return false;
	}
	
	public static boolean isDCPLDCR(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("DCPL"))
			return true;
		return false;
	}
	
	public static boolean isODDCPLDCR(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("ODDCPL"))
			return true;
		return false;
	}
	
	
	public static boolean isNWPDI(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("NWPDI"))
			return true;
		return false;
	}
	
	public static boolean isODNWPDI(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("ODNWEPDI"))
			return true;
		return false;
	}
	
	public static boolean isSITE(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals("SITE"))
			return true;
		return false;
	}
		
	public static boolean isZUBERIDCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(ZUBERI))
			return true;
		return false;			
	}
	
	public static boolean isRWISPHODCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(RWIS))
			return true;
		return false;			
	}
	
	public static boolean isRWISCECDCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(RWIS_CEC))
			return true;
		return false;			
	}
	
	public static boolean isRWISSITE(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(RWIS_SITE))
			return true;
		return false;			
	}
		
	public static boolean isEleconDCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(ELECON))
			return true;
		return false;			
	}
	
	public static boolean isEleconPHODCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(ELECON_PHO))
			return true;
		return false;			
	}
	
	public static boolean isEleconCECDCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(ELECON_CEC))
			return true;
		return false;			
	}
	
	public static boolean isEleconDCPLDCR(BusinessArea dcrBA) {
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(ELECON_DCPL))
			return true;
		return false;		
	}	
	
	public static boolean isEleconSiteDCR(BusinessArea dcrBA){
		if ((dcrBA != null) && dcrBA.getSystemPrefix().equals(ELECON_SITE))
			return true;
		return false;			
	}
	
	public static int getSlideOffset(Connection connection, int currentBASystemId,
			Request dcrRequest, Hashtable<String, String> paramTable, 
			int addRequestOffset, int updateRequestOffset, boolean isAddRequest) {
		//If revision is 'PL' and submitting again, its like add-request else it should be update request.				
		int slideOffset = 0;
		//If the request is being added for the first time, then revision is assumed to be 'PL'
		if(isAddRequest)
			slideOffset = addRequestOffset;
		else{
			String curRequestIdStr = paramTable.get(Field.REQUEST);
			Request curRequest = null;
			if ((curRequestIdStr != null) && (!curRequestIdStr.trim().equals(KSKUtils.EMPTY_STRING))){
				try {
					curRequest = Request.lookupBySystemIdAndRequestId(connection, currentBASystemId, Integer.parseInt(curRequestIdStr));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				if (curRequest != null){
					String prevRevision = curRequest.get(REVISION);
					String curRevision = dcrRequest.get(REVISION).trim();
					if (prevRevision.trim().equals(curRevision)
							&& (prevRevision.trim().equals("PL")))
						slideOffset = addRequestOffset;
					else
						slideOffset = updateRequestOffset;							
				}
			}
		}
		return slideOffset;
	}
	
	public static Timestamp getSlidedDueDate(boolean isAddRequest, int slideOffset) {		
		Calendar cal = Calendar.getInstance();	
		Date slidedDate = CalenderUtils.slideDate(cal.getTime(), slideOffset);
		Timestamp gmtDate = Timestamp.getTimestamp(slidedDate);
		return gmtDate;
	}
	
	/**
	 * 
	 * @param aSystemId
	 * @param aParentId of a particular request.
	 * @return sub-request count  
	 * @throws SQLException
	 */
	public static int getSubRequestCountBySysIdReqId (int aSystemId, int aParentId) throws SQLException{
		int count = 0;
		Connection connection = null;
	
		try {
			connection = DataSourcePool.getConnection();
	
			CallableStatement cs = connection.prepareCall("stp_request_lookupBySystemIdAndParentId ?, ?");
	
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
					LOG.warning("Exception occurred while closing the connection.");
				}
			}
	
			connection = null;
		}
	
		return count;
	}
	
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args){
		
		Connection connection = null;
		 try {
			connection = DataSourcePool.getConnection();
			getTargetBusinessAreaFields(connection, 17, 5);
		} catch (SQLException e) {
			e.printStackTrace();			
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				LOG.warn("Error occurred.");
			}
		}
	}
	
	/*
	 * Takes comma separated string and a compare string. Checks if the compare string 
	 * exists in the comma separated string.
	 */
	public static boolean isExistsInString(String parentString, String childString){
		if ((parentString == null) || (childString == null))
			return false;
		String[] strArray = parentString.split(",");
		for (String str : strArray){
			if (str.trim().equals(childString.trim()))
				return true;
			else continue;
		}
		return false;
	}
	
	public static String getTransmittalIdPrefix(Connection connection, int dcrSystemId,
			String transmittalType) {
		KSKTransmittalType transmittalTypeObj = null;
		Hashtable<String, String> tpParams = null;
		try {
			transmittalTypeObj = KSKTransmittalType.lookupTransmittalTypeBySystemIdAndName(connection, dcrSystemId, transmittalType);
			tpParams = KSKTransmittalType.getTransmittalProcessParameters(connection, dcrSystemId, transmittalTypeObj.getTransmittalTypeId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String trnIdPrefix = tpParams.get(TRANSMITTAL_ID_PREFIX);
		return trnIdPrefix;
	}

	public static boolean isSetDCPLDueDate(String statusName) {
		
		if (statusName.equals(STATUS_A4) || statusName.equals(STATUS_A5) || statusName.equals(STATUS_A6))
			return true;
		return false;
		
	}
	
	/**
	 * 
	 * @param propertyName
	 * @return value from app.properties file for a given propertyName
	 */
	public static String getProperty(String propertyName)
	{
		URL url = KSKUtils.class.getResource(APP_PROPERTIES);
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
	

	//Transmittal type bit field names
	public static final String TRANSMIT_FOR_AS_BUILT = "TransmitAsBuilt";

	public static final String TRANSMIT_FOR_RFC = "TransmitToWPCLRFC";
	
	public static final String TRANSMIT_TO_WPCL = "TransmitToWPCL";
	public static final String TRANSMITTED_TO_WPCL = "TransmittedToWPCL";
	
	public static final String TRANSMIT_TO_CEC = "TransmitToCEC";
	public static final String TRANSMITTED_TO_CEC = "TransmittedToCEC";
	
	public static final String TRANSMIT_TO_SEPCO = "TransmitToSEPCO";
	public static final String TRANSMITTED_TO_SEPCO = "TransmittedToSEPCO";

	public static final String TRANSMIT_RFC_VALIDATION = "TransmitRFCValidation";
	public static final String TRANSMITTED_RFC_VALIDATION = "TransmittedRFCValidation";

	public static final String TRANSMIT_AB_VALIDATION = "TransmitAbValidation";
	public static final String TRANSMITTED_AB_VALIDATION = "TransmittedAbValidation";
	
	public static final String TRANSMIT_TO_DCPL = "TransmitToDCPL";
	public static final String TRANSMITTED_TO_DCPL = "TransmittedToDCPL";
	
	public static final String TRANSMIT_TO_RWIS_PHO = "TransmitFromZuberiToKMPCL";
	public static final String TRANSMIT_TO_RWIS_PHO_RFC = "TransmitalRFCFromZuberi";
	
	public static final String TRANSMIT_TO_RWIS_CEC = "TransmitalFromRWISToCEC";
	
	public static final String TRANSMIT_TO_ZUBERI = "TransmitalFromCECToZuberi";
	public static final String TRANSMIT_TO_ERC_FROM_SITE = "TransmitToERCFromSite";
	
	//Status 
	public static final String STATUS_AB_AS_BUILT = "ABAsBuilt";

	public static final String STATUS_RFC_RECEIVED = "RFCReceived";

	public static final String STATUS_DOCUMENT_RECEIVED = "DocumentReceived";

	public static final String STATUS_A1 = "A1ReleaseForConstruction";	
	public static final String STATUS_A2 = "A2Approved";
	public static final String STATUS_A3 = "A3ApprovedexceptasnotedForwardFINALdrawing";
	public static final String STATUS_A4 = "A4ApprovedexceptasnotedResubmissionrequired";
	public static final String STATUS_A5 = "A5Disapproved"; 
	public static final String STATUS_A6 = "A6ForInformationReferenceonly";
		
	//Miscellaneous
	public static final String FALSE = "false";

	public static final String TRUE = "true";

	public static final String FIELD_INCOMING_TRANSMITTAL_NO = "IncomingTransmittalNo";

	public static final String TRANSMITTAL_ID_PREFIX = "transmittal_id_prefix";

	public static final String STATUS_DOCUMENT_COMMENTED = "DocumentCommented";

	public static final String STATUS_DOCUMENT_COMMENTED_BY_PHO = "DocumentCommentedByPHO";

	public static final String DOCUMENT_COMMENTED_BY_CEC = "DocumentCommentedByCEC";
	
	public static final String RFC_VALIDATION = "RFCValidation";

	public static final String APPROVED = "Approved";

	public static final String REJECTED = "Rejected";

	public static final String AS_BUILT_VALIDATION = "AsBuiltValidation";
	
	public static final String STATUS_PENDING_SUBMISSION = "PendingSubmission";

	private static final String APP_PROPERTIES = "app.properties";

	public static final String CATEGORY_TYPE_DOCUMENT = "Document";

	protected static final String SYSTEM_DEFINITION = "SystemDefinition";

	public static final String TRANSMIT_TO_ELECON_PHO = "TransmitToEleconPHO";

	public static final String TRANSMIT_TO_ELECON_PHO_RFC = "TransmitToEleconPHORFC";

	public static final String DTN_ELECON = "DTN_ELECON";

	public static final String TRANSMIT_TO_ELECON_CEC = "TransmitToEleconCEC";
	
	public static final String TRANSMIT_TO_ELECON_CEC_FROM_PHO = "TransmitToEleconCECFromPHO";

	public static final String TRANSMIT_TO_ELECON = "TransmitToElecon";

	public static final String TRANSMIT_TO_ELECON_CEC_FROM_SITE = "TransmitToEleconCECFromSite";
	
	public static final String TRANSMIT_TO_ELECON_DCPL = "TransmitToEleconDCPL";

	public static final String ELECON_DCPL = "ELECON_DCPL";

	public static final String TRANSMIT_T0_ELECON_SITE = "TransmitToEleconSite";

	public static final String TRANSMIT_TO_ELECON_RFC_VALIDATION = "TransmitToEleconRFCValidation";

	public static final String TRANSMIT_TO_ELECON_AB_VALIDATION = "TransmitToEleconABValidation";

	public static final Object TRANSMIT_TO_ELECON_AS_BUILT = "TransmitToEleconAsBuilt";

	public static final String PHO_SYS_PREFIX = "PHO";

	public static final String CEC_SYS_PREFIX = "CEC";
	
	public static final String DCPL_SYS_PREFIX = "DCPL";
	
	public static final String SITE_SYS_PREFIX = "SITE";

	public static final String DTN = "DTN";
	
}


