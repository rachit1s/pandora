package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.widget.ContentPanel;

/**
 * 
 * @author sourabh
 * 
 * Calculates the height dynamically when the widget is layout
 * 
 * Works correctly only when all the children are one below the other and none parallel. 
 * Basically height = frameHeight + borders + sum of heights of children
 */
public class EffectiveHeightContainer extends ContentPanel{

	public EffectiveHeightContainer() {
		super();
		
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		this.setLayoutOnChange(true);
	}

	public int getEffectiveHeight() {
		if(this.isAttached() && this.isRendered()){
			int childCount = this.getItemCount();
			
			int height = this.getFrameHeight() + (this.getBodyBorder() ? 2:0);
			
			for(int index = 0; index < childCount; index++){
				El child = this.getItem(index).el();
				height += child.getHeight();
			}
			
			return height;
		}
		return 0;
	}
	
	@Override
	protected void onAfterLayout() {
		super.onAfterLayout();
		
		int headingPanelHeight = this.getEffectiveHeight();
		this.setHeight(headingPanelHeight);
	}
}
