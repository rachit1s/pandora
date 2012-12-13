/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class HistoricalDates implements IRule {
	
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
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix );
		if (isApplicable){			
			try {
				ArrayList<String> fieldNameList = new ArrayList<String>();
				fieldNameList.add(TataPowerUtils.DCC_RECEIPT_DATE);
				//fieldNameList.add(TataPowerUtils.DCC_ACKNOWLEDGE);
				fieldNameList.add(TataPowerUtils.STORE_RECEIVED_DATE);
				fieldNameList.add(TataPowerUtils.UDSI_RECIEPT_DATE);
				//fieldNameList.add(TataPowerUtils.SITE_IC_CERTIFIED);								
				fieldNameList.add(TataPowerUtils.QS_RECIEPT_DATE);
				//fieldNameList.add(TataPowerUtils.QS_VERIFIED);
				fieldNameList.add(TataPowerUtils.QUALITY_RECIEVED_DATE);
				fieldNameList.add(TataPowerUtils.GH_RECIEPT_DATE);
				//fieldNameList.add(TataPowerUtils.UH_APPROVED);
				fieldNameList.add(TataPowerUtils.HOC_RECIEPT_DATE);
				//fieldNameList.add(TataPowerUtils.HOC_APPROVED);				
				fieldNameList.add(TataPowerUtils.FINANCE_RECIEPT_DATE);
				fieldNameList.add(TataPowerUtils.FIN_PAYMENT_DATE);
				
				Hashtable<String, String> dateFieldsMapping = getDateFieldsMapping();
				
				Hashtable<Field, RequestEx> extendedFields2 = null;
				if ((!isAddRequest) || (oldRequest != null))
					extendedFields2 = oldRequest.getExtendedFields();				
				return isDateNotInHistory(systemId, extendedFields, ruleResult, fieldNameList, extendedFields2, dateFieldsMapping);
				
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return ruleResult;
	}

	/**
	 * @return TODO
	 * 
	 */
	private Hashtable<String, String> getDateFieldsMapping() {
		Hashtable<String,String> fieldMap = new Hashtable<String, String>();
		fieldMap.put(TataPowerUtils.DCC_RECEIPT_DATE, TataPowerUtils.DCC_ACKNOWLEDGE);
		fieldMap.put(TataPowerUtils.STORE_RECEIVED_DATE, TataPowerUtils.STORE_ACKNOWLEDGE);
		fieldMap.put(TataPowerUtils.UDSI_RECIEPT_DATE, TataPowerUtils.SITE_IC_CERTIFIED);
		fieldMap.put(TataPowerUtils.QS_RECIEPT_DATE, TataPowerUtils.QS_VERIFIED);
		fieldMap.put(TataPowerUtils.QUALITY_RECIEVED_DATE, TataPowerUtils.QUALITY_ACKNOWLEDGE);
		fieldMap.put(TataPowerUtils.GH_RECIEPT_DATE, TataPowerUtils.UH_APPROVED);
		fieldMap.put(TataPowerUtils.FINANCE_RECIEPT_DATE, TataPowerUtils.FIN_PAYMENT_DATE);
		return fieldMap;
	}

	/**
	 * @param extendedFields
	 * @param ruleResult
	 * @param fieldNameList
	 * @param extendedFields2 
	 * @param dateFieldsMapping 
	 * @return TODO
	 * @throws DatabaseException 
	 */
	private RuleResult isDateNotInHistory(int systemId, Hashtable<Field, RequestEx> extendedFields,
			RuleResult ruleResult, ArrayList<String> fieldNameList, Hashtable<Field, RequestEx> extendedFields2, 
			Hashtable<String, String> dateFieldsMapping) throws DatabaseException {	
		
		if (fieldNameList != null){
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			Date when = new Date(c.getTimeInMillis());
			for (String fieldName : fieldNameList){
				Field dateField = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
				RequestEx reqEx = extendedFields.get(dateField);
				
				boolean hadNoPrevValue = false;
				if (extendedFields2 == null){
					hadNoPrevValue = true;
				}
				else{
					RequestEx oldReqEx = extendedFields2.get(dateField);
					if((oldReqEx == null) ||(oldReqEx.getDateTimeValue() == null)){					
						hadNoPrevValue = true;
					}
					/*else{
						//For revisited date Fields
						String associatedAckField = dateFieldsMapping.get(fieldName);
						if ((associatedAckField != null) && (!associatedAckField.trim().equals(""))){
							Field tmpActionField = Field.lookupBySystemIdAndFieldName(systemId, associatedAckField);
							if (tmpActionField != null){
								RequestEx tmpReqEx = extendedFields.get(tmpActionField);
								if ((tmpReqEx != null) && (tmpReqEx.getDateTimeValue() != null)){System.out.println("############Had prev values3");
									hadNoPrevValue = true;
								}
							}
						}
					}*/
				}
				
				
				if ((reqEx != null) && (reqEx.getDateTimeValue() != null) && hadNoPrevValue ){
					String fieldDateStr = "";
					String curDateStr = "";
					
					Timestamp ts = reqEx.getDateTimeValue();
					Timestamp ts1 = new Timestamp(c.getTimeInMillis());	
					//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
					//Date fieldDate = new Date(ts.getTime());
					//fieldDateStr = sdf.format(fieldDate);
					
					if ((ts != null) && (ts1 != null)){
						fieldDateStr = ts.toDateMin().substring(0,10);//toDateMin(ts).substring(0,10);
						curDateStr = ts1.toDateMin().substring(0,10);//toDateMin(ts1).substring(0,10);
					}
					System.out.println("fieldDate: " + fieldDateStr + ", curDate: " + curDateStr);
					if (ts != null){	
						if (fieldDateStr.equals(curDateStr) && (!fieldDateStr.equals("")) && (!curDateStr.equals(""))){
							ruleResult.setCanContinue(true);
							ruleResult.setSuccessful(true);
						}
						else if(ts.before(when)){
							if ((fieldName != null) && (fieldName.equals(TataPowerUtils.FIN_PAYMENT_DATE))){
								Field finRecieptDate = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
								return isValidFinancePaymentDate(systemId, extendedFields, finRecieptDate, ts, ruleResult);
							}
							else{
								ruleResult.setCanContinue(false);								
								ruleResult.setMessage("\"" + dateField.getDisplayName() + "\" date cannot occur in history," 
															+ " should be current day's date.");
								return ruleResult;
							}
						}
						else if(ts.after(when)){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("\"" + dateField.getDisplayName() + "\" date cannot occur in future, should" 
														+ " be current day's date.");
							return ruleResult;
						}
					}
				}
			}
		}
		ruleResult.setCanContinue(true);
		return ruleResult;
	}

	private RuleResult isValidFinancePaymentDate(int systemId,
			Hashtable<Field, RequestEx> extendedFields, Field finRecieptDate,
			Timestamp ts, RuleResult ruleResult)
			throws DatabaseException {
		
		if (finRecieptDate != null){
			RequestEx finRecieptReqEx = extendedFields.get(finRecieptDate);
			if (finRecieptReqEx != null){
				Timestamp finRecieptTS = finRecieptReqEx.getDateTimeValue();
				if (ts.before(finRecieptTS)){
					ruleResult.setCanContinue(false);								
					ruleResult.setMessage("\"" + finRecieptDate.getDisplayName() + "\" date cannot occur before, \"" 
												+ finRecieptDate.getDisplayName() 
												+ "\". Should be between the reciept date and current day's date.");
					return ruleResult;			
				}
			}
		}
		ruleResult.setCanContinue(true);
		ruleResult.setSuccessful(true);
		return ruleResult;
	}
	
	public static long getCurrentISTTime() {
		long millisSinceStart = Timestamp.getMillisSinceStart(TimeZone.getTimeZone("IST"));
		return millisSinceStart;
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
		return "HisotricalDates -  Checks if the dates do not occur in history";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		/*BusinessArea ba =BusinessArea.lookupBySystemPrefix("Bill");
		Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), 7);
		User root = User.lookupAllByUserId(1);
		IRule irule = new HistoricalDates();
		irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");*/
		/*ArrayList<String> fList = new ArrayList<String>();
		fList.add("field1,field2");
		fList.add("field3,field4");*/
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));		
		Timestamp ts = new Timestamp(c.getTimeInMillis());
		System.out.println();
		/*Date date = new Date(c.getTimeInMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");*/
		//System.out.println("Current date: " + sdf.format(date));
	}

}
