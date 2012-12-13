package expressit;

import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.scheduler.EscalationHandler;

public class ImprestEscalationHandler implements EscalationHandler {
	private static final String ESCALATE_TO = "escalate_to";
	public static final TBitsLogger LOG = TBitsLogger
	.getLogger(TBitsConstants.PKG_SCHEDULER);
	
	public RuleResult beforeEsclation( Hashtable<String, String> updateFieldValues, 
			Request request) 
	{
		RuleResult rr = new RuleResult(true, "put certain people into subscriber from escalate_to", true);
		// TODO Auto-generated method stub
		Field escalateToF;
		try {
			escalateToF = Field.lookupBySystemIdAndFieldName(request.getSystemId(), ESCALATE_TO);
			if(escalateToF == null)
			{
				LOG.warn("Since the system doesnt have a field called escalate_to. Skipping the rule.");
				return rr;
			}
			
			Hashtable<Field, RequestEx> extFields = request.getExtendedFields();
			if(extFields != null)
			{
				RequestEx re = extFields.get(escalateToF);
				if(re != null)
				{
					String value = re.getVarcharValue();
					if((value != null) && (value.trim().length() > 0))
							updateFieldValues.put(Field.SUBSCRIBER, value);
				}
			}
		} catch (DatabaseException e) {
			LOG.error("Error while getting db fields ", e);
			e.printStackTrace();
		}
		
		return rr;
	}
}
