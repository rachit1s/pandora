package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.grids.AutoHeightGridContainer;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class ILPage3 extends
		AbstractWizardPage<ContentPanel, ArrayList<String[]>> {
	private Grid<TbitsModelData> grid;

	public ILPage3(UIContext wizardContext) {
		super(wizardContext);
	}

	public void buildPage() {
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		RowNumberer rowNum = new RowNumberer();
		configs.add(rowNum);

		ColumnConfig fdatesCol = new ColumnConfig();
		fdatesCol.setHeader("From Date");
		fdatesCol.setId("fdates");
		fdatesCol.setWidth(200);
		configs.add(fdatesCol);

		ColumnConfig tdatesCol = new ColumnConfig();
		tdatesCol.setHeader("To Date");
		tdatesCol.setId("tdates");
		tdatesCol.setWidth(200);
		configs.add(tdatesCol);

		ColumnConfig itenaryCol = new ColumnConfig();
		itenaryCol.setHeader("Itinerary");
		itenaryCol.setId("itenary");
		itenaryCol.setWidth(200);
		configs.add(itenaryCol);

		ColumnConfig actionCol = new ColumnConfig();
		actionCol.setRenderer(new GridCellRenderer<TbitsModelData>() {

			public Object render(final TbitsModelData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {
				Button btn = new Button("Remove",
						new SelectionListener<ButtonEvent>() {

							public void componentSelected(ButtonEvent ce) {
								store.remove(model);
							}
						});
				return btn;
			}
		});
		actionCol.setWidth(80);
		configs.add(actionCol);

		ColumnModel cm = new ColumnModel(configs);
		grid = new Grid<TbitsModelData>(new ListStore<TbitsModelData>(), cm);
		grid.setHeight(150);
		grid.setStyleAttribute("marginBottom", "5px");

		AutoHeightGridContainer gridContainer = new AutoHeightGridContainer(
				grid, false);
		widget.add(gridContainer, new FormData());

		final DateField fdate = new DateField();
		fdate.setFieldLabel("From Date");
		fdate.setFormatValue(true);
		widget.add(fdate, new FormData("30%"));

		final DateField tdate = new DateField();
		tdate.setFieldLabel("To Date");
		tdate.setFormatValue(true);
		widget.add(tdate, new FormData("30%"));

		final TextField<String> itenary = new TextField<String>();
		itenary.setFieldLabel("Itinerary");
		widget.add(itenary, new FormData("30%"));

		Button btn = new Button("Add", new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				Date fd = fdate.getValue();
				Date td = tdate.getValue();
				String i = itenary.getValue();
				if (fd != null && td != null && i != null && !i.equals("")) {
					TbitsModelData data = new TbitsModelData();
					data.set("fdates", DateTimeFormat.getFormat("yyyy-MM-dd")
							.format(fd));
					data.set("tdates", DateTimeFormat.getFormat("yyyy-MM-dd")
							.format(td));
					data.set("itenary", i);
					grid.getStore().add(data);
				}
			}
		});
		widget.add(btn);
	}

	public int getDisplayOrder() {
		return 2;
	}

	public ArrayList<String[]> getValues() {
		List<TbitsModelData> models = grid.getStore().getModels();
		ArrayList<String[]> schedule = new ArrayList<String[]>();
		for (TbitsModelData model : models) {
			String[] arr = new String[3];
			arr[0] = model.get("fdates");
			arr[1] = model.get("tdates");
			arr[2] = model.get("itenary");
			schedule.add(arr);
		}
		return schedule;
	}

	public ContentPanel getWidget() {
		return widget;
	}

	public void onDisplay() {
		try {
			widget.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showFinishButton();
			wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showBackButton();
			wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideNextButton();
			wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showPreviewPDFButton();
//			wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showPreviewDOCButton();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onInitialize() {
		buildPage();
	}

	public boolean onLeave() {
		return true;
	}

	public void initializeWidget() {
		widget = new ContentPanel();

		widget.setHeaderVisible(false);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);

		FormLayout layout = new FormLayout();
		widget.setLayout(layout);
	}

	@Override
	public boolean canMoveToNext() {
		// TODO Auto-generated method stub
		return true;
	}
}
