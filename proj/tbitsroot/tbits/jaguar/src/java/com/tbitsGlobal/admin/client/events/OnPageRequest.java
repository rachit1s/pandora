/**
 * 
 */
package com.tbitsGlobal.admin.client.events;

import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.widgets.APPageLink;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

/**
 * @author dheeru
 * 
 */
public class OnPageRequest extends TbitsBaseEvent {

	private APPageLink pageLink;

	public OnPageRequest(APPageLink pageLink) {
		super("Loading " + pageLink.getText() + " please wait", "Failed to load "
				+ pageLink.getText());
		this.pageLink = pageLink;
	}
	

	@Override
	public boolean beforeFire() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			AppState.delayTillAppStateIsTill(AppState.BAChanged, this);
			return false;
		}
		return true;
	}

	public APPageLink getPageLink() {
		return pageLink;
	}
}
