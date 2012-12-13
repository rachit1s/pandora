package expressit;

import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.scheduler.EscalationHandler;

public class ImprestActiveRequestsEscalationHandler implements EscalationHandler {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_EXTERNAL);
	
	public RuleResult beforeEsclation(
			Hashtable<String, String> updateFieldValues, Request request) {
		
		String configuredBA = BillsRecievedInHo.getConfiguredBusinessArea();
		if(configuredBA != null)
		{
			BusinessArea ba = null;
			try {
				ba = BusinessArea.lookupBySystemId(request.getSystemId());
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				LOG.error("Error while retrieveing the ba", e);
			}
			if(ba != null)
			{
				if (request.getStatusId().getName().equalsIgnoreCase("Active")) {
					String msg = "Your Imprest Statement not received in HO & is overdue. We also request you to track your letter via NR Number & Call 022-30813091 / 30813045 to give us the status update.\r\n";
					if (updateFieldValues.containsKey(Field.DESCRIPTION)) {
						msg = msg + updateFieldValues.get(Field.DESCRIPTION);
					}
					updateFieldValues.put(Field.DESCRIPTION, msg);
				}
			}
			else
			{
				LOG.error("Unable to get the business area.");
			}
		}
		else
		{
			LOG.error("Unable to find the configured business area.");
		}
		return new RuleResult(true, "ImprestActiveRequestsEscalationHandler", true);
	}

}
