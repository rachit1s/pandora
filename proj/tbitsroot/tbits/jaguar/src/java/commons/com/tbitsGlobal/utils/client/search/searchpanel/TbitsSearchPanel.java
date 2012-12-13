package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.DQLConstants;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.ISearchBox.SearchParamType;

/**
 * Panel that provides basic search.
 * 
 * @author sourabh
 *
 */
public class TbitsSearchPanel extends AbstractSearchPanel{
	private List<ISearchBox> searchBoxes;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	/**
	 * Constructor
	 */
	public TbitsSearchPanel(String sysPrefix) {
		super(sysPrefix);
		
		this.setScrollMode(Scroll.AUTO);
		this.setHeading("Search Panel");
		this.setAnimCollapse(true);
		
		RowLayout layout = new RowLayout();
		layout.setAdjustForScroll(true);
		layout.setOrientation(Orientation.VERTICAL);
		this.setLayout(layout);
		
		this.searchBoxes = new ArrayList<ISearchBox>();
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		this.addButton(new Button("Reset", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				for(ISearchBox box : searchBoxes){
					box.resetToDefault();
				}
				TbitsSearchPanel.this.sortBox.reset();
			}}));
		
		this.addButton(this.getSaveSearchButton());
		
		this.addButton(this.getSearchButton());
		
		if(this.sysPrefix.equals(ClientUtils.getSysPrefix())){
			FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
			if(cache.isInitialized())
				buildPanel(Util.createList(cache.getValues()));
			
			observable.subscribe(OnFieldsReceived.class, new ITbitsEventHandle<OnFieldsReceived>(){
				public void handleEvent(OnFieldsReceived event) {
					FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
					buildPanel(Util.createList(cache.getValues()));
				}
			});
		}else{
			GlobalConstants.utilService.getFields(sysPrefix, new AsyncCallback<List<BAField>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Could not load fields.. Please see logs for details..", caught);
					Log.error("Could not load fields.. Please see logs for details..", caught);
				}

				public void onSuccess(List<BAField> result) {
					if(result != null)
						buildPanel(result);
				}});
		}
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
	
	protected void buildPanel(List<BAField> fields){
		Log.info("Initializing Basic Search Panel");
		
		removeAll();
		searchBoxes = new ArrayList<ISearchBox>();
		
		List<String> queryParams = ClientUtils.queryParamsforKey("j.sp");
		
		if(!queryParams.contains("none")){
			if(!queryParams.contains("text"))
				addKeywordBox();
			
			if(!queryParams.contains("type"))
				createTypeFieldsBox(fields);
			
			if(!queryParams.contains("mixed"))
				addMixedFieldsBox(fields);
			
			if(!queryParams.contains("date"))
				addDateSearchBox(fields);

			addSortingPanel(fields);
			
			if(GlobalConstants.isTagsSupported){
				addTagsSearchBox();
			}
			
			layout();
		}
		
		Log.info("Built Basic Search Panel");
	}
	
	protected DQL getDQL(){
		HashMap<String, String> dql = new HashMap<String, String>();
		dql.put(DQLConstants.NON_TEXT, "");
		dql.put(DQLConstants.TEXT, "");
		boolean hasText = false;
		boolean hasNonText = false;
		for(ISearchBox box : searchBoxes){
			if(box.getDQL().containsKey(SearchParamType.NON_TEXT)){
				if(!box.getDQL().get(SearchParamType.NON_TEXT).equals("")){
					dql.put(DQLConstants.NON_TEXT, dql.get(DQLConstants.NON_TEXT) + ((dql.get(DQLConstants.NON_TEXT).equals(""))?"":" AND ") + box.getDQL().get(SearchParamType.NON_TEXT));
					hasNonText = true;
				}
			}
			if(box.getDQL().containsKey(SearchParamType.TEXT)){
				if(!box.getDQL().get(SearchParamType.TEXT).equals("")){
					dql.put(DQLConstants.TEXT, dql.get(DQLConstants.TEXT) + ((dql.get(DQLConstants.TEXT).equals(""))?"":" AND ") + box.getDQL().get(SearchParamType.TEXT));
					hasText = true;
				}
			}
		}
		
		String finalDql = "";
		DQL dqlObject = new DQL();

		if(hasNonText)
			finalDql += DQLConstants.NON_TEXT + "(" + dql.get(DQLConstants.NON_TEXT) + ")";
		if(hasText){
			if(!finalDql.equals(""))
				finalDql += " ";
			finalDql += DQLConstants.TEXT + "(" + dql.get(DQLConstants.TEXT) + ")";
		}
		if(sortBox != null)
		{
			finalDql += DQLConstants.ORDER_BY + sortBox.getSortingQuery();
			String sortBy = sortBox.getSortBy();
			int sortDir = DQL.SORTDIR_DESC;
			String sortDirStr = sortBox.getSortDir();
			if((sortDirStr != null) && sortDirStr.equalsIgnoreCase("ASC"))
			{
				sortDir = DQL.SORTDIR_ASC;
			}
			dqlObject.sortOrder.put(sortBy, sortDir);
		}
		
		dqlObject.dql = finalDql;
		
		return dqlObject;
	}
	
	private void createTypeFieldsBox(List<BAField> fields){
		TreeStore<ModelData> store = new TreeStore<ModelData>();
		TypeFieldsBox tree = new TypeFieldsBox(store, fields);
		searchBoxes.add(tree);
	    this.add(tree, new RowData());
	}
	
	private void addMixedFieldsBox(List<BAField> fields){
		MixedFieldsBox box = new MixedFieldsBox(fields);
		searchBoxes.add(box);
		RowData data = new RowData();
		data.setWidth(1);
		this.add(box, data);
	}
	
	private void addKeywordBox(){
		KeywordBox box = new KeywordBox();
		box.addKeyListener(new KeyListener(){
			public void componentKeyDown(ComponentEvent event)
			{
				if(event.getKeyCode() == 13)
				{
					searchHandle.onSearch(getDQL());
				}
			}
		});
		searchBoxes.add(box);
		this.add(box, new RowData());
	}
	
	 private void addDateSearchBox(List<BAField> fields){
		DateSearchBox box = new DateSearchBox(fields);
		searchBoxes.add(box);
		this.add(box, new RowData());
	}
	 
	 private void addTagsSearchBox(){
		 TagsSearchBox box = new TagsSearchBox();
		 searchBoxes.add(box);
		 this.add(box, new RowData());
	 }

	 SortOrderBox sortBox = null;
	 private void addSortingPanel(List<BAField> fields)
	 {
		TreeStore<ModelData> store = new TreeStore<ModelData>();
//		if(sortBox == null) // removed as it should create new sortOrderBox when called.
		// It should not used previous one which some times corresponds to the older ba
			sortBox = new SortOrderBox(store, fields);
		this.add(sortBox, new RowData());
	 }
	 
	@Override
	protected void saveSearch(final Dialog dialog, final String searchName, HashMap<String, String> params) {
		GlobalConstants.utilService.saveSearch(sysPrefix, params, new AsyncCallback<Boolean>(){
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
	
	public static String getDQL(HashMap<String, List<String>> searchParams) {
		String dql = "";
		//TODO: special chars to be escaped. use escapeQuoteAndSlash(searchField).
		for(String searchField : searchParams.keySet()){
			List<String> params = searchParams.get(searchField);
			if(params.size() > 0){
				if(!dql.equals(""))
					dql += " AND ";
				String paramString = "";
				for(String param : params){
					if(!paramString.equals(""))
						paramString += " OR ";
					paramString += param;
				}
				if(!dql.equals(""))
					dql += " ";
				if(params.size() > 1)
					dql += searchField + ":(" + paramString + ")";
				else
					dql += searchField + ":" + paramString;
			}
		}
		
		return dql;
	}
}
