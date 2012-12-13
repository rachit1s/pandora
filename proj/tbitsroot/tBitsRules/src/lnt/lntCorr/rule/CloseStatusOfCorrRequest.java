package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import lntCorr.others.LnTConst;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

public class CloseStatusOfCorrRequest implements IRule {
	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String appBAs = PropertiesHandler.getProperty(LnTConst.CorrBaList);
		if (null == appBAs) {
			LOG.info("Property not found : " + LnTConst.CorrBaList
					+ " in tbits_properties.");
			return new RuleResult(true, "Property not found : "
					+ LnTConst.CorrBaList + " in tbits_properties.", true);
		}
		ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);

		if (null == ba || null == ba.getSystemPrefix()
				|| !sysPrefixes.contains(ba.getSystemPrefix()))
			return new RuleResult(
					true,
					"Either the ba was null Or was not applicable for correspondence module.",
					true);
      if(!ba.getSystemPrefix().equalsIgnoreCase("IOM"))
      {
		try {
			if (oldRequest != null) {
				FieldNameEntry corrNoFne = FieldNameManager
						.lookupFieldNameEntry(ba.getSystemPrefix(),
								GenericParams.CorrespondenceNumberFieldName);
				Field corrField = Field.lookupBySystemIdAndFieldName(
						ba.getSystemId(), corrNoFne.getBaFieldName());
				String corrFieldName = corrField.getName();
				Field assigneeField = Field.lookupBySystemIdAndFieldName(
						ba.getSystemId(), Field.ASSIGNEE);
				String assigneeFieldName = assigneeField.getName();
				Field subscriberField = Field.lookupBySystemIdAndFieldName(
						ba.getSystemId(), Field.SUBSCRIBER);
				String subscriberFieldName = subscriberField.getName();
				Field loggerField = Field.lookupBySystemIdAndFieldName(
						ba.getSystemId(), Field.LOGGER);
				String loggerFieldName = loggerField.getName();
				String currStatus = currentRequest.get(Field.REQUEST_TYPE);
				String protocol = currentRequest.get("disable_protocol");
				//Collection<AttachmentInfo> corrAttachments = (Collection<AttachmentInfo>) (currentRequest
				//		.getObject("CorrespondanceFile"));
				Field corrAttField = Field.lookupBySystemIdAndFieldName(
						ba.getSystemId(), "CorrespondanceFile");
				Field corrAttachmentField = Field.lookupBySystemIdAndFieldName(
						ba.getSystemId(),Field.ATTACHMENTS);
				if (protocol.equalsIgnoreCase("true")
						&& currStatus.equalsIgnoreCase("Concluded")) {

					String corrNo = oldRequest.get(corrFieldName);
					if(!corrNo.equalsIgnoreCase(""))
					{
						corrNo = corrNo + "-C";
                        currentRequest.setObject(corrField, corrNo);
					}
					Collection<RequestUser> assignee = 	(Collection<RequestUser>) oldRequest.getObject(assigneeFieldName);
					currentRequest.setObject(assigneeField, assignee);
					Collection<RequestUser> subscriber = (Collection<RequestUser>)oldRequest.getObject(subscriberFieldName);
					currentRequest.setObject(subscriberField, subscriber);
					Collection<RequestUser> logger = (Collection<RequestUser>)oldRequest.getObject(loggerFieldName);
					currentRequest.setObject(loggerField, logger);
					Collection<AttachmentInfo> corrAttFile = (Collection<AttachmentInfo>) oldRequest
							.getObject("CorrespondanceFile");
					if(corrAttFile != null)
					currentRequest.setObject(corrAttField, corrAttFile);
					Collection<AttachmentInfo> corrAtt = (Collection<AttachmentInfo>) oldRequest
							.getObject(Field.ATTACHMENTS);
					if(corrAtt != null)
					currentRequest.setObject(corrAttachmentField, corrAtt);

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("Rule did not suceed..");
			return new RuleResult(false, "Status was not closed for  ba : "
					+ ba.getSystemPrefix(), false);

		}
      }
		return new RuleResult(true, "Status closed successfully in ba : "
				+ ba.getSystemPrefix(), true);
      
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Status closed successfully";
	}

}
