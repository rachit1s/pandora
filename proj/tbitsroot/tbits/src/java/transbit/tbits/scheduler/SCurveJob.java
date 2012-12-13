package transbit.tbits.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.searcher.DqlSearcher;

public class SCurveJob implements ITBitsJob, TBitsConstants{

	public static TBitsLogger LOG	= TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);
	
	//====================================================================================

	public String getDisplayName() {
		return "S-Curve Generator Job";
	}

	//====================================================================================

	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		return new Hashtable<String, JobParameter>();
	}

	//====================================================================================

	public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException {
		return false;
	}

	//====================================================================================

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			// TODO: Currently generating all the curves. 
			// Change if the user needs to specify the curves somehow.		
			System.out.println("\nGetting list of curve ids...\n");
			ArrayList<Integer> curveList = getAllCurves(conn);
			if(curveList == null)
				return;
			
			for(int cIndex = 0; cIndex<curveList.size(); cIndex++){
				
				int curve_id = curveList.get(cIndex);
				int sys_id = 0;
				String DSQL = null;
				int user_id = 0;
				String weightage_fieldname = null;
				String factor_fieldname = null;
				String earlystart_date_fieldname = null;
				String latestart_date_fieldname = null;
				
				System.out.println("\ncurve_id = "+curve_id+"\n");
				
				boolean is_dql = true;
				
				// Read curve information
				try{
					// Read the sys_id, DSQL from the scurve_curves table
					String query = 	"select sys_id, is_dql, query, user_id, weightage_fieldname, factor_fieldname, " +
									"earlystart_date_fieldname, latestart_date_fieldname " +
									"from scurve_curves where curve_id=?";
					PreparedStatement statement = conn.prepareStatement(query);
					statement.setInt(1, curve_id);
					ResultSet rs = statement.executeQuery();
					if(rs.next()){
						sys_id = rs.getInt("sys_id");
						is_dql = rs.getBoolean("is_dql");
						DSQL = rs.getString("query");
						user_id = rs.getInt("user_id");
						weightage_fieldname = rs.getString("weightage_fieldname");
						factor_fieldname = rs.getString("factor_fieldname");
						earlystart_date_fieldname = rs.getString("earlystart_date_fieldname");
						latestart_date_fieldname = rs.getString("latestart_date_fieldname");
					}
				} 
				catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Error reading the scurve_curves table.");
					return;
				}	
				// Search for requests based on the DSQL query
				System.out.println("\nSearching for DSQL = "+DSQL+"\n");
				ArrayList<Integer> requests = new ArrayList<Integer>();
				try{
					requests = getRequestSet(sys_id, user_id, DSQL, is_dql);
				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("DSQL search unsuccessful.");
					return;
				}
			
				// Populate the scurve_curve_requests table
				try {
					populateRequests(conn, curve_id, requests);
				} 
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("Unable to populate the list of requests in scurve_curve_requests.");
					return;
				}
			
				// Execute the stored procudure for generating the S-Curve
				// Clear the scurve_curve_points table of existing points for the given curve
				System.out.println("\nClearing the points table.\n");
				String query = "delete from scurve_curve_points where curve_id=?";
				PreparedStatement statement = conn.prepareStatement(query);
				statement.setInt(1, curve_id);
				statement.execute();
				
				System.out.println("\nExecuting the stored procedure.\n");
				query = "EXECUTE stp_scurve_generate_curve ?,?,?,?,?";
				PreparedStatement stp = conn.prepareStatement(query);
				stp.setInt(1, curve_id);
				stp.setString(2, weightage_fieldname);
				stp.setString(3, factor_fieldname);
				stp.setString(4, earlystart_date_fieldname);
				stp.setString(5, latestart_date_fieldname);
				stp.execute();
				System.out.println("\nStored Procedure executed successfully.\n");
				
				conn.commit();
			} 
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error while executing the stored procedure for S-Curve generation.");
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
				System.out.println("Error while rolling back connection.");
			}
			return;
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Unable to close connection.");
				}
		}
		
		return;
	}

	//====================================================================================

	/**
	 * Fetches and returns a list of the curve ids of all the curves mentioned in the
	 * scurve_curves table.
	 * @return ArrayList of all the curve_ids for which the curves are to be made.
	 */
	private ArrayList<Integer> getAllCurves(Connection conn){
		
		// Retrieve a list of all the curves to be generated
		ArrayList<Integer> curveList = new ArrayList<Integer>();
		try {
			String query = "select curve_id from scurve_curves";
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query);
			while(rs.next()){
				curveList.add(rs.getInt(1));
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to retrieve the list of curve_ids.");
			return null;
		}
		
		return curveList;
	}
	
	//====================================================================================

	/**
	 * Populates the request table with the request ids resulting from the searched DQL.
	 * @param curve_id
	 * @param requests : a list of all the requests to be populated
	 * @throws Exception
	 */
	private void populateRequests(Connection conn, int curve_id, ArrayList<Integer> requests) throws Exception {
		
		try{
			// Clean up the table for the given curve_id
			System.out.println("\nClearing the scurve_curve_requests table.\n");
			String query = "delete from scurve_curve_requests where curve_id=?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, curve_id);
			statement.execute();
			
			System.out.println("\nAdding requests to the requests table for curve_id = "+curve_id+"\n");
			query = "insert into scurve_curve_requests values (?,?)";
			// Create batches of 100 requests at a time and insert to the table
			while(!requests.isEmpty()){
				ArrayList<Integer> tempReqList = new ArrayList<Integer>();
				// Retrieve a maximum of 100 requests
				int i;
				for(i=0; i<100; i++){
					if(requests.isEmpty())
						break;
					tempReqList.add(requests.remove(0));
				}
				// Prepare the batch query
				PreparedStatement insertStatement = conn.prepareStatement(query);
				for(i=0; i<tempReqList.size(); i++){
					insertStatement.setInt(1, curve_id);
					insertStatement.setInt(2, tempReqList.get(i));
					insertStatement.addBatch();
				}
				// Run the batch update
				int[] result = insertStatement.executeBatch();
				for (i=0; i<result.length; i++) {
			        if (result[i] == Statement.EXECUTE_FAILED) {
			            System.out.println("Failure in writing the request batch.");
			            throw new Exception();
			        }
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return;
	}

	//====================================================================================

	/**
	 * Gets the list of request ids corresponding to the search result of the given DQL.
	 * @param sys_id
	 * @param user_id
	 * @param DSQL
	 * @return ArrayList of resulting the request ids
	 * @throws TBitsException 
	 * @throws Exception
	 */
	private ArrayList<Integer> getRequestSet(int sys_id, int user_id, String DSQL, boolean isDql) throws TBitsException{
		ArrayList<Integer> requests = new ArrayList<Integer>();
		if(isDql){
			DqlSearcher searcher = new DqlSearcher(sys_id, user_id, DSQL);
			try {
				searcher.search();
			} catch (Exception e) {
				LOG.info("",(e));
				throw new TBitsException(e);
			}
			
			// Parse all the request ids searched and construct the requests ArrayList
			if(searcher.getResult().containsKey(sys_id)){
				Collection<Integer> requestIdsFetched = searcher.getResult().get(sys_id).keySet();
				if(requestIdsFetched != null)
					requests.addAll(requestIdsFetched);
			}
		}else{
			Connection conn = null;
			
			try {
				conn = DataSourcePool.getConnection();
				
				PreparedStatement statement = conn.prepareStatement(DSQL);
				ResultSet rs = statement.executeQuery();
				while (rs.next()) {
					int requestId = rs.getInt("request_id");
					requests.add(requestId);
				}
			} catch (SQLException e) {
				LOG.info("",(e));
				throw new TBitsException(e);
			}finally{
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						LOG.info("",(e));
						throw new TBitsException(e);
					}
				}
			}
		}
		
		return requests;
	}

	//====================================================================================

}
