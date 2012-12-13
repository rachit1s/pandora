package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for WorkflowRule
public class WorkflowRuleClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public WorkflowRuleClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String BUSINESS_RULE = "business_rule";
	public static String NAME = "name";
	public static String RULE_DEFINITION = "rule_definition";
	public static String RULE_ID = "rule_id";

	// getter and setter methods for variable myBusinessRule
	// public BusinessRule getBusinessRule (){
	// return (BusinessRule) this.get(BUSINESS_RULE);
	// }
	// public void setBusinessRule(BusinessRule myBusinessRule) {
	// this.set(BUSINESS_RULE, myBusinessRule);
	// }

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable myRuleDefinition
	public String getRuleDefinition() {
		return (String) this.get(RULE_DEFINITION);
	}

	public void setRuleDefinition(String myRuleDefinition) {
		this.set(RULE_DEFINITION, myRuleDefinition);
	}

	// getter and setter methods for variable myRuleId
	public int getRuleId() {
		return (Integer) this.get(RULE_ID);
	}

	public void setRuleId(int myRuleId) {
		this.set(RULE_ID, myRuleId);
	}

}