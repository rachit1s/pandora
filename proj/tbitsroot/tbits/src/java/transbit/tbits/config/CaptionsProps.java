package transbit.tbits.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;


/*
 * @author:Abhishek Agarwal
 */
public class CaptionsProps implements TBitsPropEnum{
		
	// Public constants to be used instead of hard coding properties. 
	public static String CAPTIONS_ADD_LINK_REQUESTS="captions.add.link_requests";
	public static String CAPTIONS_ALL_CAMEL_CASE_REQUEST="captions.all.camel_case_request";
	public static String CAPTIONS_ALL_CAMEL_CASE_REQUESTS="captions.all.camel_case_requests";
	public static String CAPTIONS_ALL_MY_REQUESTS="captions.all.my_requests";
	public static String CAPTIONS_ALL_NEW_REQUEST="captions.all.new_request";
	public static String CAPTIONS_ALL_REQUEST="captions.all.request";
	public static String CAPTIONS_ALL_REQUESTS="captions.all.requests";
	public static String CAPTIONS_MAIN_ALL_REQUEST_NUMBER="captions.main.all.request_number";
	public static String CAPTIONS_VIEW_ADD_REQUEST="captions.view.add_request";
	public static String CAPTIONS_VIEW_ADD_SUBREQUEST="captions.view.add_subrequest";
	public static String CAPTIONS_VIEW_PRINT_REQUEST_DETAILS="captions.view.print_request_details";
	public static String CAPTIONS_VIEW_REQUEST_DETAILS="captions.view.request_details";
	public static String CAPTIONS_VIEW_REQUEST_HISTORY="captions.view.request_history";
	public static String CAPTIONS_VIEW_SORT_REQUEST_HISTORY="captions.view.sort_request_history";
	public static String CAPTIONS_VIEW_SUBREQUESTS="captions.view.subrequests";
	public static String CAPTIONS_VIEW_TRANSFER_REQUEST="captions.view.transfer_request";
	public static String CAPTIONS_VIEW_UPDATE_REQUEST="captions.view.update_request";
	public static String CAPTIONS_ADD_SUMMARY="captions.view.add_summary";
	public static String CAPTIONS_UPDATE_SUMMARY="captions.view.update_summary";
	
	public static final String CAPTION_TABLE_NAME = "captions_properties";
	public static final String CAPTION_COL_NAME = "name";
	public static final String CAPTION_COL_VALUE = "value";
	public static final String CAPTION_COL_SYS_ID = "sys_id";

	private static String captionsFile = "etc/captions.properties";
	public static final TBitsLogger LOG         = TBitsLogger.getLogger(TBitsConstants.PKG_COMMON);
    
	private static CaptionsProps instance = null;
	private static HashMap<String, String> captionsHashMap = null;
	private static HashMap<Integer ,HashMap<String,String>> AllBAcaptionsMap = null;
	
	private CaptionsProps()
	{
		loadCaptions();
	}
	
	public static CaptionsProps getInstance()
	{
		if(instance == null)
			instance = new CaptionsProps();
		return instance;
	}
	
	Properties captionProps = null;
	
