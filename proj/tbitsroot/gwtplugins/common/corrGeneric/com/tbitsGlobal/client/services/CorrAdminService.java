package corrGeneric.com.tbitsGlobal.client.services;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
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

/**
 * Service class for making RPC calls for the Admin module of correspondence
 * For all other calls of correspondence, see 'CorrDBService.java'
 * @author devashish
 *
 */
public interface CorrAdminService extends RemoteService{

	//---------------------For Correspondence properties page---------------//
	ArrayList<CorrPropertiesClient> getCorrProperties();
	Integer saveCorrProperties(List<CorrPropertiesClient> properties);
	Integer deleteCorrProperty(List<CorrPropertiesClient> properties);
	
	//-------------------- For Correspondence protocols page---------------//
	ArrayList<CorrProtocolClient> getCorrProtocolProperties(String sysPrefix);
	Integer saveCorrProtocolProperties(List<CorrProtocolClient> properties);
	Integer deleteCorrProtocolProperties(List<CorrProtocolClient> properties);
	
	//-------------------- For Report Map page-----------------------------//
	ArrayList<ReportMapClient> getReportMapProperties(String sysPrefix);
	ArrayList<ReportMapClient> saveReportMapProperties(ArrayList<ReportMapClient> properties);
	
	ReportTypeClient getReportTypes(String sysPrefix);
	Integer deleteReportMapProperties(List<ReportMapClient> properties);
	
	//---------------------For Report Name Map page-------------------------//
	ArrayList<ReportNameClient> getReportFileNameProperties();
	Integer saveReportFileNameProperties(ArrayList<ReportNameClient> properties);
	Integer deleteReportFileNameProperties(List<ReportNameClient> properties);
	
	//---------------------For Report Params Page-----------------------------//
	ArrayList<ReportParamsClient> getReportParamProperties();
	Integer saveReportParamProperties(ArrayList<ReportParamsClient> properties);
	Integer deleteReportParamProperties(List<ReportParamsClient> properties);
	
	//---------------------For BA Field Map Page------------------------------//
	Integer saveBaFieldMap(ArrayList<BAFieldMapClient> properties);
	Integer deleteBAFieldMapProperties(List<BAFieldMapClient> properties);
	ArrayList<BAFieldMapClient> getBAFieldMap(String fromSysprefix, String toSysprefix);
	ArrayList<FieldClient> getFields(String sysprefix);
	
	//---------------------For Field Name Map Page----------------------------//
	ArrayList<FieldNameMapClient> getFieldNameMap(String sysprefix);
	Integer saveFieldNamemap(ArrayList<FieldNameMapClient> properties);
	Integer deleteFieldNameMapProperties(List<FieldNameMapClient> properties);
	
	//----------------------For On Behalf Map Page----------------------------//
	OnBehalfTypeClient getOnBehalfTypes(String sysprefix);
	ArrayList<OnBehalfMapClient> getOnBehalfMap(String sysPrefix, String userLogin);
	ArrayList<OnBehalfMapClient> saveOnBehalfMap(ArrayList<OnBehalfMapClient> properties);
	Integer deleteOnBehalfProperties(List<OnBehalfMapClient> properties);
	
	//----------------------For User Map Page---------------------------------//
	UserMapTypeClient getUserMapTypes(String sysPrefix);
	ArrayList<UserMapClient> getUserMap(String sysPrefix, String userLogin);
	ArrayList<UserMapClient> saveUserMapProperties(ArrayList<UserMapClient> properties);
	Integer deleteUserMapProperties(List<UserMapClient> properties);
	
	//---------------------For Corr Number Config Page-------------------------//
	ArrayList<CorrNumberConfigClient> getCorrNumberConfig(String sysPrefix);
	ArrayList<CorrNumberConfigClient> saveCorrNumberConfig(ArrayList<CorrNumberConfigClient> properties) ;
	CorrNumberKeyClient getCorrNumberKey(String sysPrefix);
	Integer deleteCorrNumberConfig(List<CorrNumberConfigClient> properties);
	
	//dummy
	UserClient dummyUser(UserClient user);
	TypeClient dummyType(TypeClient type);
	FieldClient dummyField(FieldClient field);
}
