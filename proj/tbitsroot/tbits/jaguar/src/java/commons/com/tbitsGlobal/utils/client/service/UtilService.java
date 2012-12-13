
package commons.com.tbitsGlobal.utils.client.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.Dummy;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
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

public interface UtilService extends RemoteService{
	
	/**
	 * @return Returns the logged in user
	 * @throws TbitsExceptionClient
	 */
	UserClient getCurrentUser() throws TbitsExceptionClient;
	
	/**
	 * List of Business Areas
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public List<BusinessAreaClient> getBAList() throws TbitsExceptionClient;
	
	/**
	 * @return The {@link BAMenuClient}
	 * @throws TbitsExceptionClient
	 */
	BAMenuClient getBAMenu() throws TbitsExceptionClient;
	
	/**
	 * Gets the requests for a dql in a BA.
	 * @param sysPrefix
	 * @param dql
	 * @param pageSize
	 * @param pageNo
	 * @param fields
	 * @return. Map of request IDs and {@link TbitsTreeRequestData}s
	 * @throws TbitsExceptionClient
	 */
	DQLResults getRequestsForDQL(String sysPrefix, DQL dql, int pageSize, int pageNo) throws TbitsExceptionClient;
	
	/**
	 * Gets the {@link TbitsTreeRequestData} for a particular request_id
	 * @param sysPrefix
	 * @param requestId
	 * @return
	 * @throws TbitsExceptionClient
	 */
	TbitsTreeRequestData getDataByRequestId(String sysPrefix, int requestId) throws TbitsExceptionClient;
	
	/**
	 * Gets requests for a set of request ids
	 * @param sysPrefix
	 * @param requestIds
	 * @return
	 */
	HashMap<Integer,TbitsTreeRequestData> getDataByRequestIds(String sysPrefix, List<Integer> requestIds);
	
	/**
	 * Sets the column preferences
	 * @param user_id
	 * @param view_id
	 * @param sys_id
	 * @param fields
	 * @return
	 * @throws TbitsExceptionClient
	 */
	String setColPreferences(int user_id,int view_id,int sys_id, List<ColPrefs> fields) throws TbitsExceptionClient;
	
	/**
	 * Adds or Updates a request.
	 * @param requestObj. Object representing request to be added or updated.
	 * @param sysPrefix.
	 * @return The {@link TbitsTreeRequestData} for the request
	 * @throws TbitsExceptionClient
	 * 
	 * If the request id inside requestObj is != 0 the this would act as update request.
	 */
	TbitsTreeRequestData addRequest(TbitsTreeRequestData requestObj, String sysPrefix) throws TbitsExceptionClient;
	
	/**
	 * @return All active users
	 * @throws TbitsExceptionClient 
	 */
	UserListLoadResult getAllActiveUsers() throws TbitsExceptionClient;
	
	/**
	 * @param query
	 * @return Active users based on a query
	 * @throws TbitsExceptionClient 
	 */
	UserListLoadResult getActiveUsers(String query, BAField baField) throws TbitsExceptionClient;
	
	/**
	 * @return All captions
	 */
	HashMap<Integer,HashMap<String, String>> getAllBACaptions();
	List<BAField> getFields(String sysPrefix) throws TbitsExceptionClient;
	
	List<BAField> getActiveFields(String sysPrefix) throws TbitsExceptionClient;
	//dummy 
	Dummy getDummy(Dummy dummy);
	
	List<BAField> getSearchGridColumnsByBA(String sysPrefix) throws TbitsExceptionClient;
	
	/**
	 * Deletes a draft
	 * @param sysPrefix
	 * @param draftId
	 * @return True if deleted. False otherwise
	 * @throws TbitsExceptionClient
	 */
	boolean deleteUserDraft(String sysPrefix, int draftId) throws TbitsExceptionClient;
	
	/**
	 * Saves a draft
	 * @param draftId
	 * @param sysPrefix
	 * @param model
	 * @return the Draft Id
	 * @throws TbitsExceptionClient
	 */
	int saveUserDraft(int draftId, String sysPrefix, TbitsTreeRequestData model) throws TbitsExceptionClient;
	
	/**
	 * Pings the server
	 * @return true if server is available
	 */
	boolean connect();
	
	/**
	 * @return True is "Logout" link is to be displayed
	 */
	boolean showLogout();
	
	/**
	 * Initialize plugins. Used only in dev mode
	 * @return
	 */
	boolean initPlugins();
	
	/**
	 * Get display groups for a BA.
	 * @param sysPrefix
	 * @return. List of Display Groups.
	 * @throws TbitsExceptionClient
	 */
	ArrayList<DisplayGroupClient> getDisplayGroups(String sysPrefix) throws TbitsExceptionClient;
	
	//------------------------------------------------------------------ Tags ---------------------------------------------------------------------//
	//============================================================================================ vv TAGS vv
	
