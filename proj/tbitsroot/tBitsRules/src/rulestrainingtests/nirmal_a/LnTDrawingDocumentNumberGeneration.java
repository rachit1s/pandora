package ulestrainingtests.nirmal_a;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

/**
 * @author
 * Rule to generate the drawing/document number automatically for a given drawing/document.
 * 
 */
public class LnTDrawingDocumentNumberGeneration implements IRule {

	private static final String LNTDCR_DOCUMENT_NUMBER_FIELD_NAMES = "lntdcr.documentNumber.fieldNames";
	private static final String LNTDCR_DOCUMENT_NUMBER_IS_APPLY_RULE = "lntdcr.documentNumber.isApplyRule";
	private static final String LNTDCR_DOCUMENT_NUMBER_FORMATTER	 = "lntdcr.documentNumber.formatter";
	
	private static final String LNTDCR_DRAWING_NUMBER_IS_APPLY_RULE = "lntdcr.drawingNumber.isApplyRule";
	private static final String LNTDCR_DRAWING_NUMBER_FIELD_NAMES = "lntdcr.drawingNumber.fieldNames";
	private static final String LNTDCR_DRAWING_NUMBER_FORMATTER	  = "lntdcr.drawingNumber.formatter"; 
	private static final String LNTDCR_NUMBER_FORMATTER	  = "lntdcr.formatter.formatter"; 
	private static final String LNTDCR_DOC_NUMBER_PADDDING		= "lntdcr.docNumberPadding";
	
	private static final String DOCUMENT = "Document";
	private static final String DRAWING = "Drawing";
	private static final String DELIVERY_CATEGORY = "DelCategory";
	private static final String GEN_LTP_STD_NO = "GenLTPstdNo";//"GenVendorNo";

	/*
	 * (non-Javadoc)
	 * 
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection,
	 * transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request,
	 * transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		String drawingFieldName = null;
		try {
			drawingFieldName = getDrawingNumberField(connection, ba.getSystemId());
		} catch (SQLException e1) {
			e1.printStackTrace();
			return new RuleResult(false, "Database error occurred while fetching drawing number field " + "for the drawing/document number generation.");
		}

		if (drawingFieldName == null) 
			return new RuleResult(true, "Continuing without generating drawing/document number "
							+ "as its not applicable to BA: "
							+ ba.getSystemPrefix()
							+ ", as no field for drawing number is configured.");

		String generationNumber = new String();
		List<String> docNumberArgs = new ArrayList<String>();

		Object boolObj = currentRequest.getObject(GEN_LTP_STD_NO);
		if ((boolObj != null) && ((Boolean) boolObj).booleanValue()) {
			String fieldNames = null;
			String fieldNamesNotFoundMsg = "";
			String applyRuleNotFoundMsg = "";
			String formatterNotFoundMsg	= "";
			String isApplyRule = "";
			String formatter = "";

			String documentType = currentRequest.get(DELIVERY_CATEGORY);

			/*
			 * First check whether the documentType is Document or Drawing, and fetch the properties of the respective type
			 * from the database which will decide the format of the nubmer which will be generated.
			 */
			if (documentType.equals(DRAWING)) {
				/*
				 * Field Names which will be used to generate the number
				 */
				fieldNames = PropertiesHandler.getProperty(LNTDCR_DRAWING_NUMBER_FIELD_NAMES);
				/*
				 * Whether the rule has to be applied or not
				 */
				isApplyRule = PropertiesHandler.getProperty(LNTDCR_DRAWING_NUMBER_IS_APPLY_RULE);
				/*
				 * The formatter to be used for generating the number
				 */
				formatter = PropertiesHandler.getProperty(LNTDCR_NUMBER_FORMATTER);
				
				if (isApplyRule == null){
					applyRuleNotFoundMsg = "Property to apply drawing number generation rule is missing: "
							+ "lntdcr.drawingNumber.isApplyRule or is set to false";
				}

				if (fieldNames == null){
					fieldNamesNotFoundMsg = "Missing field names property in app.properties, with property name: "
							+ LNTDCR_DRAWING_NUMBER_FIELD_NAMES;
				}
				
				if(formatter == null){
					formatterNotFoundMsg = "The formatter for Drawing Number (lntdcr.drawingNumber.formatter) was not found in tbits.properties";
				}
			} else if (documentType.equals(DOCUMENT)) {
				isApplyRule 	= PropertiesHandler.getProperty(LNTDCR_DOCUMENT_NUMBER_IS_APPLY_RULE);
				fieldNames 	= PropertiesHandler.getProperty(LNTDCR_DOCUMENT_NUMBER_FIELD_NAMES);
				formatter	= PropertiesHandler.getProperty(LNTDCR_NUMBER_FORMATTER); 	
				
				if (isApplyRule == null){
					applyRuleNotFoundMsg = "Property to apply drawing number generation rule is missing: "
							+ "lntdcr.documentNumber.isApplyRule OR is set to false";
				}

				if(fieldNames == null){
					fieldNamesNotFoundMsg = "Missing field names property in app.properties, with property name: "
						+ LNTDCR_DOCUMENT_NUMBER_FIELD_NAMES;
				}
				
				if(formatter == null){
					formatterNotFoundMsg = "The formatter for Document Number (lntdcr.documentNumber.formatter) was not found in tbits.properties";
				}
			}
			
