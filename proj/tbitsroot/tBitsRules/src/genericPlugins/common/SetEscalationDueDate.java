package common;
import java.sql.Connection;
import java.util.Date;

import transbit.tbits.Escalation.EscalationUtils;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.SeverityBasedEscalation;


public class SetEscalationDueDate implements IRule {
	public static final TBitsLogger LOG = TBitsLogger
	.getLogger(TBitsConstants.PKG_COMMON);
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {
		if(isAddRequest)
		{
			if(currentRequest.getDueDate() == null)
			{
				try {
					int span;

					span = SeverityBasedEscalation
							.getEscalationSpan(currentRequest);
					Date dueDate = EscalationUtils.getNextDueDate(span);
					if(dueDate != null)
					{
						Timestamp dueDateTs = Timestamp.getTimestamp(dueDate);
						System.out.println("DueDate TS: " + dueDateTs);
						//currentRequest.setDueDate(dueDateTs);
						currentRequest.setDueDate(dueDate);
					}
				} catch (TBitsException e) {
					LOG.error(e);
				}
			}
			return new RuleResult(true, "Update the due date successfully.", true);
		}
		return new RuleResult(true, "Skipping update since its update.", true);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Sets the due date on which the request should be escalated.";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
