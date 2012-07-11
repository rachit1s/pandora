package com.nattubaba.learn.jdo.utils;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.log4j.Logger;

public class JdoUtils 
{
	public static final Logger logger = Logger.getLogger("learnjdo");
	public static PersistenceManagerFactory getPMF()
	{
		PersistenceManagerFactory pmf = null;
		if( null == pmf ) 
			pmf = JDOHelper.getPersistenceManagerFactory(JdoUtils.class.getClassLoader().getResourceAsStream("jdo.properties"));
		
		return pmf;
	}

}
