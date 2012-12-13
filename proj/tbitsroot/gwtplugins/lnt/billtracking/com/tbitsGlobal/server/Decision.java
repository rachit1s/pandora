package billtracking.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
/*
 * Domain object for plugins_decision_table
 */
public class Decision {
	
	String decisionFlow;
	String keyData;
	String valueData;
	Integer currentStateId;
	Integer nextStateId;
	Integer processId;
	Integer rejectionStateId;
	
	public String getDecisionFlow() {
		return decisionFlow;
	}

	public void setDecisionFlow(String decisionFlow) {
		this.decisionFlow = decisionFlow;
	}

	public String getKeyData() {
		return keyData;
	}

	public void setKeyData(String keyData) {
		this.keyData = keyData;
	}

	public String getValueData() {
		return valueData;
	}

	public void setValueData(String valueData) {
		this.valueData = valueData;
	}

	public Integer getCurrentStateId() {
		return currentStateId;
	}

	public void setCurrentStateId(Integer currentStateId) {
		this.currentStateId = currentStateId;
	}

	public Integer getNextStateId() {
		return nextStateId;
	}

	public void setNextStateId(Integer nextStateId) {
		this.nextStateId = nextStateId;
	}

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	public Integer getRejectionStateId() {
		return rejectionStateId;
	}

	public void setRejectionStateId(Integer rejectionStateId) {
		this.rejectionStateId = rejectionStateId;
	}

	public Decision(String decisionFlow, String keyData, String valueData,
			Integer currentStateId, Integer nextStateId, Integer processId,
			Integer rejectionStateId) {
		super();
		this.decisionFlow = decisionFlow;
		this.keyData = keyData;
		this.valueData = valueData;
		this.currentStateId = currentStateId;
		this.nextStateId = nextStateId;
		this.processId = processId;
		this.rejectionStateId = rejectionStateId;
	}
	/*
	 * returns a list of decisions corresponding to a particular state in a process
	 */
	public static List<Decision> lookUpByProcessIdAndCurrentStateId(int processId,int currentStateId){
		
		List<Decision> list = new ArrayList<Decision>();
		Connection aCon;
		try {
			aCon = DataSourcePool.getConnection();
			PreparedStatement pstmt = aCon.prepareStatement("Select * from plugins_decision_table  where process_id=? " +
			"and current_state_id=?");
			
			pstmt.setInt(1,processId);
			pstmt.setInt(2,currentStateId);
			
			ResultSet rs = pstmt.executeQuery();
			while(rs!=null && rs.next()!=false){
			String decisionFlow=Utils.fieldTrimmer(rs.getString("decision_flow"));
		    String keyData=Utils.fieldTrimmer(rs.getString("key_data"));
			String valueData=Utils.fieldTrimmer(rs.getString("value_data"));	
			int nextStateId=rs.getInt("next_state_id");
			int rejStateId=rs.getInt("rej_state_id");
			
			Decision decision= new Decision(decisionFlow, keyData, valueData, currentStateId, nextStateId, processId, rejStateId);
			list.add(decision);

			}
			rs.close();
			aCon.close();
			return list;


		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

}
