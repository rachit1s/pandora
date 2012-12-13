package digitalDC.com.tbitsGlobal.client;

import java.util.ArrayList;


import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Window;

import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;

public class DDCColumnConfig {

	EditorGrid<TbitsTreeRequestData> grid = null;

	ArrayList<ColumnConfig> configs = null;

	public DDCColumnConfig() {
		this.configs = new ArrayList<ColumnConfig>();
		// Add the first column representing the serial number.
		/*
		 * RowNumberer rn = new RowNumberer(); rn.setId("serialNo");
		 * rn.setHeader("Sl. No."); rn.setWidth(20); configs.add(rn);
		 */

	}

	public ArrayList<ColumnConfig> configureColumns(BulkGridMode gridMode) {

		System.out.println(DDCClientConstants.fields.size());
		System.out.println(DDCClientConstants.DocumentFieldNameToBeUsed);
		if(DDCClientConstants.DocumentFieldNameToBeUsed  == null){
			Window.alert("Data not loaded");
		}
		ColumnConfig name = new ColumnConfig(DDCClientConstants.DocumentFieldNameToBeUsed, 200);
		name.setHeader(DDCClientConstants.DocumentFieldNameToBeUsed);
		configs.add(name);

		ColumnConfig title = new ColumnConfig();
		title.setId("subject");
		title.setFixed(true);
		title.setResizable(false);
		title.setWidth(180);
		title.setSortable(false);
		title.setHeader("Title");
		configs.add(title);

		ColumnConfig FileName = new ColumnConfig();
		FileName.setResizable(false);
		FileName.setWidth(400);
		FileName.setSortable(false);

		FileName.setId("filenameToBeUsed");
		FileName.setHeader("FileName");
		configs.add(FileName);

		/*ColumnConfig revision = new ColumnConfig();
		revision.setFixed(true);
		revision.setResizable(false);
		revision.setWidth(100);
		revision.setSortable(false);

		revision.setId("Revision");
		revision.setHeader("Revision");
		configs.add(revision);*/
		
		for(TbitsModelData tmd:DDCClientConstants.fields){
			String field_name=tmd.get("name");
			if(field_name.equals(DDCClientConstants.getDocField())){
				continue;
			}
			
			ColumnConfig config = new ColumnConfig();
			config.setFixed(true);
			config.setResizable(false);
			config.setWidth(100);
			config.setSortable(false);
			config.setId((String) tmd.get("name"));
			config.setHeader((String) tmd.get("name"));
			SetGridCellRenderer(config,(Boolean) tmd.get("is_editable"));
			configs.add(config);
			
			
		}

		ColumnConfig Status = new ColumnConfig();

		Status.setFixed(true);
		Status.setResizable(false);
		Status.setWidth(180);
		Status.setSortable(false);

		Status.setId("status");
		Status.setHeader("Status");

		configs.add(Status);

		ColumnConfig ID = new ColumnConfig();

		ID.setFixed(true);
		ID.setResizable(false);
		ID.setWidth(180);
		ID.setSortable(false);

		ID.setId("ID");
		ID.setHeader("ID");

		configs.add(ID);

		SetGridCellRenderer(ID, false);
		
		if(gridMode == BulkGridMode.SINGLE)
		SetGridCellRenderer(Status, false);
	
		SetGridCellRenderer(name, false);
		SetGridCellRenderer(FileName, true);
		SetGridCellRenderer(title, true);
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

								String style2 = "red";
								String style1 = "green";

								if (model.getIs_Regex_Correct() != null
										&& model.getIs_Regex_Correct()) {
									if (model.get("requestId") != null) {
										if ((model.get("requestId"))
												.equals(new Integer(-1))) {
											// return
											String v = "Does not exist";
											return "<span qtitle='"
													+ ""
													+ "' qtip='"
													+ v
													+ "' style='font-weight: bold;color:"
													+ style1 + "'>" + v
													+ "</span>";
										} else {
											return "Exists";
										}
									}
								} else {
									String v = "Incorrect File Name has been entered";
									return "<span qtitle='"
											+ ""
											+ "' qtip='"
											+ v
											+ "' style='font-weight: bold;color:"
											+ style2 + "'>" + v + "</span>";

								}
							}/* else if (property.equals("Revision")) {
								return model.get(property);
							} else if (property.equals("DocNumber")) {
								return model.getDOC_NUMBER();
							} else if (property.equals("FileName")) {
								return model.getfilenameToBeUsed();
							}*/
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

							if (property.equals("filenameToBeUsed")) {
								return model.getfilenameToBeUsed();
							} else if (property.equals("subject")) {
								return model.getSubject();
							}
							return model.get(property);

						}
					});
		}
	}

}
