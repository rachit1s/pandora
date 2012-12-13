package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.widgets.DualListWindow;

/**
 * Window used for Customizing grid columns.
 * 
 * @author sourabh
 *
 */
public abstract class ColPrefCustomizeColWindow extends DualListWindow<ColPrefs>{

	public ColPrefCustomizeColWindow(List<ColPrefs> models,
			List<ColPrefs> currentModels, String displayProperty,
			String[] filterProperties) {
		super(models, currentModels, displayProperty, filterProperties, "Save Preferences");
	}
	
	protected void onSubmit() {
		List<ColPrefs> fields = targetGrid.getStore().getModels();
		for(ColPrefs field : fields){
			field.set(ColPrefs.COLUMN_SIZE, field.get(ColPrefs.COLUMN_SIZE));
		}
		save(fields);
	}
	
	protected void onRight(List<ColPrefs> models){
		for(ColPrefs model : models){
			model.set(ColPrefs.COLUMN_SIZE,200 );
			targetGrid.getStore().add(model);
			sourceList.getStore().remove(model);
		}
	}
	
	protected ListView<ColPrefs> createSourceList(List<ColPrefs> models, List<ColPrefs> currentModels, String displayProperty){
		ListView<ColPrefs> sourceList = new ListView<ColPrefs>();
		sourceList.setBorders(false);
		sourceList.setDisplayProperty(displayProperty);
		
		final ListStore<ColPrefs> sourceStore = new ListStore<ColPrefs>();
		sourceStore.add(models);
		for(ColPrefs pref : currentModels){
			ColPrefs c = sourceStore.findModel(ColPrefs.NAME, pref.getName());
			sourceStore.remove(c);
		}
		sourceStore.sort(displayProperty, SortDir.ASC);
		
//		final DelayedTask task = new DelayedTask(new Listener<StoreEvent<T>>(){
//			public void handleEvent(StoreEvent<T> be) {
//				sourceStore.sort(displayProperty, SortDir.ASC);
//			}});
//		
//		sourceStore.addStoreListener(new StoreListener<ColPrefs>(){
//			@Override
//			public void storeAdd(StoreEvent<ColPrefs> se) {
//				super.storeAdd(se);
//				task.delay(100);
//			}
//			
//			@Override
//			public void storeFilter(StoreEvent<ColPrefs> se) {
//				super.storeFilter(se);
//				task.delay(100);
//			}
//		});
		
		sourceList.setStore(sourceStore);
		
		return sourceList;
	}
	
	protected EditorGrid<ColPrefs> createTargetGrid(List<ColPrefs> models, List<ColPrefs> currentModels){
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
        
		CheckBoxSelectionModel<ColPrefs> sm = new CheckBoxSelectionModel<ColPrefs>();
		columns.add(sm.getColumn());
		
		
        columns.add(new ColumnConfig(ColPrefs.DISPLAY_NAME, "Column Name", 150));
        
    	ColumnConfig column = new ColumnConfig();
      	column.setId(ColPrefs.COLUMN_SIZE);  
        column.setHeader("Column Size");  
        column.setWidth(80);  
        
        SpinnerField colSize = new SpinnerField();
        colSize.setWidth(50);
        
        column.setEditor(new TbitsCellEditor(colSize){
        	@Override
        	public Object postProcessValue(Object value) {
        		Number num = (Number) value;
        		if(num != null)
        			return num.intValue();
        		return null;
        	}
        });
        columns.add(column);  
            
        ColumnModel cm = new ColumnModel(columns);
        
        ListStore<ColPrefs> targetStore = new ListStore<ColPrefs>();
        
        for(ColPrefs col: currentModels){
        	col.set(ColPrefs.COLUMN_SIZE, col.getColSize());
        	targetStore.add(col);
        }
		
        EditorGrid<ColPrefs> targetGrid = new EditorGrid<ColPrefs>(targetStore, cm);
		targetGrid.setBorders(false);
		targetGrid.setSelectionModel(sm);
		targetGrid.setLayoutData(new FitLayout());
		targetGrid.setAutoExpandColumn(ColPrefs.DISPLAY_NAME);
		 
		return targetGrid;
	}

	protected abstract void save(List<ColPrefs> fields);
}
