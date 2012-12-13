package gmrCorr;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.Type;


public class SingleAssignee implements IRule{
	
	
	/*
	 * 
	 * During submit correspondence error pops up as ”Please select only one Assignee ” or “Please select the appropriate Assignee for the corr protocol 
	 * selected”. For appropriate assignee, 
	 * the plugin will check the Location property of the user selected in assignee field and matches it with description property of the type value 
	 * selected of the corr Protocol field.
	 */
public static TBitsLogger LOG = TBitsLogger.getLogger("gmrRules.corr");
private String sysPrefix="";
@Override
public RuleResult execute(Connection connection, BusinessArea ba,
Request oldRequest, Request currentRequest, int Source, User user,
boolean isAddRequest) {
	
	sysPrefix = ba.getSystemPrefix();
if(sysPrefix.equalsIgnoreCase("GMR_Corr"))
{
String corrProFieldName = "category_id";
String toFieldName = "assignee_ids";
Collection<RequestUser> assignees=currentRequest.getAssignees();
if(assignees.size()==1)
{
try
{
String toField = toFieldName;
Field assigneeField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), toField);

if(assigneeField!=null)
	{ 
		Type corrProtocol=currentRequest.getCategoryId();
		String discription=corrProtocol.getDescription();

		String	strassignee =(String)currentRequest.get(Field.ASSIGNEE);
		User u = User.lookupAllByUserLogin(strassignee);
		String userLocation=u.getLocation();
		if(discription.equals(userLocation))
		{
			return new RuleResult();
		}
	}
}
catch(Exception e)
{
LOG.error(TBitsLogger.getStackTrace(e));
return new RuleResult(false,"Rule with name \"" + getName() + "\" failed with message : " + e.getMessage(),false);
}
}
else if(assignees.size()>1)
{
return new RuleResult(false,"You can select only one assignee",true);
}
else{
return new RuleResult(false,"Please select only one assignee",true);
}
}
return new RuleResult();
}

@Override
public double getSequence() {
// TODO Auto-generated method stub
return 1.0;
}

@Override
public String getName() {
// TODO Auto-generated method stub
return "SingleAssignee";
}

}