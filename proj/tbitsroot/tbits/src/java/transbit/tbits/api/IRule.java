package transbit.tbits.api;


import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

public interface IRule {
	/**
	 * Execute the Rule.
	 * @param ba Current Business area
	 * @param oldRequest The previous state of the request. It is generally needed for comparisons.
	 * @param currentRequest New Request, the request under processing.
	 * @param Source The origin of action. An update can originate from Email, Web Request or Command Line
	 * @param user The current user
	 * @param extendedFields the extended fields corresponding to the new request. 
	 * 							These are just a copy of old variable plus changes done by updates and other rules
	 * To Update header description, you can use: currentRequest.setHeaderDescription();
	 * @return The rule result stating whether the processing can be continued and whether the rule was successful.
	 */
	RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user,boolean isAddRequest);
	
	/**
	 * Gets the Sequence for a Rule. It is double because it make it easier to insert a new rule between two rules.
	 * @return returns the Sequence number at which it should be executed. 
	 */
	double getSequence();
	
	/**
	 * @return Gets the name of plug-in.  
	 */
	String getName();
}
