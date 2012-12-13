package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.cellEditors.TypeFieldEditor;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

/**
 * Builds the AttachmentSelection Table configuration
 * 
 */
public class AttachmentSelectionTableColumnsConfiguration {

	HashMap<String, BAField> baFieldMap = new HashMap<String, BAField>();
	EditorGrid<TbitsTreeRequestData> grid = null;
	private HashMap<Integer, HashMap<String, TransmittalAttachmentContainer>> requestAttachmentContainerMap;
	private WizardData dataObject;
	public  boolean flag;
	
	public WizardData getDataObject() {
		return dataObject;
	}

	public void setDataObject(WizardData dataObject) {
		this.dataObject = dataObject;
	}

	ArrayList<ColumnConfig> configs = null;
//	TransmittalWizardConstants twc;

//	public AttachmentSelectionTableColumnsConfiguration() {
//		this.configs = new ArrayList<ColumnConfig>();
//		// Add the first column representing the serial number.
//		twc = TransmittalWizardConstants.getInstance();
//		RowNumberer rn = new RowNumberer();
//		rn.setId("serialNo");
//		rn.setHeader("Sl. No.");
//		rn.setWidth(20);
//		configs.add(rn);
//	}
	public AttachmentSelectionTableColumnsConfiguration(WizardData params) {
		
		configs = new ArrayList<ColumnConfig>();
		flag= false;
		this.setDataObject(params);
		requestAttachmentContainerMap = new HashMap<Integer, HashMap<String, TransmittalAttachmentContainer>>();
	}

	/**
	 * Configure the Columns of Attachment Selection Table
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ColumnConfig> configureColumns() {

		configs.clear();
		RowNumberer rn = new RowNumberer();
		rn.setId("serialNo");
		rn.setHeader("Sl. No.");
		rn.setWidth(20);
		configs.add(rn);
		
		ArrayList<TbitsModelData>AttachmentColumnList= (ArrayList<TbitsModelData>) getDataObject().getData().get("attachmentTableColumnList");
		ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject().getData().get("BAFields");
/*		Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
			public int compare(TbitsModelData o1, TbitsModelData o2) {
				if ((o1 != null) && (o2 != null)) {
					int s1 = (Integer) o1
							.get(TransmittalConstants.COLUMN_ORDER);
					int s2 = (Integer) o2
							.get(TransmittalConstants.COLUMN_ORDER);
					if (s1 > s2)
						return 1;
					else if (s1 == s2)
						return 0;
					else if (s1 < s2)
						return -1;
				}
				return 0;
			}
		};
		// Sort the column info, before creating column configs out of them. So,
		// that they maintain the sort order and
		// hence the column order in the table.
		Collections.sort(AttachmentColumnList, comp);*/

		// Create the column configs, based on whether the field is mapped to
		// corresponding field in the originating
		// DCR BA or a custom field.
	
