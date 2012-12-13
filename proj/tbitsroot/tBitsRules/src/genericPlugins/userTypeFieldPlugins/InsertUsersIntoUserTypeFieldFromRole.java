package userTypeFieldPlugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;


/*
 * 1. sys_id, role_id, condition_field_id, condition_field_value, user_type_field_id.
 * 
 */

public class InsertUsersIntoUserTypeFieldFromRole implements IRule {

	private static final String ONLY_ON_ADD_REQUEST = "only_on_add_request";
	private static final String USER_TYPE_FIELD_UPDATE_CONFIG		= "plugin_user_type_field_update_config";
	private static final String USER_TYPE_FIELD_ID 					= "user_type_field_id";
	private static final String CONDITION_FIELD_VALUE 				= "condition_field_value";
	private static final String CONDITION_FIELD_ID 					= "condition_field_id";
	private static final String ROLE_ID 							= "role_id";
	private static final String SYS_ID 								= "sys_id";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		
		try {
			int systemId = ba.getSystemId();
			ArrayList<UserTypeFieldUpdateCondition> conditionsList = getUTFUConditionBySystemId(connection, systemId);
			if (conditionsList != null){
				for (UserTypeFieldUpdateCondition utfuCondition : conditionsList){
					if ((utfuCondition != null) && (systemId == utfuCondition.systemId)){
						if ((utfuCondition.isOnlyOnAddRequest) && (!isAddRequest))
							continue;
						else
							executeCondition(systemId, currentRequest, utfuCondition);
					}			
				}
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			ruleResult.setCanContinue(true);
		}
		
		return ruleResult;
	}


	/**
	 * @param systemId
	 * @param currentRequest
	 * @param utfuCondition
	 * @throws DatabaseException
	 */
	private void executeCondition(int systemId, Request currentRequest,
			UserTypeFieldUpdateCondition utfuCondition)
			throws DatabaseException {
		Field conditionField = Field.lookupBySystemIdAndFieldId(systemId, utfuCondition.conditionFieldId);				
		if ((conditionField != null) && 
				(utfuCondition.conditionFieldValue.trim().equals(currentRequest.get(conditionField.getName())))){
			
			Field userTypeField = Field.lookupBySystemIdAndFieldId(systemId, utfuCondition.userTypeFieldId);
			ArrayList<RoleUser> roleUserList = RoleUser.lookupBySystemIdAndRoleId(systemId, utfuCondition.roleId);
			if ((roleUserList != null) && (!roleUserList.isEmpty())){				
				if (userTypeField != null){					
					ArrayList<RequestUser> reqUserList = new ArrayList<RequestUser>();
					for (RoleUser roleUser : roleUserList){
						if (roleUser != null){
							RequestUser reqUser = new RequestUser(systemId, currentRequest.getRequestId(),
														roleUser.getUserId(), 1, false, userTypeField.getFieldId());
							reqUserList.add(reqUser);
						}
					}
					currentRequest.setObject(userTypeField.getName(), reqUserList);							
				}
			}
		}
	}
	

	@Override
	public String getName() {
		return getClass().getSimpleName() + ": Updates users into user type fields from a role.";
	}

	@Override
	public double getSequence() {
		return 0;
	}


	private ArrayList<UserTypeFieldUpdateCondition> getUTFUConditionBySystemId(
			Connection connection, int systemId) throws DatabaseException{
		
		ArrayList<UserTypeFieldUpdateCondition> conditionsList = new ArrayList<UserTypeFieldUpdateCondition>();
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("SELECT * FROM " + USER_TYPE_FIELD_UPDATE_CONFIG
					+ " WHERE " + SYS_ID + " = ?");
			ps.setInt(1, systemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while (rs.next()){
					UserTypeFieldUpdateCondition utfuCondition = new UserTypeFieldUpdateCondition(
							rs.getInt(SYS_ID), rs.getInt(ROLE_ID), 
							rs.getInt(CONDITION_FIELD_ID), 
							rs.getString(CONDITION_FIELD_VALUE),
							rs.getInt(USER_TYPE_FIELD_ID), 
							rs.getBoolean(ONLY_ON_ADD_REQUEST));
					conditionsList.add(utfuCondition);
				}			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), e);
		}		
		return conditionsList;
	}
	
	private class UserTypeFieldUpdateCondition{
		int systemId, roleId, conditionFieldId, userTypeFieldId;
		String conditionFieldValue;
		boolean isOnlyOnAddRequest;
		protected UserTypeFieldUpdateCondition(int systemId, int roleId, int conditionFieldId,
					String conditionFieldValue, int userTypeFieldId, boolean isOnlyOnAddRequest){
			this.systemId = systemId;
			this.roleId   = roleId;
			this.conditionFieldId = conditionFieldId;
			this.conditionFieldValue = conditionFieldValue;
			this.userTypeFieldId = userTypeFieldId;
			this.isOnlyOnAddRequest = isOnlyOnAddRequest;
		}		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
