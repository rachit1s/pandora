package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class ILConstants {
	public static final ILServiceAsync dbService = GWT.create(ILService.class);

	public static HashMap<Integer, TbitsTreeRequestData> employees = new HashMap<Integer, TbitsTreeRequestData>();

//	public static String SYS_PREFIX = "ED";
}
