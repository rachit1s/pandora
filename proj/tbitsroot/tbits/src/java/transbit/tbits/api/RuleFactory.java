package transbit.tbits.api;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.rules.RulesManager;

public class RuleFactory {
	// The Logger.
	private static final String PKG_NAME = "transbit.tbits.api";
	public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_NAME);

	private RuleFactory() {
		LoadRules();
	}
	
	private void refreshFactoryInt()
	{
		preRules = new ArrayList<IRule>();
		postRules = new ArrayList<IPostRule>();
		LoadRules();
	}
	public static void refreshFactory()
	{
		RuleFactory rf = new RuleFactory();
		rf.refreshFactoryInt();
	}
	ArrayList<IRule> preRules = new ArrayList<IRule>();
	ArrayList<IPostRule> postRules = new ArrayList<IPostRule>();
	
	private void LoadRules() {

		ArrayList<Class> ruleClasses = PluginManager.getInstance()
				.findPluginsByInterface(IRule.class.getName());
		for(Class c: PluginManager.getInstance()
				.findPluginsByInterface(IPostRule.class.getName()))
		{
			ruleClasses.add(c);
		}
		for (Class c : ruleClasses) {
			try {
				Object o = c.newInstance();
				if(o instanceof IRule)
				{
					IRule rule = (IRule) o;
					preRules.add(rule);
				}
				else if(o instanceof IPostRule)
				{
					LOG.debug("Adding ipost rule....");
					IPostRule rule = (IPostRule) o;
					postRules.add(rule);
					LOG.debug("Added ipost rule....");
				}
			} catch (InstantiationException e) {
				LOG.error(e);
			} catch (IllegalAccessException e) {
				LOG.error(e);
			}
		}
		
	}

	private static RuleFactory instance = null;

	public static RuleFactory getInstance() {
		if (instance == null)
			instance = new RuleFactory();
		return instance;
	}

	public ArrayList<IRule> getPreRules() {
		return preRules;
	}

	public ArrayList<IPostRule> getPostRules() {
		return postRules;
	}

	public static void runPreRules(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			 boolean isAddRequest) throws TBitsException {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			runPreRules(connection, ba, oldRequest, currentRequest, Source, user, isAddRequest);
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException sqle1) {
				sqle1.printStackTrace();
			}
			throw new TBitsException("Error while running pre process rules", sqle);
		}
		catch(TBitsException te)
		{
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException sqle1) {
				sqle1.printStackTrace();
			}
			throw te;
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}
			connection = null;
		}
	}
	public static void runPreRules(Connection conn, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			boolean isAddRequest) throws TBitsException {
		try {
			RuleFactory factory = RuleFactory.getInstance();
			ArrayList<IRule> allPreRules = factory.getPreRules();
			Comparator<IRule> c = new Comparator<IRule>(){

				public int compare(IRule arg0, IRule arg1) {
					return (int) (arg0.getSequence() - arg1.getSequence());
				}
				
			};
			Collections.sort(allPreRules, c);
			for (IRule rule : allPreRules) 
			{
				RuleResult result = null ;
				try
				{
					LOG.info("Executing '" + rule.getName() + "'");
					result = rule.execute(conn, ba, oldRequest,
							currentRequest, Source, user, isAddRequest);
				}
				catch( Throwable e )
				{
					e.printStackTrace() ;
					LOG.fatal("Exception while executing the rule : " + rule.getName() + "\nException trace is : " + "",(e) ) ;
					continue ;
				}
				if( null == result )
				{
					LOG.fatal("The rule with name : " + rule.getName() + " returned a null instead of RuleResult.") ;
					continue ;
				}
				
				if (!result.isSuccessful()) {
					LOG.warn("Rule '" + rule.getName() + "' failed.");
					LOG.warn("Message: " + result.getMessage());
				}
				if (!result.canContinue()) {
					throw new TBitsException(result.getMessage());
				}
			}
			
		}
		catch( TBitsException te )
		{
			throw te ;
		}
		catch (Exception e) {
			LOG.error("Exception in rule execution", e);
		}
		
		// Run the db rules
		try{
			RulesManager manager = RulesManager.getInstance();
			ArrayList<Class<?>> preRules = manager.getRulesImplementing(IRule.class);
			for(Class<?> rule : preRules){
				String ruleName = rule.getName();
				Method m = rule.getMethod("getName", null);
				if(m != null){
					ruleName = (String) m.invoke(rule.newInstance(), null);
					LOG.info("Executing '" + ruleName + "'");
				}
				
				Method exec = rule.getMethod("execute", new Class[]{java.sql.Connection.class, transbit.tbits.domain.BusinessArea.class, 
										transbit.tbits.domain.Request.class, transbit.tbits.domain.Request.class, Integer.TYPE,
										transbit.tbits.domain.User.class, Boolean.TYPE});
				
				RuleResult result = (RuleResult) exec.invoke(rule.newInstance(), new Object[]{conn, ba, oldRequest, currentRequest, Source, user, isAddRequest});
				
				if( null == result )
				{
					LOG.fatal("The rule with name : " + ruleName + " returned a null instead of RuleResult.") ;
					continue ;
				}
				LOG.info("RULE RESULT:" + result.getMessage());
				System.out.println("RULE RESULT:" + result.getMessage());
				if (!result.isSuccessful()) {
					LOG.warn("Rule '" + ruleName + "' failed.");
					LOG.warn("Message: " + result.getMessage());
					System.out.println("Rule '" + ruleName + "' failed.");
					System.out.println("Message: " + result.getMessage());
				}
				if (!result.canContinue()) {
					throw new TBitsException(result.getMessage());
				}
			}
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void runPostRules(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			boolean isAddRequest) throws TBitsException {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			runPostRules(connection, ba, oldRequest, currentRequest, Source, user, isAddRequest);
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException sqle1) {
				sqle1.printStackTrace();
			}
			throw new TBitsException("Error while running post process rules", sqle);
		}
		catch(TBitsException te)
		{
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException sqle1) {
				sqle1.printStackTrace();
			}
			throw te;
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}
			connection = null;
		}
	}
	
	public static void runPostRules(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			boolean isAddRequest) throws TBitsException {
		try {
			RuleFactory factory = RuleFactory.getInstance();
			ArrayList<IPostRule> allRules = factory.getPostRules();
			Comparator<IPostRule> c = new Comparator<IPostRule>(){

				public int compare(IPostRule arg0, IPostRule arg1) {
					return (int) (arg0.getSequence() - arg1.getSequence());
				}
			};
			Collections.sort(allRules, c);
			LOG.info("Total number of post process rules: " + allRules.size());
			for (IPostRule rule : allRules) 
			{
				RuleResult result = null ;
				try
				{
					LOG.info("Runnning the rule: " + rule.getName());
					result = rule.execute(connection, ba, oldRequest,
							currentRequest, Source, user, isAddRequest);
				}
				catch( Exception e)
				{
					e.printStackTrace() ;
					LOG.fatal("Exception while executing the rule : " + rule.getName() + "\nException trace is : " + "",(e) ) ;
					continue ;
				}
				if( null == result )
				{
					LOG.fatal("The rule with name : " + rule.getName() + " returned a null instead of RuleResult.") ;
					continue ;
				}
				LOG.info("RULE RESULT:" + result.getMessage());
				if (!result.isSuccessful()) {
					LOG.warn("Rule '" + rule.getName() + "' failed.");
					LOG.warn("Message: " + result.getMessage());
				}
				if (!result.canContinue()) {
					throw new TBitsException(result.getMessage());
				}
				
			}
		}
		catch (TBitsException te)
		{
			throw te ;
		}
		catch (Exception e) {
			LOG.error("Exception in rule execution", e);
		}
		
		// Run the db rules
		try{
			RulesManager manager = RulesManager.getInstance();
			ArrayList<Class<?>> postRules = manager.getRulesImplementing(IPostRule.class);
			for(Class<?> rule : postRules){
				String ruleName = rule.getName();
				Method m = rule.getMethod("getName", null);
				if(m != null){
					ruleName = (String) m.invoke(rule.newInstance(), null);
					LOG.info("Executing '" + ruleName + "'");
				}
				
				Method exec = rule.getMethod("execute", new Class[]{java.sql.Connection.class, transbit.tbits.domain.BusinessArea.class, 
						transbit.tbits.domain.Request.class, transbit.tbits.domain.Request.class, Integer.TYPE,
						transbit.tbits.domain.User.class, Boolean.TYPE});
				
				RuleResult result = (RuleResult) exec.invoke(rule.newInstance(), connection, ba, oldRequest, currentRequest, Source, user, isAddRequest);
				if( null == result )
				{
					LOG.fatal("The rule with name : " + ruleName + " returned a null instead of RuleResult.") ;
					continue ;
				}
				LOG.info("RULE RESULT:" + result.getMessage());
				System.out.println("RULE RESULT:" + result.getMessage());
				if (!result.isSuccessful()) {
					LOG.warn("Rule '" + ruleName + "' failed.");
					LOG.warn("Message: " + result.getMessage());
					System.out.println("Rule '" + ruleName + "' failed.");
					System.out.println("Message: " + result.getMessage());
				}
				if (!result.canContinue()) {
					throw new TBitsException(result.getMessage());
				}
			}
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
}
