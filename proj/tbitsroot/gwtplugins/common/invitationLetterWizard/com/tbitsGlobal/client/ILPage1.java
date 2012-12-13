package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class ILPage1 extends AbstractWizardPage<FormPanel, HashMap<String, String>> {
	private SimpleComboBox<String> inviType;
	private TextField<String> ref;
	private DateField date;
	private TextField<String> days;
	private List<TypeFieldControl> typeFields;

	private static String BUSINESS_VISA = "Business Visa";
	private static String PROJECT_VISA = "Project Visa";
	
	public ILPage1(UIContext wizardContext) {
		super(wizardContext);
		
		this.buildPage();
	}

	public void buildPage() {
		final FormData formData = new FormData();

		inviType = new SimpleComboBox<String>();
		inviType.setLabelStyle("font-weight:bold");
		inviType.add(BUSINESS_VISA);
		inviType.add(PROJECT_VISA);
		inviType.setFieldLabel("Invitation Type");
		inviType.setName(IFixedFields.REQUEST_TYPE);
		inviType.setSimpleValue(BUSINESS_VISA);
		widget.add(inviType);

		date = new DateField();
		date.setFieldLabel("Date of Arrival");
		date.setName("doa");
		date.setLabelStyle("font-weight:bold");
		widget.add(date, formData);

		days = new TextField<String>();
		days.setFieldLabel("Duration (Days)");
		days.setName("days");
		days.setLabelStyle("font-weight:bold");
		widget.add(days, formData);
		
		ILWizard wizard = ((ILWizard)wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class));

		ref = new TextField<String>();
		ref.setName("ref");
		ref.setLabelStyle("font-weight:bold");
		try {
			ref.setFieldLabel(wizard.getCompiledString("ref_field_title", null));
			ref.setValue(wizard.getCompiledString("ref_prefix", null));
		} catch (TbitsExceptionClient e) {
			TbitsInfo.error("Unable to put content strings", e);
			Log.error("Unable to put content strings", e);
		}
		
		widget.add(ref, formData);
		
		typeFields = new ArrayList<TypeFieldControl>();
		ILConstants.dbService.getPage1Fields(ClientUtils.getSysPrefix(), new AsyncCallback<List<BAField>>(){
			public void onFailure(Throwable caught) {
				Log.error("Could not load type fields", caught);
			}

			public void onSuccess(List<BAField> result) {
				if(result != null){
					for(BAField baField : result){
						if(baField instanceof BAFieldCombo){
							TypeFieldControl typeField = new TypeFieldControl((BAFieldCombo) baField);
							typeFields.add(typeField);
							widget.add(typeField, formData);
							
						}
					}
					widget.layout();
				}
			}});
	}

	public int getDisplayOrder() {
		return 0;
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> values = new HashMap<String, String>();

		String inviTypeString = inviType.getSimpleValue();
		if (inviTypeString != null && !inviTypeString.trim().equals("")) {
			if (inviTypeString.equals(BUSINESS_VISA))
				inviTypeString = ILPage2.BUSINESS_INVITATION;
			else {
				inviTypeString = ILPage2.EMPLOYMENT_INVITATION;
				values.put("applicants", ILConstants.employees.size() + "");
			}
			values.put(inviType.getName(), inviTypeString);
		}

		String dateString = DateTimeFormat.getFormat("dd MMM yyyy").format(date.getValue());
		if (dateString != null && !dateString.trim().equals("")) {
			values.put(date.getName(), dateString);
		}

		String daysString = days.getValue();
		if (daysString != null && !daysString.trim().equals("0")) {
			values.put(days.getName(), daysString);
		}

		String refString = ref.getValue();
		if (refString != null && !refString.trim().equals("")) {
			values.put(ref.getName(), refString);
		}
		
		if(typeFields != null){
			for(TypeFieldControl typeField : typeFields){
				values.put(typeField.getName(), typeField.getStringValue());
			}
		}

		return values;
	}

	public FormPanel getWidget() {
		return widget;
	}

	public void onDisplay() {
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideFinishButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showNextButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideBackButton();
		//wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hidePreviewButton();
	}

	public void onInitialize() {
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideFinishButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showNextButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideBackButton();
	//	wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hidePreviewButton();
	}

	public boolean onLeave() {
		
		ILWizard wizard = ((ILWizard)wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class));
		if (ref.getValue() == null || ref.getValue().trim().equals("")) {
			String str = null;
			try {
				str = wizard.getCompiledString("ref_field_title", null);
			} catch (TbitsExceptionClient e) {
				
				e.printStackTrace();
			}
			Window.alert("Please specify your " + str);
			ref.focus();
			return false;
		}

		if (date.getValue() == null) {
			Window.alert("Please specify the Date of Arrival");
			date.focus();
			return false;
		}

		if (days.getValue() == null || days.getValue().trim().equals("")) {
			Window.alert("Please specify the Number of Days");
			days.focus();
			return false;
		}

		return true;
	}

	public void initializeWidget() {
		widget = new FormPanel();

		widget.setLabelWidth(150);
		widget.setHeaderVisible(false);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);
	}

	@Override
	public boolean canMoveToNext() {
		// TODO Auto-generated method stub
		return true;
	}
}
