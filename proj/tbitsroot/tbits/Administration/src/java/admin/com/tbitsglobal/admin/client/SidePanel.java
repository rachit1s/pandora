package admin.com.tbitsglobal.admin.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;

public class SidePanel extends ContentPanel {
	public SidePanel(final MainTabPanel tabPanel) {
		super();
		
		this.setHeaderVisible(false);
		
		AccordionLayout layout = new AccordionLayout();
		this.setLayout(layout);
		
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		
		ContentPanel trnAdminContainer = new ContentPanel(vBoxLayout);
		trnAdminContainer.setHeading("Transmittal Administration");
		trnAdminContainer.setBodyBorder(false);
		
		/*
		 * TODO : Buttons for the following
		 * 1. Add tab to create/modify Process
		 * 2. Tab to fill post transmittal values
		 * 3. Tab to fill Src - Dest field mapping
		 * 4. Tab to fill process params
		 * 5. Tab to configure circulation list
		 * 6. Tab to configure attachment list
		 */
		
		this.add(trnAdminContainer);
		
		layout.setActiveItem(trnAdminContainer);
	}
	
//	private void setStyle(LayoutContainer lc){
//		lc.setStyleAttribute("background","#F6BC5D");
//		lc.setStyleAttribute("padding","2px");
//		lc.setStyleAttribute("margin","5px");
//		lc.setStyleAttribute("cursor","pointer; cursor:hand");
//		lc.setStyleAttribute("color","white");
//		lc.setStyleAttribute("font-weight","bold");
//		lc.sinkEvents(Event.ONCLICK);
//	}
}
