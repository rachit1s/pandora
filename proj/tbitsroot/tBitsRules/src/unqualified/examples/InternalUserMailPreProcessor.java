package examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.IMailPreProcessor;

public class InternalUserMailPreProcessor implements IMailPreProcessor {

	TBitsLogger LOG = TBitsLogger.getLogger("examples") ;
	public void executeMailPreProcessor(User user, Request request, ArrayList<Action> actionList, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash, Hashtable<String,Integer>permissions) 
	{
		try
		{
			// check input parameters
			if( null != user && null != request && null != actionFileHash )
			{
				// check the ba.
				BusinessArea ba = BusinessArea.lookupBySystemId(request.getSystemId()) ;
				if( null == ba || ! ba.getSystemPrefix().equalsIgnoreCase("Corr") )
					return ;
				
				// check for the users 
				if( user.getUserTypeId() != UserType.INTERNAL_USER )
					return; 
				
				// change the permissions if required. 
				// 1. Say I don't want to show the field with name CorrespondenceNumber.
				// 2. and I don't want to send them the attachments with Field name as CorrespondenceFile
				// 3. but to send rest of the attachments as link for the current action.
				
				//1.
				Integer cnPerm = permissions.get("CorrespondanceNumber") ;
				if( null != cnPerm )
				{
					// remove the VIEW Permission : note this is the permission
					int perm = cnPerm.intValue() ;
					perm = ( perm & (~Permission.EMAIL_VIEW) ) ;
					permissions.put("CorrespondanceNumber", perm);
				}
				
				//2.
				Integer cfPerm = permissions.get("CorrespondanceFile") ;
				if( null != cfPerm )
				{
					// remove the VIEW Permission : note this is the permission
					int perm = cfPerm.intValue() ;
					perm = ( perm & (~Permission.EMAIL_VIEW) ) ;
					permissions.put("CorrespondanceFile", perm);
				}
				
				//3.			
				Collection<ActionFileInfo> actionFileInfos = actionFileHash.get(request.getMaxActionId());
				
				if( actionFileInfos != null )
				{
					for( ActionFileInfo afi : actionFileInfos )
					{
						afi.setPriority(-1); // make all files as link
					}
				}
			}
		}
		catch(Exception e)
		{
			LOG.info(TBitsLogger.getStackTrace(e));
		}
			
	}

	
	public String getMailPreProcessorName() 
	{	
		return "Makes all the files for the internal user as links";
	}

	
	public double getMailPreProcessorOrder() 
	{	
		return 2;
	}

}
