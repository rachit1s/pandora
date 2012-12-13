package examples ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.jfree.util.Log;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.mail.IMailPreProcessor;

public class AlphaMailPreProcessor implements IMailPreProcessor
{
	TBitsLogger LOG = TBitsLogger.getLogger("examples") ;
	public void executeMailPreProcessor(User user, Request request, ArrayList<Action> actionList, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash, Hashtable<String,Integer>permissions )
	{
		try
		{
			// check input parameters
			if( null != request && null != actionFileHash )
			{
				// check the ba.
				BusinessArea ba = BusinessArea.lookupBySystemId(request.getSystemId()) ;
				if( null == ba || ! ba.getSystemPrefix().equalsIgnoreCase("Corr") )
					return ;
				
				// check for the user
				if( ! user.getUserLogin().equals("gprao") )
					return; 
				
				Collection<ActionFileInfo> actionFileInfos = actionFileHash.get(request.getMaxActionId());
				
				if( actionFileInfos == null )
					return ;
				
				Comparator<ActionFileInfo> comp = new Comparator<ActionFileInfo>()
				{
					public int compare(ActionFileInfo arg0, ActionFileInfo arg1) 
					{
						return arg0.getName().compareToIgnoreCase(arg1.getName()) * -1 ; // for reverse sort
					}			
				};
				
				Collections.sort((List)actionFileInfos,comp);
				
				int i = 0 ;
				for( ActionFileInfo afi : actionFileInfos )
				{			
					afi.setPriority(i*0.5);
					i++;
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
		return "MailPreProcessor to make deleted files always appear as links.";
	}

	public double getMailPreProcessorOrder() {
		
		return 0;
	}

}
