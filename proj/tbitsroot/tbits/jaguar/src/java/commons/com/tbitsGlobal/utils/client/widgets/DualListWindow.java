package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;


/**
 * Window having a dual list type control.
 * 
 * @author sourabh
 *
 */
public abstract class DualListWindow<T extends TbitsModelData> extends Window{
	protected EditorGrid<T> targetGrid;
	protected ListView<T> sourceList;
	
	protected boolean enableOrdering = true;
	private List<T> models;
	private List<T> currentModels;
	private String displayProperty;
	private String[] filterProperties;
	
	public DualListWindow(List<T> models, List<T> currentModels, String displayProperty, final String[] filterProperties, String saveButtonCaption) {
		super();
		
		this.models = models;
		this.currentModels = currentModels;
		this.displayProperty = displayProperty;
		this.filterProperties = filterProperties;
		
		this.setSize(600, 400);
		this.setHeading("Customize Columns");
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.setModal(true);
		this.setOnEsc(true);
		
		this.addButton(new Button(saveButtonCaption, new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			}}));
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		this.createSourceContainer(models, currentModels, displayProperty, filterProperties);
		this.createButtonContainer();
		this.createTargetContainer(models, currentModels);
	}
	
	private void createSourceContainer(List<T> models, List<T> currentModels, String displayProperty, final String[] filterProperties){
		ContentPanel sourceListContainer = new ContentPanel(new FitLayout());
		sourceListContainer.setHeaderVisible(false);
		
		sourceList = this.createSourceList(models, currentModels, displayProperty);
		StoreFilterField<T> sourceFilterField = new StoreFilterField<T>(){
			protected boolean doSelect(Store<T> store, T parent, T record, String property, String filter) {
				for(String filterParam : filterProperties){
					if(((String)record.get(filterParam)).toLowerCase().contains(filter.toLowerCase()))
						return true;
				}
				return false;
			}};
		sourceFilterField.setWidth(180);
		sourceFilterField.bind(sourceList.getStore());
		
		ToolBar sourceListToolBar = new ToolBar();
		sourceListToolBar.add(new LabelToolItem("Filter : "));
		sourceListToolBar.add(sourceFilterField);
		
		sourceListContainer.setTopComponent(sourceListToolBar);
		sourceListContainer.add(sourceList, new FitData());
		this.add(sourceListContainer, new RowData(.40, 1, new Margins(5)));
	}
	
	private void createTargetContainer(List<T> models, List<T> currentModels){
		targetGrid = createTargetGrid(models, currentModels);
		targetGrid.setView( new DualListGridView() );
		ContentPanel targetListContainer = new ContentPanel(new FitLayout());
		targetListContainer.setHeaderVisible(false);
		
		if(enableOrdering){
			ButtonBar bar = new ButtonBar();
			
			Button up = new Button("Up", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					HashMap<Integer, T> prefsMap = new HashMap<Integer, T>();
					List<T> models = targetGrid.getSelectionModel().getSelectedItems();
					for(T model : models){
						int index = targetGrid.getStore().indexOf(model);
						if(index > 0){
							prefsMap.put(index, model);
						}
					}
					
					ArrayList<Integer> keySet = new ArrayList<Integer>(prefsMap.keySet());
					int n = keySet.size();
				    for (int pass=1; pass < n; pass++) {  // count how many times
				        // This next loop becomes shorter and shorter
				        for (int i=0; i < n-pass; i++) {
				            if (keySet.get(i) > keySet.get(i+1)) {
				                // exchange elements
				                int temp = keySet.get(i);  
				                keySet.set(i, keySet.get(i+1));  
				                keySet.set(i+1, temp) ;
				            }
				        }
				    }
				    
				    for(int index : keySet){
				    	T model = targetGrid.getStore().getAt(index);
				    	targetGrid.getStore().remove(model);
						targetGrid.getStore().insert(model, index - 1);
				    }
					targetGrid.getView().refresh(false);
					targetGrid.getSelectionModel().deselectAll();
					targetGrid.getSelectionModel().setSelection(models);
				}});
			bar.add(up);
			
			Button down = new Button("Down", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					HashMap<Integer, T> prefsMap = new HashMap<Integer, T>();
					List<T> models = targetGrid.getSelectionModel().getSelectedItems();
					for(T model : models){
						int index = targetGrid.getStore().indexOf(model);
						if(index < targetGrid.getStore().getCount() - 1){
							prefsMap.put(index, model);
						}
					}
					
					ArrayList<Integer> keySet = new ArrayList<Integer>(prefsMap.keySet());
					int n = keySet.size();
				    for (int pass=1; pass < n; pass++) {  // count how many times
				        // This next loop becomes shorter and shorter
				        for (int i=0; i < n-pass; i++) {
				            if (keySet.get(i) < keySet.get(i+1)) {
				                // exchange elements
				                int temp = keySet.get(i);  
				                keySet.set(i, keySet.get(i+1));  
				                keySet.set(i+1, temp) ;
				            }
				        }
				    }
				    
				    for(int index : keySet){
				    	T model = targetGrid.getStore().getAt(index);
				    	targetGrid.getStore().remove(model);
						targetGrid.getStore().insert(model, index + 1);
				    }
				    targetGrid.getView().refresh(false);
					targetGrid.getSelectionModel().deselectAll();
					targetGrid.getSelectionModel().setSelection(models);
				}});
			bar.add(down);
			targetListContainer.setBottomComponent(bar);
		}
		
		targetListContainer.add(targetGrid, new FitData());
		
		this.add(targetListContainer, new RowData(0.5,1, new Margins(5)));
	}
	
	/**
	 * Create the source {@link ListView}
	 * @param models
	 * @param currentModels
	 * @param displayProperty
	 * @return
	 */
	protected abstract ListView<T> createSourceList(List<T> models, List<T> currentModels, String displayProperty);
	
	/**
	 * Create the target {@link EditorGrid}
	 * @param models
	 * @param currentModels
	 * @return
	 */
	protected abstract EditorGrid<T> createTargetGrid(List<T> models, List<T> currentModels);
	
	/**
	 * Create the middle panel having buttons
	 */
	protected void createButtonContainer(){
		VBoxLayoutData layoutData = new VBoxLayoutData(new Margins(2));
		
		VBoxLayout vLayout = new VBoxLayout();
		vLayout.setPadding(new Padding(0));
		vLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		vLayout.setPack(BoxLayoutPack.CENTER);
		LayoutContainer buttonsContainer = new LayoutContainer(vLayout);
		Button allToRight = new Button(">>", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = sourceList.getStore().getModels();
				onRight(models);
			}});
		allToRight.setStyleAttribute("marginBottom", "5px");
		buttonsContainer.add(allToRight, layoutData);
		
		Button toRight = new Button(">", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = sourceList.getSelectionModel().getSelectedItems();
				onRight(models);
			}});
		toRight.setStyleAttribute("marginBottom", "5px");
		buttonsContainer.add(toRight, layoutData);
		
		Button toLeft = new Button("<", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = targetGrid.getSelectionModel().getSelectedItems();
				onLeft(models);
			}});
		toLeft.setStyleAttribute("marginBottom", "5px");
		buttonsContainer.add(toLeft,layoutData);
		
		Button allToLeft = new Button("<<", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = targetGrid.getStore().getModels();
				onLeft(models);
			}});
		buttonsContainer.add(allToLeft, layoutData);
		
		this.add(buttonsContainer, new RowData(.1,1));
	}
	
	/**
	 * Called when models are moved from Left to Right
	 * @param models
	 */
	protected void onRight(List<T> models){
		for(T model : models){
			targetGrid.getStore().add(model);
			sourceList.getStore().remove(model);
		}
	}
	
	/**
	 * Called when models are moved from Right to Left
	 * @param models
	 */
	protected void onLeft(List<T> models){
		sourceList.getStore().add(models);
		for(T model : models){
			targetGrid.getStore().remove(model);
		}
	}
	
	/**
	 * Called when save button is clicked
	 */
	protected abstract void onSubmit();

	public void setEnableOrdering(boolean enableOrdering) {
		this.enableOrdering = enableOrdering;
	}

	public boolean isEnableOrdering() {
		return enableOrdering;
	}
}
