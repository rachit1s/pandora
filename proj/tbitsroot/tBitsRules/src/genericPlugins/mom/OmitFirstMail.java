package mom;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class OmitFirstMail implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		RuleResult result = new RuleResult();
		
		if(isAddRequest){ //If first action 
			// Checking for MOM BAs
			Properties props = PropertiesHandler.getAppAndSysProperties();
			String prefixes = (String) props.get("MOM_PREFIXES");
			if(prefixes != null){
				String[] bas = prefixes.split(",");
				List<String> baprefixes =  Arrays.asList(bas);
				if(baprefixes.contains(ba.getSystemPrefix())){ // Is a MOM BA
					String recordType = currentRequest.get("recordtype");
					if(recordType != null && !recordType.equals("")){
						if(recordType.equals("Meeting") || recordType.equals("Agenda")){
							currentRequest.setNotify(false);
						}
					}
				}
			}
		}
		
		return result;
	}

	public String getName() {
		return "Omit First Mail in Meeting/Agenda";
	}

	public double getSequence() {
		return 0;
	}

}
