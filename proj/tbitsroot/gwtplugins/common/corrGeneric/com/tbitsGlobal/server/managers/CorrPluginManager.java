package corrGeneric.com.tbitsGlobal.server.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.plugin.PluginManager;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrManager;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportIdPlugin;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CorrPluginManager implements ICorrManager
{
	private ArrayList<ICorrConstraintPlugin> constraintsPlugins = new ArrayList<ICorrConstraintPlugin>();
	private ArrayList<IReportIdPlugin> reportIdPlugins = new ArrayList<IReportIdPlugin>();
	private ArrayList<IReportParamPlugin> reportParamPlugins = new ArrayList<IReportParamPlugin>();
	
	private CorrPluginManager() throws CorrException
	{
		loadPlugins();
	}
	
	private static CorrPluginManager instance;
	public synchronized static CorrPluginManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new CorrPluginManager();
		
		return instance;
	}
	private void loadPlugins() throws CorrException 
	{
		constraintsPlugins = new ArrayList<ICorrConstraintPlugin>();
		reportIdPlugins = new ArrayList<IReportIdPlugin>();
		reportParamPlugins = new ArrayList<IReportParamPlugin>();
		
		ArrayList<Class> plugs = PluginManager.getInstance().findPluginsByInterface(ICorrConstraintPlugin.class.getName());
		if( null != plugs )
		{
			for( Class klass : plugs )
			{
				Object obj;
				try {
					obj = klass.newInstance();
				} catch (Exception e) {
					Utility.LOG.info(TBitsLogger.getStackTrace(e));
					throw new CorrException("The CorrPluginManager could not initialized properly.");

				} 
				
				if( null != obj && (obj instanceof ICorrConstraintPlugin) )
				{
					ICorrConstraintPlugin icoco = (ICorrConstraintPlugin) obj;
					constraintsPlugins.add(icoco);
				}
			}
			
			Collections.sort(constraintsPlugins, new Comparator<ICorrConstraintPlugin>(){

				public int compare(ICorrConstraintPlugin co, ICorrConstraintPlugin co1) 
				{
					if( co.getOrder() < co1.getOrder() )
						return -1 ;
					else if( co.getOrder() > co1.getOrder() )
						return 1 ;
						
					return 0;
				}				
			});
		}
			
			ArrayList<Class> repIdplugs = PluginManager.getInstance().findPluginsByInterface(IReportIdPlugin.class.getName());
			if( null != repIdplugs )
			{
				for( Class klass : repIdplugs )
				{
					Object obj;
					try {
						obj = klass.newInstance();
					} catch (Exception e) {
						Utility.LOG.info(TBitsLogger.getStackTrace(e));
						throw new CorrException("The CorrPluginManager could not initialized properly.");
					} 
					
					if( null != obj && (obj instanceof IReportIdPlugin) )
					{
						IReportIdPlugin icoco = (IReportIdPlugin) obj;
						reportIdPlugins.add(icoco);
					}
				}			
				
				Collections.sort(reportIdPlugins, new Comparator<IReportIdPlugin>(){

					public int compare(IReportIdPlugin co, IReportIdPlugin co1) 
					{
						if( co.getOrder() < co1.getOrder() )
							return -1 ;
						else if( co.getOrder() > co1.getOrder() )
							return 1 ;
							
						return 0;
					}				
				});
			}
			
			ArrayList<Class> repParamPlugins = PluginManager.getInstance().findPluginsByInterface(IReportParamPlugin.class.getName());
			if( null != repParamPlugins )
			{
				for( Class klass : repParamPlugins )
				{
					Object obj;
					try {
						obj = klass.newInstance();
					} catch (Exception e) {
						Utility.LOG.info(TBitsLogger.getStackTrace(e));
						throw new CorrException("The CorrPluginManager could not initialized properly.");
					} 
					
					if( null != obj  )
					{
						IReportParamPlugin icoco = (IReportParamPlugin) obj;
						reportParamPlugins.add(icoco);
					}
				}			
			}
	}
	
	
	public Integer executeReportIdPlugins( Hashtable<String,Object> params ) throws CorrException
	{
		Integer retValue = null;
		for( IReportIdPlugin plugin : reportIdPlugins )
		{
			Integer rv = plugin.getReportId(params);
			if( null != rv )
				retValue = rv;
		}
		
		return retValue;
	}
	
	public String executeReportParamPlugin( Hashtable<String,Object> params, String className ) throws ClassNotFoundException, CorrException
	{
		String retValue = null ;
		IReportParamPlugin myPlugin = null ;
		for( IReportParamPlugin plugin : reportParamPlugins )
		{
			if( plugin.getClass().getName().equals(className) )
			{
				myPlugin = plugin;
				break;
			}
		}
		
		if( null == myPlugin )
			throw new ClassNotFoundException("The plugin with class name : " + className + " not found.");
		
		retValue = myPlugin.getReportParam(params);
		
		return retValue;
	}
	
	public void executeConstraints(Hashtable<String,Object> params) throws CorrException
	{
		for( ICorrConstraintPlugin plugin : constraintsPlugins )
		{
			Utility.LOG.info("Executing ConstraintPlugin : PluginClass=" + plugin.getClass().getName() + " : PluginName : " + plugin.getName());
			plugin.execute(params);
		}
	}
	public void refresh() 
	{
		// actually here I will have to load the cache again instead of clearing them
		try {
			loadPlugins();
		} catch (CorrException e) {
			e.printStackTrace();
		}
	}
}
