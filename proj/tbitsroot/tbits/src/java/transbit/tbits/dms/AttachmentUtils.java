package transbit.tbits.dms;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.Attachment;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.ActionEx;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;

public class AttachmentUtils {
	public static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.dms");
	public static JSONArray getAttachmentList (int aSystemId, Request request, String revFieldName){
		
		JSONArray attArray = new JSONArray();
		ArrayList<String> attList = null;
		int aRequestId = request.getRequestId();
		try {
			Collection<AttachmentInfo> reqAttachments = request.getAttachments();
			attList = getReqAttachmentNameList(reqAttachments);
			Action action = Action.lookupBySystemIdAndRequestIdAndActionId(aSystemId, aRequestId, request.getMaxActionId());
			JSONObject attObj = getActionAttachmentObject (aSystemId, request, action, attList, revFieldName);
			attArray.add(attObj);
			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return attArray;
	}
	
	public static ArrayList<String> getActionAttachmentNameList(Collection<ActionFileInfo> attachments){
		String attDnameList = "";	
		String attNameList = "";
		ArrayList<String> attList= new ArrayList<String>(2);
		for (ActionFileInfo ai : attachments) {					
			if (attDnameList == ""){
				attDnameList = ai.getName();
				attNameList = ai.getName()+ "<br1>" + ai.getRequestFileId() + "<br1>" + ai.getSize();
			}
			else{
				attDnameList = attDnameList.concat("<br2>" + ai.getName());
				attNameList= attNameList.concat("<br2>" + ai.getName() + "<br1>" + ai.getRequestFileId()+ "<br1>" + ai.getSize());
			}
		}	
		attList.add(attDnameList);
		attList.add(attNameList);
		return attList;		
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
	
	public static ArrayList<String> getAttachmentNameList(ArrayList<Attachment> actionAttachments){		
		String attDnameList = "";	
		String attNameList = "";
		ArrayList<String> attList= new ArrayList<String>(2);
		for (Attachment attachment : actionAttachments) {					
			if (attDnameList == ""){
				attDnameList = attachment.getDisplayName();
				attNameList = attachment.getName();
				}
			else{
				attDnameList = attDnameList.concat("," + attachment.getDisplayName());
				attNameList= attNameList.concat("," + attachment.getName());
			}
		}	
		attList.add(attDnameList);
		attList.add(attNameList);
		return attList;		
	}
	
	public static JSONObject getActionAttachmentObject(int aSystemId, Request request, Action action, ArrayList<String> attList, String revStringName){
		JSONObject attObj = new JSONObject();
		try {			
			attObj.put("attDnameList", attList.get(0));
			attObj.put("attNameList", attList.get(1));
				
			Field field = Field.lookupBySystemIdAndFieldName(aSystemId, revStringName);
			ActionEx actionEx = ActionEx.lookupBySystemIdRequestIdActionIdFieldId (aSystemId, request.getRequestId(), 
					action.getActionId(), field.getFieldId());
			Type revType = Type.lookupBySystemIdAndFieldIdAndTypeId(aSystemId, field.getFieldId(), actionEx.getTypeValue());			
			attObj.put("dateTime", action.getLastUpdatedDate().toDateMin());
			attObj.put("actionId", action.getActionId());
			attObj.put("revisionNumber", revType.getDisplayName());
		}
		catch (DatabaseException e1) {
			e1.printStackTrace();
		}
		return attObj;
	}
	
	public static JSONArray getAttachmentList (int aSystemId, Request request, int fieldId, String revFieldName){
		JSONArray attArray = new JSONArray();
		ArrayList<String> attList = null;
		int aRequestId = request.getRequestId();
		try {
			Request req = Request.lookupBySystemIdAndRequestId(aSystemId, aRequestId);
			Field extAttachmentField = Field.lookupBySystemIdAndFieldId(aSystemId, fieldId);
			String deliverableAttString = request.get(extAttachmentField.getName());
			if ((deliverableAttString == null) || deliverableAttString.trim().equals(""))
				deliverableAttString = "[]";
			Collection<AttachmentInfo> reqAttachments = AttachmentInfo.fromJson(deliverableAttString);
			attList = getReqAttachmentNameList(reqAttachments);
			Action action = Action.lookupBySystemIdAndRequestIdAndActionId(aSystemId, aRequestId, req.getMaxActionId());
			JSONObject attObj = getActionAttachmentObject (aSystemId, request, action, attList, revFieldName);
			attArray.add(attObj);		
			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return attArray;
	}

//	public static String getAttachmentEx(int aSystemId, int aRequestId, 
//				int fieldId) throws DatabaseException {
//		Request req;
//		req = Request.lookupBySystemIdAndRequestId(aSystemId, aRequestId);
//		Field extAttachmentField = Field.lookupBySystemIdAndFieldId(aSystemId, fieldId);
//		Hashtable<Field, RequestEx> extendedFields = req.getExtendedFields();
//		RequestEx asBuiltAttReqEx = extendedFields.get(extAttachmentField);
//		String extAttachments = asBuiltAttReqEx.getTextValue();
//		return extAttachments;
//	}
	
//	public static String getAttachmentEx(Connection connection, int aSystemId, int aRequestId, 
//			int fieldId) throws DatabaseException {
//	Request req;
//	req = Request.lookupBySystemIdAndRequestId(connection, aSystemId, aRequestId);
//	Field extAttachmentField = lookupBySystemIdAndFieldId(connection, aSystemId, fieldId);
//	Hashtable<Field, RequestEx> extendedFields = req.getExtendedFields();
//	RequestEx asBuiltAttReqEx = extendedFields.get(extAttachmentField);
//	String extAttachments = asBuiltAttReqEx.getTextValue();
//	return extAttachments;
//}
	
//	public static String getAttachmentEx(Request aRequest, 
//			int fieldId) throws DatabaseException {
//		RequestEx deliverableAttReqEx = null;
//		String extAttachments = "[]";
//		int aSystemId = aRequest.getSystemId();
//		Field extAttachmentField = Field.lookupBySystemIdAndFieldId(aSystemId, fieldId);
//		Hashtable<Field, RequestEx> extendedFields = aRequest.getExtendedFields();
//		if (extAttachmentField != null)
//			deliverableAttReqEx = extendedFields.get(extAttachmentField);
//		if(deliverableAttReqEx != null)
//			extAttachments = deliverableAttReqEx.getTextValue();
//		return extAttachments;
//	}
	
	public static String getAttachmentEx(Connection connection, Request aRequest, 
			int fieldId) throws DatabaseException {
		RequestEx deliverableAttReqEx = null;
		String extAttachments = "[]";
		int aSystemId = aRequest.getSystemId();
		Field extAttachmentField = lookupBySystemIdAndFieldId(connection, aSystemId, fieldId);
		/*Hashtable<Field, RequestEx> extendedFields = aRequest.getExtendedFields();
		if (extAttachmentField != null)
			deliverableAttReqEx = extendedFields.get(extAttachmentField);
		if(deliverableAttReqEx != null)
			extAttachments = deliverableAttReqEx.getTextValue();*/
		extAttachments = aRequest.get(extAttachmentField.getName());
		return extAttachments;
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

//	public static void main(String[] args) throws DatabaseException{
//		//getAttachmentList(15, Request.lookupBySystemIdAndRequestId(15, 1), "Revision");
//		Request req = null;
//		req = Request.lookupBySystemIdAndRequestId(17, 3);
//		Collection<AttachmentInfo> attachments = req.getAttachments();
//		System.out.println("Request Attachments:");
//		for (AttachmentInfo ai : attachments)
//			System.out.println("\t" + ai.name);
//		/*Hashtable<Integer, Collection<ActionFileInfo>> allActionFiles = Action.getAllActionFiles(15, 213);
//		System.out.println("Action Attachments:");
//		for (Integer aId : allActionFiles.keySet()){
//			System.out.println("\taID: " + aId);
//			Collection<ActionFileInfo> actAtt = allActionFiles.get(aId);
//			for (ActionFileInfo afi : actAtt)
//				System.out.println("\t\tafi: " + afi.getName());
//		}			*/
//		System.out.println("RFC Attachments: " + getAttachmentList(17, req, 64, "Revision"));
//	}	
}
