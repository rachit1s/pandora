package transbit.tbits.scheduler;

import java.util.Hashtable;

import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.Request;

public interface EscalationHandler {
	/**
	 * Is executed just before the escalation actually updates the request.
	 * @param updateFieldValues The fields that will be passed to update request. You can change this.
	 * @param request The request which is being updated. Changing this wouldnt matter.
	 */
	public RuleResult beforeEsclation(Hashtable<String, String> updateFieldValues, Request request);
}
