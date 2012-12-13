package admin.com.tbitsglobal.admin.client;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import commons.com.tbitsGlobal.utils.client.TbitsUncaughtExceptionHandler;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Administration implements EntryPoint, AdminConstants {
	
	private MainTabPanel tabPanel;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new TbitsUncaughtExceptionHandler());
		((ServiceDefTarget) dbService).setServiceEntryPoint("/trnadmin/db");

		Viewport viewPort = new Viewport();
//		viewPort.setLayout(new BorderLayout());
		viewPort.setLayout(new FitLayout());
		
		tabPanel = new MainTabPanel();
		
//		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
//		westData.setSplit(true);
//		SidePanel sidePanel = new SidePanel(tabPanel);
//		viewPort.add(sidePanel, westData);
//		
//		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
//		centerData.setMargins(new Margins(0,0,0,5));
		
//		viewPort.add(tabPanel, centerData);
		
		viewPort.add(tabPanel, new FitData());
		RootPanel.get().add(viewPort);
	}
}
