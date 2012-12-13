package qap.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class QapPage1 extends
		AbstractWizardPage<ContentPanel, HashMap<String, String>> {
	/*
	 public static final String CONTRACTOR = "Contractor"; 
	 public static final String SUB_VENDOR_NAME = "SubVendorName";
	 
	public static final String COMPONENTS = "Components"; 
	public static final 	String Inspection_Call_No = "InspectionCallNo"; 
	public static final 	String Propose_Date_of_Inspection = "due_datetime";
	*/

	ArrayList<TbitsTreeRequestData> requestData;
	private EditorGrid<TbitsModelData> grid1 = null;
	private EditorGrid<TbitsModelData> grid2 = null;

	protected QapPage1(UIContext wizardContext) {
		super(wizardContext);
	}

	public QapPage1(DefaultUIContext context,
			ArrayList<TbitsTreeRequestData> arg0) {
		super(context);
		requestData = arg0;
		buildPage();
	}

	@Override
	public void buildPage() {

		ArrayList<TbitsModelData> d1 = new ArrayList<TbitsModelData>();

		TbitsTreeRequestData trd = requestData.get(0);
		TbitsModelData tmd = new TbitsModelData();

		for (String eachCommonKey : QapWizard.page1Common) {
			tmd.set(eachCommonKey, trd.getAsString(eachCommonKey));
		}
		d1.add(tmd);

		/*
		 * to -do add the statements for field 3
		 */

		ArrayList<TbitsModelData> d2 = new ArrayList<TbitsModelData>();

		for (TbitsTreeRequestData trd1 : requestData) {

			TbitsModelData tmd1 = new TbitsModelData();

			for (String eachUniqueKey : QapWizard.page1Specific) {

				tmd1.set(eachUniqueKey, trd1.getAsString(eachUniqueKey));
			}
			d2.add(tmd1);
		}

		if (grid1 == null && grid2 == null) {

			List<ColumnConfig> configs1 = new ArrayList<ColumnConfig>();

			List<ColumnConfig> configs2 = new ArrayList<ColumnConfig>();

			for (String commonfield : QapWizard.page1Common) {

				ColumnConfig column1 = new ColumnConfig();

				column1.setId(commonfield);
				column1.setHeader(commonfield);
				column1.setWidth(400);
				column1.setRowHeader(true);
				column1.setAlignment(HorizontalAlignment.LEFT);
				configs1.add(column1);

			}

			for (String uniqueField : QapWizard.page1Specific) {

				ColumnConfig column1 = new ColumnConfig();

				column1.setId(uniqueField);
				column1.setHeader(uniqueField);
				column1.setWidth(400);
				column1.setRowHeader(true);
				column1.setAlignment(HorizontalAlignment.LEFT);
				configs2.add(column1);

			}

			grid1 = new EditorGrid<TbitsModelData>(
					new ListStore<TbitsModelData>(), new ColumnModel(configs1));

			grid1.setStyleAttribute("borderTop", "none");
			grid1.setBorders(true);
			grid1.setStripeRows(true);

			GridView view1 = new GridView() {
				protected void onColumnWidthChange(int column, int width) {
					super.onColumnWidthChange(column, width);
					this.refresh(false);
				}
			};

			grid1.setView(view1);

			grid2 = new EditorGrid<TbitsModelData>(
					new ListStore<TbitsModelData>(), new ColumnModel(configs2));

			grid2.setStyleAttribute("borderTop", "none");
			grid2.setBorders(true);
			grid2.setStripeRows(true);

			GridView view2 = new GridView() {
				protected void onColumnWidthChange(int column, int width) {
					super.onColumnWidthChange(column, width);
					this.refresh(false);
				}
			};

			grid2.setView(view2);

			// adding the model data to the grids

			grid1.getStore().add(d1);
			grid2.getStore().add(d2);

			this.getWidget().add(
					new Label("Common Details and Seleted Documents details"),
					new BorderLayoutData(LayoutRegion.NORTH, 20));
			this.getWidget().add(grid1,
					new BorderLayoutData(LayoutRegion.CENTER));

			// this.getWidget().add(new Label("GRID2"),new Bo));

			BorderLayoutData southData = new BorderLayoutData(
					LayoutRegion.SOUTH);
			southData.setHideCollapseTool(true);
			southData.setSplit(true);
			southData.setMargins(new Margins(2));
			this.getWidget().add(grid2, southData);

		}
	}

	@Override
	public int getDisplayOrder() {
		return 1;
	}

	@Override
	public HashMap<String, String> getValues() {

		HashMap<String, String> paramTable = new HashMap<String, String>();
		return paramTable;
	}

	@Override
	public ContentPanel getWidget() {
		return widget;
	}

	@Override
	public void initializeWidget() {
		widget = new ContentPanel(new BorderLayout());
		widget.setScrollMode(Scroll.AUTO);
		widget.setHeading("Basic Information Page");
	}

	@Override
	public void onDisplay() {

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showBackButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).hideFinishButton();
		
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showNextButton();
	}

	@Override
	public void onInitialize() {

	}

	@Override
	public boolean onLeave() {
		return true;
	}

}
