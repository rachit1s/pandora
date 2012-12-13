package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for ExternalResource
public class ExternalResourceClient extends TbitsModelData {

	public static final int RESOURCEID = 1;
	public static final int RESOURCENAME = 2;
	public static final int RESOURCEDEF = 3;

	// default constructor
	public ExternalResourceClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	// public static String RESOURCE = "resource";
	public static String RESOURCE_DEF = "resource_def";
	public static String RESOURCE_ID = "resource_id";
	public static String RESOURCE_NAME = "resource_name";

	// getter and setter methods for variable myResource
	// public Resource getResource (){
	// return (Resource) this.get(RESOURCE);
	// }
	// public void setResource(Resource myResource) {
	// this.set(RESOURCE, myResource);
	// }

	// getter and setter methods for variable myResourceDef
	public String getResourceDef() {
		return (String) this.get(RESOURCE_DEF);
	}

	public void setResourceDef(String myResourceDef) {
		this.set(RESOURCE_DEF, myResourceDef);
	}

	// getter and setter methods for variable myResourceId
	public int getResourceId() {
		return (Integer) this.get(RESOURCE_ID);
	}

	public void setResourceId(int myResourceId) {
		this.set(RESOURCE_ID, myResourceId);
	}

	// getter and setter methods for variable myResourceName
	public String getResourceName() {
		return (String) this.get(RESOURCE_NAME);
	}

	public void setResourceName(String myResourceName) {
		this.set(RESOURCE_NAME, myResourceName);
	}

}