package corrGeneric.com.tbitsGlobal.client.forms;

import java.util.ArrayList;

import com.tbitsGlobal.jaguar.client.widgets.forms.AddRequestForm;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

import corrGeneric.com.tbitsGlobal.client.objects.CorrProtocolBase;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;

public class CorrAddReqForm extends AddRequestForm
{
	public CorrAddReqForm(UIContext parentContext) {
		super(parentContext);
	}
	
	protected ArrayList<String> prefillData;
	
	protected void create()
	{
		prefillData = this.myContext.getValue(CorrConst.PREFILL_FIELDS, ArrayList.class );
		super.create();
	}
	
	@Override
	protected boolean shouldFillField(BAField baField) 
	{
		if( null != prefillData && prefillData.contains(baField.getName()))
			return true;
		
		return baField.isSetEnabled();
	}
	
	protected boolean beforeSubmit(TbitsTreeRequestData requestModel)
	{
		return (this.corrProtocol.beforeSubmit() && super.beforeSubmit(requestModel) );
	}
	// this will check if the module is already
	// initialized then it will not do it again.
	private boolean isInitialized = false ;
	private CorrProtocolBase corrProtocol = null;

	@Override
	protected void onAfterLayout() {
		super.onAfterLayout();
		
		if( isInitialized == true )
			return ;
		
		isInitialized = true ;

		if( prefillData != null && prefillData.contains(IFixedFields.DESCRIPTION) && this.getData().getRequestModel() != null)
		{
			this.editor.setHTML(this.getData().getRequestModel().getAsString(IFixedFields.DESCRIPTION));
		}
		// this registers the for with the protocol
		corrProtocol = new CorrProtocolBase(this.myContext, this, this.getData().getSysPrefix(), this.fieldConfigs, this.getData().getBAFields().getModels(), this.submitBtn, CorrProtocolBase.FormType.ADD, this.resetBtn, this.headingPanel);
	}

//	public IFieldConfig getConfig(BAField baField) 
//	{
//		if( baField instanceof BAFieldMultiValue )
//		{
//			UserCache uc = CacheRepository.getInstance().getCache(UserCache.class);
//			ArrayList<UserClient> auc = new ArrayList<UserClient>( uc.getMap().values() );
//			return new UserPickerFieldConfig((BAFieldMultiValue)baField, new UserPicker( auc ));
//		}
//			
//		if( null != corrProtocol )
//			return corrProtocol.getConfig(baField);
//		
//		return super.getConfig(baField);
//	}
	
	@Override
	protected boolean hasBAFieldPermission(BAField bafield) 
	{
		return bafield.isCanAddInBA() ;
	}
	
}
