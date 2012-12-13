package commons.com.tbitsGlobal.utils.server.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;

import commons.com.tbitsGlobal.utils.client.rules.RulesTemplate;
import commons.com.tbitsGlobal.utils.client.rules.ClassDef;
import commons.com.tbitsGlobal.utils.client.rules.FunctionDef;
import commons.com.tbitsGlobal.utils.client.rules.RuleDef;
import commons.com.tbitsGlobal.utils.client.rules.VarDef;
import commons.com.tbitsGlobal.utils.server.rules.RulesTemplateRegistry;

/**
 * This class registers all the rule templates for the creation of rules from the admin panel.
 * The register method needs to be called with the interface's class object for the intended template.
 * 
 * @author Karan Gupta
 *
 */
public class RulesTemplateRegistry {

	//================================================================================

	private static RulesTemplateRegistry instance;
	private HashMap<Class<?>, RuleDef> registry;

	//================================================================================

	/**
	 * Private constructor for the singleton class
	 */
	private RulesTemplateRegistry(){
		registry = new HashMap<Class<?>, RuleDef>();
		this.register(IRule.class, null);
		this.register(IPostRule.class, null);
	}
	
	/**
	 * @return singleton instance of the registry
	 */
	public static RulesTemplateRegistry getInstance(){
		if(instance == null)
			instance = new RulesTemplateRegistry();
		return instance;
	}

	//================================================================================

	/**
	 * Fetch all the templates stored in the registry
	 * @return A list RulesTemplate
	 */
	public ArrayList<RuleDef> getTemplates() {
		ArrayList<RuleDef> templates = new ArrayList<RuleDef>();
		for(RuleDef rt : registry.values()){
			templates.add(rt);
		}
		// Add a custom template
		templates.add(new RuleDef(RulesTemplate.CUSTOM));
		return templates;
	}
	
	//================================================================================

	/**
	 * Register an interface with the registry. The registered class is turned into a template.
	 * Reflection is heavily used.
	 * 
	 * @param c : Class to be registered
	 * @param imports : default imports to be provided
	 * @return true if the interface is successfully registered. False if the given class is not an interface.
	 */
	public boolean register(Class<?> c, List<String> imports){
		
		if(!c.isInterface())
			return false;
		
		RuleDef ruleDef = new RuleDef(c.getName());
		ruleDef.setType(c.getName());
		
		List<FunctionDef> functions = new ArrayList<FunctionDef>();
		Method[] allMethods = c.getMethods();
		for(Method meth : allMethods){
			FunctionDef fd = new FunctionDef();
			fd.name = meth.getName();
			fd.returnType = meth.getReturnType().getName();
			fd.modifiers = Modifier.toString(meth.getModifiers()-Modifier.ABSTRACT);
			
			for(Class<?> paramType : meth.getParameterTypes()){
				VarDef param = new VarDef();
				param.varName = "arg"+fd.getArgCount();
				param.varType = paramType.getName();
				fd.params.add(param);
			}
			
			functions.add(fd);
		}
		
		List<VarDef> vars = new ArrayList<VarDef>();
		Field[] allFields = c.getDeclaredFields();
		for(Field f : allFields){
			VarDef vd = new VarDef();
			vd.varName = f.getName();
			vd.varType = f.getType().getName();
			vd.modifiers = Modifier.toString(f.getModifiers());
			vars.add(vd);
		}
		
		ClassDef classDef = ruleDef.getClassDef();
		classDef.setImports(imports);
		classDef.setVars(vars);
		classDef.setFunctions(functions);
		
		registry.put(c, ruleDef);
		
		return true;
	}

	//================================================================================

}
