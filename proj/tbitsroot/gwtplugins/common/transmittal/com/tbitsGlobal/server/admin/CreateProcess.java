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

import transbit.tbits.common.DataSourcePool;
import transmittal.com.tbitsGlobal.client.admin.wizard.CreateProcessConstants;
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnCreateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;
import transmittal.com.tbitsGlobal.client.models.TrnReplicateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnSaveCreateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

/**
 * Class to provide methods for Create Process page
 * @author devashish
 *
 */
public class CreateProcess {
	
	
	private static String TRN_PROCESS			= "Overview";
	private static String TRN_PROCESS_PARAMS 	= "Parameters";
	private static String SRC_TARGET_FIELD_MAP	= "Source Target Field Mapping";
	private static String POST_TRN_FIELD_MAP	= "Post Transmittal Field Map";
	private static String VALIDATION_RULES		= "Validation Rules";
	
	/**
	 * Save all the values for a transmittal process into database.
	 */
	public static boolean saveNewProcessValues(TrnSaveCreateProcess values) throws TbitsExceptionClient{
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			saveValuesPage1(values.getValuesPage1(), connection);
			
			saveValuesPage2(values.getValuesPage2(), connection);
			
			saveValuesPage3(values.getValuesPage3(), connection);
			
			saveValuesPage4(values.getValuesPage4(), connection);
			
			saveValuesPage5(values.getValuesPage5(), connection);
			
			saveValuesPage6(values.getValuesPage6(), connection);
			
			saveValuesPage7(values.getValuesPage7());
			
			saveValuesPage8(values.getValuesPage8(), connection);
			
			connection.commit();
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
		}
		
