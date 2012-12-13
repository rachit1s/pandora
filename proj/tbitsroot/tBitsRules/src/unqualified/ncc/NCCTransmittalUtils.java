/**
 * 
 */
package ncc;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.ActionEx;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public final class NCCTransmittalUtils {
	
	static final String TRN_POST_TRANSMITTAL_FIELD_VALUES = "trn_post_transmittal_field_values";

	static final String TRN_SRC_TARGET_FIELD_MAPPING_TABLE = "trn_src_target_field_mapping";

	protected static final String SRC_SYS_ID = "src_sys_id";

	//private static final String TRANSMITTAL_TYPE_ID = "transmittal_type_id";

	public static final String STRING_DASH = "-";

	protected static final String DCR_SYS_ID = "dcr_sys_id";
	
	private static final String TO_ADDRESS = "toAddress";
	public static final String EMPTY_STRING = "";
	public static final String DELIMETER_COMMA = ",";
	public static final String DELIMETER_HASH = "#";
	public static final String DELIMETER_SEMICOLON = ";";
	public static final String PDF = ".pdf";
	
	public static final String FIELD_DRAWING_NO = "DrawingNo";
	static final String REVISION = "Revision";
	
	static final String documentTypes = "CD - Compact Disk, SC - Soft Copy by e-Mail, RT - Reproducible Tracing, PR - Paper Prints";
	static final String approvalCategory = "PL - Preliminary issue,IN - For Information,FA - For Apporval,AP - Approved Drg./Doc,RC - Released For Construction,AB - As Built Drawing";
	
	public static final String IDCBALIST = "SEPCO,PHO,CEC,DCPL";
	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	public static String SITE = "SITE";

	public static String IPLP = "IPLP";

	public static String DTR = "DTR";

	public static String IDTR = "IDTR";
	
	private static final String APP_PROPERTIES = "app.properties";

	public static File getResourceFile(String filePath){
		URL url = NCCTransmittalUtils.class.getResource(filePath);
		String file = url.getFile();
		File f = new File(file);
		return f;
	}
	
	public static String getProperty(String propertyName)
	{
		URL url = NCCTransmittalUtils.class.getResource(APP_PROPERTIES);
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
	
	//Get DTN system Id for a particular DCR system Id.
	public static int[] getTransmittalSystemId (int aDCRSystemId, int aTransmittalTypeId) throws SQLException{
		int trnSysIds[] = {0,0};
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_ba_map WHERE dcr_sys_id=? and transmittal_type_id=?");
			ps.setInt(1, aDCRSystemId);
			ps.setInt(2, aTransmittalTypeId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next()){
				trnSysIds[0] = rs.getInt(3);
				trnSysIds[1] = rs.getInt(4);
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
				linkedRequests=linkedRequests + NCCTransmittalUtils.DELIMETER_COMMA + ba.getSystemPrefix() 
									+ DELIMETER_HASH + requestId[index];
		}
		return linkedRequests;
	}

	
	/**
	 * @param transId	Transmittal Id.
	 * @return			returns a String representing transmittal request id
	 */
	public static String getFormattedStringFromNumber(int transmittalId) {
		String ref;
		NumberFormat formatter = new DecimalFormat("0000");
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
		Collection<AttachmentInfo> attachmentCollection = NCCTransmittalUtils.getSelectedAttachments(fileNames, index);
		return AttachmentInfo.toJson(attachmentCollection).toString();
	}

	/**
	 * Gets a Collection of deliverable attachments selected for transmittal for a particular request .
	 * @param fileNames
	 * @param index
	 * @return
	 */
	public static Collection<AttachmentInfo> getSelectedAttachments(String[] fileNames, int index) {
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>(); 
		
		if(fileNames != null){
			String reqFileList = fileNames[index];
			if (reqFileList.trim().equals(EMPTY_STRING))
				return trnAttCollection;
			else{
				for (String reqFileInfo : reqFileList.split("<br3>")){
					//Splits the String using semicolon because selected attachments information is passed 
					//as attachmentName:repoFieldId:attachmentSize in the URL query.
					String[] reqAttInfo = reqFileInfo.split("<br1>");
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
	public static Hashtable<String, String> getTargetBusinessAreaFields(Connection connection, int trnProcessId, int dcrSysId, int targetSysId) throws DatabaseException{
		Hashtable<String, String> fieldMap = new Hashtable<String, String>();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT src_field_id, target_field_id FROM "
					+ TRN_SRC_TARGET_FIELD_MAPPING_TABLE
					+ " WHERE trn_process_id=? and src_sys_id=? and target_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrSysId);
			ps.setInt(3, targetSysId);
			ResultSet rs = ps.executeQuery();
			if(rs != null){
				while (rs.next()){
					int dcrFieldId = rs.getInt("src_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					Field dcrField = lookupBySystemIdAndFieldId(connection, dcrSysId, dcrFieldId);
					Field targetField = lookupBySystemIdAndFieldId(connection, targetSysId, targetFieldId);
					if (dcrField == null) 
						LOG.warn("Skipping copying the value of field with DCR field Id: " + dcrFieldId + ", for business area with id: " + dcrSysId);
					else if (targetField == null)
						LOG.warn("Skipping copying the value of field with Target BA field Id: " + targetFieldId + ", for business area with id: " + targetSysId);
					else if ((dcrField != null) && (targetField != null)){
						if (dcrField.getDataTypeId() != DataType.ATTACHMENTS)
							fieldMap.put(dcrField.getName(), targetField.getName());
					}
				}
			}
			return fieldMap;			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving mapped fields.", sqle);
		}		
	}
	
	/**
	 * Retrieves the target business area fields for updation after transmittal.
	 * @param connection
	 * @param dcrSysId
	 * @param targetSysId
	 * @return
	 * @throws DatabaseException
	 */
	public static Field getTargetBusinessAreaField(Connection connection, int trnProcessId, int dcrSysId,
			int dcrFieldId, int targetSysId) throws DatabaseException{
		Field targetField = null;
		try {			
			PreparedStatement ps = connection.prepareStatement("SELECT src_field_id, target_field_id FROM "
					+ TRN_SRC_TARGET_FIELD_MAPPING_TABLE
					+ " WHERE trn_process_id=? and " + SRC_SYS_ID + "=? " +
					"and src_field_id=? and target_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrSysId);
			ps.setInt(3, dcrFieldId);
			ps.setInt(4, targetSysId);
			ResultSet rs = ps.executeQuery();
			if(rs != null){
				while (rs.next()){
					//int dcrFieldId = rs.getInt("dcr_field_id");
					int targetFieldId = rs.getInt("target_field_id");
					targetField = lookupBySystemIdAndFieldId(connection, targetSysId, targetFieldId);					
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
	 * @param userLoginList
	 * @return
	 * @throws DatabaseException
	 */
	private static ArrayList<String[]> getUserDetailList(String userLoginList)
			throws DatabaseException, TBitsException {
		ArrayList<String[]> userInfoList = new ArrayList<String[]>();
		if ((userLoginList == null) || (userLoginList.isEmpty()))
			return userInfoList;
		String[] userLogins = userLoginList.split(",");
		for (int index=0; index < userLogins.length; index++){
			String userLogin = "";
			try{
				if ((userLogins[index] != null) && (!userLogins[index].trim().equals("")))
					userLogin = getUserLoginFromAutoCompleteString(userLogins[index]);
				else
					continue;
						
			}catch(ArrayIndexOutOfBoundsException abe){
				abe.printStackTrace();
				throw new TBitsException("Invalid user-name or email id provided in To/Cc List: " + userLogins[index], abe);
			}
			User recipient = User.lookupByUserLogin(userLogin);
			if (recipient == null)
				recipient = User.lookupByEmail(userLogin);
			
			if (recipient != null){
				if (!isExistsInDistributionList(userInfoList, userLogin)){
					int slNumber = index + 1;
					String rPhone = recipient.getMobile();
					if ((rPhone == null) || rPhone.trim().equals(EMPTY_STRING))
						rPhone = STRING_DASH;
					
					String organization = "-";
					organization = recipient.getFirmCode();						
					if ((organization == null) || organization.trim().equals(""))
						organization = STRING_DASH;
					
					//System.out.println("DistList:\n" + slNumber + "," +  recipient.getDisplayName() + "," 
					//+  organization + "," +  recipient.getEmail() + "," +  recipient.getMobile() + ","
					//+  STRING_DASH + "," +  STRING_DASH + "," +  recipient.getUserLogin());
					userInfoList.add(new String[]{slNumber+"", recipient.getDisplayName(), organization,
							recipient.getEmail(), rPhone, STRING_DASH, STRING_DASH, recipient.getUserLogin()});
				}
				else 
					continue;
			}
			else
				LOG.info("User with login: \"" + userLogin + "\", could not be found in tBits. So, igonring him from the list." );
		}
		return userInfoList;
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
	
	public static final String TABLE_NAME = "user_info" ;
	
	public static final String USER_ID = "user_id" ; // int (primary) NOT NULL
	public static final String FIRM = "firm" ; // varchar(100) NOT NULL // short name of the firm 
	public static final String LOCATION = "location" ; // varchar(2) NULL 
	public static final String DESIGNATION = "designation" ; // varchar(50) NULL  
	public static final String ADDRESS = "address" ; // varchar(500) NULL 
	public static final String SEX = "sex" ;// ENUM('F','M') NULL
	public static final String FULL_FIRM_NAME = "full_firm_name" ; // varchar(200) NULL // full name of firm 
	
	private static final String ERR_DB_ACCESS = "Exception while accessing the database table the database";
	private static final String ERR_CON_ONCLOSE = "Exception while closing the connection object.";
	
	public static final String USER_INFO = "SELECT " + USER_ID +" , " + FIRM + " , " + LOCATION + " , " + DESIGNATION + " , " + ADDRESS + " , " + SEX + " , " + FULL_FIRM_NAME +
	   " FROM " + TABLE_NAME +
	   " WHERE " + USER_ID + "=?" ;

	
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
				throw new IllegalArgumentException("User does not exist in database table: " + TABLE_NAME + ", for userId: " + user_id) ; 
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
	
	
	/*public static ArrayList<Integer> lookupAllDCRBusinessAreaIds(Connection connection) throws DatabaseException {
		ArrayList<Integer> dcrBAIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection.prepareStatement("Select " + DCR_SYS_ID + " from transmittal_dcr_dtn_ba_map");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					int dcrSysId = rs.getInt(DCR_SYS_ID);
					dcrBAIdList.add(dcrSysId);
				}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Exception occurred while retrieving DCR business areas.", sqle);
		}
		return dcrBAIdList;
	}*/
	
	/*public static ArrayList<Integer> lookupAllDCRBusinessAreaIds(Connection connection) throws DatabaseException {
		ArrayList<Integer> dcrBAIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection.prepareStatement("Select DISTINCT " + DCR_SYS_ID + " from transmittal_ba_map");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					int dcrSysId = rs.getInt(DCR_SYS_ID);
					dcrBAIdList.add(dcrSysId);
				}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Exception occurred while retrieving DCR business areas.", sqle);
		}
		return dcrBAIdList;
	}*/
	
	public static ArrayList<Integer> lookupAllDCRBusinessAreaIds(Connection connection) throws DatabaseException {
		ArrayList<Integer> dcrBAIdList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = connection.prepareStatement("Select DISTINCT " + SRC_SYS_ID + " from trn_processes");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					int dcrSysId = rs.getInt(SRC_SYS_ID);
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
	
	
	public static int getSlideOffsetBasedOnRevision(Connection connection, int currentBASystemId,
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
			if ((curRequestIdStr != null) && (!curRequestIdStr.trim().equals(NCCTransmittalUtils.EMPTY_STRING))){
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
					if (!prevRevision.trim().equals(curRevision))
						slideOffset = updateRequestOffset;							
				}
			}
		}
		return slideOffset;
	}
	
	public static int getExFieldSlideOffset(Connection connection, int currentBASystemId,
			Request dcrRequest, Hashtable<String, String> paramTable, 
			int addRequestOffset, int updateRequestOffset, boolean isAddRequest) {
		//If revision is 'R0' and submitting again, its like add-request else it should be update request.				
		int slideOffset = 0;
		//If the request is being added for the first time, then revision is assumed to be 'R0'
		if(isAddRequest)
			slideOffset = addRequestOffset;
		else{
			String curRequestIdStr = paramTable.get(Field.REQUEST);
			Request curRequest = null;
			if ((curRequestIdStr != null) && (!curRequestIdStr.trim().equals(NCCTransmittalUtils.EMPTY_STRING))){
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
					if (!prevRevision.trim().equals(curRevision))
						slideOffset = updateRequestOffset;							
				}
			}
		}
		return slideOffset;
	}
	
	public static int getExFieldSlideOffset (Connection connection, int currentBASystemId,
			Request dcrRequest, Hashtable<String, String> paramTable, 
			int addRequestOffset, int updateRequestOffset, boolean isAddRequest,
			String dtnNumberFieldName) throws TBitsException {
		int slideOffset = 0;
		//If the request is being added for the first time, then revision is assumed to be 'R0'
		String curRevision = dcrRequest.get(REVISION).trim();
		if(isAddRequest){
			if (curRevision.equals("R0"))
				slideOffset = addRequestOffset;
			else
				slideOffset = updateRequestOffset;
		}		
		else if (curRevision.equals("R0")){			
			slideOffset = addRequestOffset;
		}
		else{
			String curRequestIdStr = paramTable.get(Field.REQUEST);
			Request prevRequest = null;
			String prevDtnNumber = "";
			if ((curRequestIdStr != null) && (!curRequestIdStr.trim().equals(NCCTransmittalUtils.EMPTY_STRING))){
				try {
					if (currentBASystemId == dcrRequest.getSystemId()){
						Field dtnNumberField = Field.lookupBySystemIdAndFieldName(currentBASystemId, dtnNumberFieldName);
						ActionEx prevActionEx = ActionEx.lookupBySystemIdRequestIdActionIdFieldId(dcrRequest.getSystemId(),
													dcrRequest.getRequestId(), (dcrRequest.getMaxActionId() - 1), 
													dtnNumberField.getFieldId());
						prevDtnNumber = prevActionEx.getVarcharValue();
					}
					else{
						prevRequest = Request.lookupBySystemIdAndRequestId(connection, currentBASystemId, Integer.parseInt(curRequestIdStr));
						prevDtnNumber = prevRequest.get(dtnNumberFieldName);	
					}
					
				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw new TBitsException("Number format exception occurred.");
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Database exception occurred.");
				}
				if ((prevDtnNumber != null) && !prevDtnNumber.equals("")){
					if (!prevDtnNumber.trim().equals(curRevision))
						slideOffset = updateRequestOffset;							
				}
			}
		}
		return slideOffset;
	}
	
	public static int getResponseDateOffset (Connection connection, int currentBASystemId,
			Request dcrRequest, Hashtable<String, String> paramTable, 
			int addRequestOffset, int updateRequestOffset, boolean isAddRequest) 
			throws TBitsException {
		int slideOffset = 0;
		//If the request is being added for the first time, then revision is assumed to be 'R0'
		String curRevision = dcrRequest.get(REVISION).trim();
		if(isAddRequest){
			if (curRevision.equals("R0"))
				slideOffset = addRequestOffset;
			else
				slideOffset = updateRequestOffset;
		}		
		else if (curRevision.equals("R0")){			
			slideOffset = addRequestOffset;
		}
		else  if (curRevision.equals("R1"))
			slideOffset = updateRequestOffset;
		return slideOffset;
	}
	
	
	public static int getExFieldSlideOffsetBasedOnRevision(Connection connection, int currentBASystemId,
			Request dcrRequest, Hashtable<String, String> paramTable, 
			int addRequestOffset, int updateRequestOffset, boolean isAddRequest) throws TBitsException {
					
		int slideOffset = 0;
		//If the request is being added for the first time, then revision is assumed to be 'R0'
		String curRevision = dcrRequest.get(REVISION).trim();
		if(isAddRequest)
			slideOffset = addRequestOffset;
		else if (curRevision.equals("R0")){			
			slideOffset = addRequestOffset;
		}
		else{
			String curRequestIdStr = paramTable.get(Field.REQUEST);
			Request prevRequest = null;
			String prevRevision = "";
			if ((curRequestIdStr != null) && (!curRequestIdStr.trim().equals(NCCTransmittalUtils.EMPTY_STRING))){
				try {
					if (currentBASystemId == dcrRequest.getSystemId()){
						Field revField = Field.lookupBySystemIdAndFieldName(currentBASystemId, REVISION);
						ActionEx prevActionEx = ActionEx.lookupBySystemIdRequestIdActionIdFieldId(dcrRequest.getSystemId(),
													dcrRequest.getRequestId(), (dcrRequest.getMaxActionId() - 1), 
													revField.getFieldId());
						int typeId = prevActionEx.getTypeValue();
						Type revType = Type.lookupBySystemIdAndFieldNameAndTypeId(currentBASystemId, REVISION, typeId);
						prevRevision = revType.getName();
					}
					else{
						prevRequest = Request.lookupBySystemIdAndRequestId(connection, currentBASystemId, Integer.parseInt(curRequestIdStr));
						prevRevision = prevRequest.get(REVISION);	
					}
					
				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw new TBitsException("Number format exception occurred.");
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Database exception occurred.");
				}
				if ((prevRevision != null) && !prevRevision.equals("")){
					if (!prevRevision.trim().equals(curRevision))
						slideOffset = updateRequestOffset;							
				}
			}
		}
		return slideOffset;
	}
	
	public static String getUserLoginFromAutoCompleteString(String userInfo) throws ArrayIndexOutOfBoundsException{
		String userLogin = "";
		int bIndex = userInfo.indexOf("<");
		int eIndex = userInfo.indexOf(">");
		if (bIndex > -1){	
			userLogin = userInfo.substring(0, bIndex);			
		}
		else if (eIndex > -1)
			userLogin = userInfo.substring(0, eIndex);
		else 
			return userInfo;
		return userLogin;
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
		
		 /*Connection connection = null;
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
		}*/
		//System.out.println("%%%%%%%%%%%%%%%%%%%5" + getSlidedDueDate(true, 7).toDateMin());
		String s1 = "lokesh<Lokesh S>, ritesh";
		String s2 = "Sandeepuday@test.com,Lokesh S<lokesh>, ritesh";
		for (String s : s1.split(",")){
			System.out.println("S1: " + getUserLoginFromAutoCompleteString(s));
		}

	}

	/*
	 * Takes comma separated string and a compare string. Checks if the compare string 
	 * exists in the comma separated string.
	 */
	public static boolean isExistsInString(String parentString, String childString){
		String[] strArray = parentString.split(",");
		for (String str : strArray){
			if (str.trim().equals(childString.trim()))
				return true;
			else continue;
		}
		return false;
	}

	public static final String STATUS_PENDING_SUBMISSION = "PendingSubmission";

	public static boolean isSetDCPLDueDate(String statusName) {
		
		if (statusName.equals(STATUS_A4) || statusName.equals(STATUS_A5) || statusName.equals(STATUS_A5))
			return true;
		return false;
		
	}
	
	//Transmittal type bit field names
	public static final String TRANSMIT_FOR_AS_BUILT = "TransmitAsBuilt";

	
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
	
	public static final String DECISION_FROM_IPLE = "DecisionFromIPLE";

	public static final String DTN_TO_IPLE_SITE = "DTNToIPLESITE";

	public static final String STATUS_UNDER_REVIEW = "UnderReview";

	public static final String DOCUMENT_TYPE = "DocumentType";

	public static final String STRING_NONE = "None";

	public static final String FIELD_REVISION = "Revision";

	public static final String DTN_FROM_SEPCO_EXT = "DTNFromSEPCOExt";

	public static final String PENDING_SUBMISSION = "PendingReceipt";

	public static final String FLOW_STATUS_TO_DCPL = "FlowStatusToDCPL";

	public static final String STATUS_RETURNED_WITH_DECISION = "ReturnedWithDecision";

	public static final String DECISION_TO_IPLE = "DecisionToIPLE";

	public static final String DTN_TO_SEPCO_EXT = "DTNToSEPCOExt";

	public static final String DTN_FROM_IPLE_EXT = "DTNFromIPLEExt";

	public static final String SEPCO_DRAWING_NO = "SEPCODrawingNo";

	public static final String FLOW_STATUS_TO_TPSC = "FlowStatusToTPSC";

	public static final String DECISION_FROM_DCPL = "DecisionFromDCPL";

	public static final String DCPL_RESPONSE_DATE = "DCPLResponseDate";

	public static final String RETURNED_WITH_DECISION = "ReturnedWithDecision";

	public static final String DTN_FROM_DCPL = "DTNFromDCPL";

	public static final String RELEASE_FOR_CONSTRUCTION = "ReleaseForConstruction";

	public static final String TRANSMIT_TO_IPLE = "TransmitToIPLE";

	public static final String IPLE = "IPLE";

	public static final String SECONDARY_REVIEWER = "SecondaryReviewer";

	public static final String MAPPED_BUSINESS_AREAS = "mappedBusinessAreas";

	static final String DRAWING_NO = "DrawingNo";

	public static final int NCC_SYSTEM_ID = 4;

	public static final int CSEPDI_SYSTEM_ID = 8;

	public static final int DCPL_SYSTEM_ID = 5;

	public static final int DESEIN_SYSTEM_ID = 2;

	public static final int VENDOR_SYSTEM_ID = 9;

	public static final int STUP_SYSTEM_ID = 10;
	
	public static final int EDTD_SYSTEM_ID = 7;

	protected static final int NCC_TO_DCPL_DROPDOWN_ID = 10;

	protected static final int NCC_TO_EDTD_DROPDOWN_ID = 2;

	protected static final int DESEIN_TO_NCC_DROPDOWN_ID = 3;

	protected static final int DCPL_TO_NCC_DROPDOWN_ID = 9;

	protected static final String TRN_PROCESS_ID = "trn_process_id";

	public static final int NCC_TO_VENDOR1_DROPDOWN_ID = 13;

	static final int EDTD_TO_NCC_DROPDOWN_ID = 6;

	static final int VENDOR1_TO_NCC_DROPDOWN_ID = 12;

	static final String BR_B_OR_B_BR = "\n\nOR\n\n";

	static final String ALL_DRAWINGS_SAME_CATEGORY_MSG = "Not all drawings/documents selected for transmittal belong to the same,\n " +
			"'Area'," +  "'Discipline' and 'Engineering Type'.\n";

	public static String showError( String errorMsg )
	 {
		 String html = "<script type='text/javascript'> \n" +		 		
		 		" function prefillException() \n" +
		 		" { \n" +
		 		"   // alert( 'prefillException called' ) ; \n" +
		 		"	showAutomaticRestrictions( \" " + errorMsg + " \" ) ;\n" +
		 		" } \n" +
		 		" YAHOO.util.Event.addListener( window, 'load', prefillException ) ; \n" +
		 		" </script> \n"	; 
		 
		 return html ;		 
	 }

	public static final String EQUIPMENT_CODE = "EquipmentUnitCode";

	public static final String COMPONENT_CODE = "ComponentCode";

	public static final String SYSTEM_CODE = "SystemCode";

	public static final String DOCUMENT_CODE = "DocumentCode";

	/**
	 * @param connection
	 * @param aSystemId
	 * @param maxIdName
	 * @return
	 * @throws DatabaseException
	 */
	public static int getMaxIdByName(Connection connection, int aSystemId,
			String maxIdName) throws DatabaseException {
		int maxTransmittalNumber = -1;//System.out.println("Before stp_getAndIncr : " + transmittalProcessName);
		try{
			CallableStatement cs = connection.prepareCall("stp_getAndIncrMaxId ?");//"stp_transmittal_getMaxTransmittalId ?");//
			cs.setString(1, maxIdName);
			ResultSet rs = cs.executeQuery();
			if ((rs != null) && (rs.next())){
				maxTransmittalNumber = rs.getInt("max_id");
				//System.out.println("MaxId: " + maxTransmittalNumber);
				return maxTransmittalNumber;
			}else {
				throw new SQLException();
			}
		}catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving max transmittal number for sysId: " + aSystemId, e);
		}
	}
	
	public static JSONObject getAttachmentObject(int aSystemId, Request request, ArrayList<String> attList, 
			String revStringName){
		JSONObject attObj = new JSONObject();
		attObj.put("attDnameList", attList.get(0));
		attObj.put("attNameList", attList.get(1));
		Type revType = (Type)request.getObject(revStringName);
		if (revType != null)
			attObj.put("revisionNumber", revType.getDisplayName());
		else
			attObj.put("revisionNumber", "");
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
		String lastUpdatedDate = sdf.format(request.getLastUpdatedDate());
		attObj.put("dateTime", lastUpdatedDate);
		return attObj;
	}
	
	public static JSONArray getAttachmentList (int aSystemId, Request request, String revFieldName){

		JSONArray attArray = new JSONArray();
		ArrayList<String> attList = null;
		Collection<AttachmentInfo> reqAttachments = request.getAttachments();
		attList = getReqAttachmentNameList(reqAttachments);
		JSONObject attObj = getAttachmentObject (aSystemId, request, attList, revFieldName);
		attArray.add(attObj);
		return attArray;
	}

	public static JSONArray getAttachmentList (int aSystemId, Request request, int fieldId, String revFieldName){
		JSONArray attArray = new JSONArray();
		ArrayList<String> attList = null;
		try {
			Field extAttachmentField = Field.lookupBySystemIdAndFieldId(aSystemId, fieldId);
			String deliverableAttString = request.get(extAttachmentField.getName());
			if ((deliverableAttString == null) || deliverableAttString.trim().equals(""))
				deliverableAttString = "[]";
			Collection<AttachmentInfo> reqAttachments = AttachmentInfo.fromJson(deliverableAttString);
			attList = getReqAttachmentNameList(reqAttachments);
			//Action action = Action.lookupBySystemIdAndRequestIdAndActionId(aSystemId, aRequestId, req.getMaxActionId());
			JSONObject attObj = getAttachmentObject (aSystemId, request, attList, revFieldName);
			attArray.add(attObj);		
			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return attArray;
	}
	
	public static ArrayList<String> getReqAttachmentNameList(Collection<AttachmentInfo> attachments){
		String attDnameList = "";	
		String attNameList = "";
		ArrayList<String> attList= new ArrayList<String>(2);
		for (AttachmentInfo ai : attachments) {					
			if (attDnameList == ""){
				attDnameList = ai.name;
				attNameList = ai.name + "<br1>" + ai.repoFileId + "<br1>" + ai.size;
			}
			else{
				attDnameList = attDnameList.concat("<br2>" + ai.name);
				attNameList= attNameList.concat("<br2>" + ai.name + "<br1>" + ai.repoFileId + "<br1>" + ai.size);
			}
		}	
		attList.add(attDnameList);
		attList.add(attNameList);
		return attList;		
	}
	

	static final String NCC_NO_FIELD_NAME = "NccNo"; 
}


