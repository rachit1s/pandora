package logistics.com.tbitsGlobal.client;

import com.google.gwt.core.client.GWT;

public interface LogisticsConstants {
	public final LogisticsServiceAsync logisticsService = GWT.create(LogisticsService.class); 
	
	public final int PAGE_SIZE = 10;
	
	public final int SEARCH_PAGE_SIZE = 50;
}
