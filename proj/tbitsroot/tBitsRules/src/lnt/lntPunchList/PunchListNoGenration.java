package lntPunchList;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class PunchListNoGenration implements IRule {

	private static final String DELIMETER_HYPHEN = "-";
	private static final String PROJECT_CODE = "Project"; // request_type_id
	private static final String DISCIPLINE_CODE = "Discipline";
	private static final String PACKAGE_CODE = "Package";
	private static final String PUNCH_ITEM = "Punch Item #";
	private static final String PLUGIN_LNT_SO_RULES_BALIST = "plugin.PunchList.punchListNoGeneration.baList";
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		RuleResult ruleResult = new RuleResult();

		String baListStr = PropertiesHandler.getProperty(PLUGIN_LNT_SO_RULES_BALIST);
		boolean isApplicableBA = false;
		if (baListStr != null){
			List<String> baList = Arrays.asList(baListStr.trim().split(","));
			if (baList.contains(ba.getSystemPrefix()))
				isApplicableBA = true;
		}
		 if(isApplicableBA) {
		
		

			int aSysId = ba.getSystemId();
            if (isAddRequest) {

				Type projectType = (Type) currentRequest.getObject(PROJECT_CODE);
				Type disciplineType = (Type) currentRequest.getObject(DISCIPLINE_CODE);
				Type packageType = (Type) currentRequest.getObject(PACKAGE_CODE);

				if (projectType != null && disciplineType != null
						&& packageType != null) {

					String projectCode = projectType.getDescription();
					String disciplineCode = disciplineType.getDescription();
					String packageCode = packageType.getDescription();
           // punchList item No generation
					String PunchlistPrefix = projectCode + DELIMETER_HYPHEN + "PL"
					                         + DELIMETER_HYPHEN + disciplineCode
					                         + DELIMETER_HYPHEN + packageCode + DELIMETER_HYPHEN
					                         + "00";
					
					
							
					String PunchListNo = PunchlistPrefix + getSerialNo(PunchlistPrefix);
					
					       
					
					System.out.println("PunchListItem code :" + PunchlistPrefix);
					System.out.println("PunchListItem code :" + PunchListNo);
					try {
						currentRequest.setExString(PUNCH_ITEM, PunchListNo);
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

		return ruleResult;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		return 0;
	}


// method to get the serial No for next PunchList item No & and increment id with 1.
	private int getSerialNo(String PunchListPrefix) {
		Connection conn = null;
	    CallableStatement cstmt = null;
		ResultSet rs = null;
		int serialNo = 0;
		try {
			conn = DataSourcePool.getConnection();
			cstmt = conn.prepareCall("{call stp_getAndIncrMaxId(?)}");
			cstmt.setString(1, PunchListPrefix);
		    rs = cstmt.executeQuery();
			while (rs.next()) {
			//	System.out.println("new max ids from max_id table:"+rs.getInt(1));
				serialNo = rs.getInt(1);
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				cstmt.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return serialNo;

	}
	

}
