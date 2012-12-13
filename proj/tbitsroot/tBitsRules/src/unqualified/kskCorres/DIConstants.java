package kskCorres;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;

public class DIConstants 
{
	public static String DI_SYSPREFIX = "DI" ;
	
	public static String DI_COMM_PROT_FIELD_NAME = Field.CATEGORY ;
	public static String DI_CORR_STATUS_FIELD_NAME = Field.REQUEST_TYPE ;
	public static String DI_CORR_FILE_FIELD_NAME = "CorrespondanceFile" ;
	public static String DI_CORR_NUM_FIELD_NAME = "CorrespondanceNumber" ;
	public static String DI_CORR_TYPE_FIELD_NAME = Field.SEVERITY ;
	public static String DI_CORR_SENT_BY_FIELD_NAME = Field.STATUS ;
	public static String DI_DRAFT_REPLY_FIELD_NAME = Field.SUMMARY ;
	public static String DI_RELATED_REQUEST_FIELD_NAME = Field.RELATED_REQUESTS ;
	public static String DI_LINKED_REQUEST = Field.RELATED_REQUESTS ;	 
	public static String DI_SUBJECT_FIELD_NAME = Field.SUBJECT ;
	public static String DI_OTHER_FILE_FIELD_NAME = Field.ATTACHMENTS ;
	
	public static String DI_SEND_BUTTON_NAME = "Send Reply" ;
	public static String CORR_LINK_NAME = "Discuss-Internally" ;
	public static String DI_LINK_NAME = "Send-Reply" ;
	
	public static String CORR_SYS_ID = "corrSysId" ;
	public static String CORR_REQUEST_ID = "corrRequestId" ;
	
	public static String DI_SYS_ID="diSysId" ;
	public static String DI_REQUEST_ID = "diRequestId" ;
	
	public static String DI_CORR_STATUS_OPEN = "Open" ;
	
	 public static JsonObject getAttachmentJson(BusinessArea businessArea,Hashtable<String, Integer> permissions, Hashtable<Field,Collection<AttachmentInfo>> attachTable)
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
	                       Collection<AttachmentInfo> attInfo = attachTable.get(f) ;                           
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
