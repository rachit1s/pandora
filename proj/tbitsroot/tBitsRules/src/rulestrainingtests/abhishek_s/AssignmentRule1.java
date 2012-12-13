package sharan;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class AssignmentRule1  implements IRule {
	
	//we need a logger to login in the system.
	//a business area 
	//field
	//type
	
	public static TBitsLogger logger =  TBitsLogger.getLogger("sharan");

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		//verifying whether the business area is correct or not
		
		if(!ba.getSystemPrefix().equalsIgnoreCase("testba"))
			return new RuleResult();
		
		int sysid = ba.getSystemId();
		String statusobj=currentRequest.get(Field.STATUS);

		try
		{
		if(statusobj.equals("Pending"))
		{
	
			Type fieldtype = Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysid, Field.STATUS, "Closed");
			Field fieldname = Field.lookupBySystemIdAndFieldName(sysid,Field.STATUS);
			currentRequest.setObject(fieldname,fieldtype);
		}
		 
			else if (statusobj.equals("Closed"))
			{
				
				Type fieldtype=Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysid, Field.STATUS,"Pending");
				Field fieldname=Field.lookupBySystemIdAndFieldName(sysid, Field.STATUS);
				currentRequest.setObject(fieldname,fieldtype);								
						
			}
		}
		catch(Exception e)
		{
			return new RuleResult(true,"check ur values",true);
		}
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
