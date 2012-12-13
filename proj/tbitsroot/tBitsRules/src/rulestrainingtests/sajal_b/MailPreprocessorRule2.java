/**
 * 
 */
package rulestrainingtests.sajal_b;

import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.*;
/**
 * @author sajal
 *
 */
public class MailPreprocessorRule2 implements IMailPreProcessor
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
				Collection<ActionFileInfo> latestAtts = actionFileHash.get(request.getMaxActionId());
				double priority = 1;
				if(user.getUserTypeId() == UserType.EXTERNAL_USER)
				{
					for(ActionFileInfo afi : latestAtts)
					{
						afi.setPriority(priority);
						priority = priority+1;
					}
					//Collection<AttachmentInfo> allAttachments = request.getAttachments();
				}
				else if(user.getUserTypeId() == UserType.INTERNAL_USER)
				{
					for(ActionFileInfo afi : latestAtts)
					{
						afi.setPriority(-1);
					}
				}
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
		return "Rule where files to external users go as an attachment and files to internal users, go as a link";
	}

	@Override
	public double getMailPreProcessorOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}

