package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * POJO for User Map
 * @author devashish
 *
 */
public class UserMapClient extends TbitsModelData {
	
	/**
	 * TODO: implement the missing fields...
	 */
	public static String ID = "id";
	public static String SYS_PREFIX = "sysPrefix";
	public static String USER 	= "user";
	public static String USER_MAP_TYPE1 = GenericParams.UserMapType1;
	public static String USER_MAP_TYPE2 = GenericParams.UserMapType2;
	public static String USER_MAP_TYPE3 = GenericParams.UserMapType3;
	public static String USER_TYPE_FIELD = "userTypeField";
	public static String USER_LOGIN		= "userLogin";
	public static String STRICTNESS		= "strictness";
	public static String STATUS		= "status";
	
	public UserMapClient(){
		super();
	}
	
	//-----------getter/setter for id----------------------//
	public String getId(){
		return (String) this.get(ID);
	}
	
	public void setID(String id){
		this.set(ID, id);
	}
	
	//----------getter/setter for sysprefix---------------//
	public String getSysprefix(){
		return (String) this.get(SYS_PREFIX);
	}
	
	public void setSysprefix(String sysPrefix){
		this.set(SYS_PREFIX, sysPrefix);
	}
	
	//----------getter/setter for user--------------//
	public UserClient getUser(){
		return (UserClient) this.get(USER);
	}
	
	public void setUser(UserClient user){
		this.set(USER, user);
	}
	
	//--------getter/stter for type1-------------------//
	public TypeClient getType1(){
		return (TypeClient) this.get(USER_MAP_TYPE1);
	}
	
	public void setType1(TypeClient type1){
		this.set(USER_MAP_TYPE1, type1);
	}
	
	//------------getter/setter for type2-------------//
	public TypeClient getType2(){
		return (TypeClient) this.get(USER_MAP_TYPE2);
	}
	
	public void setType2(TypeClient type2){
		this.set(USER_MAP_TYPE2, type2);
	}
	
	//----------getter/setter for type3-----------------//
	public TypeClient getType3(){
		return (TypeClient) this.get(USER_MAP_TYPE3);
	}
	
	public void setType3(TypeClient type3){
		this.set(USER_MAP_TYPE3, type3);
	}
	
	//------------getter/setter for field--------------//
	public FieldClient getUserTypeField(){
		return (FieldClient) this.get(USER_TYPE_FIELD);
	}
	
	public void setUserTypeField(FieldClient userTypeField){
		this.set(USER_TYPE_FIELD, userTypeField);
	}
	
	//------------getter/setter for userlogin field------//
	public UserClient getUserLoginValue(){
		return (UserClient) this.get(USER_LOGIN);
	}
	
	public void setUserLoginValue(UserClient userLogin){
		this.set(USER_LOGIN, userLogin);
	}
	
	//-------------getter/setter for strictness field---//
	public String getStrictness(){
		return (String) this.get(STRICTNESS);
	}
	
	public void setStrictness(String strictness){
		this.set(STRICTNESS, strictness);
	}
	
	//-------------getter/setter for status------------//
	public String getStatus(){
		return (String) this.get(STATUS);
	}
	
	public void setStatus(String status){
		this.set(STATUS, status);
	}
}
