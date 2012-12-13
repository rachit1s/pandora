package transbit.tbits.TVN;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.activation.MimetypesFileTypeMap;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.ActionEx;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

import com.tbitsglobal.tvncore.TvnType;

/**
 * All the user defined constants and properties are to be put in this class.
 * The user prefix must be chosen by the user. It will default to "user" if not chosen.
 * 
 * @author Karan Gupta
 *
 */

public class UserDefinedData implements com.tbitsglobal.tvncore.TvnConstants{
	
	//====================================================================================

	private static String USER_PREFIX = "tbits:";
//	private static String TEMP_PATH = Configuration.findAbsolutePath(PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR));
//	private static String ATTACHMENT_DIR = APIUtil.getAtt
	
	//----------------------------------------------------- File Properties
	public static final String SYS_PREFIX = "sys_prefix";
	public static final String READ_ONLY = "isreadonly";
	public static final String IS_HIDDEN = "ishidden";
	public static final String DPCOUNT = "deadprop-count";
	public static final String LOGGERS = getUserPrefix() + "loggers";
    public static final String ASSIGNEES = getUserPrefix() + "assignee";
    public static final String SUBSCRIBERS = getUserPrefix() + "subscribers";
	public static final String SEVERITY = getUserPrefix() + "severity";
	public static final String DUEDATE = getUserPrefix() + "dueDate";
	//---------------------------------------------------------------------
	
	//====================================================================================

	public static String getUserPrefix(){
		
		if(USER_PREFIX == null)
			return "user:";
		else{
			if(USER_PREFIX.endsWith(":"))
				return USER_PREFIX;
			else
				return USER_PREFIX + ":";
		}
	}

	//====================================================================================

	public static String getTempFolderPath() {
		
		return APIUtil.getTMPDir();
	}

	//====================================================================================

	public static String getAttachmentFolderPath() {
		
		return APIUtil.getAttachmentLocation();
	}

	//====================================================================================
	
