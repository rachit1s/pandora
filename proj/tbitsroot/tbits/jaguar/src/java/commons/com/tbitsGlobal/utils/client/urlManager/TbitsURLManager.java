package commons.com.tbitsGlobal.utils.client.urlManager;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.History;
import commons.com.tbitsGlobal.utils.client.Events.OnHistoryTokensChanged;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;

/**
 * 
 * @author sourabh
 * 
 * The URL manager that manages the history tokens 
 */
public class TbitsURLManager {
	
	private static TbitsURLManager manager;
	
	private List<String> singleList;
	private List<String> multiList;
	
	private TbitsURLManager(){
		singleList = new ArrayList<String>();
		multiList = new ArrayList<String>();
	}
	
	public static TbitsURLManager getInstance(){
		if(manager == null)
			manager = new TbitsURLManager();
		return manager;
	}
	
	/**
	 * Registers a key
	 * @param key
	 * @param allowOne. True to allow only one value
	 * @return
	 */
	public boolean register(String key, boolean allowOne){
		if(!singleList.contains(key) && !multiList.contains(key)){// : "Key : " + key + " is already registered with URL Manager";
			if(allowOne)
				singleList.add(key);
			else
				multiList.add(key);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Called when url is pushed through address bar by user action rather than through api.
	 */
	public void init(){
		String historyString = History.getToken();
		if(historyString.equals(""))
			return;
		ListStore<HistoryToken> store = this.stringToStore();
		TbitsEventRegister.getInstance().fireEvent(new OnHistoryTokensChanged(store));
	}
	
	/**
	 * Called when the api wants to add a token to the url.
	 * An event should be fired to notify the app about added token.
	 * @param key
	 * @param token
	 */
	public void addToken(HistoryToken token){
		if(singleList.contains(token.getKey()) || multiList.contains(token.getKey())){// : "Key : " + token.getKey() + " is not registered with URL Manager";
			if(hasToken(token) && !token.isForced())
				return;
			
			String historyString = History.getToken();
			
			if(singleList.contains(token.getKey())){
				ListStore<HistoryToken> store = this.stringToStore();
				List<HistoryToken> models = store.findModels(HistoryToken.KEY, token.getKey());
				if(models != null){
					for(HistoryToken model : models)
						store.remove(model);
				}
				historyString = this.storeToString(store);
			}
			
			historyString = addTokenToString(historyString, token);
			History.newItem(historyString, false);
			
			ListStore<HistoryToken> store = new ListStore<HistoryToken>();
			store.add(token);
			TbitsEventRegister.getInstance().fireEvent(new OnHistoryTokensChanged(store));
		}
	}
	
	/**
	 * Called when the api wants to add a list of tokens to the url.
	 * An event should be fired to notify the app about added tokens.
	 * @param key
	 * @param tokens
	 */
	public void addTokens(List<HistoryToken> tokens){
		
		ListStore<HistoryToken> tokenStore = new ListStore<HistoryToken>();
		for(HistoryToken token : tokens){
			assert singleList.contains(token.getKey()) || multiList.contains(token.getKey()) : "Key : " + token.getKey() + " is not registered with URL Manager";
			if(hasToken(token) && !token.isForced())
				return;
			
			String historyString = History.getToken();
			
			if(singleList.contains(token.getKey())){
				ListStore<HistoryToken> store = this.stringToStore();
				List<HistoryToken> models = store.findModels(HistoryToken.KEY, token.getKey());
				if(models != null){
					for(HistoryToken model : models)
						store.remove(model);
				}
				historyString = this.storeToString(store);
			}
			
			historyString = addTokenToString(historyString, token);
			History.newItem(historyString, false);
			
			tokenStore.add(token);
		}
		
		TbitsEventRegister.getInstance().fireEvent(new OnHistoryTokensChanged(tokenStore));
		
	}
	
	/**
	 * Removes a History token
	 * @param token
	 */
	public void removeToken(HistoryToken token){
		assert singleList.contains(token.getKey()) || multiList.contains(token.getKey()) : "Key : " + token.getKey() + " is not registered with URL Manager";
		ListStore<HistoryToken> store = this.stringToStore();
		if(store.findModel(token) != null)
			store.remove(token);
		History.newItem(this.storeToString(store), false);
	}
	
	/**
	 * @return A ListStore of {@link HistoryToken} formed by the current History String
	 */
	public ListStore<HistoryToken> stringToStore(){
		String historyString = History.getToken();
		ListStore<HistoryToken> store = new ListStore<HistoryToken>();
		String[] arr = historyString.split("&");
		for(String tokenString : arr){
			String[] keyValue = tokenString.split("=");
			if(keyValue.length >= 1){
				String key = keyValue[0];
				String value = "";
				if(keyValue.length == 2)
					value = keyValue[1];
				store.add(new HistoryToken(key, value, true));
			}
		}
		return store;
	}
	
	/**
	 * @param store
	 * @return Forms a history string from store
	 */
	public String storeToString(ListStore<HistoryToken> store){
		String historyString = "";
		if(store != null){
			for(HistoryToken token : store.getModels()){
				if(!historyString.equals(""))
					historyString += "&";
				historyString += token.getKey() + "=" + token.getValue();
			}
		}
		return historyString;
	}
	
	/**
	 * @param key
	 * @return true is History String has the given key
	 */
	public boolean hasKey(String key){
		return this.stringToStore().findModel(HistoryToken.KEY, key) != null;
	}
	
	private String addTokenToString(String str, HistoryToken token){
		return str + "&" + token.getKey() + "=" + token.getValue();
	}
	
	private boolean hasToken(HistoryToken token){
		String historyString = History.getToken();
		if(historyString == null || historyString.equals(""))
			return false;
		String[] arr = historyString.split("&");
		for(String tokenString : arr){
			String[] keyValue = tokenString.split("=");
			if(keyValue.length >= 1){
				String historyKey = keyValue[0];
				if(historyKey.equals(token.getKey())){
					String historyValue = "";
					if(keyValue.length == 2)
						historyValue = keyValue[1];
					if(historyValue.equals(token.getValue()))
						return true;
				}
			}
		}
		
		return false;
	}
}
