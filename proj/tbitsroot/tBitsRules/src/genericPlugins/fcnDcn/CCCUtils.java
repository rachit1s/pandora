/**
 * 
 */
package fcnDcn;

import java.util.Arrays;
import java.util.List;

import transbit.tbits.domain.BusinessArea;

/**
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
	protected static final String PLUGIN_CCC_RULES_BALIST = "plugins.CCC.rules.baList";
}
