package logistics.com.tbitsGlobal.client;

public class LogisticsUtils {
	
	/**
	 * @param stageParams
	 * @return The String to be displayed on the Button and Window to Add/Update Components
	 */
	public static String getAddHeadingString(StageParams stageParams){
		if(stageParams.getPreStageComponentName() != null)
			return "Add " + stageParams.getPreStageComponentName();
		return "";
	}
}
