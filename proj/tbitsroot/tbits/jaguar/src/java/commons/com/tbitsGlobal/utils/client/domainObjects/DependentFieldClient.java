package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for DependentField
public class DependentFieldClient extends TbitsModelData {

	// default constructor
	public DependentFieldClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	// public static String DEP_ROLE = "dep_role";
	public static String DEPENDENCY_ID = "dependency_id";
	public static String FIELD_ID = "field_id";
	public static String SYSTEM_ID = "system_id";

	// getter and setter methods for variable myDepRole
	// public DepRole getDepRole (){
	// return (DepRole) this.get(DEP_ROLE);
	// }
	// public void setDepRole(DepRole myDepRole) {
	// this.set(DEP_ROLE, myDepRole);
	// }

	// getter and setter methods for variable myDependencyId
	public int getDependencyId() {
		return (Integer) this.get(DEPENDENCY_ID);
	}

	public void setDependencyId(int myDependencyId) {
		this.set(DEPENDENCY_ID, myDependencyId);
	}

	// getter and setter methods for variable myFieldId
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

}