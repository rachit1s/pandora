package commons.com.tbitsGlobal.utils.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.Events.OnCurrentUserReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;

/**
 * 
 * @author sourabh
 * 
 * Class having some utility functions
 *
 */
public class ClientUtils implements IFixedFields{
	
	/**
	 * Sets the URL into the _previewFrame. Used to trigger previews and file downloads
	 * @param url
	 */
	public static native void showPreview(String url) /*-{
		$wnd.document.getElementById('_previewFrame').src = url;
	}-*/;
	
	/**
	 * Round off a double to given decimal places
	 * @param d
	 * @param decimalPlaces
	 * @return
	 */
	public static double round(double d, int decimalPlaces){
		String str = d + "";
		int periodIndex = str.indexOf('.');
		if(periodIndex > -1){
			int endIndex = str.length() > periodIndex + decimalPlaces + 1 ? periodIndex + decimalPlaces + 1 : str.length() - 1;
			str = str.substring(0, endIndex);
		}
		
		return Double.parseDouble(str);
	}
	
	public static ArrayList<String> toArrayList(String aString, String aSeparator) {
        ArrayList<String> arrayList = new ArrayList<String>();

        if ((aString == null) || (aString.equals("") == true)) {
            return arrayList;
        }

        String[] tokens = aString.split(aSeparator);

        for(String token : tokens) {
            if (token.trim().equals("") == false) {
                arrayList.add(token);
            }
        }

        return arrayList;
    }
	
	/**
     * This method html-decodes the given string and returns this decoded one.
     * The characters that are decoded are
     * <ul>
     *    <li> '&lt;'
     *    <li> '&gt;'
     *    <li> '&quot;'
     *    <li> '&amp;'
     * </ul>
     *
     * @param  aString the string to be html decoded
     * @return the resultant decoded string
     */
    public static String htmlDecode(String aString) {
        if ((aString == null) || aString.trim().equals("")) {
            return aString;
        }

        aString = aString.replaceAll("&lt;", "<");
        aString = aString.replaceAll("&gt;", ">");
        aString = aString.replaceAll("&quot;", "\"");

        // Order of decoding is important here. Why?
        aString = aString.replaceAll("&amp;", "&");

        return aString;
    }

    /**
     * This method html-encodes the given string and returns the encoded one.
     * The characters that are encoded are
     * <ul>
     *    <li> '&gt;'
     *    <li> '&lt;'
     *    <li> '&quot;'
     *    <li> '&amp;'
     * </ul>
     * At the same time, it does not encode &amp;# entities assuming these to
     * be representations of unicode characters.
     *
     * @param  aString the string to be html encoded
     * @return the resultant encoded string
     */
    public static String htmlEncode(String aString) {
        if ((aString == null) || aString.trim().equals("")) {
            return aString;
        }

        // Order of encoding is important here. Why?
        // &#... are unicode characters encoded. Let us not touch them. 
        // SG: Why shouldnt we touch them. This is the plain text and objective should to show the text as it 
        // SG: should be shown in text editor.
        aString = aString.replaceAll("&([^#])", "&amp;$1");
        aString = aString.replaceAll("<", "&lt;");
        aString = aString.replaceAll(">", "&gt;");
        aString = aString.replaceAll("\"", "&quot;");

        return aString;
    }
	
	/**
	 * Parse a String containing linked requests
	 * @param value
	 * @return List of {@link LinkedRequest}s
	 */
	public static List<LinkedRequest> parseLinkedRequests(String value){
		List<LinkedRequest> linkedRequests = new ArrayList<LinkedRequest>();
		
		if(value != null){
			String[] requestStrings = value.split(",");
			for(String requestString : requestStrings){
				String[] params = requestString.split("#");
				if(params.length >= 2 && params.length <= 3){
					String sysPrefix = params[0];
					String requestIdStr = params[1];
					String actionIdStr = "";
					if(params.length == 3){
						actionIdStr = params[2];
					}
					
					if(ClientUtils.getBAbySysPrefix(sysPrefix) != null){
						int requestId = 0;
						int actionId = 0;
						try{
							requestId = Integer.parseInt(requestIdStr);
							actionId = Integer.parseInt(actionIdStr);
						}catch(Exception e){
							Log.warn("Couldn't parse linked request : " + requestIdStr, e);
						}
						
						if(requestId != 0){
							LinkedRequest lRequest = null;
							if(actionId != 0){
								lRequest = new LinkedRequest(sysPrefix, requestId, actionId);
							}else{
								lRequest = new LinkedRequest(sysPrefix, requestId);
							}
							
							if(lRequest != null)
								linkedRequests.add(lRequest);
						}
					}
				}
			}
		}
		
		return linkedRequests;
	}
	
	/**
	 * @param key
	 * @return URL query parameter value for a given key
	 */
	public static List<String> queryParamsforKey(String key){
		List<String> values =  Window.Location.getParameterMap().get(key);
		if(values == null)
			values = new ArrayList<String>();
		return values;
	}
	
