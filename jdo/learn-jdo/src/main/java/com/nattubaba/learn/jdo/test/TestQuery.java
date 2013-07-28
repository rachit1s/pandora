package com.nattubaba.learn.jdo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.apache.log4j.Logger;

import com.nattubaba.learn.jdo.entities.Country;
import com.nattubaba.learn.jdo.utils.JdoUtils;

public class TestQuery 
{
	private static final Logger logger = Logger.getLogger(TestQuery.class);
	
	public static void main(String argv[])
	{
		PersistenceManagerFactory pmf = JdoUtils.getPMF();
		{
			PersistenceManager pm = pmf.getPersistenceManager();
			
			Country one = new Country("one", "1");
			Country two = new Country("two", "2");
			try
			{
//				pm.currentTransaction().begin();
//				pm.makePersistent(one);
//				pm.makePersistent(two);
//				pm.currentTransaction().commit();
			}
			finally
			{
				if(pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();
				pm.close();
			}
		}
		
		{
			PersistenceManager pm = pmf.getPersistenceManager();
			try
			{
				pm.currentTransaction().begin();
				Query query = pm.newQuery(Country.class);
				List<Country> countries = (List<Country>) query.execute();
				List<Country> myCountries = new ArrayList<Country>(countries);
				query.closeAll();
				for( Country country : countries ) // becomes empty after query close
				{
					logger.info("country : " + country);
				}
				for( Country country : myCountries ) // contains values
				{
					logger.info("my-country : " + country);
				}
				pm.currentTransaction().commit();
			}
			finally
			{
				if( pm.currentTransaction().isActive() )
					pm.currentTransaction().rollback();
				
				pm.close();
			}
		}
		
		{
			PersistenceManager pm = pmf.getPersistenceManager();
			try
			{
				pm.currentTransaction().begin();
				Query query = pm.newQuery(Country.class,"name == cName");
				query.declareParameters("String cName");
				query.setUnique(true);
				Country india = (Country)query.execute("India"); // returns unique so no error.
				query.closeAll();
				logger.info("India-Country : " + india);
				pm.currentTransaction().commit();
			}
			finally
			{
				if( pm.currentTransaction().isActive() )
					pm.currentTransaction().rollback();
				
				pm.close();
			}
		}
		
		{
			try
			{
				PersistenceManager pm = pmf.getPersistenceManager();
				try
				{
					pm.currentTransaction().begin();
					Query query = pm.newQuery(Country.class,"name == cName");
					query.declareParameters("String cName");
					query.setUnique(true);
					Country india = (Country)query.execute("one"); // returns more than one so error
					query.closeAll();
					logger.info("one-Country : " + india);
					pm.currentTransaction().commit();
				}
				finally
				{
					if( pm.currentTransaction().isActive() )
						pm.currentTransaction().rollback();
					
					pm.close();
				}
			}
			catch(Exception e)
			{
				logger.error(e);
			}
		}
		
		{
			try
			{
				PersistenceManager pm = pmf.getPersistenceManager();
				try
				{
					pm.currentTransaction().begin();
					Query query = pm.newQuery(Country.class,"name == cName");
					query.declareParameters("String cName");
					query.setUnique(true);
					Country abc = (Country)query.execute("abc"); // does not exists. so returns null
					query.closeAll();
					logger.info("abc-Country : " + abc);
					pm.currentTransaction().commit();
				}
				finally
				{
					if( pm.currentTransaction().isActive() )
						pm.currentTransaction().rollback();
					
					pm.close();
				}
			}
			catch(Exception e)
			{
				logger.error(e);
			}
		}

		{
			try
			{
				PersistenceManager pm = pmf.getPersistenceManager();
				try
				{
					pm.currentTransaction().begin();
					Query query = pm.newQuery(Country.class,"name == cName");
					query.declareParameters("String cName");
					Collection cols = (Collection) query.execute("sdf"); // test if it returns null for no result
					if( null == cols )
						logger.info("returns null for no result.");
					else logger.info("size of collectoin : " + cols.size());
					query.closeAll();
//					logger.info("abc-Country : " + abc);
					pm.currentTransaction().commit();
				}
				finally
				{
					if( pm.currentTransaction().isActive() )
						pm.currentTransaction().rollback();
					
					pm.close();
				}
			}
			catch(Exception e)
			{
				logger.error(e);
			}
		}
		
		{
			try
			{
				PersistenceManager pm = pmf.getPersistenceManager();
				try
				{
					pm.currentTransaction().begin();
					Query query = pm.newQuery(Country.class);
					Collection cols = (Collection) query.execute(); // test if it returns null for no result
					if( null == cols )
						logger.info("returns null for no result.");
					else logger.info("size of collectoin : " + cols.size());
					query.closeAll();
//					logger.info("abc-Country : " + abc);
					pm.currentTransaction().commit();
				}
				finally
				{
					if( pm.currentTransaction().isActive() )
						pm.currentTransaction().rollback();
					
					pm.close();
				}
			}
			catch(Exception e)
			{
				logger.error(e);
			}
		}
	}
}