	public static void reloadCaptions()
	{
		CaptionsProps cp = CaptionsProps.getInstance();
		cp.loadCaptions();
	}
	private void loadCaptions()
	{
		captionProps = new Properties();  
		
		AllBAcaptionsMap = new HashMap<Integer ,HashMap<String,String>>();
		
		
		boolean isLoadDefaultValues = false;
		//First Try to load the captions from Database
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
						
			try {
				//gets the no. of distinct business areas present in database 		
				PreparedStatement psCountsysId = conn.prepareStatement("SELECT DISTINCT  " +
					CAPTION_COL_SYS_ID + " FROM "+ CAPTION_TABLE_NAME + " ORDER BY " + CAPTION_COL_SYS_ID);
				ResultSet rsCountsysId = psCountsysId.executeQuery();
			
				if(null != rsCountsysId) {
					while(rsCountsysId.next()) {
						AllBAcaptionsMap.put(rsCountsysId.getInt(1),new HashMap<String, String>());
					}
					rsCountsysId.close();
				}
				psCountsysId.close();
				
				}catch(Exception e) { 
					//ignores the exception and consider the total business areas = 0
					e.printStackTrace();
				}
			
				PreparedStatement ps = conn.prepareStatement("SELECT " + CAPTION_COL_NAME + ", " +
								CAPTION_COL_VALUE + "," + CAPTION_COL_SYS_ID + " FROM "+ CAPTION_TABLE_NAME + " ORDER BY " +
								CAPTION_COL_SYS_ID );
							
				ResultSet rs = ps.executeQuery();
			
				if(null != rs) {
					while(rs.next()) {
						if(null != AllBAcaptionsMap.get(rs.getInt(3)))
							AllBAcaptionsMap.get(rs.getInt(3)).put(rs.getString(1), rs.getString(2));
					}
					rs.close();
				}
				ps.close();	
				} catch (Exception e2) {
					e2.printStackTrace();
					LOG.error(e2);	
					LOG.warn("Could Not load the captions from database: " +
							"Trying to laod it from file");
					File file = Configuration.findPath(captionsFile);
		
					if(file != null) {			
						FileInputStream fis;
						try {
							fis = new FileInputStream(file);
							captionProps.load(fis);
						} catch (FileNotFoundException e1) {
							isLoadDefaultValues = true;
						} catch (IOException e) {
							isLoadDefaultValues = true;
						}
					}
					else
						isLoadDefaultValues = true;
		
					if(isLoadDefaultValues){
						LOG.warn("Failed to read from the caption file.Loading default captions.");
						loadDefaultProperties();
					}
		
					//Creating the hashmap once. Since this config is not gonna be modified.
					// and it is going to be used very frequently.
					Enumeration<Object> captionsEnum = captionProps.keys();
					captionsHashMap = new HashMap<String, String>();
					while(captionsEnum.hasMoreElements())
					{
						String key = (String) captionsEnum.nextElement();
						String value = (String) captionProps.get(key);
						captionsHashMap.put(key, value);
					}
					AllBAcaptionsMap.put(0,captionsHashMap);
				}
				finally
				{
					if(conn != null)
					{
						try {
							conn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
		return;
	}
	
	private void loadDefaultProperties()
	{
		captionProps.put("captions.all.request", "request");
		captionProps.put("captions.all.requests", "requests");
		captionProps.put("captions.all.camel_case_request", "Request");
		captionProps.put("captions.all.camel_case_requests", "Requests");
		captionProps.put("captions.all.my_requests", "My Requests");
		captionProps.put("captions.main.all.request_number", "Request #");
		captionProps.put("captions.view.update_request", "Update Request");
		captionProps.put("captions.view.request_history", "Request History");
		captionProps.put("captions.view.sort_request_history", "Sort Request History");
		captionProps.put("captions.add.link_requests", "Link Requests");
		captionProps.put("captions.view.subrequests", "Subrequests");
		captionProps.put("captions.view.add_subrequest", "Add Subrequest");
		captionProps.put("captions.view.add_request", "Add Request");
		captionProps.put("captions.all.new_request", "New Request");
		captionProps.put("captions.view.transfer_request", "Transfer Request");
		captionProps.put("captions.view.print_request_details", "Print Request Details");
		captionProps.put("captions.view.request_details", "Request Details");
	}
	
	public HashMap<String, String> getCaptionsHashMap(int systemId)
	{
		/*By Default this hash will contain all the properties of default business area,
		 * any other particular caption properties will override the default one
		 */
		
		HashMap<String,String> tempHash = AllBAcaptionsMap.get(0);
		if(tempHash == null)
		{
			tempHash = new HashMap<String, String>();
		}
		HashMap<String,String> baHashMap = AllBAcaptionsMap.get(systemId);
		if(baHashMap != null)
			tempHash.putAll(baHashMap);
		
 		return tempHash;
	}
	
	public HashMap<String, String> getOnlyNonDefaultCaptions (int systemId) throws DatabaseException{
		HashMap<String,String> captionsHashMap = new HashMap<String,String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT " + CAPTION_COL_NAME + ", " + 
					CAPTION_COL_VALUE + " FROM "+ CAPTION_TABLE_NAME + 
					" WHERE " + CAPTION_COL_SYS_ID + "=" + systemId);

			ResultSet rs = ps.executeQuery(); 

			if(null != rs) {
				while(rs.next()) {
					captionsHashMap.put(rs.getString(1), rs.getString(2));
				}
				rs.close();
			}
			ps.close();
			
			/**
			 * the connection object was getting closed 2 times first in the
			 * try block and the again in the finally block 
			 * fixed the bug by commeting the connection.close in line below
			 */
//			connection.close();      

		} catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving captions for the BA: ").append("\nSystemId Id: ").append(systemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }
		return captionsHashMap;
	}
	
