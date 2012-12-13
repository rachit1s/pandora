package ksksco;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import static ksksco.scoConstants.*;

/**
 * @author Manoj
 * @Date 28th April 10
 * @Revision
 * 
 * PreRule : SCO Ba Number generation has to follow the document numbering system. 
 *           The logic would be as follows: KMP-M-9-CHO-0012 
 *           KMP   = Project Code = Constant
 *           M     = single letter discipline/department code. 
 *           9     = Unit Code ? single digit [alpha numeric]. Second Digit in Unit Code system 
 *           CHO   = if generating agency is SEPCO ? then CHO, if generating agency is WPCL, then KCO
 *           0012  = 4 digit running serial number.
 * 
 */
public class SCOKmpNumberGenPrerule implements IRule {

	private static final String KMP                   = "KMP";
	private static final String DELIMITER_DASH        = "-";
	private static final String GEN_AGENCY_WPCL_CODE  = "KCO";
	private static final String GEN_AGENCY_SEPCO_CODE = "CHO";
	
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger("ksk");
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleresult = new RuleResult(true, "Generate unique SCO document number or update user provided document number." );
		boolean isApplicable = ba.getSystemPrefix().equalsIgnoreCase(SCO_sysprefix);
		int systemId = ba.getSystemId();
		String warningString = "";
		
		// If update request then user not permitted to change SCO Document Number related fields.
		if (isApplicable && !isAddRequest){
			String scoNumberOld     = null;
			String scoDeptCodeOld   = null;
			String scoUnitCodeOld   = null;
			String scoGenAgencyOld  = null;
			try{
				scoNumberOld     = oldRequest.get(SCO_SCO_Order__);
				scoDeptCodeOld   = oldRequest.getCategoryId().getDescription();
				scoUnitCodeOld   = ((Type)oldRequest.getObject(SCO_Unit_Code)).getDescription();
				scoGenAgencyOld  = ((Type)oldRequest.getObject(SCO_GENERATION_AGENCY)).getDescription();
			
			}
			catch(Exception de){
				de.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occurred while retrieving oldRequest Data fields:" +
					" SCO Order No./Unit Code/Generation Agency in SCO BA.");
				return ruleresult;
			}
			
