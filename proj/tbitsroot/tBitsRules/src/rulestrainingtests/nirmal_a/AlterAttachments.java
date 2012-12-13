package rulestrainingtests.nirmal_a ;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.batik.dom.util.HashTable;

import com.ibm.icu.text.SimpleDateFormat;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;

/**
 * 1. Add-Request should not be allowed if the user has not uploaded a new file into "attachments" field.
2. Deletion of existing file from "attachments" field is not allowed.
3. On every update remove the old files from "attachments" field and add them to the list of files in "other_attachments" field. Note existing files in "other_attachments" should not be removed.
4. On every request add/update create a new request in test_ba with following fields copied from tbits to test_ba

   Tbits-fields	test_ba-field


   testers	               assignee_ids
   other_attachments	   attachments
   description	           summary
   logger_ids			    subscribers
   assignee_ids	           logger_ids
   All Type Fields	       Respective-Type Fields
   attachments	           attachments
   subscriber_ids	       subscriber_ids

Note that both "attachments" and "other_attachments" of tbits BA goes in the same field "attachments" of test_ba.
similarly "subscribers" and "logger_ids" from tbits BA goes into the "subscribers" of test_ba

Next that you should assign the duedate for test_ba request to = due_date of tbits_ba + 5 days 

Next you should set the related request link in test_ba to this update of tbits ba.
i.e. if the test_ba request 6 is created on the 5th update to 2nd request in tbits ba then it should contain the link tbits#2#5 in related request.
 *
 * @author  : nirmal
 * @version : $Id:1$
 *
 */

public class AlterAttachments implements IRule {

	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		TBitsLogger logger=TBitsLogger.getLogger("noobrulz");
		logger.info("Executing the alter attachements rule");
		String sys_prefix=ba.getSystemPrefix();
		//checking for valid ba
		if(!sys_prefix.equalsIgnoreCase("noob_tbits"))
		{
			logger.info("AlterAttachments rule is not applicable to ba : "+sys_prefix);
			return new RuleResult(true,"AlterAttachments rule is not applicable to ba : "+sys_prefix,true);
		}
		
		if(isAddRequest)
		{
			Collection<AttachmentInfo> atts = currentRequest.getAttachments();
			
			if(atts.isEmpty())
				return new RuleResult(false,"Attachments can not be empty",true);
		}
		if(!isAddRequest)
		{
			Collection<AttachmentInfo> attsOld = oldRequest.getAttachments();
			Collection<AttachmentInfo> attsNew = currentRequest.getAttachments();
			for(AttachmentInfo aOldInfo:attsOld)
			{
				int oldRepoId=aOldInfo.getRepoFileId();
				boolean flag=false;
				for(AttachmentInfo aNewInfo:attsNew)
				{
					int newRepoId=aNewInfo.repoFileId;
					if(newRepoId==oldRepoId)
					{
						flag=true;
						attsNew.remove(aNewInfo);
						break;
					}
				}
				if(flag==false)
				{
					
					return new RuleResult(false,"YOU Can not Remove : \"" + aOldInfo.getName() +"\" file From the Attachments ") ;
					
				}

			}
			
			try {
				Field otherAttField=Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "other_attachment");
				Field attField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), Field.ATTACHMENTS);
				Collection<AttachmentInfo> otherAtts=(Collection<AttachmentInfo>)currentRequest.getObject("other_attachment");
				otherAtts.addAll(attsOld);
				currentRequest.setObject(attField, attsNew);
				currentRequest.setObject(otherAttField, otherAtts);
				
			} catch (DatabaseException e) {
				logger.info("Database error other_attachment field not found");
				e.printStackTrace();
			}
			
			
		}
		Collection<AttachmentInfo> attInfos=currentRequest.getAttachments();
		Collection<AttachmentInfo> otherAttInfos=(Collection<AttachmentInfo>)currentRequest.getObject("other_attachment");
		Collection<AttachmentInfo> finalInfos=new ArrayList<AttachmentInfo>(attInfos);
		finalInfos.addAll(otherAttInfos);
	 	//Collection<RequestUser> loggerUsers=currentRequest.getLoggers();
	 	//Collection<RequestUser> subsUsers=currentRequest.getSubscribers();
	 	//subsUsers.addAll(loggerUsers);
		String attString=AttachmentInfo.toJson(finalInfos);
		
		//String otherAttString=currentRequest.get("other_attachment");
		//String attString=currentRequest.get(Field.ATTACHMENTS);
		String loggersString=currentRequest.get(Field.LOGGER);
		String subscriberString=currentRequest.get(Field.SUBSCRIBER);
		String finalSubsString="";
		if(!subscriberString.equals("") && !loggersString.equals(""))
		{
			finalSubsString=loggersString+","+subscriberString;
		}
		else if(subscriberString.equals(""))
			finalSubsString=loggersString;
		else if(loggersString.equals(""))
			finalSubsString=subscriberString;
		
		String assigneString=currentRequest.get(Field.ASSIGNEE);
		
		String testerString=currentRequest.get("testers");
		
		String descriptionString=currentRequest.get(Field.DESCRIPTION);
		String subjectString=currentRequest.get(Field.SUBJECT);
		String categoryString=currentRequest.get(Field.CATEGORY);
		String requestTypeString=currentRequest.get(Field.REQUEST_TYPE);
		
//		Field.lookupBySystemId(aSystemId, isExtended, dataType)
		
		Date dueDate=currentRequest.getDueDate();
		Date testDueDate=(Date)dueDate.clone();
		testDueDate.setDate(dueDate.getDate()+5);
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String dueDateString1= format.format(testDueDate);
		int actionId=currentRequest.getMaxActionId();
		int reqId=currentRequest.getRequestId();
		String relatedReqString=ba.getSystemPrefix()+"#"+reqId+"#"+actionId;
		//String dueDateString=currentRequest.get(Field.DUE_DATE);
		AddRequest addRequest=new AddRequest();
		Hashtable<String, String> aParamTable=new Hashtable<String, String>();
		aParamTable.put(Field.BUSINESS_AREA, "noob_test");
		aParamTable.put(Field.USER, user.getUserLogin());
		aParamTable.put(Field.DUE_DATE, dueDateString1);
		aParamTable.put(Field.SUBJECT, subjectString);
		aParamTable.put(Field.SUBSCRIBER, finalSubsString);
		aParamTable.put(Field.LOGGER, assigneString);
		aParamTable.put(Field.ASSIGNEE, testerString);
		aParamTable.put(Field.ATTACHMENTS, attString);
		aParamTable.put(Field.SUMMARY,descriptionString);
		aParamTable.put(Field.CATEGORY, categoryString);
		aParamTable.put(Field.REQUEST_TYPE, requestTypeString);
		aParamTable.put(Field.RELATED_REQUESTS, relatedReqString);
		try {
			addRequest.addRequest(aParamTable);
		} catch (APIException e) {
			logger.info("request can not be added to noob_test ba");
			e.printStackTrace();
			return new RuleResult(false,"new request in noob_test ba can not be added");
		}
		return new RuleResult(true,"Rule completed",true);
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 2;
	}

}
