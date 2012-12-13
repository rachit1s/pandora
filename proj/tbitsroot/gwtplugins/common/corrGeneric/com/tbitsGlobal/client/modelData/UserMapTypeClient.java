package corrGeneric.com.tbitsGlobal.client.modelData;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class UserMapTypeClient extends TbitsModelData {
	protected ArrayList<TypeClient> userMapType1;
	protected ArrayList<TypeClient> userMapType2;
	protected ArrayList<TypeClient> userMapType3;
	
	public UserMapTypeClient(){
		super();
		userMapType1 	= new ArrayList<TypeClient>();
		userMapType2	= new ArrayList<TypeClient>();
		userMapType3	= new ArrayList<TypeClient>();
	}
	
	public void addUserMapType(String userMapType, TypeClient value){
		if(userMapType.equals(GenericParams.UserMapType1))
			userMapType1.add(value);
		else if(userMapType.equals(GenericParams.UserMapType2))
			userMapType2.add(value);
		else if(userMapType.equals(GenericParams.UserMapType3))
			userMapType3.add(value);
	}
	
	public void addUserMapTypeList(String onBehalfType, ArrayList<TypeClient> values){
		if((!values.isEmpty()) && (values != null)){
			if(onBehalfType.equals(GenericParams.UserMapType1))
				userMapType1.addAll(values);
			else if(onBehalfType.equals(GenericParams.UserMapType2))
				userMapType2.addAll(values);
			else if(onBehalfType.equals(GenericParams.UserMapType3))
				userMapType3.addAll(values);
		}
	}
	
	public ArrayList<TypeClient> getUserMapTypeList(String onBehalfType){
		if(onBehalfType.equals(GenericParams.UserMapType1))
			return userMapType1;
		else if(onBehalfType.equals(GenericParams.UserMapType2))
			return userMapType2;
		else if(onBehalfType.equals(GenericParams.UserMapType3))
			return userMapType3;

		return null;
	}
}