		for (TbitsModelData tmd : AttachmentColumnList) {
			if (tmd != null) {
				int fieldId = (Integer) tmd
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				boolean isActive = (Boolean) tmd
						.get(TransmittalConstants.IS_ACTIVE_COLUMN);
				BAField baField;
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId,BAFieldList);

					if ((baField != null) && (isActive)) {
						configs.add(getColumnConfigBasedOnDataType(baField,
								tmd, 180));
					}
				}
			}
		}

		return this.configs;
	}

	/**
	 * Return the column configuration based on the data type of the values in
	 * that column for attachment selection table.
	 * 
	 * @param baField
	 *            - Field which has to be used for that column
	 * @param columnInfo
	 *            - The model which has to be used
	 * @param width
	 *            - width to be set for the specified column
	 * @return
	 */
	private ColumnConfig getColumnConfigBasedOnDataType(BAField baField,
			TbitsModelData columnInfo, int width) {
		boolean isEditorEnabled = (Boolean) columnInfo
				.get(TransmittalConstants.IS_EDITABLE);
		String defaultValue = (String) columnInfo
				.get(TransmittalConstants.DEFAULT_VALUE);
		ColumnConfig columnConfig = new ColumnConfig();
		columnConfig.setHeader((String) columnInfo
				.get(TransmittalConstants.NAME_COLUMN));
		columnConfig.setId(baField.getName());
		columnConfig.setAlignment(HorizontalAlignment.CENTER);
		columnConfig.setFixed(true);
		columnConfig.setResizable(false);
		columnConfig.setWidth(width);
		columnConfig.setSortable(false);
		setGridCellRenderer(baField, columnConfig, isEditorEnabled,
				defaultValue);
		return columnConfig;
	}

	/**
	 * Set the renderer for the particular column of the attachment selection
	 * table depending on its data type
	 * 
	 * @param baField
	 *            - field to be set in the column
	 * @param columnConfig
	 *            - the columnconfig that has to be used for attaching the
	 *            renderer
	 * @param isEditorEnabled
	 *            - is the column is editable or not
	 * @param defaultValue
	 */
	private void setGridCellRenderer(final BAField baField,
			ColumnConfig columnConfig, boolean isEditorEnabled,
			final String defaultValue) {
		/*
		 * If the column is not editable, then find the type of display it is
		 * configured for and return the value for that type of display
		 */
		if (!isEditorEnabled) {
			if (baField.getDataTypeId() == TransmittalConstants.TYPE)
				columnConfig.setWidth(75);
			columnConfig
					.setRenderer(new GridCellRenderer<TbitsTreeRequestData>() {
						public Object render(TbitsTreeRequestData model,
								String property, ColumnData config,
								int rowIndex, int colIndex,
								ListStore<TbitsTreeRequestData> store,
								Grid<TbitsTreeRequestData> grid) {
							if (baField instanceof BAFieldCombo) {
								BAFieldCombo baFieldCombo = (BAFieldCombo) baField;
								String displayValue = getTypeValueBasedOnConfig(baFieldCombo);
								String typeName = model.getAsString(property);
								TypeClient typeClient = null;
								if ((typeName == null)
										|| (typeName.trim().equals("")))
									typeClient = baFieldCombo.getDefaultValue();
								else
									typeClient = baFieldCombo
											.getModelForName(typeName);
								return typeClient.get(displayValue);
							}
							return model.getAsString(property);
						}
					});
		} else {
			/*
			 * If the column is editable, then set the approriate editor of it
			 */
			if (baField instanceof BAFieldCombo) {

				TypeFieldEditor editor = TypeFieldEditor
						.newInstance((BAFieldCombo) baField);
				editor.getTypeFieldControl().setDisplayField(
						getTypeValueBasedOnConfig((BAFieldCombo) baField));
				columnConfig.setEditor(editor);
				columnConfig.setWidth(75);

				columnConfig
						.setRenderer(new GridCellRenderer<TbitsTreeRequestData>() {

							public Object render(TbitsTreeRequestData model,
									String property, ColumnData config,
									int rowIndex, int colIndex,
									ListStore<TbitsTreeRequestData> store,
									Grid<TbitsTreeRequestData> grid) {
								String name = (String) model.get(property);
								BAFieldCombo baFieldCombo = (BAFieldCombo) baField;

								TypeClient typeClient = null;
								if (name == null || name.trim().equals(""))
									typeClient = baFieldCombo.getDefaultValue();
								else {
									typeClient = baFieldCombo
											.getModelForName(name);

								}

								String displayValue = getTypeValueBasedOnConfig(baFieldCombo);
								return typeClient.get(displayValue);
								// return getComboBoxField(baFieldCombo,
								// property, model);
							}
						});
			} else if (baField instanceof BAFieldAttachment) {
				/*
				 * If the column type is that of an Attachment
				 */
				requestAttachmentContainerMap.clear();
				columnConfig.setWidth(200);
				columnConfig.setFixed(true);
				columnConfig.setResizable(false);
				GridCellRenderer<TbitsTreeRequestData> gridRenderer = new GridCellRenderer<TbitsTreeRequestData>() {
					public Object render(TbitsTreeRequestData model,
							String property, ColumnData config,
							int rowIndex, int colIndex,
							ListStore<TbitsTreeRequestData> store,
							Grid<TbitsTreeRequestData> grid) {
						if (Boolean.valueOf((String) getDataObject().getData().get("inapprovalcycle"))) {
							TransmittalAttachmentContainer[] temp = new TransmittalAttachmentContainer[2];
							TransmittalAttachmentContainer tac = null;
							if (model != null) {
								HashMap<String, TransmittalAttachmentContainer> attMap = requestAttachmentContainerMap
										.get(model.getRequestId());
								if (attMap != null) {
									tac = attMap.get(property);
									if (tac != null) {
										return tac;
									}
								}

								POJO obj = model.getAsPOJO(property);
								if (obj == null
										|| !(obj instanceof POJOAttachment)) {
									obj = new POJOAttachment(
											new ArrayList<FileClient>());
								}
								List<FileClient> Sattachments = new ArrayList<FileClient>();
								List<FileClient> Uattachments = new ArrayList<FileClient>();

								List<FileClient> attachments = (List<FileClient>) obj.getValue();
								for (FileClient fc : attachments) {
									if (fc.get("IS_CHECKED_IN_TRANSIENT_DATA").equals("true")) {	
										Sattachments.add(fc);
									} else {
										Uattachments.add(fc);
									}

								}

								TableLayout tableLayout = new TableLayout(
										2);
								
								tac = new TransmittalAttachmentContainer(
										tableLayout, attachments);

							}

							setRequestAttachmentContainerMap(model,
									property, tac);
							flag=true;
							return tac;
						} else {
							TransmittalAttachmentContainer tac = null;
							if (model != null) {
								HashMap<String, TransmittalAttachmentContainer> attMap = requestAttachmentContainerMap
										.get(model.getRequestId());
								
								if (attMap != null) {
									tac = attMap.get(property);
									if (tac != null) {
										return tac;
									}
								}

								POJO obj = model.getAsPOJO(property);
								if (obj == null
										|| !(obj instanceof POJOAttachment)) {
									obj = new POJOAttachment(
											new ArrayList<FileClient>());
								}

								List<FileClient> attachments = ((POJOAttachment) obj)
										.getValue();

								TableLayout tableLayout = new TableLayout(
										2);
								if (defaultValue == null)
									tac = new TransmittalAttachmentContainer(
											tableLayout, attachments,
											true);
								else
									tac = new TransmittalAttachmentContainer(
											tableLayout,
											attachments,
											Boolean
													.valueOf(defaultValue));
							}
							
							setRequestAttachmentContainerMap(model,
									property, tac);
							flag=true;
							return tac;
						}
					}
				};
				columnConfig
						.setRenderer(gridRenderer);
			} else if (baField instanceof BAFieldInt) {

				TextField<String> textField = new TextField<String>();
				if ((defaultValue != null) && (!defaultValue.trim().isEmpty()))
					textField.setValue(defaultValue);
				columnConfig.setEditor(new TbitsCellEditor(textField));
				columnConfig.setWidth(50);
				columnConfig.getEditor().setWidth(25);
				columnConfig.getEditor().setCompleteOnEnter(true);

				columnConfig
						.setRenderer(new GridCellRenderer<TbitsTreeRequestData>() {
							public Object render(
									final TbitsTreeRequestData model,
									final String property, ColumnData config,
									int rowIndex, int colIndex,
									ListStore<TbitsTreeRequestData> store,
									final Grid<TbitsTreeRequestData> grid) {

								return model.getAsString(property);
							}
						});
			} else if ((baField instanceof BAFieldString)
					|| (baField instanceof BAFieldTextArea)) {

				TextField<String> textField = new TextField<String>();
				columnConfig.setWidth(100);
				textField.setWidth(columnConfig.getWidth() - 10);
				columnConfig.setEditor(new TbitsCellEditor(textField));
				if ((defaultValue != null) && (!defaultValue.trim().isEmpty()))
					textField.setValue(defaultValue);

				columnConfig
						.setRenderer(new GridCellRenderer<TbitsTreeRequestData>() {
							public Object render(
									final TbitsTreeRequestData model,
									final String property, ColumnData config,
									int rowIndex, int colIndex,
									ListStore<TbitsTreeRequestData> store,
									final Grid<TbitsTreeRequestData> grid) {

								return model.getAsString(property);
							}
						});
			}
		}
	}

	/**
	 * 
	 * @param baField
	 * @return
	 */
	protected String getTypeValueBasedOnConfig(BAFieldCombo baField) {
		String fValue = TypeClient.NAME;
		int typeValueSrc = 0;
		ArrayList<TbitsModelData>AttachmentColumnList= (ArrayList<TbitsModelData>) getDataObject().getData().get("attachmentTableColumnList");
		for (TbitsModelData md : AttachmentColumnList) {
			Integer fieldId = (Integer) md
					.get(TransmittalConstants.FIELD_ID_COLUMN);
			if (fieldId == baField.getFieldId()) {
				typeValueSrc = (Integer) md
						.get(TransmittalConstants.TYPE_VALUE_SOURCE_COLUMN);
			}
		}
		switch (typeValueSrc) {
		case 0:
		case 1:
			fValue = TypeClient.NAME;
			break;
		case 2:
			fValue = TypeClient.DISPLAY_NAME;
			break;
		case 3:
			fValue = TypeClient.DESCRIPTION;
			break;
		}
		return fValue;
	}

	/**
	 * @param model
	 * @param property
	 * @param tac
	 */
	private void setRequestAttachmentContainerMap(TbitsTreeRequestData model,
			String property, TransmittalAttachmentContainer tac) {
		HashMap<String, TransmittalAttachmentContainer> fieldAttachmentContainerMap = requestAttachmentContainerMap
				.get(model.getRequestId());
		if (fieldAttachmentContainerMap == null) {
			fieldAttachmentContainerMap = new HashMap<String, TransmittalAttachmentContainer>();
			fieldAttachmentContainerMap.put(property, tac);
		} else {
			fieldAttachmentContainerMap.put(property, tac);
		}
		requestAttachmentContainerMap.put(model.getRequestId(),
				fieldAttachmentContainerMap);
	}

	public HashMap<Integer, HashMap<String, TransmittalAttachmentContainer>> getRequestAttachmentContainerMap() {
		return requestAttachmentContainerMap;
	}

}
