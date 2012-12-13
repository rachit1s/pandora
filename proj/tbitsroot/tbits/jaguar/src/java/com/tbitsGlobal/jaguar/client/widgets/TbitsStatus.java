package com.tbitsGlobal.jaguar.client.widgets;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.jaguar.client.Jaguar;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.log.TbitsWidgetLogger;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Tool Bar at the bottom of the Application
 * 
 * @author sourabh
 *
 */
public class TbitsStatus extends ToolBar {
	private static TbitsStatus statusBar;
	private static Status status;
	
	private TbitsWidgetLogger divLogger;
	
	private TbitsStatus(){
		this.setBorders(false);
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		status = new Status();
		status.setText(Jaguar.copyRightText);  
		this.add(status);  
		this.add(new FillToolItem());
		
		this.add(new ToolBarButton("Logs", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(divLogger == null){
					divLogger = Log.getWidgetLogger();
				}
				divLogger.setVisible(!divLogger.isVisible());
			}}));
	}
	
	public static TbitsStatus getInstance(){
		initialize();
		return statusBar;
	}
	
	private static void initialize(){
		if(statusBar == null)
			statusBar = new TbitsStatus();
	}

	public static void setStatusMessage(String statusText){
		initialize();
		status.setBusy(statusText);
	}
	
	public static void clear(){
		initialize();
		status.clearStatus(Jaguar.copyRightText);
	}
}
