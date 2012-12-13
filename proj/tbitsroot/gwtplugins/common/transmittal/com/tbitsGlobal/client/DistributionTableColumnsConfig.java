package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

public class DistributionTableColumnsConfig {

	
	private WizardData dataObject;
	
	public WizardData getDataObject() {
		return dataObject;
	}

	public void setDataObject(WizardData dataObject) {
		this.dataObject = dataObject;
	}

	private static final String FIELD_CONFIG = "field_config";
	private static final String JSON_KEY_VALUE = "value";
	private static final String JSON_KEY_NAME = "name";
	private static final String NAME = "name";
	private static final String DISPLAY_NAME = "display_name";
	
	
	HashMap<String, BAField> baFieldMap = new HashMap<String, BAField>();
	EditorGrid<TbitsTreeRequestData> grid = null;
		
	ArrayList<ColumnConfig> configs = null;
	public DistributionTableColumnsConfig(WizardData params){
		this.configs = new ArrayList<ColumnConfig>();
		
		//Add the first column representing the serial number.
		RowNumberer rn = new RowNumberer();
		rn.setId("serialNo");
		rn.setHeader("Sl. No.");
		rn.setWidth(20);		
		configs.add(rn);
		this.setDataObject(params);
	}
	
	public DistributionTableColumnsConfig(){
		this.configs = new ArrayList<ColumnConfig>();
		
		//Add the first column representing the serial number.
		RowNumberer rn = new RowNumberer();
		rn.setId("serialNo");
		rn.setHeader("Sl. No.");
		rn.setWidth(20);		
		configs.add(rn);
	
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<ColumnConfig> configureColumns(){
		//this.isDefaultConfiguration = false;
		ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) getDataObject().getData().get("distributionTableColumnsList");
/*
		Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {		
			public int compare(TbitsModelData o1, TbitsModelData o2) {
				
				if ((o1 != null) && (o2 != null)){
					int s1 = (Integer)o1.get(TransmittalConstants.COLUMN_ORDER);
					int s2 = (Integer)o2.get(TransmittalConstants.COLUMN_ORDER);
					if (s1 > s2)
						return 1;
					else if (s1 == s2)
						return 0;
					else if (s1 < s2)
						return -1;
				}
				return 0;
			}
		};
		//Sort the column info, before creating column configs out of them. So, that they maintain the sort order and 
		//hence the column order in the table.
		Collections.sort(distributionDataColumns, comp);
*/			
		//Create the column configs, based on whether the field is mapped to corresponding field in the originating
		//DCR BA or a custom field.
		for(TbitsModelData tmd :distributionDataColumns)
		{
			int width = 200;
			ColumnConfig columnConfig = new ColumnConfig();
			String name = (String)tmd.get(TransmittalConstants.NAME_COLUMN);
			String displayName = (String)tmd.get(DISPLAY_NAME);
			if ((displayName == null) || (displayName.trim().equals("")))
				displayName = name;
			columnConfig.setHeader(displayName);
			columnConfig.setId(name);
						
			if (tmd != null && (Boolean)tmd.get("is_active") )
			{
//				int fieldId = (Integer)tmd.get(TransmittalConstants.FIELD_ID_COLUMN);
//				if (fieldId == 0)
//				{
					boolean isEditorEnabled = (Boolean)tmd.get(TransmittalConstants.IS_EDITABLE);
					if (!isEditorEnabled)
					{
						columnConfig.setWidth(width);
						columnConfig.setRenderer(new GridCellRenderer<TbitsModelData>() {
							public Object render(TbitsModelData model, String property,
									ColumnData config, int rowIndex, int colIndex,
									ListStore<TbitsModelData> store,
									Grid<TbitsModelData> grid)
							{
								return model.get(property);
							}			
						});
						this.configs.add(columnConfig);
					}
					else
					{
						int dataTypeId = (Integer)tmd.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
						//String name = (String)tmd.get(TransmittalConstants.NAME_COLUMN);
						
							if(dataTypeId == TransmittalConstants.INT ){
								columnConfig.setWidth(60);
								TextField<String> textField = new TextField<String>();
								
								columnConfig.setEditor(new TbitsCellEditor(textField));
								columnConfig.setWidth(50);
								columnConfig.getEditor().setWidth(25);
								columnConfig.getEditor().setCompleteOnEnter(true);
								
								columnConfig.setRenderer(new GridCellRenderer<TbitsModelData>() {
									public Object render(TbitsModelData model, String property,
											ColumnData config, int rowIndex, int colIndex,
											ListStore<TbitsModelData> store,
											Grid<TbitsModelData> grid) {

										return model.get(property);
									}
								});
								
								configs.add(columnConfig);
								
							}
							else if (dataTypeId== TransmittalConstants.TEXT||dataTypeId== TransmittalConstants.STRING)
							{
								columnConfig.setWidth(120);
								final String  defaultValue = (String)tmd.get(FIELD_CONFIG);
								columnConfig.setRenderer(new GridCellRenderer<TbitsModelData>() {
									public Object render(TbitsModelData model, String property,
											ColumnData config, int rowIndex, int colIndex,
											ListStore<TbitsModelData> store,
											Grid<TbitsModelData> grid) {
	
										final TbitsModelData tmpModel = model;
										final String tmpProperty = property;
										TextField<String> textField = new TextField<String>(){
											TextField<String> thisRef = this;
											DelayedTask task = new DelayedTask(new Listener<FieldEvent>(){
												public void handleEvent(FieldEvent be) {
													tmpModel.set(tmpProperty, thisRef.getValue());
												}});
	
											protected void onKeyUp(FieldEvent fe) {
												super.onKeyUp(fe);
												task.delay(500);
											}
										};
	
										textField.setWidth(35);
										model.set(property, defaultValue);
										textField.setValue((String)model.get(property));						
										return textField;
									}
								});
								configs.add(columnConfig);
								
							}
							else if(dataTypeId== TransmittalConstants.TYPE)
							{
	
								columnConfig.setWidth(100);								
								//columnConfig.setEditor(comboEditor);
								final TbitsModelData tmpTmd = tmd;
								columnConfig.setRenderer(new GridCellRenderer<TbitsModelData>() {
										
									public Object render(TbitsModelData model,
											String property, ColumnData config,
											int rowIndex, int colIndex,
											ListStore<TbitsModelData> store,
											Grid<TbitsModelData> grid) {
										HashMap<String, String> typesMap = new HashMap<String, String>();
										String typesJsonString = (String)tmpTmd.get(FIELD_CONFIG);// (String)TransmittalConstants.transmittalProcessParams.get(trnProcessParam);
										if (typesJsonString != null){	
											try
											{											
												fetchKeyValuePairsfromJsonString(typesMap, typesJsonString);
											}catch (JSONException je){
												TbitsInfo.error("Invalid value provided for configuring type field in circulation list.",
														je);
											}
										}
										else
											TbitsInfo.error("Appropriate parameter for drop down values of column: " + 
													(String)tmpTmd.get(TransmittalConstants.NAME_COLUMN) + ", was not found for " +
													"the current transmittal process.");
										ComboBox<TbitsModelData> comboBoxField = getComboBoxField(property, typesMap, model);
										comboBoxField.setWidth(80);
										return comboBoxField;
									}
								});
	
								configs.add(columnConfig);
								
							}
						}
					}
//				}
			}
		

		return this.configs;
	}
	
