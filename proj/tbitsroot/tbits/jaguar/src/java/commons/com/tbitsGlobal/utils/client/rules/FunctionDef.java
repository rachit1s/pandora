package commons.com.tbitsGlobal.utils.client.rules;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.rules.VarDef;

/**
 * This class encapsulates a standard java function.
 * 
 * @author karan
 *
 */
public class FunctionDef implements java.io.Serializable{
	

	//================================================================================

	private static final long serialVersionUID = 1L;
	
	public String name;
	public String modifiers;
	public String returnType;
	public String description;
	public String code;
	public ArrayList<VarDef> params;
	private int argCount;

	//================================================================================
	
	/**
	 * Constructor
	 */
	public FunctionDef(){
		name = "";
		modifiers = "";
		returnType = "";
		description = "";
		code = "";
		params = new ArrayList<VarDef>();
		argCount = 0;
	}
	
	//================================================================================
	
	public int getArgCount(){
		return argCount++;
	}
	
}
