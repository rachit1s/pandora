package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchyValuesClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface EscalationHierarchyServiceAsync {

	void getEscalationHierarchies(
			AsyncCallback<List<EscalationHierarchiesClient>> callback);

	void saveEscalationHierarchyValues(EscalationHierarchiesClient hiearachy,
			List<EscalationHierarchyValuesClient> values,
			AsyncCallback<List<EscalationHierarchyValuesClient>> callback);

	void getEscalationHierarchiesValues(EscalationHierarchiesClient hiearachy,
			AsyncCallback<List<EscalationHierarchyValuesClient>> callback);

	void insertEscalationHierarchies(EscalationHierarchiesClient hierarchies,
			AsyncCallback<EscalationHierarchiesClient> callback);

	void getUserClientbyUserId(int userid, AsyncCallback<UserClient> callback);

	void updateEscalationHierarchies(
			List<EscalationHierarchiesClient> hierarchiesList,
			AsyncCallback<List<EscalationHierarchiesClient>> callback);

}
