package transbit.tbits.rules;

/**
 * This class encapsulates the basic information required for java rules
 * 
 * @author karan
 *
 */
public class RuleClass {

	//================================================================================

	private Class<?> c;
	private String name;
	private String type;
	private double seq_no;

	//================================================================================

	/**
	 * Constructor
	 * @param name
	 * @param type
	 * @param seq_no
	 */
	public RuleClass(String name, String type, double seq_no){
		this.name = name;
		this.type = type;
		this.seq_no = seq_no;
	}
	
	public RuleClass(String name){
		this.name = name;
		this.type = "temp";
		this.seq_no = -10.0;
	}

	//================================================================================

	// Getter and setter methods
	public String getName(){
		return name;
	}
	
	public String getType(){
		return type;
	}
	
	public double getSequenceNumber(){
		return seq_no;
	}
	
	public Class<?> getCls(){
		return c;
	}
	
	public void setCls(Class<?> cls){
		c = cls;
	}
	
	//================================================================================

}