	/**
	 * @param path
	 * @return Url of a relative path
	 */
	public static String getUrlToFilefromBase(String path){
		UrlBuilder urlBuilder = new UrlBuilder();
		urlBuilder.setProtocol(Window.Location.getProtocol());
		urlBuilder.setHost(Window.Location.getHost());
		try
		{
			urlBuilder.setPort(Integer.parseInt(Window.Location.getPort()));
		}
		catch(Exception exp)
		{
			System.err.println("Unable to parse the port from the window.location. So, not setting it");
		}
		urlBuilder.setPath(path);
		return urlBuilder.buildString();
	}
	
	/**
	 * @return The sysprefix of the currently loaded BA
	 */
	public static String getSysPrefix() {
		BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		if(cache != null && cache.isInitialized()){
			return cache.getCurrentSysPrefix();
		}
		return null;
	}

	/**
	 * @return The Currently loaded BA
	 */
	public static BusinessAreaClient getCurrentBA() {
		BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		if(cache != null && cache.isInitialized()){
			return cache.getObject(cache.getCurrentSysPrefix());
		}
		return null;
	}
	
	/**
	 * @param sysPrefix
	 * @return BA object by sysPrefix
	 */
	public static BusinessAreaClient getBAbySysPrefix(String sysPrefix){
		BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		if(cache != null && cache.isInitialized()){
			return cache.getObject(sysPrefix);
		}
		return null;
	}
	
	public static BusinessAreaClient getBAbySysId(int sysId){
		BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		if(cache != null && cache.isInitialized()){
			for(BusinessAreaClient baClient : cache.getValues()){
				if(baClient.getSystemId() == sysId)
					return baClient;
			}
		}
		return null;
	}

	private static UserClient currentUser = null;

	/**
	 * @return The logged in User
	 */
	public static UserClient getCurrentUser() {
		return ClientUtils.currentUser;
	}
	
	public static void init(){
		if(currentUser == null){
			final MessageBox message = new MessageBox();
			message.setButtons(MessageBox.OK);
			message.setTitle("Error in Initializing");
			message.setMessage("Failed to initialize user profile.");
			message.setIcon(MessageBox.ERROR);
			
			// Initialize the logged in user
			GlobalConstants.utilService.getCurrentUser(new AsyncCallback<UserClient>(){
				public void onFailure(Throwable caught) {
					Log.error("Failed to initialize user profile.", caught);
					message.show();
				}
	
				public void onSuccess(UserClient result) {
					currentUser = result;
					if(ClientUtils.getCurrentUser() != null)
						TbitsEventRegister.getInstance().fireEvent(new OnCurrentUserReceived());
					else
						message.show();
				}
			});
		}
	}

	/**
	 * Returns {@link LayoutContainer} showing linked requests.
	 * @param value. The String value for linked_requests
	 * @return
	 */
	public static LayoutContainer getLinkedRequestsContainer(String value){
		List<LinkedRequest> linkedRequests = parseLinkedRequests(value);
		LayoutContainer container = new LayoutContainer(new ColumnLayout());
		boolean hasOne = false;
		for(final LinkedRequest linkedRequest : linkedRequests){
			TbitsHyperLink link = new TbitsHyperLink((hasOne ? ", " : "") + linkedRequest.toString(), new SelectionListener<TbitsHyperLinkEvent>(){
				@Override
				public void componentSelected(TbitsHyperLinkEvent ce) {
					if(linkedRequest.getSysPrefix().equals(getSysPrefix())){
						TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, linkedRequest.getRequestId() + "", false));
					}else{
						TbitsBaseEvent event = new ToViewRequestOtherBA(linkedRequest.getSysPrefix(), linkedRequest.getRequestId());
						TbitsEventRegister.getInstance().fireEvent(event);
					}
				}});
			
