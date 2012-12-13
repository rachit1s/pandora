package com.tbitsGlobal.jaguar.client.widgets.advsearch;

import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.events.ToSearch;

import commons.com.tbitsGlobal.utils.client.DQLConstants;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;

/**
 * 
 * @author sourabh
 *
 */
public class AdvSearchPanel extends AbstractSearchPanel {
	
	private NonTextFieldsPanel non_text;
	private TextParamsPanel text;
	private ToolBar toolbar;
	
	public AdvSearchPanel(String sysPrefix) {
		super(sysPrefix);
		
		non_text = new NonTextFieldsPanel(sysPrefix);
		text = new TextParamsPanel(sysPrefix);
		toolbar = new ToolBar();
		toolbar.setAlignment(HorizontalAlignment.CENTER);
		
		toolbar.add(this.getSaveSearchButton());
		toolbar.add(this.getSearchButton());
		
		this.setSearchHandle(new ISearchHandle(){
			public void onSearch(DQL dql) {
				TbitsEventRegister.getInstance().fireEvent(new ToSearch(AdvSearchPanel.this.sysPrefix, dql, 1, GlobalConstants.SEARCH_PAGESIZE, true,false));
			}});
		
		this.add(non_text);
		this.add(new Html("<br><b>AND</b><br>"));
		this.add(text);
		this.setScrollMode(Scroll.AUTO);
		this.setHeading("Advanced Search Panel");
		this.setBottomComponent(toolbar);
	}

	protected DQL getDQL(){
		return new DQL(DQLConstants.NON_TEXT + "(" + non_text.getDQL().dql + ") " + DQLConstants.TEXT + "(" + text.getDQL().dql + ")");
	}
	
	@Override
	protected void saveSearch(final Dialog dialog, final String searchName, HashMap<String, String> params) {
		JaguarConstants.dbService.saveSearch(sysPrefix, params, new AsyncCallback<Boolean>(){
			public void onFailure(Throwable arg0) {
				TbitsInfo.error("Error while saving search...", arg0);
			}

			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Search :" + searchName + " saved");
					dialog.hide();
				}else
					TbitsInfo.error("Search :" + searchName + " could not be saved");
			}});
	}
}
