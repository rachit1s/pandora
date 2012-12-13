package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DQL implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int SORTDIR_DESC = 0;
	public static int SORTDIR_ASC = 0;
	
	public DQL(String dql2) {
		this();
		this.dql = dql2;
	}
	public  DQL() {
		sortOrder = new HashMap<String, Integer>();
	}
	public String dql;
	
	public Map<String, Integer> sortOrder;
}
