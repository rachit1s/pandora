package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * 
 * @author sourabh
 *
 * Window that enables a user to paste data from Excel to be imported to a Bulk Grid
 *
 * @param <M>
 */
public class ExcelImportWindow<M extends ModelData> extends Window{
	
	public interface Parser{
		/**
		 * @param value
		 * @return The parsed value
		 */
		public Object parse(String value);
	}
	
	private HashMap<String, Parser> parserMap;
	private HashMap<String, ExcelImportDataType> dataTypeMap;
	
	private AbstractBulkUpdatePanel<M> bulkUpdatePanel;
	private DualListField<ExcelImportTypeModel> dualListField;
	private BulkUpdateGridAbstract<M> grid;
	
	private ComboBox<ExcelImportTypeModel> typesCombo;
	private CheckBox uniqueCheck;
	
	private String defaultUniqueMatchingProperty;
	
	public ExcelImportWindow(AbstractBulkUpdatePanel<M> bulkUpdatePanel) {
		super();
		
		this.setHeading("Excel Import");
		this.setSize(com.google.gwt.user.client.Window.getClientWidth() - 100, com.google.gwt.user.client.Window.getClientHeight() - 100);
		this.setLayout(new BorderLayout());
		
		this.bulkUpdatePanel = bulkUpdatePanel;
		this.grid = bulkUpdatePanel.getNewBulkGrid(BulkGridMode.SINGLE);
		
		this.parserMap = new HashMap<String, Parser>();
		this.dataTypeMap = new HashMap<String, ExcelImportDataType>();
		
		this.grid.addListener(Events.Render, new Listener<ComponentEvent>(){
			public void handleEvent(ComponentEvent be) {
				ListStore<ExcelImportTypeModel> toStore = dualListField.getToList().getStore();
				
				ColumnModel colModel = ExcelImportWindow.this.grid.getColumnModel();
				List<ExcelImportTypeModel> types = generateTypesFromColumnModel(colModel);
				toStore.add(types);
				
				updateGridColumns();
				
				if(defaultUniqueMatchingProperty != null && 
						typesCombo.getStore().findModel(ExcelImportTypeModel.ID, defaultUniqueMatchingProperty) != null){
					typesCombo.setValue(typesCombo.getStore().findModel(ExcelImportTypeModel.ID, defaultUniqueMatchingProperty));
					uniqueCheck.setValue(true);
				}else{
					typesCombo.disable();
				}
			}});
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		dualListField = new DualListField<ExcelImportTypeModel>();
		dualListField.getFromField().setStore(new ListStore<ExcelImportTypeModel>());
		dualListField.getToList().setStore(new ListStore<ExcelImportTypeModel>());
		dualListField.getFromList().setDisplayField(ExcelImportTypeModel.DISPLAY_NAME);
		dualListField.getToField().setDisplayField(ExcelImportTypeModel.DISPLAY_NAME);
		
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 250);
		westData.setMargins(new Margins(0, 2, 0, 0));
		westData.setSplit(true);
		this.add(dualListField, westData);
		
		dualListField.getToField().getStore().addStoreListener(new StoreListener<ExcelImportTypeModel>(){
			@Override
			public void storeAdd(StoreEvent<ExcelImportTypeModel> se) {
				super.storeAdd(se);
				
				updateGridColumns();
			}
			
			@Override
			public void storeRemove(StoreEvent<ExcelImportTypeModel> se) {
				super.storeRemove(se);
				
				updateGridColumns();
			}
			
			@Override
			public void storeClear(StoreEvent<ExcelImportTypeModel> se) {
				super.storeClear(se);
				
				updateGridColumns();
			}
		});
		
		ContentPanel gridContainer = new ContentPanel();
		gridContainer.setHeaderVisible(false);
		gridContainer.setLayout(new FitLayout());
		
		ToolBar toolbar = new ToolBar();
		final TextArea copyField = new TextArea();
		copyField.setHeight(20);
		copyField.setEmptyText("Paste Here");
		toolbar.add(copyField);
		
