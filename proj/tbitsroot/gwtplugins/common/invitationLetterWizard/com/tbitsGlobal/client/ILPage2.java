package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HtmlEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.DateTimeFormat;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class ILPage2 extends AbstractWizardPage<FormPanel, HashMap<String, String>> {
	private String invitationType;

	public static String BUSINESS_INVITATION = "Business";
	public static String EMPLOYMENT_INVITATION = "EmploymentVisa";

	private Grid<TbitsTreeRequestData> grid1;

	private HtmlEditor body1;
	private HtmlEditor body2;

	private TextField<String> subject;
	
	public ILPage2(UIContext wizardContext) {
		super(wizardContext);
		
		subject = new TextField<String>();
		subject.setFieldLabel("Subject");
		subject.setName("pdf_subject");

		body1 = new HtmlEditor();
		body1.setName("body1");
		body1.setHideLabel(true);
		body1.setHeight(200);
		body1.setValue("");

		createGrid1();

		body2 = new HtmlEditor();
		body2.setName("body2");
		body2.setHideLabel(true);
		body2.setHeight(200);
		body2.setValue("");
		
		buildPage();
	}

	public void buildPage() {
		FormData formData = new FormData("-20");

		if (!subject.isRendered())
			widget.add(subject, formData);

		if (!body1.isRendered())
			widget.add(body1, formData);

		if (!grid1.isRendered())
			widget.add(grid1, formData);

		if (!body2.isRendered())
			widget.add(body2, formData);
	}

	private void createGrid1() {
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		RowNumberer rowNum = new RowNumberer();
		rowNum.setHeader("S. No.");
		rowNum.setWidth(40);
		configs.add(rowNum);

		ColumnConfig nameCol = new ColumnConfig();
		nameCol.setHeader("Name");
		nameCol.setId("Name");
		nameCol.setWidth(200);
		configs.add(nameCol);

		ColumnConfig genderCol = new ColumnConfig();
		genderCol.setHeader("Gender");
		genderCol.setId("gender");
		genderCol.setWidth(50);
		genderCol.setRenderer(new GridCellRenderer<TbitsTreeRequestData>() {
			public Object render(TbitsTreeRequestData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<TbitsTreeRequestData> store,
					Grid<TbitsTreeRequestData> grid) {
				String gender = model.getAsString(property);
				if (gender != null && gender.toLowerCase().equals("male"))
					return "Male";
				if (gender != null && gender.toLowerCase().equals("female"))
					return "Female";
				return null;
			}
		});
		configs.add(genderCol);

		ColumnConfig dobCol = new ColumnConfig();
		dobCol.setHeader("DOB");
		dobCol.setId("DOB");
		dobCol.setWidth(200);
		dobCol.setDateTimeFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
		configs.add(dobCol);

		ColumnConfig occupationCol = new ColumnConfig();
		occupationCol.setHeader("Designation");
		occupationCol.setId("Designation");
		occupationCol.setWidth(200);
		configs.add(occupationCol);

		ColumnConfig passNoCol = new ColumnConfig();
		passNoCol.setHeader("Passport No.");
		passNoCol.setId("PassportNo");
		passNoCol.setWidth(200);
		configs.add(passNoCol);

		ColumnModel cm = new ColumnModel(configs);
		grid1 = new Grid<TbitsTreeRequestData>(
				new ListStore<TbitsTreeRequestData>(), cm);
		grid1.setStyleAttribute("marginBottom", "5px");
		grid1.setHeight(150);
		grid1.setAutoExpandColumn("Name");

		for (Integer id : ILConstants.employees.keySet()) {
			TbitsTreeRequestData details = ILConstants.employees.get(id);
			grid1.getStore().add(details);
		}
	}

	@SuppressWarnings("unchecked")
	private void fillPage() throws TbitsExceptionClient {
		HashMap<String, String> preValues = (HashMap<String, String>) prePage.getValues();
		String persons = ILConstants.employees.size() + "";
		String days = preValues.get("days");
		String doa = preValues.get("doa");
		
		String subjectStr = null;
		
		ILWizard wizard = ((ILWizard)wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class));

		if (invitationType.equals(BUSINESS_INVITATION)) {
			subjectStr = wizard.getCompiledString("subject", new String[]{"Business Visa", days});	//"Request for Issuance of Business Visa for " + days + " days";
		} else {
			subjectStr = wizard.getCompiledString("subject", new String[]{"Project Visa", days});	//"Request for Issuance of Employment Visa for " + days + " days";
		}
		
		if(subject.getValue() == null || subject.getValue().equals(""))
			subject.setValue(subjectStr);
		
		/*"<span style='text-align:justify'>M/s KSK Mahanadi Power Company Limited (KMPCL) "
			+ "(formerly known as Wardha Power Company Limited (WPCL)) is the group company of M/s. KSK Energy Ventures Limited, "
			+ "located in Hyderabad, India. KMPCL is setting up a coal based Power Project of 3600 MW capacity at Nariyara Village, "
			+ "Akaltara Tehsil, Janjgir-Champa District, Chattisgarh, India.</span><br /><br />"
			+ "<span style='text-align:justify'>M/s Shandong Electric Power Construction Company (SEPCO) is the "
			+ "most experienced and one of the largest EPC contractors of world repute. KMPCL has awarded the EPC contract for "
			+ "the above project to SEPCO on turnkey basis.</span><br /><br />"
			+ "<span style='text-align:justify'>"
			+ "<span class='Apple-style-span' style='background-color: rgb(255, 255, 0);'>"
			+ "As part of Project Management, we hereby invite a group of "
			+ persons
			+ " persons of SEPCO to attend techno-commercial meetings "
			+ "at our office in Hyderabad. Further, they will be meeting their sub-contractors at Nariyara, the project site. "
			+ "Subsequently, they will continue to work at our project site."
			+ "</span><br /><br />"
			+ "<span>"
			+ "Accordingly, they will be coming to India on " + doa
			+ " and would stay in India for " + days
			+ " days. The details of above engineers are as under:</span></span>";*/
		String body1Str = wizard.getCompiledString("body1", new String[]{persons, doa, days});
		if(body1.getValue() == null || body1.getValue().equals(""))
			body1.setValue(body1Str);
		
		String body2Str = null;
		if (invitationType.equals(BUSINESS_INVITATION)) {
			body2Str = wizard.getCompiledString("body2_business", new String[]{days});
			/*"<span style='text-align:justify'>We request you to issue "
							+ days
							+ " days Business Visa to above engineers of SEPCO to enable them to visit India "
							+ "for the purpose stated above.</span>";*/
		} else {
			body2Str = wizard.getCompiledString("body2_employement", new String[]{days});
			/*"<span style='text-align:justify'>It certified that all above personnel are SEPCO employees. It will be ensured that all the activities "
							+ "listed in tour schedule will be carried out only by the above people and will not be transferred to others. "
							+ "The specific skill required for the job for which the applicant intends to come, is not available in India.</span><br /><br />"
							+ "<span>We request you to issue "
							+ days
							+ " days employment visa to above engineers of SEPCO to enable them to visit India "
							+ "for the purpose stated above.</span>";*/
		}
		if(body2.getValue() == null || body2.getValue().equals(""))
			body2.setValue(body2Str);
	}

	public int getDisplayOrder() {
		return 1;
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (Field field : widget.getFields()) {
			if (field.getName() != null && field.getValue() != null)
				map.put(field.getName(), field.getValue().toString());
		}

		return map;
	}

	public FormPanel getWidget() {
		return widget;
	}

	public void onDisplay() {
		try {
			HashMap<String, String> preValues = (HashMap<String, String>) prePage.getValues();
			invitationType = preValues.get(IFixedFields.REQUEST_TYPE);
			fillPage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			widget.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showBackButton();
			if (invitationType.equals(BUSINESS_INVITATION)) {
				wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideFinishButton();
				wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showNextButton();
			//	wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hidePreviewButton();
			} else {
				wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showFinishButton();
				wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideNextButton();
				wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showPreviewPDFButton();
//				wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showPreviewDOCButton();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onInitialize() {

	}

	public boolean onLeave() {
		return true;
	}

	public void initializeWidget() {
		widget = new FormPanel();

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
