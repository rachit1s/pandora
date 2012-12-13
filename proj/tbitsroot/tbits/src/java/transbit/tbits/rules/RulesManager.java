package transbit.tbits.rules;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * The manager for all the java rules stored in the database.
 * Keeps maps of all the rule classes and the classbytes corresponding to each rule.
 * Every new rule needs to be added to this manager in order to be implemented.

 * The rule manager can be called to return all the rules implementing any particular interface.
 * 
 * @author karan
 *
 */
public class RulesManager {

	//================================================================================

	private static RulesManager rm;
	private static Hashtable<String, RuleClass> rulesMap;
	private static Hashtable<String, byte[]> classbytesMap;

	//================================================================================

	/**
	 * Private Constructor
	 */
	private RulesManager(){
		rulesMap = new Hashtable<String, RuleClass>();
		classbytesMap = new Hashtable<String, byte[]>();
	}

	//================================================================================

	/**
	 * @return Singleton instance of the RulesManager
	 */
	public static RulesManager getInstance(){
		if(rm == null){
			rm = new RulesManager();
			loadRules();
		}
		return rm;
	}

	//================================================================================

	/**
	 * Loads the specified class from the database.
	 * @param name
	 * @return True if the class is found in the database. False otherwise.
	 */
	private static boolean loadRules() {
		RulesClassLoader cl = new RulesClassLoader();
		try {
			ArrayList<RuleClass> allRules = RulesDbService.getInstance().getExistingRules();
			for(RuleClass rule : allRules){
				// Do not add the rule to manager if it is not deployed
				if(rule.getSequenceNumber() < 0)
					continue;
				byte[] classbytes = RulesDbService.getInstance().getRuleClassBytes(rule.getName());
				classbytesMap.put(rule.getName(), classbytes);
				Class<?> c = cl.loadClass(rule.getName());
				rule.setCls(c);
				rulesMap.put(rule.getName(), rule);
			}
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	//================================================================================

	/**
	 * Add the rule to the rules map
	 * @param rc
	 */
	public void putRule(RuleClass rc, byte[] classbytes){
		
		classbytesMap.put(rc.getName(), classbytes);
		try {
			Class<?> c;
			c = new RulesClassLoader().loadClass(rc.getName());
			rc.setCls(c);
			rulesMap.put(rc.getName(), rc);
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	//================================================================================

	/**
	 * @return classbytes of the required rule from the classbytesMap
	 */
	public byte[] getClassBytes(String name) {
		
		return classbytesMap.get(name);
	}

	//================================================================================

	/**
	 * @return all classes implementing the interface specified in the order of their sequence numbers
	 */
	public ArrayList<Class<?>> getRulesImplementing(Class<?> iClass){
		
		ArrayList<RuleClass> implementors = new ArrayList<RuleClass>();
		for(RuleClass rc : rulesMap.values()){
			if(rc.getType().equals(iClass.getName())){
				implementors = addClass(rc, implementors.toArray());
			}
		}
		ArrayList<Class<?>> orderedClasses = new ArrayList<Class<?>>();
		for(RuleClass rc : implementors){
			orderedClasses.add(rc.getCls());
		}
		return orderedClasses;
	}

	//================================================================================

	/**
	 * Utility Function
	 * @param rc
	 * @param objects
	 * @return list after adding rc in the correct order
	 */
	private ArrayList<RuleClass> addClass(RuleClass rc, Object[] objects){
		
		ArrayList<RuleClass> implementors = new ArrayList<RuleClass>();
		boolean added = false;
		for(int i=0; i<objects.length; i++){
			if(rc.getSequenceNumber() <= ((RuleClass)objects[i]).getSequenceNumber()){
				implementors.add(rc);
				added = true;
			}
			implementors.add((RuleClass) objects[i]);
		}
		if(!added){
			implementors.add(rc);
		}
		return implementors;
	}
	
	//================================================================================

}
