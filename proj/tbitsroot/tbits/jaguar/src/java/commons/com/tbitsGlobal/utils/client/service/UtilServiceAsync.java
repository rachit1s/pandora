package commons.com.tbitsGlobal.utils.client.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.Dummy;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UserListLoadResult;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;

public interface UtilServiceAsync {
	void getCurrentUser(AsyncCallback<UserClient> callback);
	
	void getDisplayGroups(String sysPrefix, AsyncCallback<ArrayList<DisplayGroupClient>> callback);
	
	void getBAList(AsyncCallback<List<BusinessAreaClient>> callback);
	void getBAMenu(AsyncCallback<BAMenuClient> callback);
	
	void getRequestsForDQL(String sysPrefix, DQL dql, int pageSize, int pageNo, AsyncCallback<DQLResults> callback);
	void getDataByRequestId(String sysPrefix, int requestId, AsyncCallback<TbitsTreeRequestData> callback);
	void getDataByRequestIds(String sysPrefix, List<Integer> requestIds, AsyncCallback<HashMap<Integer,TbitsTreeRequestData>> callback);
	void addRequest(TbitsTreeRequestData requestObj, String sysPrefix, AsyncCallback<TbitsTreeRequestData> callback);
	
	void setColPreferences(int user_id, int view_id, int sys_id, List<ColPrefs> columns, AsyncCallback<String> asyncCallback);
	
	void getAllActiveUsers(AsyncCallback<UserListLoadResult> callback);
	void getActiveUsers(String query, BAField baField, AsyncCallback<UserListLoadResult> callback);
	void getAllBACaptions(AsyncCallback<HashMap<Integer,HashMap<String, String>>> callback);
	void getFields(String sysPrefix, AsyncCallback<List<BAField>> callback);
	
	//dummy
	void getDummy(Dummy dummy, AsyncCallback<Dummy> callback);
	
	void getSearchGridColumnsByBA(String sysPrefix, AsyncCallback<List<BAField>> callback);
	
	void deleteUserDraft(String sysPrefix, int draftId, AsyncCallback<Boolean> callback);
	void saveUserDraft(int draftId, String sysPrefix, TbitsTreeRequestData model, AsyncCallback<Integer> callback);
	
	void connect(AsyncCallback<Boolean> callback);
	
	void showLogout(AsyncCallback<Boolean> callback);
	
	//plugins
	void initPlugins(AsyncCallback<Boolean> callback);
	
	void exportGrid(String sysPrefix, DQL dql, List<String> includedFields, int pageSize, int pageNo, AsyncCallback<String> callback);
	void exportGrid(String sysPrefix, Collection<TbitsTreeRequestData> models, List<String> includedFields, AsyncCallback<String> callback);
	
//============================================================================================ vv TAGS vv
	
	/**
	 * Fetch the list of tags for the specified user.
	 * @param user_id
	 * @param asyncCallback :	List of tag names for the user. null is returned only in case of a SQLException.
	 */
	void getTagsList(int user_id, AsyncCallback<HashMap<String, ArrayList<String>>> asyncCallback);
	
	/**
	 * Apply tags to the specified requests.
	 * @param tag
	 * @param user_id
	 * @param sys_id
	 * @param requests
	 * @param asyncCallback :	True if the operation was completed successfully.
	 * 							False if the tag does not exist or a SQLException occured.
	 */
	void applyTag(String tag, String tagType, int user_id, int sys_id, ArrayList<Integer> requests, AsyncCallback<Boolean> asyncCallback);
	
	/**
	 * Add the given tag to the tags table.
	 * @param tag
	 * @param userId
	 * @param asyncCallback :	True if the operation was completed successfully.
	 * 							False if the tag already exists or a SQLException occured.
	 */
	void addTag(String tag, int userId, AsyncCallback<Boolean> asyncCallback);
	
	/**
	 * Delete the tag and disassociate all requests associated with this tag.
	 * @param tag
	 * @param userId
	 * @param asyncCallback : 	True if the operation was completed successfully.
	 * 							False if the tag does not exist or a SQLException occured.
	 */
	void deleteTag(String tag, int userId, AsyncCallback<Boolean> asyncCallback);
	
	/**
	 * Change the name of oldTag to newTag for the specified user.
	 * @param oldTag
	 * @param newTag
	 * @param userId
	 * @param asyncCallback :	True if the operation was completed successfully.
	 * 							False if a SQLException occured.
	 */
	void modifyTag(String oldTag, String newTag, int userId, AsyncCallback<Boolean> asyncCallback);
	
	/**
	 * Fetch all the requests tagged with the specified tags by the specified user in the specified business area.
	 * @param tags
	 * @param userId
	 * @param sysId
	 * @param asyncCallback : 	List of TbitsTreeRequestData that holds the information of all the relevant requests.
	 * 							null returned only in case of a SQLException.
	 */
	void fetchTaggedRequests(HashMap<String, ArrayList<String>> selectedTags, int userId, int sysId, AsyncCallback<List<TbitsTreeRequestData>> asyncCallback);
	
	/**
	 * Remove all the tags defined by the specified user associated with the given requests in the specified business area.
	 * @param userId
	 * @param systemId
	 * @param requests
	 * @param asyncCallback : 	True if the operation was completed successfully.
	 * 							False if a SQLException occured.
	 */
	void removeAllTagsFromRequests(int userId, int systemId, ArrayList<Integer> requests, AsyncCallback<Boolean> asyncCallback);
	
	/**
	 * Remove the specified tag from the given list of requests.
	 * @param tag
	 * @param userId
	 * @param systemId
	 * @param requests
	 * @param asyncCallback : 	True if the operation was completed successfully.
	 * 							False if the tag does not exist or a SQLException occured.
	 */
	void removeTagFromRequests(String tag, String tagType, int userId, int systemId, ArrayList<Integer> requests, AsyncCallback<Boolean> asyncCallback);
	
	/**
	 * Fetch the value of tbits property specifying if the tag is supported
	 * @param asyncCallback
	 */
	void getIsTagsSupported(AsyncCallback<Boolean> asyncCallback);
	
	//============================================================================================ ^^ TAGS ^^
	
	//============================================================================================ vv TVN vv

	void getIsTvnSupported(AsyncCallback<Boolean> asyncCallback);
	
	void getTvnProtocolUrl(String server, int systemId, int requestId, int attFieldId, AsyncCallback<String> asyncCallback);
	
	//============================================================================================ ^^ TVN ^^
	
	//saved searches
	void saveSearch(String sysPrefix, HashMap<String, String> params, AsyncCallback<Boolean> callback);
	
	void mergePDF(String sysPrefix, int requestId, int actionId, List<FileClient> fileClients, AsyncCallback<String> callback);

	void sendEmailAgain(String sysPrefix, int requestId,
			AsyncCallback<Void> callback);
	
	
	void getEmailHtml(int sysId, int userId, int reqId, AsyncCallback<String> asyncCallback);

	void getHTMLForRequestPrint(int sysId, int userId, int reqId,
			AsyncCallback<String> callback);

	void getActiveFields(String sysPrefix,
			AsyncCallback<List<BAField>> asyncCallback);

	

}
