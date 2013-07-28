/**
 * 
 */
package com.tbitsglobal.ddc.test;

import java.util.ArrayList;

import com.tbitsglobal.ddc.dao.KeywordSetDao;
import com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import com.tbitsglobal.ddc.domain.KeywordSet;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * 
 */
public class KeywordSetDaoTest {
	public static void main(String[] args) {
		try {
			KeywordSet ks = testKeywordSetInsert();
			System.out.println("Added KS : " + ks);
		} catch (FailedToInsert e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return 
	 * @throws FailedToInsert 
	 * 
	 */
	private static KeywordSet testKeywordSetInsert() throws FailedToInsert {
		KeywordSet ks = new KeywordSet();
		ArrayList<String> k = new ArrayList<String>();
		k.add("FMG Document Number ");
		k.add("IMP Document Number ");
		k.add("Scanmin Document Number");
		ks.setKeyWords(k);
		
		return KeywordSetDao.getInstance().insert(ks);
	}
}
