/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class DateFieldsAfterRevisiting implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {

		RuleResult ruleResult = new RuleResult();
		ruleResult.setCanContinue(true);

		//Current Status should have lesser index than the past, then this rule applies	
		//All subsequent fields should be nullified. 
		String sysPrefix = ba.getSystemPrefix();
		int systemId = ba.getSystemId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);

		if (isApplicable && (!isAddRequest)) {
			Type prevStatusId = oldRequest.getStatusId();
			Type curStatusId = currentRequest.getStatusId();
			String curStatusName = curStatusId.getName();

			Hashtable<Integer, String> fieldHierarchyHT = null;

			boolean isSetRecieptAndActionDates = false;
			String prevStatusName = prevStatusId.getName();

			int prevStatusIndex = 0;
			int curStatusIndex = 0;
			String reqTypeName = currentRequest.getRequestTypeId().getName();
			if (reqTypeName.startsWith("GRN") || reqTypeName.equals(TataPowerUtils.TYPE_DESP)){
				prevStatusIndex = TataPowerUtils.getStatusIndexGRN(prevStatusName);
				curStatusIndex = TataPowerUtils.getStatusIndexGRN(curStatusName);
				fieldHierarchyHT = TataPowerUtils.getFieldHierarchyGRN();
			}
			else{
				prevStatusIndex = TataPowerUtils.getStatusIndex(prevStatusName);
				curStatusIndex = TataPowerUtils.getStatusIndex(curStatusName);
				fieldHierarchyHT = TataPowerUtils.getFieldHierarchy();
			}
			
			
			if ((prevStatusName.equals(TataPowerUtils.STATUS_CLOSED) || 
					(!curStatusName.equals(TataPowerUtils.STATUS_CLOSED)))
					&& (prevStatusIndex > curStatusIndex)){
				isSetRecieptAndActionDates = setRecieptAndActionDatesToNull(
						extendedFields, systemId, fieldHierarchyHT,
						curStatusIndex);
				if (isSetRecieptAndActionDates)
					return ruleResult;
			}
		}
		return ruleResult;
	}

	/**
	 * @param extendedFields
	 * @param systemId
	 * @param fieldHierarchyHT
	 * @param currentIndex TODO
	 * @return TODO
	 */
	private boolean setRecieptAndActionDatesToNull(
			Hashtable<Field, RequestEx> extendedFields, int systemId,
			Hashtable<Integer, String> fieldHierarchyHT, int currentIndex) {
		boolean hasSetFields = false;
		Enumeration<Integer> keys = fieldHierarchyHT.keys();
		while (keys.hasMoreElements()) {
			Integer nextElement = keys.nextElement();
			if (nextElement >= currentIndex) {
				String fieldNames = fieldHierarchyHT.get(nextElement);
				int fieldCount = 1;
				for (String fieldName : fieldNames.split(",")) {					
					try {
						Field fieldDate = Field.lookupBySystemIdAndFieldName(
								systemId, fieldName);
						if (fieldDate != null){
							RequestEx recieptDateEx = extendedFields.get(fieldDate);
							if ((nextElement == currentIndex) && (fieldCount == 1)){
								recieptDateEx.setDateTimeValue(Timestamp.getGMTNow());
							}
							else  {							
								recieptDateEx.setDateTimeValue(null);								
							}
							extendedFields.put(fieldDate, recieptDateEx);
							hasSetFields = true;
						}
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					fieldCount++;
				}
			}
		}
		return hasSetFields;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName()
				+ "- Resets all the subsequent department dates if a department is revisited.";
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
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
