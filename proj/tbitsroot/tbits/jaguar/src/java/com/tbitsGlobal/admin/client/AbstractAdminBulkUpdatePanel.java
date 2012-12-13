package com.tbitsGlobal.admin.client;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractBulkUpdatePanel;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractCommonBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractSingleBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.DefaultCommonBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.DefaultSingleBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * 
 * @author sourabh
 * 
 * Abstract Class for Bulk Grid Panel to be used in Admin Pages
 * 
 * @param <M> Model Data on which the grid would be based on
 */
public abstract class AbstractAdminBulkUpdatePanel<M extends TbitsModelData> extends AbstractBulkUpdatePanel<M> implements IAdminBulkUpdatePanel<M> {
	protected List<M> clipboard;
	
	protected boolean canCopyPasteRows = true;
	
	private int pageSize;
	protected GridPagingBar pagingBar;
	
	/**
	 * True to show "Save" button
	 */
	protected boolean canSave = true;
	
	public AbstractAdminBulkUpdatePanel() {
		super();
		
		isExcelImportSupported = true;
		
		/**
		 * Add Individual grid to the center.
		 */
		singleGridContainer = getIndividualBulkUpdateGridContainer();
		
		/**
		 * Add Common Grid to the south.
		 */
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(BulkUpdateGridAbstract.CONTEXT_SINGLE_GRID_CONTAINER, singleGridContainer);
		commonGridContainer = getCommonBulkUpdateGridContainer(context);
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		if(canCopyPasteRows){ // Enables Copy Paste of rows
			toolbar.add(new ToolBarButton("Copy Selected", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					List<M> selectedItems = singleGridContainer.getSelectedModels();
					if(selectedItems != null)
						clipboard = selectedItems;
				}}));
			toolbar.add(new ToolBarButton("Paste", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					if(clipboard != null){
						for(M model : clipboard){
							singleGridContainer.addModel((M) model.clone(getEmptyModel()));
						}
					}
				}}));
			toolbar.add(new SeparatorToolItem());
		}
		
		toolbar.add(new ToolBarButton("Refresh", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(pagingBar != null)	// Refresh through paging bar if paging is enabled 
					refresh(pagingBar.getCurrentPage());
				else
					refresh(0);
			}}));
		
		
		if(canSave){
			toolbar.add(new ToolBarButton("Save", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(final ButtonEvent ce) {
					List<M> models = singleGridContainer.getModels();
					String validationResult = singleGridContainer.getBulkGrid().getValidationResults(models);
					if(validationResult != null && !validationResult.trim().equals("")){
						Window.alert(validationResult);
					}else{
						onSave(models, ce.getButton());
					}
				}}));
		}
		
		if(pageSize > 0){
			pagingBar = new GridPagingBar(pageSize){
				@Override
				protected void loadPage(int page) {
					refresh(page);
				}};
			pagingBar.setAllowedSizes(Arrays.asList(10, 25, 50, 100));
			singleGridContainer.setBottomComponent(pagingBar);
		}
		
		
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		refresh(1);
	}
	
	/**
	 * @return a new {@link AbstractSingleBulkGridContainer}
	 */
	protected AbstractSingleBulkGridContainer<M> getIndividualBulkUpdateGridContainer() {
		BulkUpdateGridAbstract<M> bulkGrid = getNewBulkGrid(BulkGridMode.SINGLE);
		DefaultSingleBulkGridContainer<M> singleGridContainer = new DefaultSingleBulkGridContainer<M>(bulkGrid);
		return singleGridContainer;
	}
	
	/**
	 * @param context
	 * @return a new {@link AbstractCommonBulkGridContainer}
	 */
	protected AbstractCommonBulkGridContainer<M> getCommonBulkUpdateGridContainer(
			UIContext context) {
		BulkUpdateGridAbstract<M> bulkGrid = getNewBulkGrid(BulkGridMode.COMMON);
		DefaultCommonBulkGridContainer<M> commonGridContainer = new DefaultCommonBulkGridContainer<M>(context, bulkGrid);
		return commonGridContainer;
	}
	
	/**
	 * Enables paging. Pre-Render
	 * @param pageSize
	 */
	public void enablePaging(int pageSize){
		this.pageSize = pageSize;
	}
	
	public int getPageSize(){
		if(pagingBar != null)
			return pagingBar.getPageSize();
		return 0;
	}
	
	/**
	 * Called when Save Button is clicked
	 * @param models
	 * @param btn
	 */
	protected abstract void onSave(List<M> models, Button btn);
}
