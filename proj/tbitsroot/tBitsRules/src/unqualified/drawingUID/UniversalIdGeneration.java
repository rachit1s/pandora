/**
 * 
 */
package drawingUID;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class UniversalIdGeneration implements IRule {
	
	private static final String UNIVERSAL_ID = "universal_id";
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	static ArrayList<Integer> dcrBAs = new ArrayList<Integer>();

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();		
		if (dcrBAs == null)
		{
			ruleResult.setMessage("Could not continue as no DCR BAs were found or could not retrieve DCR BAs from db.");
			return ruleResult;
		}
		
		if (!isAddRequest)
			return ruleResult;
		
		if (dcrBAs.contains(ba.getSystemId()))
		{
			Field universalIdField = null;
			try 
			{
				universalIdField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), UNIVERSAL_ID);				
			} catch (DatabaseException e) {
				e.printStackTrace();
				LOG.error("Database error occurred while looking up for field: " + UNIVERSAL_ID + " in BA: " + ba.getSystemPrefix());
				ruleResult.setMessage("Database error occurred while looking up for field: " + UNIVERSAL_ID + " in BA: "
						+ ba.getSystemPrefix());
			}

			if (universalIdField == null)
			{
				LOG.warn("Field : " + UNIVERSAL_ID + " was not found and hence did not generate universalId for this request.");
				ruleResult.setMessage("Field : " + UNIVERSAL_ID + " was not found and hence did not generate universalId for" +
				" this request.");
			}
			else
			{
				try{
					String universalIdStr = currentRequest.get(UNIVERSAL_ID);					
					if ((universalIdStr == null) || (universalIdStr.trim().equals("")) 
							|| (universalIdStr.trim().equals("0")))
					{
						Integer universalId = Integer.valueOf(universalIdStr);
						try {
							universalId = getUniversalId(connection, UNIVERSAL_ID);
							currentRequest.setObject(UNIVERSAL_ID, universalId);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					else
					{
						LOG.info("Universal Id was found, hence did not generate new universal id.");
					} 	
				}catch(Exception e){
					e.printStackTrace();
					ruleResult.setMessage("Error occurred while setting UID in BA: " + ba.getSystemPrefix());
				}
			}
		}


		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Generates universal id for each request being generated.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 0.1;
	}
	
	
	static{		
		try {
			dcrBAs.addAll(getDCRBusinessAreas());
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static ArrayList<Integer> getDCRBusinessAreas()
	throws DatabaseException{

		Connection connection = null;
		ArrayList<Integer> dcrSysIdList = new ArrayList<Integer>();
		try{
			connection = DataSourcePool.getConnection();
			ArrayList<Integer> dcrBAs;
			dcrSysIdList.addAll(getDCRBusinessAreas(connection));
			
		}catch (SQLException sqle){
			LOG.error("Error while retrieving target business area ids.", sqle);
			throw new DatabaseException("Error while retrieving target business area ids.", sqle);
		}finally{
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new DatabaseException("Error occurred while fetching DCR business areas for generating universal id.", sqle);
				}
			}
		}
		
		return dcrSysIdList;	
	}

	/**
	 * @param connection
	 * @return list of DCR business areas.
	 * @throws SQLException
	 */
	private static ArrayList<Integer> getDCRBusinessAreas(Connection connection) throws SQLException {
		ArrayList<Integer> dcrSysIdList = new ArrayList<Integer>();
		if (connection != null)
		{
			PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT src_sys_id FROM trn_processes");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){				
					dcrSysIdList.add(rs.getInt("src_sys_id"));
				}
			rs.close();
		}
		return dcrSysIdList;
	}
		
	public static int getUniversalId(Connection conn, String key) throws SQLException
	{
		CallableStatement stmt = conn
		.prepareCall("stp_getAndIncrMaxId ?");
		stmt.setString(1, key);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			int id = rs.getInt("max_id");
			return id;
		} else {
			throw new SQLException();
		}
	}

	
	public static void main(String[] args) throws DatabaseException{
		//UniversalIdGeneration ug = new UniversalIdGeneration();
//		System.out.println("dcrBA" + dcrBAs.toString());
//		int i = 27;
//		if (dcrBAs.contains(i))
//			System.out.println("Yes exists!!!!");
//		else
//			System.out.println("Does not exists!!!!");
		/*AddRequest addRequest = new AddRequest();
		Hashtable<String, String> param = new Hashtable<String,String>();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

		param.put(Field.BUSINESS_AREA, "KDI_LNT");
		param.put(Field.SUBJECT, "cmd test uid");
		param.put(Field.USER, "root");*/
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Done: " + getDCRBusinessAreas());
	}

}
