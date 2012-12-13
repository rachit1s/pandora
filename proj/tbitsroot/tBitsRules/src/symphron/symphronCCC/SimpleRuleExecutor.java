package symphronCCC;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
/**
 * This class executes the rules defined in "plugin_simple_rules" table for a particular condition.
 * There must be a Application property named "plugins.cmcc.rules.baList" in tbits admin panel which
 * contains the concerned BA who follow that rule.
 * 
 * @author sajal
 *
 */
public class SimpleRuleExecutor implements IRule {

	private static final int DO_NOT_EXECUTE_ON_ADDREQUEST 	= 1;
	private static final int EXECUTE_ONLY_ON_ADDREQUEST		= 2;
	private static final int EXECUTE_ALWAYS					= 3;
	private static final String NOTEMPTY 					= "notempty";
	private static final String EMPTY 						= "empty";
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		RuleResult ruleResult = new RuleResult();
		String baListStr = PropertiesHandler.getProperty(CCCUtils.PLUGIN_CMCC_RULES_BALIST);
		boolean isApplicableBA = CCCUtils.isApplicableBA(baListStr, ba);
		
		if (!(isApplicableBA || isAddRequest)){
			return ruleResult;
		}
		
		List<SimpleRule> rulesList = null;
		try {
			rulesList = SimpleRule.lookupSourceTargetFieldInfo(
														connection, ba.getSystemId());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if ((rulesList == null) || (rulesList.isEmpty())){
			return ruleResult;
		}		
		//Run the rules
		runRules(rulesList, oldRequest, currentRequest, user, isAddRequest);		
		return ruleResult;
	}

	@SuppressWarnings("unchecked")
	private void runRules(List<SimpleRule> rulesList, Request oldRequest,
			Request currentRequest, User user, boolean isAddRequest) {
		try {
			for (SimpleRule rule : rulesList){
				if (rule != null){
					
					switch (rule.getExecutionEvent()){
						case DO_NOT_EXECUTE_ON_ADDREQUEST:{
							if (isAddRequest) continue;
							break;
						}
						case EXECUTE_ONLY_ON_ADDREQUEST:{
							if (!isAddRequest) continue;
								break;
						}
						case EXECUTE_ALWAYS: break;
					}
					
					Field srcField = Field.lookupBySystemIdAndFieldId(rule.getSystemId(), rule.getSourceFieldId());
					Field targetField = Field.lookupBySystemIdAndFieldId(rule.getSystemId(), rule.getTargetFieldId());
					String sourceFieldValue = rule.getSourceFieldValue();
					boolean isConditionTrue = false;
					
					if ((srcField != null) && (targetField != null))
						switch(srcField.getDataTypeId()){
							case DataType.BOOLEAN:{
								boolean currentSrcFieldValue = (Boolean)currentRequest.getObject(srcField.getName());
								if (Boolean.parseBoolean(sourceFieldValue) == currentSrcFieldValue){
									isConditionTrue = true;
								}
								break;
							}
							case DataType.INT:{
								isConditionTrue = false;
								break;
							}
							case DataType.ATTACHMENTS:{
								Collection<AttachmentInfo> attList = (Collection<AttachmentInfo>)currentRequest.getObject(
										srcField.getName());
								
								if (sourceFieldValue.trim().equals(EMPTY)){									
									if ((attList == null) || attList.isEmpty())
										isConditionTrue = true;
								}
								else if (sourceFieldValue.trim().equals(NOTEMPTY)){
									if ((attList != null) && (!attList.isEmpty()))
										isConditionTrue = true;
								}								
								break;
							}
							case DataType.DATE:
							case DataType.DATETIME:{
								isConditionTrue = false;
								break;
							}
							case DataType.TYPE:{
								Type curFieldType = (Type)currentRequest.getObject(srcField);
								if ((curFieldType != null) && (sourceFieldValue.trim().length() != 0)){
									List<String> typesList = Arrays.asList(sourceFieldValue.trim().split(","));
									if (typesList.contains(curFieldType.getName()))
										isConditionTrue = true;
								}								
								break;
							}
							case DataType.USERTYPE:{
								String curUsers = currentRequest.get(srcField.getName());
								if (sourceFieldValue.equals(EMPTY)){									
									if ((curUsers == null) || (curUsers.trim().length() == 0))
										isConditionTrue = true;
								}
								else if (sourceFieldValue.equals(NOTEMPTY)){
									if ((curUsers != null) && (curUsers.trim().length() != 0))
										isConditionTrue = true;
								}					
								
								break;
							}
							default: isConditionTrue = false;
						}
					if (isConditionTrue)
						setTargetFieldValue(rule, targetField, oldRequest, currentRequest, isAddRequest);
				}
			}//End of for loop
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void setTargetFieldValue(SimpleRule rule, Field targetField, 
			Request oldRequest,Request currentRequest, boolean isAddRequest){
		
		String targetFieldValue = rule.getTargetFieldValue();
		switch(targetField.getDataTypeId()){
			case DataType.BOOLEAN:{
				currentRequest.setObject(targetField, Boolean.parseBoolean(targetFieldValue));			
				break;
			}
			case DataType.INT:{
				break;
			}
			case DataType.ATTACHMENTS:{
				currentRequest.setObject(targetField, new ArrayList<AttachmentInfo>());
				break;
			}
			case DataType.DATE:
			case DataType.DATETIME:{
				break;
			}
			case DataType.TYPE:{
				try {
					Type type = Type.lookupAllBySystemIdAndFieldNameAndTypeName(
										currentRequest.getSystemId(), targetField.getName(), targetFieldValue);
					currentRequest.setObject(targetField, type);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				break;
			}
			case DataType.USERTYPE:{
				ArrayList<RequestUser> ruList = new ArrayList<RequestUser>();
				if (targetFieldValue.trim().equals("")) {				
					currentRequest.setObject(targetField, ruList);
				} else{
					for (String userLogin : targetFieldValue.split(",")){
						if (userLogin.trim().length() != 0){
							try {
								User user = User.lookupAllByUserLogin(userLogin);
								RequestUser ru = new RequestUser(currentRequest.getSystemId(),
										currentRequest.getRequestId(), user.getUserId(), 1, 
										false, targetField.getFieldId());
								ruList.add(ru);
							} catch (DatabaseException e) {
								e.printStackTrace();
							}
						}
					}
					currentRequest.setObject(targetField, ruList);
				}
				break;
			}
		}
	}
	
	@Override
	public String getName() {		
		return this.getClass().getSimpleName();
	}

	@Override
	public double getSequence() {
		return 100;
	}
}
