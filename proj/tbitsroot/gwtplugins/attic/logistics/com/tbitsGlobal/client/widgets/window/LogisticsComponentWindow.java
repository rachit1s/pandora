package logistics.com.tbitsGlobal.client.widgets.window;

import logistics.com.tbitsGlobal.client.LogisticsUtils;
import logistics.com.tbitsGlobal.client.Stage;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.search.BasicSearchContainer;
import commons.com.tbitsGlobal.utils.client.search.SearchWindow;

public class LogisticsComponentWindow extends SearchWindow{
	
	private Stage stage;
	
	private BasicSearchContainer searchContainer;
	
	public LogisticsComponentWindow(Stage stage) {
		super();
		
		this.stage = stage;
		
		this.setHeading(LogisticsUtils.getAddHeadingString(this.stage.getParams()) + " - [" + this.stage.getPreSysPrefix() + "]");
	}
	
	protected void createSearchContainer(){
		if(this.stage.getPreSysPrefix() == null || this.stage.getPreSysPrefix().trim().equals("")){
			TbitsInfo.error("Business Area for previous stage not configured. Can not proceed.");
		}else{
			this.searchContainer = new BasicSearchContainer(this.stage.getPreSysPrefix());
			this.add(searchContainer, new FitData());
		}
	}
}