	public static Hashtable<String, String> getProps(ArrayList<TvnType> structure) {
		
		TvnType propsRequiredFor = structure.get(structure.size()-1);
		Hashtable<String, String> props = new Hashtable<String, String>();
		BusinessArea myBusinessArea = null;
		Action action = null;
		Request request = null;
		
		try{
			String path = "";
			for(int i = 0; i < structure.size(); i++){
				path += "/";
				path += structure.get(i).identifierValue;
				if(structure.get(i).identifier.equals(Field.BUSINESS_AREA)){
					myBusinessArea = (BusinessArea) structure.get(i).identifierObject;
				}
				else if(structure.get(i).identifier.equals(Field.REQUEST)){
					action = (Action) structure.get(i).identifierObject;
					request = Request.lookupBySystemIdAndRequestId(action.getSystemId(), action.getRequestId());
				}
			}
			
			// Put user defined property values here
			
			// Common properties
			String baselineRelativePath = WebdavUtil.reformPath(path).replaceFirst("/?" + myBusinessArea.getSystemPrefix() + "/?","");
			
			props.put(DISPLAY_NAME, propsRequiredFor.identifierValue);
			props.put(BLRP, baselineRelativePath);
			props.put(SYS_PREFIX, myBusinessArea.getSystemPrefix());
			props.put(READ_ONLY,"false");
			props.put(IS_HIDDEN,"false");
			props.put(DPCOUNT,"0");
			
			if(propsRequiredFor.identifier.equals(Field.BUSINESS_AREA)){
				// Properties for a BA
				
				String dirPath = getAttachmentFolderPath() + File.separatorChar + myBusinessArea.getSystemPrefix();
				File myFile = new File(dirPath);
				if(myFile.exists() == true ) {
					String lastModifiedDate = FORMAT.format(new Date(myFile.lastModified()));
					props.put(LAST_MODIFIED,lastModifiedDate);
				}
				String creationDate = FORMAT.format((Date)myBusinessArea.getDateCreated());
				
				props.put(CREATOR_DISPLAY_NAME,"");
				props.put(CREATION_DATE,creationDate);
			}
			else if(propsRequiredFor.identifier.equals(Field.REQUEST)){
				// Properties for a request
				
				String creationDate = FORMAT.format((Date)action.getLoggedDate());
				String lastModifiedDate = FORMAT.format((Date)action.getLastUpdatedDate());
				User author = User.lookupByUserId(request.getUserId());
				ArrayList<RequestUser> loggers = getArrayList(request.getLoggers());
				
				StringBuilder sbLoggers = new StringBuilder();
				for (RequestUser reqUser : loggers) {
					if(sbLoggers.length() != 0)
						sbLoggers.append(", ");
					sbLoggers.append(reqUser.getUser().getUserLogin() + "(" + reqUser.getUser().getUserLogin() + ")");
				}
				if(sbLoggers.length() == 0)
					sbLoggers.append('-');
				
				ArrayList<RequestUser> assignees = getArrayList(request.getAssignees());
				StringBuilder sbAssigness = new StringBuilder();
				for (RequestUser reqUser : assignees) {
					if(sbAssigness.length() != 0)
						sbAssigness.append(", ");
					sbAssigness.append(reqUser.getUser().getDisplayName() + "(" + reqUser.getUser().getUserLogin() + ")");
				}
				if(sbAssigness.length() == 0)
					sbAssigness.append('-');
				
				ArrayList<RequestUser> subscribers = getArrayList(request.getSubscribers());
				StringBuilder sbSubscribers = new StringBuilder();
				for (RequestUser reqUser : subscribers) {
					if(sbSubscribers.length() != 0)
						sbSubscribers.append(", ");
					sbSubscribers.append(reqUser.getUser().getDisplayName() + "(" + reqUser.getUser().getUserLogin() + ")");
				}
				if(sbSubscribers.length() == 0)
					sbSubscribers.append('-');
				
				String severity = request.getSeverityId().getDisplayName();
				
				String dueDate;
				if(null != request.getDueDate())
					dueDate = FORMAT.format(request.getDueDate());
				else 
					dueDate = "-";
				
				props.put(CREATION_DATE,creationDate);
				props.put(LAST_MODIFIED,lastModifiedDate);
				props.put(CREATOR_DISPLAY_NAME,author.getDisplayName());
				props.put(LOGGERS, sbLoggers.toString());
				props.put(ASSIGNEES, sbAssigness.toString());
				props.put(SUBSCRIBERS,sbSubscribers.toString());
				props.put(SEVERITY, severity);
				props.put(DUEDATE, dueDate);
				for(int i=0; i<structure.size(); i++){
					if(structure.get(i).identifier.equals(Field.BUSINESS_AREA))
						continue;
					else if(structure.get(i).identifier.equals(Field.REQUEST))
						break;
					else
						props.put(getUserPrefix()+structure.get(i).identifier, structure.get(i).identifierValue);
				}
				
				// Fill All the extended fields
				ArrayList<Field> allExFields = Field.lookupBySystemId(request.getSystemId(), true);
				Iterator<Field> fieldList = allExFields.iterator() ;
				while(fieldList.hasNext()) {
					Field fieldKey = fieldList.next();
					String propKey = USER_PREFIX + fieldKey.getDisplayName();
					String propValue = request.get(fieldKey.getName());
					if(null != propValue)
						props.put(propKey, propValue);
				}
			}
			else if(propsRequiredFor.identifier.equals("attachment_type_id")){
				User author = User.lookupByUserId(action.getUserId());
				String creationDate = FORMAT.format((Date)action.getLastUpdatedDate());
				props.put(CREATION_DATE,creationDate);
				props.put(CREATOR_DISPLAY_NAME,author.getDisplayName());
			}
			else if(propsRequiredFor.identifier.equals("attachment_id")){
				
				User author = User.lookupByUserId(action.getUserId());
				String creationDate = FORMAT.format((Date)action.getLastUpdatedDate());
				ArrayList<Integer> loggers = action.getLoggerIds();
				
				StringBuilder sbLoggers = new StringBuilder();
				for (Integer loggerId : loggers) {
					if(sbLoggers.length() != 0)
						sbLoggers.append(", ");
					User logger = User.lookupByUserId(loggerId);
					sbLoggers.append(logger.getUserLogin() + "(" + logger.getUserLogin() + ")");
				}
				if(sbLoggers.length() == 0)
					sbLoggers.append('-');
				
				ArrayList<Integer> assignees = action.getAssigneeIds();
				StringBuilder sbAssigness = new StringBuilder();
				for (Integer assigneeId : assignees) {
					if(sbAssigness.length() != 0)
						sbAssigness.append(", ");
					User assignee = User.lookupByUserId(assigneeId);
					sbAssigness.append(assignee.getDisplayName()
							 + "(" + assignee.getUserLogin() + ")");
				}
				if(sbAssigness.length() == 0)
					sbAssigness.append('-');
				
				ArrayList<Integer> subscribers = action.getSubscriberIds();
				StringBuilder sbSubscribers = new StringBuilder();
				for (Integer subscriberId : subscribers) {
					if(sbSubscribers.length() != 0)
						sbSubscribers.append(", ");
					User subscriber = User.lookupByUserId(subscriberId);
					sbSubscribers.append(subscriber.getDisplayName()
							 + "(" + subscriber.getUserLogin() + ")");
				}
				if(sbSubscribers.length() == 0)
					sbSubscribers.append('-');
				
				String dueDate;
				if(null != action.getDueDate())
					dueDate = FORMAT.format(action.getDueDate());
				else 
					dueDate = "-";
				
				String filePath = getAttachmentFolderPath() + File.separatorChar + Uploader.getFileLocation(propsRequiredFor.identifierHandler);
				// Check if file exists.
				File myFile = new File(filePath);
				if(!myFile.exists()) {
					return null;
				}
				String lastModifiedDate = FORMAT.format(new Date(myFile.lastModified()));
				MimetypesFileTypeMap mtftm = new MimetypesFileTypeMap();
				String contentType = mtftm.getContentType(myFile);
				String contentLength = String.valueOf(myFile.length());
				
				props.put(IS_HIDDEN,"true");
				props.put(LAST_MODIFIED,lastModifiedDate);
				props.put(CREATION_DATE,creationDate);
				props.put(CONTENT_TYPE,contentType);
				props.put(CONTENT_LENGTH,contentLength);
				props.put(CREATOR_DISPLAY_NAME,author.getDisplayName());
				props.put(LOGGERS, sbLoggers.toString());
				props.put(ASSIGNEES, sbAssigness.toString());
				props.put(SUBSCRIBERS,sbSubscribers.toString());
				props.put(DUEDATE, dueDate);
				props.put(COMMENT, action.getDescription());
				
				// Fill All the extended fields 
				Hashtable<String,ActionEx> exFields = action.getExtendedFields();
				Enumeration<String> fieldList = exFields.keys();
				while(fieldList.hasMoreElements()) {
					String fieldKey = fieldList.nextElement();
					String propKey = USER_PREFIX + fieldKey;
					String propValue = exFields.get(fieldKey).getTextValue();
					if(null != propValue)
						props.put(propKey, propValue);
				}
			}
			else {
				// Properties for types
				
				String creationDate = FORMAT.format((Date)myBusinessArea.getDateCreated());
				
				props.put(CREATOR_DISPLAY_NAME,"");
				props.put(CREATION_DATE,creationDate);
			}
		}
		catch (DatabaseException e){
			// Handle Exception
		}
		
		
		return props;
	}

	//====================================================================================
	
	// Utility Methods
	
	public static ArrayList<RequestUser> getArrayList(Collection<RequestUser> reqUser)
	{
		ArrayList<RequestUser> rus = new ArrayList<RequestUser>() ;
		if( null == reqUser )
			return rus ;
		else
		{
			rus.addAll(reqUser);
			return rus ;
		}
	}

}
