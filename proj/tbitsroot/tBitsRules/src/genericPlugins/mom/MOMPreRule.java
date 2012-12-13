package mom;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class MOMPreRule implements IRule{
	
	private String classNameMOM = "MOMPrerule";
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		RuleResult result = new RuleResult();
		
		// Checking for MOM BAs
		Properties props = PropertiesHandler.getAppAndSysProperties();
		String prefixes = (String) props.get("MOM_PREFIXES");
		if(prefixes != null){
			String[] bas = prefixes.split(",");
			List<String> baprefixes =  Arrays.asList(bas);
			if(baprefixes.contains(ba.getSystemPrefix())){ // Is a MOM BA
				// if parent-request == Meeting --> current request = Action Item
				// if parent-request == Agenda --> current request = Agenda Item
				// Copy Meeting Type
				int parentRequestId = currentRequest.getParentRequestId();
				if(parentRequestId != 0){
					try {
						Request parentRequest = Request.lookupBySystemIdAndRequestId(connection, ba.getSystemId(), parentRequestId);
						String recordType = parentRequest.get("recordtype");
						if(recordType != null && !recordType.equals("")){
							if(recordType.equals("Meeting")){
								currentRequest.setObject("recordtype", Type.lookupBySystemIdAndFieldIdAndTypeName(ba.getSystemId(), Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "recordtype").getFieldId(), "Action Item"));
							}else if(recordType.equals("Agenda")){
								currentRequest.setObject("recordtype", Type.lookupBySystemIdAndFieldIdAndTypeName(ba.getSystemId(), Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "recordtype").getFieldId(), "Agenda Item"));
							}
						}
						
						Type meetingType = parentRequest.getRequestTypeId();
						if(meetingType != null){
							currentRequest.setRequestTypeId(meetingType);
						}
						
						// Start Date < End Date
						try{
							Date startDate = (Date) parentRequest.getObject("StartDate");
							Date endDate = (Date) parentRequest.getObject("EndDate");
							if(startDate.compareTo(endDate) > 0){
								result.setMessage("Start Date can not be after End Date");
								result.setCanContinue(false);
								return result;
							}
						}catch (Exception e){
							
						}
					} catch (DatabaseException de){
						de.printStackTrace();
						result.setCanContinue(false);
						result.setMessage("Error Occured in fetching parent request. "+ de.getMessage());
						return result;
					}
				}
				
				// Mark as private
				currentRequest.setIsPrivate(true);
			}
		}
		return result;
	}

	public String getName() {
		return classNameMOM;
	}

	public double getSequence() {
		return 1;
	}

}
