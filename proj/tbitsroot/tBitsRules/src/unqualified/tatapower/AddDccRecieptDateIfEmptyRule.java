/**
 * 
 */
package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class AddDccRecieptDateIfEmptyRule implements IRule {
		
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		int systemId = ba.getSystemId();
		Field dccDateField = null;

		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
				
		if (isApplicable && isAddRequest){
			try {	
				dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				if (dccDateField != null){
					if (currentRequest.getExDateTime(TataPowerUtils.DCC_RECEIPT_DATE) == null){
						Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("IST"));
						Timestamp timestamp = Timestamp.getTimestamp(new Date(cal.getTimeInMillis()));
						currentRequest.setExDate(TataPowerUtils.DCC_RECEIPT_DATE, timestamp);
						return new RuleResult(true, dccDateField.getDisplayName() + ": cannot be empty. " +
								"Hence setting current date.", true);
					}
				}
				return new RuleResult(true, "Successful");

			} catch (DatabaseException e) {
				e.printStackTrace();
				return new RuleResult(false, "Cannot continue as database error occurred.\n" + e.getMessage());
			}
		}
		else
			return new RuleResult(true, "Not applicable to this business area");

		//return new RuleResult(true, "Not applicable");
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - sets status depending on the various reciept dates";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 0;
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		BusinessArea ba =BusinessArea.lookupBySystemPrefix("Bill");
		Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), 14);
		User root = User.lookupAllByUserId(1);
		IRule irule = new StatusBasedAssigneesFor60Percent();
		/*irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");*/

	}

}
