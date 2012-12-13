package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;



import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

import com.tbitsGlobal.admin.client.modelData.EscalationConditionDetailClient;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionParametersClient;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.services.EscalationConditionService;

import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;


import commons.com.tbitsGlobal.utils.client.domainObjects.JobParameterClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

abstract public class EscalationConditionEditorForm extends TabItem {
	
	private static final int DELETE_PARAM_COLUMN = 3;
	private static final int PARAM_VALUE_COLUMN = 2;
	private static final int PARAM_COLUMN = 1;
	private String mode;
	
	
	private SelectionChangedListener<EscalationHierarchiesClient> escHierarchyComboListener;
	private SelectionListener<ButtonEvent> paramAddButtonHandler;
	private SelectionListener<ButtonEvent> saveListener;
	private ComboBox<EscalationHierarchiesClient> escHirCombo = new ComboBox<EscalationHierarchiesClient>();

	private TextField<String> BA = new TextField<String>();
	private TextField<String> dql= new TextField<String>();
	private TextField<String> disNAME = new TextField<String>();
	private TextArea description = new TextArea();
	private TextField<String> srcUSerField = new TextField<String>();
	private TextField<String> srcDateField = new TextField<String>();
	private TextField<String> destUserField = new TextField<String>();
	private TextField<String> destDateField = new TextField<String>();
	private TextField<String> OnbehalfUser = new TextField<String>();
	private TextField<String> span = new TextField<String>();
	private EscalationConditionDetailClient escCondDetail;
	private FlexTable fx = new FlexTable();
	private ToolBarButton paramAddButton = new ToolBarButton();
	private EscalationHierarchiesClient selectedHierarchy;
	//private ListStore<EscalationConditionParameters> paramStore; 
	private CheckBoxGroup condAttributesCheckboxgroup = new CheckBoxGroup();
	
	
	public EscalationConditionEditorForm(String title,String mode) {
		super(title);
		this.mode = mode;
		setClosable(true);
		setScrollMode(Scroll.AUTO);
		defineHandlers();
		setValueInEscHirCombo();
	}
	
	public EscalationConditionEditorForm(String title,String mode,EscalationConditionDetailClient escCondDetail) {
		this(title,mode);
		this.escCondDetail =escCondDetail;

	}
	
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		LayoutContainer container = new LayoutContainer(new ColumnLayout());
		//		LayoutContainer container = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
		FormPanel fp = new FormPanel();
		//		fp.setBodyBorder(false);
		FormPanel paramForm=new FormPanel();
		paramForm.setHeading("Need more Params..?");
		fp.setHeading("Enter Escalation Condition details");
		escHirCombo.setFieldLabel("Escalation Hierarchy");
		escHirCombo.setLabelSeparator(" ");
		disNAME.setFieldLabel("Display Name");
		disNAME.setLabelSeparator(" ");
		description.setFieldLabel("Description");
		description.setLabelSeparator(" ");
		dql.setFieldLabel("DQL");
		dql.setLabelSeparator(" ");
		BA.setFieldLabel("Business Area Prefix");
		BA.setLabelSeparator(" ");
		srcDateField.setFieldLabel("Source date field");
		srcDateField.setLabelSeparator(" ");
		srcUSerField.setFieldLabel("Source user field");
		srcUSerField.setLabelSeparator(" ");
		destDateField.setFieldLabel("Destination date field");
		destDateField.setLabelSeparator(" ");
	    destUserField.setFieldLabel("Destination user field");
		destUserField.setLabelSeparator(" ");
		OnbehalfUser.setFieldLabel("ON Behalf User");
		OnbehalfUser.setLabelSeparator(" ");
		span.setFieldLabel("SPAN");
		span.setLabelSeparator(" ");
		fx.setText(0, PARAM_COLUMN, "Param Name");
		fx.setText(0, PARAM_VALUE_COLUMN, "Param value");
		fx.setText(0, DELETE_PARAM_COLUMN, "Remove");
		paramAddButton = new ToolBarButton("Add more parameters", paramAddButtonHandler);

	    condAttributesCheckboxgroup.setName("ConditionStatus");
		condAttributesCheckboxgroup.setLabelSeparator(":");
		condAttributesCheckboxgroup.setFieldLabel("Condition Status");

		CheckBox isActive = new CheckBox();
		condAttributesCheckboxgroup.add(isActive);
		isActive.setBoxLabel("Active");
		isActive.setName("isActiveCond");
		isActive.setToolTip("Condition is active or not ");
		
