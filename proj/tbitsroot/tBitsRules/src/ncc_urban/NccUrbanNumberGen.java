package ncc_urban;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class NccUrbanNumberGen implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String sysPrefix=ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("INTL") && oldRequest==null)
		{
		
			String finalNumber="";
			Type subBusinessType=(Type) currentRequest.getObject("SubBusinessAreas");
			String subBusniessArea=subBusinessType.getDescription();
			Type departmentType=(Type) currentRequest.getObject("request_type_id");
			String department=departmentType.getDescription();
			String numberPaddingFormatter = "%1$04d";
			String drawingNumberPrefix=subBusniessArea+"-"+department;
			try {
				int runningNumber=getUniqDrawingNumber(connection, drawingNumberPrefix);
				finalNumber=drawingNumberPrefix+"-"+String.format(numberPaddingFormatter,runningNumber);
				currentRequest.setObject("TaskNumSys",finalNumber);
				return new RuleResult(true,"Task Number has been Generated: "+ finalNumber,true);
			} catch (SQLException e) {
				e.printStackTrace();
				return new RuleResult(true,"Not able to contact the database Hence number can not be generated",true);
			}
			
		}
		return new RuleResult();
	}
	
	public static int getUniqDrawingNumber(Connection conn,	String drawingNumberPrefix) throws SQLException {
		CallableStatement stmt = conn.prepareCall("stp_getAndIncrMaxId ?");
		stmt.setString(1, drawingNumberPrefix);
		ResultSet rs = stmt.executeQuery();
		if ((rs != null) && rs.next()) {
			int id = rs.getInt("max_id");
			return id;
		} else {
			throw new SQLException();
		}
	}

	@Override
	public String getName() {
		
		return "NccUrbanNumberGen";
	}

	@Override
	public double getSequence() {
		
		return 0;
	}

}
