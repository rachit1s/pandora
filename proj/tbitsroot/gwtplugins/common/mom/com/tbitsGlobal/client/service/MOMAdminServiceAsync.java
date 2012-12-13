package mom.com.tbitsGlobal.client.service;

import java.util.List;

import mom.com.tbitsGlobal.client.admin.models.MOMTemplate;

import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;

public interface MOMAdminServiceAsync {

	/**
	 * Get the MOM properties from database for the specified business area
	 * @param currentBa
	 * @param callback
	 */
	void getMOMTemplatesForBa(BusinessAreaClient currentBa,	AsyncCallback<List<MOMTemplate>> callback);


	/**
	 * Set the MOM properties into database
	 * @param properties
	 * @param callback
	 */
	void setMomTemplateProperties(List<MOMTemplate> properties,	AsyncCallback<List<MOMTemplate>> callback);
	
	/**
	 * Get the BA for which MOM is configured
	 * @param callback
	 */
	void getMOMBA(AsyncCallback<List<BusinessAreaClient>> callback);
	//---------------------dummy methods-----------------------//
	void getBa(BusinessAreaClient ba, AsyncCallback<BusinessAreaClient> callback);
	void getBaField(BAField baField, AsyncCallback<BAField> callback);
	void getSysconfigClient(SysConfigClient sysconfig, AsyncCallback<SysConfigClient> callback);
}
