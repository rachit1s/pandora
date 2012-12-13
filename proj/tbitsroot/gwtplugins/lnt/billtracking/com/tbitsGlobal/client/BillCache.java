package billtracking.com.tbitsGlobal.client;

import java.util.HashMap;

import billtracking.com.tbitsGlobal.client.events.OnBillPropertiesReceived;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.log.Log;
/*
 * Singleton class containing all the properties and roles related info
 */

public class BillCache implements IBillConstants,IBillProperties {
	private static final String ROLE_TRANSFER_BILL_SUBMISSION = "TransferBillSubmission";
	private static final String ROLE_GRN_CHECK = "GrnCheck";
	private static final String ROLE_VERIFY = "Verify";
	
	private static BillCache instance;
	private Boolean isVerifyer;
	private Boolean grnCheck;
	private Boolean allowTransferBillSubmission;
	private HashMap<String,String> billProperties;
	protected TbitsObservable observable;


	public Boolean getIsVerifyer() {
		return isVerifyer;
	}

	public HashMap<String, String> getBillProperties() {
		return billProperties;
	}

	private BillCache(){
		billProperties = new HashMap<String, String>();
		isVerifyer = false;
		grnCheck = false;
		allowTransferBillSubmission = false;
		attachEvents();
		load();
	}

	private void attachEvents() {
		observable = new BaseTbitsObservable();
		observable.attach();
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>() {
			@Override
			public void handleEvent(OnChangeBA event) {
				String sysPrefix=event.getSysPrefix();
				if(
						sysPrefix.equals(billProperties.get(PROPERTY_BILL_BA_PREFIX))
						|| sysPrefix.equals(billProperties.get(PROPERTY_GRN_BA_PREFIX))
						|| sysPrefix.equals(billProperties.get(PROPERTY_PO_BA_PREFIX))
						|| sysPrefix.equals(billProperties.get(PROPERTY_SUBMISSION_BA_PREFIX))
					)
				{
					load();
				}
			}
		});

		observable.subscribe(OnBillPropertiesReceived.class, new ITbitsEventHandle<OnBillPropertiesReceived>() {
			@Override
			public void handleEvent(OnBillPropertiesReceived event) {
				loadVerifyStatus();
				loadGrnCheckStatus();
				loadTransferBillPermission();
			}
		});
	}

	private void loadVerifyStatus() {
		billService.belongsToRole(billProperties.get(PROPERTY_BILL_BA_PREFIX),ClientUtils.getCurrentUser().getUserLogin(),ROLE_VERIFY,new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.error("error getting role Info",caught);

			}

			@Override
			public void onSuccess(Boolean result) {
				isVerifyer=result;
				Log.info("got verifyer status");
			}
		});
	}

	private void loadGrnCheckStatus() {
		billService.belongsToRole(billProperties.get(PROPERTY_BILL_BA_PREFIX),ClientUtils.getCurrentUser().getUserLogin(),ROLE_GRN_CHECK,new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.error("error getting role Info",caught);

			}

			@Override
			public void onSuccess(Boolean result) {
				grnCheck=result;
				Log.info("got GrnCheck status");
			}
		});
	}
 
	private void loadTransferBillPermission() {
		billService.belongsToRole(billProperties.get(PROPERTY_SUBMISSION_BA_PREFIX),ClientUtils.getCurrentUser().getUserLogin(),ROLE_TRANSFER_BILL_SUBMISSION,
				new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.error("error getting role Info",caught);

			}

			@Override
			public void onSuccess(Boolean result) {
				allowTransferBillSubmission = result;
				Log.info("got GrnCheck status");
			}
		});
	}
 /*
  * loads the properties when BA is changed and is a BillTracking BA
  */

	public void load() {
		billService.getBillProperties(new AsyncCallback<HashMap<String,String>>() {
			public void onSuccess(HashMap<String, String> result) {
				billProperties = result;
				Log.info("got bill properties");
				TbitsEventRegister.getInstance().fireEvent(new OnBillPropertiesReceived());
			}

			public void onFailure(Throwable arg0) {
				Log.error("error getting bill properties",arg0);

			}
		});
	}

	public static BillCache getInstance(){
		if(instance == null)
			instance = new BillCache();
		return instance;
	}


	public Boolean getGrnCheck() {
		return grnCheck;
	}
	
	public Boolean isTransferBillAllowed()
	{
		return allowTransferBillSubmission;
	}

}
