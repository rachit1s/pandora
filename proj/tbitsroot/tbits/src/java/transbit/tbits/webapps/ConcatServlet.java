package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.common.TBitsLogger;

/* ------------------------------------------------------------ */
/** Concatenation Servlet
 * This servlet may be used to concatenate multiple resources into
 * a single response.  It is intended to be used to load multiple
 * javascript or css files, but may be used for any content of the
 * same mime type that can be meaningfully concatenated.
 * <p>
 * The servlet uses {@link RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * to combine the requested content, so dynamically generated content
 * may be combined (Eg engine.js for DWR).
 * <p>
 * The servlet uses parameter names of the query string as resource names
 * relative to the context root.  So these script tags:
 * <pre>
 *  &lt;script type="text/javascript" src="../js/behaviour.js"&gt;&lt;/script&gt;
 *  &lt;script type="text/javascript" src="../js/ajax.js&/chat/chat.js"&gt;&lt;/script&gt;
 *  &lt;script type="text/javascript" src="../chat/chat.js"&gt;&lt;/script&gt;
 * </pre> can be replaced with the single tag (with the ConcatServlet mapped to /concat):
 * <pre>
 *  &lt;script type="text/javascript" src="../concat?/js/behaviour.js&/js/ajax.js&/chat/chat.js"&gt;&lt;/script&gt;
 * </pre>
 * The {@link ServletContext#getMimeType(String)} method is used to determine the
 * mime type of each resource.  If the types of all resources do not match, then a 415
 * UNSUPPORTED_MEDIA_TYPE error is returned.
 * <p>
 * If the init parameter "development" is set to "true" then the servlet will run in
 * development mode and the content will be concatenated on every request. Otherwise
 * the init time of the servlet is used as the lastModifiedTime of the combined content
 * and If-Modified-Since requests are handled with 206 NOT Modified responses if
 * appropriate. This means that when not in development mode, the servlet must be
 * restarted before changed content will be served.
 * See: http://blogs.webtide.com/gregw/entry/combining_javascript_and_css_content
 * @author gregw
 *
 */
public class ConcatServlet extends HttpServlet
{
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	private static final String EXPIRES_HEADER = "Expires";
	private static final String LAST_MODIFIED_HEADER = "Last-Modified" ;
	private static final String CACHE_CONTROL_HEADER = "Cache-Control" ;

    ServletContext _context;

    /* ------------------------------------------------------------ */
    public void init() throws ServletException
    {
    	/*
    	 * The servlet container is not required to keep a servlet loaded for any particular
			period of time. A servlet instance may be kept active in a servlet container for a
			period of milliseconds, for the lifetime of the servlet container (which could be a
			number of days, months, or years), or any amount of time in between.
			Nitiraj : msg : so the last modified time for each resource changes every time this
			servlet is loaded, which is absolutely wrong. It is even lesser than if we set it to
			server-start time .. 
    	 */

        _context=getServletContext();

    }

    /* ------------------------------------------------------------ */
    /*
     * @return The start time of the servlet unless in development mode, in which case -1 is returned.
     */
    protected long getLastModified(HttpServletRequest req)
    {
    	// it will return the last token of the query string. Assuming that it is the 
    	// max_last_modified time of all the resources in the string. 
    	String q = req.getQueryString() ;
    //	LOG.info("query string = " + q ) ;
    	if( null == q ) 
    		return -1 ;
    	else
    	{
    		String[] parts = q.split("\\&") ;
    		String mlmt = parts[parts.length - 1] ;
    		try
    		{
    			long mod = Long.parseLong(mlmt) ;
    			mod = mod/1000 * 1000 ;
    		//	LOG.info(" returning last modified : " + DateFormat.getInstance().format(new Date(mod))) ;
    			return mod ;
    		}
    		catch( Exception e ) 
    		{
    			e.printStackTrace() ;
    			return -1 ;
    		}
    	}
   
//        return _development?-1:_lastModified;
    }

    /* ------------------------------------------------------------ */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String q=req.getQueryString();
    //    LOG.info("queryString : " + q );
        if (q==null)
        {
            resp.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String[] parts = q.split("\\&"); 
        String type=null;
        for (int i=0;i<parts.length-1;i++)
        {
            String t = _context.getMimeType(parts[i]);
//            LOG.info("parts["+i+"] =" + parts[i] + " : mimetype = " + t );
            if (t!=null)
            {
                if (type==null)
                    type=t;
                else if (!type.equals(t))
                {
                    resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                    return;
                }
            }
        }
        
        // get the last part which is supposed to be the max_last_modified_time
        // put this in the Last Modified header : although I won't use this header
        // but browsers require Expire or cache-control and last-modified
        // or ETag
           

        if (type!=null)
        {
        	resp.setContentType(type);
        	Calendar cal = Calendar.getInstance() ; // get today's date 
        	cal.add(Calendar.MONTH, 11 ) ; // increment the date by 11 months 
        	long expiryTime = cal.getTime().getTime() ;
        //	LOG.info("setting expire date : " + DateFormat.getInstance().format(new Date(expiryTime))) ;
        	resp.setDateHeader(EXPIRES_HEADER, expiryTime) ;
        	
        	String mlmd = parts[parts.length-1] ;
        	try
        	{
        		long mod = Long.parseLong(mlmd)/1000 * 1000 ;
        //		LOG.info("setting last modified : " + DateFormat.getInstance().format(new Date(mod))) ;
        		resp.setDateHeader(LAST_MODIFIED_HEADER, mod) ;
        	}
        	catch( Exception e )
        	{
        		e.printStackTrace() ;
        	}
        	Calendar now = Calendar.getInstance() ;
        	long max_age_value = cal.getTimeInMillis() - now.getTimeInMillis() ;
        	max_age_value = max_age_value / 1000 ; // change time into seconds 
        	resp.setHeader(CACHE_CONTROL_HEADER, "public, max-age="+max_age_value) ;
        }

//        resp.set
        for (int i=0;i<parts.length-1;i++)
        {
        	RequestDispatcher dispatcher = req.getRequestDispatcher(parts[i]) ;
            if (dispatcher!=null)
                dispatcher.include(req,resp);
        }
    }
}