		fp.add(disNAME,new FormData("100%"));
		fp.add(description,new FormData("100%"));
		fp.add(escHirCombo,new FormData("100%"));
		fp.add(BA,new FormData("100%"));
		fp.add(dql,new FormData("100%"));
		fp.add(srcDateField,new FormData("100%"));
		fp.add(srcUSerField,new FormData("100%"));
		fp.add(destDateField,new FormData("100%"));
		fp.add(destUserField,new FormData("100%"));
		fp.add(span,new FormData("100%"));
		fp.add(OnbehalfUser,new FormData("100%"));
		fp.add(condAttributesCheckboxgroup);
		
		paramForm.add(fx,new FormData("90%"));
		paramForm.add(paramAddButton);
		paramForm.setStyleAttribute("padding", "10px");
		
		
		fp.setStyleAttribute("padding", "10px");
		container.add(fp, new ColumnData(.50));
		
		container.add(paramForm,new ColumnData(.45));
		
		add(container);
		ButtonBar bb = new ButtonBar();
		bb.setAlignment(HorizontalAlignment.CENTER);
		ToolBarButton saveButton = new ToolBarButton("Save Condition" , saveListener);
		bb.add(saveButton);
		if(mode == EscalationConditionService.EDIT_CONDITION){
			ToolBarButton revertButton = new ToolBarButton("Revert" , new SelectionListener<ButtonEvent>(){
				public void componentSelected(ButtonEvent ce) {
					setValueInEscHirCombo();
				}
			});
			bb.add(revertButton);
		}

