package examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.mail.IMailPreProcessor;

public class PerUserMailProcessor implements IMailPreProcessor 
{

	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> action, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		// for all business areas
		Collection<ActionFileInfo> actionFileInfos = actionFileHash.get(request.getMaxActionId());
		
		
		request.setSubject("New Subject");
		// for particular users ..
		if( user.getUserLogin().equals("gprao"))
		{
			// changing the corr no. 
			try {
				request.setExString("CorrespondanceNumber", "GPRAO's special correspondance number..!!");
				action.get(request.getMaxActionId() - 1).setDescription("GPRAO : New Description for latest action");
				
			} catch (DatabaseException e) {				
				e.printStackTrace();
			}
			
			if( actionFileInfos != null )				
				for( ActionFileInfo afi : actionFileInfos )
				{
					afi.setPriority(1.0);
				}
		}
		else if( user.getUserLogin().equals("ajay") )
		{
			try {
				request.setExString("CorrespondanceNumber", "AJAY's special correspondance number..!!");
				action.get(request.getMaxActionId() - 1).setDescription("AJAY : New Description for latest action");
			} catch (DatabaseException e) {				
				e.printStackTrace();
			}
			
			if( actionFileInfos != null )
				for( ActionFileInfo afi : actionFileInfos )
				{
					afi.setPriority(-1.0);
				}
		}
		else if( user.getUserLogin().equals("bgzarbade") )
		{
			try {
				request.setExString("CorrespondanceNumber", "BG's special correspondance number..!!");
				action.get(request.getMaxActionId() - 1).setDescription("BG : New Description for latest action");
			} catch (DatabaseException e) {				
				e.printStackTrace();
			}
			int i = 0 ;
			
			if( actionFileInfos != null )
				for( ActionFileInfo afi : actionFileInfos )
				{
					if( i % 2 == 0 )
						afi.setPriority(1.0);
					else
						afi.setPriority(-1.0);
					
					i++;
				}
		}
			
			
	}

	public String getMailPreProcessorName() {
		return "Sends all files as attachment for gprao, all files as links to ajay, alternate files as link to bgzarbade";
	}

	// with order to be almost last in the plugins... 
	public double getMailPreProcessorOrder() 
	{
		return 100;
	}

}
