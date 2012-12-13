package kskorg;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ORGConstants {

public static final String CV_sysprefix="CV";
public static final String CV_Business_Area	= "sys_id";
public static final String CV_Request	= "request_id";
public static final String CV_Impact_Level	= "category_id";
public static final String CV_Department	= "status_id";
public static final String CV_Current_Status	= "severity_id";
public static final String CV_Location	= "request_type_id";
public static final String CV_Logger	= "logger_ids";
public static final String CV_Assignee	= "assignee_ids";
public static final String CV_Subscribers	= "subscriber_ids";
public static final String CV_To	= "to_ids";
public static final String CV_Cc	= "cc_ids";
public static final String CV_Subject	= "subject";
public static final String CV_Comments	= "description";
public static final String CV_Private	= "is_private";
public static final String CV_Parent	= "parent_request_id";
public static final String CV_Last_Update_By_	= "user_id";
public static final String CV___U	= "max_action_id";
public static final String CV_Due_Date	= "due_datetime";
public static final String CV_Submitted_Date	= "logged_datetime";
public static final String CV_Last_Updated	= "lastupdated_datetime";
public static final String CV_Header_Description	= "header_description";
public static final String CV_CV	= "attachments";
public static final String CV_Summary	= "summary";
public static final String CV_Memo	= "memo";
public static final String CV_append_interface	= "append_interface";
public static final String CV_Notify	= "notify";
public static final String CV_Notify_Logger	= "notify_loggers";
public static final String CV_replied_to_action	= "replied_to_action";
public static final String CV_Linked_Requests	= "related_requests";
public static final String CV_Office	= "office_id";
public static final String CV_Send_SMS	= "SendSMS";
public static final String CV_Name	= "name";
public static final String CV_DOB	= "dob";
public static final String CV_Gender	= "gender";
public static final String CV_Designation	= "designation";
public static final String CV_TI1_Date	= "ti1date";
public static final String CV_TI2_Date	= "ti2date";
public static final String CV_TI3_Date	= "ti3date";
public static final String CV_PI1_Date	= "pi1date";
public static final String CV_PI2_Date	= "pi2date";
public static final String CV_PI3_Date	= "pi3date";
public static final String CV_Reasons_for_Hold	= "reasonsforhold";
public static final String CV_Sent_For_ShortListing="SentForShortListing";

public static final String ID_sysprefix="id";
public static final String ID_Business_Area	= "sys_id";
public static final String ID_Request	= "request_id";
public static final String ID_Location	= "category_id";
public static final String ID_Current_Status	= "status_id";
public static final String ID_Severity	= "severity_id";
public static final String ID_Department	= "request_type_id";
public static final String ID_Logger	= "logger_ids";
public static final String ID_Assignee	= "assignee_ids";
public static final String ID_Subscribers	= "subscriber_ids";
public static final String ID_To	= "to_ids";
public static final String ID_Cc	= "cc_ids";
public static final String ID_Subject	= "subject";
public static final String ID_Comments	= "description";
public static final String ID_Private	= "is_private";
public static final String ID_Parent	= "parent_request_id";
public static final String ID_Last_Update_By_	= "user_id";
public static final String ID___U	= "max_action_id";
public static final String ID_Due_Date	= "due_datetime";
public static final String ID_Submitted_Date	= "logged_datetime";
public static final String ID_Last_Updated	= "lastupdated_datetime";
public static final String ID_Header_Description	= "header_description";
public static final String ID_CV	= "attachments";
public static final String ID_Summary	= "summary";
public static final String ID_Memo	= "memo";
public static final String ID_append_interface	= "append_interface";
public static final String ID_Notify	= "notify";
public static final String ID_Notify_Logger	= "notify_loggers";
public static final String ID_replied_to_action	= "replied_to_action";
public static final String ID_Linked_Requests	= "related_requests";
public static final String ID_Office	= "office_id";
public static final String ID_Send_SMS	= "SendSMS";
public static final String ID_Name	= "name";
public static final String ID_DOB	= "dob";
public static final String ID_Gender	= "gender";
public static final String ID_Designation	= "designation";
public static final String ID_TI1_Date	= "ti1date";
public static final String ID_TI2_Date	= "ti2date";
public static final String ID_TI3_Date	= "ti3date";
public static final String ID_PI1_Date	= "pi1date";
public static final String ID_PI2_Date	= "pi2date";
public static final String ID_PI3_Date	= "pi3date";
public static final String ID_Reasons_for_Hold	= "reasonsforhold";
public static final String ID_Source_Requests="SourceRequests";


public static final String TBITS_ROOT="root";


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
