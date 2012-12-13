package gmr;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * This class is responsible for the factor changes for GMR_PTS business area.<br>
 * <br>
 * The rules are as follows :<br>
 * <table>
 * <tr><td width="300">status_id (field_id=4)</td><td width="300">DocumentCategory (field_id=58)</td><td width="300">OwnerDecisionToOC (field_id=109)</td><td width="300">v_Factor (field_id=143)</td></tr>
 * <tr><td><b>PendingReceipt</b> > <b>UnderReview</b></td><td><b>Approval</b></td><td> - </td><td><b>50%</b></td></tr>
 * <tr><td></td><td><b>Information</b></td><td> - </td><td><b>100%</b></td></tr>
 * <tr><td><b>UnderReview</b> > <b>ReturnedWithDecision</b></td><td><b>Approval</b></td><td><b>5</b></td><td><b>50%</b></td></tr>
 * <tr><td></td><td></td><td><b>4</b></td><td><b>60%</b></td></tr>
 * <tr><td></td><td></td><td><b>3</b></td><td><b>80%</b></td></tr>
 * <tr><td></td><td></td><td><b>2</b></td><td><b>90%</b></td></tr>
 * <tr><td></td><td></td><td><b>1</b></td><td><b>95%</b></td></tr>
 * <tr><td><b>UnderReview</b> > <b>ReturnedWithDecision</b></td><td>-</td><td><b>AsBuilt</b></td><td><b>100%</b></td></tr>
 * </table>
 * <br>
 * Finally, the following computation is made : <br>
 * <b>ActualComplete (field_id=141) = Weightage (field_id=140) * v_Factor (field_id=143)</b>
 * <br> to calculate the actual complete.
 * 
 * @author Karan Gupta
 *
 */
public class GmrWeightageComputationRule implements IRule{
	public static TBitsLogger LOG = TBitsLogger.getLogger("GmrWeightageRule");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
	
		// return if not in relevant BusinessArea
		if(!ba.getSystemPrefix().equalsIgnoreCase("GMR_PTS"))
			return new RuleResult();
		
		// Determine the new factor as per the rule
		String oldStatus = oldRequest == null ? "PendingReceipt" : oldRequest.get("status_id");
		String newStatus = currentRequest.get(Field.STATUS);
		String docCategory = null;
		String ownerDesc = null;
		int factor = 0;
		
		if(oldStatus != null && newStatus != null){
			
			docCategory = currentRequest.get("DocumentCategory");
			
			if(oldStatus.equalsIgnoreCase("PendingReceipt") && newStatus.equalsIgnoreCase("UnderReview")){
				if(docCategory != null && docCategory.equalsIgnoreCase("Approval")){
					factor = 50;
				}
				else if(docCategory != null && docCategory.equalsIgnoreCase("Information")){
					factor = 100;
				}
			}
			else if(newStatus.equalsIgnoreCase("ReturnedWithDecision")){
				ownerDesc = currentRequest.get("OwnerDecisionToOC");
				if(ownerDesc != null){
					if(ownerDesc.equalsIgnoreCase("AsBuilt"))
						factor = 100;
					else if(docCategory != null && docCategory.equalsIgnoreCase("Approval")){
						if(ownerDesc.equalsIgnoreCase("5"))
							factor = 50;
						else if(ownerDesc.equalsIgnoreCase("4"))
							factor = 60;
						else if(ownerDesc.equalsIgnoreCase("3"))
							factor = 90;
						else if(ownerDesc.equalsIgnoreCase("2"))
							factor = 95;
						else if(ownerDesc.equalsIgnoreCase("1"))
							factor = 97;
					}
				}
			}
		}
			
		try {
			if(factor != 0){
				Field factorField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "v_Factor");
				Field weightageField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "Weightage");
				Field actualCompleteField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "ActualComplete");
			
				currentRequest.setObject(factorField, new Double(factor));
				double weightage = (Double) currentRequest.getObject(weightageField);
				currentRequest.setObject(actualCompleteField, new Double((double)(factor*weightage)/100));
			}
		}
		catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
			RuleResult rr = new RuleResult(true, "Unable to calculate the weightages for "+ba.getSystemPrefix()+". Reason: " + e.getMessage() + "\r\n" + e.getDescription());
			rr.setSuccessful(false);
		}
		
		RuleResult rr = new RuleResult();
		rr.setCanContinue(true);
		rr.setSuccessful(true);
		return rr;
	}

	public String getName() {
		return "GmrWeightageComputationRule";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
