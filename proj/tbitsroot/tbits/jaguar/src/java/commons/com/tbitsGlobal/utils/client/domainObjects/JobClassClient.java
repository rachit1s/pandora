package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class JobClassClient extends TbitsModelData{
	private static final long serialVersionUID = 1L;
	public static String DISPLAY_NAME = "display_name";
	public static String CLASS_NAME = "class_name";
	
	public JobClassClient() {
		super();
	}
	
	public void setDisplayName(String name){
		this.set(DISPLAY_NAME, name);
	}
	public String getDisplayName(){
		return this.get(DISPLAY_NAME);
	}
	public String getClassName(){
		return this.get(CLASS_NAME);
	}
	public void setClassName(String name){
		this.set(CLASS_NAME, name);
	}

}
