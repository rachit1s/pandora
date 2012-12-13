package idc.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.exception.TBitsException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public class IDCUtils {
	public static String PGBR="<br><br><br><br>";
	public static String IDC_SYSPREFIX="IDC";
	public static final String CEC_SYSPREFIX = "CEC";
	public static final String DCPL_SYSPREFIX = "DCPL";
	public static final String PHO_SYSPREFIX="PHO";
	public static final String IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME = "FileForIDC";
	public static final String IDC_IDC_COMMENTED_FILES = "attachments" ;
	public static final String IDC_COMMENTS_COMPLETE="CommentsComplete";
	public static final String IDC_STATUS="status_id";
	public static final String IDC_INITIATED="IDCIntitiated";
	public static final String IDC_COMPLETED="IDCCompleted";
	public static final String IDC_CANCELLED="IDCCancelled";
	public static final String IDC_INWARD_DTN="IncomingTransmittalNo";
	public static final String IDC_SOURCE_REQUESTS="SourceRequests";
	public static final String TBITS_ROOT="root";

	public static final String PFIELD_LNT_NO="DrawingNo";
	public static final String PFIELD_VENDOR_NO="VendorNo";
	public static final String PFILED_TITLE="subject";
	public static final String PFIELD_REVISION="VendorRevision";
	public static final String PFIELD_VENDOR_SUBMISSION_FILE="VendorSubmissionFile";

	public static String Business_Area	= "sys_id";
	public static String Field_Request	= "request_id";
	public static String Package	= "category_id";
	public static String Flow_Status_with_L_T	= "status_id";
	public static String Decision_From_L_T	= "severity_id";
	public static String Document_Code	= "request_type_id";
	public static String Logger_Created_By_	= "logger_ids";
	public static String Assignee_Assigned_To_	= "assignee_ids";
	public static String Subscribers_Copy_To_	= "subscriber_ids";
	public static String To	= "to_ids";
	public static String Cc	= "cc_ids";
	public static String Title	= "subject";
	public static String Description	= "description";
	public static String Confidential	= "is_private";
	public static String Parent	= "parent_request_id";
	public static String Last_Update_By_	= "user_id";
	public static String __U	= "max_action_id";
	public static String Target_Date	= "due_datetime";
	public static String Submitted_Date	= "logged_datetime";
	public static String Last_Updated	= "lastupdated_datetime";
	public static String Header_Description	= "header_description";
	public static String Attachments	= "attachments";
	public static String Summary	= "summary";
	public static String Memo	= "memo";
	public static String append_interface	= "append_interface";
	public static String Notify	= "notify";
	public static String Notify_Logger	= "notify_loggers";
	public static String replied_to_action	= "replied_to_action";
	public static String Linked_Requests	= "related_requests";
	public static String Office	= "office_id";
	public static String Send_SMS	= "SendSMS";
	public static String Revision	= "Revision";
	public static String Owner_No	= "OwnerNo";
	public static String Engineering_Type	= "EngineeringType";
	public static String Vendor_Weightage	= "VendorWeightage";
	public static String Vendor_Actual___Complete	= "VendorActualComplete";
	public static String Document_Type	= "DocumentType";
	public static String As_Built_File	= "AsBuilt";
	public static String RFC_File	= "RFC";
	public static String LnT_Decision_File	= "LnTDecisionFileToVendor";
	public static String Transmit_To_L_T	= "TransmitToContractor";
	public static String L_T_Response_Date	= "ContractorResponseDate";
	public static String DTN_To_Contractor_CC	= "DTNToContractor";
	public static String DTN_From_L_T_SnL	= "DTNFromContractor";
	public static String Generation_Agency	= "GenerationAgency";
	public static String Discipline	= "Discipline";
	public static String DeliverableType	= "DeliverableType";
	public static String Vendor_No	= "VendorNo";
	public static String Vendor_Submission_File	= "VendorSubmissionFile";
	public static String L_T_No	= "DrawingNo";
	public static String Submission_File_Type	= "SubmissionFileType";
	public static String Deliverable_Category	= "DelCategory";
	public static String Sub_Area_Code	= "SubAreaCode";
	public static String Unit_Code	= "UnitCode";
	public static String Type_of_Power_Plant	= "TypeOfPowerPlant";
	public static String DTNConfigType	= "DTNConfigType";
	public static String DTNConfigQty	= "DTNConfigQty";
	public static String DTNConfigRemark	= "DTNConfigRemark";
	public static String Db_Table_Name ="plugins_idc_ba_map";

	public static AttachmentInfoClient toAttachmentInfoClient(AttachmentInfo att){
		if(att!=null){
			AttachmentInfoClient attc = new AttachmentInfoClient();
		//	 attc = new AttachmentInfoClient(att.getRepoFileId(),att.getName(),att.getSize());	
			 attc.setRepoFileId(att.getRepoFileId());
			 attc.setFileName(att.getName());
			 attc.setSize(att.getSize());			 
			 return attc;
		}		
		return null;

	}

	public static List<FileClient> toAttList(List<AttachmentInfo> attList){
		List<FileClient>attcList=new ArrayList<FileClient>();
		if(attList!=null){
			for(AttachmentInfo att:attList)
			{
				FileClient fc = new FileClient(toAttachmentInfoClient(att));
				attcList.add(fc);
			}
			return attcList;
		}
		return null;
	}

	public static String getTargetBa(String srcBa) throws TBitsException{

		Connection aCon = null;
		try {
			aCon=DataSourcePool.getConnection();
			PreparedStatement pstmt = aCon.prepareStatement("select target_ba from "+Db_Table_Name+" where src_ba =?" );
			pstmt.setString(1,srcBa);
			ResultSet rs = pstmt.executeQuery();
			if(rs==null){
				throw new TBitsException("Target BA Not Found");			
			}
			while(rs!=null && rs.next()!=false){
				return rs.getString("target_ba");
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		finally{
			try {
				aCon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static ArrayList<String> getAllTargetBAs() throws TBitsException, TbitsExceptionClient{
		ArrayList<String> validBAs=new ArrayList<String>();
		Connection aCon = null;
		try {
			aCon=DataSourcePool.getConnection();
			PreparedStatement pstmt = aCon.prepareStatement("select target_ba from "+Db_Table_Name);
			ResultSet rs = pstmt.executeQuery();
			if(rs==null){
				throw new TbitsExceptionClient("Target BA Not Found");			
			}
			while(rs!=null && rs.next()!=false){
				String ba=rs.getString("target_ba");
				validBAs.add(ba);
			}
			return validBAs;

		} catch (SQLException e) {
			e.printStackTrace();

		}
		finally{
			try {
				aCon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static ArrayList<String> getAllSrcBAs() throws TBitsException, TbitsExceptionClient{
		ArrayList<String> validBAs=new ArrayList<String>();
		Connection aCon = null;
		try {
			aCon=DataSourcePool.getConnection();
			PreparedStatement pstmt = aCon.prepareStatement("select src_ba from "+Db_Table_Name);
			ResultSet rs = pstmt.executeQuery();
			if(rs==null){
				throw new TbitsExceptionClient("Target BA Not Found");			
			}
			while(rs!=null && rs.next()!=false){
				String ba=rs.getString("src_ba");
				validBAs.add(ba);
			}
			return validBAs;

		} catch (SQLException e) {
			e.printStackTrace();

		}
		finally{
			try {
				aCon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static JsonObject getAttachmentJson(BusinessArea businessArea,Hashtable<String, Integer> permissions, Hashtable<Field,ArrayList<AttachmentInfo>> attachTable)
	{
		JsonObject rootNode = new JsonObject();
		JsonParser jsonParser = new JsonParser();
		for(Enumeration<Field> fields = attachTable.keys() ; fields.hasMoreElements() ;)
		{
			Field f = fields.nextElement() ;
			Integer fieldPermInteger = permissions.get(f.getName());
			int fieldPerm = 0;
			if(fieldPermInteger != null)
				fieldPerm = fieldPermInteger.intValue();

			if( (fieldPerm & Permission.VIEW) != 0)
			{
				Boolean canChange = ( (fieldPerm & Permission.CHANGE) != 0);
				Boolean canAdd = ( (fieldPerm & Permission.ADD) != 0);
				JsonObject fieldNode = new JsonObject();
				fieldNode.addProperty("fieldDisplayName", f.getDisplayName());
				fieldNode.addProperty("canChange", canChange);
				fieldNode.addProperty("canAdd", canAdd);
				fieldNode.addProperty("displayOder", f.getDisplayOrder());
				fieldNode.addProperty("fieldId", f.getFieldId());
				fieldNode.addProperty("numberOfFiles", 0);
				String filesStr = null;

				if (attachTable != null) 
				{
					ArrayList<AttachmentInfo> attInfo = attachTable.get(f) ;                           
					filesStr = AttachmentInfo.toJson(attInfo);                     
				}

				if( (filesStr == null) || (filesStr.trim().length() == 0))
					filesStr = "[]";

				fieldNode.add("files", jsonParser.parse(filesStr));
				rootNode.add(f.getName(), fieldNode);
			}
		}
		return rootNode;
	}


}
