package com.nattubaba.learn.jdo.test;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.nattubaba.learn.jdo.entities.City;
import com.nattubaba.learn.jdo.entities.Country;
import com.nattubaba.learn.jdo.utils.JdoUtils;

public class Test1NRelation {

	private static PersistenceManagerFactory pmf = null;
	public static void initialize()
	{
		pmf = JdoUtils.getPMF();
	}
	
	public static void dispose()
	{
		pmf.close();
	}
	
	public static void main(String[] args) {
		initialize();
				
		insertCountryCities();
		
		dispose();
	}

	private static void insertCountryCities() 
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		
		Country c1 = new Country("C1", "c1");
		Country c2 = new Country("C2", "c2");
		
		City city1 = new City("City1", "city1"); 
		City city2 = new City("City2", "city2");
		City city3 = new City("City3", "city3");
		City city4 = new City("City4", "city4");

		c1.getCities().add(city1);
		c1.getCities().add(city2);
		c1.getCities().add(city3);
		
		c2.getCities().add(city4);
		
		pm.makePersistent(c1);
		pm.makePersistent(c2);
		
		pm.close();
	}
}
