package transbit.tbits.ExtUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.plugin.PluginManager;

/**
 * 
 * @author nitiraj
 *	
 *	Factory class to handle plugins for ExtUI component renderer plugins
 */
public class SlotFillerFactory 
{
	private static final String PKG_NAME = "transbit.tbits.ExtUI";
	
	public static final String ADD_REQEUST_FOOTER_SLOT = "addRequestFooterSlot" ;
	public static final String UPDATE_REQEUST_FOOTER_SLOT = "updateRequestFooterSlot" ;
	public static final String ACTION_DETAILS_HEADER_SLOT = "actionDetailsHeaderSlot" ;
	public static final String SEARCH_RESULTS_HEADER_SLOT = "searchResultsHeaderSlot" ;
	public static final String SUB_REQEUST_FOOTER_SLOT = "addRequestFooterSlot" ; // the add-request page is same as add sub-request page so using the same slot value 
	public static final String SEARCH_FOTTER_SLOT = "searchFooterSlot" ; 
	
	public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_NAME);
	
	private static SlotFillerFactory instance = null;
//	private static ArrayList<ISlotFiller> extUIRendererList = new ArrayList<ISlotFiller>();

	private ArrayList<IAddRequestFooterSlotFiller> addRequestFooterSlotFillerList;

	private ArrayList<IUpdateRequestFooterSlotFiller> updateRequestFooterSlotFillerList;

	private ArrayList<IActionDetailsHeaderSlotFiller> actionDetailsHeaderSlotFillerList;

	private ArrayList<ISearchFooterSlotFiller> searchFooterSlotFillerList;

	private ArrayList<ISearchResultsHeaderSlotFiller> searchResultsHeaderSlotFillerList;
	
//	private ArrayList<ISlotFiller> allSlotFillerList ;

	private ArrayList<ISubRequestFooterSlotFiller> subRequestFooterSlotFillerList;
	
	private SlotFillerFactory() 
	{
//		allSlotFillerList = new ArrayList<ISlotFiller>() ;
		addRequestFooterSlotFillerList = new ArrayList<IAddRequestFooterSlotFiller>() ;
		updateRequestFooterSlotFillerList = new ArrayList<IUpdateRequestFooterSlotFiller>() ;
		actionDetailsHeaderSlotFillerList = new ArrayList<IActionDetailsHeaderSlotFiller>() ;
		searchFooterSlotFillerList = new ArrayList<ISearchFooterSlotFiller>() ;
		searchResultsHeaderSlotFillerList = new ArrayList<ISearchResultsHeaderSlotFiller>() ;
		subRequestFooterSlotFillerList = new ArrayList<ISubRequestFooterSlotFiller>() ;
		loadPlugins();
	}	
	
	public void refreshFactoryInit()
	{
	//	extUIRendererList = new ArrayList<ISlotFiller>();
//		allSlotFillerList = new ArrayList<ISlotFiller>() ;
		addRequestFooterSlotFillerList = new ArrayList<IAddRequestFooterSlotFiller>() ;
		updateRequestFooterSlotFillerList = new ArrayList<IUpdateRequestFooterSlotFiller>() ;
		actionDetailsHeaderSlotFillerList = new ArrayList<IActionDetailsHeaderSlotFiller>() ;
		searchFooterSlotFillerList = new ArrayList<ISearchFooterSlotFiller>() ;
		searchResultsHeaderSlotFillerList = new ArrayList<ISearchResultsHeaderSlotFiller>() ;
		subRequestFooterSlotFillerList = new ArrayList<ISubRequestFooterSlotFiller>() ;
		
		loadPlugins();
	}
	
	public static synchronized SlotFillerFactory getInstance() {
		if (instance == null)
			instance = new SlotFillerFactory();
		return instance;
	}
 
//	public ArrayList<ISlotFiller> getExtUIRendererList() {
//		return allSlotFillerList;
//	}

