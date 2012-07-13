package com.nattubaba.learn.jdo.test;

import java.util.HashMap;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;

import com.nattubaba.learn.jdo.entities.Country;
import com.nattubaba.learn.jdo.utils.JdoUtils;

public class TestProperties {

	private static final String NO_OF_UT = "No. of UT";
	private static final String NO_OF_STATE = "No. of State";
	
	private static final String NO_OF_UTs = "No. of UTs";
	private static final String NO_OF_STATES = "No. of States";
	
	private static final String POPULATION = "population";
	
	private static final Logger logger = Logger.getLogger(TestProperties.class);
	
	public static void create()
	{
		PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
		
		Country india = new Country("India", "ind");
		HashMap<String,String> props = new HashMap<String,String>();
		props.put(NO_OF_STATE, "28");
		props.put(NO_OF_UT, "7");
		india.setProperties(props);
		
		india = pm.makePersistent(india);

		logger.info("india = " + india);
		logger.info("India-Props : " + india.getProperties());

		india.getProperties().remove(NO_OF_UT);
		india.getProperties().put(NO_OF_UTs, "7");
		india.getProperties().put(POPULATION, "1200000000");
		
		india = pm.makePersistent(india);
		
		logger.info("india = " + india);
		logger.info("India-Props : " + india.getProperties());
		pm.close();
	}
	
	public static void fetchUpdate()
	{
		PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
		Query query = pm.newQuery(Country.class, "name == givenName");
		query.declareParameters("String givenName");
		query.setUnique(true);
		Country india = (Country) query.execute("India");
		
		logger.info("india : " + india);
		logger.info("India-Props : " + india.getProperties());
		
		HashMap<String, String> props = new HashMap<String,String>();
		props.put("Girls", "500000000");
		props.put("Boys", "700000000");
		
		india.setProperties(props);
		
		india = pm.makePersistent(india);
		
		logger.info("india : " + india);
		logger.info("India-Props : " + india.getProperties());
		
		pm.close();
	}
	public static void main(String[] args) {
		fetchUpdate();
		
	}
}
