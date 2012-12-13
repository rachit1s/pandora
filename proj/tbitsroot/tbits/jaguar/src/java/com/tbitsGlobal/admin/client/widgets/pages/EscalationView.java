package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.events.OnUsersReceived;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * The Class for Escalation Hierarchy & Escalation Condition Removing Elements
 * from Hierarchy in ClientSide need to be revised
 * 
 * @author ankit
 * 
 */

public class EscalationView extends APTabItem {
	// List of AllUser
	private ListView<UserClient> userlist;

	// TreeStore containing the CurrentUserHierarchy
	private TreePanel<UserClient> userTree;

	// Map of Parent & all its Children
	private HashMap<UserClient, List<UserClient>> mapParentChild;

	private ListStore<TypeClient> severityTypeClient = new ListStore<TypeClient>();
	private ListStore<TypeClient> categoryTypeClient = new ListStore<TypeClient>();
	private ListStore<TypeClient> statusTypeClient = new ListStore<TypeClient>();
	private ListStore<TypeClient> typeTypeClient = new ListStore<TypeClient>();

	private ListStore<TbitsModelData> list = new ListStore<TbitsModelData>();

	public EscalationView(LinkIdentifier linkId) {
		super(linkId);

		this.setClosable(true);
		this.setLayout(new FitLayout());

		userlist = new ListView<UserClient>();
		userlist.setDisplayProperty(UserClient.USER_LOGIN);
		userlist.setStore(new ListStore<UserClient>());

		userTree = new TreePanel<UserClient>(new TreeStore<UserClient>());
		userTree.setDisplayProperty(UserClient.USER_LOGIN);

		mapParentChild = new HashMap<UserClient, List<UserClient>>();

		observable.subscribe(OnChangeBA.class,
				new ITbitsEventHandle<OnChangeBA>() {
					public void handleEvent(OnChangeBA event) {

						userTree.getStore().removeAll();
						mapParentChild.clear();
						getAllParentChildMap();

						severityTypeClient.removeAll();
						categoryTypeClient.removeAll();
						typeTypeClient.removeAll();
						statusTypeClient.removeAll();

						addToCategory();
						addToSeverity();
						addToStatus();
						addToType();
						getCurrentEscalationCondition();
					}
				});

		observable.subscribe(OnUsersReceived.class,
				new ITbitsEventHandle<OnUsersReceived>() {
					public void handleEvent(OnUsersReceived event) {
						userTree.getStore().removeAll();
						mapParentChild.clear();
						getAllUser();
						getAllParentChildMap();
					}
				});
	}

	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		getAllUser();

		addToCategory();
		addToSeverity();
		addToStatus();
		addToType();
		getCurrentEscalationCondition();

		LayoutContainer mainPanel = new LayoutContainer();
		mainPanel.setLayout(new BorderLayout());

		// Filter for AllUsers
		StoreFilterField<UserClient> filter = new StoreFilterField<UserClient>() {
			@Override
			protected boolean doSelect(Store<UserClient> store,
					UserClient parent, UserClient record, String property,
					String filter) {
				String login = record.getUserLogin();
				login = login.toLowerCase();
				if (login.contains(filter.toLowerCase())) {
					return true;
				}
				return false;
			}
		};
		filter.bind(userlist.getStore());
		filter.setEmptyText("Search a User");
		ToolBar toolBar = new ToolBar();
		toolBar.setBorders(true);
		toolBar.add(new LabelToolItem("Search :"));
		toolBar.add(filter);

		ContentPanel leftpanel = new ContentPanel();
		leftpanel.setHeading("Users");
		leftpanel.setLayout(new FitLayout());
		leftpanel.setTopComponent(toolBar);
		leftpanel.add(userlist, new FitData());

		BorderLayoutData leftData = new BorderLayoutData(LayoutRegion.WEST);
		leftData.setSize(new Float(0.45));
		mainPanel.add(leftpanel, leftData);

