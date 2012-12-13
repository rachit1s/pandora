package transmittal.com.tbitsGlobal.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

/**
 * Utility class to provide functions to manipulate
 * trn_validation_rules table
 * @author devashish
 *
 */
public class ValidationRules {
	
	
	/**
	 * Save the validation rules for the specified process into database
	 * @param process
	 * @param list
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnValidationRule> saveValidationRulesForProcess(TrnProcess process, List<TrnValidationRule> list) throws TbitsExceptionClient{
		List<TrnValidationRule> savedList = new ArrayList<TrnValidationRule>();
		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "delete from trn_validation_rules where trn_process_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, process.getProcessId());
			
			ps.execute();
			ps.close();
			
			for(TrnValidationRule rule : list){
				savedList.add(insertIntoValidationRules(connection, rule));
			}
			connection.commit();
			
		}catch (SQLException e){
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				//TODO: Log it --SG
				e1.printStackTrace();
				throw new TbitsExceptionClient(e1);
			}
			e.printStackTrace();
			throw new TbitsExceptionClient();
		}finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    throw new TbitsExceptionClient(sqle);
                }
                connection = null;
            }
        }
		return savedList;
	}
	
	private static TrnValidationRule insertIntoValidationRules(Connection connection, TrnValidationRule rule) throws SQLException{
		
		String sql = "insert into trn_validation_rules " +
					" (trn_process_id, field_id, value) "+
					" VALUES(?,?,?)";
		
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, rule.getProcess().getProcessId());
		ps.setInt(2, rule.getField().getFieldId());
		ps.setString(3, rule.getRule());
		
		ps.execute();
		ps.close();
		
		rule.set(IBulkUpdateConstants.RESPONSE_STATUS, IBulkUpdateConstants.UPDATED);
		return rule;
	}
	
	/**
	 * Get the validation rules for the specified proces
	 * @param process
	 * @param request
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TrnValidationRule> getValidationRulesForProcess(TrnProcess process, HttpServletRequest request) throws TbitsExceptionClient{
		List<TrnValidationRule> rulesList = new ArrayList<TrnValidationRule>();
		
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
		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "SELECT * FROM trn_validation_rules where trn_process_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ps.setInt(1, process.getProcessId());
			
			ResultSet rs = ps.executeQuery();
			
			if(null != rs){
				while(rs.next()){
					TrnValidationRule ruleEntry = new TrnValidationRule();
					int fieldId = rs.getInt("field_id");
					String rule = rs.getString("value");
					
					Field field;
					try {
						field = Field.lookupBySystemIdAndFieldId(process.getSrcBA().getSystemId(), fieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						
						ruleEntry.setField(baField);
						ruleEntry.setRule(rule);
						ruleEntry.setProcess(process);
						ruleEntry.setSrcBa(process.getSrcBA());
						
						rulesList.add(ruleEntry);
						
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
			}
		}catch (SQLException e){
			e.printStackTrace();
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                connection = null;
            }
        }
		return rulesList;
	}
	
}
