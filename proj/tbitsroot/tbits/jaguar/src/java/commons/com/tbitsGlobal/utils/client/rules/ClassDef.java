package commons.com.tbitsGlobal.utils.client.rules;

import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.rules.FunctionDef;
import commons.com.tbitsGlobal.utils.client.rules.VarDef;

/**
 * This class encapsulates a standard java class.
 * 
 * @author karan
 *
 */
public class ClassDef implements java.io.Serializable{
	
	//================================================================================

	private static final long serialVersionUID = 1L;

	private String name;
	private String implementsClass;
	private List<String> imports;
	private List<VarDef> vars;
	private List<FunctionDef> functions;

	//================================================================================

	/**
	 * Constructors
	 */
	public ClassDef(String name, String iClass){
		this.name = name;
		this.implementsClass = iClass;
		imports = new ArrayList<String>();
		vars = new ArrayList<VarDef>();
		functions = new ArrayList<FunctionDef>();
	}
	
	public ClassDef(){
		this.name = null;
		this.implementsClass = null;
		imports = new ArrayList<String>();
		vars = new ArrayList<VarDef>();
		functions = new ArrayList<FunctionDef>();
	}
	
	//================================================================================

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setImplementsClass(String implementsClass) {
		this.implementsClass = implementsClass;
	}
	
	public String getImplementsClass() {
		return implementsClass;
	}
	
	public void setFunctions(List<FunctionDef> functions) {
		this.functions = functions;
	}
	
	public List<FunctionDef> getFunctions() {
		return functions;
	}

	public void setImports(List<String> imports) {
		this.imports = imports;
	}

	public List<String> getImports() {
		return imports;
	}

	public void setVars(List<VarDef> vars) {
		this.vars = vars;
	}

	public List<VarDef> getVars() {
		return vars;
	}

	//================================================================================

}
