package kskQlt;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class QLTIDCConstants {

    public static final String IDC_SYSPREFIX = "TRIP";
    public static final String IDC_FILES_RELEASED_FOR_IDC_FIELD_NAME = Field.ATTACHMENTS;
    public static final String IDC_IDC_COMMENTED_FILES = "LloydsTripReportFile" ;
    public static final String IDC_COMMENTS_COMPLETE="CommentsComplete";
    public static final String IDC_SOURCE_REQUESTS="related_requests";
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