		LayoutContainer l = new LayoutContainer();
		l.setStyleAttribute("margin-bottom", "30px");
		l.add(bb);
		add(l);
		escHirCombo.addSelectionChangedListener(escHierarchyComboListener);
		if(selectedHierarchy == null)
			paramAddButton.disable();
		
	}
	
	private void defineHandlers(){
		escHierarchyComboListener = new SelectionChangedListener<EscalationHierarchiesClient>(){
			public void selectionChanged(SelectionChangedEvent<EscalationHierarchiesClient> se) {
				selectedHierarchy = se.getSelectedItem();
				if(selectedHierarchy != null){
					paramAddButton.enable();
					setConditionDetailsInFields();
				}
			}};
			paramAddButtonHandler = new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					TextField<String> temp = new TextField<String>();
					fx.setWidget(fx.getRowCount() , PARAM_COLUMN , temp);
					temp = new TextField<String>();
					temp.setWidth(250);
					fx.setWidget(fx.getRowCount() - 1 , PARAM_VALUE_COLUMN , temp);
					//				fx.setText(paramCount, DELETE_PARAM_COLUMN, "delete");
					ToolBarButton delButton = new ToolBarButton("Delete", 
							new SelectionListener<ButtonEvent>(){
						@Override
						public void componentSelected(ButtonEvent ce) {
							String id = ce.getButton().getId();
							for(int row = 1; row <  fx.getRowCount() ; row++){
								if(fx.getCellCount(row) < 4)
									continue;
								Widget w = fx.getWidget(row, DELETE_PARAM_COLUMN);
								if(w == null)
									continue;
								if(! (w instanceof ToolBarButton))
									continue;
								if(((ToolBarButton)w).getId().equals(id)){
									fx.removeRow(row);
									//								paramCount --;
								}
							}

						}
					});
					delButton.getId();
					fx.setWidget(fx.getRowCount() - 1, DELETE_PARAM_COLUMN, delButton);
				}};

				saveListener = new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent ce) {
						EscalationConditionDetailClient ecdc = new EscalationConditionDetailClient();
						if(escCondDetail!=null)
							ecdc.setEscCondId(escCondDetail.getEscCondId());
						
						ecdc.setDisName(disNAME.getValue());
						ecdc.setDescription(description.getValue());
						ecdc.setEscHierarchy(escHirCombo.getValue());
						ecdc.setSrcBa(BA.getValue());
						ecdc.setSrcDateField(srcDateField.getValue());
						ecdc.setSrcUserField(srcUSerField.getValue());
						ecdc.setDesDateField(destDateField.getValue());
						ecdc.setDesUserField(destUserField.getValue());
						ecdc.setSpan(span.getValue());
						ecdc.setOnBehalfUser(OnbehalfUser.getValue());
						ecdc.setDql(dql.getValue());
						for(Field<?> cbf:condAttributesCheckboxgroup.getAll()){
							if(cbf.getName().equals("isActiveCond")) ecdc.setIsActive(((CheckBox)cbf).getValue());
							

						}

						ArrayList<EscalationConditionParametersClient> jpcList = new ArrayList<EscalationConditionParametersClient>();
						for(int row = 1 ; row < fx.getRowCount() ; row++){
							EscalationConditionParametersClient jpc = new EscalationConditionParametersClient();
							String paramName = ((TextField<String>)fx.getWidget(row, PARAM_COLUMN)).getValue();
							if(paramName == null)
								continue;
							jpc.setName(paramName.trim());
							Field f  = ((Field)fx.getWidget(row, PARAM_VALUE_COLUMN));
							if(f.getValue() != null){
								String paramValue = "";
								if(f instanceof SimpleComboBox)
									paramValue = (((SimpleComboBox)f).getRawValue());
								else
									paramValue = (f.getValue().toString());
								jpc.setValue(paramValue);
							}
							else 
								jpc.setValue(null);
							jpcList.add(jpc);
						}
						APConstants.apService.saveCondition(mode,ecdc, jpcList,new AsyncCallback<Boolean>(){
							public void onFailure(Throwable caught) {
								TbitsInfo.error(caught.getMessage(), caught);
							}
							public void onSuccess(Boolean result) {
								reloadGrid();
								TbitsInfo.info("Escalation condition saved successfully");
							}
						});

					}

				};
	}
	
	
	private void setValueInEscHirCombo()
	{
		escHirCombo.setStore(new ListStore<EscalationHierarchiesClient>());
		escHirCombo.setDisplayField(EscalationHierarchiesClient.DISPLAY_NAME);
		escHirCombo.setEditable(false);
		APConstants.apService.getEscalationHierarchies(new AsyncCallback<List<EscalationHierarchiesClient>>() {

			@Override
			public void onFailure(Throwable caught) {

				TbitsInfo.error(
						"Error in fetching escalations hierarchies",
						caught);
				Log.error("Error in fetching escalations hierarchies",
						caught);

			}

			@Override
			public void onSuccess(List<EscalationHierarchiesClient> result) {
				for(EscalationHierarchiesClient eh : result){
					escHirCombo.getStore().add(eh);
				}
				if(mode == EscalationConditionService.CREATE_CONDITION)
					escHirCombo.setValue(escHirCombo.getStore().getAt(0));
				else if(mode == EscalationConditionService.EDIT_CONDITION){
					escHirCombo.setValue(escHirCombo.getStore().
							findModel(EscalationHierarchiesClient.ESC_ID, escCondDetail.getEscHierarchy().getEscId()));
				}

			}
		});
	}
	
	
	
	public void setConditionDetailsInFields()
	{
		if(escCondDetail!=null)
		{
			disNAME.setValue(escCondDetail.getDisName());
			description.setValue(escCondDetail.getDescription());
			escHirCombo.setValueField(escCondDetail.getEscHierarchy().ESC_ID);
			BA.setValue(escCondDetail.getSrcBa());
			srcUSerField.setValue(escCondDetail.getSrcUserField());
			srcDateField.setValue(escCondDetail.getSrcDateField());
			destUserField.setValue(escCondDetail.getDesUserField());
			destDateField.setValue(escCondDetail.getDesDateField());
			dql.setValue(escCondDetail.getDql());
			span.setValue(escCondDetail.getSpan());
			OnbehalfUser.setValue(escCondDetail.getOnBehalfUser());
			for(Field<?> cbf:condAttributesCheckboxgroup.getAll()){
				if(cbf.getName().equals("isActiveCond")) ((CheckBox)cbf).setValue(escCondDetail.getIsActive());
			}
			setValueInParamContainer(escCondDetail.getParams());
		}
	}
	
	private void setValueInParamContainer(ArrayList<EscalationConditionParametersClient> ecpList){
		for(EscalationConditionParametersClient ecp : ecpList){
			if(ecp.getValues() == null)
				continue;
				TextField<String> temp = new TextField<String>();
				temp.setValue(ecp.getName());
				fx.setWidget(fx.getRowCount(), PARAM_COLUMN , temp);
				temp = new TextField<String>();
				temp.setWidth(250);
				temp.setValue((String)ecp.getValues());
				fx.setWidget(fx.getRowCount() - 1 , PARAM_VALUE_COLUMN , temp);
				ToolBarButton delButton = new ToolBarButton("Delete", 
						new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						String id = ce.getButton().getId();
						for(int row = 1; row <  fx.getRowCount() ; row++){
							if(fx.getCellCount(row) < 4)
								continue;
							Widget w = fx.getWidget(row, DELETE_PARAM_COLUMN);
							if(w == null)
								continue;
							if(! (w instanceof ToolBarButton))
								continue;
							if(((ToolBarButton)w).getId().equals(id)){
								fx.removeRow(row);
								//								paramCount --;
							}
						}

					}
				});
				delButton.getId();
				fx.setWidget(fx.getRowCount() - 1, DELETE_PARAM_COLUMN, delButton);
			}
		}
	
	public abstract void reloadGrid();

}
