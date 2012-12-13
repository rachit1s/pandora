package commons.com.tbitsGlobal.utils.client.bafield;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;

/**
 * 
 * @author sourabh
 * 
 * Class for type fields
 */
public class BAFieldCombo extends BAField implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<TypeClient> types = new ArrayList<TypeClient>();
	
	private List<TypeDependency> dependencies = new ArrayList<TypeDependency>();

	public void setTypes(List<TypeClient> types) {
		this.types = types;
	}

	public List<TypeClient> getTypes() {
		return types;
	}

	public TypeClient getDefaultValue() {
		for(TypeClient type : types){
			if(type.getIsDefault())
				return type;
		}
		return null;
	}
	
	public List<TypeClient> getCheckedValues() {
		List<TypeClient> checkedValues = new ArrayList<TypeClient>();
		for(TypeClient type : types){
			if(type.getIsChecked())
				checkedValues.add(type);
		}
		return checkedValues;
	}
	
	public TypeClient getModelForName(String name){
		for(TypeClient type : types){
			if(type.getName().equals(name))
				return type;
		}
		return null;
	}

	public void setDependencies(List<TypeDependency> dependencies) {
		this.dependencies = dependencies;
	}

	public List<TypeDependency> getDependencies() {
		return dependencies;
	}

}
