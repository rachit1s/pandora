package examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.mail.IMailPreProcessor;

public class PDFAsAttachment implements IMailPreProcessor 
{	
	TBitsLogger LOG = TBitsLogger.getLogger("examples") ;
	public void executeMailPreProcessor(User user, Request request, ArrayList<Action> actionList, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash, Hashtable<String,Integer>permissions ) 
	{
		try
		{
			// check input parameters
			if( null != user && null != request && null != actionFileHash )
			{
				// for all the business areas.
								
				// for all users 

				Collection<ActionFileInfo> actionFileInfos = actionFileHash.get(request.getMaxActionId()) ;
				if( actionFileInfos != null )
				{
					for( ActionFileInfo afi : actionFileInfos )
					{
						// the afi.getName() will give the display name of the files
						// which can be misleading. so using the dot extension of the location
						String fileName = afi.getLocation() ;
						int index = fileName.lastIndexOf('.');
						if( index != -1 && index < fileName.length() - 1 )
						{
							String extension = fileName.substring(index+1);
							// send only pdf files as attachments
							if(extension.trim().equalsIgnoreCase("pdf"))
								afi.setPriority(100);
							else// rest as links
								afi.setPriority(-1.0);
						}
						else// rest as links
							afi.setPriority(-1.0);
						
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
		return "MailPreProcessor to send only pdf files as attachments in all ba, and rest of the files as links";
	}

	// set the order of execution of this plugin.
	public double getMailPreProcessorOrder() 
	{
		return 102;
	}

}