			try{
				String scoNumberNew     = currentRequest.get(SCO_SCO_Order__);	
				String scoDeptCodeNew   = currentRequest.getCategoryId().getDescription();	
				String scoUnitCodeNew   = ((Type)currentRequest.getObject(SCO_Unit_Code)).getDescription();	
				String scoGenAgencyNew  = ((Type)currentRequest.getObject(SCO_GENERATION_AGENCY)).getDescription();	
			
				if(!scoNumberOld.equals(scoNumberNew)){
					warningString = "Mismatch in SCO Order # while updating request.";
				}
				if(!scoDeptCodeOld.equals(scoDeptCodeNew)){
					warningString = "Mismatch in Dept Code while updating request.";
				}
				if(!scoUnitCodeOld.equals(scoUnitCodeNew)){
					warningString = "Mismatch in Unit Code while updating request.";
				}
				if(!scoGenAgencyOld.equals(scoGenAgencyNew)){
					warningString = "Mismatch in Generation Agency while updating request.";
				}
				if(warningString.length()>0){
					ruleresult.setCanContinue(false);
					ruleresult.setMessage(warningString);
					return ruleresult;
				}
				
				LOG.info(" Allowing the user to update Request in SCO BA, restrict changing the Unique SCO Order related fields.");	
				
			}
			catch(Exception de){
				de.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occurred while retrieving currentRequest Data fields:" +
						" SCO Order No./Unit Code/Generation Agency in SCO BA.");
				return ruleresult;
			}
			
		}
		
		// If New Request or New SubRequest.
		if( isApplicable && isAddRequest ){
			String scoDocumentNumber = KMP ;
			String userProvidedScoNumber = "";
			
			// Get the user provided SCO Number.
			try{
				userProvidedScoNumber = currentRequest.get(SCO_SCO_Order__).trim();
			}
			catch (IllegalStateException ie) {
				ie.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occurred while retrieving user-provided SCO Document number.");
				return ruleresult;
			}
			catch (Exception de) {
				de.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occurred while retrieving user-provided SCO Document number.");
				return ruleresult;
			}
			
			if( userProvidedScoNumber == null || !(userProvidedScoNumber.length()>0) ){
				LOG.info("Continuing to generate new SCO document number as the user has not provided a number.");
			}

			// User has Provided the string - Validate and update it in max_ids
			else{
				LOG.info("Continuing to Validate and Check the user provided SCO document number.");
				String normalizationMessage = getNormalizedAndValidatedNumber(userProvidedScoNumber);
				if (normalizationMessage.trim().equals("")) {
					scoDocumentNumber = userProvidedScoNumber;
					scoDocumentNumber = replaceStrangeDash(scoDocumentNumber);
					scoDocumentNumber = removeRedundantDashes(scoDocumentNumber);
					scoDocumentNumber = scoDocumentNumber.toUpperCase();
					
					String[] validateTokens = scoDocumentNumber.split(DELIMITER_DASH);
					
					// Check for Department Code match with user provided number.
					String deptCode = currentRequest.getCategoryId().getDescription();
					if(!deptCode.equalsIgnoreCase(validateTokens[1])){
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Department code mismatch in user provided SCO Number.");
						return ruleresult;
					}
					
					// Check for Unit Code match with user provided number.  
					try{
						String scoDeptClassification = ((Type)currentRequest.getObject(SCO_Unit_Code)).getDescription();
						String[] scoDeptClassificationType = scoDeptClassification.split(",");
						
						if ((scoDeptClassificationType.length > 1) 
							&& ((scoDeptClassificationType[1] != null) 
								&& (!scoDeptClassificationType[1].trim().equals("")))) {
							if(!scoDeptClassificationType[1].equalsIgnoreCase(validateTokens[2])){
								ruleresult.setCanContinue(false);
								ruleresult.setMessage("Unit code mismatch in user provided SCO Number.");
								return ruleresult;
							}
						}
						else{
							if(!scoDeptClassification.equalsIgnoreCase(validateTokens[2])){
								ruleresult.setCanContinue(false);
								ruleresult.setMessage("Unit code mismatch in user provided SCO Number.");
								return ruleresult;
							}
						}
					}
					catch (IllegalStateException e2) {
						e2.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Invalid field name: 'Unit Code'");
						return ruleresult;
					}
					catch (Exception e2) {
						e2.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Invalid field name: 'Unit Code'");
						return ruleresult;
					}
					
					// Check for Generation Agency Code match with user provided number.
					try{
						String generationAgency = ((Type)currentRequest.getObject(SCO_GENERATION_AGENCY)).getDescription();
						if(!generationAgency.equalsIgnoreCase(validateTokens[3])){
							ruleresult.setCanContinue(false);
							ruleresult.setMessage("Generation Agency code mismatch in user provided SCO Number.");
							return ruleresult;
						}
					}
					catch(Exception de){
						de.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Invalid field name: 'Generation Agency'");
						return ruleresult;
					}
				}
				
				else{
					warningString = normalizationMessage;
					if (!warningString.equals("")){
						ruleresult.setCanContinue(false);
						ruleresult.setMessage(warningString);
						return ruleresult;
					}
				}
				
				// Continue with user Provided String.
				// Add request and update request SCO number must not change.
				
				if( scoDocumentNumber != null && !(scoDocumentNumber.trim().equals("")) ){
					/*Check if a series exists and then carry on the validation process.
					 * a. If exists, check if less than the existing max_id and check for conflict. If there is a conflict prompt an error else
					 *    allow the user with to continue with the provided number.
					 * b. Check if equals to max_id, if so prompt error.
					 * c. If, equals max-id+1, allow. 
					 * d. If greater than max-id+1, also allow, but by shifting the max-id to the latest number.
					*/
					
					try {
						int lastIndexOfDash = scoDocumentNumber.lastIndexOf(DELIMITER_DASH);
						String userDocumentNumberPrefix = scoDocumentNumber.substring(0, lastIndexOfDash); // Prefix Code: KMP-M-9-CHO 
						int maxId = 0;
						maxId = getMaxId(connection, userDocumentNumberPrefix);
						String curIdString = scoDocumentNumber.substring(lastIndexOfDash + 1);
						int userProvidedId = Integer.parseInt(curIdString); // Document Id: 0015
						
						if (maxId == 0){
							String insertQuery = "INSERT INTO max_ids (name, id) VALUES (?, ?)"; //'" + documentNumberPrefix + "', " + userProvidedId + ")"
							PreparedStatement ps2 = connection.prepareStatement(insertQuery);
							ps2.setString(1, userDocumentNumberPrefix);
							ps2.setInt(2, userProvidedId);
							ps2.execute();
							ps2.close();
						}
						else if (maxId > 0){
							//Check if the Request is present of the SCO Number provided by the user.
							if (userProvidedId <= maxId){
								Field scoNumberField = Field.lookupBySystemIdAndFieldName(systemId, SCO_SCO_Order__);
								if (scoNumberField != null){
									int requestId = getRequestIdBySCONumber(connection, ba.getSystemId(), scoNumberField.getFieldId(), scoDocumentNumber);
									if (requestId == 0){
										LOG.info("Allowing the user to add the SCO number as a document with the number : \"" + scoDocumentNumber 
												+ "\", does not exist.");
									}
									else if (requestId > 0){
										ruleresult.setCanContinue(false);
										ruleresult.setMessage("This document number(" + scoDocumentNumber + ") already exists in tBits at SCO# " +requestId
												+ ". Please provide a different number or Leave it blank for Tbits to generate a new number.");
										return ruleresult;
									}
								}
							}
							else if (userProvidedId == (maxId + 1)){
								int generated_MaxId = getNextDocumentNumberCount(connection, userDocumentNumberPrefix);
							}
							else if (userProvidedId > (maxId + 1)){
								String updateQuery = "UPDATE max_ids SET id=? WHERE name=?"; //+ userProvidedId + " WHERE name='" + documentNumberPrefix + "'";
								PreparedStatement ps3 = connection.prepareStatement(updateQuery);
								ps3.setInt(1, userProvidedId);
								ps3.setString(2, userDocumentNumberPrefix);
								ps3.execute();
								ps3.close();
							}
						}
						//Finally put the number in the SCO document number field.
						currentRequest.setObject(SCO_SCO_Order__, scoDocumentNumber);
						ruleresult.setCanContinue(true);
						ruleresult.setMessage("Unique document number is : " + scoDocumentNumber);

						LOG.info("User Provided SCO Document Number: " + scoDocumentNumber + " is Validated and Checked." +
						" Updated in the max_ids table. Continue to use it in the Current Request.");
						return ruleresult;

					}
					catch(DatabaseException de){
						de.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Error occurred while retrieving user-provided SCO Document number.");
						return ruleresult;
					}
					catch(SQLException se){
						se.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Error occurred while validating user-provided SCO Document number.");
						return ruleresult;
					}
				}
			}
			
			// Continue to Generate new SCO Number: KMP-M-9-CHO-0012
			
			//Department Code
			Type categoryTypeId = currentRequest.getCategoryId();
			String depCode = categoryTypeId.getDescription();
			scoDocumentNumber = scoDocumentNumber + DELIMITER_DASH + depCode + DELIMITER_DASH;
			
			// Unit Code
			try {					
				String scoDeptClassification = ((Type)currentRequest.getObject(SCO_Unit_Code)).getDescription();
				String[] scoDeptClassificationType = scoDeptClassification.split(",");
				
				if ((scoDeptClassificationType.length > 1) 
						&& ((scoDeptClassificationType[1] != null) 
							&& (!scoDeptClassificationType[1].trim().equals(""))))
					scoDocumentNumber = scoDocumentNumber + scoDeptClassificationType[1] + DELIMITER_DASH;
				else 
					scoDocumentNumber = scoDocumentNumber + scoDeptClassification + DELIMITER_DASH;
				
			}
			catch (IllegalStateException e2) {
				e2.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Invalid field name: 'Unit Code'");
				return ruleresult;
			}
			catch (Exception e2) {
				e2.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Invalid field name: 'Unit Code'");
				return ruleresult;
			}
			
			try{
				// Generating Agency: SEPCO / WPCL
				String genAgency = ((Type)currentRequest.getObject(SCO_GENERATION_AGENCY)).getDescription();
			    scoDocumentNumber = scoDocumentNumber + genAgency;
			
    			// Get Next DocumentNumber
	    		int nextDocumentNumberCount = getNextDocumentNumberCount(connection, scoDocumentNumber);
		    	NumberFormat formatter = new DecimalFormat("0000");
			    String serialNumber = formatter.format(nextDocumentNumberCount);
			    scoDocumentNumber = scoDocumentNumber + DELIMITER_DASH + serialNumber;
			    
			    //Finally put the number in the SCO document number field.
				currentRequest.setObject(SCO_SCO_Order__, scoDocumentNumber);
			
			}
			catch (IllegalStateException e){
				e.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Invalid field name: 'Generation Agency'");
				return ruleresult;
			}
			catch (SQLException de){
				de.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occured while retrieving Generation Agency Type or Could not set SCO Number");
				return ruleresult;
			}
			catch (Exception se){
				se.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occurred which retrieving count of a particular document number.");
				return ruleresult;
			}
			ruleresult.setCanContinue(true);
			ruleresult.setMessage("Unique document number is : " + scoDocumentNumber);
			
			LOG.info("New Generated SCO Document Number: " + scoDocumentNumber + 
			". Updated in the max_ids table. Continue to use it in the Current Request.");
			
			return ruleresult;
			
		}
		else{
			ruleresult.setCanContinue(true);
			ruleresult.setMessage("Not applicable to the current business area: " + ba.getSystemPrefix() 
									+ " or is not add request action.");
			return ruleresult;
		}
	}
	
	
	private int getNextDocumentNumberCount (Connection con,
			String userDocumentNumberPrefix) throws SQLException {
		
		System.out.println("Generating document No. for : " + userDocumentNumberPrefix );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, userDocumentNumberPrefix );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			}
			else{
				throw new SQLException();
			}
		}
		catch (SQLException e) {
			throw e;
		}
	}


	private int getRequestIdBySCONumber(Connection con, int aSystemId,
			int aFieldId, String scoNumber) throws SQLException {
		try{	
			CallableStatement stmt = con.prepareCall("stp_get_requestIdByDocNo ?,?,?");
			stmt.setInt(1, aSystemId);
			stmt.setInt(2, aFieldId);
			stmt.setString(3, scoNumber);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("request_id");
				rs.close();
				stmt.close();
				return id;
			}
			else{
				return 0;
			}
		}
		catch (SQLException e) {
			throw e;
		}
	}

	
	private int getMaxId(Connection con, String documentNumberPrefix) throws SQLException {
		
		try {
			PreparedStatement ps1 = con.prepareStatement("SELECT id from max_ids where name = ? ;");
			ps1.setString(1, documentNumberPrefix );
			ResultSet rs = ps1.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("id");
				rs.close();
				ps1.close();
				return id;
			}
			else{
				return 0;
			}
		}
		catch (SQLException e) {
			throw e;
		}
	}

	
	private String getNormalizedAndValidatedNumber(String userProvidedScoNumber) {
		// SCO Document Number: KMP-M-9-CHO-0012
		String errorMessage = "";
		String validatedSCONumber = "";
		
		// Replace invalid dash characters.
		userProvidedScoNumber = replaceStrangeDash(userProvidedScoNumber);
		// Replace redundant dashes
		userProvidedScoNumber = removeRedundantDashes(userProvidedScoNumber);
		// Convert to upper-case for convenience
		validatedSCONumber = userProvidedScoNumber.toUpperCase();
		String[] validateTokens = validatedSCONumber.split(DELIMITER_DASH);
		
		Pattern SCONumberPattern1 = Pattern.compile(KMP+"-[A-Z]-[1-9ABC]-"+GEN_AGENCY_SEPCO_CODE+"-[0-9]{4}");
		Pattern SCONumberPattern2 = Pattern.compile(KMP+"-[A-Z]-[1-9ABC]-"+GEN_AGENCY_WPCL_CODE+"-[0-9]{4}");
		
		if (!SCONumberPattern1.matcher(validatedSCONumber).matches() && !SCONumberPattern2.matcher(validatedSCONumber).matches())
		{
			errorMessage = "Invalid SCO Number, please provide valid number or please leave SCO Number field empty*." +
			" *Leaving the SCO number field will lead to the tBits system generating the SCO number.";
		}
		
		if(!validateTokens[0].equals(KMP)){
			errorMessage = "Invalid project code in the user-provided document number.";
			return errorMessage;
		}
		
		if (validateTokens.length < 2)
			return "Invalid document number.";
		else if (!Pattern.matches("[A-Z]", validateTokens[1])){
			errorMessage = "Invalid \"Department Code\" in the user-provided document number.";
			return errorMessage;
		}
		if (validateTokens.length < 3)
			return "Invalid document number.";
		else if (!Pattern.matches("[0-9ABC]", validateTokens[2])){
			errorMessage = "Invalid \"Unit Code\" in the user-provided document number.";
			return errorMessage;
		}
		if (validateTokens.length < 4)
			return "Invalid document number.";
		else if (!Pattern.matches(GEN_AGENCY_SEPCO_CODE, validateTokens[3]) && !Pattern.matches(GEN_AGENCY_WPCL_CODE, validateTokens[3])){
			errorMessage = "Invalid \"Generation Agency Code\" in the user-provided document number.";
			return errorMessage;
		}		
		if (validateTokens.length < 5)
			return "Invalid document number.";
		else if (!Pattern.matches("[0-9]{4}", validateTokens[4])){
			errorMessage = "Invalid SCO number";
			return errorMessage;
		}

		return errorMessage;
	}

	
	private String replaceStrangeDash( String str ) 
	{
		String out = "" ;
		if( null == str ) 
			return out ;
		char strangeDash1 = (char)150 ;
		char strangeDash2 = (char)8211 ;
		char validDash = '-' ;
		for( int i = 0 ; i < str.length() ; i++ )
		{
			if( str.charAt(i) == strangeDash1 || str.charAt(i) == strangeDash2 )
				out += validDash ;
			else
				out += str.charAt(i) ;
		}
		return out ;
	}
	
	
	private String removeRedundantDashes(String corrNo )
	{
		if( null == corrNo || corrNo.equalsIgnoreCase("") ) 
			return "" ;
		
		String ncn = "" ;
		boolean gotD = false ;
		
		for( int i = 0 ; i < corrNo.length() ; i++ )
		{
			if(  '-' == corrNo.charAt(i) ){
				if( true == gotD  )
					continue ;
				else{
					ncn += corrNo.charAt(i) ;
					gotD = true ;
				}
			}
			else{
				ncn += corrNo.charAt(i) ;
				gotD = false ;
			}
		}
		
		if(ncn.length() == 0 )
			return ncn ;
		else{
			// remove trailing -es
			if( '-' == ncn.charAt(0) ) {	// first character{
				if( ncn.length() > 1 ){
					ncn = ncn.substring(1) ;				
				}
				else return "" ;
			}
			
			if('-' == ncn.charAt(ncn.length()-1)) // last character
			{
				if( ncn.length() > 1 ){
					ncn = ncn.substring(0, ncn.length()-1) ;
				}
				else return "" ;
			}
		}
		
		return ncn ;
	}
	
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName() + ": Generates unique SCO Document Number or " +
				"updates user defined Doc Number for a new Scope Change Order being logged.";
	}

	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		return 3;
	}

}
