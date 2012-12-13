package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

public interface ILService extends RemoteService {
	boolean createTransmittal(String sysPrefix, HashMap<Integer, TbitsTreeRequestData> data,
			ArrayList<String[]> scheduleList, HashMap<String, String> paramTable) throws TbitsExceptionClient;

	ArrayList<String> getPdfPreviewPath(String sysPrefix, 
			HashMap<Integer, TbitsTreeRequestData> users,
			ArrayList<String[]> scheduleList,
			HashMap<String, String> paramTable, boolean hasToBePublic,
			String outputFormat) throws TbitsExceptionClient;

	String verifyEmployees(String sysPrefix, String inviType,
			HashMap<Integer, TbitsTreeRequestData> employees) throws TbitsExceptionClient;

	List<String> getValidBAList() throws TbitsExceptionClient;
	
	HashMap<String, String> getTextStrings(String sysPrefix) throws TbitsExceptionClient;
	
	List<BAField> getPage1Fields(String sysPrefix) throws TbitsExceptionClient;
}
