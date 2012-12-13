package commons.com.tbitsGlobal.utils.client;

import java.util.HashMap;

public class Captions {
	public static String CAPTIONS_ADD_LINK_REQUESTS			="captions.add.link_requests";
	public static String CAPTIONS_ALL_CAMEL_CASE_REQUEST	="captions.all.camel_case_request";
	public static String CAPTIONS_ALL_CAMEL_CASE_REQUESTS	="captions.all.camel_case_requests";
	public static String CAPTIONS_ALL_MY_REQUESTS			="captions.all.my_requests";
	public static String CAPTIONS_ALL_NEW_REQUEST			="captions.all.new_request";
	public static String CAPTIONS_ALL_REQUEST				="captions.all.request";
	public static String CAPTIONS_ALL_REQUESTS				="captions.all.requests";
	public static String CAPTIONS_MAIN_ALL_REQUEST_NUMBER	="captions.main.all.request_number";
	public static String CAPTIONS_VIEW_ADD_REQUEST			="captions.view.add_request";
	public static String CAPTIONS_VIEW_ADD_SUBREQUEST		="captions.view.add_subrequest";
	public static String CAPTIONS_VIEW_PRINT_REQUEST_DETAILS="captions.view.print_request_details";
	public static String CAPTIONS_VIEW_REQUEST_DETAILS		="captions.view.request_details";
	public static String CAPTIONS_VIEW_REQUEST_HISTORY		="captions.view.request_history";
	public static String CAPTIONS_VIEW_SORT_REQUEST_HISTORY	="captions.view.sort_request_history";
	public static String CAPTIONS_VIEW_SUBREQUESTS			="captions.view.subrequests";
	public static String CAPTIONS_VIEW_TRANSFER_REQUEST		="captions.view.transfer_request";
	public static String CAPTIONS_VIEW_UPDATE_REQUEST		="captions.view.update_request";
	public static String CAPTIONS_ADD_SUMMARY				="captions.view.add_summary";
	public static String CAPTIONS_UPDATE_SUMMARY			="captions.view.update_summary";
	
	public static final String CAPTION_TABLE_NAME 			= "captions_properties";
	public static final String CAPTION_COL_NAME 			= "name";
	public static final String CAPTION_COL_VALUE 			= "value";
	public static final String CAPTION_COL_SYS_ID 			= "sys_id";
	
	private static HashMap<Integer,HashMap<String, String>> allBACaptionsMap = new HashMap<Integer, HashMap<String,String>>();
	
	public static HashMap<String, String> getBACaptions(int sysId){
		if(allBACaptionsMap == null)
			return null;
		return allBACaptionsMap.get(sysId);
	}
	
	/**
	 * @param key
	 * @return The Caption for Currentlt loaded BA
	 */
	public static String getCurrentBACaptionByKey(String key){
		int sysId = ClientUtils.getCurrentBA().getSystemId();
		return getBACaptionByKey(sysId, key);
	}
	
	/**
	 * @param sysId
	 * @param key
	 * @return The caption for given BA
	 */
	public static String getBACaptionByKey(int sysId, String key){
		HashMap<String, String> baCaptions = getBACaptions(sysId);
		if(baCaptions != null){
			String value = baCaptions.get(key);
			if(value != null)
				return value;
		}
		
		return getBACaptionByKey(0, key);
	}
	
	/**
	 * @param key
	 * @return The Caption that is common for all BA
	 */
	public static String getCommonCaptionByKey(String key){
		HashMap<String, String> baCaptions = getBACaptions(0);
		if(baCaptions != null){
			return baCaptions.get(key);
		}
		
		return "";
	}
	
	/**
	 * @return The Record display name for Currently loaded BA
	 */
	public static String getRecordDisplayName(){
		String recordDisplayName = getCurrentBACaptionByKey(Captions.CAPTIONS_ALL_CAMEL_CASE_REQUEST);
		if(recordDisplayName == null || recordDisplayName.equals(""))
			recordDisplayName = "Request";
		return recordDisplayName;
	}
	
	/**
	 * @param sysPrefix
	 * @return The Record display name for given BA
	 */
	public static String getRecordDisplayName(String sysPrefix){
		int sysId = ClientUtils.getBAbySysPrefix(sysPrefix).getSystemId();
		String recordDisplayName = getBACaptionByKey(sysId, Captions.CAPTIONS_ALL_CAMEL_CASE_REQUEST);
		if(recordDisplayName == null || recordDisplayName.equals(""))
			recordDisplayName = "Request";
		return recordDisplayName;
	}
	
	public static void setAllBACaptionsMap(HashMap<Integer,HashMap<String, String>> allBACaptionsMap) {
		Captions.allBACaptionsMap = allBACaptionsMap;
	}
}
