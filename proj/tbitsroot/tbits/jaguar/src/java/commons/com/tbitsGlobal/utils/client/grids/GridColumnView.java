package commons.com.tbitsGlobal.utils.client.grids;

/**
 * 
 * @author sourabh
 * 
 * An enum to provide different configurations to different grids.
 * 
 * Currently it is being used for the Column Preferences
 */
public enum GridColumnView {	
	SearchGrid(1),
	AdvSearchGrid(2),
	MyRequestsGrid(3),
	BulkUpdateGrid(4);
	
	private int id;
	
	private GridColumnView(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