	public static void insert (int aSystemId, String captionName, String captionValue) throws DatabaseException{
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();			
			CallableStatement cs = connection.prepareCall("stp_ba_caption_insert ?, ?, ?");
			cs.setInt(1, aSystemId);
			cs.setString(2, captionName);
			cs.setString(3, captionValue);
			cs.execute();
			cs.close();	
			/**
			 * the connection object was getting closed 2 times first in the
			 * try block and the again in the finally block 
			 * fixed the bug by commeting the connection.close in line below
			 */
			//connection.close();
		} catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while inserting captions for the BA: ").append("\nSystemId Id: ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }
	}
	
	public static void main(String[] args) throws SQLException, DatabaseException
	{
		//HashMap<String, String> captions = CaptionsProps.getInstance().getCaptionsHashMap(2);
		/*HashMap<String, String> captions = CaptionsProps.getInstance().getOnlyNonDefaultCaptions(2); 
		
		for(String key: captions.keySet())
		{
			System.out.println(key + "=" + captions.get(key));
			
		}*/
		insert(11, "test.caption11", "test11");
		/*System.out.println(CaptionsProps.getInstance().getCaptionsHashMap(2).get("captions.view.update_request"));
		System.out.println(CaptionsProps.getInstance().getCaptionsHashMap(0).get("captions.view.update_request"));
		System.out.println(CaptionsProps.getInstance().getCaptionsHashMap(2).get("captions.view.update_request"));*/
		
	/*	System.out.println("CAPTIONS_ADD_LINK_REQUESTS=" +captions.get(CaptionsProps.CAPTIONS_ADD_LINK_REQUESTS));
		System.out.println("CAPTIONS_ALL_CAMEL_CASE_REQUEST=" +captions.get(CaptionsProps.CAPTIONS_ALL_CAMEL_CASE_REQUEST));
		System.out.println("CAPTIONS_ALL_CAMEL_CASE_REQUESTS=" +captions.get(CaptionsProps.CAPTIONS_ALL_CAMEL_CASE_REQUESTS));
		System.out.println("CAPTIONS_ALL_MY_REQUESTS=" +captions.get(CaptionsProps.CAPTIONS_ALL_MY_REQUESTS));
		System.out.println("CAPTIONS_ALL_NEW_REQUEST=" +captions.get(CaptionsProps.CAPTIONS_ALL_NEW_REQUEST));
		System.out.println("CAPTIONS_ALL_REQUEST=" +captions.get(CaptionsProps.CAPTIONS_ALL_REQUEST));
		System.out.println("CAPTIONS_ALL_REQUESTS=" +captions.get(CaptionsProps.CAPTIONS_ALL_REQUESTS));
		System.out.println("CAPTIONS_MAIN_ALL_REQUEST_NUMBER=" +captions.get(CaptionsProps.CAPTIONS_MAIN_ALL_REQUEST_NUMBER));
		System.out.println("CAPTIONS_VIEW_ADD_REQUEST=" +captions.get(CaptionsProps.CAPTIONS_VIEW_ADD_REQUEST));
		System.out.println("CAPTIONS_VIEW_ADD_SUBREQUEST=" +captions.get(CaptionsProps.CAPTIONS_VIEW_ADD_SUBREQUEST));
		System.out.println("CAPTIONS_VIEW_PRINT_REQUEST_DETAILS=" +captions.get(CaptionsProps.CAPTIONS_VIEW_PRINT_REQUEST_DETAILS));
		System.out.println("CAPTIONS_VIEW_REQUEST_DETAILS=" +captions.get(CaptionsProps.CAPTIONS_VIEW_REQUEST_DETAILS));
		System.out.println("CAPTIONS_VIEW_REQUEST_HISTORY=" +captions.get(CaptionsProps.CAPTIONS_VIEW_REQUEST_HISTORY));
		System.out.println("CAPTIONS_VIEW_SORT_REQUEST_HISTORY=" +captions.get(CaptionsProps.CAPTIONS_VIEW_SORT_REQUEST_HISTORY));
		System.out.println("CAPTIONS_VIEW_SUBREQUESTS=" +captions.get(CaptionsProps.CAPTIONS_VIEW_SUBREQUESTS));
		System.out.println("CAPTIONS_VIEW_TRANSFER_REQUEST=" +captions.get(CaptionsProps.CAPTIONS_VIEW_TRANSFER_REQUEST));
		System.out.println("CAPTIONS_VIEW_UPDATE_REQUEST=" +captions.get(CaptionsProps.CAPTIONS_VIEW_UPDATE_REQUEST));
*/
		
	}

	public HashMap<Integer ,HashMap<String,String>> getAllBAcaptionsMap() {
		return AllBAcaptionsMap;
	}
}
