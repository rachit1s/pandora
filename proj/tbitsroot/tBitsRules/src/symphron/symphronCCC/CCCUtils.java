/**
 * 
 */
package symphronCCC;

import java.util.Arrays;
import java.util.List;

import transbit.tbits.domain.BusinessArea;

/**
 * In this class, we are checking if the logged BA is the concerned BA i.e. one of the BA
 * which is defined in "plugins.cmcc.rules.baList" App property in tbits.
 * @author Sajal
 *
 */
public class CCCUtils {
	/**
	 * @param baListStr
	 * @param ba
	 */
	public static boolean isApplicableBA(String baListStr, BusinessArea ba) {
		boolean isApplicableBA = false;
		if ((baListStr != null) && (baListStr.trim().length() != 0)){
			List<String> baList = Arrays.asList(baListStr.split(","));			
			if (baList.contains(ba.getSystemPrefix()))
				isApplicableBA  = true;
		}
		return isApplicableBA;
	}
	protected static final String PLUGIN_CMCC_RULES_BALIST = "plugins.cmcc.rules.baList";
}

