package com.tbitsGlobal.admin.client.widgets.pages;


import java.util.ArrayList;

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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tbitsGlobal.admin.client.services.JobActionService;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobClassClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobParameterClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * this class creates an edit/create tab 
 * for editing/creating a job
 *
 * @author Kshitiz 
 *
 */
abstract public class JobEditorForm extends TabItem{
	private static final int DELETE_PARAM_COLUMN = 3;
	private static final int PARAM_VALUE_COLUMN = 2;
	private static final int PARAM_COLUMN = 1;

	private String mode;

	private SelectionChangedListener<JobClassClient> classComboListener;
	private SelectionListener<ButtonEvent> paramAddButtonHandler;
	private SelectionListener<ButtonEvent> saveListener;


	private ComboBox<JobClassClient> classCombo = new ComboBox<JobClassClient>();

	private TextField<String> otherClassField = new TextField<String>();
	private TextField<String> groupField= new TextField<String>();
	private TextField<String> nameField = new TextField<String>();
	private TextArea descField = new TextArea();
	private FlexTable fx = new FlexTable();
	private ToolBarButton paramAddButton = new ToolBarButton();
	private CronExpressionForm cronForm;

	private JobClassClient selectedClass;
	private JobDetailClient jobDetail;
	private String prevJobName = "";
	private String prevJobGroup = "";
	private ListStore<JobParameterClient> paramStore; 
	private CheckBoxGroup jobAttributesCheckboxgroup = new CheckBoxGroup();
	//	private int paramCount = 1; 

	public JobEditorForm(String title,String mode) {
		super(title);
		this.mode = mode;
		setClosable(true);
		setScrollMode(Scroll.AUTO);
		defineHandlers();
		cronForm = new CronExpressionForm(mode);
		setValueinClassCombo();
	}
	public JobEditorForm(String title,String mode, JobDetailClient jobDetail) {
		this(title,mode);
		this.jobDetail = jobDetail;

	}
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		LayoutContainer container = new LayoutContainer(new ColumnLayout());
		//		LayoutContainer container = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
		FormPanel fp = new FormPanel();
		//		fp.setBodyBorder(false);
		fp.setHeading("Enter Job details");
		classCombo.setFieldLabel("Job Class");
		classCombo.setLabelSeparator(" ");
		otherClassField.setFieldLabel("other job class");
		otherClassField.setLabelSeparator(" ");
		groupField.setFieldLabel("group");
		groupField.setLabelSeparator(" ");
		nameField.setFieldLabel("name");
		nameField.setLabelSeparator(" ");
		descField.setFieldLabel("description");
		descField.setLabelSeparator(" ");  
		paramAddButton = new ToolBarButton("Add more parameters", paramAddButtonHandler);

		jobAttributesCheckboxgroup.setName("jobAttributes");
		jobAttributesCheckboxgroup.setLabelSeparator(":");
		jobAttributesCheckboxgroup.setFieldLabel("Job Attributes");

		CheckBox isDurable = new CheckBox();
		jobAttributesCheckboxgroup.add(isDurable);
		isDurable.setBoxLabel("durable");
		isDurable.setHideLabel(true);
		isDurable.setName("cbxDurable");
		isDurable.setToolTip("Whether or not the Job should remain stored after it is orphaned " +
		"(no Triggers point to it).If not explicitly set, the default value is false. ");

		CheckBox isVolatile = new CheckBox();
		isVolatile.setHideLabel(true);
		isVolatile.setBoxLabel("volatile");
		isVolatile.setName("cbxVolatile");
		jobAttributesCheckboxgroup.add(isVolatile);
		isVolatile.setToolTip("Whether or not the Job should not be persisted in the" +
				" JobStore for re-use after program restarts." +
				"If not explicitly set, the default value is false. ");


		CheckBox isRecoverable = new CheckBox();
		isRecoverable.setHideLabel(true);
		isRecoverable.setBoxLabel("recoverable");
		isRecoverable.setName("cbxRecoverable");
		jobAttributesCheckboxgroup.add(isRecoverable);
		isRecoverable.setToolTip("Set whether or not the the Scheduler should re-execute the Job if a 'recovery' or 'fail-over' " +
				"situation is encountered." +
				"If not explicitly set, the default value is false. ");

		fp.add(classCombo,new FormData("100%"));
		fp.add(otherClassField,new FormData("100%"));
		fp.add(groupField,new FormData("100%"));
		fp.add(nameField,new FormData("100%"));
		fp.add(descField,new FormData("100%"));
		fp.add(jobAttributesCheckboxgroup);
		fp.add(fx,new FormData("100%"));
		fp.add(paramAddButton);
		fp.setStyleAttribute("padding", "10px");
		container.add(fp, new ColumnData(0.50));
		//		container.add(fp, new RowData(0.4,-1,new Margins(10,10,10,10)));

