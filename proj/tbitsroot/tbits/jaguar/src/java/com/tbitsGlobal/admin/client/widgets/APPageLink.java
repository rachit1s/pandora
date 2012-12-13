/**
 * 
 */
package com.tbitsGlobal.admin.client.widgets;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.events.OnPageRequest;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnHistoryTokensChanged;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;

/**
 * A button which is linked to a APTabItem 
 * @author dheeru
 *
 */
public abstract class APPageLink extends TbitsHyperLink{

	protected LinkIdentifier linkIdentifier;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public APPageLink(LinkIdentifier linkId){
		super();
		
		this.setText(linkId.getPageCaption());
		this.setStyleAttribute("margin", "3px");
		this.setStyleAttribute("text-decoration", "none");
		
		this.linkIdentifier = linkId;
		
		TbitsURLManager.getInstance().register(linkIdentifier.getHistoryKey(), true);
		
		this.addSelectionListener(new SelectionListener<TbitsHyperLinkEvent>() {
			@Override
			public void componentSelected(TbitsHyperLinkEvent ce) {
				onSelect();
			}
		});
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		observable.subscribe(OnHistoryTokensChanged.class, new ITbitsEventHandle<OnHistoryTokensChanged>() {
			@Override
			public void handleEvent(OnHistoryTokensChanged event) {
				HistoryToken token = event.getStore().findModel(HistoryToken.KEY, linkIdentifier.getHistoryKey());
				if(token != null)
					TbitsEventRegister.getInstance().fireEvent(new OnPageRequest(APPageLink.this));
			}
		});
		
		this.addListener(Events.OnMouseOver, new Listener<TbitsHyperLinkEvent>() {
			@Override
			public void handleEvent(TbitsHyperLinkEvent be) {
				be.getLink().setStyleAttribute("color", "#F82");
			}
		});
		
		this.addListener(Events.OnMouseOut, new Listener<TbitsHyperLinkEvent>() {
			@Override
			public void handleEvent(TbitsHyperLinkEvent be) {
				be.getLink().setStyleAttribute("color", COLOR_DEFAULT_BLUE);
			}
		});
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		HistoryToken token = TbitsURLManager.getInstance().stringToStore().findModel(HistoryToken.KEY, linkIdentifier.getHistoryKey());
		if(token != null)
			onSelect();
	}
	
	private void onSelect(){
		if(!TbitsURLManager.getInstance().hasKey(linkIdentifier.getHistoryKey()))
			TbitsURLManager.getInstance().addToken(new HistoryToken(linkIdentifier.getHistoryKey(), "", false));
		else
			TbitsEventRegister.getInstance().fireEvent(new OnPageRequest(APPageLink.this));
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
	 * @return returns the Page to be displayed when the link is clicked
	 */
	public abstract APTabItem getPage();

	public LinkIdentifier getLinkIdentifier() {
		return linkIdentifier;
	}
}