		toolbar.add(new ToolBarButton("Go", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				String value = copyField.getValue();
				copyField.clear();
				
				fillGrid(value);
			}
		}));
		
		toolbar.add(new SeparatorToolItem());
		
		toolbar.add(new ToolBarButton("Remove Selected", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<M> selectedItems = grid.getSelectionModel().getSelectedItems();
				if(selectedItems != null){
					for(M model : selectedItems){
						grid.getStore().remove(model);
					}
				}
			}}));
		
		toolbar.add(new ToolBarButton("Clear", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				grid.getStore().removeAll();
			}}));
		
		toolbar.add(new SeparatorToolItem());
		
		uniqueCheck = new CheckBox();
		uniqueCheck.setBoxLabel("Unique Matching ");
		toolbar.add(uniqueCheck);
		
		typesCombo = new ComboBox<ExcelImportTypeModel>();
		typesCombo.setDisplayField(ExcelImportTypeModel.DISPLAY_NAME);
		typesCombo.setStore(dualListField.getToList().getStore());
		toolbar.add(typesCombo);
		
		uniqueCheck.addListener(Events.Change, new Listener<FieldEvent>(){
			public void handleEvent(FieldEvent be) {
				boolean value = (Boolean) be.getValue();
				if(value)
					typesCombo.enable();
				else
					typesCombo.disable();
			}});
		
		gridContainer.setTopComponent(toolbar);
		gridContainer.add(grid, new FitData());
		
		gridContainer.addButton(new Button("Submit", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<M> models = grid.getStore().getModels();
				String uniqueMatchingField = null;
				if(uniqueCheck.getValue() && typesCombo.getValue() != null)
					uniqueMatchingField = typesCombo.getValue().getID();
				onSubmit(models, uniqueMatchingField);
			}}));
		
		this.add(gridContainer, new BorderLayoutData(LayoutRegion.CENTER));
	}
	
	/**
	 * Generate Column Types that can be imported from the Column Model of Bulk Grid
	 * 
	 * Note : if a column is marked fixed in Bulk Grid, it wont be imported
	 * 
	 * @param colModel
	 * @return
	 */
	protected List<ExcelImportTypeModel> generateTypesFromColumnModel(ColumnModel colModel){
		List<ColumnConfig> columns = colModel.getColumns();
		
		List<ExcelImportTypeModel> types = new ArrayList<ExcelImportTypeModel>();
		if(columns != null){
			for(ColumnConfig column : columns){
				if(!column.isFixed()){ 
					// || colModel.isCellEditable(colModel.getIndexById(column.getId())) || column instanceof CheckColumnConfig){ 
					// removed the condition for now
					ExcelImportTypeModel typeModel = getTypeModel(column);
					types.add(typeModel);
				}
			}
		}
		return types;
	}
	
	protected ExcelImportTypeModel getTypeModel(ColumnConfig column){
		ExcelImportTypeModel model = new ExcelImportTypeModel();
		model.setID(column.getId());
		model.setDisplayName(column.getHeader());
		
		ExcelImportDataType dataType = this.dataTypeMap.get(column.getId()); // find the specified data type for a column
		
		if(dataType == null) // hasn't been specified.. we try to guess
			dataType = guessDataType(column);
		
		if(dataType != null){
			model.setDataType(dataType);
		}
		return model;
	}
	
	/**
	 * Guesses the data type of column based on their editors
	 * @param column
	 * @return
	 */
	private ExcelImportDataType guessDataType(ColumnConfig column){
		if(column instanceof CheckColumnConfig){
			return ExcelImportDataType.Boolean;
		}
		
		CellEditor editor = column.getEditor();
		if(editor != null){
			Field field = editor.getField();
			if(field != null){
				if(field instanceof TextField || field instanceof TextArea || field instanceof ComboBox)
					return ExcelImportDataType.Text;
				if(field instanceof NumberField)
					return ExcelImportDataType.Number;
				if(field instanceof DateField)
					return ExcelImportDataType.Date;
			}
		}
		
		return ExcelImportDataType.Other;
	}
	
	/**
	 * Updates Columns in grid 
	 */
	private void updateGridColumns(){
		ListStore<ExcelImportTypeModel> toStore = dualListField.getToList().getStore();
		ColumnModel colModel = this.grid.getColumnModel();
		
		for(ColumnConfig column : colModel.getColumns()){
			boolean colFound = toStore.findModel(ExcelImportTypeModel.ID, column.getId()) != null;
			column.setHidden(!column.isFixed() && !colFound);
		}
		ExcelImportWindow.this.grid.getView().refresh(true);
	}
	
	/**
	 * Fills the grid with the pasted value
	 * @param value
	 */
	private void fillGrid(String value){
		//TODO: What if \n is commented?
		//TODO: what if the character are non-english?
		String[] rowValues = value.split("\n"); // Split rows
		if(rowValues != null){
			rowValues = processTokens(rowValues, "\n");
			ListStore<ExcelImportTypeModel> toStore = dualListField.getToList().getStore();
			List<M> models = new ArrayList<M>();
			for(String rowValue : rowValues){
				M model = bulkUpdatePanel.getEmptyModel();
				String[] colValues = rowValue.split("\t"); // Split Columns
				if(colValues != null){
					colValues = processTokens(colValues, "\t");
					for(int i = 0; i < colValues.length; i++){
						ExcelImportTypeModel type = toStore.getAt(i);
						if(type != null){
							Object parsedVal = parse(type, colValues[i]); // Parse the pasted value
							if(parsedVal != null)
								model.set(type.getID(), parsedVal);
						}
					}
				}
				models.add(model);
			}
			grid.getStore().add(models);
		}
	}
	
	private String[] processTokens(String[] tokens, String splitStr){
		int foundAnOpenString = -1;
		int count = 0;
		for(String token : tokens){
			if(hasAnOpenString(token)){
				if(foundAnOpenString != -1){
					return processTokens(mergeTokens(tokens, foundAnOpenString, count, splitStr), splitStr);
				}else{
					foundAnOpenString = count;
				}
			}
			count++;
		}
		return tokens;
	}
	
	private String[] mergeTokens(String[] tokens, int x, int y, String splitStr){
		String[] newTokens = new String[tokens.length - (y - x)];
		int count = 0;
		for(int i = 0; i < x; i++){
			newTokens[count] = tokens[i];
			count++;
		}
		String merged = "";
		for(int i = x; i <= y; i++){
			if(!merged.equals(""))
				merged += splitStr;
			merged += tokens[i];
		}
		newTokens[count] = merged;
		count++;
		for(int i = y + 1; i < tokens.length; i++){
			newTokens[count] = tokens[i];
			count++;
		}
		
		return newTokens;
	}
	
	private boolean hasAnOpenString(String str){
		int quoteFirstIndex = getNextOccurence(str, 0);
		if(quoteFirstIndex != -1){
			int quoteSecondIndex = getNextOccurence(str, quoteFirstIndex + 1);
			if(quoteSecondIndex == -1)
				return true;
			else
				return hasAnOpenString(str.substring(quoteSecondIndex + 1));
		}
		return false;
	}
	
	private int getNextOccurence(String str, int offset){
		int index = str.indexOf('"', offset);
		if(index != -1){
			if(index != str.length() - 1 && str.charAt(index + 1) == '"'){ // It is ""
				if(index != str.length() - 2)
					return getNextOccurence(str, index + 2);
				else return -1;
			}
		}
		return index;
	}
	
	/**
	 * Parses the value according to the type
	 * @param type
	 * @param value
	 * @return
	 */
	protected Object parse(ExcelImportTypeModel type, String value){
		String id = type.getID();
		if(id != null && this.parserMap.containsKey(id)){ // parse if custom parser has been specified
			Parser parser = this.parserMap.get(id);
			if(parser != null)
				return parser.parse(value);
		}else{ // Use Default strategies
			ExcelImportDataType dataType = type.getDataType();
			if(dataType == ExcelImportDataType.Text){
				return  value;// ClientUtils.htmlify(value);
			}else if(dataType == ExcelImportDataType.Boolean){ // (true == yes || y || 1 || t || true) (false == n || no || 0 || f || false)
				if(value.toLowerCase().equals("y") || value.toLowerCase().equals("yes") || 
						value.toLowerCase().equals("1") || value.toLowerCase().equals("t") || value.toLowerCase().equals("true"))
					return true;
				if(value.toLowerCase().equals("n") || value.toLowerCase().equals("no") || 
						value.toLowerCase().equals("0") || value.toLowerCase().equals("f") || value.toLowerCase().equals("false"))
					return false;
			}else if(dataType == ExcelImportDataType.Number){
				try{
					return Util.parseInt(value, 0);
				}catch(Exception e){
					Log.error("Couldn't parse number value : " + value, e);
				}
			}else if(dataType == ExcelImportDataType.Date){
				try{
					DateTimeFormat format = DateTimeFormat.getFormat(GlobalConstants.API_DATE_FORMAT);
					return format.parse(value);
				}catch(Exception e){
					Log.error("Couldn't parse date value : " + value, e);
				}
			}else if(dataType == ExcelImportDataType.Other){
				return value;
			}
		}
		return null;
	}
	
	public void setParser(String id, Parser parser){
		this.parserMap.put(id, parser);
	}
	
	public void removeParser(String id){
		this.parserMap.remove(id);
	}
	
	public void setDataType(String id, ExcelImportDataType dataType){
		this.dataTypeMap.put(id, dataType);
	}
	
	public void removeDataType(String id){
		this.dataTypeMap.remove(id);
	}
	
	/**
	 * Called when the Data is submitted to the Bulk Grid for Addition/Updation
	 * 
	 * @param models
	 * @param uniqueMatchingField. Null if no unique matching is required
	 */
	protected void onSubmit(List<M> models, String uniqueMatchingField){
		if(bulkUpdatePanel.beforeImportSubmit(models, uniqueMatchingField)){
			bulkUpdatePanel.onImportSubmit(models, uniqueMatchingField);
			this.hide();
		}
	}

	/**
	 * Sets default property for unique matching. Pre-Render
	 * @param defaultUniqueMatchingProperty
	 */
	public void setDefaultUniqueMatchingProperty(
			String defaultUniqueMatchingProperty) {
		this.defaultUniqueMatchingProperty = defaultUniqueMatchingProperty;
	}

	public String getDefaultUniqueMatchingProperty() {
		return defaultUniqueMatchingProperty;
	}

	public void setParserMap(HashMap<String, Parser> parserMap) {
		this.parserMap = parserMap;
	}

	public HashMap<String, Parser> getParserMap() {
		return parserMap;
	}

	public void setDataTypeMap(HashMap<String, ExcelImportDataType> dataTypeMap) {
		this.dataTypeMap = dataTypeMap;
	}

	public HashMap<String, ExcelImportDataType> getDataTypeMap() {
		return dataTypeMap;
	}
}
