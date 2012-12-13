package transbit.tbits.TVN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;

import com.tbitsglobal.tvncore.Error;
import com.tbitsglobal.tvncore.TvnException;
import com.tbitsglobal.tvncore.TvnType;
import com.tbitsglobal.tvncore.Utils;

/**
 * Provides all the external services related to versioning.
 * 
 * @author karan
 *
 */

public class VersioningServices {

	//====================================================================================

	private static Hashtable<String, Integer> HeadVersions = new Hashtable<String, Integer>();

	//====================================================================================

	/**
	 * Returns the head version of the path. The head version is the maximum version for a given path.
	 * 
	 * @param path
	 * @return
	 * @throws TvnException 
	 */
	public static int getHeadVersion(String path) throws TvnException {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
		
		conn.setAutoCommit(false);
		int headRevision = getHeadVersion(conn, path);
		conn.commit();
		return headRevision;
		} catch (SQLException e) {
			try {
				if(conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new TvnException("Error getting head revision");
		}
		finally
		{
			if(conn != null)
			{
				try
				{
					conn.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//====================================================================================

	public static int getHeadVersion(Connection conn,String path) throws TvnException {
		if(path == null)
			throw new TvnException(Error.NO_PATH_SUPPLIED);
		
		/*  First get the sys_prefix from the path */
		path = Utils.reformPath(path);		//Removes any special URI if there are any
		StringTokenizer st = new StringTokenizer(path, "/\\");
		if(!st.hasMoreTokens())
			throw new TvnException("No sys_prefix found");
		String sys_prefix = st.nextToken().toLowerCase();	//to lower case will solve the problem 
															// regarding case of system prefix
		
		if(HeadVersions == null)
			HeadVersions = new Hashtable<String, Integer>();
		else 
			loadHeadVersion(conn,sys_prefix);
		return HeadVersions.get(sys_prefix);
		
	}
	
	//====================================================================================

	public static int loadHeadVersion(Connection conn,String path) throws TvnException {
		
		if(null == path)
			throw new TvnException("No Path Supplied");
		
		/*  First get the sys_prefix from the path */
		path = Utils.reformPath(path);		//Removes any special URI if there are any
		StringTokenizer st = new StringTokenizer(path, "/\\");
		if(!st.hasMoreTokens())
			throw new TvnException("Cannot Obtain the system prefix.");
		String sys_prefix = st.nextToken();
		try {
			BusinessArea myBusinessArea  = BusinessArea.lookupBySystemPrefix(sys_prefix);
			int sys_id = myBusinessArea.getSystemId();
			String sql = "SELECT  max_version_no version from business_areas where sys_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, sys_id);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				HeadVersions.put(sys_prefix, 0);
				return 0;
			}
			int headRev = rs.getInt(1);
			//if this BA does not contain any requests,its version will be 1
			headRev = (headRev == 0)?1:headRev;
			HeadVersions.put(sys_prefix, headRev);
			rs.close();
			return headRev;
		}
		catch (SQLException e) {
			throw new TvnException("Error in finding Head Revision: " + e.toString());
		} catch (DatabaseException e) {
			throw new TvnException("Error in finding Head Revision: " + e.toString());
		}
	}

	//====================================================================================
	
	public static Integer getVersionOfAttachment(Connection conn,
			int systemId, int requestId, int actionId) {
		try {
			String sql = "SELECT distinct version_no from versions "
					+ "where sys_id = ? and "
					+ "request_id = ? and action_id = ? ";
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1, systemId);
			cs.setInt(2, requestId);
			cs.setInt(3, actionId);

			ResultSet rs = cs.executeQuery();

			// System.out.println("Successfully read From this function");

			if (rs.next()) {
				int verNum = rs.getInt(1);
				rs.close();
				return verNum;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	//====================================================================================

	/**
	 * This function will return the last version, at which request was committed
	 * if at this revision, request was deleted, it will return -1
	 * if the request does not even exists at this revision, it will return 0
	 * 
	 * @param request
	 * @param verNum
	 * @return
	 */
	public static int getLastVersion(Connection conn,int systemId,int requestId,int verNum) {

		try {
			/*
			 * TODO: if the request is not present in last version
			 * its version will be considered equal to 1
			 * I can think of two possible solutions
			 * assume that every request of a versionable BA will 
			 * be in versions table and if not,take its version as 1
			 * otherwise you pick all the requests from the 
			 * versions table itself !!!!!!
			 */
			String sql = "SELECT  max(version_no) as version  from versions " +
			"where sys_id = ? and " +
			"request_id = ? and version_no <= ? GROUP BY request_id";
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1, systemId);
			cs.setInt(2,requestId);
			cs.setInt(3, verNum);

			ResultSet rs = cs.executeQuery();

			if(rs.next()) {
				int lastVersion = rs.getInt(1);
				rs.close();
				cs.close();
				return lastVersion;
			}
			else {
				rs.close();
				cs.close();
				return 0;
			}
			
		}
		catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
			return 0;
		}
	}

	//====================================================================================

	public static int lastCommittedVersion(ArrayList<TvnType> structure, int version) {
		
		int lastCommittedVer = -1;
		Action action = null;
		boolean attachmentPresent = false;
		for(int i=0; i<structure.size(); i++){
			if(structure.get(i).identifier.equals("attachment_id")){
				attachmentPresent = false;
			}
			if(structure.get(i).identifier.equals(Field.REQUEST)){
				action = (Action) structure.get(i).identifierObject;
			}
		}
		
		Connection conn = null;
		
		try{
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			if(action!=null){
				if(attachmentPresent){
					lastCommittedVer =  VersioningServices.getVersionOfAttachment(conn,structure.get(0).identifierHandler,
																				action.getRequestId(), action.getActionId());
				}
				else{
					lastCommittedVer = VersioningServices.getLastVersion(conn, action.getSystemId(), action.getRequestId(),version);
				}
			}
			
			conn.commit();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				if(conn != null)
					conn.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(lastCommittedVer == -1)
			lastCommittedVer = version;
		return lastCommittedVer;
	}

	//====================================================================================

}
