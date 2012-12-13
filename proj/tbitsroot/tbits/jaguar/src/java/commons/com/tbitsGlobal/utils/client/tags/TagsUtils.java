package commons.com.tbitsGlobal.utils.client.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.RefreshTagsList;
import commons.com.tbitsGlobal.utils.client.Events.ShowTaggedRequests;
import commons.com.tbitsGlobal.utils.client.Events.TagRequests;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * The class provides utility functions for all the tags related functionalities.
 * It also maintains a list of existing tags for each user.
 * This list is managed internally.
 * 
 * @author karan
 *
 */
public class TagsUtils {

	//============================================================================================

	public static final int PUBLIC_TAGS_USER = -1;
	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";
	
	// Hacks for the tags to be used as search filter with fields. 
	// Any change in this constant needs to be relected in transbit.tbits.searcher.Searcher
	// for the searcher to recognise them.
	public static final String PUBLIC_TAGS_FIELD_FILTER = "__public_tags";
	public static final String PRIVATE_TAGS_FIELD_FILTER = "__private_tags";

	//============================================================================================

	/**
	 * The hashmap carries all the existing tags by a user. 
	 * It is updated everytime the tags are fetched.
	 */
	private static HashMap<Integer, List<String>> existingTags = new HashMap<Integer, List<String>>();
	
	/**
	 * Associate the specified tags with the given user_id.
	 * 
	 * @param user_id
	 * @param tags
	 */
	private static void setTags(int user_id, ArrayList<String> tags){
		existingTags.put(user_id, tags);
	}
	
	/**
	 * Remove the tag from the corresponding list of tags.
	 * 
	 * @param user_id
	 * @param tag
	 */
	private static void removeTag(int user_id, String tag){
		if(!isTagExists(user_id, tag))
			return;
		existingTags.get(user_id).remove(tag);
	}
	
	/**
	 * Add the tag to the corresponding list of tags.
	 * 
	 * @param user_id
	 * @param tag
	 */
	private static void addTag(int user_id, String tag){
		if(!isTagExists(user_id, tag))
			existingTags.get(user_id).add(tag);
	}
	
	/**
	 * Check if the specified tag is already defined.
	 * 
	 * @param id
	 * @param tag
	 * @return Whether the tag is defined by the user
	 */
	private static boolean isTagExists(int user_id, String tag){
		if(existingTags.containsKey(user_id)){
			if(existingTags.get(user_id).contains(tag))
				return true;
		}
		return false;
	}
	
	/**
	 * @param userId
	 * @return cached tags for the given user
	 */
	public static List<String> getExistingTagsBy(int userId){
		return existingTags.get(userId);
	}

	//============================================================================================

