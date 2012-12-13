package kskorg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

public class OrgNonRepeatedAssignee implements IRule {

	
	
	
	  private static final String ORG = "org";
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {
		  
		
		  RuleResult ruleresult = new RuleResult();
		  String Sysprefix = ba.getSystemPrefix();
		  int RequestId = currentRequest.getRequestId();
		  
		if(Sysprefix.equalsIgnoreCase(ORG))
		  {
		  
			       Collection<RequestUser> assignee = currentRequest.getAssignees();
			       Iterator<RequestUser> i=assignee.iterator();
			       int AssigneeId = i.next().getUserId();
		  
			//	  System.out.println("current assignee:"+assignee.toString());
				  System.out.println("current assignee ID:"+ AssigneeId);
				  
				  // ArrayList<String> assigneeList = new ArrayList<String>();
		  
		
				  Statement stmt = null;
				  ResultSet rs = null;
          
		       try
		        {   
			  
		    	  connection = DataSourcePool.getConnection();
	          
		    	  String Query = "select request_id,r.user_id ,u.display_name from request_users r "
						     +"join business_areas ba on ba.sys_id  = r.sys_id "
						     +"join users u on u.user_id  = r.user_id "
						     +"where ba.sys_prefix = 'Org' and r.user_type_id = 3";
	          
	          
		    	  stmt = connection.createStatement();
		    	  stmt.executeQuery(Query);
		    	  rs =  stmt.getResultSet();
	           
	         
		    	  while(rs.next())
		    	  	{   
		    		 // System.out.println(rs.getInt(2));
		    		 // System.out.println(rs.getInt(1));
		    		  	if((RequestId != rs.getInt(1))&& (AssigneeId == rs.getInt(2)))
		    		  		{
		    		  			ruleresult.setCanContinue(false);
		    		  			ruleresult.setMessage("this user is assignee  previously in request id :#"+rs.getInt(1));
		    		  			break;
		    		  	     }
		    	  	}
	          
		       }
		  catch (SQLException e)
		    {
			  e.printStackTrace();
		    }
		  finally
		    {
			  try
				{
					rs.close();
					stmt.close();
					connection.close();
				}
				catch(SQLException e)
				{
					
					e.printStackTrace();
				}
		    }
		  }
		else
			ruleresult.setCanContinue(true);
		return ruleresult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "non repeating Assignee";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