		container.add(cronForm, new ColumnData(0.35));
		cronForm.setStyleAttribute("padding", "10px");
		//		container.add(cronForm, new RowData(0.4,-1,new Margins(10,10,10,10)));

		add(container);
		ButtonBar bb = new ButtonBar();
		bb.setAlignment(HorizontalAlignment.CENTER);
		ToolBarButton saveButton = new ToolBarButton("Save Job" , saveListener);
		bb.add(saveButton);
		if(mode == JobActionService.EDIT_JOB){
			ToolBarButton revertButton = new ToolBarButton("Revert" , new SelectionListener<ButtonEvent>(){
				public void componentSelected(ButtonEvent ce) {
					setValueinClassCombo();
				}
			});
			bb.add(revertButton);
		}

		LayoutContainer l = new LayoutContainer();
		l.setStyleAttribute("margin-bottom", "30px");
		l.add(bb);
		add(l);
		classCombo.addSelectionChangedListener(classComboListener);
		if(selectedClass == null)
			paramAddButton.disable();
		//		if(mode == JobActionService.EDIT_JOB){
		//			setJobDetailsInFields();
		//		}
	}
	private void defineHandlers(){
		classComboListener = new SelectionChangedListener<JobClassClient>(){
			public void selectionChanged(SelectionChangedEvent<JobClassClient> se) {
				selectedClass = se.getSelectedItem();
				if(selectedClass != null){
					paramAddButton.enable();
					makeDefaultParamContainer();
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
						JobDetailClient jdc = new JobDetailClient();
						jdc.setJobName(nameField.getValue());
						jdc.setJobClassName(classCombo.getValue().getClassName());
						jdc.setJobGroup(groupField.getValue());
						jdc.setDescription(descField.getValue());
						jdc.setCronExpression(cronForm.getCronExpression());
						jdc.setEndDate(cronForm.getEndDate());
						jdc.setStartDate(cronForm.getStartDate());
						for(Field<?> cbf:jobAttributesCheckboxgroup.getAll()){
							if(cbf.getName().equals("cbxVolatile")) jdc.setVolatility(((CheckBox)cbf).getValue());
							else if(cbf.getName().equals("cbxDurable")) jdc.setDurability(((CheckBox)cbf).getValue());
							else if(cbf.getName().equals("cbxRecoverable")) jdc.setRequestRecovery(((CheckBox)cbf).getValue());

						}

						ArrayList<JobParameterClient> jpcList = new ArrayList<JobParameterClient>();
						for(int row = 1 ; row < fx.getRowCount() ; row++){
							JobParameterClient jpc = new JobParameterClient();
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
								jpc.setValues(paramValue);
							}
							else 
								jpc.setValues(null);
							jpcList.add(jpc);
						}
						APConstants.apService.saveJob(mode, jdc, jpcList,prevJobName , prevJobGroup , new AsyncCallback<Boolean>(){
							public void onFailure(Throwable caught) {
								TbitsInfo.error(caught.getMessage(), caught);
							}
							public void onSuccess(Boolean result) {
								reloadGrid();
								TbitsInfo.info("job saved successfully");
							}
						});

					}

				};
	}
	public void setJobDetailsInFields(){
		if(jobDetail != null){
			prevJobGroup = jobDetail.getJobGroup();
			prevJobName  = jobDetail.getJobName();
			groupField.setValue(prevJobGroup);
			nameField.setValue(prevJobName); 
			descField.setValue(jobDetail.getDescripton());
			cronForm.setField(jobDetail);
			setValueInParamContainer(jobDetail.getJobParameters());
			for(Field<?> cbf:jobAttributesCheckboxgroup.getAll()){
				if(cbf.getName().equals("cbxVolatile")) ((CheckBox)cbf).setValue(jobDetail.isVolatile());
				else if(cbf.getName().equals("cbxDurable")) ((CheckBox)cbf).setValue(jobDetail.isDurable());
				else if(cbf.getName().equals("cbxRecoverable"))((CheckBox)cbf).setValue(jobDetail.requestRecovery());

			}
		}
	}

	private void setValueinClassCombo(){
		classCombo.setStore(new ListStore<JobClassClient>());
		classCombo.setDisplayField(JobClassClient.DISPLAY_NAME);
		classCombo.setEditable(false);
		APConstants.apService.getJobClasses(new AsyncCallback<ArrayList<JobClassClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error(caught.toString(), caught);
			}
			public void onSuccess(ArrayList<JobClassClient> result) {
				for(JobClassClient jcc : result){
					classCombo.getStore().add(jcc);
				}
				if(mode == JobActionService.CREATE_JOB)
					classCombo.setValue(classCombo.getStore().getAt(0));
				else if(mode == JobActionService.EDIT_JOB){
					classCombo.setValue(classCombo.getStore().
							findModel(JobClassClient.CLASS_NAME, jobDetail.getJobClassName()));
				}
			}});
	}
	private void makeDefaultParamContainer(){
		APConstants.apService.getJobParams(selectedClass.getClassName(),"", new AsyncCallback<ArrayList<JobParameterClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed",caught);
			}

			public void onSuccess(ArrayList<JobParameterClient> result) {
				paramStore = new ListStore<JobParameterClient>();
				fx.removeAllRows();
				fx.setText(0, PARAM_COLUMN, "parameter");
				fx.setText(0, PARAM_VALUE_COLUMN, "value");
				for( JobParameterClient jpc : result){
					HorizontalPanel hp = new HorizontalPanel();
					hp.setSpacing(10);
					TextField<String> paramField = new TextField<String>();
					paramField.setValue(jpc.getName());
					paramField.setReadOnly(true);
					fx.setWidget(fx.getRowCount(), PARAM_COLUMN, paramField);
					Field valueField = null;
					Object defaultValue = null;
					if(mode == JobActionService.CREATE_JOB);
					defaultValue = jpc.getDefaultValue();
					if(jpc.getType().equalsIgnoreCase(JobParameterClient.Integer)){
						valueField = new TextField<Integer>();
						if(defaultValue!= null && defaultValue instanceof Number)
							valueField.setValue((Number)(defaultValue));
					}
					else if(jpc.getType().equalsIgnoreCase( JobParameterClient.TextArea)){
						valueField = new TextArea();
						if(defaultValue!= null && defaultValue instanceof String)
							valueField.setValue((String)(defaultValue));
					}
					else if(jpc.getType() .equalsIgnoreCase(JobParameterClient.CheckBox)){
						valueField = new CheckBox();
						if(defaultValue!= null && defaultValue instanceof Boolean)
							valueField.setValue((Boolean)(defaultValue));
					}
					else if(jpc.getType().equalsIgnoreCase(JobParameterClient.Text)){
						valueField = new TextField<String>();
						if(defaultValue!= null && defaultValue instanceof String)
							valueField.setValue((String)(defaultValue));
					}
					else if(jpc.getType().equalsIgnoreCase(JobParameterClient.Select)){
						valueField = new SimpleComboBox<String>();
						((SimpleComboBox)valueField).setEditable(false);
						ArrayList<String> values = (ArrayList<String>) jpc.getValues();
						if(values != null)
							for(String val : values){
								((SimpleComboBox)valueField).add(val);
							}
						if(defaultValue != null && defaultValue instanceof String)
							((SimpleComboBox)valueField).setSimpleValue(((String)defaultValue));
						else
							((SimpleComboBox)valueField).setValue(((SimpleComboBox)valueField)
									.getStore().getAt(0));
					}
					if(valueField == null)
						valueField = new TextField<String>();
					valueField.setWidth(250);
					if(jpc.getIsMandatory() && valueField instanceof TextField){
						fx.setText(fx.getRowCount() - 1, PARAM_VALUE_COLUMN + 1, "*");
						//((TextField)valueField).setAllowBlank(false);
					}
					fx.setWidget(fx.getRowCount() - 1, PARAM_VALUE_COLUMN, valueField);
					jpc.set("row", fx.getRowCount() - 1);
					paramStore.add(jpc);
				}
				if(mode == JobActionService.EDIT_JOB && jobDetail
						.getJobClassName().equalsIgnoreCase(selectedClass
								.getClassName())){
					//					setValueInParamContainer(jobDetail.getJobParameters());
					setJobDetailsInFields();
				}
			}

		});
	}
	private void setValueInParamContainer(ArrayList<JobParameterClient> jpList){
		for(JobParameterClient jpc : jpList){
			if(jpc.getValues() == null)
				continue;
			JobParameterClient jpOfClass = null;
			if(paramStore != null)
				jpOfClass = paramStore.findModel(JobParameterClient.NAME, jpc.getName());
			if(jpOfClass != null){
				Widget w = fx.getWidget((Integer)jpOfClass.get("row"), PARAM_VALUE_COLUMN);
				if(w instanceof CheckBox){
					((CheckBox)w).setValue(((String)jpc.getValues()).equalsIgnoreCase("true"));
				}
				else if(w instanceof SimpleComboBox){
					((SimpleComboBox)w).setRawValue((String)jpc.getValues());
				}
				else
					((Field)w).setValue(jpc.getValues());
			}
			else{
				TextField<String> temp = new TextField<String>();
				temp.setValue(jpc.getName());
				fx.setWidget(fx.getRowCount(), PARAM_COLUMN , temp);
				temp = new TextField<String>();
				temp.setWidth(250);
				temp.setValue((String)jpc.getValues());
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
	}


	public abstract void reloadGrid();
}
