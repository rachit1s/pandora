package mom.com.tbitsGlobal.client.service;

import java.util.List;

import mom.com.tbitsGlobal.client.admin.models.MOMTemplate;

import com.google.gwt.user.client.rpc.RemoteService;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;

public interface MOMAdminService extends RemoteService {
	public List<MOMTemplate> getMOMTemplatesForBa(BusinessAreaClient currentBa) throws TbitsExceptionClient;
	public List<MOMTemplate> setMomTemplateProperties(List<MOMTemplate> properties) throws TbitsExceptionClient;
	public List<BusinessAreaClient> getMOMBA() throws TbitsExceptionClient;

	//--------------dummy methods--------------//
	public BusinessAreaClient getBa(BusinessAreaClient ba);
	public SysConfigClient getSysconfigClient(SysConfigClient sysconfig);
	public BAField getBaField(BAField baField);
}
