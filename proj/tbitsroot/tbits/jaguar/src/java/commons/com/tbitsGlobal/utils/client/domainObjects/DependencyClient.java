package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Dependency
public class DependencyClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public DependencyClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DEP_CONFIG = "dep_config";
	// public static String DEP_CONFIG_OBJECT = "dep_config_object";
	public static String DEPENDENCY_ID = "dependency_id";
	public static String DEPENDENCY_NAME = "dependency_name";
	// public static String LEVEL = "level";
	public static String SYSTEM_ID = "system_id";

	// public static String TYPE = "type";

	// getter and setter methods for variable myDepConfig
	public String getDepConfig() {
		return (String) this.get(DEP_CONFIG);
	}

	public void setDepConfig(String myDepConfig) {
		this.set(DEP_CONFIG, myDepConfig);
	}

	// getter and setter methods for variable myDepConfigObject
	// public DependencyConfig getDepConfigObject (){
	// return (DependencyConfig) this.get(DEP_CONFIG_OBJECT);
	// }
	// public void setDepConfigObject(DependencyConfig myDepConfigObject) {
	// this.set(DEP_CONFIG_OBJECT, myDepConfigObject);
	// }

	// getter and setter methods for variable myDependencyId
	public int getDependencyId() {
		return (Integer) this.get(DEPENDENCY_ID);
	}

	public void setDependencyId(int myDependencyId) {
		this.set(DEPENDENCY_ID, myDependencyId);
	}

	// getter and setter methods for variable myDependencyName
	public String getDependencyName() {
		return (String) this.get(DEPENDENCY_NAME);
	}

	public void setDependencyName(String myDependencyName) {
		this.set(DEPENDENCY_NAME, myDependencyName);
	}

	// getter and setter methods for variable myLevel
	// public DepLevel getLevel (){
	// return (DepLevel) this.get(LEVEL);
	// }
	// public void setLevel(DepLevel myLevel) {
	// this.set(LEVEL, myLevel);
	// }

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myType
	// public DepType getType (){
	// return (DepType) this.get(TYPE);
	// }
	// public void setType(DepType myType) {
	// this.set(TYPE, myType);
	// }

}