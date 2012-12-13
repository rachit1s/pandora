package corrGeneric.com.tbitsGlobal.client.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

import corrGeneric.com.tbitsGlobal.client.modelData.BAFieldMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberConfigClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberKeyClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrPropertiesClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrProtocolClient;
import corrGeneric.com.tbitsGlobal.client.modelData.FieldNameMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfTypeClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportNameClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportParamsClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportTypeClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapTypeClient;


public interface CorrAdminServiceAsync {
	void getCorrProperties(AsyncCallback<ArrayList<CorrPropertiesClient>> callback);
	void saveCorrProperties(List<CorrPropertiesClient> properties, AsyncCallback<Integer> callback);
	void deleteCorrProperty(List<CorrPropertiesClient> property, AsyncCallback<Integer> callback);
	
	void getCorrProtocolProperties(String sysPrefix, AsyncCallback<ArrayList<CorrProtocolClient>> callback);
	void saveCorrProtocolProperties(List<CorrProtocolClient> properties, AsyncCallback<Integer> callback);
	void deleteCorrProtocolProperties(List<CorrProtocolClient> properties, AsyncCallback<Integer> callback);
	
	void getReportMapProperties(String sysPrefix, AsyncCallback<ArrayList<ReportMapClient>> callback);
	void saveReportMapProperties(ArrayList<ReportMapClient> properties, AsyncCallback<ArrayList<ReportMapClient>> callback);
	void getReportTypes(String sysPrefix, AsyncCallback<ReportTypeClient> callback);
	void deleteReportMapProperties(List<ReportMapClient> properties, AsyncCallback<Integer> callback);
	
	void getReportFileNameProperties(AsyncCallback<ArrayList<ReportNameClient>> callback);
	void saveReportFileNameProperties(ArrayList<ReportNameClient> properties, AsyncCallback<Integer> callback);
	void deleteReportFileNameProperties(List<ReportNameClient> properties, AsyncCallback<Integer> callback);
	
	void getReportParamProperties(AsyncCallback<ArrayList<ReportParamsClient>> callback);
	void saveReportParamProperties(ArrayList<ReportParamsClient> properties, AsyncCallback<Integer> callback);
	void deleteReportParamProperties(List<ReportParamsClient> properties, AsyncCallback<Integer> callback);
	
	
	void getBAFieldMap(String fromSysprefix, String toSysprefix, AsyncCallback<ArrayList<BAFieldMapClient>> callback);
	void getFields(String sysprefix, AsyncCallback<ArrayList<FieldClient>> callback);
	void saveBaFieldMap(ArrayList<BAFieldMapClient> properties,	AsyncCallback<Integer> callback);
	void deleteBAFieldMapProperties(List<BAFieldMapClient> properties,	AsyncCallback<Integer> callback);
	
	void getFieldNameMap(String sysprefix, AsyncCallback<ArrayList<FieldNameMapClient>> callback);
	void saveFieldNamemap(ArrayList<FieldNameMapClient> properties,	AsyncCallback<Integer> callback);
	void deleteFieldNameMapProperties(List<FieldNameMapClient> properties,	AsyncCallback<Integer> callback);
	
	void getOnBehalfTypes(String sysprefix,	AsyncCallback<OnBehalfTypeClient> asyncCallback);
	void getOnBehalfMap(String sysPrefix, String userLogin, AsyncCallback<ArrayList<OnBehalfMapClient>> callback);
	void saveOnBehalfMap(ArrayList<OnBehalfMapClient> properties, AsyncCallback<ArrayList<OnBehalfMapClient>> callback);
	void deleteOnBehalfProperties(List<OnBehalfMapClient> properties, AsyncCallback<Integer> callback);
	
	void getUserMapTypes(String sysPrefix, AsyncCallback<UserMapTypeClient> callback);
	void getUserMap(String sysPrefix, String userLogin,	AsyncCallback<ArrayList<UserMapClient>> callback);
	void deleteUserMapProperties(List<UserMapClient> properties, AsyncCallback<Integer> callback);
	void saveUserMapProperties(ArrayList<UserMapClient> properties, AsyncCallback<ArrayList<UserMapClient>> callback);
	
	void getCorrNumberConfig(String sysPrefix, AsyncCallback<ArrayList<CorrNumberConfigClient>> callback);
	void saveCorrNumberConfig(ArrayList<CorrNumberConfigClient> savedProperties, AsyncCallback<ArrayList<CorrNumberConfigClient>> callback);
	void getCorrNumberKey(String sysPrefix, AsyncCallback<CorrNumberKeyClient> callback);
	void deleteCorrNumberConfig(List<CorrNumberConfigClient> properties, AsyncCallback<Integer> callback);
	
	// dummy
	void dummyType(TypeClient type, AsyncCallback<TypeClient> callback);
	void dummyField(FieldClient field, AsyncCallback<FieldClient> callback);
	void dummyUser(UserClient user, AsyncCallback<UserClient> callback);
	

}
