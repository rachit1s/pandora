package commons.com.tbitsGlobal.utils.client.rules;

import commons.com.tbitsGlobal.utils.client.rules.ClassDef;

/**
 * This class encapsulates a java rule.
 * 
 * @author karan
 *
 */
public class RuleDef implements java.io.Serializable{

	//================================================================================

	private static final long serialVersionUID = 1L;

	private String name;
	private String type;
	private double seq_no;
	private ClassDef classDef;
	private String ruleCode;

	//================================================================================

	/**
	 * Constructors
	 */
	public RuleDef(){
		name = "";
		type = null;
		seq_no = -1.0;
		classDef = new ClassDef();
		ruleCode = null;
	}
	
	public RuleDef(String iClass){
		name = "";
		type = null;
		seq_no = -1.0;
		classDef = new ClassDef();
		classDef.setImplementsClass(iClass);
		ruleCode = null;
	}
	
	//================================================================================

	// Getter and setter functions
	
	public void setImplementsClass(String iClass){
		classDef.setImplementsClass(iClass);
	}
	
	public void setName(String rName) {
		this.name = rName;
	}
	
	public String getName() {
		return name;
	}

	public void setType(String rType) {
		this.type = rType;
	}

	public String getType() {
		return type;
	}

	public void setSeqNo(double seq_no) {
		this.seq_no = seq_no;
	}

	public double getSeqNo() {
		return seq_no;
	}

	public ClassDef getClassDef() {
		return classDef;
	}

	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}

	public String getRuleCode() {
		return ruleCode;
	}
	
	//================================================================================

}
