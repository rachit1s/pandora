package icms.icmscommon;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * Rule for automatic generation of Drawing/Document number for documents. The configuration
 * values are picked from the database for generating the document number for a particular BA, 
 * for a particular document/drawing, consisting of certain fields and adhering to a certain
 * pattern. 
 * @author devashish
 *
 */
public class DrawingNumberGeneration implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if(isAddRequest)
		{
		List<String> docNumberArgs = new ArrayList<String>();
		String generatedNumber = "";
		
		//-------------------Get the Field in which drawing/document number has to be inserted---------//
		Field drawingNumberField = null;
		try {
			drawingNumberField = getDrawingNumberField(connection, ba);
		} catch (SQLException e) {
			e.printStackTrace();
			return new RuleResult(false, "Error while fetching the Drawing Number field from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "]");
		}
		
		if (null == drawingNumberField){ 
			return new RuleResult(true, "Continuing without generating drawing/document number as its not applicable to BA: "
							+ ba.getSystemPrefix() + ", as no field for drawing number is configured.");
		}
		
		//----------Get the field which specifies whether the drawing/document number has to be generated or not------//
		Field decisionField = null;
		try {
			decisionField = getDecisionField(connection, ba);
		} catch (SQLException e) {
			e.printStackTrace();
			return new RuleResult(false, "Error while fetching the Decision Field from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "] " +
					" for the generation of drawing/document number.");
		}
		
		if(null == decisionField){
			return new RuleResult(true, "Continuing without generating drawing/document number there is no field in BA :"
					+ ba.getSystemId() + "[" + ba.getSystemPrefix() + "] to decide whether to generate Drawing Number or not.");
		}
		
		Object generateDwgNumber = currentRequest.getObject(decisionField.getName());
		if((null != generateDwgNumber)  && ((Boolean)generateDwgNumber).booleanValue()){
			/*
			 * Generate dwg/doc number 'true' and a valid field found in which to insert the generated number.
			 */
			//------------------------Get the Fields List --------------------//
			String fieldsString = null;
			try {
				fieldsString = getFieldsList(connection, ba);
			} catch (SQLException e) {
				e.printStackTrace();
				return new RuleResult(false, "Error while fetching the Fields from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "] " +
						" for the generation of drawing/document number.");
			}
			
			if(null == fieldsString){
				return new RuleResult(true, "Continuing without generating drawing/document number as no fields are configured for BA: "
						+ ba.getSystemPrefix() + " for the generation.");
			}
			
			//------------------------Get the Formatter String----------------//
			String formatterString = null;
			try {
				formatterString = getFormatterString(connection, ba);
			} catch (SQLException e) {
				e.printStackTrace();
				return new RuleResult(false, "Error while fetching the Formatter String from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "] " +
						" for the generation of drawing/document number.");
			}
			
			if(null == formatterString){
				return new RuleResult(true, "Continuing without generating drawing/document number as no formatter string is configured for BA: "
						+ ba.getSystemPrefix() + " for the generation.");
			}
			
			//------------------------Get the Running Number Padding-----------//
			Integer padding = null;
			try {
				padding = getRunningNumberPadding(connection, ba);
			} catch (SQLException e) {
				e.printStackTrace();
				return new RuleResult(false, "Error while fetching the Running Number Padding from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "] " +
				" for the generation of drawing/document number.");
			}
			
			if(null == padding){
				padding = 0;
			}
			
			/*
			 * All values necessary for the generation of Drawing/Document number have been received. Any failures after this step
			 * onwards should return false.
			 */
			
			String projectCode = ba.getType();
			if ((projectCode == null) || (projectCode.trim().equals(""))) {
				return new RuleResult(false, "No \"Project Code\" found for the current business area. Please provide "
								+ "appropriate value in business area type.", false);
			}else{
				docNumberArgs.add(projectCode);
				generatedNumber = projectCode;
			}
			
			for (String fieldName : fieldsString.split(",")) {
				RuleResult ruleResult = new RuleResult();
				appendTypeDescriptionOfField(currentRequest, fieldName.trim(), ruleResult, docNumberArgs);
				if (!ruleResult.canContinue()) {
					ruleResult.setMessage("Following fields do not contain appropriate values: " + ruleResult.getMessage());
					return ruleResult;
				}
			}
			
			String []argsArray = new String[docNumberArgs.size()];
			docNumberArgs.toArray(argsArray);
			
			generatedNumber = String.format(formatterString, argsArray);
			/*
			 * Formatted String generated, without the running number.
			 */
			
			//-------------------------Fetch the Running Number to be appended-------------------------//
			Integer runningNumber = null;
			try {
				runningNumber = getRunningNumber(connection, generatedNumber);
			} catch (SQLException e) {
				e.printStackTrace();
				return new RuleResult(false, "Error while fetching the Running Number from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "] " +
				" for the generation of drawing/document number.");
			}
			
			if(null == runningNumber){
				return new RuleResult(true, "Continuing without generating drawing/document number as no running number is available for BA: "
						+ ba.getSystemPrefix() + " for the Dwg/Doc Number prefix " + generatedNumber);
			}
			
			String paddingFormatter = "%1$0" + padding + "d";
			
			while(true){
				if(runningNumber > 0){
					StringBuffer docNumber = new StringBuffer(generatedNumber);
					docNumber.append(String.format(paddingFormatter, runningNumber));
					
					String finalGeneratedDocNumber = docNumber.toString();
					
					try {
						if(isExtendedFieldValueUnique(connection, finalGeneratedDocNumber, drawingNumberField, false, true, ba.getSystemId(), currentRequest.getRequestId())){
							currentRequest.setObject(drawingNumberField.getName(), finalGeneratedDocNumber); 
							break;
						}else{
							runningNumber = getRunningNumber(connection, generatedNumber);
						}
						
					} catch (TBitsException e) {
						e.printStackTrace();
						return new RuleResult(false, "Could not fetch document number from database.");
					} catch (SQLException e) {
						e.printStackTrace();
						return new RuleResult(false, "Error while fetching the Running Number from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "] " +
						" for the generation of drawing/document number.");
					}
				}
			}
		}
		
		docNumberArgs.clear();
		return new RuleResult();
	}
		else
			return new RuleResult();
	}
	
	/**
	 * Check if the specified 'valueObject' is unique or not in the database.
	 * @param connection
	 * @param valueObject
	 * @param field
	 * @param isUpdate
	 * @param isRequestLevelUniqueness
	 * @param sysId
	 * @param requestId
	 * @return
	 * @throws TBitsException
	 */
	private Boolean isExtendedFieldValueUnique(Connection connection, Object valueObject, Field field, Boolean isUpdate, 
			Boolean isRequestLevelUniqueness, Integer sysId, Integer requestId) throws TBitsException {
		try {
			String query = "";
			
			PreparedStatement ps = null; 
			
			switch(field.getDataTypeId())
			{				
			case DataType.INT:
				query = "select count(*) from requests_ex where varchar_value = ? " 
				+ " and sys_id = " + sysId  + " and field_id = " + field.getFieldId();
				if(isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;
				
				ps = connection.prepareStatement(query);
				ps.setInt(1, ((Integer) valueObject).intValue());
				break;
				
			case DataType.REAL:
				//rather than using abs(rvalue - value) < 0.0001, using this approach so that table scan can be avoided
				query = "select count(*) from requests_ex where varchar_value < (? + 0.0001) and varchar_value > (? - 0.0001) " 
				+ " and sys_id = " + sysId  + " and field_id = " + field.getFieldId();
				if(isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;
				
				ps = connection.prepareStatement(query);
				ps.setDouble(1, ((Double)valueObject).doubleValue());
				ps.setDouble(2, ((Double)valueObject).doubleValue());

				break;
			case DataType.STRING:
				query = "select count(*) from requests_ex where varchar_value = ? " 
				+ " and sys_id = " + sysId  + " and field_id = " + field.getFieldId();
				if(isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;
				
				ps = connection.prepareStatement(query);
				ps.setString(1, (String)valueObject);
				break;
			default:
				throw new IllegalArgumentException("Unsupported datatype for Uniqueness");
			}
			
			boolean retValue = true;
			
			if(ps != null)
			{
				ResultSet rs = ps.executeQuery();
				if(rs.next())
				{
					int numberOfRecords = rs.getInt(1);
					if(numberOfRecords > 0)
						retValue = false;
				}
				
				rs.close();
				ps.close();
			}
			return retValue;
		}
		catch(Exception exp){
			throw new TBitsException("Unknown database error has occurred", exp);
		}
	}
	
	
	/**
	 * Get the Running Number for the specified drawing number prefix
	 * @param conn
	 * @param drawingNumberPrefix
	 * @return Running Number to be appended to the generated drawing number
	 * @throws SQLException
	 */
	private Integer getRunningNumber(Connection connection, String drawingNumberPrefix) throws SQLException {
		CallableStatement stmt = connection.prepareCall("stp_getAndIncrMaxId ?");
		stmt.setString(1, drawingNumberPrefix);
		ResultSet rs = stmt.executeQuery();
		
		if ((rs != null) && rs.next()) {
			Integer runningNumber = rs.getInt("max_id");
			return (runningNumber == null) ? null : runningNumber;
		}
		return null;
	}
	
	/**
	 * Fetch the 'Type' of the 'fieldName' and add it to list of types i.e. docNumberArgs
	 * @param currentRequest
	 * @param fieldName
	 * @param ruleResult
	 * @param docNumberArgs
	 */
	private void appendTypeDescriptionOfField(Request currentRequest, String fieldName, RuleResult ruleResult, List<String> docNumberArgs) {

		Field field = null;
		try {
			field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
		} catch (DatabaseException e) {
			e.printStackTrace();
			ruleResult.setMessage("Field with name: " + fieldName
							+ ", not found in this business area. Please check the configuration"
							+ " of fields included for generation of drawing/document number.");
			ruleResult.setCanContinue(false);
		}
		
		if (field != null) {
			if ((field.getDataTypeId() == DataType.TYPE)) {
				Type generatingAgencyType = (Type) currentRequest.getObject(fieldName);
				if (generatingAgencyType != null) {
					String gACode = generatingAgencyType.getDescription();
					
					if ((gACode != null) && (!gACode.trim().isEmpty())){
						docNumberArgs.add(gACode);
					}
				}
			} else if ((field.getDataTypeId() == DataType.STRING)
					|| (field.getDataTypeId() == DataType.INT)
					|| (field.getDataTypeId() == DataType.REAL)) {
				
				docNumberArgs.add(currentRequest.get(field.getName()));
			} else {
				String rrMsg = ruleResult.getMessage();
				rrMsg = (rrMsg.trim().equals("")) ? field.getDisplayName() : rrMsg + ", " + field.getDisplayName();
				ruleResult.setMessage(rrMsg);
				ruleResult.setCanContinue(false);
			}
		} else {
			ruleResult
					.setMessage("Field with name: "	+ fieldName
							+ ", not found in this business area. Please check the configuration"
							+ " of fields included for generation of drawing/document number.");
			ruleResult.setCanContinue(false);
		}
	}
	

	/**
	 * Get the Field in the specified BA into which the generated drawing/document number has to be inserted
	 * @param connection
	 * @param ba
	 * @return
	 * @throws SQLException
	 */
	private Field getDrawingNumberField(Connection connection, BusinessArea ba) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("select field_name from trn_drawing_number_field where sys_id = ?");
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if ((rs != null) && rs.next()){
			String drawingNumberFieldName = rs.getString(1);
			Field drawingNumberField = null;
			try {
				drawingNumberField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), drawingNumberFieldName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				return null;
			}
			return (drawingNumberField == null) ? null : drawingNumberField;
		}
		return null;
	}
	
	/**
	 * Get the padding to be used for the running number which is appended at the end of document/drawing number string
	 * already constructed.
	 * @param connection
	 * @param ba
	 * @return
	 * @throws SQLException
	 */
	private Integer getRunningNumberPadding(Connection connection, BusinessArea ba) throws SQLException{
		String sql = "select padding from drawing_number_generation_config where sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if((null != rs) && rs.next()){
			Integer padding = rs.getInt(1);
			return (padding == null) ? null : padding;
		}
		return null;
	}
	
	/**
	 * Get the formatter String which will dictate the output format of the document/drawing number for
	 * the specified BA.
	 * @param connection
	 * @param ba
	 * @return
	 * @throws SQLException
	 */
	private String getFormatterString(Connection connection, BusinessArea ba) throws SQLException{
		String sql = "select formatter from drawing_number_generation_config where sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if((null != rs) && rs.next()){
			String formatterString = rs.getString(1);
			return (formatterString == null) ? null : formatterString;
		}
		return null;
	}
	
	/**
	 * Get the list of fields, values of which will be used to generate the drawing number
	 * @param connection
	 * @param ba
	 * @return
	 * @throws SQLException
	 */
	private String getFieldsList(Connection connection, BusinessArea ba) throws SQLException{
		String sql = "select fields from drawing_number_generation_config where sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if(null != rs){
			while(rs.next()){
				String fieldsList = rs.getString(1);
				return (fieldsList == null) ? null : fieldsList;
			}
		}
		return null;
	}
	
	/**
	 * Get the field that has to be checked as true in order to generate the document/drawing number for 
	 * the specified BA. Only when the value of this field is true, is the automatic generation of number
	 * takes place
	 * @param connection
	 * @param ba
	 * @return null, if no such field exists
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private Field getDecisionField(Connection connection, BusinessArea ba) throws SQLException{
		String sql = "select field_id from drawing_number_generation_config where sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, ba.getSystemId());
		
		ResultSet rs = ps.executeQuery();
		if(null != rs){
			while(rs.next()){
				Integer fieldId = rs.getInt(1);
				if(null != fieldId){
					Field decisionField = null;
					try {
						decisionField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldId);
					} catch (DatabaseException e) {
						e.printStackTrace();
						return null;
					}
					return (decisionField == null) ? null : decisionField;
				}
			}
		}
		return null;
	}
	
	/**
	 * Check whether the Drawing/document number generation rule is applicable for the current BA or not
	 * @param connection 
	 * @param ba 
	 * @return true, if rule is to be applied; false otherwise
	 * @throws SQLException
	 */
	private Boolean isRuleApplicableForBA(Connection connection, BusinessArea ba) throws SQLException{
		String sql  = "select count(sys_id) from drawing_number_generation_config where sys_id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if(null != rs){
			while(rs.next()){
				Integer count = rs.getInt(1);
				return (count > 0) ? true : false;
			}
		}
		return false;
	}
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName() + ": Generates the drawing/document number after fetching corresponding properties from DB";
	}

	@Override
	public double getSequence() {
		return 2.0;
	}

}
