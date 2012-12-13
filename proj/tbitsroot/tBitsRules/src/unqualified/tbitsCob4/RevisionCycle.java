package tbitsCob4;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class RevisionCycle implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {

		RuleResult ruleResult = new RuleResult();

		final String COB4 = "cob4";
		final String Rev = "Revision_Number";


		String SysPrefix = ba.getSystemPrefix();
		int aSystemId = ba.getSystemId();

		if(SysPrefix.equals(COB4))
		{


			try {
				Field RevField = Field.lookupBySystemIdAndFieldName(aSystemId, Rev);

				RequestEx CurrentRevRex = currentRequest.getExtendedFields().get(RevField);
				Type CurrentRevType =  currentRequest.getExType(Rev);
				int CurrentRevOrder =  CurrentRevType.getOrdering();




				RequestEx OldRevRex    = oldRequest.getExtendedFields().get(RevField);
				Type OldRevType      = oldRequest.getExType(Rev);
				int OldRevOrder = OldRevType.getOrdering();



				// current rev Number ordering should be one more then previous
				// we use ordering field from type Table for sequence of revision       
				int CompareOldRev = OldRevOrder + 1;



				if(RevField == null)
				{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("revision Field is not exists ");
				}
				else
				{
					if(OldRevOrder == CurrentRevOrder)
					{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("revision not modified");
					}
					else
					{
						if(CompareOldRev == CurrentRevOrder)
						{
							ruleResult.setCanContinue(true);
						}
						else
						{
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("revision is not applicable");
						}
					}
				}       

			}
			catch (DatabaseException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("rule is not applicabe for"+ ba.getName()+" this business Areas");
		}



		return ruleResult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "this rule is use to  maintained the revision sequence on each actions";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
