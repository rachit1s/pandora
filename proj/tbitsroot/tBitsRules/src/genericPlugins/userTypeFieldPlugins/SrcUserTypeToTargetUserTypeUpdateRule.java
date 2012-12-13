package userTypeFieldPlugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;


/*
 *  Moves the user from source to target user type field if the user(current logger) has 
 *  approved/commented(or any similar value) the document/drawing/request.
 * 
 */

/*
 *  sys_id, conditionFieldId, conditionValue
 */
public class SrcUserTypeToTargetUserTypeUpdateRule implements IRule {
	
	private static final String TARGET_USER_TYPE_FIELD_ID = "target_user_type_field_id";
	private static final String SRC_USER_TYPE_FIELD_ID = "src_user_type_field_id";
	private static final String SRC_TO_TARGET_USER_FIELD_UPDATE_CONFIG	= "plugin_src_to_target_user_type_update_config";
	private static final String CONDITION_FIELD_VALUE 					= "condition_field_value";
	private static final String CONDITION_FIELD_ID 						= "condition_field_id";
	private static final String SYS_ID 									= "sys_id";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		
		int systemId = ba.getSystemId();		
		try {
			ArrayList<SrcToTargetUserTypeFieldUpdateConfig> atsConfigList = getATSConditionBySystemId(connection, systemId);
			if (atsConfigList != null){
				for (SrcToTargetUserTypeFieldUpdateConfig atsConfig : atsConfigList)
					executeCondition(systemId, currentRequest, user, atsConfig);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			ruleResult.setMessage(e.getMessage());
		}
				
		return ruleResult;
	}

	/**
	 * @param systemId
	 * @param currentRequest
	 * @param user
	 * @param atsCondition
	 * @throws DatabaseException
	 */
	private void executeCondition(int systemId, Request currentRequest,
			User user, SrcToTargetUserTypeFieldUpdateConfig atsCondition)
			throws DatabaseException {
		
		if (atsCondition != null){			
			Field srcUserTypeField = Field.lookupBySystemIdAndFieldId(systemId, atsCondition.srcUserTypeFieldId);
			Field targetUserTypeField = Field.lookupBySystemIdAndFieldId(systemId, atsCondition.targetUserTypeFieldId);
			Field conditionField = Field.lookupBySystemIdAndFieldId(systemId, atsCondition.conditionFieldId);
			
			if ((srcUserTypeField != null) 
					&& (targetUserTypeField != null)
					&& (conditionField != null)){
				
				String val = currentRequest.get(conditionField.getName());
				if ((val != null) && val.trim().equals(atsCondition.conditionFieldValue.trim())){
					
					HashSet<RequestUser> targetUsersSet = new HashSet<RequestUser>();
					Collection<RequestUser> srcUsers = (Collection<RequestUser>) currentRequest.getObject(
															srcUserTypeField);
					
					if ((srcUsers != null) && (!srcUsers.isEmpty())){													
						for (RequestUser ru : srcUsers){
							if ((ru != null)&& (user.getUserId() == ru.getUserId())){
								srcUsers.remove(ru);
								ru.setFieldId(targetUserTypeField.getFieldId());
								targetUsersSet.add(ru);
								break;
							}									
						}
						
						if (!targetUsersSet.isEmpty()){
							Collection<RequestUser> targetUsers = (Collection<RequestUser>)currentRequest.getObject(
																		targetUserTypeField);
							if (targetUsers != null){	
								targetUsersSet.addAll(targetUsers);
								targetUsers.clear();
							}
							else{
								targetUsers = new ArrayList<RequestUser>();									
							}
							targetUsers.addAll(targetUsersSet);
							currentRequest.setObject(targetUserTypeField.getName(), targetUsers);
							currentRequest.setObject(srcUserTypeField.getName(), srcUsers);
						}							
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName() + ": Moves user from source user type field to target user field based on a fields status.";
	}

	@Override
	public double getSequence() {
		return 0;
	}

	private ArrayList<SrcToTargetUserTypeFieldUpdateConfig> getATSConditionBySystemId(
			Connection connection, int systemId) throws DatabaseException{
		
		ArrayList<SrcToTargetUserTypeFieldUpdateConfig> configList = new ArrayList<SrcToTargetUserTypeFieldUpdateConfig>();		
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + SRC_TO_TARGET_USER_FIELD_UPDATE_CONFIG
					+ " WHERE " + SYS_ID + " = ?");
			ps.setInt(1, systemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while (rs.next()){
					SrcToTargetUserTypeFieldUpdateConfig atsConfig = new SrcToTargetUserTypeFieldUpdateConfig(rs.getInt(SYS_ID), 
							rs.getInt(CONDITION_FIELD_ID), rs.getString(CONDITION_FIELD_VALUE),
							rs.getInt(SRC_USER_TYPE_FIELD_ID), rs.getInt(TARGET_USER_TYPE_FIELD_ID));
					configList.add(atsConfig);
				}			
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), e);
		}		
		return configList;
	}
	
	private class SrcToTargetUserTypeFieldUpdateConfig{
		int systemId, conditionFieldId;
		String conditionFieldValue;
		int srcUserTypeFieldId, targetUserTypeFieldId;
		protected SrcToTargetUserTypeFieldUpdateConfig(int systemId, int conditionFieldId,
					String conditionFieldValue, int srcUserTypeFieldId, int targetUserTypeFieldId){
			this.systemId = systemId;
			this.conditionFieldId = conditionFieldId;
			this.conditionFieldValue = conditionFieldValue;
			this.srcUserTypeFieldId = srcUserTypeFieldId;
			this.targetUserTypeFieldId = targetUserTypeFieldId;
		}		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
