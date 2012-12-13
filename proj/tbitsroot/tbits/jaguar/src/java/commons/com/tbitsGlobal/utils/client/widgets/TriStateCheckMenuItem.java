package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;
import commons.com.tbitsGlobal.utils.client.IconConstants;

/**
 * 
 * @author sourabh
 * 
 * A menu item with three selectable states.
 */
public class TriStateCheckMenuItem extends MenuItem {

	public enum TriState{
		CHECKED,
		UNCHECKED,
		PARTIAL
	}
	
	private TriState triState = TriState.UNCHECKED;
	private boolean isTriState = true;
	private TriState lastState;
	public TriState nextState = null;
	
	public TriStateCheckMenuItem() {
		hideOnClick = true;
	    itemStyle = "x-menu-item x-menu-check-item";
	    canActivate = true;
	}
	
	public TriStateCheckMenuItem(String text) {
	    this();
	    setText(text);
	}
	
	/**
	 * Toggles state
	 */
	public void setNextState(){
		if(nextState == null){
			if(triState.equals(TriState.CHECKED)){
				if(isTriState){
					if(lastState != null && lastState.equals(TriState.UNCHECKED))
						setTriState(TriState.PARTIAL);
					else
						setTriState(TriState.UNCHECKED);
				}
				else
					setTriState(TriState.UNCHECKED);
			}else if(isTriState && triState.equals(TriState.PARTIAL)){
				if(lastState != null && lastState.equals(TriState.CHECKED))
					setTriState(TriState.UNCHECKED);
				else
					setTriState(TriState.CHECKED);
			}
			else{
				if(isTriState){
					if(lastState != null && lastState.equals(TriState.CHECKED))
						setTriState(TriState.PARTIAL);
					else
						setTriState(TriState.CHECKED);
				}
				else
					setTriState(TriState.CHECKED);
			}
		}
		else{
			setTriState(nextState);
			nextState = null;
		}
	}
	
	public void setTriState(TriState state){
		setTriState(state, false);
	}
	
	public void setTriState(TriState state, boolean supressEvent){
		lastState = triState;
		
		if(!isTriState && state.equals(TriState.PARTIAL))
			return;
		
		if (!rendered) {
			this.triState = state;
			return;
	    }
	    MenuEvent me = new MenuEvent(parentMenu);
	    me.setItem(this);
	    if (supressEvent || fireEvent(Events.BeforeCheckChange, me)) {
		    if(state.equals(TriState.CHECKED))
		    	this.setIcon(GXT.IMAGES.checked());
		    else if(state.equals(TriState.UNCHECKED))
		    	this.setIcon(GXT.IMAGES.unchecked());
		    else{
		    	this.setIcon(new ClippedImagePrototype(IconConstants.THIRD_STATE, 0, 26, 13, 15));
		    }
		    this.triState = state;
		    if (!supressEvent) {
		        fireEvent(Events.CheckChange, me);
		    }
	    }
	}
	
	public TriState getTriState() {
		return triState;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.setTriState(TriState.UNCHECKED);
	}
	
	@Override
	protected void onClick(ComponentEvent be) {
		if (!disabled) {
			setNextState();
	    }
	    super.onClick(be);
	}

	public void setIsTriState(boolean isTriState) {
		this.isTriState = isTriState;
		
		if(this.triState.equals(TriState.PARTIAL))
			this.setTriState(TriState.UNCHECKED);
	}

	public boolean isTriState() {
		return isTriState;
	}
}
