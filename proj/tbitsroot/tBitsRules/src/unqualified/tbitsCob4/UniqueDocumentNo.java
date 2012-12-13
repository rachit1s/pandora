package tbitsCob4;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

public class UniqueDocumentNo implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		
		 final String COB4  = "cob4";
		 final String DwgNo = "Drawing_Number";
		 
		 
		 RuleResult ruleresult = new RuleResult();
		 
		 String sysPrefix = ba.getSystemPrefix();
		 int aSystemId = ba.getSystemId();
		 int RequestId = currentRequest.getRequestId();
		 
		 if(sysPrefix.equals(COB4))
		 {
		 
		     try {
			       Field DwgNoField = Field.lookupBySystemIdAndFieldName(aSystemId,DwgNo);
			       RequestEx DwgRex = extendedFields.get(DwgNoField);
			
			       if(DwgNoField == null)
			         {
				     ruleresult.setCanContinue(true);
				     ruleresult.setMessage("Dwg No fields is not exist");
			         }
			     else
			       {
				       if(DwgRex.getRealValue()== 0)
				          {
					         ruleresult.setCanContinue(true);
					         ruleresult.setMessage("dwg Number fields value is empty");
				          }
				       else 
				           {
					          connection = DataSourcePool.getConnection();
					          Statement stmt = null;
					          ResultSet rs = null;
					
					          String Query = " select r.request_id ,re.real_value from requests r "
                                    +"join business_areas ba on r.sys_id  = ba.sys_id " 
                                    +"join requests_ex re on r.sys_id = re.sys_id  and " 
                                   	+"r.request_id  = re.request_id  and re.field_id  =  49 "
                                    +"where ba.sys_prefix = 'cob4' ";
					
					          stmt = connection.createStatement();
					          stmt.executeQuery(Query);
					          rs =  stmt.getResultSet();
					          float DwgNumber = (float) DwgRex.getRealValue();
					
					          if(rs == null)
					            {
						          ruleresult.setCanContinue(true);
						          ruleresult.setMessage("no DWGNo. value in Data base for Cob4");
					             }
					          else
					              {
						            while(rs.next())
						              {
						           
							            if((RequestId!= rs.getInt(1))&& DwgNumber == rs.getFloat(2))
							                    {
							                      ruleresult.setCanContinue(false);
								                  ruleresult.setMessage("you can't insert Duplicate Dwg Number");
								                  break;
							                    }
						              }
					               }
					
					
		                  }
			        }
			
			    } 
		  catch (DatabaseException e) 
		    {
			 e.printStackTrace();
		     }
		   catch (SQLException e)
		    {
			  e.printStackTrace();
		    }
	   }
	 else
	  {
		ruleresult.setCanContinue(true);
		ruleresult.setMessage("this rule is not applicable for "+ ba.getName()+ " business Areas");
	   }
		 
		
		
		
		return ruleresult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "this rule is use to avoid the duplicate value in DwgNumber field";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
