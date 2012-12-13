package com.tbitsGlobal.jaguar.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.tbitsGlobal.jaguar.client.widgets.forms.RequestFormFactory;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IViewRequestForm;

/**
 * 
 * @author sourabh
 * 
 * Tab class for View Request Form
 */
public class ViewRequestTab extends HistoryEnabledTab implements IFixedFields{
	
	private IViewRequestForm view;
	
	public ViewRequestTab(int requestId, String title, TbitsTreeRequestData model, HashMap<String, ArrayList<HistoryEnabledTab>> tabMap) {
		super(title, GlobalConstants.TOKEN_VIEW, requestId + "", tabMap);
		this.setLayout(new FitLayout());
		
		view = this.getRequestView(this, model);
		
		this.add(view.getWidget(), new FitData());
	}
	
	private IViewRequestForm getRequestView(TabItem tab, TbitsTreeRequestData model) {
		DefaultUIContext mainContext = new DefaultUIContext();
		mainContext.setValue(IViewRequestForm.CONTEXT_PARENT_TAB, tab);
		mainContext.setValue(IRequestFormData.CONTEXT_MODEL, model);
		
		Integer sysId = model.get(IFixedFields.BUSINESS_AREA);
		if( null != sysId )
		{
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			for( BusinessAreaClient ba : cache.getValues() )
			{
				if( null != ba && ba.getSystemId() == sysId )
				{					
					mainContext.setValue(RequestFormFactory.SYS_PREFIX, ba.getSystemPrefix());
					break ;
				}
			}
		}
			
		IViewRequestForm form = RequestFormFactory.getInstance().getViewRequestForm(mainContext);
		return form;
	}
	
	@Override
	public void refresh() {
		super.refresh();
		
		view.refresh();
	}
}
