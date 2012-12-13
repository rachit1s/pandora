package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnReplicateProcess;
import transmittal.com.tbitsGlobal.server.TransmittalUtils;

/**
 * Utility class for providing methods to replicate a transmittal process
 * 
 * @author devashish
 * 
 */
public class ReplicateProcess {

	/**
	 * Replicate the Given transmittal process for the specfied Businessa Area
	 * as the source ba
	 * 
	 * @param processParams
	 *            - The properties of the transmittal process which is
	 *            replicated and the new process fetched from user
	 * @param currentProcess
	 *            - Trasmittal Process that is replicated
	 * @param destProcessSrcBa
	 *            - The source business area of replicated process
	 * @param request
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnReplicateProcess> copyProcess(
			List<TrnReplicateProcess> processParams, TrnProcess currentProcess,
			BusinessAreaClient destProcessSrcBa, HttpServletRequest request) throws TbitsExceptionClient {

		HashMap<Integer, Integer> srcTargetDestBaMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> postTrnBaMap = new HashMap<Integer, Integer>();

		Integer destProcessId = null;
		Integer dropdownId = null;
		String dropdownName = null;
		Integer dropdownSortOrder = null;
		String processDesc = null;
		Integer dtrSysId = null;
		Integer dtnSysId = null;
		String maxKey = null;

		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			for (TrnReplicateProcess entry : processParams) {

				if (entry.getParamName().equals(TrnReplicateProcess.DEST_BA)) {
					Integer startIndex = entry.getParamValueOld().indexOf("[");
					Integer stopIndex = entry.getParamValueOld().indexOf("]");
					String sysDestBaId = entry.getParamValueOld().substring(
							(startIndex + 1), stopIndex);

					srcTargetDestBaMap.put(Integer.valueOf(sysDestBaId),
							Integer.valueOf(entry.getParamValueNew()));
				} else if (entry.getParamName().equals(
						TrnReplicateProcess.DEST_BA_POST_TRN)) {
					Integer startIndex = entry.getParamValueOld().indexOf("[");
					Integer stopIndex = entry.getParamValueOld().indexOf("]");
					String sysDestBaId = entry.getParamValueOld().substring(
							(startIndex + 1), stopIndex);

					postTrnBaMap.put(Integer.valueOf(sysDestBaId), Integer
							.valueOf(entry.getParamValueNew()));
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.PROCESS_ID)) {
					destProcessId = Integer.valueOf(entry.getParamValueNew());
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.DROPDOWN_ID)) {
					dropdownId = Integer.valueOf(entry.getParamValueNew());
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.DROPDOWN_NAME)) {
					dropdownName = entry.getParamValueNew();
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.DROPDOWN_SORT_ORDER)) {
					dropdownSortOrder = Integer.valueOf(entry
							.getParamValueNew());
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.PROCESS_DESC)) {
					processDesc = entry.getParamValueNew();
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.DTR_SYS_ID)) {
					dtrSysId = Integer.valueOf(entry.getParamValueNew());
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.DTN_SYS_ID)) {
					dtnSysId = Integer.valueOf(entry.getParamValueNew());
				} else if (entry.getParamName().contains(
						TrnReplicateProcess.MAX_KEY)) {
					maxKey = entry.getParamValueNew();
				}
			}

			insertIntoDropdownTable(connection, destProcessSrcBa.getSystemId(),
					dropdownId, dropdownName, dropdownSortOrder);

			insertIntoProcessesTable(connection,
					destProcessSrcBa.getSystemId(), destProcessId, processDesc,
					maxKey, dtnSysId, dtrSysId, dropdownId);

			if(!isMaxKeyExists(connection, maxKey))
			insertMaxId(connection, maxKey);

			copyTrnProcessParameters(connection, currentProcess.getProcessId(),
					destProcessSrcBa.getSystemId(), destProcessId);

			for (Integer destBaOld : postTrnBaMap.keySet()) {
				copyPostTrnFieldMapForTargetId(connection, request,
						currentProcess.getProcessId(), destBaOld,
						destProcessSrcBa.getSystemId(), destProcessId,
						postTrnBaMap.get(destBaOld));
			}

			for (Integer destBaOld : srcTargetDestBaMap.keySet()) {
				copySrcTargetFieldMap(connection, request, currentProcess
						.getProcessId(), currentProcess.getSrcBA()
						.getSystemId(), destBaOld, destProcessId,
						destProcessSrcBa.getSystemId(), srcTargetDestBaMap
								.get(destBaOld));
			}

			copyAttachmentSelectionTable(connection, request, currentProcess
					.getProcessId(), currentProcess.getSrcBA().getSystemId(),
					destProcessId, destProcessSrcBa.getSystemId());

			copyDistributionTableColConfig(connection, currentProcess
					.getProcessId(), destProcessId);

			copyValidationRulesTable(connection, currentProcess.getProcessId(),
					destProcessId);

			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			try {
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return processParams;
	}

	/**
	 * Insert into trn_dropdown table the entries for new transmittal process
	 * 
	 * @param connection
	 * @param srcSysId
	 *            - Source sys id of process
	 * @param id
	 *            - Dropdown id
	 * @param dropdownName
	 *            - Dropdown Name
	 * @param sortOrder
	 *            - Dropdown Sort Order
	 * @throws SQLException
	 */
	public static void insertIntoDropdownTable(Connection connection,
			Integer srcSysId, Integer id, String dropdownName, Integer sortOrder)
			throws SQLException {
		String sql = "insert into trn_dropdown "
				+ " (src_sys_id, id, name, sort_order) " + " values (?,?,?,?) ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcSysId);
		ps.setInt(2, id);
		ps.setString(3, dropdownName);
		ps.setInt(4, sortOrder);

		ps.execute();
		ps.close();
	}

