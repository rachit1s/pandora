package browse.com.tbitsglobal.browse.client;

import java.util.List;

import com.tbitsGlobal.jaguar.client.widgets.forms.UpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.BrowseFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;

public class BrowseUpdateRequestForm extends UpdateRequestForm{
	
	private List<String> fieldNameList;
	
	public BrowseUpdateRequestForm(UIContext context, Params params) {
		super(context);
		
		fieldNameList = BrowseUtils.getFieldNameListFromParams(params);
	}
	
	@Override
	public IFieldConfig getConfig(BAField baField) {
		if(fieldNameList != null && fieldNameList.contains(baField.getName())){
			return new BrowseFieldConfig(baField);
		}
		return super.getConfig(baField);
	}
}