	private ComboBox<TbitsModelData> getComboBoxField(final String name, HashMap<String, String> valueMap,final TbitsModelData model) 
	{		
		ComboBox<TbitsModelData> comboBoxField = new ComboBox<TbitsModelData>();	
		comboBoxField.setStore(new ListStore<TbitsModelData>());
		comboBoxField.setName(name);
		comboBoxField.setFieldLabel(name);
		comboBoxField.setLabelStyle("font-weight:bold");
		comboBoxField.setDisplayField(NAME);//"name");
		
		if(valueMap != null){
			TbitsModelData tmd = null;
			for(String s:valueMap.keySet()){
				TbitsModelData tmpModel = new TbitsModelData();
				tmpModel.set(NAME, s);
				tmpModel.set(DISPLAY_NAME, valueMap.get(s));
				comboBoxField.getStore().add(tmpModel);
				if (tmd == null)
					tmd = tmpModel;
			}
			String dropDownValue=model.get(name);

			for (String str : valueMap.keySet())
			{
				if(str.equals(dropDownValue))
				{
					tmd.set(NAME, str);	tmd.set(DISPLAY_NAME, valueMap.get(str));
					
				}
			}
			
		
			
			comboBoxField.setValue(tmd);
		}
		comboBoxField.addSelectionChangedListener(new SelectionChangedListener<TbitsModelData>() {
			
			public void selectionChanged(SelectionChangedEvent<TbitsModelData> se) {				
				TbitsModelData selectedItem = se.getSelectedItem();
				if (se.getSource() instanceof ComboBox<?>)
				{
					ComboBox<?> source = (ComboBox<?>)se.getSource();
					source.setRawValue((String)selectedItem.get(NAME));
					model.set(name, (String)selectedItem.get(NAME));
				}
			}
		});
		
		return comboBoxField;
	}

	/**
	 * @param jsonMap
	 * @param jsonString
	 */
	@SuppressWarnings("deprecation")
	protected static void fetchKeyValuePairsfromJsonString(
			HashMap<String, String> jsonMap, String jsonString) throws JSONException{
		if (jsonString != null){
			//jsonMap.put("-", "-");
			JSONValue parsedJson = JSONParser.parse(jsonString);
			JSONArray jsonArray = parsedJson.isArray();
			if (jsonArray != null){
				for(int i=0; i < jsonArray.size(); i++){
					JSONObject jsonObj = jsonArray.get(i).isObject();
					if (jsonObj != null){
						String name = jsonObj.get(JSON_KEY_NAME).isString().stringValue();
						String value = jsonObj.get(JSON_KEY_VALUE).isString().stringValue();	
						jsonMap.put(value, name);
					}				
				}		
			}
		}
	}
}
