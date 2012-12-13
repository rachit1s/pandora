package commons.com.tbitsGlobal.utils.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


/**
 * 
 * @author sourabh
 * 
 * Carries the results of a dql serach
 */
public class DQLResults implements Serializable{
	
	private int totalRecords;
	private List<TbitsTreeRequestData> requests;
	private String orderByColumnName;
	int sortDirection = 0;
	
	public DQLResults() {
		super();
	}

	public DQLResults(List<TbitsTreeRequestData> requests,
			int totalRecords) {
		super();
		this.requests = requests;
		this.totalRecords = totalRecords;
	}

	/**
	 * @return The total number of records present in db corresponding to the dql
	 */
	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	/**
	 * @return A Map of request ids and Requests
	 */
	public List<TbitsTreeRequestData> getRequests() {
		return requests;
	}

	public void setRequests(List<TbitsTreeRequestData> requests) {
		this.requests = requests;
	}

	public void setSortDirection(int sortDir) {
		sortDirection = sortDir; 
	}

	public void setSortColumn(String orderByColumnNames) {
		orderByColumnName = orderByColumnNames;
	}

	public int getSortDirection() {
		return sortDirection;
	}

	public String getSortColumn() {
		return orderByColumnName;
	}

}
