package com.tbitsGlobal.admin.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.plugins.IPagePlugin;
import com.tbitsGlobal.admin.client.plugins.slots.PagePluginsSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

/**
 * Menu on the left side of the admin panel. This will hold the links to main admin panel
 * and the links to plugins registered for the admin panel.
 *
 */
public class APLeftMenu extends ContentPanel {

	private BAPageContainer baFieldsContainer;
	private UniversalPageContainer universalFieldsContainer;

	public APLeftMenu() {
		super();
		this.setHeaderVisible(false);
		this.setLayout(new AccordionLayout());
	}

	/**
	 * Content Panel which contains the BA Dropdown.
	 * @return
	 */
	protected ContentPanel addBADropdown(){
		RowData rowData = new RowData();
		rowData.setWidth(1);
		
		BACombo comboWrapper = new BACombo();
		
		ContentPanel cp = new ContentPanel();
		cp.setBodyBorder(false);
		cp.setHeaderVisible(false);
		cp.setLayout(new RowLayout());
		
		cp.add(comboWrapper, rowData);
		
		return cp;
	}
	
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		ContentPanel topContainer = new ContentPanel(new BorderLayout());
		topContainer.setHeading("BA Specific");
		topContainer.setBodyBorder(false);
		baFieldsContainer = new BAPageContainer();
		
		/**
		 * FIXME: Note that the BA Selection Combo Box cannot be in the baFieldsContainer, because then it
		 * becomes the part of Row Layout and the vertical scroll bar which then appears, treats it like a row item,
		 * hence overriding it and showing on top of it. So it has to be added in a separate panel and then
		 * be attached on the top of the main container holding the menu items.
		 */
		topContainer.setTopComponent(addBADropdown());
		
		topContainer.add(baFieldsContainer, new BorderLayoutData(LayoutRegion.NORTH));
		
		universalFieldsContainer = new UniversalPageContainer();
		topContainer.add(universalFieldsContainer, new BorderLayoutData(LayoutRegion.CENTER));
		
		this.add(topContainer);
		
		List<IPagePlugin> plugins = GWTPluginRegister.getInstance().getPlugins(PagePluginsSlot.class, IPagePlugin.class);
		
		if(plugins != null){
			for(IPagePlugin plugin : plugins){
				AbstractPagePluginContainer pageLinkConatiner = plugin.getWidget(null);
				this.add(pageLinkConatiner);
			}
		}
	}
}