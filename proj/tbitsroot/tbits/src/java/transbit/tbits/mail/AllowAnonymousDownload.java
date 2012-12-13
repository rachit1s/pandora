package transbit.tbits.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

public class AllowAnonymousDownload implements IMailPreProcessor {

	private static final String EXTERNAL = "external";
	private static final String ALL = "all";
	private static final String NONE = "none";

	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) {
		// Deciding on the basis of the user type. If EXTERNAL.
		String allowTo = NONE;
		if(PropertiesHandler.getAppProperties().containsKey("AllowAnonymousDownload"))
			allowTo = (String) PropertiesHandler.getAppProperties().get("AllowAnonymousDownload");
		// Set the boolean value of isAnonymousDownload
		boolean isAnonymousDownload = false;
		if(allowTo.equals(NONE))
			isAnonymousDownload = false;
		else if(allowTo.equals(ALL))
			isAnonymousDownload = true;
		else if(allowTo.equals(EXTERNAL) && (user.getUserTypeId() == UserType.EXTERNAL_USER))
			isAnonymousDownload = true;
		
		// Iterate over all the  ActionFileInfos and set the value of isAnonymousDownload
		Enumeration<Integer> allKeys = actionFileHash.keys();
		while(allKeys.hasMoreElements()){
			int key = allKeys.nextElement();
			Collection<ActionFileInfo> currCollection = actionFileHash.get(key);
			for(ActionFileInfo afi : currCollection){
				afi.setAnonymousDownload(isAnonymousDownload);
			}
		}
	}

	public String getMailPreProcessorName() {
		return "Decide the download link based on AllowAnonymousDownload properties.";
	}

	public double getMailPreProcessorOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
