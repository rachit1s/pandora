package corrGeneric.com.tbitsGlobal.client.forms;

//import static corrGeneric.com.tbitsGlobal.shared.CorrConst.genCorrFieldNamesMap;
//import static corrGeneric.com.tbitsGlobal.shared.CorrConst.showConfirmationValues;

import java.util.ArrayList;

import com.tbitsGlobal.jaguar.client.widgets.forms.UpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;

import corrGeneric.com.tbitsGlobal.client.objects.CorrProtocolBase;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;

public class CorrUpdateReqForm extends UpdateRequestForm
{
	public CorrUpdateReqForm(UIContext parentContext) {
		super(parentContext);
	}
	
	protected ArrayList<String> prefillData;
	private CorrProtocolBase corrProtocol = null;

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
//		String message = "You are about to create a correspondence ";
//					
//		if( null == showConfirmationValues || null == showConfirmationValues.get(this.sysPrefix) || showConfirmationValues.get(sysPrefix).equals(GenericParams.ShowConfirmationOnSubmit_Yes))
//		{
//			String gcfv = null;
//			if( null != genCorrFieldNamesMap  )
//			{
//				String fieldName = genCorrFieldNamesMap.get(sysPrefix);
//				if( null != fieldName )
//				{
//					TypeFieldConfig fieldConfig = (TypeFieldConfig)this.fieldConfigs.get(fieldName);
//					if( null != fieldConfig )
//					{
//						TypeFieldControl gctfc = fieldConfig.getWidget();
//						if( null != gctfc )
//						{
//							TypeClient tc = gctfc.getValue();
//							if( null != tc )
//								message +=  "with " + fieldConfig.getBaField().getDisplayName() + " set to " + tc.getDisplayName();  
//						}
//					}
//				}
//			}
//			
//			message += ".\nPlease click Ok to submit.";
//			return Window.confirm(message);
//		}
//		
//		return true;
	}
	// this will check if the module is already
	// initialized then it will not do it again.
	private boolean isInitialized = false ;

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
		corrProtocol = new CorrProtocolBase(this.myContext, this, this.getData().getSysPrefix(), this.fieldConfigs, this.getData().getBAFields().getModels(), this.submitBtn, CorrProtocolBase.FormType.UPDATE, this.resetBtn, this.headingPanel);
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
//		return super.getConfig(baField);
//	}
	
	@Override
	protected boolean hasBAFieldPermission(BAField bafield) 
	{
		int perm = 0;
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel.getPerms().containsKey(bafield.getName()))
			perm = requestModel.getPerms().get(bafield.getName());
		
		return ((perm & PermissionClient.CHANGE) != 0) && bafield.isCanUpdate() ;
	}
}
