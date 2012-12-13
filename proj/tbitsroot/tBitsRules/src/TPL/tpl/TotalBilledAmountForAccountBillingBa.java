package tpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class TotalBilledAmountForAccountBillingBa implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub
		int sysId = ba.getSystemId();
		String sysPrefix = ba.getSystemPrefix();
		if(sysId ==37 && sysPrefix.equalsIgnoreCase("Acc_Billing"))
		{	
			String refNo = currentRequest.get("DTNNumber");
			String sql = "SELECT request_id FROM requests_ex where sys_id = ? and field_id=? and varchar_value=?";
			PreparedStatement ps;
			ArrayList reqList;
	            try {
	   			    ps = connection.prepareStatement(sql);
		            ps.setInt(1, 36);
		            ps.setInt(2, 40);
					ps.setString(3, refNo);
					reqList = new ArrayList();
			            ResultSet rs = ps.executeQuery();
			            while (rs.next())
			            {
			              reqList.add(Integer.valueOf(rs.getInt("request_id")));
			            }
			            rs.close();
			            ps.close();
			            
			            double a = 0.0;
			            ArrayList<Request> reqListLogistic=null;
						try {
							reqListLogistic = Request.lookupBySystemIdAndRequestIdList(36, reqList);
						} catch (DatabaseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            for (Request req : reqListLogistic)
			            {
			            	String var = req.getObject("BilledAmount").toString();
			            	double RefNo = Double.valueOf(var).doubleValue();
			              a = a + RefNo;
			            }
			            currentRequest.setObject("BilledAmount", a);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
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