			container.add(link, new ColumnData());
			hasOne = true;
		}
		
		return container;
	}
	
	public static List<TbitsTreeRequestData> listToRequestTree(List<TbitsTreeRequestData> requestList, String uniqueProperty, String treeProperty){
		HashMap<Object, TbitsTreeRequestData> requestMap = new HashMap<Object, TbitsTreeRequestData>();
		for(TbitsTreeRequestData model : requestList){
			POJO pojo = model.getAsPOJO(uniqueProperty);
			if(pojo != null)
				requestMap.put(pojo.getValue(), model);
		}
		
		return mapToRequestTree(requestMap, treeProperty);
	}
	
	private static List<TbitsTreeRequestData> mapToRequestTree(HashMap<Object, TbitsTreeRequestData> requestMap, String treeProperty){
		List<TbitsTreeRequestData> requestTree = new ArrayList<TbitsTreeRequestData>();
		for(TbitsTreeRequestData model : requestMap.values()){
			POJO pojo = model.getAsPOJO(treeProperty);
			
			if(pojo != null){
				TbitsTreeRequestData parentModel = requestMap.get(pojo.getValue());
				if(parentModel != null){
					parentModel.add(model);
					continue;
				}
			}
			
			requestTree.add(model);
		}
		return requestTree;
	}
	
	public static List<TbitsTreeRequestData> sortRequests(List<TbitsTreeRequestData> requestList, String property){
		//USE Collections.sort instead
		HashMap<POJO, TbitsTreeRequestData> requestMap = new HashMap<POJO, TbitsTreeRequestData>();
		for(TbitsTreeRequestData model : requestList){
			POJO pojo = model.getAsPOJO(property);
			if(pojo != null)
				requestMap.put(pojo, model);
		}
		List<POJO> keySet = new ArrayList<POJO>(requestMap.keySet());
		int n = keySet.size();
	    for (int pass=1; pass < n; pass++) {  // count how many times
	        // This next loop becomes shorter and shorter
	        for (int i=0; i < n-pass; i++) {
	            if (keySet.get(i).compareTo(keySet.get(i+1)) < 0) {
	                // exchange elements
	                POJO temp = keySet.get(i);  
	                keySet.set(i, keySet.get(i+1));  
	                keySet.set(i+1, temp) ;
	            }
	        }
	    }
	    List<TbitsTreeRequestData> sortedRequests = new ArrayList<TbitsTreeRequestData>();
	    for(POJO pojo : keySet){
	    	if(requestMap.containsKey(pojo))
	    		sortedRequests.add(requestMap.get(pojo));
	    }
	    return sortedRequests;
	}

	/**
	 * Sort {@link TbitsTreeRequestData} according to request_id in Descending order
	 * @param requestMap. The input map
	 * @return. The sorted list
	 */
	public static List<TbitsTreeRequestData> sortRequests(List<TbitsTreeRequestData> requestList){
		return sortRequests(requestList, REQUEST);
	}

	/**
	 * Get the list of fields to be displayed in the view.
	 * 
	 * @return List of columns.
	 */
	public static List<ColPrefs> getPrefsForView(GridColumnView gridColumnView, String sysPrefix){
		List<ColPrefs> currentViewColPrefs = null;
		
		BusinessAreaClient baClient = getBAbySysPrefix(sysPrefix);
		if(baClient != null && getCurrentUser().getColPrefs() != null){
			HashMap<Integer, List<ColPrefs>> prefsForBA = getCurrentUser().getColPrefs().get(baClient.getSystemId());
			if(prefsForBA != null)
				currentViewColPrefs = prefsForBA.get(gridColumnView.getId());
		}
		return currentViewColPrefs;
	}

	public static ColPrefs fieldToColPref(BAField baField){
		ColPrefs pref = new ColPrefs();
		pref.setColSize(200);
		pref.setFieldId(baField.getFieldId());
		pref.setName(baField.getName());
		pref.setDisplayName(baField.getDisplayName());
		return pref;
	}

	/**
	 * Clears request_id in the model and its childrens
	 * @param data
	 */
	public static void clearRequestId(TbitsTreeRequestData data){
		data.setRequestId(0);
		for(ModelData d : data.getChildren()){
			clearRequestId((TbitsTreeRequestData) d);
		}
	}
	
	/**
	 * Sorts the list of model data. 
	 * Can give a specific page in the whole sorted list.
	 * @param <T>
	 * @param models
	 * @param pageSize
	 * @param page
	 * @param asc. True to sort ascending
	 * @return The list of sorted models
	 */
	public static <T extends TbitsModelData> List<T> sort(List<T> models, int pageSize, int page, boolean asc){
		if(models != null){
			for(int i = 0; i < models.size() - 1; i++){
				for(int j = i + 1; j < models.size(); j++){
					boolean swap = false;
					if(asc){
						swap = models.get(i).compareTo(models.get(j)) > 0;
					}else{
						swap = models.get(i).compareTo(models.get(j)) < 0;
					}
					if(swap){
						T temp = models.remove(j);
						models.add(j, models.get(i));
						models.remove(i);
						models.add(i, temp);
					}
				}
				
				if(pageSize > 0 && page > 0 && i == (pageSize * page) - 1){
					break;
				}
			}
			
			if(pageSize > 0 && page > 0){
				if(models.size() >= (pageSize * page))
					return models.subList(pageSize * (page - 1), (pageSize * page));
				else
					return models.subList(pageSize * (page - 1), models.size());
			}
			return models;
		}
		return null;
	}

	public static String htmlify(String value){
		if(value == null)
			return null;
		return value.replaceAll("&", "&amp;").replaceAll(" ", "&nbsp;").replaceAll(">", "&gt;")
		.replaceAll("<", "&lt;").replaceAll("\\n", "<br />");
	}
}
