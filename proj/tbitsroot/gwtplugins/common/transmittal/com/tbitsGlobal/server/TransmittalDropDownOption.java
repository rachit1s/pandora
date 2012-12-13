/**
 * 
 */
package transmittal.com.tbitsGlobal.server;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * @author lokesh
 * 
 */
public class TransmittalDropDownOption {
	int dcrSystemId, id, sortOrder;
	String name;

	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	private static final String MAIN_DROP_DOWN_SELECT_QUERY = "select * from  trn_dropdown where src_sys_id=?";
	private static final String DROP_DOWN_SELCTION_FOR_KEY1 = "select a.sys_id,a.trn_process_id,a.field_id,a.value,a.parameter,b.display_name"
			+ " from trn_processkey_field a,types b "
			+ "where a.sys_id=? and a.trn_process_id=?and a.field_id=? and a.sys_id=b.sys_id and a.field_id =b.field_id and a.type_id =b.type_id";

	static String DROP_DOWN_SELCTION_FOR_KEY = "select b.src_sys_id,a.trn_process_id,a.field_id,a.type_id,a.value,t.display_name,t.name"
			+ " from trn_processkey_field a join trn_processes b "
			+ " on a.trn_process_id=b.trn_process_id "
			+ " join  types t "
			+  "on t.type_id=a.type_id and t.sys_id=b.src_sys_id and a.field_id=t.field_id and t.sys_id=? and t.field_id=? "
			+ " and a.trn_process_id=?";

	public TransmittalDropDownOption(int dcrSystemId, int id, String name,
			int sortOrder) {
		this.dcrSystemId = dcrSystemId;
		this.id = id;
		this.name = name;
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

	public static ArrayList<TransmittalDropDownOption> lookupTransmittalDropDownOptionsBySystemId(
			int aSystemId) throws SQLException {
		Connection connection = null;
		ArrayList<TransmittalDropDownOption> ttList = new ArrayList<TransmittalDropDownOption>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement(MAIN_DROP_DOWN_SELECT_QUERY);
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while (rs.next())
					ttList.add(new TransmittalDropDownOption(rs
							.getInt(TransmittalUtils.SRC_SYS_ID), rs
							.getInt("id"), rs.getString("name"), rs
							.getInt("sort_order")));
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ttList;
	}

	public static TransmittalDropDownOption lookupTransmittalDropDownBySystemIdAndDropdownId(
			int aSystemId, int dropdownid) throws SQLException {
		Connection connection = null;
		TransmittalDropDownOption ntp = null;
		try {
			connection = DataSourcePool.getConnection();
			ntp = lookupTrnDropdownOptionByDropdownId(connection, aSystemId,
					dropdownid);

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ntp;
	}

	public static TransmittalDropDownOption lookupTransmittalDropDownByProcessId(
			int aSystemId, int processId) throws SQLException {
		Connection connection = null;
		TransmittalDropDownOption ntp = null;
		try {
			connection = DataSourcePool.getConnection();
			ntp = lookupTrnDropdownOptionByProcessId(connection, aSystemId,
					processId);

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ntp;
	}

	/**
	 * @param connection
	 * @param aSystemId
	 * @param dropdownid
	 * @param ntp
	 * @return
	 * @throws SQLException
	 */
	public static TransmittalDropDownOption lookupTrnDropdownOptionByDropdownId(
			Connection connection, int aSystemId, int dropdownid)
			throws SQLException {
		TransmittalDropDownOption ntp = null;
		PreparedStatement ps = connection
				.prepareStatement("select * from  trn_dropdown where id=?");
		;
		Integer ddid = null;
		ps.setInt(1, dropdownid);
		ResultSet rs = ps.executeQuery();
		if ((rs != null) && rs.next())
			ntp = new TransmittalDropDownOption(rs
					.getInt(TransmittalUtils.SRC_SYS_ID), rs.getInt("id"), rs
					.getString("name"), rs.getInt("sort_order"));

		// PreparedStatement ps1 =
		// connection.prepareStatement("select * from  trn_dropdown where id="+
		// ddid.toString());

		rs.close();
		ps.close();
		return ntp;
	}

	/**
	 * @param connection
	 * @param aSystemId
	 * @param aId
	 * @param ntp
	 * @return
	 * @throws SQLException
	 */
	public static TransmittalDropDownOption lookupTrnDropdownOptionByProcessId(
			Connection connection, int aSystemId, int aId) throws SQLException {
		TransmittalDropDownOption ntp = null;
		PreparedStatement ps = connection
				.prepareStatement("select trn_dropdown_id from  trn_processes where trn_process_id=?");
		Integer ddid = null;
		ps.setInt(1, aId);
		ResultSet rs = ps.executeQuery();
		if (rs != null)
			while (rs.next()) {
				ddid = rs.getInt("trn_dropdown_id");
			}

		PreparedStatement ps1 = connection
				.prepareStatement("select * from  trn_dropdown where id="
						+ ddid.toString());
		ResultSet rs1 = ps1.executeQuery();
		if ((rs1 != null) && rs1.next())
			ntp = new TransmittalDropDownOption(rs1
					.getInt(TransmittalUtils.SRC_SYS_ID), rs1.getInt("id"), rs1
					.getString("name"), rs1.getInt("sort_order"));
		rs.close();
		ps.close();
		return ntp;
	}

	public static ArrayList<TbitsModelData> getTransmittalDropdownOtionsForkey(
			int aSystemId, int trnProcessId, int fieldId) throws SQLException {
		Connection connection = null;
		ArrayList<TbitsModelData> ttList = new ArrayList<TbitsModelData>();
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement(DROP_DOWN_SELCTION_FOR_KEY);
			ps.setInt(1, aSystemId);
			ps.setInt(2, fieldId);
			ps.setInt(3, trnProcessId);

			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					TbitsModelData tmd = new TbitsModelData();
					tmd.set("sys_id", rs.getInt("src_sys_id"));
					tmd.set("trn_process_id", rs.getInt("trn_process_id"));
					tmd.set("field_id", rs.getInt("field_id"));
					tmd.set("value", rs.getString("value"));
					tmd.set("display_name", rs.getString("display_name"));
					tmd.set("name", rs.getString("name"));
					ttList.add(tmd);
				}
			}
			rs.close();
			ps.close();

		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG
							.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		return ttList;
	}

}
