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
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.*;
/**
 * @author sajal
 *
 */
public class MailPreprocessorRule3 implements IMailPreProcessor
{
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_MAIL);
	@Override
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		double priority = 1;
		try
		{
			if(request != null && actionFileHash != null && user != null)
			{
				Collection<ActionFileInfo> latestAtts = actionFileHash.get(request.getMaxActionId());
				//if(user.getMemberOf() != null){}
				ArrayList<User> listOfUsers = MailListUser.getMailListsByRecursiveMembership(user.getUserId());
				if(listOfUsers != null)
				{
					//User is not a member of any mailing list. Hence the files will be send as an attachment
					for(ActionFileInfo afi : latestAtts)
					{
						afi.setPriority(-1);
					}
				}	
				else
				{
					for(ActionFileInfo afi : latestAtts)
					{
						afi.setPriority(priority);
						priority = priority+1;
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
		return "Rule where files goes as a link if user is in a mailing-list and as an attachment if user is not in any mailing-list";
	}

	@Override
	public double getMailPreProcessorOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}

