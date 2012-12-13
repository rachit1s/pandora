package transbit.tbits.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import transbit.tbits.common.DataSourcePool;

public class MigrateProperties {
	
	public static void main(String args[]) throws IOException {
		
		
		if(args.length < 2) {
			System.out.println("Please Provide the Absolute" +
					"Path of Properties File And Table Name");
			System.out.println("Syntax: java MigrateProperties PropertiesFileName TableName");
			return;
		}
		
		System.out.println("Syntax: java MigrateProperties PropertiesFileName TableName");
		
		String propertiesFile = args[0];
		String TableName = args[1];
		
	    System.out.println("Attempting to read Properties");
    	File file1 = new File(propertiesFile);
    	FileInputStream fis = new FileInputStream(file1);
    	
    	Properties tempProp = new Properties();
    	tempProp.load(fis);
    	System.out.println("loading properties into database");
    	
    	int count = 0;
    	
    	Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
		
    	PreparedStatement ps = null;
    	
    	System.out.println("Deleting old Entries");
    	String sql = "DELETE FROM " + TableName;
    	ps = conn.prepareStatement(sql);
   		ps.execute();
    	
    	for(Object key : tempProp.keySet()) {
       		sql = "INSERT INTO " + TableName + "(name,value) Values('" 
       				+ (String)key +"','" + (String)tempProp.get(key) + "')\n";
       		ps = conn.prepareStatement(sql);
       		ps.execute();
       		count++;
        	}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    	System.out.println("Total Rows inserted " + count);	
	}
	

}
