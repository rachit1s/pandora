package com.tbitsGlobal.admin.client.widgets;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.log.TbitsWidgetLogger;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class APStatusBar extends ToolBar {

	private String copyRightText = GlobalConstants.copyRightText;
	private Status status;
	private TbitsWidgetLogger divLogger;

	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public APStatusBar() {
		super();
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}

	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);

		status = new Status();
		status.setText(copyRightText);
		this.add(status);

		this.add(new FillToolItem());
		
		Button logButton = new ToolBarButton("Logs", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (divLogger == null) {
					divLogger = Log.getWidgetLogger();
				}
				divLogger.setVisible(!divLogger.isVisible());
			}
		});
		this.add(logButton);
	}

	public void setStatusMessage(String statusText) {
		status.setBusy(statusText);
	}

	public void clear() {
		status.clearStatus(copyRightText);
	}
}
