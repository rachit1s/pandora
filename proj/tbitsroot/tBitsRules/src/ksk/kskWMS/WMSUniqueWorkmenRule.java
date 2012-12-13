package kskWMS;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Collection;
import java.util.Date;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import static kskWMS.WMSConstants.*;

/**
 * @author Manoj 20th april'10
 * PreRule: The combination of Name, Father's Name, Sex & DOB should be unique for WMS Ba.
 */

public class WMSUniqueWorkmenRule implements IRule {

	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger("ksk");
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		RuleResult ruleresult = new RuleResult(true, "Ba: WMS Unique workmen screening Prerule.");
		if(ba.getSystemPrefix().equalsIgnoreCase(WMS_PREFIX)){
		
			// Continue if Update Request.
			if(!isAddRequest){
				String nameOld         = null;
				String fathernameOld   = null;
				String dateofbirthOld  = null;
				String warningstring   = null;
				int sexOld             = 0;
				
				try{
					nameOld           = oldRequest.get(WMS_NAME);
					fathernameOld     = oldRequest.get(WMS_FATHER_NAME);
					dateofbirthOld    = oldRequest.get(WMS_DATE_OF_BIRTH).split("\\s")[0];
					sexOld            = oldRequest.getCategoryId().getTypeId();
				}
				catch (IllegalStateException e){
					e.printStackTrace();
				}
				catch(Exception de){
					de.printStackTrace();
					ruleresult.setCanContinue(false);
					ruleresult.setMessage("Error occured while retrieving old request Ex-field parameters.");
					return ruleresult;
				}
				
				try{
					String nameNew        = currentRequest.get(WMS_NAME);
					String fathernameNew  = currentRequest.get(WMS_FATHER_NAME);
					String dateofbirthNew = currentRequest.get(WMS_DATE_OF_BIRTH).split("\\s")[0];
					int sexNew            = currentRequest.getCategoryId().getTypeId();
					if(!nameOld.equals(nameNew)){
						warningstring = "Name mismatch while updating Request.";
					}
					if(!fathernameOld.equals(fathernameNew)){
						warningstring = "Father Name mismatch while updating Request.";
					}
					if(!dateofbirthOld.equals(dateofbirthNew)){
						warningstring = "Date of Birth mismatch while updating Request.";
					}
					if(!(sexOld == sexNew)){
						warningstring = "Sex mismatch while updating Request.";
					}
					if(warningstring.length()>0){
						ruleresult.setCanContinue(false);
						ruleresult.setMessage(warningstring);
						return ruleresult;
					}
				}
				catch(IllegalStateException ie){
					ie.printStackTrace();
				}
				catch(Exception e){
					e.printStackTrace();
					ruleresult.setCanContinue(false);
					ruleresult.setMessage("Error occured while retrieving current request Ex-field parameters."+e.getMessage());
					return ruleresult;
				}
				ruleresult.setMessage("Update Request Screened w.r.t Unique Workmen.");
			}
	
			// Continue if New Request
			if (isAddRequest){
				int sysid = currentRequest.getSystemId();
				String name = "";
				String fathername = "";
				Date dobCurrent = null;
				try{	
					// name pre processing, remove extra spaces and set to the field again
					name = currentRequest.get(WMS_NAME).trim().replaceAll("\\s+"," "); // Data Type - varchar
					if(name.length()>0){
						currentRequest.setObject(WMS_NAME, name);
					}
					//father name pre processing, remove extra spaces and set to the field again
					fathername = currentRequest.get(WMS_FATHER_NAME).trim().replaceAll("\\s+", " "); //Data Type - varchar		
					if(fathername.length()>0){
						currentRequest.setObject(WMS_FATHER_NAME, fathername);
					}
					// DOB preprocessing and set time to 12:00:00:000 Noon
					dobCurrent = (Date)currentRequest.getObject(WMS_DATE_OF_BIRTH); //Data Type - date, Returns String yyyy-MM-dd HH:mm:ss
					if(dobCurrent != null){
						//String sqlFormDateTime = tsFormDOB.toSqlDate(); //Returns the Sql date in (yyyyMMdd HH:mm:ss) format
						String dobCurr = currentRequest.get(WMS_DATE_OF_BIRTH);
						String [] formDateTime = dobCurr.split("\\s"); 
						String formDate = formDateTime[0].replace("-", "");
						String modDateTime = formDate.concat("120000");// Set Time to 12:00:00 Noon in custom format
						try{
							Timestamp tsmoddatetime = new Timestamp (modDateTime);//The date should be specified in yyyyMMddHHmmss format.
							//Calendar cal = Calendar.getInstance();
							//cal.setTimeInMillis(tsmoddatetime.getTime());
							Date dobDateNoon = new Date();
							dobDateNoon.setTime(tsmoddatetime.getTime());
							currentRequest.setObject(WMS_DATE_OF_BIRTH, dobDateNoon);
							
							LOG.info("Continue to set DOB at 12 Noon.");
							//re.setDateTimeValue(tsmoddatetime);
							//currentRequest.setExDate("DateOfBirth", tsmoddatetime); ---> Not Working 
						}
						catch (Exception e){
							e.printStackTrace();
							ruleresult.setCanContinue(false);
							ruleresult.setMessage("Error occured while setting DOB to 12 Noon.");
							return ruleresult;
						}
					}
				}
				catch(IllegalStateException e){
					e.printStackTrace();
				}
				catch (Exception de){
					de.printStackTrace();
					ruleresult.setCanContinue(false);
					ruleresult.setMessage("Error occured while retrieving current request Ex-field parameters.");
					return ruleresult;
				}
				
				if(name.length()>0 && fathername.length()>0 && dobCurrent != null){
	
					try{
						//RequestEx reName = extendedFields.get(Field.lookupBySystemIdAndFieldName(sysid, WMS_NAME));
						Field nameField = Field.lookupBySystemIdAndFieldName(sysid, WMS_NAME);
						int nameFieldId = nameField.getFieldId();
						
						//RequestEx reFather = extendedFields.get(Field.lookupBySystemIdAndFieldName(sysid, WMS_FATHER_NAME));
						Field fatherField = Field.lookupBySystemIdAndFieldName(sysid, WMS_FATHER_NAME);
						int fatherFieldId = fatherField.getFieldId();
						
						// Get DOB field_id, year, month and day
						// RequestEx reqxModifiedDOB = extendedFields.get(Field.lookupBySystemIdAndFieldName(sysid, WMS_DATE_OF_BIRTH));
						Field dobField = Field.lookupBySystemIdAndFieldName(sysid, WMS_DATE_OF_BIRTH);
						int dobFieldId = dobField.getFieldId();
						
						// Timestamp tsModDOB = reqxModifiedDOB.getDateTimeValue();
						
						
						
						// String modDateofBirth = tsModDOB.toCustomFormat("MM-dd-yyyy");
						
						String dobCurt = currentRequest.get(WMS_DATE_OF_BIRTH); //Data Type - date, Returns String yyyy-MM-dd HH:mm:ss
						String dobPart = dobCurt.split("\\s")[0];
						
						String[] modDOBTokens = dobPart.split("-");
						int yearDOB  = Integer.parseInt(modDOBTokens[0]);
						int monthDOB = Integer.parseInt(modDOBTokens[1]);
						int dayDOB   = Integer.parseInt(modDOBTokens[2]);
						
						// Sex: category_id
						Type catType = currentRequest.getCategoryId(); //Data Type - type
						int sexTypeId = catType.getTypeId();
						
						String searchQuery = "";
						searchQuery = "SELECT name.request_id FROM requests_ex AS name " +
							"JOIN requests_ex AS dob " +
							"ON name.sys_id = dob.sys_id AND name.request_id = dob.request_id AND dob.field_id = ? " +
							"AND DATEPART (\"yyyy\", dob.datetime_value) = ? AND DATEPART (\"m\", dob.datetime_value) = ? AND DATEPART (\"d\", dob.datetime_value) = ? " +
							
							"JOIN requests_ex AS father " +
							"ON father.sys_id = name.sys_id AND father.request_id = name.request_id AND father.field_id = ? AND father.varchar_value = ? " +
							
							"JOIN requests AS categoryType " +
							"ON name.sys_id = categoryType.sys_id AND name.request_id = categoryType.request_id AND categoryType.category_id = ? " +
							
							"WHERE name.sys_id = ? AND name.field_id = ? AND name.varchar_value = ?;";
						
						PreparedStatement ps = connection.prepareStatement(searchQuery);
						
						ps.setInt(1, dobFieldId);
						ps.setInt(2, yearDOB);
						ps.setInt(3, monthDOB);
						ps.setInt(4, dayDOB);
						ps.setInt(5, fatherFieldId);
						ps.setString(6, fathername);
						ps.setInt(7, sexTypeId);
						ps.setInt(8, sysid);
						ps.setInt(9, nameFieldId);
						ps.setString(10, name);
						ResultSet rs = ps.executeQuery();
						// if workmen matches then request_id had value.
						while (rs.next()){
							int matchingRequestId = rs.getInt("request_id"); 
							ruleresult.setCanContinue(false);
							ruleresult.setMessage("Identity matches for WMS# "+matchingRequestId);
						}
						// close result set and ps for request_id  
						rs.close();
						ps.close();
					}
					catch(IllegalStateException ie){
						ie.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Error occured while retrieving Ex-field parameters." +ie.getMessage());
						return ruleresult;
					}
					catch(DatabaseException de){
						de.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Error occured while retrieving Ex-field parameters." +de.getMessage());
						return ruleresult;
					}
					catch(SQLException se){
						StringBuilder message = new StringBuilder();
						message.append("An exception occured while retrieving the RequestEx / Request table field.").append("\nSystem Id: ").append(currentRequest.getSystemId()).append("\n");
						se.printStackTrace();
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Error occured while retrieving Ex-field parameters." +se.getMessage());
						return ruleresult;
					}
					if(!ruleresult.canContinue())
					return ruleresult;
				}
				else{
					ruleresult.setCanContinue(false);
					ruleresult.setMessage("Name, Father name & Date of Birth cannot be Empty or Null.");
					return ruleresult;
				}
			}	
			// Check for Emergency Contact No.
			try{
				// Removes extra blank spaces from the string and set it.
				String emergencyContact = currentRequest.get(WMS_EMERGENCY_CONTACT_NO).trim().replaceAll("\\s+", " ");
				
				if (emergencyContact.length() > 5){
					
					// Check for contact no. to be '0-9' '+' '-' ',' and ' ' & <50 character long. 
					if (emergencyContact.matches("^[0-9\\+ ,-]{1,50}$")){
						ruleresult.setCanContinue(true);
						ruleresult.setMessage("Emergency contact number exists, hence rule successful.");
					}
					else{
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("Emergency contact number must be Numeric or within field limit.");
						return ruleresult;
					}
				}
				// Contact no. results Null or empty.
				else{
					ruleresult.setCanContinue(false);
					ruleresult.setMessage("Provide Emergency contact number: Atleast six characters.");
					return ruleresult;
				}
				currentRequest.setObject(WMS_EMERGENCY_CONTACT_NO, emergencyContact);
			}
			catch (Exception e) {
				e.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occured while retrieving Emergency contact Ex-Field.");
				return ruleresult;
			}
			
				// Check for Application Form
			try{
				String exattachAppForm = currentRequest.get(WMS_APPLICATION_fORM); // its json string now
				Collection<AttachmentInfo> exAttachedAppForm = AttachmentInfo.fromJson(exattachAppForm);
				
				if (exAttachedAppForm.isEmpty()){
					ruleresult.setCanContinue(false);
					ruleresult.setMessage("Attach the Application Form.");
					return ruleresult;
				}
				else{
					ruleresult.setMessage("BA [WMS] Rule Pass: Application Form is attached.");
				}
				
			}
			catch (IllegalStateException ie){
				ie.printStackTrace();
				ruleresult.setCanContinue(false);
				ruleresult.setMessage("Error occured while handling Ex-Attachments file."+ie.getMessage());
				return ruleresult;
			}
			
		}else{
			ruleresult.setMessage("Unique Workmen Rule: Not applicable for this BA - "+currentRequest.getSystemId());
		}
		return ruleresult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName(){
		return this.getClass().getSimpleName() + " - Checks for Uniqueness of Workmen w.r.t. Name, Father's name, sex and DOB.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence(){
		return 1;
	}

}
