/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.DefaultHolidayCalendar;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * CommentDateBasedDueDate is the plug-in which ensures, if the status change to 'Approved/Approved With Comments/Rejected'
 * can happen only if the previous status was 'Submitted To Client'. 
 * 
 * Also ensures that, the comment date field is provided with a value and not is not empty, if the previous status was 
 * 'Submitted To Client' and now the status is changing to 'Approved/Approved With Comments/Rejected'. 
 * 
 * Based on the comment date, the due date is set accordingly.
 *
 */
public class CommentDateBasedDueDate implements IRule {

	//Status names
	private static final String RE_SUBMISSION_REQUIRED = "ReSubmissionRequired";
	private static final String APPROVED_WITH_COMMENTS = "ApprovedWithComments";
	private static final String APPROVED = "Approved";
	private static final String SUBMITTEDTOCLIENT = "submittedtoclient";

	//Field names
	private static final String CLIENT_DECISION_DATE = "ClientDecisionDate";

	/**
	 * 
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(ba.getSystemPrefix());
		int systemId = ba.getSystemId();
		Type curStatusType = currentRequest.getStatusId();
		String curStatus = curStatusType.getName();
		boolean isPermissibleCurStatus = curStatus.equals(RE_SUBMISSION_REQUIRED) || 
											curStatus.equals(APPROVED) ||
											curStatus.equals(APPROVED_WITH_COMMENTS);
		
		if (isApplicable && isPermissibleCurStatus){
			Field commentDateField;
			try {
				commentDateField = Field.lookupBySystemIdAndFieldName(systemId, CLIENT_DECISION_DATE);
				RequestEx commentDateReqEx = extendedFields.get(commentDateField);
				if (commentDateReqEx != null){
					Type prevStatusType = oldRequest.getStatusId();
					String prevStatusName = prevStatusType.getName();
					if (prevStatusName.equals(SUBMITTEDTOCLIENT)){
						Timestamp cdTimeStamp = commentDateReqEx.getDateTimeValue();
						if (cdTimeStamp == null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Please set comment date");
						}
						else{
							Date date = new Date(cdTimeStamp.getTime());
							int dueDateOffset = PyramidUtils.getPropertyDueDateOffset();
							Date dueDate = CalenderUtils.slideDate(date, dueDateOffset, new DefaultHolidayCalendar());	
							Timestamp duedateTS = Timestamp.getTimestamp(dueDate);
							currentRequest.setDueDate(duedateTS);
							ruleResult.setCanContinue(true);
							ruleResult.setSuccessful(true);	
						}
					}
					else if(prevStatusName.equals(RE_SUBMISSION_REQUIRED)||
							prevStatusName.equals(APPROVED)|| 
							prevStatusName.equals(APPROVED_WITH_COMMENTS)){
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Previous status was the same as current status hence continuing.");
					}
					else{
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Cannot set the status to 'Approved/Approved with comments/Rejected' since the previous status was not 'Submitted to client'");
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Not applicable since required comment date field was not found.");
				}				
			} catch (DatabaseException e) {
				e.printStackTrace();
				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Error occurred while retrieving field: " + CLIENT_DECISION_DATE);
				ruleResult.setSuccessful(false);
			}			
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable");
		}
		return ruleResult;	
	}
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "CommentDateBasedDueDate - Slides the due date to 7 days from the comment date " +
		"if status is \"approved/approved with comment/rejected\" and the previous action's status" +
		" is \"submitted to client\"";
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
