package transbit.tbits.Escalation;

import java.io.Serializable;



public class EscalationHierarchies implements Serializable  {
	
	//ESC_ID is escalation hierarchy id

	public  int ESC_ID ;
	public  String NAME;
	public  String DISPLAY_NAME ;
	public  String DESCRIPTION ;

	
	public int getEscId(){
		return ESC_ID;
	}
	
	public void setEscId(int escId){
		this.ESC_ID=escId;
	}
	
	public String getName(){
		return NAME;
	}
	
	public void setName(String name){
		this.NAME=name;
	}
	
	public String getDescription(){
		return DESCRIPTION;
	}
	
	public void setDescription(String description){
		this.DESCRIPTION=description;
	}
	
	public String getDisplayName(){
		return DISPLAY_NAME;
	}
	
	public void setDisplayName(String displayName){
		this.DISPLAY_NAME=displayName;
	}

	@Override
	public String toString() {
		return "EscalationHierarchies [DESCRIPTION=" + DESCRIPTION
				+ ", DISPLAY_NAME=" + DISPLAY_NAME + ", ESC_ID=" + ESC_ID
				+ ", NAME=" + NAME + "]";
	}
	
	

	
	
	
	
}
