/**
 * 
 */
package kskED;

import java.sql.Connection;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import com.ibm.icu.text.SimpleDateFormat;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;

/**
 * @author lokesh
 *
 */
public class DOBRule implements IRule {
	
	private static final String IL = "IL";
	private static final String IL_EXPIRY = "ILExpiry";
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	private static final String ED = "ED";
	
	private enum DateFields {
		DOB("DOB"), ISSUE_DATE("issuedate"), PP_EXPIRY_DATE("PPExpiryDate");
		
		private String fieldName = "";
		private DateFields(String fieldName){
			this.fieldName = fieldName;
		}		
		public String getFieldName(){ return this.fieldName; }		
	}

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		
		if (ba.getSystemPrefix().equals(ED)){	
			for (DateFields df : DateFields.values()){
				if (df != null)
					setModifiedDate(df.getFieldName(), currentRequest);				
			}
		}
		else if (ba.getSystemPrefix().equals(IL))
			setModifiedDate(IL_EXPIRY, currentRequest);
		
		return ruleResult;
	}

	private static void setModifiedDate(String fieldName, Request currentRequest) {
		String curDateStr = currentRequest.get(fieldName);
		if ((curDateStr != null) && (curDateStr.trim().length() != 0))
		{
			SimpleDateFormat sdf = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);
			try {
				Date curDate = sdf.parse(curDateStr);				
				TimeZone   zone   = TimeZone.getDefault();
		    	Calendar cal = Calendar.getInstance(zone);
		    	cal.setTime(curDate);
		    	cal.add(Calendar.HOUR_OF_DAY, 9);
				Date changedDate = cal.getTime();				
				currentRequest.setObject(fieldName, changedDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		return 0;
	}
	
	public static void main(String[] args) throws APIException{
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> pTable = new Hashtable<String, String>();
		pTable.put(Field.BUSINESS_AREA, "KDI_LNT");
		pTable.put(Field.USER, "root");
		pTable.put(Field.SUBJECT, "From command line.");
		pTable.put("OwnerResponseDate", "2010-11-13 00:00:00");
		Request addRequest2 = addRequest.addRequest(pTable);
		System.out.println("%%Done: " + addRequest2.get("OwnerResponseDate"));
	}

}
