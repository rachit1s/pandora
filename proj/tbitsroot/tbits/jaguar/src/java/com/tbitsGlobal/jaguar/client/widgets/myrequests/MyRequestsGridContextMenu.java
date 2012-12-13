package com.tbitsGlobal.jaguar.client.widgets.myrequests;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.tbitsGlobal.jaguar.client.events.ToUpdateRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.grids.AbstractGridContextMenu;
import commons.com.tbitsGlobal.utils.client.grids.GridMenuItem;
import commons.com.tbitsGlobal.utils.client.grids.IRequestsGrid;

/**
 * Context Menu for {@link MyRequestsGrid}.
 * 
 * @author sourabh
 *
 */
public class MyRequestsGridContextMenu extends AbstractGridContextMenu{
	private String sysPrefix;

	/**
	 * Constructor
	 * 
	 * @param sysPrefix
	 */
	public MyRequestsGridContextMenu(String sysPrefix, IRequestsGrid iGrid) {
		super(iGrid);
		this.sysPrefix = sysPrefix;
		
		this.add(this.getViewRequestMenu());
		this.add(this.getUpdateRequestMenu());
	}

	public String getSysPrefix() {
		return sysPrefix;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public GridMenuItem getViewRequestMenu(){
		GridMenuItem viewRequest = new GridMenuItem("View " + Captions.getRecordDisplayName(sysPrefix));
		viewRequest.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				if(selModel != null)
					TbitsEventRegister.getInstance().fireEvent(new ToViewRequestOtherBA(sysPrefix, selModel.getRequestId()));
			}});
		this.add(viewRequest);
		
		return viewRequest;
	}
	
	public GridMenuItem getUpdateRequestMenu(){
		GridMenuItem updateRequest = new GridMenuItem("Update " + Captions.getRecordDisplayName(sysPrefix));
		updateRequest.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				if(selModel != null)
					TbitsEventRegister.getInstance().fireEvent(new ToUpdateRequestOtherBA(sysPrefix, selModel.getRequestId()));
			}});
		this.add(updateRequest);
		
		return updateRequest;
	}
	
}
