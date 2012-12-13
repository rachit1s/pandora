package com.tbitsGlobal.admin.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.events.OnBACreated;
import com.tbitsGlobal.admin.client.events.OnBAListReceived;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.MultiFilterStore;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;
import commons.com.tbitsGlobal.utils.client.widgets.ModelPropertyEditorExtended;

/**
 * @author dheeru
 * 
 */
public class BACombo extends ComboBox<BusinessAreaClient> {

	private MultiFilterStore<BusinessAreaClient> baList;
	private boolean isSetList = false;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;

	public BACombo() {
		super();
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		baList = new MultiFilterStore<BusinessAreaClient>();
	}

	@Override
	protected void beforeRender() {
		if (!isSetList) {
			this.setEmptyText("please Wait...");
		} else {
			this.setEmptyText("choose a ba");
		}
	}

	@Override
	protected void onRender(Element parent, int pos) {
		this.setStore(baList);
		this.setForceSelection(false);		//managing force selection with a handler on onBlur event
		this.setTriggerAction(TriggerAction.ALL);
		this.setTemplate(getComboTemplate());
		this.setSelectOnFocus(true);
		
		ModelPropertyEditorExtended<BusinessAreaClient> propEditor = new ModelPropertyEditorExtended<BusinessAreaClient>();
		propEditor.setTemplate("{display_name} : [{system_prefix}]");
		propEditor.setNeedsSeparator(false);
		this.setPropertyEditor(propEditor);
		
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			List<BusinessAreaClient> allBas = new ArrayList<BusinessAreaClient>();
			
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			allBas.addAll(cache.getValues());
			setBAList(allBas);
		}
		
		baList.addToFilterProperties(BusinessAreaClient.DISPLAY_NAME);
		baList.addToFilterProperties(BusinessAreaClient.SYSTEM_PREFIX);
		
		setHandlers();
		super.onRender(parent, pos);
	}

	private void setHandlers() {

		this.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				TbitsURLManager.getInstance().addToken(new HistoryToken(APConstants.TOKEN_BA, se.getSelectedItem().getSystemPrefix(),true));
			}
		});

		// reset BAList on BAList Recieved
		observable.subscribe(OnBAListReceived.class, new ITbitsEventHandle<OnBAListReceived>() {
			public void handleEvent(OnBAListReceived event) {
				BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
				List<BusinessAreaClient> list = new ArrayList<BusinessAreaClient>(cache.getValues());
				setBAList(list);
			}
		});

		// request display groups and field cache on BA Change
		observable.subscribe(OnChangeBA.class,new ITbitsEventHandle<OnChangeBA>() {
			public void handleEvent(OnChangeBA event) {
				BACombo.this.setValue(ClientUtils.getCurrentBA());
				BACombo.this.setToolTip(getPropertyEditor().getStringValue(ClientUtils.getCurrentBA()));
			}
		});
		
		observable.subscribe(OnBACreated.class, new ITbitsEventHandle<OnBACreated>() {
			public void handleEvent(OnBACreated event) {
				BACombo.this.baList.add(event.getNewBA());
			}
		});
	}

	private native String getComboTemplate()/*-{ 
		    return  [ 
		    '<tpl for=".">', 
		    '<div class="x-combo-list-item" qtip="Description:{description}" qtitle="{system_prefix}">{display_name} [{system_prefix}]</div>', 
		    '</tpl>' 
		    ].join("");
	}-*/;

	protected void setBAList(List<BusinessAreaClient> list) {
		if (list != null && list.size() != 0) {
			baList.removeAll();
			this.baList.add(list);
			this.baList.sort(BusinessAreaClient.DISPLAY_NAME, SortDir.ASC);
			this.isSetList = true;
			this.setEmptyText("Choose a BA");
			if (baList.getCount() != 0){
				ListStore<HistoryToken> store = TbitsURLManager.getInstance().stringToStore();
				HistoryToken baToken = store.findModel(HistoryToken.KEY,APConstants.TOKEN_BA);
				BusinessAreaClient baC = this.getStore().findModel(BusinessAreaClient.SYSTEM_PREFIX,baToken.getValue());
				if(baC != null)
					BACombo.this.setValue(baC);
				else
					BACombo.this.setValue(baList.getAt(0));
			}
		} else {
			this.setEmptyText("BAs not loaded, try reloading..");
		}
	}
}
