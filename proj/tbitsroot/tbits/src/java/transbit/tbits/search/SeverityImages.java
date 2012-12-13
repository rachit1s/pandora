package transbit.tbits.search;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.util.Hashtable;
import java.util.StringTokenizer;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;


public class SeverityImages {
	 // Application logger.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	Hashtable<String, String> mySeverityImgTable;
	private static SeverityImages instance = null;
	private SeverityImages(String imagePath)
	{
		mySeverityImgTable = new Hashtable<String, String>();
		try
        {
        	loadSeverityImages(mySeverityImgTable, imagePath);
        }
        catch(Exception exp)
        {
        	LOG.warn(exp);
        	//Fail safe
	        // Images based on the severity value.
	        String image = "";
	        //TODO:Need to externalize these.
	        mySeverityImgTable.clear();
	        image                 = "<IMG src='" + imagePath + "critical.gif' alt='Critical' />";
	        mySeverityImgTable.put("critical", image);
	        mySeverityImgTable.put("+critical", image);
	        image = "<IMG src='" + imagePath + "high.gif' alt='High' />";
	        mySeverityImgTable.put("high", image);
	        mySeverityImgTable.put("+high", image);
	        image = "<IMG src='" + imagePath + "low.gif' alt='Low' />";
	        mySeverityImgTable.put("low", image);
	        mySeverityImgTable.put("+low", image);
	        image = "<IMG src='" + imagePath + "v-low.gif' alt='Very low' />";
	        mySeverityImgTable.put("very-low", image);
	        mySeverityImgTable.put("very low", image);
	        mySeverityImgTable.put("+very-low", image);
	        mySeverityImgTable.put("+very low", image);
        }
	}
	
	public static SeverityImages getInstance(String imageUrl)
	{
		if(instance == null)
			instance = new SeverityImages(imageUrl);
		return instance;
	}
	public Hashtable<String, String> getSeverityImgTable()
	{
		return mySeverityImgTable;
	}
	
	private static void loadSeverityImages(Hashtable<String, String> aSeverityImageTable, String imagePath) 
    {
    	if(aSeverityImageTable == null)
    		throw new IllegalArgumentException();
    	
    	aSeverityImageTable.clear();
    	
    	String propertyVal = "";
    	try
    	{
    		propertyVal = PropertiesHandler.getProperty(TBitsPropEnum.KEY_SEVERITY_IMGS);
    	}
    	catch(Exception exp)
    	{
    		LOG.warn("'" + TBitsPropEnum.KEY_SEVERITY_IMGS + " is not mentioned in tbits.properties file.");
    	}
    	if((propertyVal == null) || (propertyVal == ""))
    	{
    		propertyVal = "critical/+critical;critical.gif;Critical,"
    			+ "high/+high;high.gif;High,"
    			+ "low/+low;low.gif;Low,"
    			+ "very-low/very low/+very-low/+very low;v-low.gif;Very low";
    	}
    	//System.out.println("Image Property: " + propertyVal);
    	StringTokenizer st = new StringTokenizer(propertyVal, ",");
    	while(st.hasMoreTokens())
    	{
    		String sevDetails = st.nextToken();
    		//System.out.println("sevDetails: " + sevDetails);
    		StringTokenizer st1 = new StringTokenizer(sevDetails, ";");
    		if(st1.countTokens() != 3)
    		{
    			//System.out.println("Token count: " + st1.countTokens());
    			throw new IllegalArgumentException();
    		}
    		String[] names = st1.nextToken().split("/");
    		String img  = st1.nextToken();
    		String alt = st1.nextToken();
    		String image                 = "<IMG src='" + imagePath + img + "' alt='" + alt + "' />";
    		for(String name:names)
    		{	
    			//System.out.println(name + ":" + image);
    			aSeverityImageTable.put(name, image);
    		}
    	}
    	//System.out.println("Severity Image Table: " + ourSeverityImageTable);
    }
}