	/**
	 * Apply the given tag to the currently selected requests.
	 * @param tag
	 */
	public static void applyTag(UserClient user, BusinessAreaClient ba, ArrayList<Integer> requests, final String tag, final String tagType) {
		
		GlobalConstants.utilService.applyTag(tag, tagType, user.getUserId(), ba.getSystemId(), requests, new AsyncCallback<Boolean>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while applying tag '" + tag + "'.", caught);
				Log.error("Error while applying tag '" + tag + "'.", caught);
			}

			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Successfully applied tag '" + tag + "'. Please reload to see the changes.");
					Log.info("Successfully applied tag '" + tag + "'. Please reload to see the changes.");
				}
				else{
					TbitsInfo.error("Error while applying tag '" + tag + "'.");
					Log.error("Error while applying tag '" + tag + "'.");
				}
			}
			
		});
	}

	//============================================================================================

	/**
	 * Remove all tags of the specified user from the given requests.
	 * @param requests
	 */
	public static void removeAllTagsFromRequests(UserClient user, BusinessAreaClient ba, ArrayList<Integer> requests) {
		
		GlobalConstants.utilService.removeAllTagsFromRequests(user.getUserId(), ba.getSystemId(), requests, new AsyncCallback<Boolean>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while removing tag...", caught);
				Log.error("Error while removing tag...", caught);
			}

			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Successfully removed tags. Please reload to see the changes.");
					Log.info("Successfully removed tags. Please reload to see the changes.");
				}
				else{
					TbitsInfo.error("Error while removing tags.");
					Log.error("Error while removing tags.");
				}
			}
		});
	}

	//============================================================================================

	/**
	 * Remove the given tag from the specified requests.
	 * @param requests
	 * @param tag
	 */
	public static void removeTagFromRequests(UserClient user, BusinessAreaClient ba, ArrayList<Integer> requests, final String tag, final String tagType) {
		
		GlobalConstants.utilService.removeTagFromRequests(tag, tagType, user.getUserId(), ba.getSystemId(), requests, new AsyncCallback<Boolean>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while removing tag.", caught);
				Log.error("Error while removing tag.", caught);
			}

			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Successfully removed tag '" + tag + "'. Please reload to see the changes.");
					Log.info("Successfully removed tag '" + tag + "'. Please reload to see the changes.");
				}
				else{
					TbitsInfo.error("Error while removing tag '" + tag + "'.");
					Log.error("Error while removing tag '" + tag + "'.");
				}
			}
		});
	}
	
	//============================================================================================

	/**
	 * Fetch all the requests in the specified BA which are tagged 
	 * by the selected tags corresponding to the user.
	 * 
	 * @param selectedTags
	 * @param user
	 * @param ba
	 */
	public static void fetchTaggedRequests(HashMap<String, ArrayList<String>> selectedTags, UserClient user, BusinessAreaClient ba) {
		TbitsInfo.info("Fetching tagged requests. Please wait.");
		GlobalConstants.utilService.fetchTaggedRequests(selectedTags, user.getUserId(), ba.getSystemId(), new AsyncCallback<List<TbitsTreeRequestData>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while fetching tagged requests...", caught);
				Log.error("Error while fetching tagged requests...", caught);
			}

			public void onSuccess(List<TbitsTreeRequestData> result) {
				TbitsEventRegister.getInstance().fireEvent(new ShowTaggedRequests(result));
			}
		});
	}
	
	//============================================================================================

	/**
	 * Fetch all the tags defined by the user.
	 * @param user
	 */
	public static void fetchTags(final UserClient user){
		fetchTags(user, true);
	}
	
	/**
	 * Fetch all the tags defined by the user.
	 * @param user
	 * @param force : force a DB lookup
	 */
	private static void fetchTags(final UserClient user, boolean force){
		if(!force && existingTags.containsKey(user.getUserId())){
			TbitsEventRegister.getInstance().fireEvent(new RefreshTagsList(existingTags.get(PUBLIC_TAGS_USER), existingTags.get(user.getUserId())));
		}	
		else{
			GlobalConstants.utilService.getTagsList(user.getUserId(), new AsyncCallback<HashMap<String, ArrayList<String>>>() {
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error while fetching tags...", caught);
					Log.error("Error while fetching tags...", caught);
				}

				public void onSuccess(HashMap<String, ArrayList<String>> tags) {
					setTags(PUBLIC_TAGS_USER, tags.get("public"));
					setTags(user.getUserId(), tags.get("private"));
					TbitsEventRegister.getInstance().fireEvent(new RefreshTagsList(existingTags.get(PUBLIC_TAGS_USER), existingTags.get(user.getUserId())));
				}
			});
		}
	}

	//============================================================================================

	/**
	 * Modify the given old tag to new tag. The associations are also shifted to the new tag.
	 * 
	 * @param oldTag
	 * @param newTag
	 * @param user
	 */
	public static void modifyTag(final String oldTag, final String newTag, final UserClient user) {
		
		// Check if tag is empty string or already exists
		if(newTag == null || newTag.equals("")){
			TbitsInfo.error("Invalid tag.");
			Log.error("Invalid tag.");
		}
		else if(isTagExists(user.getUserId(), newTag)){
			TbitsInfo.error("The tag " + newTag + " already exists.");
			Log.error("The tag " + newTag + " already exists.");
		}
		// Modify the tag
		else{
			GlobalConstants.utilService.modifyTag(oldTag, newTag, user.getUserId(), new AsyncCallback<Boolean>(){
				
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error while applying tag...", caught);
					Log.error("Error while applying tag...", caught);
				}
	
				public void onSuccess(Boolean result) {
					if(result){
						removeTag(user.getUserId(), oldTag);
						addTag(user.getUserId(), newTag);
						fetchTags(user, false);
						TbitsInfo.info("Modified " + oldTag + " to " + newTag + " for " + user.getUserLogin() +".");
						Log.info("Modified " + oldTag + " to " + newTag + " for " + user.getUserLogin() +".");
					}
					else{
						TbitsInfo.error("Error while modifing " + oldTag + " to " + newTag);
						Log.error("Error while modifing " + oldTag + " to " + newTag);
					}
				}
			});
		}
	}

	//============================================================================================

	/**
	 * Add the mentioned tag to the list of tags for the user.
	 * 
	 * @param tag
	 * @param user
	 */
	public static void addTag(final String tag, final UserClient user) {
		
		// Check if tag is empty string or already exists
		if(tag == null || tag.equals("")){
			TbitsInfo.error("Invalid tag.");
			Log.error("Invalid tag.");
		}
		else if(isTagExists(user.getUserId(), tag)){
			TbitsInfo.error("The tag " + tag + " already exists.");
			Log.error("The tag " + tag + " already exists.");
		}
		else{
			GlobalConstants.utilService.addTag(tag, user.getUserId(), new AsyncCallback<Boolean>(){
				
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error while applying tag...", caught);
					Log.error("Error while applying tag...", caught);
				}
	
				public void onSuccess(Boolean result) {
					if(result){
						addTag(user.getUserId(), tag);
						fetchTags(user, false);
						TbitsEventRegister.getInstance().fireEvent(new TagRequests("private", tag, TagRequests.APPLY));
						Log.info("Created new tag : " + tag + " for " + user.getUserLogin()  +". Please reload to see the changes.");
					}
					else
						Log.error("Error creating new tag : " + tag);
				}
				
			});
		}
	}
	
	//============================================================================================

	/**
	 * Delete the specified tags for the user. Remove all associations of tags from the requests.
	 * 
	 * @param user_id
	 * @param list
	 */
	public static void deleteTags(final UserClient user, final List<String> list){
		
		if(list == null || list.size() == 0)
			return;
		
		for(final String tag : list){
			deleteTag(user, tag);
		}
	}
	
	/**
	 * Delete the specified tag for the user. Remove all associations of tag from the requests.
	 * 
	 * @param user_id
	 * @param tag
	 */
	public static void deleteTag(final UserClient user, final String tag){
		
		final int user_id = user.getUserId();
		if(isTagExists(user_id, tag)){
			GlobalConstants.utilService.deleteTag(tag, user_id, new AsyncCallback<Boolean>(){
	
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error while deleting tag...", caught);
					Log.error("Error while deleting tag...", caught);
				}
	
				public void onSuccess(Boolean result) {
					if(result){
						removeTag(user_id, tag);
						fetchTags(user, true);
						TbitsInfo.info("Deleted tag : " + tag + " for " + user.getUserLogin() +". Please reload to see the changes.");
						Log.info("Deleted tag : " + tag + " for " + user.getUserLogin() +". Please reload to see the changes.");
					}
					else{
						TbitsInfo.error("Error while deleting tag : " + tag);
						Log.error("Error while deleting tag : " + tag);
					}
				}
				
			});
		}
		else if(isTagExists(PUBLIC_TAGS_USER, tag)){
			TbitsInfo.info("Public tags cannot be deleted!");
			Log.info("Public tags cannot be deleted!");
		}
	}
	
	//============================================================================================

}
