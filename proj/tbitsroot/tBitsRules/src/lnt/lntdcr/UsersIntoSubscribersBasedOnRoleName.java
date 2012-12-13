/**
 * 
 */
package lntdcr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class UsersIntoSubscribersBasedOnRoleName implements IRule {

	private static final String FIELD_DISCIPLINE = "Discipline";
	private static final String FIELD_GENERATION_AGENCY = "GenerationAgency";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult(true, "Not applicable to this business area: " + ba.getSystemPrefix());
		
		if (ba.getSystemPrefix().equalsIgnoreCase("KDI_LNT"))
		{
			String disciplineType = currentRequest.get(FIELD_DISCIPLINE);
			String generationAgency = currentRequest.get(FIELD_GENERATION_AGENCY);
			if (((disciplineType != null) && (!disciplineType.equals("-")))
					&& ((generationAgency != null) && (generationAgency.equals("LTSL")))){
				try {
					Field subscriberField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), Field.SUBSCRIBER);
					Role disciplineRole = Role.lookupBySystemIdAndRoleName(ba.getSystemId(), disciplineType);
					if ((disciplineRole != null) && (subscriberField != null))
					{
						ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(ba.getSystemId(), disciplineRole.getRoleId());
						if ((roleUsers != null) && (!roleUsers.isEmpty()))
						{
							for(RoleUser roleUser : roleUsers)
							{
								Collection<RequestUser> subscribers = currentRequest.getSubscribers();
								if (subscribers != null)
								{
									Iterator<RequestUser> ruIter = subscribers.iterator();
									if (ruIter != null)
									{
										boolean isExists = false;
										while(ruIter.hasNext())
										{
											RequestUser ru = ruIter.next();
											if ((ru != null) && (ru.getUserId() == roleUser.getUserId())){
												isExists = true;
												break;
											}
										}
										if (!isExists){
											RequestUser newReqUser = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(),
													roleUser.getUserId(), 1, false, subscriberField.getFieldId());
											subscribers.add(newReqUser);
										}
									}

									currentRequest.setSubscribers(subscribers);
									ruleResult.setMessage("Successfully added users into subscribers list.");
								}
							}
						}
					}
					else{
						ruleResult.setMessage("No role was found for the current discipline, hence no applying this rule.");
					}
				} catch (DatabaseException e) {
					e.printStackTrace();
					ruleResult.setMessage("Database error occurred while fetching role based on the discipline type.");
				}
			}
			else{
				ruleResult.setMessage("Not applicable to for this discipline: " + disciplineType);
			}
		}
		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "- Add users belonging to a particular role based on the type of \"Discipline\" to subscribers," +
				" so that they get mails on addition or update of a request.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getSequence()
	 */
	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
