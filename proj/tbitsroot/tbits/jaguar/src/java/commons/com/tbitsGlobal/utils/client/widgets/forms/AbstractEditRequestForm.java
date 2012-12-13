package commons.com.tbitsGlobal.utils.client.widgets.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.OnDeleteDraft;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentFieldContainer;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentGrid;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.AttachmentFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.BooleanComboFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.CheckBoxFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.DateFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.NumberFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.BrowseFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TextAreaFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TextFieldFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.TypeFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.UserPickerFieldConfig;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IEditRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;

/**
 * Form Panel used to Add and Update Requests.
 * 
 * @author sourabh
 *
 */
public abstract class AbstractEditRequestForm extends AbstractRequestForm implements IEditRequestForm{
	public static String CONTEXT_DATA_TYPE		=	"data_type";

	private boolean dbOperationsEnabled;
	
	/**
	 * The Submit Button;
	 */
	protected Button submitBtn;
	
	protected Button resetBtn;
	
	protected Button draftBtn;
		
	private final String draftBtnText = "Save Draft";
	
	protected RTEditor editor;
	
	protected int draftId;
	
	protected Timer draftTimer;
	
	protected boolean isSourceDraft;
	
	protected boolean submitDone = false;
	
	private HashMap<BAField,HashMap<Integer, HashMap<BAField, List<TypeClient>>>> affectedTypesMap = new HashMap<BAField,HashMap<Integer, HashMap<BAField, List<TypeClient>>>>(); 
	
	private HashMap<BAField,ListStore<BAField>> affectedFieldsMap = new HashMap<BAField,ListStore<BAField>>();  
	/**
	 * Map of all {@link AttachmentGrid}.
	 */
	protected HashMap<String, AttachmentFieldContainer> attachmentFieldContainers;
	
	@Override
	public void onComponentEvent(ComponentEvent ce) {
		super.onComponentEvent(ce);
		if(dbOperationsEnabled){
			if(ce.getEventTypeInt() == Event.ONKEYPRESS){
				if(ce.getKeyCode() == KeyCodes.KEY_ENTER && ce.isControlKey()){
					onSubmit();
				}
			}
		}
	}
	
	/**
	 * Construtor
	 * 
	 * @param parentContext
	 */
	public AbstractEditRequestForm(UIContext parentContext){
		super(parentContext);
		
		dbOperationsEnabled = true;
		
		attachmentFieldContainers = new HashMap<String, AttachmentFieldContainer>();
		
		isSourceDraft = false;
		if(myContext.hasKey(IRequestFormData.CONTEXT_DRAFT)){
			UserDraftClient draft = myContext.getValue(IRequestFormData.CONTEXT_DRAFT, UserDraftClient.class);
			draftId = draft.getDraftId();
			isSourceDraft = true;
		}
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		if(dbOperationsEnabled){
			submitBtn = new Button("Submit", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					onSubmit();
				}
			});
			this.addButton(submitBtn);
			submitBtn.disable();
			
