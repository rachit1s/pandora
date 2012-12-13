package commons.com.tbitsGlobal.utils.client.widgets.forms;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.ui.Widget;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.AttachmentFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.LabelFieldConfig;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IViewRequestForm;

/**
 * 
 * @author sourabh
 *
 * The Abstract class for View Request Form
 */
public abstract class AbstractViewRequestForm extends AbstractRequestForm implements IViewRequestForm {
	
	protected AbstractViewRequestForm(UIContext parentContext) {
		super(parentContext);
	}
	
	@SuppressWarnings("unchecked")
	protected LayoutContainer createField(BAField baField, IFieldConfig config){
		if(!hasBAFieldPermission(baField))
			return null;
		
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		
		if(requestModel != null){
			LayoutContainer panel = new LayoutContainer();
			FormLayout layout = new FormLayout();
			layout.setLabelAlign(LabelAlign.RIGHT);
			layout.setLabelWidth(150);
			panel.setLayout(layout);
		    panel.setBorders(false);
		    FormData formData = new FormData("-10");
		    
		    if(baField.getName().equals(RELATED_REQUESTS)){
		    	POJO pojo = requestModel.getAsPOJO(baField.getName());
		    	if(pojo != null){
		    		String value = pojo.toString();
		    		LayoutContainer container = ClientUtils.getLinkedRequestsContainer(value);
		    		AdapterField field = new AdapterField(container);
	    			field.setName(baField.getName());
	    	    	field.setFieldLabel(baField.getDisplayName());
	    	    	field.setLabelStyle("font-weight:bold");
		    		panel.add(field, formData);
		    	}
		    }else if(baField.getName().equals(PARENT_REQUEST_ID)) {
		    	POJO poj = requestModel.getAsPOJO(baField.getName());
		    	if(poj != null){
		    		String value = poj.toString();
		    		String sysPrefix = this.getData().getSysPrefix();
		    		value= sysPrefix +"#"+value;
		    		LayoutContainer container = ClientUtils.getLinkedRequestsContainer(value);
		    
		    		AdapterField field = new AdapterField(container);
	    			field.setName(baField.getName());
	    	    	field.setFieldLabel(baField.getDisplayName());
	    	    	field.setLabelStyle("font-weight:bold");
		    		panel.add(field, formData);
		    	}
	       }else{
		    if(config == null)
		    	return null;
		    
	    	POJO poj = requestModel.getAsPOJO(config.getName());
			if(poj != null)
				config.setPOJO(poj);
		    fieldConfigs.put(baField.getName(), config);
		  
		    
		    Widget viewerWidget = config.getWidget();
		    panel.add(viewerWidget, formData);
	    }
	    
		    
		    return panel;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public IFieldConfig getConfig(BAField baField) {
		if(baField instanceof BAFieldAttachment){
			TbitsTreeRequestData requestModel = this.getData().getRequestModel();
			String sysPrefix = this.getData().getSysPrefix();
			return new AttachmentFieldConfig(Mode.VIEW, sysPrefix, requestModel, (BAFieldAttachment) baField);
		}else if(baField instanceof BAFieldCheckBox){
			return new LabelFieldConfig<Boolean>(baField);
		}else if(baField instanceof BAFieldCombo){
			return new LabelFieldConfig<String>(baField);
		}else if(baField instanceof BAFieldDate){
			return new LabelFieldConfig<String>(baField);
		}else if(baField instanceof BAFieldInt){
			return new LabelFieldConfig<Integer>(baField);
		}else if(baField instanceof BAFieldMultiValue){
			return new LabelFieldConfig<String>(baField);
		}else if(baField instanceof BAFieldString){
			return new LabelFieldConfig<String>(baField);
		}else if(baField instanceof BAFieldTextArea){
			return new LabelFieldConfig<String>(baField);
		}
		return null;
	}

	@Override
	protected boolean hasBAFieldPermission(BAField bafield) {
		int perm = 0;
		
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null && requestModel.getPerms().containsKey(bafield.getName()))
			perm = requestModel.getPerms().get(bafield.getName());
		
		return ((perm & PermissionClient.VIEW) != 0) && bafield.isCanView() ;
	}
}
