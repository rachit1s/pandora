package corrGeneric.com.tbitsGlobal.client.modelData;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrNumberKeyClient extends TbitsModelData {
	protected ArrayList<TypeClient> numType1;
	protected ArrayList<TypeClient> numType2;
	protected ArrayList<TypeClient> numType3;
	
	public CorrNumberKeyClient(){
		super();
		numType1 	= new ArrayList<TypeClient>();
		numType2	= new ArrayList<TypeClient>();
		numType3	= new ArrayList<TypeClient>();
	}
	
	public void addCorrNumberKey(String numType, TypeClient value){
		if(numType.equals(GenericParams.NumType1))
			numType1.add(value);
		else if(numType.equals(GenericParams.NumType2))
			numType2.add(value);
		else if(numType.equals(GenericParams.NumType3))
			numType3.add(value);
	}
	
	public void addCorrNumberKeyList(String numType, ArrayList<TypeClient> values){
		if((!values.isEmpty()) && (values != null)){
			if(numType.equals(GenericParams.NumType1))
				numType1.addAll(values);
			else if(numType.equals(GenericParams.NumType2))
				numType2.addAll(values);
			else if(numType.equals(GenericParams.NumType3))
				numType3.addAll(values);
		}
	}
	
	public ArrayList<TypeClient> getCorrNumberKeyList(String numType){
		if(numType.equals(GenericParams.NumType1))
			return numType1;
		else if(numType.equals(GenericParams.NumType2))
			return numType2;
		else if(numType.equals(GenericParams.NumType3))
			return numType3;

		return null;
	}

}