			if ((isApplyRule == null) || (!Boolean.valueOf(isApplyRule)))
				return new RuleResult(true, applyRuleNotFoundMsg);

			if (fieldNames == null)
				return new RuleResult(false, fieldNamesNotFoundMsg);
			
			if (formatter == null)
				return new RuleResult(false, formatterNotFoundMsg);

			/*
			 * Get the project code. This is the first parameter for generating any document number.
			 */
			String projectCode = ba.getType();
			if ((projectCode == null) || (projectCode.trim().equals(""))) {
				return new RuleResult(false,
						"No \"Project Code\" found for the current business area. Please provide "
								+ "appropriate value in business area type.", false);
			} else
				generationNumber = projectCode;
			
			docNumberArgs.add(projectCode);
			
			Integer count = 0;
			
			for (String fieldName : fieldNames.split(",")) {
				RuleResult ruleResult = new RuleResult();
				generationNumber = appendTypeDescriptionOfField(currentRequest, fieldName.trim(), generationNumber, ruleResult, count, docNumberArgs);
				if (!ruleResult.canContinue()) {
					ruleResult.setMessage("Following fields do not contain appropriate values: " + ruleResult.getMessage());
					return ruleResult;
				}
				count++;
			}
			
			
			if((null == formatter) || (formatter.trim().equals(""))){
				RuleResult ruleResult = new RuleResult();
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("No Formatter String defined in tbits.properties table for document number generation");
				return ruleResult;
			}
			
			generationNumber = projectCode;
			String []argsArray = new String[docNumberArgs.size()];
			docNumberArgs.toArray(argsArray);
			
			generationNumber = String.format(formatter, argsArray);
			
