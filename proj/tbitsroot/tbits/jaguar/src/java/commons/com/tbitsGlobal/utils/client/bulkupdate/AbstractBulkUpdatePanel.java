package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow.Parser;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * 
 * @author sourabh
 *
 * The abstract class for the panel which carries bulk update grids
 * 
 * @param <M extends {@link ModelData}> The Model on which the grid works
 */
public abstract class AbstractBulkUpdatePanel<M extends ModelData> extends ContentPanel implements IBulkUpdatePanel<M>{
	/**
	 * The container having grid in single mode
	 */
	protected AbstractSingleBulkGridContainer<M> singleGridContainer;
	
	/**
	 * The container having grid in common mode
	 */
	protected AbstractCommonBulkGridContainer<M> commonGridContainer;
	
	/**
	 * True to display only individual grid
	 */
	protected boolean commonGridDisabled;
	
	/**
	 * True to show "Add Row" Button on Toolbar
	 */
	protected boolean canAddRows = true;
	
	/**
	 * True to show "Remove Selected" Button on Toolbar
	 */
	protected boolean canDeleteRows = true;
	
	/**
	 * True to show "Up" and "Down" buttons on Toolbar
	 */
	protected boolean canReorderRows = true;
	
	protected ToolBar toolbar;
	
	/**
	 * True to enable Import from excel
	 */
	protected boolean isExcelImportSupported = false;
	
	private ToolBarPosition toolBarPosition;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	/**
	 * 
	 * @author sourabh
	 * 
	 * Determines Tool Bar position on the Bulk Update Panel
	 */
	public enum ToolBarPosition{
		TOP, BOTTOM
	}
	
