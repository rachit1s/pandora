package rulestrainingtests.syeda_m;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import antlr.collections.List;

import com.ibm.icu.text.SimpleDateFormat;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;

public class RuleForhomework implements IRule {
	public static TBitsLogger LOG = TBitsLogger.getLogger("testrule");
	String sysPrefix = "";

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		sysPrefix = ba.getSystemPrefix();
		if (sysPrefix.equalsIgnoreCase("mercy_tbits")) {
			try {
				// 1.
				Collection<AttachmentInfo> attachments = (Collection<AttachmentInfo>) (currentRequest
						.getObject(Field.ATTACHMENTS));

				if (isAddRequest) {
					if (attachments.isEmpty()) {
						return new RuleResult(
								false,
								"cannot add a request when there is no attachment",
								true);
					}

				} else if (!(oldRequest == null)) {
					// 2.
					Collection<AttachmentInfo> oldAtt = (Collection<AttachmentInfo>) oldRequest.getObject("attachments");
					Collection<AttachmentInfo> newAtt = (Collection<AttachmentInfo>) currentRequest
							.getObject("attachments");

					for (AttachmentInfo oldaAfi : oldAtt) {
						boolean found = false;
						int oldRepoId = oldaAfi.getRepoFileId();
						for (AttachmentInfo newAfi : newAtt) {
							int newRepoId = newAfi.getRepoFileId();
							if (oldRepoId == newRepoId) {
								found = true ;
						newAtt.remove(newAfi);
							}
						}
						
						if( !found )
						return new RuleResult(false,
								"cannot delete an attachment", true);
							
					}

					// 3.

					Collection<AttachmentInfo> otherAttachments = (Collection<AttachmentInfo>) (currentRequest
							.getObject("other_attachment"));

					Field otherAttField = Field.lookupBySystemIdAndFieldName(
							ba.getSystemId(), "other_attachment");
					Field attField = Field.lookupBySystemIdAndFieldName(
							ba.getSystemId(), Field.ATTACHMENTS);
					otherAttachments.addAll(oldAtt);
					
					currentRequest.setObject(otherAttField, otherAttachments);
					currentRequest.setObject(attField, newAtt);				}
					// 5.due date for mercy_ba= duedate of mercy_tbits+5 days

					AddRequest addRequest = new AddRequest();
					TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
					Hashtable<String, String> aParamTable = new Hashtable<String, String>();
					//Request addedRequest=addRequest.addRequest(connection, tbitsResMgr, aParamTable);
					aParamTable.put(Field.BUSINESS_AREA, "mercy_ba");
					Date dueDate = currentRequest.getDueDate();
					Calendar newBaDueDate = (Calendar) dueDate.clone();
					//TODO : Use calendar class . settime and add
					newBaDueDate.add(newBaDueDate.DATE, 5);
					
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							TBitsConstants.API_DATE_FORMAT);
					String lastUpdatedDate = dateFormat.format(newBaDueDate);
					aParamTable.put(Field.DUE_DATE, lastUpdatedDate);
				
					// 4.
					// testers to assignee
					String tester = currentRequest.get("testers");
					aParamTable.put(Field.ASSIGNEE, tester);

					// otherattachments and attachments to attachments
					Collection<AttachmentInfo> newAtt1 = (Collection<AttachmentInfo>) currentRequest.getObject(Field.ATTACHMENTS);
					
					Collection<AttachmentInfo> otherAttachments1 = (Collection<AttachmentInfo>) (currentRequest
							.getObject("other_attachment"));
					Collection<AttachmentInfo> finalAtt = new ArrayList<AttachmentInfo>(
							newAtt1);
					finalAtt.addAll(otherAttachments1);
					String attachment = AttachmentInfo.toJson(finalAtt);
					aParamTable.put(Field.ATTACHMENTS, attachment);

					

					// assignee to logger
					String assigneString = currentRequest.get(Field.ASSIGNEE);
					aParamTable.put(Field.LOGGER, assigneString);

					// description to summary
					String description = currentRequest.get(Field.DESCRIPTION);
					aParamTable.put(Field.SUMMARY, description);

					// type field to type
					String subject = currentRequest.get(Field.SUBJECT);
					String category = currentRequest.get(Field.CATEGORY);
					String requestType = currentRequest.get(Field.REQUEST_TYPE);
					aParamTable.put(Field.USER, user.getUserLogin());
					aParamTable.put(Field.SUBJECT, subject);
					aParamTable.put(Field.CATEGORY, category);
					aParamTable.put(Field.REQUEST_TYPE, requestType);
					// subscriber + logger to subscriber.
					Collection<RequestUser> subscribers = (Collection<RequestUser>) currentRequest.getObject(Field.SUBSCRIBER);
					Collection<RequestUser> loggers = (Collection<RequestUser>) currentRequest.getObject(Field.LOGGER);
					ArrayList<RequestUser> allRUs = new ArrayList<RequestUser>();
					if( null != loggers )
						allRUs.addAll(loggers);
					
					if( null != subscribers )
						allRUs.addAll(subscribers);
					
					HashSet<Integer> userIds = new HashSet<Integer>();
					for(RequestUser ru : allRUs )
					{
						userIds.add(ru.getUserId());
					}
					
					StringBuffer sb = new StringBuffer();
					boolean first = true;
					for(Integer userId : userIds )
					{
						User u = User.lookupByUserId(userId);
						if( null != u )
						if( first )
						{
							sb.append(u.getUserLogin());
						}
						else
						{
							sb.append(","+u.getUserLogin());
						}
					}
					
					
					aParamTable.put(Field.SUBSCRIBER, sb.toString());
//				String logger= currentRequest.get(Field.LOGGER);
//				String subscriber = currentRequest.get(Field.SUBSCRIBER);
//					String finalSubcriber="";
//					if (logger.equalsIgnoreCase("")&&subscriber.equalsIgnoreCase(""))
//					{
//						finalSubcriber="";
//					}
//					else if ((!logger.equalsIgnoreCase(""))&&(!subscriber.equalsIgnoreCase("")))
//					{
//						
//						finalSubcriber=logger+","+subscriber;
//						
//					}
//					finalSubcriber.split(",");
//					  Set<String> set = new HashSet<String>();
//					  set.add(finalSubcriber.toString());
//					  
//					  aParamTable.put(Field.SUBSCRIBER, );   

					  
				
					
					Request addedRequest=addRequest.addRequest(connection, tbitsResMgr, aParamTable);

			} catch (Exception e) {
				LOG.error(TBitsLogger.getStackTrace(e));
				return new RuleResult(true, "Rule with name \"" + getName()
						+ "\" failed with message : " + e.getMessage(), false);
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new RuleResult(true, "", true);
	}

	@Override
	public double getSequence() {

		return 1;
	}

	@Override
	public String getName() {
		return "homework";

	}

}