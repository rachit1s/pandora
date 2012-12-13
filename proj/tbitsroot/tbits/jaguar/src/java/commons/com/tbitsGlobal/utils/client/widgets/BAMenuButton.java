package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class BAMenuButton extends ToolBarButton{

	protected Menu baMenu;
	
	public BAMenuButton() {
		super("Business Area");
		
		baMenu = new Menu(){
			@Override
			protected void beforeRender() {
				super.beforeRender();
				
				showSeparator = false;
			}
		};
		this.setMenu(baMenu);
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.initializeBAs();
	}
	
	private void initializeBAs(){
		GlobalConstants.utilService.getBAMenu(new AsyncCallback<BAMenuClient>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while initializing Business Areas...", caught);
				Log.error("Error while initializing Business Areas...", caught);
			}

			public void onSuccess(BAMenuClient menu) {
				createMenu(baMenu, menu);
			}
		});
	}
	
	private void createMenu(Menu menu, BAMenuClient menuClient){
		ArrayList<BAMenuClient> menus = menuClient.getSubMenu(); // Submenus for a BA Menu
		if(menus != null){
			for(BAMenuClient menuClientChild : menus){
				MenuItem itemChild = getMenuItem(menuClientChild); // Gets MenuItem for for each submenu recursively
				menu.add(itemChild); // Adds it to the menu
			}
			
		}
		
		ArrayList<BusinessAreaClient> baList = menuClient.getBaList(); // List of actual Business Areas at this menu
		if(baList != null){
			for(final BusinessAreaClient baClient : baList){
				MenuItem itemChild = new MenuItem(baClient.getDisplayText()
						, new SelectionListener<MenuEvent>(){
					@Override
					public void componentSelected(MenuEvent ce) {
						DelayedTask task = new DelayedTask(new Listener<BaseEvent>(){
							public void handleEvent(BaseEvent be) {
								onSelect(baClient);
							}});
						task.delay(10);
					}});
				menu.add(itemChild);
			}
		}
	}
	
	public void onSelect(BusinessAreaClient baClient){
		
	}
	
	/**
	 * @param menuClient
	 * @return. A {@link MenuItem} corresponding to a {@link BAMenuClient}
	 */
	private MenuItem getMenuItem(BAMenuClient menuClient){
		MenuItem item = new MenuItem(menuClient.getMenuCaption());
		
		Menu menu = new Menu(){
			@Override
			protected void beforeRender() {
				super.beforeRender();
				
				showSeparator = false;
			}
		};
		
		createMenu(menu, menuClient);
		
		item.setSubMenu(menu); // Adds submenu to the Menu Item
		
		return item;
	}
}
