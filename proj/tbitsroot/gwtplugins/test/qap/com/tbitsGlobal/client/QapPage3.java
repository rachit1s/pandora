package qap.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import transmittal.com.tbitsGlobal.client.DistributionDataSelectionPage;
import transmittal.com.tbitsGlobal.client.DistributionTableColumnsConfig;
import transmittal.com.tbitsGlobal.client.TransmittalConstants;
import transmittal.com.tbitsGlobal.client.TransmittalWizardConstants;
import transmittal.com.tbitsGlobal.client.WizardData;

import com.axeiya.gwtckeditor.client.CKConfig;
import com.axeiya.gwtckeditor.client.CKEditor;
//import com.cimmetry.core.User;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.Window;
import com.tbitsGlobal.jaguar.client.cache.UserCache;

import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.DateTimeControl;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class QapPage3 extends AbstractWizardPage<FormPanel, HashMap<String, String>> {

	public QapDataWizard qapDataObject= new QapDataWizard();
	
	private static final String NOTIFY = "notify";
	private static final String FROM = "logger_ids";
	private static final String ACCESS_TO = "AccessTo";
	private static final String DRAFTED_BY = "draftedBy";
	private static final String EMAIL_BODY = "emailBody";
	private static final String SUBJECT = "subject";
	private static final String CC_LIST = "subscriber_ids";
	private static final String OS_Unit1 = "OS_Unit";
	private static final String QA_Ref_No1 = "QA_Ref_No";
	private static final String Shipping_Release_No1 = "Shipping_Release_No";
	private static final String Office_SR_No1 = "Office_SR_No";
	private static final String QA_Ref_No_for_Main_Order1 = "QA_Ref_No_for_Main_Order";
	private static final String TO_LIST = "assignee_ids";
	
//	public String tolist;
//	public String cclist;
//	public String subject;
	
	HashMap<String, String> fieldvaluesMap;
	private UserPicker toList; // , from;
	TextField<String> from;
	Label validationResult;
	private UserPicker ccList;
	private UserPicker accessTo;
	private TextField<String> Subject;
	private TextField<String> OS_Unit;
	private TextField<String> QA_Ref_No;
	private TextField<String> Shipping_Release_No;
	private TextField<String> Office_SR_No;
	private TextField<String> QA_Ref_No_for_Main_Order;
	private CKEditor emailBody;
	private HashMap<String, Integer> stickinessMap = new HashMap<String, Integer>();
	private ArrayList<TbitsModelData> validationRulesList = new ArrayList<TbitsModelData>();
	boolean isCanContinue = true;
	private CheckBox notify;
	HashMap<Integer, TbitsTreeRequestData> requestData;
	ArrayList<TbitsTreeRequestData> requests;
	private String listOfCC = null;
	protected QapPage3(UIContext wizardContext) {
		super(wizardContext);
		// TODO Auto-generated constructor stub
	}

	public QapPage3(DefaultUIContext context,
			ArrayList<TbitsTreeRequestData> requests) {
		super(context);
		this.requests=requests;
		buildPage();
		// TODO Auto-generated constructor stub
	}
	
//	public QapDataWizard getDataObject() {
//		return qapDataObject;
//	}
//
//	public void setDataObject(QapDataWizard data) {
//		this.qapDataObject = data;
//	}
	
	public void setValues(){
		    HashMap<String, Object> hm = new HashMap<String, Object>();
			String selectedCCList = (ccList.getStringValue() == null) ? ""
					: ccList.getStringValue().trim();
			
			String selectedtoList = (toList.getStringValue() == null) ? ""
					: toList.getStringValue().trim();

			String listOfActiveCC = "";
			String listOfActiveto = "";
			HashSet<String> userSet = new HashSet<String>();
			HashSet<String> userSet1 = new HashSet<String>();
			String nonActiveCC = "";
			String nonActiveto = "";
			String msg = "These are the users that are no longer active :"
					+ "\n";

			UserCache userCache = CacheRepository.getInstance().getCache(
					UserCache.class);

			for (String userLogin : selectedCCList.trim().split(",")) {
				if (!userLogin.trim().equals("")) {
					userLogin = userLogin.trim();

					UserClient uc = userCache.getObject(userLogin);

					if (uc != null)
						userSet.add(userLogin);
					else {
						if (nonActiveCC.equals("")) {
							nonActiveCC = nonActiveCC + userLogin;
						} else {
							nonActiveCC = nonActiveCC + "," + userLogin;
						}
					}
				}
			}
			
			for (String userLogin : selectedtoList.trim().split(",")) {
				if (!userLogin.trim().equals("")) {
					userLogin = userLogin.trim();

					UserClient uc = userCache.getObject(userLogin);

					if (uc != null)
						userSet1.add(userLogin);
					else {
						if (nonActiveto.equals("")) {
							nonActiveto = nonActiveto + userLogin;
						} else {
							nonActiveto = nonActiveto + "," + userLogin;
						}
					}
				}
			}
			if (!nonActiveCC.equals("")) {
				Window.alert(msg + nonActiveCC);
				Log.info(msg + nonActiveCC);
			}
			
			if (!nonActiveto.equals("")) {
				Window.alert(msg + nonActiveto);
				Log.info(msg + nonActiveto);
			}
			if (userSet.size() > 0) {
				for (String userLogin : userSet) {
					if (listOfActiveCC.equals("")) {
						listOfActiveCC = listOfActiveCC + userLogin;
					} else {
						listOfActiveCC = listOfActiveCC + "," + userLogin;
					}
				}
			}
			
			if (userSet.size() > 0) {
				for (String userLogin : userSet1) {
					if (listOfActiveto.equals("")) {
						listOfActiveto = listOfActiveto + userLogin;
					} else {
						listOfActiveto = listOfActiveto + "," + userLogin;
					}
				}
			}

			//getDataObject().getData().put(ccList.getName(), listOfActiveCC);

//			if (ccList.getStringValue() == null) {
//				listOfCC = "";
//			} else {
//				listOfCC = listOfActiveCC;
//			}
            hm.put("from", from.getValue());
            hm.put("toList", listOfActiveto);
			hm.put("ccList", listOfActiveCC);
			hm.put("accessTo",(accessTo.getStringValue() == null) ? "" : accessTo.getStringValue().trim());
            hm.put("notify",String.valueOf(notify.getValue()));
			hm.put("emailBody", emailBody.getHTML());
			hm.put("subject", Subject.getValue());
			hm.put("OS_Unit", OS_Unit.getValue());
			hm.put("QA_Ref_No", QA_Ref_No.getValue());
			hm.put("Shipping_Release_No", Shipping_Release_No.getValue());
			hm.put("Office_SR_No", Office_SR_No.getValue());
			hm.put("QA_Ref_No_for_Main_Order", QA_Ref_No_for_Main_Order.getValue());
			qapDataObject.setData(hm);
			}
			
	

	@Override
	public FormPanel getWidget() {
		// TODO Auto-generated method stub
		return widget;
	}

	@Override
	public void initializeWidget() {
		// TODO Auto-generated method stub
		widget = new FormPanel();
		widget.setLabelWidth(150);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);
		widget.setHeading("Information Page");
		widget.setFrame(true);
	}

	@Override
	public void buildPage() {
		int i=0,j=0;
		ArrayList<String> assignees = new ArrayList<String>();
		ArrayList<String> subscribers = new ArrayList<String>();
		ArrayList<String> loggers = new ArrayList<String>();
		ArrayList<TbitsTreeRequestData> requests = this.requests;
		for(TbitsTreeRequestData trd:requests)
		{
			String Assignee =trd.getAsString("assignee_ids");
			String Subscriber =trd.getAsString("subscriber_ids");
//			String Logger =trd.getAsString("logger_ids");
			if(assignees.isEmpty())
				assignees.add(Assignee);
			else
			{
				for(String a :assignees )
				{
					if(a.equalsIgnoreCase(Assignee))
						i++;
				}
				if(i==0)
					assignees.add(Assignee);
			}
			i=0;
			
			if(subscribers.isEmpty())
				subscribers.add(Subscriber);
			else
			{
				for(String a :subscribers )
				{
					if(a.equalsIgnoreCase(Subscriber))
						j++;
				}
				if(j==0)
					subscribers.add(Subscriber);
			}
			j=0;
			
//			if(loggers.isEmpty())
//				loggers.add(Logger);
//			else
//			{
//				for(String a :loggers )
//				{
//					if(a.equalsIgnoreCase(Logger))
//						k++;
//				}
//				if(k==0)
//					loggers.add(Logger);
//			}
//			k=0;
			
		}
		String assignee = "";
		String subscriber = "";
		String logger = ClientUtils.getCurrentUser().getUserLogin();
		for(String a :assignees )
			assignee = a + "," + assignee ;	
				
		for(String a :subscribers )
			subscriber = a + "," + subscriber;
						
//		for(String a :loggers )
//			logger = a + "," + logger;
		final FormData formData = new FormData();
		formData.setWidth(700);
		
		final FormData formData1 = new FormData();
		formData1.setWidth(300);
		
		ListStore<UserClient> store = new ListStore<UserClient>();
		
		OS_Unit = new TextField<String>();
		OS_Unit.setFieldLabel("OS Unit");
		OS_Unit.setName(OS_Unit1);
		widget.add(OS_Unit, formData1);
		
		QA_Ref_No = new TextField<String>();
		QA_Ref_No.setFieldLabel("QA Ref No");
		QA_Ref_No.setName(QA_Ref_No1);
		widget.add(QA_Ref_No, formData1);
		
		Shipping_Release_No = new TextField<String>();
		Shipping_Release_No.setFieldLabel("Shipping Release No");
		Shipping_Release_No.setName(Shipping_Release_No1);
		widget.add(Shipping_Release_No, formData1);
		
		Office_SR_No = new TextField<String>();
		Office_SR_No.setFieldLabel("Office SR No");
		Office_SR_No.setName(Office_SR_No1);
		widget.add(Office_SR_No, formData1);
		
		QA_Ref_No_for_Main_Order = new TextField<String>();
		QA_Ref_No_for_Main_Order.setFieldLabel("QA Ref No for Main Order");
		QA_Ref_No_for_Main_Order.setName(QA_Ref_No_for_Main_Order1);
		widget.add(QA_Ref_No_for_Main_Order, formData1);

		from = new TextField<String>();
		from.setFieldLabel("From");
		from.setName(FROM);
		from.setValue(logger);
		widget.add(from, formData1);

		toList = new UserPicker(
				(BAFieldMultiValue) TransmittalConstants.fieldCache
						.getObject(IFixedFields.ASSIGNEE));
		toList.setFieldLabel("To");
		toList.setName(TO_LIST);
		toList.setStringValue(assignee);
		widget.add(toList, formData);
//		this.tolist = toList.getStringValue();
//		System.out.print(this.tolist);
//		UserClient abc = toList.getValue();
//		System.out.print(abc);

		ccList = new UserPicker(
				(BAFieldMultiValue) TransmittalConstants.fieldCache
						.getObject(IFixedFields.SUBSCRIBER));
		ccList.setFieldLabel("Cc");
		ccList.setStringValue(subscriber);
		widget.add(ccList, formData);
//		this.cclist = toList.getStringValue();
//		System.out.print(this.cclist);

		accessTo = new UserPicker(
				(BAFieldMultiValue) TransmittalConstants.fieldCache
						.getObject(IFixedFields.SUBSCRIBER));
		accessTo.setFieldLabel("Access To");
		accessTo.setName(ACCESS_TO);

		widget.add(accessTo, formData);

		notify = new CheckBox();
		notify.setFieldLabel("Notify");
		notify.setName(NOTIFY);
		notify.setBoxLabel("");
		notify.setToolTip("Enable/disable email notification");
		widget.add(notify, formData);
		validationResult = new Label();
		validationResult.setPosition(0, 0);
		validationResult.setStyleAttribute("position", "relative");
		validationResult.setStyleAttribute("color", "red");
		widget.add(validationResult);
		
		Subject = new TextField<String>();
		Subject.setFieldLabel("Subject");
		Subject.setName(SUBJECT);
		widget.add(Subject, formData);
//		this.subject = toList.getStringValue();
//		System.out.print(this.subject);
//		String subject = toList.getStringValue();
//		System.out.print(subject);

		LabelField labelField = new LabelField();
		labelField.setFieldLabel("Email Body:");
		widget.add(labelField, new FormData("-20"));

		CKConfig ckc = new CKConfig();
		ckc.setResizeMinHeight(100);
		ckc.setWidth("860px");
		ckc.setHeight("100px");
		ckc.setToolbar(CKConfig.PRESET_TOOLBAR.BASIC);
		emailBody = new CKEditor(ckc);

		widget.add(emailBody, formData);
		widget.layout(true);
		String a = from.getValue();
		String b = Subject.getValue();
		fieldvaluesMap = new HashMap<String, String>();
		fieldvaluesMap.put("logger", from.getValue());
		fieldvaluesMap.put("subject", Subject.getValue());
		
	}

	@Override
	public int getDisplayOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisplay() {
		// TODO Auto-generated method stub
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).hideBackButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).hideFinishButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showNextButton();
	}

	@Override
	public boolean onLeave() {
		// TODO Auto-generated method stub
		String tolist = toList.getStringValue();
		System.out.print(tolist);
		if ((toList.getStringValue() == null)
				|| toList.getStringValue().trim().equals("")) {
			Window.alert("'To' field cannot be empty. Please select a user.");
			return false;
		}
		this.setValues();
//		HashMap<String, Object> hmabc =qapDataObject.getData();
	    return true;
	}

	@Override
	public HashMap<String, String> getValues() {
		// TODO Auto-generated method stub
		HashMap<String, String> hmpg3 = new HashMap<String, String>();
		HashMap<String, Object> hmabc =qapDataObject.getData();
		hmpg3.put("from", (String)hmabc.get("from"));
		hmpg3.put("toList", (String)hmabc.get("toList"));
		hmpg3.put("ccList", (String)hmabc.get("ccList"));
		hmpg3.put("accessTo", (String)hmabc.get("accessTo"));
		hmpg3.put("notify", (String)hmabc.get("notify"));
		hmpg3.put("subject", (String)hmabc.get("subject"));
		hmpg3.put("emailBody", (String)hmabc.get("emailBody"));
		hmpg3.put("OS_Unit", (String)hmabc.get("OS_Unit"));
		hmpg3.put("QA_Ref_No", (String)hmabc.get("QA_Ref_No"));
		hmpg3.put("Shipping_Release_No", (String)hmabc.get("Shipping_Release_No"));
		hmpg3.put("Office_SR_No", (String)hmabc.get("Office_SR_No"));
		hmpg3.put("QA_Ref_No_for_Main_Order", (String)hmabc.get("QA_Ref_No_for_Main_Order"));
		return hmpg3;
	}

}
