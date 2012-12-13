/**
 * 
 */
package rulestrainingtests.sajal_b;

import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.*;
/**
 * @author sajal
 *
 */
public class MailPreprocessorRule implements IMailPreProcessor
{
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_MAIL);
	@Override
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		try
		{
			if(request != null && actionFileHash != null && user != null)
			{
				//Collection<AttachmentInfo> otherAttachments = (Collection<AttachmentInfo>) (request.getAttachmentsOfType("other_attachment"));
				Collection<ActionFileInfo> latestAtts = actionFileHash.get(request.getMaxActionId());
				Field attachField = (Field)request.getObject("attachments");
				Field otherAttachField = (Field)request.getObject("other_attachment");
				int attachmentFieldId = attachField.getFieldId();
				int otherAttachmentFieldId = otherAttachField.getFieldId();
				Collection<ActionFileInfo> attachFileCollection = null;
				String s1 = null;
				String s2 = null;
				String temp = null;
				double priority = 0;
				//Collection<ActionFileInfo> latestAtts = actionFileHash.get(request.getMaxActionId());
				if( latestAtts != null )
				{
					for( ActionFileInfo afi : latestAtts )
					{
						// for all kind of files ( Added, Modified, Deleted )
						if(otherAttachmentFieldId == afi.getFieldId())
						afi.setPriority(-1); // all will appear as link in email
						else if(attachmentFieldId == afi.getFieldId())
							attachFileCollection.add(afi);//store attachments into a collection
					}
					//Collections.sort((ActionFileInfo)List<ActionFileInfo> attachFileCollection);
					//Comparator<ActionFileInfo> CompAttachments = (Comparator<ActionFileInfo>) attachFileCollection;
					
					Iterator<ActionFileInfo> itr = attachFileCollection.iterator();
					if(itr.hasNext())
					s1 = (String)((itr.next()).getName());
				
					while(itr.hasNext())
					{
						s2 = (itr.next()).getName();
						if(s1.compareTo(s2)>0)
						{
							temp = s1;
							s1 = s2;
							s2 = temp;
						}
					}
		
					
					for(ActionFileInfo af:attachFileCollection)
					{
						af.setPriority(priority);
						priority = priority+1;
					}
				}
				
					//Field other_attachments = Field.lookupBySystemIdAndFieldName(request.getSystemId(), "other_attachments"); 
			}
		}
		catch(Exception e)
		{
			LOG.info("Exception occured while executing the plugin : " + TBitsLogger.getStackTrace(e));
		}
	}

	@Override
	public String getMailPreProcessorName() {
		// TODO Auto-generated method stub
		return "Rules where files in other_attachment field go as a link and as a attachment for attachment field";
	}

	@Override
	public double getMailPreProcessorOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}