	/**
	 * Insert the entry of the new process in the trn_processes_table
	 * 
	 * @param connection
	 * @param srcSysId
	 *            - source sysid of the new process
	 * @param trnProcessId
	 *            - transmittal process id of the new process
	 * @param description
	 *            - description of new process
	 * @param maxKey
	 *            - Max serial key of the new process
	 * @param dtnSysId
	 *            - DTN Sys Id
	 * @param dtrSysId
	 *            - DTR sys Id
	 * @param dropdownId
	 *            - Dropdown Id
	 * @throws SQLException
	 */
	public static void insertIntoProcessesTable(Connection connection,
			Integer srcSysId, Integer trnProcessId, String description,
			String maxKey, Integer dtnSysId, Integer dtrSysId,
			Integer dropdownId) throws SQLException {
		String sql = "insert into trn_processes "
				+ "(src_sys_id, trn_process_id, description, trn_max_sn_key, dtn_sys_id, dtr_sys_id, trn_dropdown_id) "
				+ " values(?,?,?,?,?,?,?) ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcSysId);
		ps.setInt(2, trnProcessId);
		ps.setString(3, description);
		ps.setString(4, maxKey);
		ps.setInt(5, dtnSysId);
		ps.setInt(6, dtrSysId);
		ps.setInt(7, dropdownId);

		ps.execute();
		ps.close();
	}

	/**
	 * Insert the max id string into the max_id table
	 * 
	 * @param connection
	 * @param id
	 *            - Max id to be inserted
	 * @throws SQLException
	 */
	public static void insertMaxId(Connection connection, String id)
			throws SQLException {
		String sql = "insert into max_ids " + " (name, id) " + " values(?,?)";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, id);
		ps.setInt(2, 0);

