package nccIDC;

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
    public static final String IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME = "FileForIDC";
    public static final String IDC_IDC_COMMENTED_FILES = Field.ATTACHMENTS ;
    public static final String IDC_COMMENTS_COMPLETE="CommentsComplete";
    public static final String IDC_STATUS=Field.STATUS;
    public static final String IDC_INITIATED="IDCIntitiated";
    public static final String IDC_COMPLETED="IDCCompleted";
    public static final String IDC_CANCELLED="IDCCancelled";
    public static final String IDC_INWARD_DTN="IncomingTransmittalNo";
    public static final String IDC_LINKED_REQUESTS="related_requests";
    public static final String TBITS_ROOT="root";
    
    public static final String CSEPDI_SYSPREFIX = "KNPL_CSEPDI";
    public static final String CSEPDI_SUBMISSION_FILE = "CSEPDISubmissionFile";
    
    public static final String DCPL_SYSPREFIX = "KNPL_DCPL";
    public static final String DCPL_SOURCE_SUBMISSION_FILE = "SourceSubmissionFile";
    
    public static final String DESEIN_SYSPREFIX="KNPL_DESEIN";
    public static final String DESEIN_NCC_SUBMISSION_FILE="NCCSubmissionFile";
    
    public static final String EDTD_SYSPREFIX="KNPL_EDTD";
    public static final String EDTD_SUBMISSION_FILE="EDTDSubmissionFile";
    
    public static final String KVK_SYSPREFIX="KNPL_KVK";
    public static final String KVK_SUBMISSION_FILE="NCCSubmissionFile";
    
    public static final String NCC_SYSPREFIX="KNPL_NCC";
    public static final String NCC_SUBMISSION_FILE="NCCSubmissionFile";
    
    
    public static final String SITE_SYSPREFIX="KNPL_SITE";
    public static final String SITE_SUBMISSION_FILE="NCCSubmissionFile";
    
    public static final String STUP_SYSPREFIX="KNPL_STUP";
    public static final String STUP_SUBMISSION_FILE="STUPSubmissionFile";
    
    public static final String VENDOR1_SYSPREFIX="KNPL_VENDOR1";
    public static final String VENDOR1_SUBMISSION_FILE="Vendor1SubmissionFile";
    
    public static String TABLE_DRAWING_NUMBER="DrawingNo";
    public static String TABLE_ASBUILT_FIELD_NAME = "AsBuilt" ;
    public static String TABLE_REVISION= "Revision";
    public static String TABLE_DOCUMENT_TYPE="DocumentType";
    public static String TABLE_UNIT="Unit";
    public static String TABLE_ENGINEERING_TYPE="EngineeringType";
    public static String TABLE_ACTUAL_PERCENT_COMPLETE="ActualPercComplete";
    public static String TABLE_WEIGHTAGE="Weightage";
    public static String TABLE_AREA="Area";
    
    
    
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
