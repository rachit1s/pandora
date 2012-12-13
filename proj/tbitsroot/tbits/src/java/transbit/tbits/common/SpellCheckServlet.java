package transbit.tbits.common;

//import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.Helper.TBitsConstants;

import com.google.gson.Gson;

public class SpellCheckServlet extends HttpServlet 
{
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_COMMON);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String MY_STRING = "MY_STRING";
	
	public void doGet( HttpServletRequest aRequest, HttpServletResponse aResponse )
	{
		try {
			handleRequest(aRequest,aResponse) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doPost( HttpServletRequest aRequest, HttpServletResponse aResponse )
	{
		try {
			handleRequest(aRequest,aResponse) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws IOException 
	{	
		ArrayList<WordSuggestions> mySuggestions = null ;
		// get all suggestions
		// form the json and send		
		String myString = aRequest.getParameter(MY_STRING) ;
		if( myString != null )
		{
			mySuggestions = TBitsSpellChecker.getInstance().getErrors(myString);
		}
		 
		if( mySuggestions == null )
			mySuggestions = new ArrayList<WordSuggestions>() ; 
		Gson gson = new Gson() ;
    	String myJson = gson.toJson(mySuggestions) ;
    	PrintWriter out = aResponse.getWriter() ;
		out.println(myJson);	
		out.flush() ;
		return ;
	}
}

class SuggestData
{
	Hashtable<String,ArrayList<String>> data ;
	public SuggestData( Hashtable<String,ArrayList<String>> data )
	{
		this.data = data ;
	}
}
