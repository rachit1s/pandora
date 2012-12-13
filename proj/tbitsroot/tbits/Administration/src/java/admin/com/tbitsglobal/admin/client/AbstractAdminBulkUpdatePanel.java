package admin.com.tbitsglobal.admin.client;

import java.util.List;

import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractBulkUpdatePanel;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public abstract class AbstractAdminBulkUpdatePanel<M extends TbitsModelData> extends AbstractBulkUpdatePanel<M> {
	protected List<M> clipboard;
	
	protected ToolBar toolbar;
	public AbstractAdminBulkUpdatePanel() {
		super();
		
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
		
		toolbar = new ToolBar();
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		toolbar.add(new ToolBarButton("Add Row", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				singleGridContainer.addModel(getEmptyModel());
			}}));
		toolbar.add(new ToolBarButton("Remove Selected", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<M> selectedItems = singleGridContainer.getSelectedModels();
				if(selectedItems != null){
					for(M model : selectedItems){
						singleGridContainer.getBulkGrid().getStore().remove(model);
					}
				}
			}}));
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
		toolbar.add(new ToolBarButton("Refresh", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				refresh();
			}}));
		toolbar.add(new ToolBarButton("Save", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(final ButtonEvent ce) {
				List<M> models = singleGridContainer.getModels();
				onSave(models, ce.getButton());
			}}));
		this.setTopComponent(toolbar);
		
		refresh();
	}
	
	public abstract AbstractIndividualBulkGridContainer<M> getIndividualBulkUpdateGridContainer();
	
	public abstract AbstractCommonBulkGridContainer<M> getCommonBulkUpdateGridContainer(UIContext context);
	
	public abstract void onSave(List<M> models, Button btn);
	
	public abstract void refresh();
}
