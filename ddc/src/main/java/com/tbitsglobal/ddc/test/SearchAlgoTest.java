/**
 * 
 */
package com.tbitsglobal.ddc.test;

import java.util.List;

import com.tbitsglobal.ddc.dao.SearchAlgoDao;
import com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import com.tbitsglobal.ddc.domain.SearchAlgo;
import com.tbitsglobal.ddc.exception.FailedToFindObject;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * 
 */
public class SearchAlgoTest {
	public static void main(String[] args) {
		/*try {
			SearchAlgo sa = testSearchAlgoInsert();
			System.out.println("Added SA : " + sa);
			
		} catch (FailedToInsert e) {
			e.printStackTrace();
		}*/
		
		try {
			List<SearchAlgo> sas = SearchAlgoDao.getInstance().getAll();
			System.out.println("sas : " + sas);
		} catch (FailedToFindObject e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws FailedToInsert 
	 * 
	 */
	private static SearchAlgo testSearchAlgoInsert() throws FailedToInsert {
		SearchAlgo sa = new SearchAlgo(-1, SearchAlgo.SearchType_After, SearchAlgo.SearchWhat_Exact, null, "FMG Document Number", null);
		return SearchAlgoDao.getInstance().insert(sa);
	}
}
