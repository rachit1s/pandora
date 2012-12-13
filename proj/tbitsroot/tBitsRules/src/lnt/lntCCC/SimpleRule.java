/**
 * 
 */
package lntCCC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import transbit.tbits.common.DatabaseException;

/**
 * @author Lokesh
 *
 */
public class SimpleRule {
	
	public SimpleRule(int ruleId, int systemId, int sourceFieldId, 
			String sourceFieldValue, int targetFieldId, 
			String targetFieldValue, int executionOrder, 
			int executionEvent) {
	
		this.setRuleId(ruleId);
		this.systemId = systemId;
		this.sourceFieldId = sourceFieldId;
		this.sourceFieldValue = sourceFieldValue;
		this.targetFieldId = targetFieldId;
		this.targetFieldValue = targetFieldValue;
		this.executionOrder = executionOrder;
		this.executionEvent = executionEvent;
	}

	private int systemId, sourceFieldId, targetFieldId, ruleId, executionOrder, executionEvent;
	private String sourceFieldValue, targetFieldValue;
	
	public int getRuleId() {
		return ruleId;
	}
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}	
	public int getSystemId() {
		return systemId;
	}
	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}
	public int getSourceFieldId() {
		return sourceFieldId;
	}
	public void setSourceFieldId(int sourceFieldId) {
		this.sourceFieldId = sourceFieldId;
	}
	public int getTargetFieldId() {
		return targetFieldId;
	}
	public void setTargetFieldId(int targetFieldId) {
		this.targetFieldId = targetFieldId;
	}
	public String getSourceFieldValue() {
		return sourceFieldValue;
	}
	public void setSourceFieldValue(String sourceFieldValue) {
		this.sourceFieldValue = sourceFieldValue;
	}
	public String getTargetFieldValue() {
		return targetFieldValue;
	}
	public void setTargetFieldValue(String targetFieldValue) {
		this.targetFieldValue = targetFieldValue;
	}	
	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}
	public int getExecutionOrder() {
		return executionOrder;
	}
	
	public void setExecutionEvent(int executionEvent) {
		this.executionEvent = executionEvent;
	}
	public int getExecutionEvent() {
		return executionEvent;
	}
	public static List<SimpleRule> lookupSourceTargetFieldInfo(
			Connection connection, int systemId) throws DatabaseException{
		
		ArrayList<SimpleRule> simpleRuleList = new ArrayList<SimpleRule>();
		String query = "SELECT * FROM plugin_simple_rules where sys_id=?";
		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, systemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					SimpleRule sr = new SimpleRule(rs.getInt("rule_id"), rs.getInt("sys_id"),
										rs.getInt("condition_field_id"), rs.getString("condition_field_value"),
										rs.getInt("target_field_id"), rs.getString("target_field_value"),
										rs.getInt("execution_order"), rs.getInt("execution_event_id"));
					simpleRuleList.add(sr);
				}
				Collections.sort(simpleRuleList, comparator);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while fetching simple rules for system id: " + systemId, e);
		}		
		return simpleRuleList;		
	}
	
	static Comparator<SimpleRule> comparator = new Comparator<SimpleRule>(){
		public int compare(SimpleRule arg0, SimpleRule arg1) {
			int diff = arg0.getExecutionOrder() - arg1.getExecutionOrder();
			if(diff > 0)
				return 1;
			else if(diff == 0)
				return 0;
			else 
				return -1;
		}
	};
}
