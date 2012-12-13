/**
 * 
 */
package ncc;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

/**
 * @author lokesh
 *
 */
public class NCCTransmittalDropDownOption {
	int dcrSystemId, id, sortOrder;
	String name, displayName;
	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	private static final String MAIN_PROCESS_SELECT_QUERY = "select * from  trn_dropdown where src_sys_id=?";
	
	public NCCTransmittalDropDownOption(int dcrSystemId, int id, String name,
			String displayName, int sortOrder) {
		this.dcrSystemId = dcrSystemId;
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.sortOrder = sortOrder;
	}
	public int getDcrSystemId() {
		return dcrSystemId;
	}
	public void setDcrSystemId(int dcrSystemId) {
		this.dcrSystemId = dcrSystemId;
	}
	public int getId() {
		return id;
	}
	public void setTargetSystemId(int targetSystemId) {
		this.id = targetSystemId;
	}
	public int getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public static ArrayList<NCCTransmittalDropDownOption> lookupTransmittalProcessBySystemId(int aSystemId) throws SQLException{
		Connection connection = null;
		ArrayList<NCCTransmittalDropDownOption> ttList = new ArrayList<NCCTransmittalDropDownOption>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(MAIN_PROCESS_SELECT_QUERY);
			ps.setInt(1, aSystemId);
			//ps.setInt(2, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while(rs.next())
					ttList.add(new NCCTransmittalDropDownOption(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt("id"), rs.getString("name"), 
														rs.getString("display_name"), rs.getInt("sort_order")));
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ttList;
	}
	
	public static NCCTransmittalDropDownOption lookupTransmittalProcessBySystemId(int aSystemId, int aId) throws SQLException{
		Connection connection = null;
		NCCTransmittalDropDownOption ntp = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(MAIN_PROCESS_SELECT_QUERY + " and id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
					ntp = new NCCTransmittalDropDownOption(rs.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt("id"), rs.getString("name"), 
														rs.getString("display_name"), rs.getInt("sort_order"));
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ntp;
	}
}