	/**
	 * Fetch the list of tags for the specified user.
	 * @param user_id
	 * @return List of tag names for the user. null is returned only in case of a SQLException.
	 * @throws TbitsExceptionClient 
	 */
	HashMap<String, ArrayList<String>> getTagsList(int user_id) throws TbitsExceptionClient;

	/**
	 * Apply tags to the specified requests.
	 * @param tag
	 * @param user_id
	 * @param sys_id
	 * @param requests
	 * @return True if the operation was completed successfully.
	 * 			False if the tag does not exist or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	boolean applyTag(String tag, String tagType, int user_id, int sys_id, ArrayList<Integer> requests) throws TbitsExceptionClient;

	/**
	 * Add the given tag to the tags table.
	 * @param tag
	 * @param user_id
	 * @return True if the operation was completed successfully.
	 * 			False if the tag already exists or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	boolean addTag(String tag, int userId) throws TbitsExceptionClient;

	/**
	 * Delete the tag and disassociate all requests associated with this tag.
	 * @param tag
	 * @param user_id
	 * @return True if the operation was completed successfully.
	 * 			False if the tag does not exist or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	boolean deleteTag(String tag, int userId) throws TbitsExceptionClient;

	/**
	 * Change the name of oldTag to newTag for the specified user.
	 * @param oldTag
	 * @param newTag
	 * @param user_id
	 * @return True if the operation was completed successfully.
	 * 			False if a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	boolean modifyTag(String oldTag, String newTag, int userId) throws TbitsExceptionClient;

	/**
	 * Fetch all the requests tagged with the specified tags by the specified user in the specified business area.
	 * @param tags
	 * @param user_id
	 * @param sys_id
	 * @return List of TbitsTreeRequestData that holds the information of all the relevant requests.
	 * 			null returned only in case of a SQLException.
	 * @throws TbitsExceptionClient 
	 */
	List<TbitsTreeRequestData> fetchTaggedRequests(HashMap<String, ArrayList<String>> selectedTags, int userId, int sysId) throws TbitsExceptionClient;

	/**
	 * Remove all the tags defined by the specified user associated with the given requests in the specified business area.
	 * @param user_id
	 * @param system_id
	 * @param requests
	 * @return True if the operation was completed successfully.
	 * 			False if a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	boolean removeAllTagsFromRequests(int userId, int systemId, ArrayList<Integer> requests) throws TbitsExceptionClient;

	/**
	 * Remove the specified tag from the given list of requests.
	 * @param tag
	 * @param user_id
	 * @param system_id
	 * @param requests
	 * @return True if the operation was completed successfully.
	 * 			False if the tag does not exist or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	boolean removeTagFromRequests(String tag, String tagType, int userId, int systemId, ArrayList<Integer> requests) throws TbitsExceptionClient;

	/**
	 * Fetch the value of tbits property specifying if the tag is supported
	 * @return boolean value telling whether tags are supported
	 * @throws TbitsExceptionClient 
	 */
	boolean getIsTagsSupported() throws TbitsExceptionClient;

	//============================================================================================ ^^ TAGS ^^
	
	/**
	 * Exports the requests returned from the dql on a csv file
	 * @param sysPrefix
	 * @param dql
	 * @return. The URL of the exported CSV file
	 * @throws TbitsExceptionClient
	 */
	public String exportGrid(String sysPrefix, DQL dql, List<String> includedFields, int pageSize, int pageNo) throws TbitsExceptionClient;
	
	/**
	 * Exports the requests on a csv file
	 * @param sysPrefix
	 * @param models
	 * @return. The URL of the exported CSV file
	 * @throws TbitsExceptionClient
	 */
	String exportGrid(String sysPrefix, Collection<TbitsTreeRequestData> models, List<String> includedFields) throws TbitsExceptionClient;
	
	/**
	 * Saves a search query
	 * @param sysPrefix
	 * @param params
	 * @return
	 * @throws TbitsExceptionClient
	 */
	boolean saveSearch(String sysPrefix, HashMap<String, String> params) throws TbitsExceptionClient;
	
	/**
	 * Merges the given PDF Files and gives out the output
	 * @param sysPrefix
	 * @param requestId
	 * @param actionId
	 * @param fileClients
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public String mergePDF(String sysPrefix, int requestId, int actionId, List<FileClient> fileClients) throws TbitsExceptionClient;
	
	/**
	 * Sends email of a request. The way it was send when adding/updating a request earlier.
	 * @param sysPrefix
	 * @param requestId
	 * @throws TbitsExceptionClient
	 */
	public void sendEmailAgain(String sysPrefix, int requestId) throws TbitsExceptionClient;
	
	/**
	* Get the HTML formatted for email to be send for a request wrt a user
	*/
	String getEmailHtml(int sysId, int userId, int reqId);
	
	String getHTMLForRequestPrint(int sysId, int userId, int reqId);

	boolean getIsTvnSupported();

	String getTvnProtocolUrl(String server, int systemId, int requestId, int attFieldId) throws TbitsExceptionClient;

	
	
}
