package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeUserClient;

public interface FieldServiceAsync{
	public void createNewField(String sys_prefix, String field_name,int field_type, AsyncCallback<FieldClient> callback);

	public void deleteField(FieldClient field, AsyncCallback<Boolean> callback);
	
	public void deleteFields(List<FieldClient> selectedItems, AsyncCallback<Boolean> asyncCallback);
	
	public void updateField(String sys_prefix, FieldClient field,AsyncCallback<FieldClient> callback);
	
	public void updateFields(String sysPrefix, List<FieldClient> fields,AsyncCallback<List<FieldClient>> callback);
	
	public void getFieldClients(String sysPrefix, AsyncCallback<List<FieldClient>> asyncCallback);
	
	public void getTypeList(String sys_prefix, String field_name, AsyncCallback<ArrayList<TypeClient>> callback);

	public void getTypeList(String sys_prefix,AsyncCallback<HashMap<String, HashMap<Integer, TypeClient>>> callback);
	
	public void createNewTypes(String sys_prefix, String field_name,int field_id, ArrayList<String> type_name,AsyncCallback<Boolean> callback); 

	public void createNewType(String sys_prefix, String field_name,int field_id, String type_name, AsyncCallback<TypeClient> callback);

	public void deleteType(String sys_prefix,String fieldname,TypeClient typeClient,AsyncCallback<TypeClient> callback);
	
	public void deleteTypes(String sys_prefix,String fieldname,List<TypeClient> types,AsyncCallback<Boolean> callback);
	
	public void updateType(String sys_prefix, String fieldname, TypeClient typeClient, AsyncCallback<TypeClient> callback);
	
	public void updateTypes(String sys_prefix, String fieldname, List<TypeClient> types, AsyncCallback<List<TypeClient>> callback);
	
	public void getTypeUser(String sysPrefix,int fieldId, int typeId,AsyncCallback<HashMap<Integer, TypeUserClient>> callback);
	
	void updateTypeUser(String sysPrefix,int fieldId, int typeId,List<TypeUserClient> typeUserClient,
			AsyncCallback<HashMap<Integer, TypeUserClient>> callback);

	void getTypeDependencies(String sysPrefix, AsyncCallback<List<TypeDependency>> callback);

	void getTypeDependenciesForType(TypeClient type, AsyncCallback<List<TypeDependency>> callback);

	void updateTypeDependencies(TypeClient type,
			List<TypeDependency> dependencies, AsyncCallback<Boolean> callback);
}
