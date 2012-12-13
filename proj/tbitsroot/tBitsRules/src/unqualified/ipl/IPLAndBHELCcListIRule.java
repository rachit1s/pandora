/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * @author Lokesh
 *
 */
public class IPLAndBHELCcListIRule implements IRule {

	private static final String IPL_AND_BHEL_CC_LIST = "ipl.IPLAndBHELCcList";
	private static final String BHEL = "BHEL";
	private static final String INDIABULLS = "Indiabulls";

	/* (non-Javadoc)
	 * @see tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest, Collection<AttachmentInfo> attachments)
	{
		RuleResult ruleResult = new RuleResult();
		
		if (!isAddRequest)
			return ruleResult;

		if (ba.getSystemPrefix().trim().equals("NSKDTN") || ba.getSystemPrefix().trim().equals("AMRDTN"))
		{
			String originator = currentRequest.getCategoryId().getName();
			String recipient  =	currentRequest.getRequestTypeId().getName();

			if (((originator != null) && (originator.trim().length()!= 0))
					&& ((recipient != null) && (recipient.trim().length() != 0)))
			{			
				if ((originator.equals(INDIABULLS) && recipient.equals(BHEL))
						|| (originator.equals(BHEL) && recipient.equals(INDIABULLS)))
				{
					ArrayList<RequestUser> subscribers = currentRequest.getSubscribers();
					if (subscribers == null)
						subscribers = new ArrayList<RequestUser>();
					String ccList = PropertiesHandler.getProperty(IPL_AND_BHEL_CC_LIST);
					if ((ccList != null) && (ccList.trim().length() != 0))
					{
						for (String userLogin : ccList.split(",")){
							User mUser = null;
							try {
								mUser = User.lookupByUserLogin(userLogin);
							} catch (DatabaseException e) {
								e.printStackTrace();
							}
							if (mUser != null){
								RequestUser ru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.SUBSCRIBER,
										mUser.getUserId(), 1, false);
								if (!subscribers.contains(ru))
									subscribers.add(ru);
							}
						}
						currentRequest.setSubscribers(subscribers);
					}	
				}
			}
		}

		return ruleResult;
	}
	
		
	/* (non-Javadoc)
	 * @see tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
