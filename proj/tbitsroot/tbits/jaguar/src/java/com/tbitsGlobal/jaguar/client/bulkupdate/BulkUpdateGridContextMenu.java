package com.tbitsGlobal.jaguar.client.bulkupdate;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToCustomizeColumns;
import commons.com.tbitsGlobal.utils.client.grids.AbstractGridContextMenu;
import commons.com.tbitsGlobal.utils.client.grids.GridMenuItem;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;

/**
 * Context Menu to be shown on a right click inside {@link BulkUpdateGrid}
 * 
 * @author sourabh
 *
 */
public class BulkUpdateGridContextMenu extends AbstractGridContextMenu{

	public BulkUpdateGridContextMenu(BulkUpdateGrid iGrid) {
		super(iGrid);
		
		this.add(this.getCutMenu());
		this.add(this.getCopyMenu());
		this.add(this.getPasteMenu());
		this.add(this.getDeleteMenu());
		
		if(iGrid.isCustomizable())
			this.add(this.getConfigureColumnsMenu());
	}
	
	public GridMenuItem getCopyMenu(){
		GridMenuItem copyValue = new GridMenuItem("Copy Cell Value"){
			@Override
			protected boolean validate() {
				return selModel != null && property != null;
			}
		};
		copyValue.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				POJO pojo = selModel.getAsPOJO(property);
				if(pojo != null)
					BulkUpdateConstants.clipboard = pojo.clone();
			}});
		
		return copyValue;
	}
	
	public GridMenuItem getPasteMenu(){
		GridMenuItem pasteValue = new GridMenuItem("Paste Copied Value"){
			@Override
			protected boolean validate() {
				return selModel != null && property != null && BulkUpdateConstants.clipboard != null;
			}
		};
		pasteValue.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				POJO oldValue = selModel.getAsPOJO(property);
				selModel.set(property, BulkUpdateConstants.clipboard.clone());
				Grid<TbitsTreeRequestData> grid = iGrid.getGrid();
				if(grid != null){
					try{
						grid.getView().refresh(false);
					}catch (Exception e){
						if(oldValue != null)
							selModel.set(property, oldValue);
						else
							selModel.remove(property);
					}
				}
			}});
		
		return pasteValue;
	}
	
	public GridMenuItem getCutMenu(){
		GridMenuItem cutValue = new GridMenuItem("Cut Cell Value"){
			@Override
			protected boolean validate() {
				return selModel != null && property != null;
			}
		};
		cutValue.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				POJO pojo = selModel.getAsPOJO(property);
				if(pojo != null){
					BulkUpdateConstants.clipboard = pojo.clone();
					selModel.remove(property);
					Grid<TbitsTreeRequestData> grid = iGrid.getGrid();
					if(grid != null)
						grid.getView().refresh(false);
				}
			}});
		
		return cutValue;
	}
	
	public GridMenuItem getDeleteMenu(){
		GridMenuItem deleteValue = new GridMenuItem("Delete Cell Value"){
			@Override
			protected boolean validate() {
				return selModel != null && property != null;
			}
		};
		deleteValue.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				selModel.remove(property);
				Grid<TbitsTreeRequestData> grid = iGrid.getGrid();
				if(grid != null)
					grid.getView().refresh(false);
			}});
		
		return deleteValue;
	}
	
	public GridMenuItem getConfigureColumnsMenu(){
		GridMenuItem confColumns = new GridMenuItem("Customize Columns");
		confColumns.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				TbitsEventRegister.getInstance().fireEvent(new ToCustomizeColumns(iGrid.getGrid()));
			}});
		confColumns.setNeedsSeparator(true);
		return confColumns;
	}
}
