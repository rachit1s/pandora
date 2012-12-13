package mom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import transbit.tbits.domain.User;
/**
 * Sets the first_target_date to the due_date which was marked for the first time.
 * @author sandeepgiri
 *
 */
public class SetFirstTargetDate implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		// Checking for MOM BAs
		Properties props = PropertiesHandler.getAppAndSysProperties();
		String prefixes = (String) props.get("MOM_PREFIXES");
		if(prefixes != null){
			String[] bas = prefixes.split(",");
			List<String> baprefixes =  Arrays.asList(bas);
			if(!baprefixes.contains(ba.getSystemPrefix())){ // Is a MOM BA
				return new RuleResult(true, "Skipping the the rule", true);
			}
		}
		else
			return new RuleResult(true, "No Mom prefixes found. skiping the rule.", true);
		
		String firstTargetDateFieldName = "first_target_date";
		Field firstTargetDateField;
		try {
			firstTargetDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), firstTargetDateFieldName);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			RuleResult ruleResult = new RuleResult(true, "Unable to get the field '" + firstTargetDateFieldName + "'. " + e1.getMessage(), false);
			return ruleResult;
		}
		
		Date newTargetDate = null;
		
		if(oldRequest != null)
		{
			String query = " select ae.due_datetime from actions ae"
				+ " where ae.action_id = "
				+ " 	( select min(action_id) from actions mae "
				+ " 		where mae.sys_id = ? and mae.request_id = ? and mae.due_datetime IS NOT NULL) "
				+ " and ae.sys_id = ? and ae.request_id = ? ";
			
			try {
			
				PreparedStatement ps = connection.prepareStatement(query);
				ps.setInt(1, oldRequest.getSystemId());
				ps.setInt(2, oldRequest.getRequestId());
				ps.setInt(3, oldRequest.getSystemId());
				ps.setInt(4, oldRequest.getRequestId());
				ResultSet rs = ps.executeQuery();
				if(rs.next())
					newTargetDate = rs.getDate(1);
				if( (newTargetDate == null) && currentRequest.getDueDate() != null)
				{
					newTargetDate = currentRequest.getDueDate();
				}
			
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		else
		{
			if(currentRequest.getDueDate() != null)
			{
				newTargetDate = currentRequest.getDueDate();
			}
		}
		if(newTargetDate != null)
		{
			currentRequest.setObject(firstTargetDateField, newTargetDate);
		}
		return new RuleResult(true, "The first_target_date is updated.", true);
	}

	public String getName() {
		return "Set First Target Date";
	}

	public double getSequence() {
		return 0;
	}

}
