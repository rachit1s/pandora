package commons.com.tbitsGlobal.utils.client.rules;

/**
 * This class encapsulates a standard java variable.
 * 
 * @author karan
 *
 */
public class VarDef implements java.io.Serializable{


	//================================================================================

	private static final long serialVersionUID = 1L;
	
	public String modifiers;
	public String varName;
	public String varType;
	
	//================================================================================
	
	/**
	 * Constructor
	 */
	public VarDef(){
		modifiers = "";
		varName = "";
		varType = "";
	}

	//================================================================================
}
