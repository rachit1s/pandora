package transbit.tbits.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;

/**
 * The Db interaction services required or the java rules.
 * This is a singleton class.
 * 
 * @author karan
 *
 */
public class RulesDbService {
	
	//================================================================================

	private static RulesDbService instance;
	
	// The db tables
	private static final String MAIN_TABLE = "rules_definitions";
	private static final String CODE_TABLE = "rules_storage";

	//================================================================================

	/**
	 * Constructor
	 */
	private RulesDbService(){}
	
	/**
	 * @return singleton instance of the class
	 */
	public static RulesDbService getInstance(){
		if(instance == null)
			instance = new RulesDbService();
		return instance;
	}
	
	//================================================================================

	/**
	 * Get all the existing rules from the database. The rules can be deployed or undeployed.
	 * 
	 * @return ArrayList of RulesClient
	 */
	
	public ArrayList<RuleClass> getExistingRules() {
		
		Connection conn = null;
		ArrayList<RuleClass> rules = new ArrayList<RuleClass>();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			String query = "select * from " + MAIN_TABLE;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				RuleClass rc = new RuleClass(rs.getString("name"), rs.getString("type"), rs.getDouble("seq_number"));
				rules.add(rc);
			}
			
			conn.commit();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return rules;
	}

	//================================================================================

	/**
	 * Get the bytes corresponding to the given rule class
	 * @param name
	 * @return class bytes for the required rule
	 * @throws Exception 
	 */
	public byte[] getRuleClassBytes(String name) throws Exception {
		Connection conn = null;
		byte[] classbytes = null;
    	try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
	    	
	    	String query = "select class from " + CODE_TABLE + " where name=?";
	    	PreparedStatement stmt = conn.prepareStatement(query);
	    	stmt.setString(1, name);
	    	ResultSet rs = stmt.executeQuery();
	    	if(rs.next()){
	    		classbytes = rs.getBytes(1);
	    	}
	    	else
	    		throw new Exception("No such rule exists : " + name);
	    	
	    	conn.commit();
		}
    	catch(SQLException e){
    		e.printStackTrace();
    		conn.rollback();
    	}
    	finally{
			if(conn!=null)
				conn.close();
		}
		
    	return classbytes;
	}

	//================================================================================

}
