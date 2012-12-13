package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;



import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionDetailClient;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionParametersClient;


import commons.com.tbitsGlobal.utils.client.domainObjects.JobParameterClient;

public interface EscalationConditionServiceAsync {

	void saveCondition(String mode, EscalationConditionDetailClient condDetail,
			ArrayList<EscalationConditionParametersClient> ecpcList,
			AsyncCallback<Boolean> callback);

	void getAllEscCondition(
			AsyncCallback<ArrayList<EscalationConditionDetailClient>> callback);

	void deleteEscCondtion(Integer escCondId, AsyncCallback<Boolean> callback);

}
