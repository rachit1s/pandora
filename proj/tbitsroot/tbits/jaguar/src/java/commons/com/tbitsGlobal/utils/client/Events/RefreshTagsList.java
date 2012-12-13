package commons.com.tbitsGlobal.utils.client.Events;

import java.util.List;


/**
 * This event is called when the tags are edited or when the tags are fetched from DB. 
 * The event is fired by the tag addition TagsMenuItem in the TagsMenuButton.
 * <br>
 * The new tags can be fetched from the event using the <b>getNewTags</b> method.
 * 
 * @author karan
 *
 */

public class RefreshTagsList extends TbitsBaseEvent {
	
	private List<String> privateTags;
	private List<String> publicTags;
	
	public RefreshTagsList(List<String> list, List<String> list2){
		this.publicTags = list;
		this.privateTags = list2;
	}
	
	public List<String> getPrivateTags(){
		return privateTags;
	}

	public List<String> getPublicTags() {
		return publicTags;
	}

}