		LayoutContainer buttonsContainer = new LayoutContainer(
				new CenterLayout());
		buttonsContainer.setStyleAttribute("background", "white");
		Button shiftButton = new Button("Add >>>");
		buttonsContainer.add(shiftButton);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		mainPanel.add(buttonsContainer, centerData);

		ToolBar buttonPanel = new ToolBar();
		buttonPanel.setAlignment(HorizontalAlignment.CENTER);
		ToolBarButton save = new ToolBarButton(" Save all Changes ",
				new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {
						TbitsInfo.info("clicked on save changes button");

					}
				});
		buttonPanel.add(save);
		ToolBarButton deleteuser = new ToolBarButton(
				"Delete from Current Hierarchy");
		buttonPanel.add(deleteuser);

		// Filter for TreeStore
		StoreFilterField<UserClient> treefilter = new StoreFilterField<UserClient>() {
			@Override
			protected boolean doSelect(Store<UserClient> store,
					UserClient parent, UserClient record, String property,
					String filter) {
				String login = record.getUserLogin();
				login = login.toLowerCase();
				if (login.contains(filter.toLowerCase())) {
					return true;
				}
				return false;
			}
		};

		treefilter.bind(userTree.getStore());
		treefilter.setEmptyText("Search a User");
		ToolBar treeBar = new ToolBar();
		treeBar.setBorders(true);
		treeBar.add(new LabelToolItem("Search :"));
		treeBar.add(treefilter);

		ContentPanel rightpanel = new ContentPanel();
		rightpanel.setHeading("Current Hierarchy");
		rightpanel.setLayout(new FitLayout());
		rightpanel.setTopComponent(treeBar);

		rightpanel.add(userTree, new FitData());
		rightpanel.setBottomComponent(buttonPanel);

		BorderLayoutData rightData = new BorderLayoutData(LayoutRegion.EAST);
		rightData.setSize(new Float(0.45));
		mainPanel.add(rightpanel, rightData);

		shiftButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				UserClient username = userTree.getSelectionModel()
						.getSelectedItem();
				List<UserClient> toAddUsers = userlist.getSelectionModel()
						.getSelectedItems();

				// No Users are Selected to Add
				if (toAddUsers != null) {

					// Nodes of Tree and Users to Add are both Selected
					if (!toAddUsers.isEmpty() && username != null) {
						for (UserClient toAdd : toAddUsers) {

							// The Selected Item to Add already exist in Tree
							if (userTree.getStore().contains(toAdd)) {

								// The Selected item to Add is the Root Node
								if (userTree.getStore().getRootItems()
										.contains(toAdd)) {

									// Check For the Cycle if Present(Root is
									// not itself is not the parent)
									if (!checkForCycle(toAdd, username)) {
										TbitsInfo
												.warn("Cyclic Order ...Please add another User ...");
									}

									else {
										// The RootNode have Children
										if (mapParentChild
												.containsKey(username)) {
											List<UserClient> arr = mapParentChild
													.get(username);
											arr.add(toAdd);
											mapParentChild.remove(username);
											mapParentChild.put(username, arr);
										}
										// The Root Node does not have Children
										else {
											ArrayList<UserClient> arr = new ArrayList<UserClient>();
											arr.add(toAdd);
											mapParentChild.put(username, arr);
										}
										createTreeStructure();
										userTree.collapseAll();
										userTree.setExpanded(username, true);
									}
								} else
									TbitsInfo
											.warn("Selected User already Present ... ");
							}

							// The Selected item to Add does not exist in Tree
							else {
								userTree.getStore().add(username, toAdd, true);
								userTree.collapseAll();
								userTree.setExpanded(username, true);

								// The Selected Item of Tree contains Children
								if (!mapParentChild.containsKey(username)) {
									ArrayList<UserClient> arr = new ArrayList<UserClient>();
									arr.add(toAdd);
									mapParentChild.put(username, arr);
								}
								// The Selected Item of Tree does not contains
								// Children
								else {
									List<UserClient> arr = mapParentChild
											.get(username);
									arr.add(toAdd);
									mapParentChild.remove(username);
									mapParentChild.put(username, arr);
								}
							}
						}
					}

					// Users to Add are Selected and Nodes from Tree not
					// Selected
					else if (!toAddUsers.isEmpty() && username == null) {
						for (UserClient toAdd : toAddUsers) {
							if (!userTree.getStore().contains(toAdd))
								userTree.getStore().add(toAdd, true);
							else
								TbitsInfo
										.warn("Selected User already Present ... ");
						}
					}
				} else {
					TbitsInfo.warn("Select a User to Add ...");
				}
				userTree.getSelectionModel().deselectAll();
				userlist.getSelectionModel().deselectAll();
			}
		});

		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				for (UserClient uc : mapParentChild.keySet()) {
					for (UserClient u : mapParentChild.get(uc)) {
						addUserHierarch(u.getUserId(), uc.getUserId());
					}
				}
				createTreeStructure();
			}
		});

		// The deleteuser button Handler Delete user from server
		deleteuser.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				deleteFromHeirarchy();
			}
		});

		ContentPanel escalationCond = new ContentPanel();
		escalationCond.setHeaderVisible(true);
		escalationCond.setLayout(new FitLayout());
		escalationCond.setHeading("Escalation Condition");

		ToolBar tb = new ToolBar();
		ToolBarButton addBtn = new ToolBarButton("Add Escalation Condition");
		addBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				addDefaultEscalation();
			}
		});
		tb.add(addBtn);
		escalationCond.setTopComponent(tb);

		Grid<TbitsModelData> grid = createEscalationGrid();
		escalationCond.add(grid, new FitData());

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH);
		mainPanel.add(escalationCond, southData);

		this.add(mainPanel, new FitData());
	}

	private void deleteFromHeirarchy() {
		Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>() {
			public void handleEvent(MessageBoxEvent be) {
				Button b = be.getButtonClicked();
				if (b.getText().endsWith("Yes")) {

					UserClient toDelete = userTree.getSelectionModel()
							.getSelectedItem();
					if (toDelete != null) {
						UserClient parent = userTree.getStore().getParent(
								toDelete);

						Boolean hasParent = false;
						Boolean hasChild = false;

						if (mapParentChild.containsKey(toDelete))
							hasChild = true;

						for (UserClient uc : mapParentChild.keySet()) {
							for (UserClient u : mapParentChild.get(uc)) {
								if (u.getUserLogin().equals(
										toDelete.getUserLogin()))
									hasParent = true;
							}
						}
						// if(mapParentChild.containsValue(toDelete)) hasParent
						// = true;

						if (!hasChild && !hasParent)
							userTree.getStore().remove(toDelete);
						else if (!hasChild && hasParent)
							deleteFromParent(toDelete);
						else if (hasChild && !hasParent)
							deleteRoot(toDelete);
						else if (hasChild && hasParent)
							deleteFunction(toDelete);

						createTreeStructure();

						userTree.getSelectionModel().deselectAll();
						if (parent != null) {
							userTree.collapseAll();
							userTree.setExpanded(parent, true);
						}
					}
				}
			}
		};
		MessageBox.confirm("Confirm", "Are you sure you want to Delete ?", l);
	}

	private Grid<TbitsModelData> createEscalationGrid() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		final ColumnConfig severityConf = new ColumnConfig("severity_id",
				"Severity", 200);
		GridCellRenderer<TbitsModelData> severityRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {
				ComboBox<TypeClient> severityCombobox = new ComboBox<TypeClient>();
				severityCombobox.setWidth(severityConf.getWidth() - 20);
				severityCombobox.setEmptyText("select a severity");
				severityCombobox.setStore(severityTypeClient);
				severityCombobox.setDisplayField(TypeClient.DISPLAY_NAME);

				if (!severityTypeClient.getModels().isEmpty()) {
					for (TypeClient typeClient : severityTypeClient.getModels()) {
						if (((Integer) typeClient.get(TypeClient.TYPE_ID)) == ((Integer) (model
								.get(property)))) {
							severityCombobox.setValue(typeClient);
							break;
						}
					}
				}
				severityCombobox
						.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
							public void selectionChanged(
									SelectionChangedEvent<TypeClient> se) {
								model.set("severity_id", se.getSelectedItem()
										.getTypeId());
							}
						});
				return severityCombobox;
			}
		};
		severityConf.setRenderer(severityRenderer);
		configs.add(severityConf);

		final ColumnConfig categoryConf = new ColumnConfig("category_id",
				"Category", 200);
		GridCellRenderer<TbitsModelData> categoryRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {
				ComboBox<TypeClient> categoryCombobox = new ComboBox<TypeClient>();
				categoryCombobox.setWidth(categoryConf.getWidth() - 20);
				categoryCombobox.setEmptyText("select a category");
				categoryCombobox.setStore(categoryTypeClient);
				categoryCombobox.setDisplayField(TypeClient.DISPLAY_NAME);

				if (!categoryTypeClient.getModels().isEmpty()) {
					for (TypeClient typeClient : categoryTypeClient.getModels()) {
						if (((Integer) typeClient.get(TypeClient.TYPE_ID)) == ((Integer) (model
								.get(property)))) {
							categoryCombobox.setValue(typeClient);
							break;
						}
					}
				}
				categoryCombobox
						.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
							public void selectionChanged(
									SelectionChangedEvent<TypeClient> se) {
								model.set("category_id", se.getSelectedItem()
										.getTypeId());
							}
						});
				return categoryCombobox;
			}
		};
		categoryConf.setRenderer(categoryRenderer);
		configs.add(categoryConf);

		final ColumnConfig statusConf = new ColumnConfig("status_id", "Status",
				200);
		GridCellRenderer<TbitsModelData> statusRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {
				ComboBox<TypeClient> statusCombobox = new ComboBox<TypeClient>();
				statusCombobox.setWidth(statusConf.getWidth() - 20);
				statusCombobox.setEmptyText("select a status");
				statusCombobox.setStore(statusTypeClient);
				statusCombobox.setDisplayField(TypeClient.DISPLAY_NAME);

				if (!statusTypeClient.getModels().isEmpty()) {
					for (TypeClient typeClient : statusTypeClient.getModels()) {
						if (((Integer) typeClient.get(TypeClient.TYPE_ID)) == ((Integer) (model
								.get(property)))) {
							statusCombobox.setValue(typeClient);
							break;
						}
					}
				}
				statusCombobox
						.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
							public void selectionChanged(
									SelectionChangedEvent<TypeClient> se) {
								model.set("status_id", se.getSelectedItem()
										.getTypeId());
							}
						});
				return statusCombobox;
			}
		};
		statusConf.setRenderer(statusRenderer);
		configs.add(statusConf);

		final ColumnConfig typeConf = new ColumnConfig("type_id", "Type", 200);
		GridCellRenderer<TbitsModelData> typeRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {
				ComboBox<TypeClient> typeCombobox = new ComboBox<TypeClient>();
				typeCombobox.setWidth(typeConf.getWidth() - 20);
				typeCombobox.setEmptyText("select a type");
				typeCombobox.setStore(typeTypeClient);
				typeCombobox.setDisplayField(TypeClient.DISPLAY_NAME);

				if (!typeTypeClient.getModels().isEmpty()) {
					for (TypeClient typeClient : typeTypeClient.getModels()) {
						if (((Integer) typeClient.get(TypeClient.TYPE_ID)) == ((Integer) (model
								.get(property)))) {
							typeCombobox.setValue(typeClient);
							break;
						}
					}
				}
				typeCombobox
						.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
							public void selectionChanged(
									SelectionChangedEvent<TypeClient> se) {
								model.set("type-id", se.getSelectedItem()
										.getTypeId());
							}
						});
				return typeCombobox;
			}
		};
		typeConf.setRenderer(typeRenderer);
		configs.add(typeConf);

		final ColumnConfig spanConf = new ColumnConfig("span", "span", 50);
		GridCellRenderer<TbitsModelData> spanRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					Grid<TbitsModelData> grid) {
				final NumberField time = new NumberField();
				time.setAllowDecimals(false);
				time.setAllowNegative(false);
				time.setWidth(spanConf.getWidth() - 20);
				time.addListener(Events.OnBlur, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						if (time.getValue() != null)
							model.set("span", time.getValue().intValue());
					}
				});
				time.setValue((Integer) model.get("span"));
				return time;
			}
		};
		spanConf.setRenderer(spanRenderer);
		configs.add(spanConf);

		final ColumnConfig deletecolumn = new ColumnConfig("delete", "Delete",
				70);
		GridCellRenderer<TbitsModelData> deletebuttonRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<TbitsModelData> store,
					final Grid<TbitsModelData> grid) {
				Button delete = new Button("Delete",
						new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {

								Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>() {
									public void handleEvent(MessageBoxEvent be) {
										Button b = be.getButtonClicked();
										if (b.getText().endsWith("Yes")) {

											APConstants.apService
													.deleteEscalationCondition(
															ClientUtils
																	.getCurrentBA()
																	.getSystemId(),
															model,
															new AsyncCallback<Boolean>() {
																public void onFailure(
																		Throwable caught) {
																	TbitsInfo
																			.error(
																					caught
																							.getMessage(),
																					caught);
																	Log
																			.error(
																					caught
																							.getMessage(),
																					caught);
																}

																public void onSuccess(
																		Boolean result) {
																	if (result) {
																		list
																				.remove(model);
																	} else {
																		TbitsInfo
																				.error("Could not delete escalation heirarchy... Please try again");
																		Log
																				.error("Could not delete escalation heirarchy... Please try again");
																	}
																}
															});
										}
									}
								};
								MessageBox.confirm("Confirm",
										"Are you sure you want to Delete ?", l);
							}
						});
				delete.setWidth(deletecolumn.getWidth() - 20);
				return delete;
			}
		};
		deletecolumn.setRenderer(deletebuttonRenderer);
		configs.add(deletecolumn);

		final ColumnConfig savecolumn = new ColumnConfig("save", "save", 70);
		GridCellRenderer<TbitsModelData> savebuttonRenderer = new GridCellRenderer<TbitsModelData>() {
			public Object render(final TbitsModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					final int rowIndex, final int colIndex,
					final ListStore<TbitsModelData> store,
					final Grid<TbitsModelData> grid) {
				Button save = new Button("Save",
						new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {
								APConstants.apService
										.getEscalationCondition(
												ClientUtils.getCurrentBA()
														.getSystemId(),
												new AsyncCallback<ArrayList<TbitsModelData>>() {
													public void onFailure(
															Throwable caught) {
														TbitsInfo
																.error(
																		"Escalation Condition not Loaded ...Please Refresh ...",
																		caught);
													}

													public void onSuccess(
															ArrayList<TbitsModelData> result) {
														if (result != null) {
															for (TbitsModelData tb : result) {
																APConstants.apService
																		.deleteEscalationCondition(
																				ClientUtils
																						.getCurrentBA()
																						.getSystemId(),
																				tb,
																				new AsyncCallback<Boolean>() {
																					public void onFailure(
																							Throwable caught) {
																					}

																					public void onSuccess(
																							Boolean result) {
																					}
																				});
															}
														}
														for (TbitsModelData tb : list
																.getModels()) {
															APConstants.apService
																	.insertEscalationCondition(
																			ClientUtils
																					.getCurrentBA()
																					.getSystemId(),
																			tb,
																			new AsyncCallback<Boolean>() {
																				public void onFailure(
																						Throwable caught) {
																					TbitsInfo
																							.error(
																									"Escalation Condition not Added ...Please Refresh ...",
																									caught);
																					;
																				}

																				public void onSuccess(
																						Boolean result) {
																					if (result) {
																						TbitsInfo
																								.info("Escalation Condition Added ...");
																					}
																				}
																			});
														}
													}
												});
							}
						});
				save.setWidth(savecolumn.getWidth() - 20);
				return save;
			}
		};
		savecolumn.setRenderer(savebuttonRenderer);
		configs.add(savecolumn);

		ColumnModel cm = new ColumnModel(configs);
		final EditorGrid<TbitsModelData> grid = new EditorGrid<TbitsModelData>(
				list, cm);
		grid.setAutoWidth(true);
		grid.setAutoHeight(true);
		grid.addListener(Events.ColumnResize,
				new Listener<GridEvent<DisplayGroupClient>>() {
					public void handleEvent(GridEvent<DisplayGroupClient> be) {
						be.getGrid().getView().refresh(false);
					}
				});

		return grid;
	}

	// Function to get AllUsers from server
	private void getAllUser() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}

		HashMap<String, UserClient> users = CacheRepository.getInstance()
				.getCache(UserCacheAdmin.class).getMap();
		if (users == null)
			return;
		userlist.getStore().removeAll();
		for (UserClient uc : users.values()) {
			userlist.getStore().add(uc);
		}
	}

	// Function to get HashMap <UserClient (Parent) <ArrayList <UserClient>
	// (Children)> > from server
	private void getAllParentChildMap() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		mapParentChild.clear();
		APConstants.apService.getAllParentChildMapping(ClientUtils
				.getCurrentBA().getSystemId(),
				new AsyncCallback<HashMap<Integer, ArrayList<Integer>>>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error(
										"Error in Loading the User Hierarchy ...Please Refresh ...",
										caught);
					}

					public void onSuccess(
							HashMap<Integer, ArrayList<Integer>> result) {
						if (result != null) {
							for (int p : result.keySet()) {
								ArrayList<UserClient> childList = new ArrayList<UserClient>();
								UserClient uc = userIdtoUserClient(p);
								if (uc == null)
									continue;
								for (int c : result.get(p)) {
									UserClient child = userIdtoUserClient(c);
									if (child == null)
										continue;
									childList.add(child);
								}
								mapParentChild.put(uc, childList);
							}
							createTreeStructure();
						} else
							TbitsInfo.warn("User Hierarchy not Present ...");
					}
				});
	}

	// Function to add Child Parent Hierarchy to Database
	private void addUserHierarch(int iduser, int idparent) {
		APConstants.apService.insertUserhierarchy(ClientUtils.getCurrentBA()
				.getSystemId(), iduser, idparent, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error(
						"User Hierarchy not Added ...Please Refresh ...",
						caught);
			}

			public void onSuccess(Boolean result) {
				// createTreeStructure();
			}
		});
	}

	/**
	 * Function to delete Child Parent Hierarchy from Database
	 * 
	 * @param iduser
	 * @param idparent
	 */
	private void deleteUserHierarch(int iduser, int idparent) {
		APConstants.apService.deleteUserhierarchy(ClientUtils.getCurrentBA()
				.getSystemId(), iduser, idparent, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error(
						"User Hierarchy not Deleted ...Please Refresh ...",
						caught);
			}

			public void onSuccess(Boolean result) {
				// createTreeStructure();
			}
		});
	}

	// Function converts UserID to UserClient
	private UserClient userIdtoUserClient(int id) {
		for (UserClient temp : userlist.getStore().getModels()) {
			if (temp.getUserId() == id) {
				return temp;
			}
		}
		return null;
	}

	// Function creates the Tree Structure using mapParentChild (HashMap)
	private void createTreeStructure() {
		userTree.getStore().removeAll();
		if (mapParentChild.isEmpty())
			return;
		for (UserClient parentuc : mapParentChild.keySet()) {
			if (parentuc == null)
				continue;
			if (userTree.getStore().contains(parentuc))
				continue;
			userTree.getStore().add(parentuc, true);
			for (UserClient childuc : mapParentChild.get(parentuc)) {
				if (childuc == null)
					continue;
				if (userTree.getStore().contains(childuc)
						&& userTree.getStore().getParent(childuc) == null)
					userTree.getStore().remove(childuc);
				userTree.getStore().add(parentuc, childuc, true);
				findChildren(childuc);
			}
		}
	}

	// Function finds if Children exits of the Parent (To Add)
	private void findChildren(UserClient parent) {
		if (!mapParentChild.containsKey(parent))
			return;
		for (UserClient parentuc : mapParentChild.get(parent)) {
			if (parentuc == null)
				continue;
			if (userTree.getStore().contains(parentuc)
					&& userTree.getStore().getParent(parentuc) == null)
				userTree.getStore().remove(parentuc);
			userTree.getStore().add(parent, parentuc, true);
			findChildren(parentuc);
		}
	}

	// The Following Function to Delete User from the Current Hierarchy from
	// Server side

	// Function to Delete Root of Tree
	private void deleteRoot(UserClient toDelete) {
		if (toDelete == null)
			return;

		List<UserClient> children = mapParentChild.get(toDelete);
		for (UserClient childuc : children) {
			userTree.getStore().add(childuc, true);
			deleteUserHierarch(childuc.getUserId(), toDelete.getUserId());
		}
		mapParentChild.remove(toDelete);
	}

	// Function to Delete the leaf Node
	private void deleteFromParent(UserClient toDelete) {
		if (toDelete == null)
			return;
		UserClient parent = userTree.getStore().getParent(toDelete);
		List<UserClient> siblings = mapParentChild.get(parent);
		siblings.remove(toDelete);
		deleteUserHierarch(toDelete.getUserId(), parent.getUserId());
	}

	/**
	 * Function to Delete a node from Tree
	 */
	private void deleteFunction(UserClient toDelete) {
		if (toDelete == null)
			return;
		List<UserClient> children = mapParentChild.get(toDelete);
		UserClient parent = userTree.getStore().getParent(toDelete);
		List<UserClient> siblings = mapParentChild.get(parent);
		siblings.remove(toDelete);

		for (UserClient childuc : children) {
			siblings.add(childuc);
			deleteUserHierarch(childuc.getUserId(), toDelete.getUserId());
			addUserHierarch(childuc.getUserId(), parent.getUserId());
		}
		mapParentChild.remove(toDelete);
	}

	// The Function to Check if the Cyclic Order is present
	private boolean checkForCycle(UserClient parent, UserClient child) {
		if (parent == null || child == null)
			return false;
		if (mapParentChild.isEmpty() || mapParentChild == null)
			return true;
		if (!mapParentChild.containsKey(parent))
			return true;
		for (UserClient childuc : mapParentChild.get(parent)) {
			if (childuc == null)
				continue;
			if (childuc.getUserLogin().equals(child.getUserLogin()))
				return false;
			if (mapParentChild.containsKey(childuc))
				if (!checkForCycle(childuc, child))
					return false;
		}
		return true;
	}

	// Function gets Escalation Condition "Severity" from Database
	private void addToSeverity() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		APConstants.apService.getTypeList(ClientUtils.getCurrentBA()
				.getSystemPrefix(), "severity_id",
				new AsyncCallback<ArrayList<TypeClient>>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error(
										"Severity Condition not Loaded ...Please Refresh ...",
										caught);
					}

					public void onSuccess(ArrayList<TypeClient> result) {
						if (result != null) {
							TypeClient temp = new TypeClient();
							temp.setDisplayName("--Any--");
							temp.set(TypeClient.TYPE_ID, 0);
							severityTypeClient.add(temp);

							for (TypeClient tempr : result) {
								severityTypeClient.add(tempr);
							}
						}
					}
				});
	}

	// Function gets Escalation Condition "Category" from Database
	private void addToCategory() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		APConstants.apService.getTypeList(ClientUtils.getCurrentBA()
				.getSystemPrefix(), "category_id",
				new AsyncCallback<ArrayList<TypeClient>>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error(
										"Category Condition not Loaded ...Please Refresh ...",
										caught);
					}

					public void onSuccess(ArrayList<TypeClient> result) {
						if (result != null) {
							TypeClient temp = new TypeClient();
							temp.setDisplayName("--Any--");
							temp.set(TypeClient.TYPE_ID, 0);
							categoryTypeClient.add(temp);

							for (TypeClient tempr : result) {
								categoryTypeClient.add(tempr);
							}
						}
					}
				});
	}

	// Function gets Escalation Condition "Status" from Database
	private void addToStatus() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		APConstants.apService.getTypeList(ClientUtils.getCurrentBA()
				.getSystemPrefix(), "status_id",
				new AsyncCallback<ArrayList<TypeClient>>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error(
										"Status Condition not Loaded ...Please Refresh ...",
										caught);
					}

					public void onSuccess(ArrayList<TypeClient> result) {
						if (result != null) {
							TypeClient temp = new TypeClient();
							temp.setDisplayName("--Any--");
							temp.set(TypeClient.TYPE_ID, 0);
							statusTypeClient.add(temp);

							for (TypeClient tempr : result) {
								statusTypeClient.add(tempr);
							}
						}
					}
				});
	}

	// Function gets Escalation Condition "Type" from Database
	private void addToType() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		APConstants.apService.getTypeList(ClientUtils.getCurrentBA()
				.getSystemPrefix(), "request_type_id",
				new AsyncCallback<ArrayList<TypeClient>>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error(
										"Type Condition not Loaded ...Please Refresh ...",
										caught);
					}

					public void onSuccess(ArrayList<TypeClient> result) {
						if (result != null) {
							TypeClient temp = new TypeClient();
							temp.setDisplayName("--Any--");
							temp.set(TypeClient.TYPE_ID, 0);
							typeTypeClient.add(temp);

							for (TypeClient tempr : result) {
								typeTypeClient.add(tempr);
							}
						}
					}
				});
	}

	// Function adds a Default Escalation Condition to the List
	private void addDefaultEscalation() {
		TbitsModelData temp = new TbitsModelData();
		temp.set("span", 0);
		if (categoryTypeClient != null)
			temp.set("category_id", categoryTypeClient.getAt(0).get(
					TypeClient.TYPE_ID));
		if (statusTypeClient != null)
			temp.set("status_id", statusTypeClient.getAt(0).get(
					TypeClient.TYPE_ID));
		if (severityTypeClient != null)
			temp.set("severity_id", severityTypeClient.getAt(0).get(
					TypeClient.TYPE_ID));
		if (typeTypeClient != null)
			temp
					.set("type_id", typeTypeClient.getAt(0).get(
							TypeClient.TYPE_ID));

		list.add(temp);
	}

	// Function gets the Escalation Condition from Database
	private void getCurrentEscalationCondition() {
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		APConstants.apService.getEscalationCondition(ClientUtils.getCurrentBA()
				.getSystemId(), new AsyncCallback<ArrayList<TbitsModelData>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error(
						"Escalaton Condition not Loaded ...Please Refresh ...",
						caught);
			}

			public void onSuccess(ArrayList<TbitsModelData> result) {
				if (result != null) {
					list.add(result);
				} else
					TbitsInfo.warn("Escalation Condition not Present ...");
			}
		});
	}
}