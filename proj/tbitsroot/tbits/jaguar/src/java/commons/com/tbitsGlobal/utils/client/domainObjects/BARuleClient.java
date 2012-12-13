package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for BARule
public class BARuleClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public BARuleClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	// public static String RULE = "rule";
	public static String RULE_ID = "rule_id";
	public static String SEQUENCE_NUMBER = "sequence_number";
	public static String SYSTEM_ID = "system_id";

	// getter and setter methods for variable myRule
	// public WorkflowRule getRule (){
	// return (WorkflowRule) this.get(RULE);
	// }
	// public void setRule(WorkflowRule myRule) {
	// this.set(RULE, myRule);
	// }

	// getter and setter methods for variable myRuleId
	public int getRuleId() {
		return (Integer) this.get(RULE_ID);
	}

	public void setRuleId(int myRuleId) {
		this.set(RULE_ID, myRuleId);
	}

	// getter and setter methods for variable mySequenceNumber
	public int getSequenceNumber() {
		return (Integer) this.get(SEQUENCE_NUMBER);
	}

	public void setSequenceNumber(int mySequenceNumber) {
		this.set(SEQUENCE_NUMBER, mySequenceNumber);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

}