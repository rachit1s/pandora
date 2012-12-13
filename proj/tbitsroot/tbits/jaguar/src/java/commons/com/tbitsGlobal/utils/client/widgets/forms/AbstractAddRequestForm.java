package commons.com.tbitsGlobal.utils.client.widgets.forms;

import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IAddRequestForm;

public abstract class AbstractAddRequestForm extends AbstractEditRequestForm implements IAddRequestForm {

	protected AbstractAddRequestForm(UIContext parentContext) {
		super(parentContext);
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.setBooleanDefaultValue();
	}
	
	@SuppressWarnings("unchecked")
	private void setBooleanDefaultValue(){
		BusinessAreaClient baClient = ClientUtils.getBAbySysPrefix(this.getData().getSysPrefix());
    	
    	if(baClient != null){
    		SysConfigClient sysConfigClient = baClient.getSysConfigObject();
    		if(sysConfigClient != null){
    			IFieldConfig config = fieldConfigs.get(IFixedFields.NOTIFY);
    			if(config != null){
			    	if(sysConfigClient.getRequestNotify() == 1)
			    		config.setPOJO(new POJOBoolean(true));
			    	else
			    		config.setPOJO(new POJOBoolean(false));
    			}
		    	
		    	config = fieldConfigs.get(IFixedFields.NOTIFY_LOGGERS);
		    	if(config != null){
		    		if(sysConfigClient.getRequestNotifyLoggers())
			    		config.setPOJO(new POJOBoolean(true));
			    	else
			    		config.setPOJO(new POJOBoolean(false));
		    	}
    		}
    	}
	}
}
