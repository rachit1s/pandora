package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchyValuesClient;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface EscalationHierarchyService  extends RemoteService {
	
	public List<EscalationHierarchiesClient> getEscalationHierarchies() throws TbitsExceptionClient;
	
	public List<EscalationHierarchyValuesClient> saveEscalationHierarchyValues(EscalationHierarchiesClient hiearachy, List<EscalationHierarchyValuesClient> values) throws TbitsExceptionClient;
	
	public List<EscalationHierarchyValuesClient> getEscalationHierarchiesValues(EscalationHierarchiesClient hiearachy) throws TbitsExceptionClient;
	
	public EscalationHierarchiesClient insertEscalationHierarchies(EscalationHierarchiesClient hierarchies) throws TbitsExceptionClient;
	
	public UserClient getUserClientbyUserId(int userid) throws TbitsExceptionClient;
	
	public List<EscalationHierarchiesClient> updateEscalationHierarchies(List<EscalationHierarchiesClient> hierarchiesList) throws TbitsExceptionClient; 
}
