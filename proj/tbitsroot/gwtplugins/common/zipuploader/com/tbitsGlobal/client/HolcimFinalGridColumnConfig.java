package zipuploader.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class HolcimFinalGridColumnConfig {

	EditorGrid<TbitsTreeRequestData> grid = null;

	ArrayList<ColumnConfig> configs = null;

	public HolcimFinalGridColumnConfig() {
		this.configs = new ArrayList<ColumnConfig>();
		// Add the first column representing the serial number.
		RowNumberer rn = new RowNumberer();
		rn.setId("serialNo");
		rn.setHeader("Sl. No.");
		rn.setWidth(20);
		configs.add(rn);
	}

	public ArrayList<ColumnConfig> configureColumns() {

		ColumnConfig name = new ColumnConfig();
		name.setId(HolcimClientConstants.DocumentFieldNameToBeUsed);
		name.setHeader("Name");
		name.setWidth(400);
		configs.add(name);

		ColumnConfig title = new ColumnConfig();
		title.setId("subject");
		title.setHeader("Title");
		title.setWidth(150);
		configs.add(title);

		ColumnConfig revision = new ColumnConfig();
		revision.setId("Revision");
		revision.setHeader("Revision");
		revision.setWidth(100);
		configs.add(revision);

		ColumnConfig Status = new ColumnConfig();
		Status.setId("status");
		Status.setHeader("Status");
		Status.setWidth(100);
		configs.add(Status);

		ColumnConfig ID = new ColumnConfig();
		ID.setId("ID");
		ID.setHeader("ID");
		ID.setWidth(100);
		configs.add(ID);
		SetGridCellRenderer(ID, false);
		SetGridCellRenderer(Status, false);
		SetGridCellRenderer(name, false);
		SetGridCellRenderer(title, false);
		return configs;
	}

	private void SetGridCellRenderer(ColumnConfig columnConfig,
			boolean isEditorEnable) {
		if (!isEditorEnable) {

			columnConfig
					.setRenderer(new GridCellRenderer<DocNumberFileTuple>() {
						public Object render(DocNumberFileTuple model,
								String property, ColumnData config,
								int rowIndex, int colIndex,
								ListStore<DocNumberFileTuple> store,
								Grid<DocNumberFileTuple> grid) {

							if (property.equals("ID")) {
								if (model.get("requestId") != null) {
									if ((model.get("requestId"))
											.equals(new Integer(-1))) {
										String style = "red";
										String v = "0";

										return "<span qtitle='"
												+ ""
												+ "' qtip='"
												+ v
												+ "' style='font-weight: bold;color:"
												+ style + "'>" + v + "</span>";
									} else {
										return model.getRequestID();
									}
								}
							} else if (property.equals("status")) {

								String style = "red";

								if (model.get("op").equals("add")) {
									if (model.get("is_added") != null) {
										if ((model.get("is_added")
												.equals("true"))) {
											return "request has been added";

										} else {

											String v = "Request has not been been added";
											return "<span qtitle='"
													+ ""
													+ "' qtip='"
													+ v
													+ "' style='font-weight: bold;color:"
													+ style + "'>" + v
													+ "</span>";
										}
									}
								} else {
									if (model.get("is_updated") != null) {
										if ((model.get("is_updated")
												.equals("true"))) {
											return "request has been updated";

										} else {

											String v = "Request has not been been updated";
											return "<span qtitle='"
													+ ""
													+ "' qtip='"
													+ v
													+ "' style='font-weight: bold;color:"
													+ style + "'>" + v
													+ "</span>";
										}
									}
								}

							}/*
							 * else if (property.equals("Revision")) { return
							 * model.getREVISION(); } else if
							 * (property.equals("DocNumber")) { return
							 * model.getDOC_NUMBER(); }else if
							 * (property.equals("subject")) { return
							 * model.getSubject(); }
							 */

							return model.get(property);
						}
					});
		} else {
			TextField<String> textField = new TextField<String>();

			textField.setWidth(columnConfig.getWidth());
			columnConfig.setEditor(new TbitsCellEditor(textField));
			columnConfig.getEditor().setCompleteOnEnter(true);

			columnConfig
					.setRenderer(new GridCellRenderer<DocNumberFileTuple>() {
						public Object render(final DocNumberFileTuple model,
								final String property, ColumnData config,
								int rowIndex, int colIndex,
								ListStore<DocNumberFileTuple> store,
								final Grid<DocNumberFileTuple> grid) {

							return model.get(property);
						}
					});
		}
	}

}