		return true;
	}
	
	
	/**
	 * Save the values for page8 of the 'Create Process' wizard
	 * @param values	- values to be saved in validation_rules table
	 * @return	values	- values that have been saved and status, if any
	 * @throws TbitsExceptionClient
	 * @throws SQLException 
	 */
	private static List<TrnValidationRule> saveValuesPage8(List<TrnValidationRule> values, Connection connection) throws TbitsExceptionClient, SQLException{
			if(values.size()==0)
				return null;
		for(TrnValidationRule entry : values){
			ReplicateProcess.insertIntoValidationRules(connection, entry.getProcess().getProcessId(), entry.getField().getFieldId(), entry.getRule());
		}
			
		return values;
	}
	
	/**
	 * Save the values for page 7 of the 'Create Transmittal' wizard
	 * @param values	- values to be saved in drawing_number table
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static List<TrnDrawingNumber> saveValuesPage7(List<TrnDrawingNumber> values) throws TbitsExceptionClient{
	
		if(values.size()==0)
			return null;
		
		return DrawingNumberField.saveDrawingNumberFields(values);
	}
	
	/**
	 * Save the values for page 6 of the 'Create Transmittal' wizard
	 * @param values	- Values to be saved in distribution_table_column_config table
	 * @return			- values that have been saved and status, if any
	 * @throws TbitsExceptionClient
	 * @throws SQLException 
	 */
	private static List<TrnDistList> saveValuesPage6(List<TrnDistList> values, Connection connection) throws TbitsExceptionClient, SQLException{
			
		for(TrnDistList entry : values){
			ReplicateProcess.insertIntoDistributionTableColumnConfig(connection, entry);
		}
			
		return values;
	}
	
	
	/**
	 * Save the values for Page 5 of the 'Create Transmittal Wizard'
	 * @param values 	- values to be saved in attachemt_selection_table_columns table
	 * @return values	- values that have been saved and status, if any
	 * @throws TbitsExceptionClient
	 * @throws SQLException 
	 */
	private static List<TrnAttachmentList> saveValuesPage5(List<TrnAttachmentList> values, Connection connection) throws TbitsExceptionClient, SQLException{

		for(TrnAttachmentList entry : values){
			ReplicateProcess.insertIntoAttachmentSelectionTable(connection, entry);
		}
			
		return values;
	}
	
	/**
	 * Save values for page 4 of 'Create Process' Wizard
	 * @param values - List of values to be saved in src_target_field_mapping table
	 * @return	- saved values and status, if any
	 * @throws TbitsExceptionClient
	 * @throws SQLException 
	 */
	private static List<TrnFieldMapping> saveValuesPage4(List<TrnFieldMapping> values, Connection connection) throws TbitsExceptionClient, SQLException{
		
		for(TrnFieldMapping entry : values){
			ReplicateProcess.insertIntoSrcTargetFieldMap(connection, entry);
		}
		return values;
	}
	
	
	/**
	 * Save the values for page 2 of 'Create Process' wizard
	 * @param values - List of values to be inserted in post_trn_field_map
	 * @return	values - list of saved values and return status, if any
	 * @throws TbitsExceptionClient
	 * @throws SQLException 
	 */
	private static List<TrnPostProcessValue> saveValuesPage3(List<TrnPostProcessValue> values, Connection connection) throws TbitsExceptionClient, SQLException{
			
		for(TrnPostProcessValue entry : values){
			ReplicateProcess.insertIntoPostTrnFieldMap(connection, entry);
		}
		return values;
	}
	
	
	/**
	 * Save the values for page 2 of 'Create Transmittal' wizard
	 * @param values	- values in trn_process_param table
	 * @param request	- current request
	 * @return			- List of saved values, with result status, if any
	 * @throws TbitsExceptionClient
	 * @throws SQLException 
	 */
	private static List<TrnProcessParam> saveValuesPage2(List<TrnProcessParam> values, Connection connection) throws TbitsExceptionClient, SQLException{
			
		for(TrnProcessParam entry : values){
			Integer trnProcessId = entry.getProcessId();
			Integer srcSysId 	 = entry.getSrcBA().getSystemId();
			String param 		 = entry.getName();
			String value 		 = entry.getValue();
			
			ReplicateProcess.insertIntoTrnProcessParameters(connection, srcSysId, trnProcessId, param, value);
		}
		
		return values;
	}
	
	
	/**
	 * Save the values for page one
	 * @param values
	 * @param reques
	 * @return
	 * @throws TbitsExceptionClient 
	 * @throws SQLException 
	 */
	private static HashMap<String, String> saveValuesPage1(HashMap<String, String> values, Connection connection) throws TbitsExceptionClient, SQLException{
			
		Integer srcSysId	= Integer.valueOf(values.get(CreateProcessConstants.SRC_BA));
		Integer trnProcessId= Integer.valueOf(values.get(CreateProcessConstants.TRN_PROCESS_ID));
		Integer dropdownId	= Integer.valueOf(values.get(CreateProcessConstants.TRN_DROPDOWN_ID));
		String description 	= values.get(CreateProcessConstants.TRN_PROCESS_DESC);
		String maxKey 		= values.get(CreateProcessConstants.TRN_MAX_KEY);
		Integer dtnSysId 	= Integer.valueOf(values.get(CreateProcessConstants.DTN_BA));
		Integer dtrSysId	= Integer.valueOf(values.get(CreateProcessConstants.DTR_BA));
		
		
		ReplicateProcess.insertIntoProcessesTable(connection, srcSysId, trnProcessId, description, maxKey, dtnSysId, dtrSysId, dropdownId);
		
		String dropdownName = values.get(CreateProcessConstants.TRN_DROPDOWN_NAME);
		Integer sortOrder 	= Integer.valueOf(values.get(CreateProcessConstants.TRN_DROPDOWN_SORT_ORDER));
		
		ReplicateProcess.insertIntoDropdownTable(connection, srcSysId, dropdownId, dropdownName, sortOrder);
		
		
		return values;
	}
	
	
	/**
	 * Get the maximum value of Transmittal Process Id
	 * @param connection
	 * @return max value of transmittal process id
	 * @throws TbitsExceptionClient 
	 */
	public static Integer getMaxIdTrnProcess() throws TbitsExceptionClient{
		
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
		
			String sql = "select max(trn_process_id) from trn_processes";
			PreparedStatement ps;
			
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					Integer maxId = rs.getInt(1);
					maxId+=1;
					return maxId;
				}
			}
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return 1;
	}
	
	/**
	 * Get the maximum value of dropdown Id
	 * @param connection
	 * @return Max Value of transmittal dropdown Id
	 * @throws TbitsExceptionClient 
	 */
	public static Integer getMaxIdTrnDropdown() throws TbitsExceptionClient{
		
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select max(id) from trn_dropdown";
			PreparedStatement ps;
			
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();	
			if(null != rs){
				while(rs.next()){
					Integer maxDropdownId = rs.getInt(1);
					maxDropdownId+=1;
					return maxDropdownId;
				}
			}
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return 1;
	}
	
	/**
	 * Get all the values of all the parameters associated with the specified process
	 * @param process - whose values are to be fetched
	 * @param request
	 * @return - List containing all the values
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnCreateProcess> getAllProcessParams(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
		List<TrnCreateProcess> paramsList = new ArrayList<TrnCreateProcess>();
		
		paramsList.addAll(getProcessParams(process));
		
		paramsList.addAll(getTrnProcessParams(process));
		
		paramsList.addAll(getSrcTargetFieldMapParams(process, request));
		
		paramsList.addAll(getPostTrnFieldParams(process, request));
		
		paramsList.addAll(getValidationRules(process, request));
		
		return paramsList;
	}
	
	/**
	 * Get validation rules map
	 * @param process
	 * @param request
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static List<TrnCreateProcess> getValidationRules(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
		List<TrnCreateProcess> validationRuleParams = new ArrayList<TrnCreateProcess>();
		
		List<TrnValidationRule> validationRulesMap = new ArrayList<TrnValidationRule>(ValidationRules.getValidationRulesForProcess(process, request));
		for(TrnValidationRule param : validationRulesMap){
			TrnCreateProcess entry = new TrnCreateProcess();
			entry.setGroup(VALIDATION_RULES);
			entry.setName(param.getField().getDisplayName() + " <" + param.getField().getFieldId() + ">");
			entry.setValue(param.getRule());
			
			validationRuleParams.add(entry);
			
		}
		return validationRuleParams;
	}
	
	
	/**
	 * Get the post transmittal field value map
	 * @param process
	 * @param request
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static List<TrnCreateProcess> getPostTrnFieldParams(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
		List<TrnCreateProcess> postTrnFieldMapParams = new ArrayList<TrnCreateProcess>();
		
		List<TrnPostProcessValue> postTrnFieldMap = new ArrayList<TrnPostProcessValue>(PostTrnFieldMap.getPostProcessFieldValues(process, request));
		for(TrnPostProcessValue entry : postTrnFieldMap){
			TrnCreateProcess paramEntry = new TrnCreateProcess();
			paramEntry.setGroup(POST_TRN_FIELD_MAP);
			
			String targetBAField = "[ " + entry.getTargetBA().getSystemPrefix() + " <" + entry.getTargetBA().getSystemId() + "> ]  [ " + entry.getTargetField().getName() + "<" + entry.getTargetField().getFieldId() + "> ]";
			paramEntry.setName(targetBAField);
			
			paramEntry.setValue(entry.getTargetFieldValue());
			
			postTrnFieldMapParams.add(paramEntry);
	
		}
		return postTrnFieldMapParams;
	}
	
	/**
	 * Get source target field map properties
	 * @param process
	 * @param request
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static List<TrnCreateProcess> getSrcTargetFieldMapParams(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
		List<TrnCreateProcess> srcTargetFieldMapParams = new ArrayList<TrnCreateProcess>();
		
		List<TrnFieldMapping> srcTargetFieldMap = new ArrayList<TrnFieldMapping>(SrcTargetFieldMap.getSrcTargetFieldMap(process, request));
		for(TrnFieldMapping entry : srcTargetFieldMap){
			TrnCreateProcess paramEntry = new TrnCreateProcess();
			
			paramEntry.setGroup(SRC_TARGET_FIELD_MAP);
			String srcBaField = "[ " + entry.getSrcBA().getSystemPrefix() + " <" + entry.getSrcBA().getSystemId() + "> ]  [ " + entry.getSrcField().getName()+ " <" + entry.getSrcField().getFieldId() + "> ]";
			paramEntry.setName(srcBaField);
			String targetBaField = "[ " + entry.getTargetBA().getSystemPrefix() + "<" + entry.getTargetBA().getSystemId() + "> ]  [ " + entry.getTargetField().getName() + " <" + entry.getTargetField().getFieldId() + "> ]";
			paramEntry.setValue(targetBaField);
			
			srcTargetFieldMapParams.add(paramEntry);
		}
		
		return srcTargetFieldMapParams;
	}
	
	
	/**
	 * Get transmittal Process Parameters
	 * @param process
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static List<TrnCreateProcess> getTrnProcessParams(TrnProcess process) throws TbitsExceptionClient{
		List<TrnCreateProcess> trnProcessParamsList = new ArrayList<TrnCreateProcess>();
		
		List<TrnProcessParam> processParamsList = new ArrayList<TrnProcessParam>(TrnProcessParams.getProcessParams(process));
		for(TrnProcessParam entry : processParamsList){
			TrnCreateProcess paramEntry = new TrnCreateProcess();
			paramEntry.setGroup(TRN_PROCESS_PARAMS);
			paramEntry.setName(entry.getName());
			paramEntry.setValue(entry.getValue());
			
			trnProcessParamsList.add(paramEntry);
		}
		
		return trnProcessParamsList;
	}
	
	/**
	 * Get all the parameters from processes table
	 * @param process
	 * @return
	 * @throws TbitsExceptionClient
	 */
	private static List<TrnCreateProcess> getProcessParams(TrnProcess process) throws TbitsExceptionClient{
		List<TrnCreateProcess> trnProcessParamsList = new ArrayList<TrnCreateProcess>();
		
		List<TrnReplicateProcess> processParams = new ArrayList<TrnReplicateProcess>(ReplicateProcess.getProcessParams(process, process.getSrcBA()));
		for(TrnReplicateProcess entry : processParams){
			TrnCreateProcess paramEntry = new TrnCreateProcess();
			
			paramEntry.setGroup(TRN_PROCESS);
			paramEntry.setName(entry.getParamName());
			paramEntry.setValue(entry.getParamValueOld());
			trnProcessParamsList.add(paramEntry);
		}
		return trnProcessParamsList;
	}
}
