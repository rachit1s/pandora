package com.tbitsGlobal.admin.client.widgets;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AdminUtils;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.jaguar.client.JaguarUtils;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class APHeader extends ToolBar {
	
	protected TbitsObservable observable;
	
	public APHeader() {
		super();
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	/**
	 * Adds User Info Box on the {@link HeaderToolBar}
	 */
	private void addUserBox(){
		final Status user = new Status();
		user.setBox(true);
		
		UserClient currentUser = ClientUtils.getCurrentUser();
		user.setText(currentUser.getDisplayName()); 
		user.setToolTip("Login : " + currentUser.getUserLogin() + "<br />" 
				+ "E-Mail : " + currentUser.getEmail());
		
		this.add(user);
	}

	/**
	 * Add link for the old admin panel.
	 */
	private void addOldAdminLink(){
		final LayoutContainer admin = new LayoutContainer();
		this.add(admin);
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				admin.removeAll();
				admin.addText("<a href='" + AdminUtils.getAppBaseURL() + "web/admin-frame.html#?q=" +
						ClientUtils.getSysPrefix() + "' target='_blank'><b>&nbsp;Old Admin Panel&nbsp;</b></a>");
				admin.layout();
			}
		});
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		this.add(new FillToolItem());
		
		// Link to logout.
		final LayoutContainer logout = new LayoutContainer();
		APConstants.apService.showLogout(new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				Log.error("Unable to know whether to show 'Old Version' link or not. Please upgrade your database.");
			}

			public void onSuccess(Boolean result) {
				if(result){
					logout.addText("<a href='" + AdminUtils.getAppBaseURL() + "logout'><b>Logout</b></a>");
					logout.layout();
				}
			}});
		
		this.addOldAdminLink();
		
		this.add(logout);
		
		// User Info Box.
		this.addUserBox();
	}
}
