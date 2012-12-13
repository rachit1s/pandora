/**
 * 
 */
package dcn.com.tbitsGlobal.client.utils;

import java.util.ArrayList;

import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteClientUtils {
	public static boolean isExistsInListAsTargetBA(
			ArrayList<ChangeNoteConfig> changeNoteConfigList, String sysPrefix) {
		
		if (changeNoteConfigList != null)
			for (ChangeNoteConfig cnc : changeNoteConfigList){
				if (cnc.getTargetSysPrefix().trim().equals(sysPrefix))
					return true;
			}
		
		return false;
	}
	
	public static boolean isExistsInListAsSourceBA(
			ArrayList<ChangeNoteConfig> changeNoteConfigList, String sysPrefix) {
		
		if (changeNoteConfigList != null)
			for (ChangeNoteConfig cnc : changeNoteConfigList){
				if (cnc.getSrcSysPrefix().trim().equals(sysPrefix))
					return true;
			}
		
		return false;
	}
	
	public static ChangeNoteConfig getChangeNoteConfigFromListUsingSrcSysPrefix(
			ArrayList<ChangeNoteConfig> changeNoteConfigList, String sysPrefix) {
		
		if (changeNoteConfigList != null)
			for (ChangeNoteConfig cnc : changeNoteConfigList){
				if (cnc.getSrcSysPrefix().trim().equals(sysPrefix))
					return cnc;
			}
		
		return null;
	}
	
	public static ChangeNoteConfig getChangeNoteConfigFromListUsingSrcSysPrefixAndBAType(
			ArrayList<ChangeNoteConfig> changeNoteConfigList, String sysPrefix, String baType) {
		
		if (changeNoteConfigList != null)
			for (ChangeNoteConfig cnc : changeNoteConfigList){
				if (cnc.getSrcSysPrefix().trim().equals(sysPrefix) 
						&& cnc.getBaType().trim().equals(baType))
					return cnc;
			}
		
		return null;
	}
	
	public static ChangeNoteConfig getChangeNoteConfigFromListUsingTargetSysPrefix(
			ArrayList<ChangeNoteConfig> changeNoteConfigList, String sysPrefix) {
		
		if (changeNoteConfigList != null)
			for (ChangeNoteConfig cnc : changeNoteConfigList){
				if (cnc.getTargetSysPrefix().trim().equals(sysPrefix))
					return cnc;
			}
		
		return null;
	}
}
