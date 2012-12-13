package DeletedAttAsLink;

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
import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;

public class SendDeletedAttachmentsAsLink implements IMailPreProcessor {

	public static String name = "This plugin marks all the delete files as linked." ;
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_MAIL);
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		try
		{
			// for all business-areas and all kind of users.
			if( request != null && actionFileHash != null  )
			{
				int maxActionId = request.getMaxActionId() ;
				Collection<ActionFileInfo> latestAtts = actionFileHash.get(maxActionId);
				if( latestAtts != null )
				{
					for( ActionFileInfo afi : latestAtts )
					{
						// files ( Deleted )
						if( afi.getFileAction() == null || afi.getFileAction().equalsIgnoreCase("D") )
							afi.setPriority(-1); // this will appear as link in email
					}
				}
			}
		}
		catch(Exception e)
		{
			LOG.info("Exception occured while executing the plugin : " + TBitsLogger.getStackTrace(e));
		}
	}

	public String getMailPreProcessorName() {		
		return name ;
	}

	public double getMailPreProcessorOrder() {
		return 1;
	}

}
