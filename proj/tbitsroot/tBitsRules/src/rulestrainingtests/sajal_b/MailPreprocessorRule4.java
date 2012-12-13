/**
 * 
 */
package rulestrainingtests.sajal_b;

import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.*;
/**
 * @author sajal
 *
 */
public class MailPreprocessorRule4 implements IMailPreProcessor
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
			Collection<ActionFileInfo> latestAtts = actionFileHash.get(request.getMaxActionId());
			Field attachField = (Field)request.getObject("attachments");
			Field otherAttachField = (Field)request.getObject("other_attachment");
			int attachmentFieldId = attachField.getFieldId();
			int otherAttachmentFieldId = otherAttachField.getFieldId();
			//Collection<ActionFileInfo> attachFileCollection = null;
			if(request != null && actionFileHash != null && user != null)
			{
				ArrayList<Role> userRoles = Role.lookupRolesBySystemIdAndUserId(request.getSystemId(), user.getUserId());
				for(Role uRole : userRoles)
				{
					if(uRole.getRoleName() == "Manager")
					{
						for(ActionFileInfo afi: latestAtts)
						{
							if(afi.getFieldId() == attachmentFieldId)
							{
								afi.setPriority(priority);
								priority = priority+1;
							}
							else if(afi.getFieldId() == otherAttachmentFieldId)
							{
								afi.setPriority(-1);
							}
						}
					}
					else
					{
						for(ActionFileInfo afi: latestAtts)
						{
							afi.setPriority(-1);
						}
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
		return "Rule where files of other_attachment go as a link and vice-versa if user is in a particular-role and as an attachment if user is not in that particular role";
	}

	@Override
	public double getMailPreProcessorOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}

