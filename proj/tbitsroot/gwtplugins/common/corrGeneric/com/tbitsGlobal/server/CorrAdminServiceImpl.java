package corrGeneric.com.tbitsGlobal.server;


import java.util.ArrayList;
import java.util.List;

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

import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import corrGeneric.com.tbitsGlobal.client.services.CorrAdminService;
import corrGeneric.com.tbitsGlobal.server.adm.CorrProperties;

public class CorrAdminServiceImpl extends TbitsRemoteServiceServlet implements CorrAdminService {


	public Integer deleteUserMapProperties(List<UserMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteUserMapProperties(properties);
	}

	public ArrayList<UserMapClient> saveUserMapProperties(ArrayList<UserMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setUserMapProperties(properties);
	}
	
	public UserMapTypeClient getUserMapTypes(String sysPrefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.getUserMapTypes(sysPrefix);
	}

	public ArrayList<UserMapClient> getUserMap(String sysPrefix, String userLogin) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherUserMap(sysPrefix, userLogin);
	}

	public Integer deleteOnBehalfProperties(List<OnBehalfMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteOnBehalfMapProperties(properties);
	}

	
	public ArrayList<OnBehalfMapClient> saveOnBehalfMap(ArrayList<OnBehalfMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setOnBehalfMapProperties(properties);
	}

	
	public ArrayList<OnBehalfMapClient> getOnBehalfMap(String sysPrefix, String userLogin) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherOnBehalfMap(sysPrefix, userLogin);
	}
	
	public OnBehalfTypeClient getOnBehalfTypes(String sysprefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.getOnBehalfTypes(sysprefix);
	}

	public Integer deleteFieldNameMapProperties(List<FieldNameMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteFieldNameMapProperties(properties);
	}

	
	public Integer saveFieldNamemap(ArrayList<FieldNameMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setFieldNameMapProperties(properties);
	}
	
	public ArrayList<FieldNameMapClient> getFieldNameMap(String sysprefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherFieldNameMapProperties(sysprefix);
	}

	
	public Integer deleteBAFieldMapProperties(List<BAFieldMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteBaFieldProperties(properties);
	}
	
	public Integer saveBaFieldMap(ArrayList<BAFieldMapClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setBAFieldMap(properties);
	}
	
	public ArrayList<FieldClient> getFields(String sysprefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.getFields(sysprefix);
	}
	
	public ArrayList<BAFieldMapClient> getBAFieldMap(String fromSysprefix, String toSysprefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherBAFieldMap(fromSysprefix, toSysprefix);
	}
	
	
	public Integer deleteReportParamProperties(List<ReportParamsClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteReportParamProperties(properties);
	}
	
	public Integer saveReportParamProperties(ArrayList<ReportParamsClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setReportParamProperties(properties);
	}
	
	public ArrayList<ReportParamsClient> getReportParamProperties(){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherReportParamProperties();
	}
	
	public ArrayList<ReportNameClient> getReportFileNameProperties(){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherReportNameMapProperties();
	}

	public Integer deleteReportFileNameProperties(List<ReportNameClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteReportNameMapProperties(properties);
	}

	public Integer saveReportFileNameProperties(ArrayList<ReportNameClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.saveReportNameMapProperties(properties);
	}
	
	
	public Integer deleteReportMapProperties(List<ReportMapClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteReportMapProperties(properties);
	}
	
	public ReportTypeClient getReportTypes(String sysPrefix){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.getReportTypes(sysPrefix);
	}
	
	public ArrayList<ReportMapClient> saveReportMapProperties(ArrayList<ReportMapClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setReportMapProperties(properties);
	}
	
	public ArrayList<ReportMapClient> getReportMapProperties(String sysPrefix){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherReportMapProperties(sysPrefix);
	}
	
	public Integer deleteCorrProtocolProperties(List<CorrProtocolClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteCorrProtocolProperties(properties);
	}
	
	public Integer saveCorrProtocolProperties(List<CorrProtocolClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setCorrProtocolProperties(properties);
	}
	
	/**
	 * Returns correpondence protocol properties from db
	 */
	public ArrayList<CorrProtocolClient> getCorrProtocolProperties(String sysPrefix){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherCorrProtocolProperties(sysPrefix);
	}
	
	/**
	 * Returns correspondence properties from db
	 */
	public ArrayList<CorrPropertiesClient> getCorrProperties() {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherCorrProperties();
	}
	
	/**
	 * Saves the correspondence properties received from the client
	 */
	public Integer saveCorrProperties(List<CorrPropertiesClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setCorrProperties(properties);
	}
	
	/**
	 * Deletes the specified properties from db
	 * @param List of properties to be deleted
	 * @return number of properties successfullly deleted
	 */
	public Integer deleteCorrProperty(List<CorrPropertiesClient> properties){
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteCorrProperty(properties);
	}

	public TypeClient dummyType(TypeClient type) {
		return type;
	}

	public FieldClient dummyField(FieldClient field) {
		return field;
	}


	public UserClient dummyUser(UserClient user) {
		return user;
	}

	@Override
	public ArrayList<CorrNumberConfigClient> getCorrNumberConfig(
			String sysPrefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.gatherCorrNumberConfigProperties(sysPrefix);
	}

	@Override
	public ArrayList<CorrNumberConfigClient> saveCorrNumberConfig(
			ArrayList<CorrNumberConfigClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.setCorrNumberConfigProperties(properties);
	}

	@Override
	public CorrNumberKeyClient getCorrNumberKey(String sysPrefix) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.getCorrNumberKey(sysPrefix);
	}

	@Override
	public Integer deleteCorrNumberConfig(
			List<CorrNumberConfigClient> properties) {
		CorrProperties corrProperties = new CorrProperties();
		return corrProperties.deleteCorrNumber(properties);
	}
}
