package com.nattubaba.learn.jdo.test;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.nattubaba.learn.jdo.entities.Country;
import com.nattubaba.learn.jdo.utils.JdoUtils;

public class TestNullDetach 
{
	public static void main(String[] args) 
	{
		PersistenceManagerFactory pmf = JdoUtils.getPMF();
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try
		{
			tx.begin();
			Query q = pm.newQuery(Country.class, "name == givenName");
			q.declareParameters("String givenName");
			q.setUnique(true);
			Country aus = (Country) q.execute("Aus");
			// detaching null does not cause any problem
			aus = pm.detachCopy(aus); 
			System.out.println("aus = " + aus);
			q.closeAll();
			tx.commit();
		}
		finally
		{
			if( tx.isActive())
				tx.rollback();
			
			pm.close();
		}
	}
}
