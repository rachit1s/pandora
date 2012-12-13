package lntCorr.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.IMailPreProcessor;

public class EmailAttachmentPreprocessor implements IMailPreProcessor {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		String smartLink = null;
		try
		{
			String validBas = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			if( null == validBas )
			{
				LOG.info("Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
				return;
			}
			BusinessArea ba = BusinessArea.lookupBySystemId(request.getSystemId());
			
			ArrayList<String> validBaList = Utility.splitToArrayList(validBas);
			if(null == ba || null == validBaList || !validBaList.contains(ba.getSystemPrefix()))
			{
				LOG.debug("This mail preprocessor is not applicable for this ba.");
				return ;
			}
			
			smartLink = ba.getSystemPrefix() + "#" + request.getRequestId() + "#" + request.getMaxActionId();
			
			FieldNameEntry corrFileFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceFileFieldName);
			FieldNameEntry otherFileFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.OtherAttachmentFieldName);
			
			Field corrFileField = null ;
			if( null != corrFileFne )
				corrFileField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), corrFileFne.getBaFieldName());
			
//			Field otherFileField = null; 
//			if( null != otherFileFne )
//				otherFileField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), otherFileFne.getBaFieldName());
			
			int maxActionId = request.getMaxActionId();
			Collection<ActionFileInfo> currAtts = actionFileHash.get(maxActionId);
			if( null == currAtts || currAtts.size() == 0 )
			{
				LOG.info("No attachments to handle for request : " + smartLink);
				return ;
			}
			
			double corrFilePriority = 0;
			double otherFilePriority = 0; 
			
			switch( user.getUserTypeId() )
			{
				case UserType.EXTERNAL_USER :
				{
					corrFilePriority = 2;
					otherFilePriority = 1;
					break;
				}
				case UserType.INTERNAL_USER :
				{
					corrFilePriority = 2;
					otherFilePriority = -1 ; // always link
					break;
				}
				default :
				{
					LOG.info("The user " + user + " was neither internal user nor external user. So skiping the mail preprocesor.");
					return;
				}
			}
			
			for( ActionFileInfo afi : currAtts )
			{
				if( null == afi )
				{
					continue; 
				}
				else if (afi.getFileAction().equals("D"))
				{
					afi.setPriority(-1);
				}
				else if( null != corrFileField && afi.getFieldId() == corrFileField.getFieldId() )
				{
					afi.setPriority(corrFilePriority);
				}
				else // for all other files.
				{
					afi.setPriority(otherFilePriority);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error("Exception occured while excuting mail preprocessor : ");
			LOG.error(e);
		}	
	}

	public String getMailPreProcessorName() {
		return "This processor runs for correspondence ba's and decides which files should go as link or attachment in email.";
	}

	public double getMailPreProcessorOrder() {
		return 0;
	}

}