//	private void runExtUIRenderers(  HttpServletRequest request, HttpServletResponse response,
//			Hashtable tagTable, ArrayList<String> tagList,BusinessArea ba, int action ) throws TBitsException
//	{
//		for( ISlotFiller ieur : extUIRendererList )
//		{
//			ieur.process(request, response, tagTable, ba, action) ;
//		}
//	}
	
	public void runAddRequestSlotFillers( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, User user, Hashtable<String,Object> tagTable)
	{
		// get all the IAddRequestSlotFillers.
		// replace the tag in tagtable with its concatenated html 
		String html = "" ;		
		for( IAddRequestFooterSlotFiller sf : addRequestFooterSlotFillerList ) 
		{
			try
			{
				String filler = sf.getAddRequestFooterHtml(httpRequest, httpResponse, ba, user) ;
				if( null != filler && ! filler.trim().equals(""))
					html += "<td align='left'>" + filler + "</td>" ;
			}
			catch(Exception e)
			{
				LOG.severe("Exception while executing the slog filler plugin : " + sf);
				LOG.severe("",(e)) ;				
			}
		}
		
		tagTable.put(ADD_REQEUST_FOOTER_SLOT, html) ;
	}
	
	public void runSubRequestSlotFillers(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, Request parentRequest, User user,
			Hashtable<String, Object> tagTable) 
	{
		// get all the ISubRequestSlotFillers.
		// replace the tag in tagtable with its concatenated html 
		String html = "" ;		
		for( ISubRequestFooterSlotFiller sf : subRequestFooterSlotFillerList ) 
		{
			try
			{
				String filler = sf.getSubRequestFooterHtml(httpRequest, httpResponse, ba, parentRequest, user) ;
				if( null != filler && ! filler.trim().equals("") )
					html += " " + filler + " " ;
			}
			catch(Exception e)
			{
				LOG.severe("Exception while executing the slog filler plugin : " + sf);
				LOG.severe("",(e)) ;				
			}
			
		}
		tagTable.put(SUB_REQEUST_FOOTER_SLOT, html) ;	
	}	
	
	public void runUpdateRequestSlotFillers( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, Request oldRequest, User user, Hashtable<String,Object> tagTable)
	{
		// get all the IUpdateRequestSlotFillers.
		// replace the tag in tagtable with its concatenated html 
		String html = "" ;		
		for( IUpdateRequestFooterSlotFiller sf : updateRequestFooterSlotFillerList ) 
		{
			try
			{
				String filler = sf.getUpdateRequestFooterHtml(httpRequest, httpResponse, ba, oldRequest, user) ;
				if( null != filler && ! filler.trim().equals("") )
					html += " " + filler + " " ;
			}
			catch(Exception e)
			{
				LOG.severe("Exception while executing the slog filler plugin : " + sf);
				LOG.severe("",(e)) ;				
			}
		}
		
		tagTable.put(UPDATE_REQEUST_FOOTER_SLOT, html) ;
	}
	
	public void runActionDetailsSlotFillers( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, Request oldRequest, User user, Hashtable<String,String> tagTable)
	{
		// get all the IActionDetailsSlotFillers.
		// sort them : this should be done in the begining itself ?? what say ?  
		// replace the tag in tagtable with its concatenated html 
		String html ="" ;		
		for( IActionDetailsHeaderSlotFiller sf : actionDetailsHeaderSlotFillerList ) 
		{
			try
			{
				String filler = sf.getActionDetailsHeaderHtml(httpRequest, httpResponse, ba, oldRequest, user) ;
				if( null != filler && ! filler.trim().equals(""))
					html += " " + filler + " " ;
			}
			catch(Exception e)
			{
				LOG.severe("Exception while executing the slog filler plugin : " + sf);
				LOG.severe("",(e)) ;				
			}
		}
		
		tagTable.put(ACTION_DETAILS_HEADER_SLOT, html) ;
	}
	
	public void runSearchSlotFillers( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, User user, Hashtable<String, String> tagTable)
	{
		// get all the ISearchSlotFillers.
		// replace the tag in tagtable with its concatenated html 
		String html = "" ;
		for( ISearchFooterSlotFiller sf : searchFooterSlotFillerList ) 
		{
			try
			{
				String filler = sf.getSearchFooterHtml(httpRequest, httpResponse, ba, user) ;
				if( null != filler && ! filler.trim().equals(""))
					html += " " + filler + " " ;
			}
			catch(Exception e)
			{
				LOG.severe("Exception while executing the slog filler plugin : " + sf);
				LOG.severe("",(e)) ;				
			}
		}
		
		tagTable.put(SEARCH_FOTTER_SLOT, html ) ;
	}
	
	public void runSearchResultsSlotFillers( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, User user, Hashtable<String,String> tagTable)
	{
		// get all the ISearchResultsSlotFillers.
		// replace the tag in tagtable with its concatenated html 
		String html = "" ;		
		for( ISearchResultsHeaderSlotFiller sf : searchResultsHeaderSlotFillerList ) 
		{
			try
			{
				String filler = sf.getSearchResultsHeaderHtml(httpRequest, httpResponse, ba, user) ;			
				if( null != filler && ! filler.trim().equals(""))
					html += " " + filler + " " ;
			}
			catch(Exception e)
			{
				LOG.severe("Exception while executing the slog filler plugin : " + sf);
				LOG.severe("",(e)) ;				
			}
		}
		
		tagTable.put(SEARCH_RESULTS_HEADER_SLOT, html) ;
	}
