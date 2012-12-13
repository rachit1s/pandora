package commons.com.tbitsGlobal.utils.client.Events;


/**
 * The event is fired when a tag needs to be applied to or removed from a list of requests.
 * The event is heard by various grids that hold the list of requests that need to be tagged.
 * These grids in turn call the appropriate functions to apply/remove the tags.
 * <br>
 * The tag can be fetched from the event using the <b>getTag</b> method.
 * 
 * @author karan
 *
 */

public class TagRequests extends TbitsBaseEvent {
	
	public static final int APPLY = 1;
	public static final int REMOVE = 2;
	
	private String tag;
	private String type;
	private int action;
	
	public String getTagType(){
		return type;
	}
	
	public String getTag(){
		return tag;
	}
	
	public int getAction(){
		return action;
	}
	
	public TagRequests(String tagType, String tag, int action){
		this.type = tagType;
		this.tag = tag;
		this.action = action;
	}

}
