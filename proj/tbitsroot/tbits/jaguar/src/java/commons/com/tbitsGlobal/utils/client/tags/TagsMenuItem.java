package commons.com.tbitsGlobal.utils.client.tags;

import commons.com.tbitsGlobal.utils.client.widgets.TriStateCheckMenuItem;


/**
 * Returns the tag menu item. These menu items are attached to the menu in the TagsMenuButton.
 * <br>
 * Each TagsMenuItem carries the names of a tag. 
 * Clicking on the name of the tag will tag fires thee ToApplyTag event. 
 * There is a <b>X</b> button provided next to every tag 
 * to remove the tag from selected requests. Clicking on the <b>X</b> button
 * fires the ToRemoveTag event.
 *
 * @author karan
 *
 */

public class TagsMenuItem extends TriStateCheckMenuItem{
	
	//============================================================================================

	protected String tagName;
	protected String tagType;
	private boolean found;
	private boolean foundInAll;
	
	//============================================================================================

	public TagsMenuItem(String tagType, String tagName){
		super(tagName);
		this.tagType = tagType;
		this.tagName = tagName;
		this.hideOnClick = false;
		reset();
	}
	
	public void setTriState(TriState state){
		super.setTriState(state);
		
		if(state.equals(TriState.CHECKED)){
			found = true;
			foundInAll = true;
		}
		else if(state.equals(TriState.PARTIAL)){
			found = true;
			foundInAll = false;
		}
		else if(state.equals(TriState.UNCHECKED)){
			found = false;
		}
	}
	
	public void reset(){
		found = false;
		foundInAll = true;
	}
	
	public boolean isFound(){
		return found;
	}
	
	public boolean isFoundInAll(){
		return foundInAll;
	}
	
	public void setFound(boolean found){
		this.found = found;
	}
	
	public void setFoundInAll(boolean foundInAll){
		this.foundInAll = foundInAll;
	}

	//============================================================================================

}
