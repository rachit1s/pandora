package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

public interface ILServiceAsync {
	void createTransmittal(String sysPrefix, HashMap<Integer, TbitsTreeRequestData> data,
			ArrayList<String[]> scheduleList,
			HashMap<String, String> paramTable, AsyncCallback<Boolean> callback);

	void getPdfPreviewPath(String sysPrefix, HashMap<Integer, TbitsTreeRequestData> users,
			ArrayList<String[]> scheduleList,
			HashMap<String, String> paramTable, boolean hasToBePublic,
			String outputFormat, AsyncCallback<ArrayList<String>> callback);

	void verifyEmployees(String sysPrefix, String inviType,
			HashMap<Integer, TbitsTreeRequestData> employees,
			AsyncCallback<String> callback);

	void getValidBAList(AsyncCallback<List<String>> asyncCallback);

	void getTextStrings(String sysPrefix, AsyncCallback<HashMap<String, String>> callback);

	void getPage1Fields(String sysPrefix, AsyncCallback<List<BAField>> callback);
}
