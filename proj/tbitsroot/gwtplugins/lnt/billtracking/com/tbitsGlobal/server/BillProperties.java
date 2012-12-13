package billtracking.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;

import transbit.tbits.common.DataSourcePool;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

public class BillProperties implements IBillConstants, IState, IBillProperties {

	public static HashMap<String, String> billProperties;
	static {
		loadBillProperties();
	}

	private static void loadBillProperties() {
		try {
			billProperties = new HashMap<String, String>();
			Connection con = DataSourcePool.getConnection();
			PreparedStatement ps;
			ps = con.prepareStatement("Select key_data,value_data from plugins_bill_properties");
			ResultSet rs = ps.executeQuery();
			while (rs != null && rs.next() != false) {
				String keyData = Utils.fieldTrimmer(rs.getString("key_data"));
				String valueData = rs.getString("value_data");
				billProperties.put(keyData, valueData);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String get(String propertyName) {
		return billProperties.get(propertyName);

	}

	public static HashMap<String, String> getBillProperties() {
		loadBillProperties();
		return billProperties;

	}

	public static void insertstateFlowRow(Connection aCon, String pluginName,
			String processId, String stateId, String keyData, String valueData) {
		PreparedStatement ps;
		try {
			ps = aCon
					.prepareStatement("INSERT INTO "
							+ process_table_Name
							+ "(plugin_name,process_id,state_id,key_data,value_data) VALUES  (?,?,?,?,?);");
			ps.setString(1, pluginName);
			ps.setString(2, processId);
			ps.setString(3, stateId);
			ps.setString(4, keyData);
			ps.setString(5, valueData);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void updatestateFlowRow(Connection aCon, String processId,
			String stateId, String keyData, String valueData) {
		PreparedStatement ps;
		try {
			ps = aCon
					.prepareStatement("update "
							+ process_table_Name
							+ " set value_data=? where process_id=? and state_id=? and key_data=?");
			ps.setString(1, valueData);
			ps.setString(2, processId);
			ps.setString(3, stateId);
			ps.setString(4, keyData);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		// try {

		BillProperties.loadBillProperties();
		System.out.println(BillProperties.get(PROPERTY_TOTAL_DURATION));
		// String
		// input="Project                                          ";
		// System.out.println(Utils.fieldTrimmer(input));
		// generateEmptyTableStructure();

		// } catch (SQLException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private static void generateEmptyTableStructure() throws SQLException {
		Connection con = DataSourcePool.getConnection();
		// con.prepareStatement("delete from "+process_table_Name).execute();
		// con.prepareStatement("delete from "+project_table_Name).execute();

		for (int j = 2; j < 131; j++)
			for (int i = 1; i < 12; i++) {
				insertstateData4aProcess4astate(con, Integer.toString(j),
						Integer.toString(i));
			}
		con.close();
	}

	private static void insertstateData4aProcess4astate(Connection con,
			String processId, String stateId) {

		HashMap<String, String> stateMap = new HashMap<String, String>();
		stateMap.put(State.state_duration, "");
		stateMap.put(state_Decision_Field_Name, "");
		stateMap.put(State.next_state_id, "");
		stateMap.put(prev_state_id, "");
		stateMap.put(reject_state_id, "");
		stateMap.put(is_state_decision, "");
		stateMap.put(state_usertype_field_names, "");
		stateMap.put(state_usertype_field_values, "");
		stateMap.put(state_target_date, "");
		stateMap.put(state_pending_with, "");
		stateMap.put(state_Dep_Receipt_Date, "");
		stateMap.put(state_Dep_Acknowledge_Date, "");
		stateMap.put(state_attachment_ids, "");

		Enumeration<String> keys = (Enumeration<String>) stateMap.keySet();
		while (keys.hasMoreElements()) {
			String keyData = keys.nextElement();
			String valueData = stateMap.get(keyData);
			insertstateFlowRow(con, "lntBillTracking", processId, stateId,
					keyData, valueData);
		}

	}

}
