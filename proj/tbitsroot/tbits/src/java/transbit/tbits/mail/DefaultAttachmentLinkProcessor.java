package transbit.tbits.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

public class DefaultAttachmentLinkProcessor implements IMailPreProcessor {

	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_MAIL);
	
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		try
		{
			//
			if( null != actionFileHash )
			{
				Collection<ActionFileInfo> atts = actionFileHash.get(request.getMaxActionId());
				if( null != atts )
				{
					for( ActionFileInfo afi : atts )
					{
						if( afi.getFileAction().equalsIgnoreCase("D"))
						{
							// if it is a delete file then set it to be sent as link
							afi.setPriority(-1);
						}
						// if it is an external user then send the file as attachment
						else if( user.getUserTypeId() == UserType.EXTERNAL_USER )
						{
							afi.setPriority(0); // try sending as attachment
						}
						// for internal user send it as a link
						else // internal user
						{
							afi.setPriority(-1); // send as link
						}
					}
				}
			}
		}
		catch(Exception e)
		{
//			e.printStackTrace();
			LOG.error("Exception occured in " + this.getClass().getName(), e);
		}
	}

	public String getMailPreProcessorName() {
		return "always send deleted files as link AND send link to internal users and attachments to external users for all attachment types.";
	}

	public double getMailPreProcessorOrder() {
		return 0;
	}

}
