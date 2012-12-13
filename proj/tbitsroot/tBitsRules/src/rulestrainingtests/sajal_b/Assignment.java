package rulestrainingtests.sajal_b;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

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
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;

public class Assignment implements IRule 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("rulestrainingtests.sajal_b");

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
	
			try {
				if(!ba.getSystemPrefix().equalsIgnoreCase("tbits"))
				{
					return new RuleResult(true,"Select tbits business_area",true);
				}
			
				// 1.
				ArrayList<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>(
						currentRequest.getAttachmentsOfType("attachments"));

				if (isAddRequest) 
				{
					if (attachments.isEmpty()) 
					{
						return new RuleResult(false,"cannot add a request when there is no attachment",true);
					}
				}
				else if (!(oldRequest == null)) 
				{
					// 2.
					Collection<AttachmentInfo> oldAttachments = oldRequest
							.getAttachmentsOfType("attachments");
					Collection<AttachmentInfo> newAttachments = currentRequest
							.getAttachmentsOfType("attachments");

					for (AttachmentInfo oldAttachmentFiles : oldAttachments) 
					{
						int oldRepoId = oldAttachmentFiles.getRepoFileId();
						for (AttachmentInfo newAttachmentFiles : newAttachments) 
						{
							int newRepoId = newAttachmentFiles.getRepoFileId();
							if (oldRepoId == newRepoId) 
							{
								break;
							} 
							else 
							{
								return new RuleResult(false,"cannot delete an attachment", true);
							}
						}
					}

					// 3.

					Collection<AttachmentInfo> otherAttachments = (Collection<AttachmentInfo>) (currentRequest
							.getAttachmentsOfType("other_attachment"));
					Field otherAttField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "other_attachment");
					Field attField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), Field.ATTACHMENTS);
					otherAttachments.addAll(oldAttachments);
					newAttachments.clear();
					currentRequest.setObject(otherAttField, otherAttachments);
				}
					// New request added in other BA

					AddRequest addRequest = new AddRequest();
					TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
					Hashtable<String, String> aParamTable = new Hashtable<String, String>();
					//Request addedRequest=addRequest.addRequest(connection, tbitsResMgr, aParamTable);
					aParamTable.put(Field.BUSINESS_AREA, "testba");
					Date dueDate = currentRequest.getDueDate();
					Date newBaDueDate = (Date) dueDate.clone();
					newBaDueDate.setDate(newBaDueDate.getDate() + 5);
					SimpleDateFormat dateFormat = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);
					String lastUpdatedDate = dateFormat.format(newBaDueDate);
					aParamTable.put(Field.DUE_DATE, lastUpdatedDate);
				
					// 4.
					// testers to assignee
					//String tester = currentRequest.get("testers");
					//aParamTable.put(Field.ASSIGNEE, tester);

					// otherattachments and attachments to attachments
					Collection<AttachmentInfo> newAtt1 = currentRequest.getAttachmentsOfType("attachments");
					Collection<AttachmentInfo> otherAttachments1 = (Collection<AttachmentInfo>) (currentRequest.getAttachmentsOfType("other_attachment"));
					Collection<AttachmentInfo> finalInfos = new ArrayList<AttachmentInfo>(newAtt1);
					finalInfos.addAll(otherAttachments1);
					String attString = AttachmentInfo.toJson(finalInfos);
					aParamTable.put(Field.ATTACHMENTS, attString);

					// logger_ids and subscriber_ids in subscriber
					String loggers = currentRequest.get(Field.LOGGER);
					String subscriber = currentRequest.get(Field.SUBSCRIBER);
					String newSubscriber = "";
					if (!subscriber.equals("") && !loggers.equals("")) {
						newSubscriber = loggers + "," + subscriber;
					} else if (subscriber.equals("")) {
						newSubscriber = loggers;
					} else if (loggers.equals("")) {
						newSubscriber = subscriber;
					}
					aParamTable.put(Field.SUBSCRIBER, newSubscriber);

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
					// 6.set related request
					int actionId = currentRequest.getMaxActionId();
					int requestId = currentRequest.getRequestId();
					String relatedRequest = ba.getSystemPrefix() + "#" + requestId + "#" + actionId;
					aParamTable.put(Field.RELATED_REQUESTS, relatedRequest);
					
					Request addedRequest=addRequest.addRequest(connection, tbitsResMgr, aParamTable);

			} 
			catch (Exception e) {
				LOG.error(TBitsLogger.getStackTrace(e));
				return new RuleResult(true, "Rule with name \"" + getName()
						+ "\" failed with message : " + e.getMessage(), false);
			} 
			catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

		return new RuleResult(true, "", true);
	}

	@Override
	public double getSequence() {

		return 1;
	}

	@Override
	public String getName() {
		return "RuleForChangeInAttachments";

	}

}