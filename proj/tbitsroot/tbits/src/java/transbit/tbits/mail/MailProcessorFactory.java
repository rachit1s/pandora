package transbit.tbits.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.plugin.PluginManager;

/**
 * 
 * @author nitiraj
 *
 */

/**
 * This class is a factory class that can executed MailPlugins called IMailPreProcessor for you.
 */
public class MailProcessorFactory 
{
	private static final String PKG_NAME = "transbit.tbits.mail";
	private static ArrayList<IMailPreProcessor> preProcessors = new ArrayList<IMailPreProcessor>() ;
	public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_NAME);
	private static MailProcessorFactory instance = null ;
	private static ArrayList<IMailPreProcessor> defaultProcessors = new ArrayList<IMailPreProcessor>();
	
	private MailProcessorFactory() 
	{	
		preProcessors = new ArrayList<IMailPreProcessor>() ;
		loadPlugins();
	}
	private void loadPlugins( Class c, ArrayList plugins )
	{
		ArrayList<Class> classes = PluginManager.getInstance().findPluginsByInterface(c.getName()) ;
		for( Class klass : classes )
		{
			Object o;
			try {
				o = klass.newInstance();
				plugins.add(o) ;
			} catch (InstantiationException e) {				
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}				
		}
	}
	
	public void refreshFactoryInit()
	{
		preProcessors = new ArrayList<IMailPreProcessor>();
		loadPlugins();
	}
	
	private void loadPlugins() 
	{
		
		loadPlugins( IMailPreProcessor.class, preProcessors ) ;
		defaultProcessors.add(new AllowAnonymousDownload());
		defaultProcessors.add(new DefaultAttachmentLinkProcessor());
		Comparator<IMailPreProcessor> comp = new Comparator<IMailPreProcessor>()
		{
			public int compare(IMailPreProcessor arg0, IMailPreProcessor arg1) {
				double d = arg0.getMailPreProcessorOrder()- arg1.getMailPreProcessorOrder();
				if( 0 == d )
				{
					return 0 ;
				}
				else if ( d < 0 )
				{
					return -1 ;
				}				
				
				return 1 ;
			}
		};
		// sort the arrayList in the start only so that we don't have to sort them
		// every time we call them ..
		Collections.sort(preProcessors, comp);
		Collections.sort(defaultProcessors,comp);
	}
	
	public static synchronized MailProcessorFactory getInstance()
	{
		if( null == instance )
		{
			instance = new MailProcessorFactory() ;
		}
		
		return instance ;
	}
	
	//Hashtable<Integer,Collection<ActionFileInfo>> clonedActionFileHash = cloneActionFileHash(actionFileHash);
	//ArrayList<Action> clonedActionList = cloneActionList(myActionList);
	public void executeMailPreProcessors(User user, Request request,ArrayList<Action> actionList, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash, Hashtable<String,Integer>permissions )
	{
		for(IMailPreProcessor impp : defaultProcessors)
		{
			try
			{
				LOG.info("Executing the MailPreProcessor with name : " + impp.getMailPreProcessorName() );
				impp.executeMailPreProcessor(user, request,actionList,actionFileHash, permissions);
			}
			catch(Exception e)
			{
				LOG.error("",(e));
				LOG.info("Exception while executing the MailPreProcessor : " + impp.getMailPreProcessorName() + " : for the request : " + request + " : for the user : " + user.getUserLogin());
			}
		}
		
		for(IMailPreProcessor impp : preProcessors)
		{
			try
			{
				LOG.info("Executing the MailPreProcessor with name : " + impp.getMailPreProcessorName() );
				impp.executeMailPreProcessor(user, request,actionList,actionFileHash, permissions);
			}
			catch(Exception e)
			{
				LOG.error("",(e));
				LOG.info("Exception while executing the MailPreProcessor : " + impp.getMailPreProcessorName() + " : for the request : " + request + " : for the user : " + user.getUserLogin());
			}
		}
	}
}
