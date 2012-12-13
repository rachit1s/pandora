/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
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
public class InvoiceDateNotInFuture implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		int systemId = ba.getSystemId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		if (isApplicable){
			try {
				Field invoiceDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.INVOICE_DATE);
				if (invoiceDateField != null){
					RequestEx invoiceReqEx = extendedFields.get(invoiceDateField);
					if ((invoiceReqEx != null) && (invoiceReqEx.getDateTimeValue() != null)){
						Timestamp invoiceDateTS = invoiceReqEx.getDateTimeValue();
						
						//Date invDate = new Date(invoiceDateTS.getTime());
						Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
						Date curDate = new Date(c.getTimeInMillis());
						/*SimpleDateFormat curSdf = new SimpleDateFormat("dd-MM-yyyy");
						String curDateStr = curSdf.format(curDate);
						String invDateStr = curSdf.format(invDate);*/
						String invDateStr = toDateMin(invoiceDateTS).substring(0,10);;
						Timestamp ts1 = new Timestamp(c.getTimeInMillis());	
						String curDateStr = toDateMin(ts1).substring(0,10);;
						System.out.println("InvDate: " + invDateStr + ", curDate: " + curDateStr);
						
						if (invDateStr.equals(curDateStr) && (!invDateStr.equals("")) && (!curDateStr.equals(""))){
							ruleResult.setCanContinue(true);
							ruleResult.setSuccessful(true);
						}
						/*else if(invoiceDateTS.before(curDate)){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage(invDateField.getDisplayName() + " date cannot occur in history, should be current day's date.");
							return ruleResult;
						}*/
						else if(invoiceDateTS.after(curDate)){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("\""+ invoiceDateField.getDisplayName() + "\" date cannot occur in future.");
							return ruleResult;
						}						
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setSuccessful(false);
					ruleResult.setMessage("No invoice date field found");
				}				
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		return ruleResult;
	}
	
	public static String toDateMin(Timestamp ts) {
    	TimeZone   zone   = TimeZone.getTimeZone("GMT");
    	 Calendar cal = Calendar.getInstance(zone);

         cal.setTime(new Timestamp(ts.getTime()));

         int    year       = cal.get(Calendar.YEAR);
         int    month      = cal.get(Calendar.MONTH) + 1;
         int    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
         int    hourOfDay  = cal.get(Calendar.HOUR_OF_DAY);
         int    minute     = cal.get(Calendar.MINUTE);
         String s          = "GMT";

         // LOG.info(zone.getID());
        /* if (zone.getID().trim().equals("America/New_York") || (zone.getID().trim().equals("US/Eastern"))) {
             s = "EST";
         } else {
             s = "IST";
         }
*/
         return twoDigitPad(month) + "/" + twoDigitPad(dayOfMonth) + "/" + year + ' ' + twoDigitPad(hourOfDay) + ':' + twoDigitPad(minute) + ' ' + s;
     }
	
	 /**
     * Given an integer, return a two-digit String to represent that
     * integer, left-padding with zeroes if necessary.
     *
     * @param num   The number to process.
     * @return      The String which represents num.
     */
    private static String twoDigitPad(int num) {
        if (num < 10) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + ": Ensures if the Invoice Date is not later than current date.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
