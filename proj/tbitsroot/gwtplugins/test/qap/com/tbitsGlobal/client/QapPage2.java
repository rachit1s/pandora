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
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.DateTimeControl;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class QapPage2 extends AbstractWizardPage<FormPanel, HashMap<String, String>> {

	public QapDataWizard qapDataObj= new QapDataWizard();
	
	private static final String POItemNo1 = "POItemNo";
	private static final String QuantityOrdered1 = "QuantityOrdered";
	private static final String QuantityEarlierReleased1 = "QuantityEarlierReleased";
	private static final String Description1 = "Description";
	private static final String QuantityAccepted1 = "QuantityAccepted";
	private static final String QuantitySubmitted1 = "QuantitySubmitted";
	private static final String QuantityRejected1 = "QuantityRejected";
	private static final String Remarks1 = "Remarks";
		
	HashMap<String, String> fieldvaluesMap;
	private TextField<String> POItemNo;
	private TextField<String> QuantityOrdered;
	private TextField<String> QuantityEarlierReleased;
	private TextField<String> Remarks;
	private TextField<String> QuantityAccepted;
	private TextField<String> QuantitySubmitted;
	private TextField<String> QuantityRejected;
	private CKEditor Description;

	boolean isCanContinue = true;
	public String OfferQuantity;
	public String irn;
	public String nan;

	HashMap<Integer, TbitsTreeRequestData> requestData;
	ArrayList<TbitsTreeRequestData> requests;

	protected QapPage2(UIContext wizardContext) {
		super(wizardContext);
		// TODO Auto-generated constructor stub
	}

	public QapPage2(DefaultUIContext context,
			ArrayList<TbitsTreeRequestData> requests) {
		super(context);
		this.requests=requests;
		buildPage();
		// TODO Auto-generated constructor stub
	}
	
	public void setValues()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("Remarks", Remarks.getValue());
		hm.put("Description", Description.getHTML());
		qapDataObj.setData1(hm);
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
		widget.setHeading("Shipment Details");
		widget.setFrame(true);
	}

	@Override
	public void buildPage() {
		int i=0,j=0,k=0;
		ArrayList<String> assignees = new ArrayList<String>();
		ArrayList<String> subscribers = new ArrayList<String>();
		ArrayList<String> loggers = new ArrayList<String>();
		ArrayList<TbitsTreeRequestData> requests = this.requests;
		for(TbitsTreeRequestData trd:requests)
		{
			OfferQuantity =trd.getAsString("OfferQuantity");
			irn =trd.getAsString("no_of_irn");
			nan =trd.getAsString("no_of_nan");
			
		}

		final FormData formData = new FormData();
		formData.setWidth(700);
		
		final FormData formData1 = new FormData();
		formData1.setWidth(300);
		ListStore<UserClient> store = new ListStore<UserClient>();

		POItemNo = new TextField<String>();
		POItemNo.setFieldLabel("PO Item No");
		POItemNo.setName(POItemNo1);
		POItemNo.setEnabled(false);
		widget.add(POItemNo, formData1);
		
		QuantityOrdered = new TextField<String>();
		QuantityOrdered.setFieldLabel("Quantity Ordered");
		QuantityOrdered.setName(QuantityOrdered1);
		QuantityOrdered.setEnabled(false);
		widget.add(QuantityOrdered, formData1);
		
		QuantityEarlierReleased = new TextField<String>();
		QuantityEarlierReleased.setFieldLabel("Quantity Earlier Released");
		QuantityEarlierReleased.setName(QuantityEarlierReleased1);
		QuantityEarlierReleased.setEnabled(false);
		widget.add(QuantityEarlierReleased, formData1);
		
		QuantityAccepted = new TextField<String>();
		QuantityAccepted.setFieldLabel("Quantity Accepted");
		QuantityAccepted.setName(QuantityAccepted1);
		QuantityAccepted.setValue(irn);
		QuantityAccepted.setEnabled(false);
		widget.add(QuantityAccepted, formData1);
		
		QuantitySubmitted = new TextField<String>();
		QuantitySubmitted.setFieldLabel("Quantity Submitted");
		QuantitySubmitted.setName(QuantitySubmitted1);
		QuantitySubmitted.setValue(OfferQuantity);
		QuantitySubmitted.setEnabled(false);
		widget.add(QuantitySubmitted, formData1);
		
		QuantityRejected = new TextField<String>();
		QuantityRejected.setFieldLabel("Quantity Rejected");
		QuantityRejected.setName(QuantityRejected1);
		QuantityRejected.setValue(nan);
		QuantityRejected.setEnabled(false);
		widget.add(QuantityRejected, formData1);
		
		Remarks = new TextField<String>();
		Remarks.setFieldLabel("Remarks");
		Remarks.setName(Remarks1);
		widget.add(Remarks, formData);

		LabelField labelField = new LabelField();
		labelField.setFieldLabel("Description");
		widget.add(labelField, new FormData("-20"));

		CKConfig ckc = new CKConfig();
		ckc.setResizeMinHeight(100);
		ckc.setWidth("860px");
		ckc.setHeight("100px");
		ckc.setToolbar(CKConfig.PRESET_TOOLBAR.BASIC);
		Description = new CKEditor(ckc);

		widget.add(Description, formData);
		widget.layout(true);
		
	}

	@Override
	public int getDisplayOrder() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisplay() {
		// TODO Auto-generated method stub
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showBackButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).hideFinishButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showNextButton();
	}

	@Override
	public boolean onLeave() {
		// TODO Auto-generated method stub
		this.setValues();
	    return true;
	}

	@Override
	public HashMap<String, String> getValues() {
		// TODO Auto-generated method stub
		HashMap<String, String> hmpg2 = new HashMap<String, String>();
		HashMap<String, Object> hmabc =qapDataObj.getData1();
		hmpg2.put("Remarks", (String)hmabc.get("Remarks"));
		hmpg2.put("Description", (String)hmabc.get("Description"));
		return hmpg2;
	}

}
