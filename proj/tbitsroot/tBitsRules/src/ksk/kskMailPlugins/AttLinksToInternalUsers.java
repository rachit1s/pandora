package kskMailPlugins;

import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.IMailPreProcessor;

public class AttLinksToInternalUsers implements IMailPreProcessor {

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_MAIL);
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		try
		{
			// for all business-areas
			if( request != null && actionFileHash != null && null != user && user.getUserTypeId() == UserType.INTERNAL_USER )
			{
				int maxActionId = request.getMaxActionId() ;
				Collection<ActionFileInfo> latestAtts = actionFileHash.get(maxActionId);
				if( latestAtts != null )
				{
					for( ActionFileInfo afi : latestAtts )
					{
						// for all kind of files ( Added, Modified, Deleted )
						afi.setPriority(-1); // all will appear as link in email
					}
				}
			}
		}
		catch(Exception e)
		{
			LOG.info("Exception occured while executing the plugin : " + TBitsLogger.getStackTrace(e));
		}
	}

	public String getMailPreProcessorName() 
	{		
		return "This plugin will make all the attachments to appear as link in the email for Internal Users";
	}

	public double getMailPreProcessorOrder() 
	{
		return 10;
	}

}