			try {
				Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), drawingFieldName);
				int runningNumber = getUniqDrawingNumber(connection, generationNumber.substring(0, generationNumber.length()));
				
				String docNumberPadding = PropertiesHandler.getProperty(LNTDCR_DOC_NUMBER_PADDDING);
				if((null == docNumberPadding) || (docNumberPadding.equals(""))){
					docNumberPadding = "0";
				}
				String numberPaddingFormatter = "%1$0" + Integer.valueOf(docNumberPadding) + "d";
				
				while(true){
					if(runningNumber > 0){
						StringBuffer docNumber = new StringBuffer(generationNumber.substring(0, generationNumber.length()));
						docNumber.append("-");
						docNumber.append(String.format(numberPaddingFormatter,runningNumber));
						
						String finalGeneratedDocNumber = docNumber.toString();
						
						if(isExtendedFieldValueUnique(connection, "requests_ex", "varchar_value", finalGeneratedDocNumber, field, false, true, ba.getSystemId(), currentRequest.getRequestId())){
							currentRequest.setObject(drawingFieldName, finalGeneratedDocNumber); // LNT_STD_NO,
							break;
						}else{
							runningNumber = getUniqDrawingNumber(connection, generationNumber.substring(0, generationNumber.length()));
						}
					}
				}
					
			} catch (SQLException e) {
				e.printStackTrace();
				return new RuleResult(false, "Database error occurred while fetching running serial number "
								+ "for the drawing/document number generation.");
			} catch (DatabaseException e) {
				e.printStackTrace();
				return new RuleResult(false, "Database error occurred while fetching running serial number "
						+ "for the drawing/document number generation.");
			} catch (TBitsException e) {
				e.printStackTrace();
				return new RuleResult(false, "Could not fetch document number from database.");
				
			}

		}
		docNumberArgs.clear();
		return new RuleResult();
	}

	/**
	 * @param currentRequest
	 * @param msgString
	 * @param drawingNumber
	 * @throws DatabaseException
	 */
	private String appendTypeDescriptionOfField(Request currentRequest, String fieldName, String drawingNumber, 
			RuleResult ruleResult, Integer count, List<String> docNumberArgs) {

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
						drawingNumber = drawingNumber.concat("-").concat(gACode);
						docNumberArgs.add(gACode);
						return drawingNumber;
					}
				}
			} else if ((field.getDataTypeId() == DataType.STRING)
					|| (field.getDataTypeId() == DataType.INT)
					|| (field.getDataTypeId() == DataType.REAL)) {
				
				drawingNumber = drawingNumber.concat("-").concat(currentRequest.get(field.getName()));
				docNumberArgs.add(currentRequest.get(field.getName()));
				
				return drawingNumber;
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
		return null;
	}

	/**
	 * @param connection
	 * @return list of DCR business areas.
	 * @throws SQLException
	 */
	private static ArrayList<Integer> getDCRBusinessAreas(Connection connection)
			throws SQLException {
		ArrayList<Integer> dcrSysIdList = new ArrayList<Integer>();
		if (connection != null) {
			PreparedStatement ps = connection
					.prepareStatement("SELECT DISTINCT src_sys_id FROM trn_processes");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while (rs.next()) {
					dcrSysIdList.add(rs.getInt("src_sys_id"));
				}
			rs.close();
		}
		return dcrSysIdList;
	}

	public static int getUniqDrawingNumber(Connection conn,	String drawingNumberPrefix) throws SQLException {
		CallableStatement stmt = conn.prepareCall("stp_getAndIncrMaxId ?");
		stmt.setString(1, drawingNumberPrefix);
		ResultSet rs = stmt.executeQuery();
		if ((rs != null) && rs.next()) {
			int id = rs.getInt("max_id");
			return id;
		} else {
			throw new SQLException();
		}
	}

	public static String getDrawingNumberField(Connection conn, int systemId)
			throws SQLException {
		// trn_drawing_number_field
		String drawingNumber = null;
		PreparedStatement ps = conn
				.prepareStatement("SELECT field_name FROM trn_drawing_number_field where sys_id=?");
		ps.setInt(1, systemId);
		ResultSet rs = ps.executeQuery();
		if ((rs != null) && rs.next())
			drawingNumber = rs.getString("field_name");
		return drawingNumber;
	}

	/*
	 * Takes comma separated string and a compare string. Checks if the compare
	 * string exists in the comma separated string.
	 */
	public static boolean isExistsInString(String parentString,
			String childString) {
		String[] strArray = parentString.split(",");
		for (String str : strArray) {
			if (str.trim().equals(childString.trim()))
				return true;
			else
				continue;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName()
				+ ": Generates the drawing/document number.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 0;
	}
	
	private static boolean isExtendedFieldValueUnique(Connection connection, String tableName,
			String colName, Object valueObject, Field field, boolean isUpdate, boolean isRequestLevelUniqueness, 
			int sysId, int requestId) throws TBitsException {
		try
		{
			String query = "";
			
			PreparedStatement ps = null; 
			switch(field.getDataTypeId())
			{				
			case DataType.INT:
				query = "select count(*) from " + tableName + " where " + colName + " = ? " 
				+ " and sys_id = " + sysId  + " and field_id = " + field.getFieldId();
				if(isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;
				
				ps = connection.prepareStatement(query);
				ps.setInt(1, ((Integer) valueObject).intValue());
				break;
				
			case DataType.REAL:
				//rather than using abs(rvalue - value) < 0.0001, using this approach so that table scan can be avoided
				query = "select count(*) from " + tableName + " where " + colName + " < (? + 0.0001) and " + colName + " > (? - 0.0001) " 
				+ " and sys_id = " + sysId  + " and field_id = " + field.getFieldId();
				if(isRequestLevelUniqueness && isUpdate)
					query += " and request_id != " + requestId;
				
				ps = connection.prepareStatement(query);
				ps.setDouble(1, ((Double)valueObject).doubleValue());
				ps.setDouble(2, ((Double)valueObject).doubleValue());

				break;
			case DataType.STRING:
				query = "select count(*) from " + tableName + " where " + colName + " = ? " 
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

	public static void main(String[] args) throws APIException {

//		AddRequest addRequest = new AddRequest();
//		addRequest.setSource(AddRequest.SOURCE_CMDLINE);
//		Hashtable<String, String> paramTable = new Hashtable<String, String>();
//		paramTable.put(Field.BUSINESS_AREA, "MALWA_LNT");
//		paramTable.put(Field.USER, "root");
//		paramTable.put("GenLTPstdNo", "true");
//		paramTable.put(Field.DESCRIPTION, "Test generation number: MALWA_LNT");
//		addRequest.addRequest(paramTable);
//		String str = 
	}

}
