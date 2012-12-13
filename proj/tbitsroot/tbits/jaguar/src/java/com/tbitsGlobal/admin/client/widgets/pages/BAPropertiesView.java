package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AdminUtils;
import com.tbitsGlobal.admin.client.events.OnBACreated;
import com.tbitsGlobal.admin.client.events.OnBAUpdate;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnCacheUpdates;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMailAccountClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Page for displaying "BA Properties" page in the admin panel
 * @author dheeru
 */

public class BAPropertiesView extends APTabItem {

	protected String sysPrefix;
	
	// map contains all widgets fields of this form
	private HashMap<String, Field> map = new HashMap<String, Field>();
	private ListStore<BAMailAccountClient> list = new ListStore<BAMailAccountClient>();
	final ListStore<TypeClient> typeClients = new ListStore<TypeClient>();

	public BAPropertiesView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		
		sysPrefix = ClientUtils.getSysPrefix();
	}

	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		observable.subscribe(OnChangeBA.class,
				new ITbitsEventHandle<OnChangeBA>() {
			public void handleEvent(OnChangeBA event) {
				sysPrefix = event.getSysPrefix();
				reloadData();
			}
		});
		
		observable.subscribe(OnCacheUpdates.class,
				new ITbitsEventHandle<OnCacheUpdates>() {
			public void handleEvent(OnCacheUpdates event) {
				reloadData();
			}
		});

		ContentPanel form = new ContentPanel();
		form.setScrollMode(Scroll.AUTO);
		form.setLayout(new RowLayout());
		form.setHeaderVisible(false);
		form.setBodyBorder(false);

		LayoutContainer topContainer = getTopContainer();
		
		RowData rowData = new RowData();
		rowData.setMargins(new Margins(2));
		form.add(topContainer, rowData);

		LayoutContainer desContainer = new LayoutContainer();
		desContainer.setLayout(new FormLayout());
		TextArea description = new TextArea();
		description.setFieldLabel("Description");
		description.setHeight(75);
		description.setLabelStyle("font-weight:bold");
		desContainer.add(description, new FormData("-20"));
		map.put(BusinessAreaClient.DESCRIPTION, description);

		form.add(desContainer, new RowData());
		
		FieldSet emailSettings = new FieldSet();
		emailSettings.setHeading("E-mail Settings");
		emailSettings.setLayout(new ColumnLayout());

		LayoutContainer leftPanel = new LayoutContainer();
		FormLayout leftPanelLayout = new FormLayout();
		leftPanelLayout.setLabelSeparator("");
		leftPanelLayout.setLabelWidth(165);
		leftPanel.setLayout(leftPanelLayout);

		TextField<String> mail = new TextField<String>();
		mail.setFieldLabel("E-mail");
		map.put(BusinessAreaClient.EMAIL, mail);
		leftPanel.add(mail);

		NumberField noOfActions = new NumberField();
		noOfActions.setAllowDecimals(false);
		noOfActions.setAllowNegative(false);
		noOfActions.setFieldLabel("No. Of Actions in E-mail");
		map.put(BusinessAreaClient.MAX_EMAIL_ACTIONS, noOfActions);
		leftPanel.add(noOfActions);

		emailSettings.add(leftPanel, new ColumnData(0.60));

		LayoutContainer rightPanel = new LayoutContainer();
		FormLayout rightPanelLayout = new FormLayout();
		rightPanelLayout.setLabelSeparator("");
		rightPanelLayout.setLabelWidth(200);
		rightPanelLayout.setDefaultWidth(25);
		rightPanel.setLayout(rightPanelLayout);

		CheckBoxGroup isEmailActive = new CheckBoxGroup();
		isEmailActive.setFieldLabel("Set E-mail Active");

		CheckBox isActive = new CheckBox();
		isEmailActive.add(isActive);
		map.put(BusinessAreaClient.IS_EMAIL_ACTIVE, isActive);
		rightPanel.add(isEmailActive);

		CheckBoxGroup notify = new CheckBoxGroup();
		notify.setFieldLabel("Notify Appender for every action");

		CheckBox notifyAppender = new CheckBox();
		notify.add(notifyAppender);
		map.put(SysConfigClient.MY_NOTIFY_APPENDER, notifyAppender);
		rightPanel.add(notify);

		emailSettings.add(rightPanel, new ColumnData(0.40));

		form.add(emailSettings, new RowData());

		Grid<TbitsModelData> grid = createGrid();
		
		ContentPanel gridContainer = new ContentPanel(new FitLayout());
		gridContainer.setHeading("INCOMING MAIL SETTINGS");
		gridContainer.setCollapsible(true);
		gridContainer.add(grid, new FitData());
		
		ToolBar toolBar = new ToolBar();
		ToolBarButton saveBtn = new ToolBarButton("Save Changes");
		saveBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				BAPropertiesView.this.updatedMailClients();
			}
		});
		toolBar.add(saveBtn);
		ToolBarButton addBtn = new ToolBarButton("Add Incoming Email");
		addBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				addDefaultMailSettings();
			}
		});
		toolBar.add(addBtn);
		gridContainer.setBottomComponent(toolBar);

		form.add(gridContainer, new RowData());

		ToolBar topBar = getTopToolBar();
		form.setTopComponent(topBar);
		
		this.add(form, new FitData());

		reloadData();
	}
	
	/**
	 * Build and return the top container
	 * @return
	 */
	private LayoutContainer getTopContainer(){
		FormData formData = new FormData("-20");
		
		LayoutContainer topContainer = new LayoutContainer(new ColumnLayout());
		LayoutContainer left = new LayoutContainer();
		
		FormLayout leftLayout = new FormLayout();
		left.setLayout(leftLayout);

		LabelField sysid = new LabelField();
		sysid.setReadOnly(true);
		sysid.setFieldLabel("System ID");
		sysid.setLabelStyle("font-weight:bold");
		left.add(sysid, formData);
		map.put(BusinessAreaClient.SYSTEM_ID, sysid);

		LabelField sysprefix = new LabelField();
		sysprefix.setFieldLabel("System Prefix");
		sysprefix.setReadOnly(true);
		sysprefix.setLabelStyle("font-weight:bold");
		left.add(sysprefix, formData);
		map.put(BusinessAreaClient.SYSTEM_PREFIX, sysprefix);

		LabelField dateCreated = new LabelField();
		dateCreated.setFieldLabel("Date Created");
		dateCreated.setLabelStyle("font-weight:bold");
		map.put(BusinessAreaClient.DATE_CREATED, dateCreated);
		left.add(dateCreated, formData);

		TextField<String> name = new TextField<String>();
		name.setFieldLabel("Name");
		name.setLabelStyle("font-weight:bold");
		left.add(name, formData);
		map.put(BusinessAreaClient.NAME, name);

		TextField<String> displname = new TextField<String>();
		displname.setFieldLabel("Display Name");
		displname.setLabelStyle("font-weight:bold");
		left.add(displname, formData);
		map.put(BusinessAreaClient.DISPLAY_NAME, displname);

		TextField<String> type = new TextField<String>();
		type.setFieldLabel("Type");
		type.setLabelStyle("font-weight:bold");
		left.add(type, formData);
		map.put(BusinessAreaClient.TYPE, type);
		
		CheckBoxGroup isPrivateBA = new CheckBoxGroup();
		isPrivateBA.setFieldLabel("Private");
		isPrivateBA.setLabelStyle("font-weight:bold");
		CheckBox isPrivateBox = new CheckBox();
		isPrivateBA.add(isPrivateBox);
		map.put(BusinessAreaClient.IS_PRIVATE, isPrivateBox);
		left.add(isPrivateBA, formData);
		
		CheckBoxGroup isActiveBA = new CheckBoxGroup();
		isActiveBA.setFieldLabel("Active");
		isActiveBA.setLabelStyle("font-weight:bold");
		CheckBox isActiveBox	= new CheckBox();
		isActiveBA.add(isActiveBox);
		map.put(BusinessAreaClient.IS_ACTIVE, isActiveBox);
		left.add(isActiveBA, formData);

		topContainer.add(left, new ColumnData(0.45));

		LayoutContainer right = new LayoutContainer();
		FormLayout rightLayout = new FormLayout();
		rightLayout.setLabelAlign(LabelAlign.TOP);
		right.setLayout(rightLayout);

		CheckBoxGroup addDocDefaults = new CheckBoxGroup();
		addDocDefaults.setFieldLabel("Select ADD Request Defaults");
		addDocDefaults.setLabelStyle("font-weight:bold");
		CheckBox emailBox = new CheckBox();
		emailBox.setBoxLabel("Notify by Email Option");
		emailBox.setLabelStyle("font-weight:bold");
		map.put(SysConfigClient.MY_REQUEST_NOTIFY, emailBox);

		CheckBox loggerBox = new CheckBox();
		loggerBox.setBoxLabel("Logger Option");
		loggerBox.setLabelStyle("font-weight:bold");
		map.put(SysConfigClient.MY_REQUEST_NOTIFY_LOGGERS, loggerBox);

		addDocDefaults.add(emailBox);
		addDocDefaults.add(loggerBox);

		right.add(addDocDefaults, formData);

		CheckBoxGroup updateDocDefaults = new CheckBoxGroup();
		updateDocDefaults.setFieldLabel("Select Update Action Defaults");
		updateDocDefaults.setLabelStyle("font-weight:bold");
		CheckBox updateEmailBox = new CheckBox();
		updateEmailBox.setBoxLabel("Notify by Email Option");
		updateEmailBox.setLabelStyle("font-weight:bold");
		map.put(SysConfigClient.MY_ACTION_NOTIFY, updateEmailBox);
		updateDocDefaults.add(updateEmailBox);

		CheckBox updateLoggerBox = new CheckBox();
		updateLoggerBox.setBoxLabel("Logger Option");
		updateLoggerBox.setLabelStyle("font-weight:bold");
		map.put(SysConfigClient.MY_ACTION_NOTIFY_LOGGERS, updateLoggerBox);
		updateDocDefaults.add(updateLoggerBox);

		right.add(updateDocDefaults, formData);

		CheckBoxGroup allowToAll = new CheckBoxGroup();
		allowToAll.setFieldLabel("Assignee");
		allowToAll.setLabelStyle("font-weight:bold");
		CheckBox allow = new CheckBox();
		allow.setBoxLabel("Allow assigning to all users");
		allow.setLabelStyle("font-weight:bold");
		allowToAll.add(allow);
		right.add(allowToAll, formData);
		map.put(SysConfigClient.MY_ASSIGN_TO_ALL, allow);
		
		RadioGroup assignMethod = new RadioGroup();
		assignMethod.setFieldLabel("Assign Method");
		assignMethod.setLabelStyle("font-weight:bold");
		
		Radio rr = new Radio();
		rr.setBoxLabel("Round Robin");
		rr.setLabelStyle("font-weight:bold");
		assignMethod.add(rr);
		
		Radio rand = new Radio();
		rand.setBoxLabel("Random");
		rand.setLabelStyle("font-weight:bold");
		assignMethod.add(rand);
		
		Radio none = new Radio();
		none.setBoxLabel("None");
		none.setLabelStyle("font-weight:bold");
		assignMethod.add(none);
		right.add(assignMethod, formData);
		map.put(SysConfigClient.MY_VOLUNTEER, assignMethod);
		
		topContainer.add(right, new ColumnData(0.45));
		
		return topContainer;
	}
	
	/**
	 * Build and return top toolbar which contains buttons for saving/creating and manipulating BA settings
	 * @return Top Toolbar
	 */
	private ToolBar getTopToolBar(){
		ToolBar topBar = new ToolBar();

		ToolBarButton saveButton = new ToolBarButton("Save Changes", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateBA();
			}
		});
		topBar.add(saveButton);

		ToolBarButton resetButton = new ToolBarButton("Reset to Last Saved Settings", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				reloadData();
			}
		});
		topBar.add(resetButton);
		
		topBar.add(new SeparatorToolItem());
		
		ToolBarButton addButton = new ToolBarButton("Create Business Area",
				new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Window w = createNewBAWindow();
				w.show();
			}
		});
		topBar.add(addButton);
		
		ToolBarButton exportButton = new ToolBarButton("Export Business Area", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				com.google.gwt.user.client.Window.open(AdminUtils.getAppBaseURL() + "exportbusinessarea?ba=" +
						sysPrefix +"&actionType=export", "", "");
			}

		});
		topBar.add(exportButton);
		
		ToolBarButton importButton = new ToolBarButton("Import Business Area", new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				Window w = importBAWindow();
				w.show();
			}
		});
		topBar.add(importButton);
		
		return topBar;
	}
	
	/**
	 * Create and return grid to manipulate 'Incoming Mail Settings' section of BA Properties
	 * @return incoming mail setting grid
	 */
	private Grid<TbitsModelData> createGrid(){
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig servercolumn = new ColumnConfig(BAMailAccountClient.MAIL_SERVER, "Server Name", 120);
		TextField<String> servername = new TextField<String>();
		servercolumn.setEditor(new TbitsCellEditor(servername));
		configs.add(servercolumn);

		ColumnConfig logincolumn = new ColumnConfig(BAMailAccountClient.EMAIL_ID, "Login ID", 100);
		TextField<String> loginid = new TextField<String>();
		logincolumn.setEditor(new TbitsCellEditor(loginid));
		configs.add(logincolumn);

		ColumnConfig passwordcolumn = new ColumnConfig(BAMailAccountClient.PASSWARD, "Password", 120);
		passwordcolumn.setRenderer(new GridCellRenderer<BAMailAccountClient>() {
			public Object render(final BAMailAccountClient model,
					String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					ListStore<BAMailAccountClient> store,
					Grid<BAMailAccountClient> grid) {
				TextField<String> passwordField = new TextField<String>();
				passwordField.setPassword(true);
				passwordField.setWidth("110");
				passwordField.addListener(Events.OnBlur,
						new Listener<BaseEvent>() {
					@SuppressWarnings("unchecked")
					public void handleEvent(BaseEvent be) {
						model.setPassward(((TextField<String>) be
								.getSource()).getValue());
					}
				});
				passwordField.setValue(model.getPassward());
				return passwordField;
			}
		});
		configs.add(passwordcolumn);

		ColumnConfig protocolColumn = new ColumnConfig(BAMailAccountClient.PROTOCOL, "Protocol", 60);
		TextField<String> protocol = new TextField<String>();
		protocolColumn.setEditor(new TbitsCellEditor(protocol));
		configs.add(protocolColumn);

		ColumnConfig portcolumn = new ColumnConfig(BAMailAccountClient.PORT, "Port", 80);
		portcolumn.setRenderer(new GridCellRenderer<BAMailAccountClient>() {
			public Object render(final BAMailAccountClient model, String property, com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,	ListStore<BAMailAccountClient> store, Grid<BAMailAccountClient> grid) {
				
				NumberField portField = new NumberField();
				portField.setAllowDecimals(false);
				portField.setAllowNegative(false);
				portField.addListener(Events.OnBlur, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						if (((NumberField) be.getSource()).getValue() != null)
							model.setPort(((NumberField) be.getSource()).getValue().intValue());
						else {
							model.setPort(0);
						}
					}
				});
				portField.setValue(model.getPort());
				portField.setWidth(70);
				return portField;
			}
		});
		configs.add(portcolumn);

		ColumnConfig emailcolumn = new ColumnConfig(BAMailAccountClient.BA_EMAIL_ADDRESS, "Email Address", 140);
		TextField<String> email = new TextField<String>();
		emailcolumn.setEditor(new TbitsCellEditor(email));
		configs.add(emailcolumn);

		GridCellRenderer<TbitsModelData> categoryRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {

				final ComboBox<TypeClient> categoryBox = new ComboBox<TypeClient>();
				categoryBox.setWidth(120);
				categoryBox.setEditable(false);
				categoryBox.setStore(typeClients);
				categoryBox.setDisplayField(TypeClient.DISPLAY_NAME);
				for (TypeClient typeClient : typeClients.getModels()) {
					if (((Integer) typeClient.get(TypeClient.TYPE_ID)) == ((Integer) (model.get(property)))) {
						categoryBox.setValue(typeClient);
						break;
					}
				}
				categoryBox.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
					public void selectionChanged(
							SelectionChangedEvent<TypeClient> se) {
						model.set(BAMailAccountClient.CATEGORY_ID, se
								.getSelectedItem().getTypeId());
					}
				});
				return categoryBox;
			}
		};
		ColumnConfig categoryConf = new ColumnConfig(BAMailAccountClient.CATEGORY_ID, "Category", 140);
		categoryConf.setRenderer(categoryRenderer);
		configs.add(categoryConf);

		CheckColumnConfig isActivecolumn = new CheckColumnConfig(BAMailAccountClient.IS_ACTIVE, "Is Active", 50);
		GridCellRenderer<BAMailAccountClient> isActiveRenderer = new GridCellRenderer<BAMailAccountClient>() {
			public Object render(final BAMailAccountClient model,
					String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<BAMailAccountClient> store,
					final Grid<BAMailAccountClient> grid) {
				final CheckBox isActiveCheck = new CheckBox();
				isActiveCheck.addListener(Events.OnClick,
						new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						model.setIsActive(((CheckBox) be.getSource()).getValue());
					}
				});
				if(model.get(BAMailAccountClient.IS_ACTIVE) != null)
					isActiveCheck.setValue(model.getIsActive());
				return isActiveCheck;
			}
		};
		isActivecolumn.setRenderer(isActiveRenderer);
		configs.add(isActivecolumn);

		GridCellRenderer<TbitsModelData> deletebuttonRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					final Grid<TbitsModelData> grid) {

				Button delete = new Button("Delete", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						MessageBox confirmationBox = new MessageBox();
						confirmationBox.setClosable(false);
						confirmationBox.setMessage("Do you really want to delete mail setting");
						confirmationBox.setTitle("Delete Mail Setting");
						confirmationBox.setButtons(MessageBox.YESNO);
						confirmationBox.setIcon(MessageBox.QUESTION);
						confirmationBox.addCallback(new Listener<MessageBoxEvent>() {

							public void handleEvent(MessageBoxEvent be) {
								if (be.getButtonClicked().getText().toLowerCase().equals("yes")) {
									store.remove(model);
								}
							}

						});
						confirmationBox.show();
					}
				});
				delete.setWidth(50);
				return delete;
			}
		};
		ColumnConfig deletecolumn = new ColumnConfig("delete", "Delete", 60);
		deletecolumn.setRenderer(deletebuttonRenderer);
		configs.add(deletecolumn);

		GridCellRenderer<BAMailAccountClient> testButtonRenderer = new GridCellRenderer<BAMailAccountClient>() {
			public Object render(final BAMailAccountClient model,
					String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<BAMailAccountClient> store,
					Grid<BAMailAccountClient> grid) {

				Button test = new Button("Test", new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						APConstants.apService.testMailSetting(model.getMailServer(), ((Object) model.getPort()).toString(), model
								.getEmailID(), model.getPassward(),model.getProtocol(),	new AsyncCallback<Boolean>() {
							public void onFailure(Throwable caught) {
								TbitsInfo.error("Test Failed, ", caught);
								Log.error("Test Failed, ", caught);
							}

							public void onSuccess(Boolean result) {
								if (result) {
									TbitsInfo.info("Tested mail settings successfully");
								} else {
									TbitsInfo.error("Test Failed, could not connect to server");
								}
							}

						});
					}
				});
				test.setWidth(50);
				return test;
			}
		};
		ColumnConfig testColumn = new ColumnConfig("test", "Test", 60);
		testColumn.setRenderer(testButtonRenderer);
		configs.add(testColumn);

		ColumnModel cm = new ColumnModel(configs);

		EditorGrid<TbitsModelData> grid = new EditorGrid<TbitsModelData>(list, cm);
		grid.setAutoExpandColumn(BAMailAccountClient.BA_EMAIL_ADDRESS);
		grid.setAutoExpandMin(100);
		grid.setAutoExpandMax(400);
		grid.setAutoHeight(true);
		
		return grid;
	}

	public void addDefaultMailSettings() {
		BAMailAccountClient temp = new BAMailAccountClient();
		temp.set(BAMailAccountClient.MAIL_SERVER, "");
		temp.set(BAMailAccountClient.EMAIL_ID, "");
		temp.set(BAMailAccountClient.PASSWARD, "");
		temp.set(BAMailAccountClient.PROTOCOL,BAMailAccountClient.DEFAULT_PROTOCOL);
		temp.set(BAMailAccountClient.PORT, BAMailAccountClient.DEFAULT_PORT);
		temp.set(BAMailAccountClient.BA_EMAIL_ADDRESS, "");
		temp.set(BAMailAccountClient.IS_ACTIVE, true);
		temp.set(BAMailAccountClient.BA_MAIL_AC_ID, 0); // Must put 0 because of
		// server side
		// implementation
		temp.set(BAMailAccountClient.CATEGORY_ID, 1);
		temp.set(BAMailAccountClient.BA_PREFIX, sysPrefix);

		list.add(temp);
	}

	@SuppressWarnings("unchecked")
	private void reloadData() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			TbitsInfo.info("Business Area not loaded, waiting....");
			return;
		}
		for (String key : map.keySet()) {
			if (ClientUtils.getBAbySysPrefix(sysPrefix).getProperties().containsKey(key)) {
				map.get(key).setValue(ClientUtils.getBAbySysPrefix(sysPrefix).get(key));
			} else if (ClientUtils.getBAbySysPrefix(sysPrefix).getSysConfigObject().getProperties().containsKey(key)) {
				SysConfigClient sc = ((SysConfigClient) ClientUtils.getBAbySysPrefix(sysPrefix).get(BusinessAreaClient.SYS_CONFIG_OBJECT));
				if (key == SysConfigClient.MY_VOLUNTEER) {
					((Radio) ((RadioGroup) map.get(key)).get(2 - sc.getVolunteer())).setValue(true);
				}else if(key.equals(SysConfigClient.MY_ACTION_NOTIFY) || key.equals(SysConfigClient.MY_REQUEST_NOTIFY)){
					int val = (Integer)sc.get(key);
					if(val == 1)
						map.get(key).setValue(true);
				}else {
					map.get(key).setValue(sc.get(key));
				}
			}
		}
		loadCategories();
		reloadBAMailAccounts();

		TbitsInfo.info("Business Area loaded");
	}

	private void reloadBAMailAccounts() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			TbitsInfo.info("Business Area not loaded");
			return;
		}
		
		list.removeAll();
		APConstants.apService.getBAMailAccount(sysPrefix, new AsyncCallback<List<BAMailAccountClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to load ba mail account settings", caught);
				Log.error("Failed to load ba mail account settings", caught);
			}

			public void onSuccess(List<BAMailAccountClient> result) {
				list.add(result);
				list.sort(BAMailAccountClient.BA_MAIL_AC_ID, SortDir.ASC);
			}
		});
	}
	
	private void loadCategories(){
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			TbitsInfo.info("Business Area not loaded");
			return;
		}
		
		typeClients.removeAll();
		APConstants.apService.getTypeList(sysPrefix, IFixedFields.CATEGORY, new AsyncCallback<ArrayList<TypeClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to load categories", caught);
				Log.error("Failed to load categories", caught);
			}

			public void onSuccess(ArrayList<TypeClient> result) {
				for (TypeClient type : result) {
					typeClients.add(type);
				}
			}
		});
	}

	private BusinessAreaClient getUpdatedBAClient() {
		BusinessAreaClient baClient = new BusinessAreaClient();
		for (String key : ClientUtils.getBAbySysPrefix(sysPrefix).getPropertyNames()) {
			if (map.containsKey(key) && map.get(key).isEnabled()
					&& !map.get(key).isReadOnly()
					&& !(map.get(key) instanceof LabelField)) {
				if (map.get(key) instanceof NumberField) {
					baClient.set(key, ((NumberField) map.get(key)).getValue().intValue());
				} else {
					baClient.set(key, map.get(key).getValue());
				}
			} else if (!key.equals(BusinessAreaClient.SYS_CONFIG_OBJECT)) {
				baClient.set(key, ClientUtils.getBAbySysPrefix(sysPrefix).get(key));
			}
		}

		Map<String, Object> sysConf = ClientUtils.getBAbySysPrefix(sysPrefix).getSysConfigObject().getProperties();
		SysConfigClient sysConfClient = new SysConfigClient();
		for (String key : map.keySet()) {
			if (sysConf.containsKey(key) && map.get(key).isEnabled()
					&& !map.get(key).isReadOnly()
					&& !(map.get(key) instanceof LabelField)) {
				if (key.equals(SysConfigClient.MY_VOLUNTEER)) {
					String selectedRadio = ((RadioGroup) map.get(key)).getValue().getBoxLabel();
					if (selectedRadio == "Round Robin") {
						sysConfClient.set(SysConfigClient.MY_VOLUNTEER, 2);
					} else if (selectedRadio == "Random") {
						sysConfClient.set(SysConfigClient.MY_VOLUNTEER, 1);
					} else {
						sysConfClient.set(SysConfigClient.MY_VOLUNTEER, 0);
					}
				} else if (map.get(key) instanceof NumberField) {
					sysConfClient.set(key, (Integer) ((NumberField) map.get(key)).getValue().intValue());
				}else if(key.equals(SysConfigClient.MY_REQUEST_NOTIFY) || key.equals(SysConfigClient.MY_ACTION_NOTIFY)){
					sysConfClient.set(key, ((Boolean)map.get(key).getValue()) ? 1 : 0);
				} else {
					sysConfClient.set(key, map.get(key).getValue());
				}
			}
		}
//		for (String key : sysConf.keySet()) {
//			if (sysConfClient.get(key) == null) {
//				sysConfClient.set(key, sysConf.get(key));
//			}
//		}
		baClient.set(BusinessAreaClient.SYS_CONFIG_OBJECT, sysConfClient);
		return baClient;
	}

	private void updateBA() {
		final BusinessAreaClient ba = getUpdatedBAClient();
		TbitsInfo.info("Updating BA Properties... Please Wait...");
		APConstants.apService.updateBA(ba, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("failed to update Bussiness Area", caught);
				Log.error("failed to update Bussiness Area", caught);
			}

			public void onSuccess(Boolean result) {
				if (result) {
					updatedMailClients();
					TbitsEventRegister.getInstance().fireEvent(new OnBAUpdate(ba));
					TbitsInfo.info("BA successfully updated");
				} else {
					TbitsInfo.error("Failed to update BA");
				}
			}
		});
	}

	/**
	 * Save/update the values in 'Incoming Mail Settings' grid
	 */
	private void updatedMailClients() {
		TbitsInfo.info("Updating Mail Client Properties... Please Wait...");
		APConstants.apService.updateMailAccounts(sysPrefix, list.getModels(), new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to update mail Settings", caught);
				Log.error("Failed to update mail Settings", caught);
			}

			public void onSuccess(Boolean result) {
				if (result) {
					TbitsInfo.info("Mail Settings Saved Successfully..");
					reloadBAMailAccounts();
				} else {
					TbitsInfo.error("Failed to update BA Mail Accounts");
				}
			}
		});
	}

	private Window createNewBAWindow() {
		final Window addNewBAWindow = new Window();
		addNewBAWindow.setSize(400, 150);
		addNewBAWindow.setPlain(false);
		addNewBAWindow.setModal(true);
		addNewBAWindow.setHeading("Create Business Area");
		addNewBAWindow.setLayout(new FitLayout());
		
		FormPanel form = new FormPanel();
		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setFieldWidth(180);
		form.setLabelWidth(150);
		
		final TextField<String> sysPrefix = new TextField<String>();
		sysPrefix.setFieldLabel("System Prefix");
		form.add(sysPrefix);

		final TextField<String> baName = new TextField<String>();
		baName.setFieldLabel("Business Area Name");
		form.add(baName);
		
		addNewBAWindow.add(form, new FitData());

		addNewBAWindow.addButton(new ToolBarButton("Submit", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (sysPrefix.getValue() == null || sysPrefix.getValue().trim().equals("")
						|| baName.getValue() == null
						|| baName.getValue().trim().equals("")) {
					TbitsInfo.info("Both the fields must be non empty");
					return;
				}
				
				TbitsInfo.info("Creating a new BA... Please Wait...");
				APConstants.apService.createNewBA(sysPrefix.getValue(),	baName.getValue(), new AsyncCallback<BusinessAreaClient>() {
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Failed to create Business Area", caught);
						Log.error("Failed to create Business Area", caught);
					}

					public void onSuccess(BusinessAreaClient result) {
						if (result != null) {
							addNewBAWindow.hide();
							TbitsInfo.info("Successfully created new Business Area");
							TbitsEventRegister.getInstance().fireEvent(new OnBACreated(result));
						} else {
							TbitsInfo.error("Failed to create new Business Area");
						}
					}

				});
			}
		}));

		addNewBAWindow.addButton(new ToolBarButton("Cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				addNewBAWindow.hide();
			}
		}));

		addNewBAWindow.hide();
		return addNewBAWindow;
	}
	
	private Window importBAWindow(){
		final Window importBAWindow = new Window();
		importBAWindow.setSize(500,200);
		importBAWindow.setPlain(false);
		importBAWindow.setModal(true);
		importBAWindow.setHeading("Create Business Area");
		importBAWindow.setLayout(new FitLayout());
		
		final FormPanel importBAForm = new FormPanel();
		importBAForm.setHeaderVisible(false);
		importBAForm.setBodyBorder(false);
		importBAForm.setFieldWidth(180);
		importBAForm.setLabelWidth(150);
		
		importBAForm.setMethod(Method.POST);
		importBAForm.setEncoding(Encoding.MULTIPART);
		importBAForm.setAction(AdminUtils.getAppBaseURL() + "exportbusinessarea?ba="
		    			+ sysPrefix
		    			+ "&actionType=import");

		final TextField<String> sysPrefix = new TextField<String>();
		sysPrefix.setFieldLabel("System Prefix");
		sysPrefix.setName("sysPrefix");
		importBAForm.add(sysPrefix);

		final TextField<String> baName = new TextField<String>();
		baName.setFieldLabel("Business Area Name");
		baName.setName("sysName");
		importBAForm.add(baName);
		
		final TextField<String> sysEMail = new TextField<String>();
		sysEMail.setFieldLabel("E - Mail");
		sysEMail.setName("sysEmail");
		importBAForm.add(sysEMail);
		
		FileUploadField fUpload = new FileUploadField();
		fUpload.setFieldLabel("File to be imported");
		fUpload.setName("import");
		importBAForm.add(fUpload);
		
		importBAWindow.add(importBAForm, new FitData());
		
		importBAWindow.addButton(new Button("import", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				importBAForm.submit();
				importBAWindow.disable();
			}
	    }));
		
		importBAForm.addListener(Events.Submit, new Listener<FormEvent>(){

			public void handleEvent(FormEvent be) {
				String result = be.getResultHtml();
				TbitsInfo.info(result);
				com.google.gwt.user.client.Window.alert(result);
				importBAWindow.enable();
			}});

		return importBAWindow;
	}
}
