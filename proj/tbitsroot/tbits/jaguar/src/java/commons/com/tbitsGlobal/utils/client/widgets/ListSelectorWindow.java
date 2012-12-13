package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public abstract class ListSelectorWindow<T extends ModelData> extends Window{
	private RowData rowData;
	
	protected ListView<T> sourceList;
	protected ListView<T> targetList;
	
	private ArrayList<T> models;
	private ArrayList<T> currentModels;
	private String displayProperty;
	private String[] filterProperties;
	
	public ListSelectorWindow(ArrayList<T> models, ArrayList<T> currentModels, String displayProperty, String[] filterProperties) {
		super();
		
		this.models = models;
		this.currentModels = currentModels;
		
		this.displayProperty = displayProperty;
		this.filterProperties = filterProperties;
		
		rowData = new RowData(.45, 1);
		rowData.setMargins(new Margins(5));
		
		this.setSize(400, 400);
		this.setHeading("Select Users");
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.setModal(true);
		this.setOnEsc(true);
		
		this.createSourceList(this.currentModels);
		this.createButtonContainer();
		this.createTargetList(this.currentModels);
	}
	
	private void createSourceList(ArrayList<T> currentModels){
		ContentPanel sourceListContainer = new ContentPanel(new FitLayout());
		sourceListContainer.setHeaderVisible(false);
		
		sourceList = new ListView<T>();
		sourceList.setBorders(true);
		sourceList.setDisplayProperty(this.displayProperty);
		
		final ListStore<T> sourceStore = new ListStore<T>();
		sourceStore.add(models);
		for(T user : currentModels)
			sourceStore.remove(user);
		sourceStore.sort(this.displayProperty, SortDir.ASC);
		
		final DelayedTask task = new DelayedTask(new Listener<StoreEvent<T>>(){
			public void handleEvent(StoreEvent<T> be) {
				sourceStore.sort(displayProperty, SortDir.ASC);
			}});
		
		sourceStore.addStoreListener(new StoreListener<T>(){
			@Override
			public void storeAdd(StoreEvent<T> se) {
				super.storeAdd(se);
				task.delay(100);
			}
			
			@Override
			public void storeFilter(StoreEvent<T> se) {
				super.storeFilter(se);
				task.delay(100);
			}
		});
		
		sourceList.setStore(sourceStore);
		
		ToolBar sourceListToolBar = new ToolBar();
		sourceListToolBar.add(new LabelToolItem("Filter : "));
		
		StoreFilterField<T> sourceFilterField = new StoreFilterField<T>(){
			@Override
			protected boolean doSelect(Store<T> store,
					T parent, T record, String property,
					String filter) {
				for(String filterParam : filterProperties){
					if(((String)record.get(filterParam)).contains(filter))
						return true;
				}
				return false;
			}};
		sourceFilterField.setWidth(120);
		sourceFilterField.bind(sourceStore);
		
		sourceListToolBar.add(sourceFilterField);
		
		sourceListContainer.setTopComponent(sourceListToolBar);
		sourceListContainer.add(sourceList, new FitData());
		
		this.add(sourceListContainer, rowData);
	}
	
	private void createTargetList(ArrayList<T> currentModels){
		ContentPanel targetListContainer = new ContentPanel(new FitLayout());
		targetListContainer.setHeaderVisible(false);
		
		targetList = new ListView<T>();  
		targetList.setBorders(true);
		targetList.setDisplayProperty(this.displayProperty);
		ListStore<T> targetStore = new ListStore<T>();
		targetStore.add(currentModels);
		targetList.setStore(targetStore);
		
		ButtonBar bar = new ButtonBar();
		
		Button up = new Button("Up", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = targetList.getSelectionModel().getSelectedItems();
				for(T model : models){
					int index = targetList.getStore().indexOf(model);
					if(index > 0){
						targetList.getStore().remove(model);
						targetList.getStore().insert(model, index - 1);
					}
				}
				targetList.getSelectionModel().deselectAll();
				targetList.getSelectionModel().setSelection(models);
			}});
		bar.add(up);
		
		Button down = new Button("Down", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = targetList.getSelectionModel().getSelectedItems();
				for(T model : models){
					int index = targetList.getStore().indexOf(model);
					if(index < targetList.getStore().getCount() - 1){
						targetList.getStore().remove(model);
						targetList.getStore().insert(model, index + 1);
					}
				}
				targetList.getSelectionModel().deselectAll();
				targetList.getSelectionModel().setSelection(models);
			}});
		bar.add(down);
		
		targetListContainer.setBottomComponent(bar);
		targetListContainer.add(targetList, new FitData());
		
		this.add(targetListContainer, rowData);
	}
	
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
				targetList.getStore().add(models);
				for(T model : models){
					sourceList.getStore().remove(model);
				}
			}});
		allToRight.setStyleAttribute("marginBottom", "5px");
		buttonsContainer.add(allToRight, layoutData);
		
		Button toRight = new Button(">", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = sourceList.getSelectionModel().getSelectedItems();
				targetList.getStore().add(models);
				for(T model : models){
					sourceList.getStore().remove(model);
				}
			}});
		toRight.setStyleAttribute("marginBottom", "5px");
		buttonsContainer.add(toRight, layoutData);
		
		Button toLeft = new Button("<", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = targetList.getSelectionModel().getSelectedItems();
				sourceList.getStore().add(models);
				for(T model : models){
					targetList.getStore().remove(model);
				}
			}});
		toLeft.setStyleAttribute("marginBottom", "5px");
		buttonsContainer.add(toLeft,layoutData);
		
		Button allToLeft = new Button("<<", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<T> models = targetList.getStore().getModels();
				sourceList.getStore().add(models);
				for(T model : models){
					targetList.getStore().remove(model);
				}
			}});
		buttonsContainer.add(allToLeft, layoutData);
		
		this.add(buttonsContainer, new RowData(.1,1));
	}
	
	public abstract void onSubmit();
	
	public void addSubmitButton(String caption){
		this.addButton(new Button(caption, new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			}}));
	};
}
