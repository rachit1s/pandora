package commons.com.tbitsGlobal.utils.client.Events;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

/**
 * This event is fired to display the tagged requests in the tags tab.
 * The event is fired when the fetching of the requests is completed.
 * <br>
 * The requests to be displayed can be fetched by the <b>getRequests</b> method
 * that returns a list of TbitsTreeRequestData data model.
 * 
 * @author karan
 *
 */

public class ShowTaggedRequests extends TbitsBaseEvent {
	
	private List<TbitsTreeRequestData> requests;
	
	public List<TbitsTreeRequestData> getRequests(){
		return requests;
	}
	
	public ShowTaggedRequests(List<TbitsTreeRequestData> requests){
		this.requests = requests;
	}

}
