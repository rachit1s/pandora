package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

/**
 * Implementer should Provide SearchHandler (what should be done on search method call), ValueCalculator, getDQL method, saveSearch
 * Provides - search button and save search Button,  
 * @author sourabh
 * 
 * Abstract class for Search panels
 */
public abstract class AbstractSearchPanel extends ContentPanel {
	
	/**
	 * 
	 * @author sutta
	 * 
	 * Handle to be executed when search button is clicked
	 */
	public interface ISearchHandle{
		public void onSearch(DQL dql);
	}
	
	public interface IValueCalculator{
		String getValue();
	}
	
	protected String sysPrefix;
	
	protected ISearchHandle searchHandle;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public AbstractSearchPanel(String sysPrefix) {
		super();
		
		this.sysPrefix = sysPrefix;
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	/**
	 * Make the "Save Search" Button
	 * @return
	 */
	protected Button getSaveSearchButton(){
		Button saveSearchButton = new Button("Save Search", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(final ButtonEvent ce) {
				final Dialog dialog = new Dialog(){
					@Override
					protected void onHide() {
						super.onHide();
						ce.getButton().enable();
					}
					
					@Override
					protected void onShow() {
						super.onShow();
						ce.getButton().disable();
					}
				};
				dialog.setHeading("Save Search");
				dialog.setLayout(new FitLayout());
				dialog.setSize(400, 110);
				dialog.setButtons(Dialog.OK);
				
				FormPanel form = new FormPanel();
				form.setHeaderVisible(false);
				form.setBodyBorder(false);
				form.setLabelWidth(100);
				
				final TextField<String> name = new TextField<String>();
				name.setName("scName");
				name.setFieldLabel("Search Name");
				form.add(name, new FormData("100%"));
				
				dialog.add(form, new FitData());
				dialog.show();
				dialog.getButtonById("ok").addSelectionListener(new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put(name.getName(), name.getValue());
//						params.put(isDefault.getName(), isDefault.getValue() ? "1" : "0");
//						params.put(isPublic.getName(), isPublic.getValue() ? "1" : "0");
//						params.put(isBAWide.getName(), isBAWide.getValue() ? "1" : "0");
						params.put("query", getDQL().dql);
						
						saveSearch(dialog, name.getValue(), params);
						ce.getButton().enable();
					}});
			}});
		
		return saveSearchButton;
	}
	
	/**
	 * @return Return the DQL
	 */
	protected abstract DQL getDQL();
	
	/**
	 * Saves current DQL in saved searches
	 * @param dialog
	 * @param searchName
	 * @param params
	 */
	protected abstract void saveSearch(Dialog dialog, String searchName, HashMap<String, String> params);
	
	/**
	 * Get the Search Button
	 * @return
	 */
	protected Button getSearchButton(){
		Button filterBtn = new Button("Search", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(searchHandle != null){
					searchHandle.onSearch(getDQL());
				}
			}
	    });
		return filterBtn;
	}

	public void setSearchHandle(ISearchHandle searchHandle) {
		this.searchHandle = searchHandle;
	}

	public ISearchHandle getSearchHandle() {
		return searchHandle;
	}
}