	protected AbstractBulkUpdatePanel() {
		super();
		
		this.setLayout(new BorderLayout());
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		
		commonGridDisabled = false;
		
		toolBarPosition = ToolBarPosition.TOP;
		
		toolbar = new ToolBar();
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	/**
	 * this method is always called when a tab item is removed
	 */
	public void onDetach(){
		observable.detach();
		
		super.onDetach();
	}

	public AbstractBulkUpdatePanel(
			AbstractCommonBulkGridContainer<M> commonGridContainer,
			AbstractSingleBulkGridContainer<M> singleGridContainer) {
		this();
		
		this.commonGridContainer = commonGridContainer;
		this.singleGridContainer = singleGridContainer;
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		if(canReorderRows){
			toolbar.add(new ToolBarButton("Up", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					List<M> models = singleGridContainer.getSelectedModels();
					BulkUpdateGridAbstract<M> bulkGrid = singleGridContainer.bulkGrid;
					for(M model : models){
						int index = bulkGrid.getStore().indexOf(model);
						if(index > 0){
							bulkGrid.getStore().remove(model);
							bulkGrid.getStore().insert(model, index - 1);
						}
					}
					bulkGrid.getSelectionModel().deselectAll();
					bulkGrid.getSelectionModel().setSelection(models);
				}}));
			
			toolbar.add(new ToolBarButton("Down", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					List<M> models = singleGridContainer.getSelectedModels();
					BulkUpdateGridAbstract<M> bulkGrid = singleGridContainer.bulkGrid;
					for(int i = models.size() - 1; i >=0; i--){
						M model = models.get(i);
						int index = bulkGrid.getStore().indexOf(model);
						if(index < bulkGrid.getStore().getCount() - 1){
							bulkGrid.getStore().remove(model);
							bulkGrid.getStore().insert(model, index + 1);
						}
					}
					bulkGrid.getSelectionModel().deselectAll();
					bulkGrid.getSelectionModel().setSelection(models);
				}}));
			
			toolbar.add(new SeparatorToolItem());
		}
		
		if(canAddRows){
			toolbar.add(new ToolBarButton("Add Row", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					onAdd();
				}}));
		}
		
		if(canDeleteRows){
			toolbar.add(new ToolBarButton("Remove Selected", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					if(beforeRemove())
						onRemove();
				}}));
		}
		
		if(isExcelImportSupported){
			toolbar.add(new ToolBarButton("Import from Excel", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					onImport().show();
				}}));
		}
		
		if(this.toolBarPosition == ToolBarPosition.TOP)
			this.setTopComponent(toolbar);
		else if(this.toolBarPosition == ToolBarPosition.BOTTOM)
			this.setBottomComponent(toolbar);
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		/**
		 * Add Individual grid to the center.
		 */
		if(singleGridContainer != null){
			this.add(singleGridContainer, new BorderLayoutData(LayoutRegion.CENTER));
		}
		
		/**
		 * Add Common Grid to the south.
		 */
		if(!commonGridDisabled && commonGridContainer != null){
			BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 70);
			southData.setHideCollapseTool(true);
			southData.setSplit(true);
			southData.setMargins(new Margins(2));
			
			this.add(commonGridContainer, southData);
			
			// Add an empty model
			commonGridContainer.addModel(getEmptyModel());
		}
	}
	
	public ISingleBulkGridContainer<M> getSingleGridContainer() {
		return singleGridContainer;
	}

	public ICommonBulkGridContainer<M> getCommonGridContainer() {
		return commonGridContainer;
	}
	
	/**
	 * Called when "Add Row" is clicked
	 */
	protected void onAdd(){
		singleGridContainer.addModel(getEmptyModel());
	}
	
	/**
	 * Called when "Remove Selected" is clicked before the model is actually removed
	 */
	protected boolean beforeRemove(){
		return true;
	}
	
	/**
	 * Called when "Remove Selected" is clicked
	 */
	protected void onRemove(){
		List<M> selectedItems = singleGridContainer.getSelectedModels();
		if(selectedItems != null){
			for(M model : selectedItems){
				singleGridContainer.getBulkGrid().getStore().remove(model);
			}
		}
	}
	
	/**
	 * Called when Import from excel is called.
	 */
	protected ExcelImportWindow<M> onImport(){
		ExcelImportWindow<M> window = new ExcelImportWindow<M>(this);
		
		HashMap<String, Parser> parserMap = singleGridContainer.getBulkGrid().getParserMap();
		if(parserMap != null){
			for(String id : parserMap.keySet()){
				window.setParser(id, parserMap.get(id));
			}
		}
		
		HashMap<String, ExcelImportDataType> dataTypeMap = singleGridContainer.getBulkGrid().getDataTypeMap();
		if(dataTypeMap != null){
			for(String id : dataTypeMap.keySet()){
				window.setDataType(id, dataTypeMap.get(id));
			}
		}
		
		return window;
	}
	
	/**
	 * For any kind of validations
	 * 
	 * @param models
	 * @param uniqueMatchingField
	 * @return Will stop submit if returned false
	 */
	public boolean beforeImportSubmit(List<M> models, String uniqueMatchingField){
		return true;
	}
	
	/**
	 * Called when Submit is clicked in {@link ExcelImportWindow}
	 * @param models
	 * @param uniqueMatchingField
	 */
	public void onImportSubmit(List<M> models, String uniqueMatchingField){
		if(uniqueMatchingField != null){
			ListStore<M> bulkGridStore = singleGridContainer.bulkGrid.getStore();
			for(M model : models){
				Object value = model.get(uniqueMatchingField);
				if(value != null){
					M matchedModel = bulkGridStore.findModel(uniqueMatchingField, value);
					if(matchedModel != null){
						int index = bulkGridStore.indexOf(matchedModel);
						bulkGridStore.remove(index);
						bulkGridStore.insert(model, index);
					}else{
						singleGridContainer.addModel(model);
					}
				}
			}
			singleGridContainer.bulkGrid.getView().refresh(false);
		}else{
			singleGridContainer.addModel(models);
		}
	}
	
	/**
	 * @return An empty model
	 */
	public abstract M getEmptyModel();
	
	/**
	 * @param mode
	 * @return new instance of Bulk Grid
	 */
	protected abstract BulkUpdateGridAbstract<M> getNewBulkGrid(BulkGridMode mode);

	public void setCommonGridDisabled(boolean commonGridDisabled) {
		this.commonGridDisabled = commonGridDisabled;
	}

	public boolean isCommonGridDisabled() {
		return commonGridDisabled;
	}

	public void setToolBarPosition(ToolBarPosition toolBarPosition) {
		this.toolBarPosition = toolBarPosition;
	}

	public ToolBarPosition getToolBarPosition() {
		return toolBarPosition;
	}
}
