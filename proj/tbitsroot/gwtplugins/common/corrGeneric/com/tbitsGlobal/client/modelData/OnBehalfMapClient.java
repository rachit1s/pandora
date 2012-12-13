package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class OnBehalfMapClient extends TbitsModelData {
	public static String ID 			= "id";
	public static String ON_BEHALF_USER = "onBehalfUser";
	public static String USER 			= "user";
	public static String SYS_PREFIX		= "sysPrefix";
	public static String TYPE_1 		= GenericParams.OnBehalfType1;
	public static String TYPE_2 		= GenericParams.OnBehalfType2;
	public static String TYPE_3			= GenericParams.OnBehalfType3;
	public static String STATUS 		= "status";
	
	public OnBehalfMapClient(){
		super();
	}
	
	///-----------getter/setter for id-------------------------//
	public String getId(){
		return (String) this.get(ID);
	}
	
	public void setID(String id){
		this.set(ID, id);
	}
	
	//------------getter/setter for on behalf user--------------------//
	public UserClient getOnBehalfUser(){
		return (UserClient) this.get(ON_BEHALF_USER);
	}
	
	public void setOnBehalfUser(UserClient user){
		this.set(ON_BEHALF_USER, user);
	}
	
	//-------------getter/setter for user---------------------//
	public UserClient getUser(){
		return (UserClient) this.get(USER);
	}
	
	public void setUser(UserClient user){
		this.set(USER, user);
	}
	
	//-------------getter/setter for sysPrefix--------------//
	public String getSysprefix(){
		return (String) this.get(SYS_PREFIX);
	}
	
	public void setSysprefix(String sysPrefix){
		this.set(SYS_PREFIX, sysPrefix);
	}
	
	//------------getter/setter for type1------------------//
	public TypeClient getType1(){
		return (TypeClient) this.get(TYPE_1);
	}
	
	public void setType1(TypeClient type1){
		this.set(TYPE_1, type1);
	}
	
	//------------getter/setter for type2------------------//
	public TypeClient getType2(){
		return (TypeClient) this.get(TYPE_2);
	}
	
	public void setType2(TypeClient type2){
		this.set(TYPE_2, type2);
	}
	
	//------------getter/setter for type3------------------//
	public TypeClient getType3(){
		return (TypeClient) this.get(TYPE_3);
	}
	
	public void setType3(TypeClient type3){
		this.set(TYPE_3, type3);
	}
	
	//------------getter/setter for status-----------------//
	public String getStatus(){
		return (String) this.get(STATUS);
	}
	
	public void setStatus(String status){
		this.set(STATUS, status);
	}
}