			resetBtn = new Button("Reset",new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					headingPanel.getMessageBox().error("Form reset");
					resetForm();
				}
			});
			this.addButton(resetBtn);
			
			draftBtn = new Button(draftBtnText, new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					saveDraft();
				}
			});
			this.addButton(draftBtn);
						
		}
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.calculateAndApplyDependencies();
		
		
		final TabItem parentTab = myContext.getValue(CONTEXT_PARENT_TAB, TabItem.class);
		if(parentTab != null){
			parentTab.addListener(Events.BeforeClose, new Listener<TabPanelEvent>(){
				Listener<TabPanelEvent> l = this;
				
				public void handleEvent(TabPanelEvent be) {
					if(dbOperationsEnabled){
						if(draftId > 0 && !submitDone){
							be.setCancelled(true);
							MessageBox confirm = new MessageBox();
							confirm.setButtons(MessageBox.YESNOCANCEL);
							confirm.setTitle("Discard Draft");
							confirm.setMessage("Do you want to save the changes? Clicking 'No' would not delete the draft.");
							confirm.addCallback(new Listener<MessageBoxEvent>(){
								public void handleEvent(MessageBoxEvent be) {
									if(be.getButtonClicked().getText().toLowerCase().equals("no")){
	//									deleteDraft();
									}else if(be.getButtonClicked().getText().toLowerCase().equals("cancel")){
										return;
									}else{
										saveDraft();
									}
									parentTab.removeListener(Events.BeforeClose, l);
									draftTimer.cancel();
									parentTab.close();
									
								}});
							confirm.show();
						}
					}
				}});
		}
	}
	
	@Override
	protected void afterRender() {
		super.afterRender();
		
		if(dbOperationsEnabled){
			draftTimer = new Timer(){
				@Override
				public void run() {
					saveDraft();
				}};
			draftTimer.scheduleRepeating(30 * 1000);
			
			submitBtn.enable();
		}
	}

	@Override
	protected void create() {
		super.create();
		
		ListStore<BAField> fields = this.getData().getBAFields();
		if(fields != null){
			if(fields.findModel(BAField.NAME, IFixedFields.DESCRIPTION) != null){
				BAField descField = fields.findModel(BAField.NAME, IFixedFields.DESCRIPTION);
				if(!hasBAFieldPermission(descField))
					return;
				editor = new RTEditor();
				LabelField labelField = new LabelField();
				labelField.setFieldLabel(descField.getDisplayName());
				labelField.setLabelStyle("font-weight:bold");
				form.add(labelField, new FormData("-20"));
				form.add(editor, new FormData("-20"));	
				
				TbitsTreeRequestData requestModel = this.getData().getRequestModel();
				if(requestModel != null && shouldFillField(descField)){
					POJO pojo = requestModel.getAsPOJO(descField.getName());
					if(pojo != null)
						editor.setHTML(pojo.toString());
				}
			}
		}
	}
	
	/**
	 * override this method if you want to do some more validation of your own.
	 * if it returns false then the the request won't submit
	 * else it will go ahead with submission
	 * @param requestModel
	 * @return
	 */
	protected boolean beforeSubmit(TbitsTreeRequestData requestModel)
	{
		if(submitBtn != null)
			submitBtn.disable();
		return true ;
	}
	/**
	 * prepares the data and performs the add or update operation.
	 */
	public void onSubmit()
	{
		if(dbOperationsEnabled){
			for(AttachmentFieldContainer attContainer : attachmentFieldContainers.values())
			{
				if(attContainer.getInProgressUploads() + attContainer.getQueuedUploads() > 0)
				{
					Window.alert("File Uploads in progess. \nPlease wait till they finish or cancel them first.");
					return;
				}
			}
			
			// complete any compulsory validation before - submit
			TbitsTreeRequestData requestModel = this.createRequestModel() ;
			if( requestModel == null )
			{
				return ;		
			}
			
			// call the before submit hooks
			if( beforeSubmit(requestModel) == false )
			{
				return ;
			}
	
			GlobalConstants.utilService.addRequest(requestModel, this.getData().getSysPrefix(),  new AsyncCallback<TbitsTreeRequestData>(){	
				public void onFailure(Throwable caught) {
					//TODO: propery display the error message. The error that is thrown by tBits comprised of an array of exception objects
					//These objects need to be enumerated and error message concatenated. Rightnow, if a rule need to display an error, it is not possible.
					//IT is extremely unfriendly. For every validation check, the user would end up calling system admin
					
					TbitsInfo.error("Error while submitting request. Please see error messages at the top of the tab.", caught);
					AbstractEditRequestForm.this.getHeadingPanel().getMessageBox().error(caught.getMessage(), false);
					Log.error("Error while submitting request...", caught);
					submitBtn.enable();
				}
	
				public void onSuccess(TbitsTreeRequestData result) {
					String dataType = myContext.hasKey(CONTEXT_DATA_TYPE)?
							myContext.getValue(CONTEXT_DATA_TYPE, String.class):Captions.getRecordDisplayName();
					
					if(result != null){
						TbitsInfo.info(dataType + " Added ");
						submitDone = true;
						
						// Close the tab
						if(myContext.hasKey(CONTEXT_PARENT_TAB)){
							TabItem parentTab = myContext.getValue(CONTEXT_PARENT_TAB, TabItem.class);
							parentTab.close();
						}
						
						// Stop the timer
						draftTimer.cancel();
						
						if(draftId > 0){ // Delete draft if present
							deleteDraft();
						}
						
						int requestId = result.getRequestId();
						String sysPrefix = getData().getSysPrefix();
						afterSubmit(sysPrefix, requestId);
					}
					else
						TbitsInfo.error("Unknown error occurred while submitting the " + dataType);
					
					submitBtn.enable();
				}
			});	
		}
	}
	
	public void afterSubmit(String sysPrefix, int requestId){}
	
	/**
	 * Should the field be filled with value in requestModel
	 * @param baField
	 * @return
	 */
	protected abstract boolean shouldFillField(BAField baField );
	
	/**
	 * Creates a field in the form
	 * 
	 * @param baField. 
	 */
	@SuppressWarnings("unchecked")
	protected LayoutContainer createField(BAField baField, IFieldConfig config) {
		if(config == null)
	    	return null;
		
		if(!hasBAFieldPermission(baField))
			return null;
		
		// The containing panel
		LayoutContainer panel = new LayoutContainer();  
		FormLayout layout = new FormLayout();
		layout.setLabelAlign(LabelAlign.RIGHT);
		layout.setLabelWidth(150);
		panel.setLayout(layout);
	    panel.setBorders(false);
	    FormData formData = new FormData("-10");
	    
	    fieldConfigs.put(baField.getName(), config);
	    Widget editorWidget = config.getWidget();
    	panel.add(editorWidget, formData); 
    	
    	if(editorWidget instanceof AttachmentFieldContainer)
    		attachmentFieldContainers.put(baField.getName(), (AttachmentFieldContainer) editorWidget);
    	
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null){
	    	fillField(requestModel, baField, config);
	    }
		
	    return panel;
	}
	
	/**
	 * This method checks for the dependencies among the type fields and adds {@link SelectionChangedListener}s to the target fields.
	 */
	protected void calculateAndApplyDependencies(){
		ListStore<BAField> fields = this.getData().getBAFields();
		
		if(fields != null){
			for(final BAField baField : fields.getModels()){
				if(baField instanceof BAFieldCombo){ // It is a type field
					// Source field config
					IFieldConfig srcFieldConfig = fieldConfigs.get(baField.getName());
					if(srcFieldConfig != null && srcFieldConfig instanceof TypeFieldConfig){
						// All the dependencies from this field
						List<TypeDependency> dependencies = ((BAFieldCombo)baField).getDependencies();
						
						if(dependencies.size() > 0){
							// Track the fields which are affected by this field so that we can reset them to default everytime this field is changed.
							final ListStore<BAField> affectedFields = new ListStore<BAField>();
							
							/*
							 *  Map of all the changes in types that have to done when a type is selected
							 *  Map id of this nature : HashMap<src_type_id, HashMap<target_field,List<target_type>>>
							 */
							final HashMap<Integer, HashMap<BAField, List<TypeClient>>> affectedTypes = new HashMap<Integer, HashMap<BAField, List<TypeClient>>>();
							for(TypeDependency dependency : dependencies){
								int srcTypeId = dependency.getSrcTypeId();
								int targetFieldId = dependency.getDestFieldId();
								int targetTypeId = dependency.getDestTypeId();
								
								// See if already added to affected fields
								BAField targetField = affectedFields.findModel(BAField.FIELD_ID, targetFieldId);
								
								if(targetField == null){ // not yet added to affected fields
									for(BAField field : fields.getModels()){ // find in cache
										if(field.getFieldId() == targetFieldId && field instanceof BAFieldCombo){
											affectedFields.add(field); // add to affected fields
											targetField = field;
											break;
										}
									}
								}
								
								if(targetField != null){
									List<TypeClient> targetFieldTypes = ((BAFieldCombo) targetField).getTypes();
									if(targetFieldTypes != null){
										// Look for targetTypeId in targeFieldTypes and put it in affected types
										for(TypeClient targetFieldType : targetFieldTypes){
											if(targetFieldType.getTypeId() == targetTypeId){
												if(!affectedTypes.containsKey(srcTypeId))
													affectedTypes.put(srcTypeId, new HashMap<BAField, List<TypeClient>>());
												if(!affectedTypes.get(srcTypeId).containsKey(targetField))
													affectedTypes.get(srcTypeId).put(targetField, new ArrayList<TypeClient>());
												affectedTypes.get(srcTypeId).get(targetField).add(targetFieldType);
											}
										}
									}
								}
							}
							
							affectedFieldsMap.put(baField, affectedFields);
							affectedTypesMap.put(baField, affectedTypes);
							
							/*
							 * Now that we have collected all the info. Let's put the handle
							 */
							TypeFieldControl srcTypeControl = ((TypeFieldConfig)srcFieldConfig).getWidget();
							srcTypeControl.addSelectionChangedListener(new SelectionChangedListener<TypeClient>(){
								@Override
								public void selectionChanged(SelectionChangedEvent<TypeClient> se) {
									TypeClient tc = se.getSelectedItem();
									changeSelection(baField,tc);
									
							}});
							
							// set the type dependencies for the initial values.
//							String currType = this.getData().getRequestModel().getAsString(baField.getName());
							List<TypeClient> currentTypeClientList = srcTypeControl.getSelection();
							if( null != currentTypeClientList )
							{
								TypeClient currentTypeClient = currentTypeClientList.iterator().next(); // consider only one type is selected.
								changeSelection(baField, currentTypeClient);
							}
							
						}
					}
				}
			}
		}
	}
	
		private void changeSelection(BAField baField, TypeClient srcType)
		{
//			TypeClient srcType = 
			if(srcType != null){
				ListStore<BAField> affectedFields = affectedFieldsMap.get(baField);
				HashMap<Integer, HashMap<BAField, List<TypeClient>>> affectedTypes = affectedTypesMap.get(baField);
				// Set all the affected fields to default
				for(BAField field : affectedFields.getModels()){
					IFieldConfig fieldConfig = fieldConfigs.get(field.getName());
					if(fieldConfig != null){
						TypeFieldControl typeControl = ((TypeFieldConfig)fieldConfig).getWidget();
						TypeClient currentValue = typeControl.getValue();
						typeControl.getStore().removeAll();
						typeControl.getStore().add(((BAFieldCombo) field).getTypes());
						if( null != currentValue )
							typeControl.setValue(currentValue);
						else
							typeControl.setValue(((BAFieldCombo) field).getDefaultValue());
					}
				}
				
				// Now change the stores of the target fields
				int srcTypeId = srcType.getTypeId();
				HashMap<BAField, List<TypeClient>> targetTypeMap = affectedTypes.get(srcTypeId);
				if(targetTypeMap != null){
					for(BAField targetField : targetTypeMap.keySet()){
						IFieldConfig targetFieldConfig = fieldConfigs.get(targetField.getName());
						if(targetFieldConfig != null){
							// The type control to be affected
							TypeFieldControl targetTypeControl = ((TypeFieldConfig)targetFieldConfig).getWidget();
							
							// Backup the current value
							TypeClient currentValue = targetTypeControl.getValue();
							
							// List of Types to be set in target control
							List<TypeClient> targetTypes = targetTypeMap.get(targetField);
							if(targetTypes != null && targetTypes.size() > 0){
								// Remove all types and add the valid ones
								targetTypeControl.getStore().removeAll();
								targetTypeControl.getStore().add(targetTypes);
								
								/*
								 *  If currentValue is one amongst the valid values, set it again
								 *  else clear the control
								 */
								if(currentValue != null && targetTypeControl.getStore().findModel(TypeClient.NAME, currentValue.getName()) != null)
									targetTypeControl.setValue(currentValue);
								else 
									targetTypeControl.clearSelections();
							}
						}
					}
				}
			}
		}
	
	/**
	 * Gathers data from all the fields.
	 * 
	 * @return The model.
	 */
	@SuppressWarnings("unchecked")
	public TbitsTreeRequestData createRequestModel()
	{
		TbitsTreeRequestData requestModel = new TbitsTreeRequestData(); // empty model
		
		// Collect POJOs and put them in model
		for(IFieldConfig config : fieldConfigs.values()){ 
			POJO pojo = config.getPOJO();
			if(pojo != null)
				requestModel.set(config.getName(), config.getPOJO());
		}
		
		// special treatment of description as it is not included in fieldConfigs
		if(editor != null)
		{
			String description = editor.getHTML();
			Log.info("Description **************** start *****************\n" + description + "\nDescription  ************************** end *******************\n");
			requestModel.set(DESCRIPTION, description );
		}
		
		String sysPrefix = this.getData().getSysPrefix();
		BusinessAreaClient ba = ClientUtils.getBAbySysPrefix(sysPrefix);
		if( null == ba )
		{
			TbitsInfo.error("Cannot find business_area with sys_prefix : " + sysPrefix);
			return null;
		}
		requestModel.set(BUSINESS_AREA, ba.getSystemId()); // set the sys_id
		requestModel.set(USER, ClientUtils.getCurrentUser().getUserId()); // set the user_id
		
		TbitsTreeRequestData model = this.getData().getRequestModel();
		if(model != null){ // It is an update request
			requestModel.setRequestId(model.getRequestId());
			requestModel.setMaxActionId(model.getMaxActionId());
		}
		
		return requestModel;
	}
	
	public void saveDraft(){
		TbitsTreeRequestData model = this.createRequestModel();
		GlobalConstants.utilService.saveUserDraft(draftId, this.getData().getSysPrefix(), model, new AsyncCallback<Integer>(){
			public void onFailure(Throwable caught) {
				Log.error(caught.getMessage(), caught);
				TbitsInfo.error("Error saving draft.., Please see logs for details.", caught);
				draftBtn.setText(draftBtnText + " (Error saving draft... Please see logs for details.)");
			}

			public void onSuccess(Integer result) {
				if(result != 0){
					draftId = result;
					Log.info("draft saved DraftId : " + result);
					draftBtn.setText(draftBtnText + " (Last saved at " + DateTimeFormat.getMediumTimeFormat().format(new Date()) + ")");
				}
			}});
	}
	
	public void deleteDraft(){
		GlobalConstants.utilService.deleteUserDraft(ClientUtils.getSysPrefix(), draftId, new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				Log.error("Couldn't delete draft Id : " + draftId, caught);
			}

			public void onSuccess(Boolean result) {
				if(!result){
					Log.error("Couldn't delete draft Id : " + draftId);
				}else{
					Log.info("Deleted draft Id : " + draftId);
					TbitsEventRegister.getInstance().fireEvent(new OnDeleteDraft(draftId));
				}
			}});
		
	}
	
	@SuppressWarnings("unchecked")
	public IFieldConfig getConfig(BAField baField) {
		if(baField.getName().equals(RELATED_REQUESTS)){
			return new BrowseFieldConfig(baField);
		}else if(baField instanceof BAFieldAttachment){
			TbitsTreeRequestData requestModel = this.getData().getRequestModel();
			String sysPrefix = this.getData().getSysPrefix();
			return new AttachmentFieldConfig(Mode.EDIT, sysPrefix, requestModel, (BAFieldAttachment) baField);
		}else if(baField instanceof BAFieldCheckBox){
			if(GXT.isIE)
				return new BooleanComboFieldConfig((BAFieldCheckBox) baField);
			return new CheckBoxFieldConfig((BAFieldCheckBox) baField);
		}else if(baField instanceof BAFieldCombo){
			return new TypeFieldConfig((BAFieldCombo) baField);
		}else if(baField instanceof BAFieldDate){
			return new DateFieldConfig((BAFieldDate) baField);
		}else if(baField instanceof BAFieldInt){
			return new NumberFieldConfig((BAFieldInt) baField);
		}else if(baField instanceof BAFieldMultiValue){
			return new UserPickerFieldConfig((BAFieldMultiValue) baField);
		}else if(baField instanceof BAFieldString){
			return new TextFieldFieldConfig((BAFieldString) baField);
		}else if(baField instanceof BAFieldTextArea){
			return new TextAreaFieldConfig((BAFieldTextArea) baField);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void resetForm(){
		
		// Clear all except for the attachment type fields
		for(IFieldConfig config : fieldConfigs.values()){
			if(!(config instanceof AttachmentFieldConfig))
				config.clear();
		}
		
		fillForm();
	}
	
	protected void fillForm(){
		// fills the form in case of update request
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null){
			for(BAField baField : this.getData().getBAFields().getModels()){
				fillField(requestModel, baField, fieldConfigs.get(baField.getName()));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void fillField(TbitsTreeRequestData requestModel, BAField baField, IFieldConfig config){
		if(shouldFillField(baField)){
			if(config == null)
				config = fieldConfigs.get(baField.getName());
			if(config != null){
				POJO pojo = requestModel.getAsPOJO(baField.getName());
				if(pojo != null)
					config.setPOJO(pojo);
				else{ // Set default values where applicable
					if(baField instanceof BAFieldCombo){
						TypeClient defaultValue = ((BAFieldCombo) baField).getDefaultValue();
						if(defaultValue != null)
							config.setPOJO(new POJOString(defaultValue.getName()));
					}
				}
			}
		}
	}

	/**
	 * True to enable submit and draft operations. False to disable
	 * @param dbOperationsEnabled
	 */
	public void setDbOperationsEnabled(boolean dbOperationsEnabled) {
		this.dbOperationsEnabled = dbOperationsEnabled;
	}

	public boolean isDbOperationsEnabled() {
		return dbOperationsEnabled;
	}
}
