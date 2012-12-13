package kskidc;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class IDCConstants {

    public static final String IDC_SYSPREFIX = "IDC";
    public static final String CEC_SYSPREFIX = "CEC";
    public static final String DCPL_SYSPREFIX = "DCPL";
    public static final String PHO_SYSPREFIX="PHO";
    public static final String IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME = "FileForIDC";
    public static final String IDC_IDC_COMMENTED_FILES = Field.ATTACHMENTS ;
    public static final String IDC_COMMENTS_COMPLETE="CommentsComplete";
    public static final String IDC_STATUS=Field.STATUS;
    public static final String IDC_INITIATED="IDCIntitiated";
    public static final String IDC_COMPLETED="IDCCompleted";
    public static final String IDC_CANCELLED="IDCCancelled";
    public static final String IDC_INWARD_DTN="IncomingTransmittalNo";
    public static final String IDC_SOURCE_REQUESTS="SourceRequests";
    public static final String TBITS_ROOT="root";
    
    public static final String SEPCO_SYSPREFIX = "SEPCO";
    public static String SEPCO_ASBUILT_FIELD_NAME = "AsBuilt" ;
    public static String SEPCO_DOCUMENT_NUMBER= "SEPCODocumentNumber";
    public static String SEPCO_WPCL_NUMBER ="DrawingNumber";
    public static String SEPCO_REVISION = "Revision";
    public static String SEPCO_TYPE="type";
    public static String SEPCO_SUBMISSION_FILE ="SubmittedDeliverable";
    public static String SEPCO_OTHER_ATTACHMENTS=Field.ATTACHMENTS;
	
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