//	
//	private void updateTable(Hashtable replacements,
//			Hashtable fillTable) 
//	{
//		if( null != fillTable && null != replacements )
//		{	
//			for( Enumeration tags = fillTable.keys() ; tags.hasMoreElements() ;  )
//			{
//				String tag = (String) tags.nextElement() ;
//				String html = (String) fillTable.get(tag) ;
//				if( null != html && ! html.trim().equalsIgnoreCase("")) 
//				{
//					String currHtml = (String) replacements.get(tag) ;
//					if( null == currHtml ) 
//						currHtml = "" ;
//					
//					currHtml += " " + html + " " ;
//					replacements.put(tag, currHtml) ;
//				}				
//			}
//		}		
//	}

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
				
		}
	}
	private void loadPlugins() 
	{
		
		loadPlugins( IAddRequestFooterSlotFiller.class, addRequestFooterSlotFillerList ) ;
		
		loadPlugins( ISubRequestFooterSlotFiller.class, subRequestFooterSlotFillerList ) ;
		loadPlugins( IUpdateRequestFooterSlotFiller.class, updateRequestFooterSlotFillerList ) ;
		loadPlugins( IActionDetailsHeaderSlotFiller.class, actionDetailsHeaderSlotFillerList ) ;
		loadPlugins( ISearchResultsHeaderSlotFiller.class, searchResultsHeaderSlotFillerList ) ;
		loadPlugins(ISearchFooterSlotFiller.class, searchFooterSlotFillerList) ;
		
			Comparator<IAddRequestFooterSlotFiller> comp = new Comparator<IAddRequestFooterSlotFiller>()
			{
				public int compare(IAddRequestFooterSlotFiller arg0, IAddRequestFooterSlotFiller arg1) {
					double d = arg0.getAddRequestFooterSlotFillerOrder() - arg1.getAddRequestFooterSlotFillerOrder();
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
			Collections.sort(addRequestFooterSlotFillerList, comp);
			
			Comparator<ISubRequestFooterSlotFiller> subcomp = new Comparator<ISubRequestFooterSlotFiller>()
			{
				public int compare(ISubRequestFooterSlotFiller arg0, ISubRequestFooterSlotFiller arg1) {
					double d = arg0.getSubRequestFooterOrder() - arg1.getSubRequestFooterOrder();
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
			Collections.sort(subRequestFooterSlotFillerList, subcomp);
			
			Comparator<IUpdateRequestFooterSlotFiller> ucomp = new Comparator<IUpdateRequestFooterSlotFiller>()
			{
				public int compare(IUpdateRequestFooterSlotFiller arg0, IUpdateRequestFooterSlotFiller arg1) {
					double d = arg0.getUpdateRequestFooterSlotFillerOrder() - arg1.getUpdateRequestFooterSlotFillerOrder();
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
			Collections.sort(updateRequestFooterSlotFillerList, ucomp);
			
			Comparator<IActionDetailsHeaderSlotFiller> adcomp = new Comparator<IActionDetailsHeaderSlotFiller>()
			{
				public int compare(IActionDetailsHeaderSlotFiller arg0, IActionDetailsHeaderSlotFiller arg1) {
					double d = arg0.getActionDetailsHeaderOrder() - arg1.getActionDetailsHeaderOrder();
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
			Collections.sort(actionDetailsHeaderSlotFillerList, adcomp);
			
			Comparator<ISearchFooterSlotFiller> scomp = new Comparator<ISearchFooterSlotFiller>()
			{
				public int compare(ISearchFooterSlotFiller arg0, ISearchFooterSlotFiller arg1) {
					double d = arg0.getSearchFooterOrder() - arg1.getSearchFooterOrder();
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
			Collections.sort(searchFooterSlotFillerList, scomp);
			
			Comparator<ISearchResultsHeaderSlotFiller> srcomp = new Comparator<ISearchResultsHeaderSlotFiller>()
			{
				public int compare(ISearchResultsHeaderSlotFiller arg0, ISearchResultsHeaderSlotFiller arg1) {
					double d = arg0.getSearchResultsHeaderOrder() - arg1.getSearchResultsHeaderOrder();
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
			Collections.sort(searchResultsHeaderSlotFillerList, srcomp);		
	}	
}
