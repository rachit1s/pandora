package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeUserClient;

public interface FieldService extends RemoteService{
	public FieldClient createNewField(String sys_prefix, String field_name,int field_type) throws TbitsExceptionClient;
	
	boolean deleteField(FieldClient field) throws TbitsExceptionClient;
	
	boolean deleteFields(List<FieldClient> selectedItems) throws TbitsExceptionClient;

	public FieldClient updateField(String sys_prefix, FieldClient field) throws TbitsExceptionClient;
	
	public List<FieldClient> updateFields(String sysPrefix, List<FieldClient> fields) throws TbitsExceptionClient;
	
	public List<FieldClient> getFieldClients(String sysPrefix) throws TbitsExceptionClient;

	public ArrayList<TypeClient> getTypeList(String sys_prefix, String field_name) throws TbitsExceptionClient;

	public HashMap<String, HashMap<Integer, TypeClient>> getTypeList(String sys_prefix) throws TbitsExceptionClient;

	public boolean createNewTypes(String sys_prefix, String field_name,int field_id, ArrayList<String> type_name) throws TbitsExceptionClient; 
	
	public TypeClient createNewType(String sys_prefix, String field_name,int field_id, String type_name) throws TbitsExceptionClient;
	
	public TypeClient deleteType(String sys_prefix,String fieldname,TypeClient typeClient) throws TbitsExceptionClient;
	
	public boolean deleteTypes(String sys_prefix,String fieldname,List<TypeClient> types) throws TbitsExceptionClient;

	public TypeClient updateType(String sys_prefix, String fieldname, TypeClient typeClient) throws TbitsExceptionClient;
	
	public List<TypeClient> updateTypes(String sys_prefix, String fieldname, List<TypeClient> types) throws TbitsExceptionClient;
	
	public HashMap<Integer, TypeUserClient> getTypeUser(String sysPrefix,int fieldId, int typeId) throws TbitsExceptionClient;

	public HashMap<Integer, TypeUserClient> updateTypeUser(String sysPrefix,int fieldId, int typeId,List<TypeUserClient> typeUserClient) throws TbitsExceptionClient;

	public List<TypeDependency> getTypeDependencies(String sysPrefix) throws TbitsExceptionClient;
	
	public List<TypeDependency> getTypeDependenciesForType(TypeClient type) throws TbitsExceptionClient;
	
	public boolean updateTypeDependencies(TypeClient type, List<TypeDependency> dependencies) throws TbitsExceptionClient;
}
