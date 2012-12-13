package corrGeneric.com.tbitsGlobal.client.modelData;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class OnBehalfTypeClient extends TbitsModelData {
	protected ArrayList<TypeClient> onBehalfType1;
	protected ArrayList<TypeClient> onBehalfType2;
	protected ArrayList<TypeClient> onBehalfType3;
	
	public OnBehalfTypeClient(){
		super();
		onBehalfType1 	= new ArrayList<TypeClient>();
		onBehalfType2	= new ArrayList<TypeClient>();
		onBehalfType3	= new ArrayList<TypeClient>();
	}
	
	public void addOnBehalfType(String onBehalfType, TypeClient value){
		if(onBehalfType.equals(GenericParams.OnBehalfType1))
			onBehalfType1.add(value);
		else if(onBehalfType.equals(GenericParams.OnBehalfType2))
			onBehalfType2.add(value);
		else if(onBehalfType.equals(GenericParams.OnBehalfType3))
			onBehalfType3.add(value);
	}
	
	public void addOnBehalfTypeList(String onBehalfType, ArrayList<TypeClient> values){
		if((!values.isEmpty()) && (values != null)){
			if(onBehalfType.equals(GenericParams.OnBehalfType1))
				onBehalfType1.addAll(values);
			else if(onBehalfType.equals(GenericParams.OnBehalfType2))
				onBehalfType2.addAll(values);
			else if(onBehalfType.equals(GenericParams.OnBehalfType3))
				onBehalfType3.addAll(values);
		}
	}
	
	public ArrayList<TypeClient> getOnBehalfTypeList(String onBehalfType){
		if(onBehalfType.equals(GenericParams.OnBehalfType1))
			return onBehalfType1;
		else if(onBehalfType.equals(GenericParams.OnBehalfType2))
			return onBehalfType2;
		else if(onBehalfType.equals(GenericParams.OnBehalfType3))
			return onBehalfType3;

		return null;
	}
	
}
