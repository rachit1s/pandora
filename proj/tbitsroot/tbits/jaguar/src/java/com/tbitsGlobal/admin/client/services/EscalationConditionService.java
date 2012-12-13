package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;



import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionDetailClient;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionParametersClient;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public interface EscalationConditionService extends RemoteService {
	
	public static final String EDIT_CONDITION = "Edit";
	public static final String CREATE_CONDITION = "Create Condition";
	
	
	public ArrayList<EscalationConditionDetailClient> getAllEscCondition() throws TbitsExceptionClient; 
	
	public boolean deleteEscCondtion(Integer escCondId) throws TbitsExceptionClient; 
	
	public boolean saveCondition(String mode,EscalationConditionDetailClient condDetail,ArrayList<EscalationConditionParametersClient> ecpcList) throws TbitsExceptionClient;

}
