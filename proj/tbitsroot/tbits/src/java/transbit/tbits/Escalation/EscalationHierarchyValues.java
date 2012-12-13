package transbit.tbits.Escalation;

import java.io.Serializable;

import transbit.tbits.domain.User;



public class EscalationHierarchyValues implements Serializable{

	public int ESC_ID		;
	public User CHILD_USER	 ;
	public User PARENT_USER ;
	
	public int getEscId(){
		
		return ESC_ID;
	}
	
	public void setEscId(int escId){
		this.ESC_ID=escId;
	}
	public User getChlidUser(){
		return CHILD_USER;
	}
	
	public void setChildUser(User childUser){
		this.CHILD_USER=childUser;
	}
	
	public User getParentUser(){
		return PARENT_USER;
	}
	
	public void setParentUser(User parentUser){
		this.PARENT_USER=parentUser;
	}

	@Override
	public String toString() {
		return "EscalationHierarchyValues [CHILD_USER=" + CHILD_USER
				+ ", ESC_ID=" + ESC_ID + ", PARENT_USER=" + PARENT_USER + "]";
	}
	
	
}
