package kskMom;

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

public class MomNoGenration implements IRule {

	private static final String DELIMETER_HYPHEN = "-";
	
	private static final String PROJECT_CODE = "P-KM"; // request_type_id
	private static final String Record_TYPE = "recordtype";
	private static final String MOM_NO = "kmpclid";
	
	private static final String PLUGIN_KSK_MOM_SN_RULES_BALIST = "plugin.MomNoGeneration.baList";
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		RuleResult ruleResult = new RuleResult();

		String baListStr = PropertiesHandler.getProperty(PLUGIN_KSK_MOM_SN_RULES_BALIST);
		boolean isApplicableBA = false;
		if (baListStr != null){
			List<String> baList = Arrays.asList(baListStr.trim().split(","));
			if (baList.contains(ba.getSystemPrefix()))
				isApplicableBA = true;
		}
		 if(isApplicableBA) {
		
			 System.out.println("Mom no is executed here..&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

			int aSysId = ba.getSystemId();
            if (isAddRequest) {

				
				Type RecordType = (Type) currentRequest.getObject(Record_TYPE);
			    
				if(RecordType.getName().equalsIgnoreCase("Action Item") ||RecordType.getName().equalsIgnoreCase("Agenda Item"))
				{
					
					 System.out.println("Record Type :"+RecordType.getName());
           // MOM item No generation
					String MomNoPrefix = PROJECT_CODE   //+ disciplineCode
					                         + "-000";
					
					
							
					String MomSrNo = MomNoPrefix + getSerialNo(MomNoPrefix);
					
					       
					
					System.out.println("PunchListItem code :" + MomNoPrefix);
					System.out.println("PunchListItem code :" + MomSrNo);
					try {
						currentRequest.setExString(MOM_NO, MomSrNo);
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


// method to get the serial No for next MOM  item No & and increment id with 1.
	private int getSerialNo(String MomNoPrefix) {
		Connection conn = null;
	    CallableStatement cstmt = null;
		ResultSet rs = null;
		int serialNo = 0;
		try {
			conn = DataSourcePool.getConnection();
			cstmt = conn.prepareCall("{call stp_getAndIncrMaxId(?)}");
			cstmt.setString(1, MomNoPrefix);
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
