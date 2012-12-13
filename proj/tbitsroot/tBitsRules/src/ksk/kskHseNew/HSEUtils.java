package kskHseNew;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;

public class HSEUtils implements Serializable {
	
	static TBitsLogger LOG = TBitsLogger.getLogger("hse");

	public static int getNextRepNo(Connection con, String repCat ) throws SQLException
	{
	System.out.println("generating rep. no. for : " + repCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, repCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw e;
		}		
	}

	
	
	public static String extractRelatedRequestId(String relatedReqString,String sysPref) {
		ArrayList<String> relReqs = Utilities.toArrayList(relatedReqString) ;
		String relCorrReq = null ;
		for( String r : relReqs )
		{
			String[] parts = r.split("#") ;
			if(null != parts && parts.length > 1 )
			{
				String sysPrefix = parts[0].trim() ;
				if( sysPrefix.equalsIgnoreCase(sysPref))
				{	relCorrReq = parts[1].trim() ;
				break;
				}
			}
		}
		return relCorrReq;
	}

}
