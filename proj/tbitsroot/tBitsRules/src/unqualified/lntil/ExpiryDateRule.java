package lntil;

import java.sql.Connection;
import java.util.Date;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class ExpiryDateRule implements IRule{

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		RuleResult result = new RuleResult();
		String commaSeparatedPrefixes = PropertiesHandler.getProperty("IL_PREFIXES");
		if(commaSeparatedPrefixes != null && !commaSeparatedPrefixes.equals("")){
			String[] prefixes = commaSeparatedPrefixes.split(",");
			for(String prefix : prefixes){
				if(ba.getSystemPrefix().equals(prefix.trim())){
					Object obj = currentRequest.getObject(Field.DUE_DATE);
					if(obj != null && obj instanceof Date){
						Date expiryDate = (Date) obj;
						Date today = new Date();
						long diff = expiryDate.getTime() - today.getTime();
						if(diff < 7 * 30 * 24 * 60 * 60 * 1000){
							result.setCanContinue(false);
							result.setMessage("Passport expiry date is closer than 7 months from today");
						}
					}
					break;
				}
			}
		}
		return result;
	}

	@Override
	public String getName() {
		return "Passport Expiry Date Rule";
	}

	@Override
	public double getSequence() {
		return 0;
	}

}