		ps.execute();
		ps.close();
	}

	/**
	 * Check if maxKey already exist
	 * 
	 * @param maxkey
	 * @return
	 */
	public static Boolean isMaxKeyExists(Connection connection, String max_key) {
		String sql = "select count(*) as count from max_ids where name = ? ";
		try {
			PreparedStatement ps;

			ps = connection.prepareStatement(sql);

			ps.setString(1, max_key);

			ResultSet rs = ps.executeQuery();
			int count = 0;

			if (null != rs) {
				while (rs.next()) {

					count = rs.getInt("count");
					if (count == 0) {
						return false;
					} else if (count >= 1) {
						return true;
					}
				}
			}
			ps.close();
		} catch (SQLException e) {

			e.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * Copy transmittal rule validation table from source process to destination
	 * process
	 * 
	 * @param connection
	 * @param srcTrnProcessId
	 *            - process from which the table is to be copied
	 * @param destTrnProcessId
	 *            - process into which the table is to be copied
	 * @throws SQLException
	 */
	private static void copyValidationRulesTable(Connection connection,
			Integer srcTrnProcessId, Integer destTrnProcessId)
			throws SQLException {
		String sql = "select * from trn_validation_rules where trn_process_id = ? ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcTrnProcessId);

		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				Integer fieldId = rs.getInt("field_id");
				String value = rs.getString("value");

				insertIntoValidationRules(connection, destTrnProcessId,
						fieldId, value);
			}
		}

		ps.close();
	}

	/**
	 * Insert an entry into Validation Rules Table for the new process
	 * 
	 * @param connection
	 * @param trnProcessId
	 *            - Process of of the new process
	 * @param fieldId
	 *            - Field for which the validation rule has to be applied
	 * @param value
	 *            - Value of the validation rule
	 * @throws SQLException
	 */
	public static void insertIntoValidationRules(Connection connection,
			Integer trnProcessId, Integer fieldId, String value)
			throws SQLException {
		String sql = "insert into trn_validation_rules "
				+ "(trn_process_id, field_id, value) " + "values(?,?,?)";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, trnProcessId);
		ps.setInt(2, fieldId);
		ps.setString(3, value);

		ps.execute();
		ps.close();
	}

	/**
	 * Copy the distribution table column config from the source process to
	 * destination
	 * 
	 * @param connection
	 * @param srcTrnProcessId
	 *            - Process whose table is to be copied
	 * @param destTrnProcessId
	 *            - Process in which the table is to be copied
	 * @throws SQLException
	 */
	private static void copyDistributionTableColConfig(Connection connection,
			Integer srcTrnProcessId, Integer destTrnProcessId)
			throws SQLException {
		List<TrnDistList> distributionTableColConfigList = new ArrayList<TrnDistList>();

		String sql = "select * from trn_distribution_table_column_config "
				+ " where trn_process_id = ? ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcTrnProcessId);
		ResultSet rs = ps.executeQuery();

		if (null != rs) {
			while (rs.next()) {
				TrnDistList entry = new TrnDistList();

				String name = rs.getString("name");
				String displayName = rs.getString("display_name");
				Integer dataTypeId = rs.getInt("data_type_id");
				String fieldConfig = rs.getString("field_config");
				boolean isEditable = rs.getBoolean("is_editable");
				boolean isActive = rs.getBoolean("is_active");
				Integer columnOrder = rs.getInt("column_order");

				entry.setProcessId(destTrnProcessId);
				entry.setName(name);
				entry.setDisplayName(displayName);
				entry.setDataType(dataTypeId);
				entry.setFieldConfig(fieldConfig);
				entry.setIsEditable(isEditable);
				entry.setIsActive(isActive);
				entry.setOrder(columnOrder);

				distributionTableColConfigList.add(entry);
			}
		}
		ps.close();

		for (TrnDistList entry : distributionTableColConfigList) {
			insertIntoDistributionTableColumnConfig(connection, entry);
		}
	}

	/**
	 * Insert an entry into Distribution Table for the new process
	 * 
	 * @param connection
	 * @param entry
	 *            - Entry to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoDistributionTableColumnConfig(
			Connection connection, TrnDistList entry) throws SQLException {
		String sql = "insert into trn_distribution_table_column_config "
				+ "(trn_process_id, name, display_name, data_type_id, field_config, is_editable, is_active, column_order) "
				+ "values(?,?,?,?,?,?,?,?) ";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, entry.getProcessId());
		ps.setString(2, entry.getName());
		ps.setString(3, entry.getDisplayName());
		ps.setInt(4, entry.getDataType());
		ps.setString(5, entry.getFieldConfig());
		if(entry.getIsEditable()==null)
			entry.setIsEditable(false);
		
		ps.setBoolean(6, entry.getIsEditable());
		
		if(entry.getIsActive()==null)
			entry.setIsActive(false);
		
		ps.setBoolean(7, entry.getIsActive());
		ps.setInt(8, entry.getOrder());

		ps.execute();
		ps.close();
	}

	/**
	 * Copy attachment selection table columns from source transmittal process
	 * to a destination process
	 * 
	 * @param connection
	 * @param request
	 * @param srcTrnProcessId
	 *            - Process whose attachment selection table is to be copied
	 * @param srcSysId
	 *            - Source sys id of the process
	 * @param destProcessId
	 *            - Process in which the table is to be copied
	 * @param destProcessSrcSysId
	 *            - Source sys id of the destination process
	 * @throws SQLException
	 * @throws DatabaseException
	 * @throws TbitsExceptionClient
	 */
	private static void copyAttachmentSelectionTable(Connection connection,
			HttpServletRequest request, Integer srcTrnProcessId,
			Integer srcSysId, Integer destProcessId, Integer destProcessSrcSysId)
			throws SQLException, DatabaseException, TbitsExceptionClient {
		User user;
		try {
			user = WebUtil.validateUser(request);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		List<TrnAttachmentList> attachmentSelectionList = new ArrayList<TrnAttachmentList>();

		String sql = "select * from trn_attachment_selection_table_columns "
				+ "where trn_process_id = ? ";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcTrnProcessId);

		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				TrnAttachmentList entry = new TrnAttachmentList();

				String property = rs.getString("name");
				Integer fieldId = rs.getInt("field_id");
				Integer dataTypeId = rs.getInt("data_type_id");
				String defaultValue = rs.getString("default_value");
				boolean isEditable = rs.getBoolean("is_editable");
				boolean isActive = rs.getBoolean("is_active");
				Integer columnOrder = rs.getInt("column_order");
				Integer typeValueSource = rs.getInt("type_value_source");
				boolean isIncluded = rs.getBoolean("is_included");

				Field field;
				try {
					// get the field in the source ba of the source process
					Field srcField = Field.lookupBySystemIdAndFieldId(srcSysId,
							fieldId);
					// lookup for that field in the source ba of the destination
					// process
					field = Field.lookupBySystemIdAndFieldName(
							destProcessSrcSysId, srcField.getName());

					BAField baField = GWTServiceHelper.fromField(field, user,
							null, BAField.class);
					if (null == baField)
						throw new TbitsExceptionClient(
								"[Attachment Selection Table Copy] \n"
										+ "Field ["
										+ fieldId.toString()
										+ "] does not exist for the new process Id : "
										+ destProcessId.toString());

					entry.setProcessId(destProcessId);
					entry.setName(property);
					entry.setField(baField);
					entry.setDataType(dataTypeId);
					entry.setDefaultValue(defaultValue);
					entry.setIsEditable(isEditable);
					entry.setIsActive(isActive);
					entry.setOrder(columnOrder);
					entry.setTypeValueSource(typeValueSource);
					entry.setIsIncluded(isIncluded);
					attachmentSelectionList.add(entry);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
			}
		}
		ps.close();

		for (TrnAttachmentList entry : attachmentSelectionList) {
			insertIntoAttachmentSelectionTable(connection, entry);
		}
	}

	/**
	 * Insert an entry into Attachment selection table for the new process
	 * 
	 * @param connection
	 * @param entry
	 *            - Entry to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoAttachmentSelectionTable(
			Connection connection, TrnAttachmentList entry) throws SQLException {
		String sql = "insert into trn_attachment_selection_table_columns "
				+ "(trn_process_id, name, field_id, data_type_id, default_value, is_editable, is_active, column_order, type_value_source, is_included) "
				+ " values (?,?,?,?,?,?,?,?,?,?) ";
		PreparedStatement ps = connection.prepareStatement(sql);

		ps.setInt(1, entry.getProcessId());
		ps.setString(2, entry.getName());
		ps.setInt(3, entry.getField().getFieldId());
		ps.setInt(4, entry.getDataType());
		ps.setString(5, entry.getDefaultValue());
		if(entry.getIsEditable()==null)
			entry.setIsEditable(false);
		ps.setBoolean(6, entry.getIsEditable());
		
		if(entry.getIsActive()==null)
			entry.setIsActive(false);
		ps.setBoolean(7, entry.getIsActive());
		ps.setInt(8, entry.getOrder());
		ps.setInt(9, entry.getTypeValueSource());
		
		if(entry.getIsIncluded()==null)
			entry.setIsIncluded(false);
		ps.setBoolean(10, entry.getIsIncluded());

		ps.execute();
		ps.close();

	}

	/**
	 * Copy source target field mapping from the specified process to the
	 * destination process
	 * 
	 * @param connection
	 * @param trnProcessId
	 *            - source process whose mapping is to be copied
	 * @param srcSysId
	 *            - source BA for source process
	 * @param srcTargetSysId
	 *            - target BA for source process
	 * @param destProcessId
	 *            - destination process in which the mapping is to be copied
	 * @param destSrcSysId
	 *            - source BA for destination process
	 * @param destTargetSysId
	 *            - target BA for destination process
	 * @throws DatabaseException
	 * @throws SQLException
	 * @throws TbitsExceptionClient
	 */
	private static void copySrcTargetFieldMap(Connection connection,
			HttpServletRequest request, Integer trnProcessId, Integer srcSysId,
			Integer srcTargetSysId, Integer destProcessId,
			Integer destSrcSysId, Integer destTargetSysId)
			throws DatabaseException, SQLException, TbitsExceptionClient {
		User user = User.lookupByUserLogin("root");
		try {
			user = WebUtil.validateUser(request);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		List<TrnFieldMapping> srcTargetFieldMapping = new ArrayList<TrnFieldMapping>();

		String sql = " select * from trn_src_target_field_mapping where "
				+ " trn_process_id = ? and target_sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, trnProcessId);
		ps.setInt(2, srcTargetSysId);

		ResultSet rs = ps.executeQuery();

		if (null != rs) {
			while (rs.next()) {
				TrnFieldMapping entry = new TrnFieldMapping();

				Integer srcFieldId = rs.getInt("src_field_id");
				Integer targetFieldId = rs.getInt("target_field_id");

				entry.setProcessId(destProcessId);
				entry.setSrcBA(TransmittalUtils.getBAforSysId(destSrcSysId));
				entry.setTargetBA(TransmittalUtils
						.getBAforSysId(destTargetSysId));

				Field srcField;
				Field targetField;

				try {
					// get the field for source ba for source process
					Field srcSrcField = Field.lookupBySystemIdAndFieldId(
							srcSysId, srcFieldId);
					// look for the field with that name in the srouce ba for
					// destination process
					srcField = Field.lookupBySystemIdAndFieldName(destSrcSysId,
							srcSrcField.getName());
					BAField srcBaField = GWTServiceHelper.fromField(srcField,
							user, null, BAField.class);
					if (null == srcBaField)
						throw new TbitsExceptionClient(
								"[Src Target Field Map Copy] \n"
										+ "Source Field ["
										+ srcFieldId.toString()
										+ "] does not exist in BA: "
										+ destSrcSysId
										+ "\nThe field is copied from BA: ["
										+ destTargetSysId
										+ "] from the source transmittal process.");

					// get the field for target ba for source process
					Field srcDestField = Field.lookupBySystemIdAndFieldId(
							srcTargetSysId, targetFieldId);
					// look for that field in the target ba for the destination
					// process
					targetField = Field.lookupBySystemIdAndFieldName(
							destTargetSysId, srcDestField.getName());
					BAField targetBaField = GWTServiceHelper.fromField(
							targetField, user, null, BAField.class);
					if (null == targetField)
						throw new TbitsExceptionClient(
								"[Src Target Field Map Copy] \n"
										+ "Destination Field ["
										+ targetFieldId.toString()
										+ "] does not exist in BA: "
										+ destTargetSysId
										+ "\nThe field is copied from BA: ["
										+ destTargetSysId
										+ "] from the source transmittal process.");

					entry.setSrcField(srcBaField);
					entry.setTargetField(targetBaField);

				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}
				srcTargetFieldMapping.add(entry);
			}
		}
		ps.close();

		for (TrnFieldMapping entry : srcTargetFieldMapping) {
			insertIntoSrcTargetFieldMap(connection, entry);
		}

	}

	/**
	 * Insert an entry into Source Target Field Mapping table for new process
	 * 
	 * @param connection
	 * @param entry
	 *            - entry to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoSrcTargetFieldMap(Connection connection,
			TrnFieldMapping entry) throws SQLException {
		String sql = "insert into trn_src_target_field_mapping "
				+ " (trn_process_id, src_sys_id, src_field_id, target_sys_id, target_field_id) "
				+ " values (?,?,?,?,?)";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, entry.getProcessId());
		ps.setInt(2, entry.getSrcBA().getSystemId());
		ps.setInt(3, entry.getSrcField().getFieldId());
		ps.setInt(4, entry.getTargetBA().getSystemId());
		ps.setInt(5, entry.getTargetField().getFieldId());

		ps.execute();
		ps.close();
		// System.out.println("inserted...");
	}

	/**
	 * Copy Post transmittal field map for the specified target ba in source
	 * transmittal process
	 * 
	 * @param connection
	 * @param request
	 * @param srcProcessId
	 *            - Process Id whose table is to be copied
	 * @param srcTargetId
	 *            - Target sys id of the srcProcessId
	 * @param destProcessSrcSysId
	 *            - Source sys id of the target process in which the table is
	 *            being copied
	 * @param destProcessId
	 *            - Id of the target transmittal process
	 * @param destProcessTargetId
	 *            - Target sys id of the destProcessId
	 * @throws SQLException
	 * @throws TbitsExceptionClient
	 * @throws DatabaseException
	 */
	private static void copyPostTrnFieldMapForTargetId(Connection connection,
			HttpServletRequest request, Integer srcProcessId,
			Integer srcTargetId, Integer destProcessSrcSysId,
			Integer destProcessId, Integer destProcessTargetId)
			throws SQLException, TbitsExceptionClient, DatabaseException {
		User user;
		try {
			user = WebUtil.validateUser(request);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}

		List<TrnPostProcessValue> postProcessMap = new ArrayList<TrnPostProcessValue>();

		String sql = "select * from trn_post_transmittal_field_values where "
				+ " trn_process_id = ? and target_sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcProcessId);
		ps.setInt(2, srcTargetId);
		ResultSet rs = ps.executeQuery();

		if (null != rs) {
			while (rs.next()) {
				TrnPostProcessValue entry = new TrnPostProcessValue();

				/*
				 * Field id in the new process in which values are to be copied
				 */
				Integer targetFieldId = rs.getInt("target_field_id");
				String targetFieldValue = rs.getString("target_field_value");

				entry.setProcessId(destProcessId);
				entry.setSrcBA(TransmittalUtils
						.getBAforSysId(destProcessSrcSysId));
				entry.setTargetBA(TransmittalUtils
						.getBAforSysId(destProcessTargetId));

				Field field;

				try {
					// get the field in the target ba of source process
					Field srcTargetField = Field.lookupBySystemIdAndFieldId(
							srcTargetId, targetFieldId);
					// look for a field with that name in the target ba of
					// destination process
					field = Field.lookupBySystemIdAndFieldName(
							destProcessTargetId, srcTargetField.getName());
					BAField baField = GWTServiceHelper.fromField(field, user,
							null, BAField.class);
					if (null == field)
						throw new TbitsExceptionClient(
								"[Post Trn Field Map Copy] \n"
										+ "Target Field ["
										+ targetFieldId.toString()
										+ "] does not exist in BA: "
										+ destProcessTargetId
										+ "\nThe field is copied from BA: ["
										+ srcTargetId
										+ "] from the source transmittal process.");

					entry.setTargetField(baField);
					entry.setTargetFieldValue(targetFieldValue);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TbitsExceptionClient(e);
				}

				postProcessMap.add(entry);
			}
		}
		ps.close();

		for (TrnPostProcessValue entry : postProcessMap) {
			insertIntoPostTrnFieldMap(connection, entry);
		}
	}

	/**
	 * Insert an entry into Post Transmittal Field Values table
	 * 
	 * @param connection
	 * @param entry
	 *            - Entry to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoPostTrnFieldMap(Connection connection,
			TrnPostProcessValue entry) throws SQLException {
		String sql = "insert into trn_post_transmittal_field_values "
				+ " (src_sys_id, trn_process_id, target_sys_id, target_field_id, target_field_value) "
				+ " values (?,?,?,?,?) ";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, entry.getSrcBA().getSystemId());
		ps.setInt(2, entry.getProcessId());
		ps.setInt(3, entry.getTargetBA().getSystemId());
		ps.setInt(4, entry.getTargetField().getFieldId());
		ps.setString(5, entry.getTargetFieldValue());
		ps.execute();
		ps.close();
	}

	/**
	 * Copy Transmittal process parameters from one process id to another from
	 * trn_process_params table
	 * 
	 * @param connection
	 * @param srcProcessId
	 *            - Id of the process whose parameters are to be copied
	 * @param destProcessSrcSysId
	 *            - Source sys id of the process to which the parameters are to
	 *            be copied
	 * @param destProcesId
	 *            - Process id of the process into which the parameters are to
	 *            be copied
	 * @return - true, if the parameters were copied successfully
	 * @throws SQLException
	 */
	private static void copyTrnProcessParameters(Connection connection,
			Integer srcProcessId, Integer destProcessSrcSysId,
			Integer destProcesId) throws SQLException {
		HashMap<String, String> processParams = new HashMap<String, String>();

		String sql = "select * from trn_process_parameters "
				+ " where trn_process_id = ? ";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcProcessId);

		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				String parameter = rs.getString("parameter");
				String value = rs.getString("value");
				processParams.put(parameter, value);
			}
		}
		ps.close();

		for (String param : processParams.keySet()) {
			insertIntoTrnProcessParameters(connection, destProcessSrcSysId,
					destProcesId, param, processParams.get(param));
		}
	}

	/**
	 * Insert into Transmittal Process Parameters table the values of new
	 * transmittal process
	 * 
	 * @param connection
	 * @param srcSysId
	 *            - Source Sys id of the new transmittal process
	 * @param trnProcessId
	 *            - Process ID of th new transmittal process
	 * @param param
	 *            - Parameter to be inserted
	 * @param value
	 *            - Value of the parameter
	 * @throws SQLException
	 */
	public static void insertIntoTrnProcessParameters(Connection connection,
			Integer srcSysId, Integer trnProcessId, String param, String value)
			throws SQLException {
		String sql2 = "insert into trn_process_parameters "
				+ "(src_sys_id, trn_process_id, parameter, value) "
				+ " values(?,?,?,?) ";
		PreparedStatement ps = connection.prepareStatement(sql2);
		ps.setInt(1, srcSysId);
		ps.setInt(2, trnProcessId);
		ps.setString(3, param);
		ps.setString(4, value);

		ps.execute();
		ps.close();
	}

	/**
	 * Return all the parameters associated with the current process
	 * 
	 * @param process
	 * @param srcBa
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnReplicateProcess> getProcessParams(
			TrnProcess process, BusinessAreaClient srcBa)
			throws TbitsExceptionClient {
		List<TrnReplicateProcess> processParams = new ArrayList<TrnReplicateProcess>();

		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			TrnReplicateProcess processIdEntry = new TrnReplicateProcess();
			processIdEntry.setParamName(TrnReplicateProcess.PROCESS_ID);
			processIdEntry.setParamValueNew("");
			processIdEntry.setParamValueOld(Integer.toString(process
					.getProcessId()));
			processIdEntry.setParamValueNew(Integer
					.toString(getMaxIdTrnProcess(connection)));
			processParams.add(processIdEntry);

			processParams.addAll(getDestSysIdsForSrcTargetFieldMap(connection,
					process));

			processParams.addAll(getDestSysIdsForPostTrnFieldMap(connection,
					process));

			processParams
					.addAll(getParamsFromTrnProcesses(connection, process));

		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
			try {
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return processParams;
	}

	private static Integer getMaxIdTrnProcess(Connection connection)
			throws SQLException {
		String sql = "select max(trn_process_id) from trn_processes";
		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				Integer maxId = rs.getInt(1);
				maxId += 1;
				return maxId;
			}
		}
		return 1;
	}

	/**
	 * Get all the process parameters from trn_processes table
	 * 
	 * @param connection
	 * @param process
	 * @return
	 * @throws SQLException
	 */
	private static List<TrnReplicateProcess> getParamsFromTrnProcesses(
			Connection connection, TrnProcess process) throws SQLException {
		List<TrnReplicateProcess> paramsList = new ArrayList<TrnReplicateProcess>();

		String sql = "select trn_dropdown_id, dtr_sys_id, dtn_sys_id, description, trn_max_sn_key "
				+ " from trn_processes where trn_process_id = ? ";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, process.getProcessId());
		ResultSet rs = ps.executeQuery();

		if (null != rs) {
			while (rs.next()) {
				Integer dropdownId = rs.getInt("trn_dropdown_id");
				Integer dtrSysId = rs.getInt("dtr_sys_id");
				Integer dtnSysId = rs.getInt("dtn_sys_id");
				String description = rs.getString("description");
				String maxSnKey = rs.getString("trn_max_sn_key");

				TrnReplicateProcess dropdownEntry = new TrnReplicateProcess();
				dropdownEntry.setParamName(TrnReplicateProcess.DROPDOWN_ID);
				dropdownEntry.setParamValueNew("");
				dropdownEntry.setParamValueOld(dropdownId.toString());
				dropdownEntry.setParamValueNew(Integer
						.toString(getMaxIdTrnDropdown(connection)));
				paramsList.add(dropdownEntry);
				paramsList.addAll(getParamsFromDropdown(connection, process
						.getSrcBA().getSystemId(), dropdownId));

				TrnReplicateProcess descriptionEntry = new TrnReplicateProcess();
				descriptionEntry.setParamName(TrnReplicateProcess.PROCESS_DESC);
				descriptionEntry.setParamValueNew("");
				descriptionEntry.setParamValueOld(description);
				paramsList.add(descriptionEntry);

				if ((null == TransmittalUtils.getBAforSysId(dtrSysId))
						|| (null == TransmittalUtils.getBAforSysId(dtnSysId)))
					continue;

				TrnReplicateProcess dtrSysIdEntry = new TrnReplicateProcess();
				dtrSysIdEntry.setParamName(TrnReplicateProcess.DTR_SYS_ID);
				dtrSysIdEntry.setParamValueNew("");
				dtrSysIdEntry.setParamValueOld(TransmittalUtils.getBAforSysId(
						dtrSysId).getSystemPrefix()
						+ " [" + dtrSysId.toString() + "]");
				paramsList.add(dtrSysIdEntry);

				TrnReplicateProcess dtnSysIdEntry = new TrnReplicateProcess();
				dtnSysIdEntry.setParamName(TrnReplicateProcess.DTN_SYS_ID);
				dtnSysIdEntry.setParamValueNew("");
				dtnSysIdEntry.setParamValueOld(TransmittalUtils.getBAforSysId(
						dtnSysId).getSystemPrefix()
						+ " [" + dtnSysId.toString() + "]");
				paramsList.add(dtnSysIdEntry);

				TrnReplicateProcess maxKeyEntry = new TrnReplicateProcess();
				maxKeyEntry.setParamName(TrnReplicateProcess.MAX_KEY);
				maxKeyEntry.setParamValueNew("");
				maxKeyEntry.setParamValueOld(maxSnKey);
				paramsList.add(maxKeyEntry);
			}
		}
		ps.close();

		return paramsList;
	}

	private static Integer getMaxIdTrnDropdown(Connection connection)
			throws SQLException {
		String sql = "select max(id) from trn_dropdown";
		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				Integer maxDropdownId = rs.getInt(1);
				maxDropdownId += 1;
				return maxDropdownId;
			}
		}
		return 1;
	}

	/**
	 * Fetch all the parameters from trn_dropdown table
	 * 
	 * @param connection
	 * @param dropdownId
	 * @return
	 * @throws SQLException
	 */
	private static List<TrnReplicateProcess> getParamsFromDropdown(
			Connection connection, Integer srcSysId, Integer dropdownId)
			throws SQLException {
		List<TrnReplicateProcess> paramsList = new ArrayList<TrnReplicateProcess>();

		String sql = "select * from trn_dropdown where src_sys_id = ? and id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, srcSysId);
		ps.setInt(2, dropdownId);

		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				String dropdownName = rs.getString("name");
				Integer sortOrder = rs.getInt("sort_order");

				TrnReplicateProcess dropdownNameEntry = new TrnReplicateProcess();
				dropdownNameEntry
						.setParamName(TrnReplicateProcess.DROPDOWN_NAME);
				dropdownNameEntry.setParamValueNew("");
				dropdownNameEntry.setParamValueOld(dropdownName);
				paramsList.add(dropdownNameEntry);

				TrnReplicateProcess sortOrderEntry = new TrnReplicateProcess();
				sortOrderEntry
						.setParamName(TrnReplicateProcess.DROPDOWN_SORT_ORDER);
				sortOrderEntry.setParamValueNew("");
				sortOrderEntry.setParamValueOld(sortOrder.toString());
				paramsList.add(sortOrderEntry);
			}
		}
		ps.close();
		return paramsList;
	}

	private static List<TrnReplicateProcess> getDestSysIdsForPostTrnFieldMap(
			Connection connection, TrnProcess process) throws SQLException {
		List<TrnReplicateProcess> destSysIdList = new ArrayList<TrnReplicateProcess>();
		String sql = "select distinct target_sys_id from trn_post_transmittal_field_values where "
				+ "trn_process_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, process.getProcessId());

		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				TrnReplicateProcess entry = new TrnReplicateProcess();
				Integer targetSysId = rs.getInt("target_sys_id");
				entry.setParamName(TrnReplicateProcess.DEST_BA_POST_TRN);
				entry.setParamValueOld(TransmittalUtils.getBAforSysId(
						targetSysId).getSystemPrefix()
						+ " [" + targetSysId.toString() + "]");
				entry.setParamValueNew("");
				destSysIdList.add(entry);
			}
		}
		ps.close();

		return destSysIdList;
	}

	/**
	 * Get the destination business areas for selected transmittal process from
	 * trn_src_target_field_mapping table
	 * 
	 * @param connection
	 * @param process
	 * @return list of destination business areas
	 * @throws SQLException
	 */
	private static List<TrnReplicateProcess> getDestSysIdsForSrcTargetFieldMap(
			Connection connection, TrnProcess process) throws SQLException {
		List<TrnReplicateProcess> destSysIdList = new ArrayList<TrnReplicateProcess>();

		String sql = "select distinct target_sys_id from trn_src_target_field_mapping "
				+ " where trn_process_id = ? ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, process.getProcessId());

		ResultSet rs = ps.executeQuery();
		if (null != rs) {
			while (rs.next()) {
				TrnReplicateProcess entry = new TrnReplicateProcess();
				Integer targetSysId = rs.getInt("target_sys_id");
				entry.setParamName(TrnReplicateProcess.DEST_BA);
				entry.setParamValueOld(TransmittalUtils.getBAforSysId(
						targetSysId).getSystemPrefix()
						+ " [" + targetSysId.toString() + "]");
				entry.setParamValueNew("");
				destSysIdList.add(entry);
			}
		}
		ps.close();

		return destSysIdList;
	}

	public static void main(String[] args) throws TbitsExceptionClient {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			// copyTrnProcessParameters(connection, 7, 100, 110);
			// copyPostTrnFieldMap(connection, 7, 5, 4, 5, 5);
			// copySrcTargetFieldMap(connection, 45, 56, 30, 45, 56, 30);
			// copyAttachmentSelectionTable(connection, 7, 5, 99);
			// copyValidationRulesTable(connection, 7, 99);
			// System.out.println(getMaxIdTrnProcess(connection) + "");
			connection.commit();
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
	}
}
