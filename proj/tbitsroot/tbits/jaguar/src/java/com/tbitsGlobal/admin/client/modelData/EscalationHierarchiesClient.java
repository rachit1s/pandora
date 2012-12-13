package com.tbitsGlobal.admin.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;





/**
 * this class is for escaltion hierarche metadata for client side.
 * @author Nirmal Agrawal
 * 
 *
 */
public class EscalationHierarchiesClient extends TbitsModelData {
	
	//ESC_ID is escalation hierarchy id

	public static String ESC_ID = "esc_id";
	public static String NAME = "name";
	public static String DISPLAY_NAME = "display_name";
	public static String DESCRIPTION = "desc";
	
	
	public int getEscId(){
		if(this.getPropertyNames().contains(ESC_ID))
			return (Integer)this.get(ESC_ID);
		return 0;
	}
	
	public void setEscId(int escId){
		this.set(ESC_ID, escId);
	}
	
	public String getName(){
		return (String)this.get(NAME);
	}
	
	public void setName(String name){
		this.set(NAME, name);
	}
	
	public String getDescription(){
		return (String)this.get(DESCRIPTION);
	}
	
	public void setDescription(String description){
		this.set(DESCRIPTION, description);
	}
	
	public String getDisplayName(){
		return (String)this.get(DISPLAY_NAME);
	}
	
	public void setDisplayName(String displayName){
		this.set(DISPLAY_NAME, displayName);
	}
	
	public <T extends TbitsModelData> T clone(T model) {
		model = super.clone(model);
		model.remove(ESC_ID);
		return model;
	}

	@Override
	public String toString() {
		return "EscalationHierarchiesClient [getDescription()="
				+ getDescription() + ", getDisplayName()=" + getDisplayName()
				+ ", getEscId()=" + getEscId() + ", getName()=" + getName()
				+ "]";
	}
	
	
	
}
