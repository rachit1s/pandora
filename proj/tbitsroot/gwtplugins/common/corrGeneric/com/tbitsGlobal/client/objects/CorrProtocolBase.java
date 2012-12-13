package corrGeneric.com.tbitsGlobal.client.objects;

import static corrGeneric.com.tbitsGlobal.client.utils.CorrHelperClient.getDefaultUserList;
import static corrGeneric.com.tbitsGlobal.client.utils.CorrHelperClient.getUserClientArray;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitFieldNameMap;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitOnBehalfMap;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitOptionsMap;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.DisableProtocolFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorrespondenceFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerationAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.LoggerFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.MoreThanOneLoggerAllowed;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.MoreThanOneLoggerAllowed_No;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalfType1;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalfType2;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalfType3;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OriginatorFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.ProtFollowOnBehalf;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.ProtFollowOnBehalf_No;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientUserTypeFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType1;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType2;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.tbitsGlobal.jaguar.client.cache.UserCache;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TypeFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.UserPickerFieldConfig;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractEditRequestForm;
import commons.com.tbitsGlobal.utils.client.widgets.forms.FormHeadingPanel;

import corrGeneric.com.tbitsGlobal.client.utils.CorrHelperClient;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrProtocolBase 
{
	/**
	 * @return the logUP
	 */
	protected UserPicker getLogUP() {
		return logUP;
	}

	/**
	 * @return the obType1FC
	 */
	protected TypeFieldControl getObType1FC() {
		return obType1FC;
	}

	/**
	 * @return the obType2FC
	 */
	protected TypeFieldControl getObType2FC() {
		return obType2FC;
	}

	/**
	 * @return the obType3FC
	 */
	protected TypeFieldControl getObType3FC() {
		return obType3FC;
	}

	/**
	 * @return the umType1FC
	 */
	protected TypeFieldControl getUmType1FC() {
		return umType1FC;
	}

	/**
	 * @return the umType2FC
	 */
	protected TypeFieldControl getUmType2FC() {
		return umType2FC;
	}

	/**
	 * @return the umType3FC
	 */
	protected TypeFieldControl getUmType3FC() {
		return umType3FC;
	}

	/**
	 * @return the genCorr
	 */
	protected TypeFieldControl getGenCorr() {
		return genCorr;
	}

	/**
	 * @param baFields the baFields to set
	 */
	protected void setBaFields(List<BAField> baFields) {
		this.baFields = baFields;
	}

	/**
	 * @return the editForm
	 */
	protected AbstractEditRequestForm getEditForm() {
		return editForm;
	}

	/**
	 * @param editForm the editForm to set
	 */
	protected void setEditForm(AbstractEditRequestForm editForm) {
		this.editForm = editForm;
	}

	/**
	 * @return the sysPrefix
	 */
	protected String getSysPrefix() {
		return sysPrefix;
	}

	/**
	 * @param sysPrefix the sysPrefix to set
	 */
	protected void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	/**
	 * @return the configs
	 */
	protected HashMap<String, IFieldConfig> getConfigs() {
		return configs;
	}

	/**
	 * @param configs the configs to set
	 */
	protected void setConfigs(HashMap<String, IFieldConfig> configs) {
		this.configs = configs;
	}

	/**
	 * @return the fieldsMap
	 */
	protected HashMap<String, FieldNameEntry> getFieldsMap() {
		return fieldsMap;
	}

	/**
	 * @param fieldsMap the fieldsMap to set
	 */
	protected void setFieldsMap(HashMap<String, FieldNameEntry> fieldsMap) {
		this.fieldsMap = fieldsMap;
	}

	/**
	 * @return the userMap
	 */
	protected ArrayList<UserMapEntry> getUserMap() {
		return userMap;
	}

	/**
	 * @param userMap the userMap to set
	 */
	protected void setUserMap(ArrayList<UserMapEntry> userMap) {
		this.userMap = userMap;
	}

	/**
	 * @return the previewButton
	 */
	protected Button getPreviewButton() {
		return previewButton;
	}

	/**
	 * @param previewButton the previewButton to set
	 */
	protected void setPreviewButton(Button previewButton) {
		this.previewButton = previewButton;
	}

	/**
	 * @return the submitButton
	 */
	protected Button getSubmitButton() {
		return submitButton;
	}

	/**
	 * @param submitButton the submitButton to set
	 */
	protected void setSubmitButton(Button submitButton) {
		this.submitButton = submitButton;
	}

	/**
	 * @return the onBehalfList
	 */
	protected HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> getOnBehalfMap() {
		return onBehalfMap;
	}

	/**
	 * @param onBehalfList the onBehalfList to set
	 */
	protected void setOnBehalfMap(HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> onBehalfMap) {
		this.onBehalfMap = onBehalfMap;
	}

	/**
	 * @return the formType
	 */
	protected FormType getFormType() {
		return formType;
	}

	/**
	 * @param formType the formType to set
	 */
	protected void setFormType(FormType formType) {
		this.formType = formType;
	}

	private static final String NOTALLOWED = "You (" + ClientUtils.getCurrentUser().getUserLogin() + ") are not allowed to create a correpondence";
	private static final String PREFILL_FAILED = "Error occured in automatic form filler.\nPlease fill it manually.";

	private boolean isRegistered = false;
	
	AbstractEditRequestForm editForm;
	String sysPrefix; 
	
	UserPicker logUP;
	UserPicker recepUP ;
	
	TypeFieldControl disableProtocolFC;
	
	TypeFieldControl obType1FC;
	TypeFieldControl obType2FC;
	TypeFieldControl obType3FC;
	
	TypeFieldControl umType1FC;
	TypeFieldControl umType2FC;
	TypeFieldControl umType3FC;
	
	TypeFieldControl genCorr;
	TypeFieldControl receAgencyType; 
	TypeFieldControl genAgencyType;
	TypeFieldControl origAgencyType;  
	
	HashMap<String,IFieldConfig> configs;
	HashMap<String,FieldNameEntry> fieldsMap = null;
	ArrayList<UserMapEntry> userMap = null;
	HashMap<String,ProtocolOptionEntry> optionMap = null;
	HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> onBehalfMap = null ;

	public static enum FormType { ADD, UPDATE };
	
	FormType formType ;
	
	Button previewButton;
	Button submitButton;
	Button resetButton;

	private List<BAField> baFields;

	private UIContext uiContext;
	private FormHeadingPanel headerPanel;
	
	/**
	 * @return the baFields
	 */
	public List<BAField> getBaFields() {
		return baFields;
	}

	/**
	 * @return the optionMap
	 */
	protected HashMap<String, ProtocolOptionEntry> getOptionMap() {
		return optionMap;
	}

	/**
	 * @param optionMap the optionMap to set
	 */
	protected void setOptionMap(HashMap<String, ProtocolOptionEntry> optionMap) {
		this.optionMap = optionMap;
	}

	public CorrProtocolBase(UIContext uiContext, AbstractEditRequestForm editForm,
			String sysPrefix, HashMap<String,IFieldConfig> configs, List<BAField> baFields, Button submitButton, FormType formType, Button resetBtn, FormHeadingPanel hdrPanel) {
		super();
		this.uiContext = uiContext;
		this.editForm = editForm;
		this.baFields = baFields;
		this.configs = configs;
		this.sysPrefix = sysPrefix;
		this.submitButton = submitButton;
		this.formType = formType;
		this.resetButton = resetBtn;
		this.headerPanel = hdrPanel;
		initialize();	
	}
	
	/**
	 * @return the uiContext
	 */
	protected UIContext getUiContext() {
		return uiContext;
	}

	private String fn( String fieldName )
	{
		if( null == fieldName )
			return null ;

		for( BAField baf : editForm.getData().getBAFields().getModels() )
		{
			if( baf.getName().equals(fieldName))
				return baf.getDisplayName();				
		}
		
		return fieldName;
	}
	
	private String noPerm( String fieldDisplayName )
	{
		return "You(" + ClientUtils.getCurrentUser().getUserLogin() + ") do not have sufficient permission to modify " + fieldDisplayName + ".";
	}
	
	private void error( String msg )
	{
		Log.error(msg);		
		this.headerPanel.getMessageBox().error(msg,false);
	}
	
	private void enableSubmitButton()
	{
		this.submitButton.enable();
	}
	private void enablePreivewButton()
	{
		if( null != this.previewButton )
			this.previewButton.enable();
	}
	private void enableButtons()
	{
		this.enablePreivewButton();
		this.enableSubmitButton();
	}
	private void disableSubmitButton()
	{
		this.submitButton.disable() ;
	}
	
	private void disablePreviewButton()
	{
		if( null != this.previewButton )
			this.previewButton.disable() ;
	}
	
	private void disableButtons()
	{
		disablePreviewButton();
		disableSubmitButton() ;
	}
	
	protected void reEvaluate(Object caller)
	{
		try
		{
			Log.info("Starting reEvaluate");
			if( this.getOnBehalfMap() == null || this.getOnBehalfMap().size() == 0)
			{
				error("You(" + ClientUtils.getCurrentUser().getUserLogin() + ") are not allowed to log on behalf of anyone.");
				disableButtons();
				return;
			}
// 			brain
//			StringBuffer errors = new StringBuffer() ;
			enableButtons();
	
			String t1 = null;
			String t2 = null;
			String t3 = null;
			if( obType1FC != null )
			{
				TypeClient t = obType1FC.getValue();
				if( null != t )
					t1 = t.getName() ;
				else
				{
					error( "Please select a valid value in : " + fdn(this.getFieldsMap().get(OnBehalfType1).getBaFieldName()) );
					return;
//					Set<String> validType1 = this.getOnBehalfMap().keySet();
//					setValue( this.getFieldsMap().get(OnBehalfType1).getBaFieldName(), getCommaSeparatedString(validType1) );
//					t1 = ( null != obType1FC.getValue() ) ? obType1FC.getValue().getName() : null ;
				}
			}
			
			if( obType2FC != null )
			{
				TypeClient t = obType2FC.getValue();
				if( null != t )
					t2 = t.getName() ;
				else 
				{
					error( "Please select a valid value in : " + fdn(this.getFieldsMap().get(OnBehalfType2).getBaFieldName()) );
					return;					
//					Set<String> validType2 = this.getOnBehalfMap().get(t1).keySet();
//					setValue( this.getFieldsMap().get(OnBehalfType2).getBaFieldName(), getCommaSeparatedString(validType2) );
//					t2 = ( null != obType2FC.getValue() ) ? obType2FC.getValue().getName() : null ;
				}
			}
			
			if( obType3FC != null )
			{
				TypeClient t = obType3FC.getValue();
				if( null != t )
					t3 = t.getName() ;
				else
				{
					error( "Please select a valid value in : " + fdn(this.getFieldsMap().get(OnBehalfType3).getBaFieldName()) );
					return;
//					Set<String> validType3 = this.getOnBehalfMap().get(t1).get(t2).keySet();
//					setValue( this.getFieldsMap().get(OnBehalfType3).getBaFieldName(), getCommaSeparatedString(validType3) );
//					t3 = ( null != obType3FC.getValue() ) ? obType3FC.getValue().getName() : null ;
				}
			}
	
			HashMap<String, HashMap<String, HashMap<String, Collection<String>>>> map = this.getOnBehalfMap();
			HashMap<String, HashMap<String, Collection<String>>> map1 = map.get(t1);
			if( null == map1 )
			{
				error( "No mapping found for this selection. Please select something else.");
				disableButtons();
				return; 
			}
			
			HashMap<String, Collection<String>> map2 = map1.get(t2);
			
			if( null == map2 )
			{
				error( "No mapping found for this selection. Please select something else.");
				disableButtons();
				return; 
			}
			
			Collection<String> allowedUsers = map2.get(t3);
			
			if( null == allowedUsers || allowedUsers.size() == 0 )
			{
				error("You(" + ClientUtils.getCurrentUser().getUserLogin() + ") do not have correct mapping for correspondence.");
				disableButtons();
				return;
			}
			
			ArrayList<UserClient> ucc = getUserClientArray(allowedUsers);
//			ListStore<UserClient> ucStore = CorrHelperClient.getListStore(ucc);
			this.getLogUP().getStore().removeAll();
			this.getLogUP().getStore().add(ucc);
			
			// if the property is set correctly then set the user to self.
			ProtocolOptionEntry setLoggerProp = this.getOptionMap().get(GenericParams.ProtSetLoggerToSelf);
			if(null != setLoggerProp )
			{
				if( setLoggerProp.getValue().equals(GenericParams.ProtSetLoggerToSelf_Yes) )
				{
					if( allowedUsers.contains(ClientUtils.getCurrentUser().getUserLogin()))
					{
						setValue(this.getFieldsMap().get(LoggerFieldName).getBaFieldName(), ClientUtils.getCurrentUser().getUserLogin());
						changeUserTypes(this.getLogUP());
					}
					else
					{
						setValue(this.getFieldsMap().get(LoggerFieldName).getBaFieldName(), "");
					}
				}
				else
				{
					// no change
//					setValue(this.getFieldsMap().get(LoggerFieldName).getBaFieldName(), "");
				}
			}
			
			Log.info("Finished reEvaluate");
		}
		catch(Exception e)
		{
			Log.error("Exception 2 : " , e);
			error("client side prefill rule failed. You can try Refreshing or manually fill the form.");
			restoreForm();
		}
	}

	private void firstTimeConfiguration() throws CorrNotAllowedException 
	{
			Log.info("Starting firstTimeConfiguration");
			initializeLocalMembers();
			
			String t1 = null;
			String t2 = null;
			String t3 = null;

			if( null == this.getOnBehalfMap() || this.getOnBehalfMap().size() == 0)
				throw new CorrNotAllowedException(NOTALLOWED);
			
			if( obType1FC != null )
			{
				if( null == this.getOnBehalfMap().keySet())
					throw new CorrNotAllowedException(NOTALLOWED);
				
				Set<String> validType1 = this.getOnBehalfMap().keySet();
				setValue( this.getFieldsMap().get(OnBehalfType1).getBaFieldName(), getCommaSeparatedString(validType1) );
				t1 = ( null != obType1FC.getValue() ) ? obType1FC.getValue().getName() : null ;
			}
			
			if( obType2FC != null )
			{
				if( null == this.getOnBehalfMap().get(t1).keySet())
					throw new CorrNotAllowedException(NOTALLOWED);
				
				Set<String> validType2 = this.getOnBehalfMap().get(t1).keySet();
				setValue( this.getFieldsMap().get(OnBehalfType2).getBaFieldName(), getCommaSeparatedString(validType2) );
				t2 = ( null != obType2FC.getValue() ) ? obType2FC.getValue().getName() : null ;
			}
			
			if( obType3FC != null )
			{
				if( null == this.getOnBehalfMap().get(t1).get(t2).keySet())
					throw new CorrNotAllowedException(NOTALLOWED);
				
				Set<String> validType3 = this.getOnBehalfMap().get(t1).get(t2).keySet();
				setValue( this.getFieldsMap().get(OnBehalfType3).getBaFieldName(), getCommaSeparatedString(validType3) );
				t3 = ( null != obType3FC.getValue() ) ? obType3FC.getValue().getName() : null ;
			}
			
			Log.info("Finished firstTimeConfiguration");
	}

	public IFieldConfig getConfig(BAField baField)
	{
		return this.getConfigs().get(baField);
	}
	
	private void initializeLocalMembers()
	{
		Log.info("Starting initializeLocalMembers");
		FieldNameEntry fneLog = this.getFieldsMap().get(LoggerFieldName);
		if( null == fneLog || null == fneLog.getBaFieldName())
		{
			error("Cannot find " + LoggerFieldName + " mapping. Please configure correspondence properly for ba : " + this.getSysPrefix() + ". And refresh.");
			disableButtons();
			return;
		}
		
		UserCache uc = CacheRepository.getInstance().getCache(UserCache.class);
		if( null == uc )
		{
			Log.info("user-cache was null.");
		}

		ArrayList<UserClient> auc = new ArrayList<UserClient>( uc.getValues() );

		UserPicker up = new UserPicker( auc );
//		IFieldConfig up1fc =  this.getConfigs().get(fneLog.getBaFieldName());
		BAFieldMultiValue upBaField = (BAFieldMultiValue)CorrHelperClient.lookupBAField(this.getSysPrefix(), fneLog.getBaFieldName());
		UserPickerFieldConfig up1fc = new UserPickerFieldConfig(upBaField, up);
		up1fc.setWidget(up);
//		editForm.getFieldConfigs().put(fneLog.getBaFieldName(), up1fc);
		boolean fieldChangeStatus = editForm.reDrawField(upBaField,up1fc);
		if( fieldChangeStatus == false )
		{
			error("Cannot properly handle the " + fdn(fneLog.getBaFieldName()) + " properly. Please fill values manually.");
		}
		
		logUP = (UserPicker) this.getConfigs().get(fneLog.getBaFieldName()).getWidget();
	
		FieldNameEntry recAgnfne = this.getFieldsMap().get(RecepientAgencyFieldName);
		if( null !=  recAgnfne && null != recAgnfne.getBaFieldName())
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(recAgnfne.getBaFieldName());
			if( null != tfc )
			{
				receAgencyType = tfc.getWidget();
				CorrHelperClient.registerType(this.getSysPrefix(), recAgnfne.getBaFieldName());
			}
		}

		FieldNameEntry orgAgenfne = this.getFieldsMap().get(OriginatorFieldName) ;
		if( null != orgAgenfne && null != orgAgenfne.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(orgAgenfne.getBaFieldName());
			if( null != tfc )
			{
				origAgencyType = tfc.getWidget();
				CorrHelperClient.registerType(this.getSysPrefix(), orgAgenfne.getBaFieldName());
			}
		}

		FieldNameEntry genAgenfne = this.getFieldsMap().get(GenerationAgencyFieldName) ; 
		if( null != genAgenfne && null != genAgenfne.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(genAgenfne.getBaFieldName());
			if( null != tfc )
			{
				genAgencyType = tfc.getWidget();
				CorrHelperClient.registerType(this.getSysPrefix(), genAgenfne.getBaFieldName());
			}
		}
		
		FieldNameEntry repUpfne = this.getFieldsMap().get(RecepientUserTypeFieldName); 
		if( null != repUpfne && null != repUpfne.getBaFieldName() )
		{
			UserPickerFieldConfig upfc = (UserPickerFieldConfig) this.getConfigs().get(repUpfne.getBaFieldName());
			if( null != upfc )
				recepUP = upfc.getWidget() ;
		}

		FieldNameEntry onBehalfType1 = this.getFieldsMap().get(OnBehalfType1);
		if( null != onBehalfType1 && null != onBehalfType1.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(onBehalfType1.getBaFieldName());
			if( null != tfc )
			{
				obType1FC = tfc.getWidget() ;
				CorrHelperClient.registerType(this.getSysPrefix(), onBehalfType1.getBaFieldName());	
			}
		}
		
		FieldNameEntry onBehalfType2 = this.getFieldsMap().get(OnBehalfType2);
		if( null != onBehalfType2 && null != onBehalfType2.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(onBehalfType2.getBaFieldName());
			if( null != tfc )
			{
				obType2FC = tfc.getWidget() ;
				CorrHelperClient.registerType(this.getSysPrefix(), onBehalfType2.getBaFieldName());
			}
			
		}
		
		FieldNameEntry onBehalfType3 = this.getFieldsMap().get(OnBehalfType3);
		if( null != onBehalfType3 && null != onBehalfType3.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(onBehalfType3.getBaFieldName());
			if( null != tfc )
			{
				obType3FC = tfc.getWidget() ;
				CorrHelperClient.registerType(this.getSysPrefix(), onBehalfType3.getBaFieldName());	
			}
		}
		
		FieldNameEntry umType1 = this.getFieldsMap().get(UserMapType1);
		if( null != umType1 && null != umType1.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(umType1.getBaFieldName());
			if( null != tfc )
			{
				umType1FC = tfc.getWidget() ;
				CorrHelperClient.registerType(this.getSysPrefix(), umType1.getBaFieldName());
			}
		}
		
		FieldNameEntry umType2 = this.getFieldsMap().get(UserMapType2);
		if( null != umType2 && null != umType2.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(umType2.getBaFieldName());
			if( null != tfc )
			{
				umType2FC = tfc.getWidget() ;
				CorrHelperClient.registerType(this.getSysPrefix(), umType2.getBaFieldName());
			}
		}
		
		FieldNameEntry umType3 = this.getFieldsMap().get(UserMapType3);
		if( null != umType3 && null != umType3.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(umType3.getBaFieldName());
			if( null != tfc )
			{
				umType3FC = tfc.getWidget() ;
				CorrHelperClient.registerType(this.getSysPrefix(), umType3.getBaFieldName());		
			}
		}
	
		FieldNameEntry genCN = this.getFieldsMap().get(GenerateCorrespondenceFieldName);
		if( null != genCN && null != genCN.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(genCN.getBaFieldName());
			if( null != tfc )
			{
				genCorr = tfc.getWidget();
				CorrHelperClient.registerType(this.getSysPrefix(), genCN.getBaFieldName());
			}
		}

		FieldNameEntry disProt = this.getFieldsMap().get(DisableProtocolFieldName);
		if( null != disProt && null != disProt.getBaFieldName() )
		{
			TypeFieldConfig tfc = (TypeFieldConfig) this.getConfigs().get(disProt.getBaFieldName());
			if( null != tfc )
			{
				disableProtocolFC = tfc.getWidget();
			}
		}
		Log.info("Finished initializeLocalMembers");
		
	}

	private void unregisterProtocol() 
	{
		if( isRegistered == false )
			return ;

		Log.info("Starting unregisterProtocol");
		this.disablePreviewButton();
		
		unregisterEvents();
		
		restoreFields();
		isRegistered = false ;
		
		Log.info("Finished unregisterProtocol");
	}

	private void restoreFields() 
	{
		Log.info("Starting restoring fields");
		this.enableButtons();
		
		// register types
		FieldNameEntry obt1 = this.getFieldsMap().get(OnBehalfType1);
		if( null != obt1 && null != obt1.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), obt1.getBaFieldName());
		
		FieldNameEntry obt2 = this.getFieldsMap().get(OnBehalfType2);
		if( null != obt2 && null != obt2.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), obt2.getBaFieldName());
		
		FieldNameEntry obt3 = this.getFieldsMap().get(OnBehalfType3);
		if( null != obt3 && null != obt3.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), obt3.getBaFieldName());
		
		FieldNameEntry umt1 = this.getFieldsMap().get(UserMapType1);
		if( null != umt1 && null != umt1.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), umt1.getBaFieldName());
		
		FieldNameEntry umt2 = this.getFieldsMap().get(UserMapType2);
		if( null != umt2 && null != umt2.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), umt2.getBaFieldName());
		
		FieldNameEntry umt3 = this.getFieldsMap().get(UserMapType3);
		if( null != umt3 && null != umt3.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), umt3.getBaFieldName());
		
		FieldNameEntry genAgenEntry = this.getFieldsMap().get(GenerationAgencyFieldName);
		if( null != genAgenEntry && null != genAgenEntry.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), genAgenEntry.getBaFieldName());
		
		FieldNameEntry recepEntry = this.getFieldsMap().get(RecepientAgencyFieldName);
		if( null != recepEntry && null != recepEntry.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), recepEntry.getBaFieldName());
		
		FieldNameEntry origAgenEntry = this.getFieldsMap().get(OriginatorFieldName);
		if( null != origAgenEntry && null != origAgenEntry.getBaFieldName() )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), origAgenEntry.getBaFieldName());
		
		List<UserClient> ls = getDefaultUserList(logUP.getStore().getModels());
		logUP.getStore().removeAll();
		logUP.getStore().add(ls);
		
		if( null != receAgencyType )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), receAgencyType.getName());
		if( null != origAgencyType )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), origAgencyType.getName());
		if( null != genAgencyType )
			CorrHelperClient.setDefaultType(this.getSysPrefix(), genAgencyType.getName());
		
		Log.info("Finished reStoring fields.");
	}

	private void unregisterEvents() 
	{
		Log.info("Starting unregisterEvents");
		
		if( null != logUP)
			logUP.removeAllListeners();
		
		if( null != obType1FC )
			obType1FC.removeAllListeners();
		
		if( null != obType2FC )
			obType2FC.removeAllListeners();
		
		if( null != obType3FC )			
			obType3FC.removeAllListeners();
		
		if( null != umType1FC )
			umType1FC.removeAllListeners();
		
		if( null != umType2FC )
			umType2FC.removeAllListeners();
		
		if( null != umType3FC)
			umType3FC.removeAllListeners();
		
		if( null != recepUP)
			recepUP.removeAllListeners();
		
		Log.info("Finished unregisterEvents");
	}

	private boolean setValue(String fieldName,
			String fieldValue)
	{
		return CorrHelperClient.setValue(this.getSysPrefix(), fieldName, fieldValue);
	}

	private String getCommaSeparatedString(Collection<String> list)
	{
		StringBuffer sb = new StringBuffer() ;
		if( null == list )
			return sb.toString() ;
		
		boolean first = true ;
		for( String str : list )
		{
			if( first )
			{
				sb.append(str);
				first = false;
			}
			else
			{
				sb.append(","+str);
			}
		}
		
		return sb.toString();
	}
	
	protected void registerEvents()
	{
		Log.info("Starting registerEvents");
		
		if( null == logUP )
		{
			error("Cannot find " + LoggerFieldName + " mapping. Please configure correspondence properly for ba : " + this.getSysPrefix() + ". And refresh.");
			disableButtons();
			return;
		}
		
		logUP.addListener(Events.OnBlur, new Listener<BaseEvent>(){
			public void handleEvent(BaseEvent be) {
				changeUserTypes(logUP);
			}			
		});

		if( null != this.getOptionMap() && null != this.getOptionMap().get(ProtFollowOnBehalf) 
				&& this.getOptionMap().get(ProtFollowOnBehalf).equals(ProtFollowOnBehalf_No))
		{
			// do not register any event on OnBehalf 
		}
		else
		{	// register events.
			if( null != obType1FC)
			{
				obType1FC.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
					
					@Override
					public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
					{
	//					reEvaluate(obType1FC);
						onChangeObType1();
					}
				});
			}
			
			if( null != obType2FC)
			{
				obType2FC.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
					
					@Override
					public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
					{
	//					reEvaluate(obType2FC);
						onChangeObType2();
					}
				});
			}
			
			if( null != obType3FC)
			{
				obType3FC.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
					
					@Override
					public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
					{
	//					reEvaluate(obType3FC);
						onChangeObType3();
					}
				});
			}
		}
		
		if( null != umType1FC)
		{
			umType1FC.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
				{
					changeUserTypes(umType1FC);
				}
			});
		}

		if( null != umType2FC)
		{
			umType2FC.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
				{
					changeUserTypes(umType2FC);
				}
			});
		}
		
		if( null != umType3FC)
		{
			umType3FC.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
				{
					changeUserTypes(umType3FC);
				}
			});
		}

		if( null != recepUP && null != receAgencyType)
		{
			recepUP.addListener(Events.OnBlur, new Listener<BaseEvent>(){
				public void handleEvent(BaseEvent be) 
				{
					setRecepientAgency();
				}			
			});
		}
		
		Log.info("finished registerEvents");
	}
		
	private void registerProtocol()
	{
		if( isRegistered == true )
			return ;
		
		Log.info("Starting registerProtocol");
		registerEvents();
		reEvaluate(genCorr);
		disableReset();	
		
		isRegistered = true ;
		
		Log.info("Finishing registerProtocol");
	}
	
	private void disableReset() 
	{
		resetButton.disable();
	}

	private void registerDisableProtocol()
	{
		Log.info("Starting registerGenCorr");
		if( null != disableProtocolFC)
		{
			disableProtocolFC.addSelectionChangedListener( new SelectionChangedListener<TypeClient>() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent<TypeClient> se) 
				{
					TypeClient type = se.getSelectedItem();

					handleDisableProtocolChange(type.getName());
				}
			}); 
		}
		
		Log.info("Finishing registerGenCorr");
	}

	protected void handleDisableProtocolChange(String name) 
	{
		if( name.equalsIgnoreCase(GenericParams.DisableProtocol_True))
		{
			unregisterProtocol();
		}
		else
		{
			registerProtocol();
		}
	}

	private void unregisterGenCorr()
	{
		Log.info("Starting unregisterGenCorr");
		if( null != genCorr )
			genCorr.removeAllListeners();
		
		Log.info("Finishing unregisterGenCorr");
	}
	
	private void restoreForm()
	{
		Log.info("Starting restoreForm");
		unregisterProtocol();
		unregisterGenCorr();
		Log.info("Finishing restoreForm");
	}
	
	private void addPreviewButton() {
		
		String currentBa=ClientUtils.getCurrentBA().getSystemPrefix();
		System.out.println("current ba :" + currentBa);
		
		CorrConst.corrDBService.isPreviewPdfEnable(currentBa, new AsyncCallback<Boolean>() {
			
			@Override
			public void onSuccess(Boolean result) {
				
				if(result == true)
				{
				previewButton = new Button("Preview PDF", new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) 
					{
						TbitsTreeRequestData ttrd = editForm.createRequestModel() ;				
						openPDF(ttrd);
					}
				});
				// create the preview button.
				editForm.addButton(previewButton);
				}
				
			}
			
			@Override
			public void onFailure(Throwable arg0) {
				
				error("Preview Pdf can not be generated..Qap");
				
			}
		});

	
	}

	protected void onChangeObType3() {
		reEvaluate(obType3FC);
	}

	protected void onChangeObType2() 
	{
		String t1 = null;
		
		if( obType1FC != null )
		{
			TypeClient t = obType1FC.getValue();
			if( null != t )
				t1 = t.getName() ;
			else
			{
				error( "Please select a valid value in : " + fdn(this.getFieldsMap().get(OnBehalfType1).getBaFieldName()) );
				return;
			}
		}
		
		String t2 = null;
		if( obType2FC != null )
		{
			TypeClient t = obType2FC.getValue();
			if( null != t )
				t2 = t.getName() ;
			else 
			{
				error( "Please select a valid value in : " + fdn(this.getFieldsMap().get(OnBehalfType2).getBaFieldName()) );
				return;					
//				Set<String> validType2 = this.getOnBehalfMap().get(t1).keySet();
//				setValue( this.getFieldsMap().get(OnBehalfType2).getBaFieldName(), getCommaSeparatedString(validType2) );
//				t2 = ( null != obType2FC.getValue() ) ? obType2FC.getValue().getName() : null ;
			}
			
			if( obType3FC != null )
			{
				Set<String> validType3 = this.getOnBehalfMap().get(t1).get(t2).keySet();
				setValue( this.getFieldsMap().get(OnBehalfType3).getBaFieldName(), getCommaSeparatedString(validType3) );
//				t3 = ( null != obType3FC.getValue() ) ? obType3FC.getValue().getName() : null ;
			}	
		}
		
		reEvaluate(obType2FC);
	}

	protected void onChangeObType1() 
	{
		String t1 = null;
		if( obType1FC != null )
		{
			TypeClient t = obType1FC.getValue();
			if( null != t )
				t1 = t.getName() ;
			else
			{
				error( "Please select a valid value in : " + fdn(this.getFieldsMap().get(OnBehalfType1).getBaFieldName()) );
				return;
//				Set<String> validType1 = this.getOnBehalfMap().keySet();
//				setValue( this.getFieldsMap().get(OnBehalfType1).getBaFieldName(), getCommaSeparatedString(validType1) );
//				t1 = ( null != obType1FC.getValue() ) ? obType1FC.getValue().getName() : null ;
			}
			
			if( obType2FC != null )
			{
				Set<String> validType2 = this.getOnBehalfMap().get(t1).keySet();
				setValue( this.getFieldsMap().get(OnBehalfType2).getBaFieldName(), getCommaSeparatedString(validType2) );
//				t2 = ( null != obType2FC.getValue() ) ? obType2FC.getValue().getName() : null ;
			}			
		}
		
		reEvaluate(obType1FC);
	}

	public String fdn( String fieldName )
	{
		return CorrHelperClient.fdn(this.getSysPrefix(), fieldName);
	}
	
	protected boolean changeUserTypes(Object source) 
	{
		try
		{
			// get the logger
			ArrayList<UserClient> logger = null;
			try
			{
				logger = CorrHelperClient.getUserClients(this.sysPrefix,logUP);
			}
			catch(UserNotFoundException unfe)
			{
				error(unfe.getMessage());
			}
			
			if( null == logger || logger.size() == 0 )
			{
				error("Please select a valid user in : " + fdn(this.getFieldsMap().get(LoggerFieldName).getBaFieldName()));
				return false ;
			}
			
			if( logger.size() > 1 && null != this.getOptionMap().get(MoreThanOneLoggerAllowed) && this.getOptionMap().get(MoreThanOneLoggerAllowed).getValue().equals(MoreThanOneLoggerAllowed_No))
			{
				error("More than one user is not allowed in : " + fdn(this.getFieldsMap().get(LoggerFieldName).getBaFieldName()));
				return false ;
			}
			
			final UserClient logger0 = logger.get(0);
			boolean returnValue = true ;
			if( source == logUP )
			{
				boolean retValue = 	setAgencyAndOriginator(logger0);
				returnValue = ( returnValue && retValue);
			}
			// make the async call 
			// check if we have the user-map for this user ?
			// else retrive it and go ahead
			if( null == this.getUserMap() || this.getUserMap().size() == 0 || ( ! this.getUserMap().get(0).getUserLogin().equals(logger0.getUserLogin()))  )
			{
				CorrConst.corrDBService.getUserMap(this.getSysPrefix(), logger0.getUserLogin(), new AsyncCallback<ArrayList<UserMapEntry>>() {
					
					public void onSuccess(ArrayList<UserMapEntry> result) 
					{
						if( null == result )
						{
							error("No mapping found for user " + logger0.getUserLogin() );
							return;
						}
						
		//				TODO : cahce : map can be cached here
						CorrProtocolBase.this.setUserMap(result);
						fillUserTypes();
					}
					
					public void onFailure(Throwable caught) 
					{
						error( "An error occured while retrieving the user-map for " + logger0.getUserLogin() + ". Please try filling the " + fdn(CorrProtocolBase.this.getFieldsMap().get(LoggerFieldName).getBaFieldName()) + " again. Error msg : " + caught.getMessage() );
						return ;
					}
				});
				
			}
			else
			{
				boolean retValue = fillUserTypes();
				returnValue = (returnValue && retValue);
			}
			
			return returnValue;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Log.error("Exception in changeUserTypes : ", e);
			error("Exception occured in prefilling. Please try refreshing the page or fill the form manually.");
			restoreForm();
			return false;
		}
	}

	private boolean setAgencyAndOriginator(UserClient logger0) 
	{
		boolean gRetValue = setGenAgency(logger0);
		boolean oRetValue = setOriginator(logger0);
		
		return (gRetValue && oRetValue);
	}

	private boolean setOriginator(UserClient logger0) 
	{
		if( this.getFormType() == FormType.UPDATE )
		{
			// don't do anything
			return true;
		}
		else
		{
			if( null != origAgencyType )
			{
				boolean retValue =	setValue(origAgencyType.getName(), logger0.getLocation());
				if( retValue == false )
				{
					error("The field " + fdn(this.getFieldsMap().get(OriginatorFieldName).getBaFieldName() + " was not set properly."));
				}
				return retValue ;
			}
		}
		
		return true;
	}

	private boolean setGenAgency(UserClient logger0) {
		if( null != genAgencyType )
		{
			boolean retValue = setValue(genAgencyType.getName(), logger0.getLocation());
			if( retValue == false )
			{
				error(fdn(this.getFieldsMap().get(GenerationAgencyFieldName).getBaFieldName() + " was be set properly for user : " + logger0.getUserLogin()));
			}
			return retValue;
		}
		
		return true;
	}

	protected void openPDF(TbitsTreeRequestData ttrd) 
	{
//		System.out.println("Now I will generate pdf.");
//		Window.alert("Please wait while we generate preview for you.");
		CorrConst.corrDBService.getPDFUrl(ttrd, new AsyncCallback<String>()
		{
			public void onFailure(Throwable caught) 
			{
				Window.alert("Preview generation failed because of follwing reason :\n" + caught.getMessage());
			}

			public void onSuccess(String result) 
			{				
				Window.open(  result , "_blank", "");
			}			
		});
	}
	
	private boolean fillUserTypes()
	{
		String t1 = null;
		String t2 = null;
		String t3 = null;
		if( umType1FC != null )
		{
			TypeClient t = umType1FC.getValue();
			if( null != t )
				t1 = t.getName() ;
			else
			{
				error("Please select a value in " + fdn(this.getFieldsMap().get(UserMapType1).getBaFieldName()));
				return false;
			}
		}
		
		if( umType2FC != null )
		{
			TypeClient t = umType2FC.getValue();
			if( null != t )
				t2 = t.getName() ;
			else 
			{
				error("Please select a value in " + fdn(this.getFieldsMap().get(UserMapType2).getBaFieldName()));
				return false;
			}
		}
		
		if( umType3FC != null )
		{
			TypeClient t = umType3FC.getValue();
			if( null != t )
				t3 = t.getName() ;
			else 
			{
				error("Please select a value in " + fdn(this.getFieldsMap().get(UserMapType3).getBaFieldName()));
				return false;
			}
		}
		
		if( null == this.getUserMap() )
		{
			error("Please select a value in " + fdn(this.getFieldsMap().get(LoggerFieldName).getBaFieldName()));
			return false ;
		}
		
		HashMap<String, ArrayList<String>> usersMap = new HashMap<String,ArrayList<String>>();
		for( UserMapEntry ume : this.getUserMap() )
		{
			if( (ume.getType1() == null && t1 == null) || ( null != ume.getType1() && null != t1 && ume.getType1().equals(t1) ) )
			{
				if( (ume.getType2() == null && t2 == null) || ( null != ume.getType2() && null != t2 && ume.getType2().equals(t2) ) )
				{
					if( (ume.getType3() == null && t3 == null) || ( null != ume.getType3() && null != t3 && ume.getType3().equals(t3) ) )
					{
						String userTypeFieldName = ume.getUserTypeFieldName();
						String userLogin = ume.getUserLoginValue();
						ArrayList<String> users = usersMap.get(userTypeFieldName);
						if( null == users )
							users = new ArrayList<String>();
						
						users.add(userLogin);
						usersMap.put(userTypeFieldName, users);
					}
				}
			}
		}
		
		Set<String> fields = usersMap.keySet() ;
		boolean returnValue = true ;
		for( Iterator<String> fieldIter =  fields.iterator() ; fieldIter.hasNext() ; )
		{
			String fieldName = fieldIter.next();
			String userList = getCommaSeparatedString(usersMap.get(fieldName));
			boolean retValue = setValue(fieldName, userList);
			if( retValue == false )
			{
				error("Value " + userList + " might not have got set properly in field " + fieldName );
			}
			returnValue = (returnValue && retValue );
		}
		
		setRecepientAgency();
		
		return returnValue ;
	}

	private boolean setRecepientAgency() 
	{
		if( null != receAgencyType && null != recepUP )
		{
			ArrayList<UserClient> users = null;
			try {
				users = CorrHelperClient.getUserClients(sysPrefix, recepUP);
			} catch (UserNotFoundException e) {
				error(e.getMessage());
			}
			if( null != users && users.size() != 0 )
			{
				UserClient firstRec = users.get(0);
				boolean retValue = setValue(receAgencyType.getName(), firstRec.getLocation());
				if( false == retValue )
				{
					error("The field : " + fdn(this.getFieldsMap().get(RecepientAgencyFieldName).getBaFieldName() + " might not be set correctly."));
				}
				return retValue;
			}
			else
			{
				error( "Please fill a valid user in : " + fdn( this.getFieldsMap().get(RecepientUserTypeFieldName).getBaFieldName()));
				return false;
			}
		}
		else 
		{
			return true;
		}
	}

	protected void initialize() 
	{
		try
		{
			preInitialize() ;
			
			boolean success = true ;
	
			StringBuffer errors = new StringBuffer() ;
			CorrConst.corrDBService.getInitializingParams(this.getSysPrefix(), ClientUtils.getCurrentUser().getUserLogin() , new AsyncCallback<HashMap<String,Object>>()
					{
						public void onFailure(Throwable caught) 
						{
							error(PREFILL_FAILED + " : for reason : " + caught.getMessage());
						}
	
						public void onSuccess(HashMap<String, Object> result) 
						{
							if( null == result )
							{
								error(PREFILL_FAILED);
								return;
							}
							else
							{
								HashMap<String,FieldNameEntry> fieldsMap = (HashMap<String, FieldNameEntry>) result.get(InitFieldNameMap);
								if( null == fieldsMap )
								{
									error(PREFILL_FAILED + " unable to get the fieldsMap for ba : " + CorrProtocolBase.this.getSysPrefix());
									return;
								}
								CorrProtocolBase.this.setFieldsMap(fieldsMap);
								
								registerWithCorrHelper();
								//       type1         type2          type3   loggers
								HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> obMap = (HashMap<String, HashMap<String, HashMap<String, Collection<String>>>>) result.get(InitOnBehalfMap);
	//							ArrayList<OnBehalfClient> obArr = (ArrayList<OnBehalfClient>) result.get(InitOnBehalfList);
								
								//  TODO :remove this when client side plugins are ready : as this should be checked by the plugin
								if( null == obMap )
								{
									error("You(" + ClientUtils.getCurrentUser().getUserLogin() + ") are not allowed to create correspondence on any one's behalf.");
									disableButtons();
									return;
								}
	
								CorrProtocolBase.this.setOnBehalfMap(obMap);
								
								HashMap<String,ProtocolOptionEntry> optionMap = (HashMap<String, ProtocolOptionEntry>) result.get(InitOptionsMap);
								if( null == optionMap )
								{
									// just create an empty optionMap
									optionMap = new HashMap<String, ProtocolOptionEntry>();
								}
								CorrProtocolBase.this.setOptionMap(optionMap);
								
								try 
								{
									addPreviewButton();
									firstTimeConfiguration();
									registerDisableProtocol();
									handleInitialProtocolStatus();
//									registerProtocol() ;
								} catch (CorrNotAllowedException e) {
									disableButtons();
									error(e);
								}
							}								
						}							
					}) ;
					
			postInitialize( success ) ;
		}
		catch(Exception e)
		{
			error(e);
			restoreForm();
		}
	}

	protected void handleInitialProtocolStatus() 
	{
		if( null != disableProtocolFC )
		{
			TypeClient type = disableProtocolFC.getValue();
			if( null != type )
			{
				handleDisableProtocolChange(type.getName());
			}
			else
			{
				registerProtocol();
			}
		}
		else
		{
			registerProtocol();
		}
	}

	protected void error(Exception e) 
	{
		error(e.getMessage());
		Log.error("Exception Details : " ,e);
	}

	protected void registerWithCorrHelper() 
	{
		// register the bafields
		CorrHelperClient.registerBAFields(this.getSysPrefix(), this.getBaFields());
		// register configs
		CorrHelperClient.registerFields(this.getSysPrefix(), this.getConfigs());
	}		

	protected void postInitialize(boolean success) {
		// TODO Auto-generated method stub
		
	}

	protected void preInitialize() {
		// TODO Auto-generated method stub
		
	}

	protected boolean validateRequestData( String fieldsStr, StringBuffer error) 
	{
		String [] fields = fieldsStr.split(",");
		for( String field : fields)
		{
			IFieldConfig ifc = configs.get(field);
			if( null == ifc )
			{
				error.append( noPerm(fn(field)));
				return false ;			
			}
			else
			{
				Widget w = ifc.getWidget() ;
				if( null == w )
				{
					error.append( noPerm(fn(field)));
					return false ;
				}
			}				
		}

		return true ;
	}
	
	public boolean beforeSubmit()
	{
		String message = "You are about to create a correspondence ";
		
		if( null != this.getOptionMap().get(GenericParams.ProtShowConfirmationOnSubmit) && this.getOptionMap().get(GenericParams.ProtShowConfirmationOnSubmit).getValue().equals(GenericParams.ProtShowConfirmationOnSubmit_Yes))
		{
			String gcfv = null;
			if( null != this.getFieldsMap().get(GenericParams.GenerateCorrespondenceFieldName)  )
			{
				String fieldName = this.getFieldsMap().get(GenericParams.GenerateCorrespondenceFieldName).getBaFieldName() ;//genCorrFieldNamesMap.get(sysPrefix);
				if( null != fieldName )
				{
					TypeFieldConfig fieldConfig = (TypeFieldConfig) this.getConfigs().get(fieldName);
					if( null != fieldConfig )
					{
						TypeFieldControl gctfc = fieldConfig.getWidget();
						if( null != gctfc )
						{
							TypeClient tc = gctfc.getValue();
							if( null != tc )
								message +=  "with " + fieldConfig.getBaField().getDisplayName() + " set to " + tc.getDisplayName();  
						}
					}
				}
			}
			
			message += ".\nPlease click Ok to submit.";
			return Window.confirm(message);
		}
		
		return true;
	}
	
}

class CorrNotAllowedException extends TbitsExceptionClient
{
	public CorrNotAllowedException(String message) 
	{
		super(message);
	}
}
