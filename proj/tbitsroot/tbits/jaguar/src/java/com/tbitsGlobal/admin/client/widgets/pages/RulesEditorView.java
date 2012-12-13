package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.RulesClient;
import commons.com.tbitsGlobal.utils.client.rules.FunctionDef;
import commons.com.tbitsGlobal.utils.client.rules.RuleDef;
import commons.com.tbitsGlobal.utils.client.rules.RulesTemplate;
import commons.com.tbitsGlobal.utils.client.rules.VarDef;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Creates a rule editor view for the admin panel. The rule editor view opens in a new tab.
 * The view is itself a tab panel supporting ruleList, newRule and editRule tabs.
 * 
 * @author Karan Gupta
 *
 */
public class RulesEditorView extends APTabItem {

	//================================================================================

	private TabPanel tabPanel;
	private ToolBar toolbar;
	private TabItem rulesListing;
	private Grid<RulesClient> grid;
	private GroupingStore<RulesClient> rulesListStore;
	private ListStore<RulesTemplate> templateStore;
	private Window compilationLog;
	
	//================================================================================

	/**
	 * Constructor
	 * @param header
	 * @param pageCaption
	 */
	public RulesEditorView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setLayout(new FitLayout());
		this.setClosable(true);
		initialise();
	}

	//================================================================================

	/**
	 * Initialise the tabs panel and the toolbar at the bottom.
	 */
	private void initialise() {
		
		ContentPanel cp = new ContentPanel(new FitLayout());
		cp.setHeaderVisible(false);
		cp.setBorders(false);
		tabPanel = new TabPanel();
		toolbar = new ToolBar();
		toolbar.setHeight(30);
		toolbar.setAlignment(HorizontalAlignment.CENTER);
		
		compilationLog = new Window();
		compilationLog.setSize(500, 300); 
		compilationLog.setPlain(true);
		compilationLog.setModal(true);
		compilationLog.setBlinkModal(true);
		compilationLog.setHeading("Compiler Output");
		compilationLog.setLayout(new FitLayout());
		compilationLog.setScrollMode(Scroll.AUTO);
		compilationLog.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				compilationLog.hide();
			}
		}));
		
		cp.add(tabPanel);
		cp.setBottomComponent(toolbar);
		this.add(cp);
		
		showRulesListing();
	}

	//================================================================================

	/**
	 * Show the listing of all the rules.
	 */
	private void showRulesListing() {

		// Initialise the tabPanel if not already initialised
		if(tabPanel == null)
			initialise();
		// Simply focus on the rulesListing if it is already constructed
		if(rulesListing != null){
			rulesListing.focus();
			return;
		}
		
		rulesListing = new TabItem("Available Rules");
		rulesListing.setClosable(false);
		
		rulesListStore = new GroupingStore<RulesClient>();
		APConstants.apService.getExistingRules(new AsyncCallback<ArrayList<RulesClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error in fetching existing rules.", caught);
			}
			public void onSuccess(ArrayList<RulesClient> result) {
				rulesListStore.removeAll();
				if(result != null)
					rulesListStore.add(result);
				rulesListStore.groupBy("type");  
			}
		});
	  
		// Make the column configuration to be displayed
	    List<ColumnConfig> config = new ArrayList<ColumnConfig>();

	    CheckBoxSelectionModel<RulesClient> sm = new CheckBoxSelectionModel<RulesClient>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		config.add(sm.getColumn());
		
	    ColumnConfig column = new ColumnConfig();
	    column.setId(RulesClient.NAME);
	    column.setHeader("Rule");
	    column.setResizable(true);
	    column.setWidth(300);
	    config.add(column);

	    config.add(new ColumnConfig(RulesClient.TYPE, "Type", 200));
	    
	    column = new ColumnConfig();
	    column.setId(RulesClient.SEQ_NO);
	    column.setHeader("Seq.No.");
	    column.setAlignment(HorizontalAlignment.CENTER);
	    column.setWidth(60);
	    column.setResizable(false);
	    column.setEditor(new CellEditor(new NumberField()));
	    config.add(column);
	    
	    column = new ColumnConfig();
	    column.setId(RulesClient.IS_DEPLOYED);
	    column.setHeader("Deployed?");
	    column.setAlignment(HorizontalAlignment.CENTER);
	    column.setWidth(80);
	    column.setResizable(false);
	    config.add(column);
	    
	    final ColumnModel cm = new ColumnModel(config);  
	  
	    GroupingView view = new GroupingView();  
	    view.setShowGroupedColumn(false);  
	    view.setGroupRenderer(new GridGroupRenderer() {  
	      public String render(GroupColumnData data) {  
	        return cm.getColumnById(data.field).getHeader() + ": " + data.group;  
	      }  
	    });  
	  
	    grid = new Grid<RulesClient>(rulesListStore, cm);  
	    grid.setView(view);  
	    grid.setBorders(false); 
	    grid.setAutoHeight(true);
	    grid.setColumnLines(true);
	    grid.setSelectionModel(sm);
	    
	    // Add the grid to the tab and sink the SELECT event
		rulesListing.add(grid);
		tabPanel.add(rulesListing);
		rulesListing.sinkEvents(Events.Select.getEventCode());
		rulesListing.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				showListingToolbar();
				if(!toolbar.isEnabled())
					toolbar.enable();
				APConstants.apService.getExistingRules(new AsyncCallback<ArrayList<RulesClient>>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Error in fetching existing rules.", caught);
					}
					public void onSuccess(ArrayList<RulesClient> result) {
						rulesListStore.removeAll();
						if(result != null)
							rulesListStore.add(result);
						rulesListStore.groupBy("type");  
					}
				});
			}
		});
		showListingToolbar();
		if(!toolbar.isEnabled())
			toolbar.enable();
	}
	
	//================================================================================

	/**
	 * Display the new rule editor.
	 * This tab takes the basic information for the rule from the user and forwards to the main editor for code input.
	 */
	private void showNewRuleEditor() {
		
		// Make a new rule editor
		final TabItem newRule = new TabItem("New Rule");
		newRule.setClosable(true);
		
		// Get the list of all the available templates
		if(templateStore == null){
			templateStore = new ListStore<RulesTemplate>();
			APConstants.apService.getRuleTemplates(new AsyncCallback<ArrayList<RuleDef>>(){

				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching available interfaces.", caught);
				}

				public void onSuccess(ArrayList<RuleDef> result) {
					templateStore.removeAll();
					if(result != null){
						for(RuleDef rd : result){
							RulesTemplate rt = new RulesTemplate(rd);
							templateStore.add(rt);
						}
					}
				}
				
			});
		}
		
		final ContentPanel newRulePanel = new ContentPanel();  
		newRulePanel.setFrame(true);  
		newRulePanel.setHeading("Implement an Interface.");  
		newRulePanel.setWidth(500);  
		newRulePanel.setAutoHeight(true);  
		newRulePanel.setBodyBorder(false);  
	  
		final TextField<String> ruleName = new TextField<String>();  
	    ruleName.setFieldLabel("Rule Name");
	    ruleName.setAllowBlank(false);
	    ruleName.setWidth(490);
	    ContentPanel namePanel = new ContentPanel();  
	    namePanel.setFrame(false);  
	    namePanel.setHeading("Rule Name");  
	    namePanel.setAutoWidth(true);  
	    namePanel.setAutoHeight(true);  
	    namePanel.setBodyBorder(false);  
	    namePanel.add(ruleName);
	    newRulePanel.add(namePanel);
	    
	    final CheckBoxSelectionModel<RulesTemplate> sm = new CheckBoxSelectionModel<RulesTemplate>();
	    sm.setSelectionMode(SelectionMode.SINGLE);
	    List<ColumnConfig> config = new ArrayList<ColumnConfig>();
	    config.add(sm.getColumn());
	    ColumnConfig column = new ColumnConfig();
	    column.setId(RulesTemplate.NAME);
	    column.setHeader("<b>Interface</b>");
	    column.setWidth(465);
	    config.add(column);

	    final ColumnModel cm = new ColumnModel(config);  
	    final Grid<RulesTemplate> grid = new Grid<RulesTemplate>(templateStore, cm);  
	    grid.setBorders(false); 
	    grid.setAutoHeight(true);
	    grid.setColumnLines(true);
	    grid.addPlugin(sm);
	    grid.setSelectionModel(sm);
	    newRulePanel.add(grid);

	    newRulePanel.addButton(new Button("Implement", new SelectionListener<ButtonEvent>() {  
	    	public void componentSelected(ButtonEvent ce) { 
	    		if(ruleName.getValue() == null || ruleName.getValue() == ""){
					TbitsInfo.info("Please enter a rule name.");
					return;
				}
				if(Character.isLowerCase(ruleName.getValue().charAt(0))){
					// TODO add more constraints for the name
					TbitsInfo.info("Rule name must start with an upper case character.");
					return;
				}
				if(grid.getSelectionModel().getSelection().size() == 0){
					TbitsInfo.info("Select an interface to implement.");
					return;
				}
				APConstants.apService.getRuleDetails(ruleName.getValue(), new AsyncCallback<RulesClient>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to fetch rule details!", caught);
					}

					public void onSuccess(RulesClient result) {
						if(result != null){
							TbitsInfo.error("A rule named " + result.name + " already exists. Please enter a different name.");
							return;
						}
						tabPanel.remove(newRule);
			    		showEditor(ruleName.getValue(), grid.getSelectionModel().getSelectedItem());
					}
				});
	    		
	    	}

	    }));  
	    
	    // Add the grid and content panel to the tab and sink the SELECT event
	    newRule.add(newRulePanel, new ColumnData(0.5));
	    
		tabPanel.add(newRule);
		tabPanel.setSelection(newRule);
		newRule.sinkEvents(Events.Select.getEventCode());
		newRule.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				showEditorToolbar(null, null);
				if(toolbar.isEnabled())
					toolbar.disable();
			}
		});
		showEditorToolbar(null, null);
		if(toolbar.isEnabled())
			toolbar.disable();
	}
	
	//================================================================================

	/**
	 * Create an editor for the specified template.
	 * @param name
	 * @param template
	 */
	private void showEditor(String name, final RulesTemplate template) {
		
		final TabItem ruleEditor = new TabItem(name);
		template.setName(name);
		ruleEditor.setClosable(true);
		ruleEditor.setLayout(new FlowLayout());
		ruleEditor.setScrollMode(Style.Scroll.AUTO);
		final ContentPanel cp = new ContentPanel();
		cp.setWidth(1130);
		
		if(template.get(RulesTemplate.NAME).equals(RulesTemplate.CUSTOM)){
			TextArea customClass= new TextArea();  
			customClass.setPreventScrollbars(false);  
			customClass.setFieldLabel("Class");
			customClass.setWidth(1130);
			customClass.setHeight(370);
			
			cp.setHeading("Define the entire class");
			cp.add(customClass);
			template.setObjType(RulesTemplate.CODE);
			template.setObj(customClass);
		}
		else{
			cp.setHeading("Implements " + template.get(RulesTemplate.NAME));
			HashMap<FunctionDef, TextArea> funcMap = new HashMap<FunctionDef, TextArea>();
			Iterable<FunctionDef> funcs = template.getRuleDef().getClassDef().getFunctions();
			for(FunctionDef func : funcs){
				String desc = (func.description==null)?"":func.description;
				String paramString = "";
				for(int i=0; i< func.params.size(); i++){
					if(i > 0)
						paramString += ", ";
					VarDef param = func.params.get(i);
					paramString += param.varType + " " + param.varName;
				}
				TextArea function = new TextArea();  
				function.setPreventScrollbars(false);  
				function.setFieldLabel(func.name);
				function.setWidth(1130);
				if(func.name.equals("execute")){
					function.setHeight(300);
				}
				cp.add(new Html(desc));
				cp.add(new Html("<br>" + func.modifiers + " " + func.returnType + " <b>" + func.name + "</b>(" + paramString + ") {"));
				cp.add(function);
			    cp.add(new Html("}<br>"));
			    funcMap.put(func, function);
			}
			template.setObjType(RulesTemplate.FUNCTION_MAP);
			template.setObj(funcMap);
			
			final ToolBar docLinkage = new ToolBar();
			final String iClass = template.getRuleDef().getClassDef().getImplementsClass();
			APConstants.apService.getClassDocumentationUrl(iClass, new AsyncCallback<String>() {

				public void onFailure(Throwable caught) {
					TbitsInfo.info("Could not fetch the documentation for " + iClass, caught);
				}

				public void onSuccess(final String result) {

					ToolBarButton btn = new ToolBarButton("View Interface Documentation", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							com.google.gwt.user.client.Window.open(result, iClass,
							           "menubar=no," + 
							           "location=false," + 
							           "resizable=yes," + 
							           "scrollbars=yes," + 
							           "status=no," + 
							           "dependent=true");

						}
						
					});
					docLinkage.add(btn);
				}
			});
			docLinkage.setAlignment(HorizontalAlignment.RIGHT);
			cp.setTopComponent(docLinkage);
		}
		
		ruleEditor.add(cp);
		tabPanel.add(ruleEditor);
		tabPanel.setSelection(ruleEditor);
		
		ruleEditor.sinkEvents(Events.Select.getEventCode());
		ruleEditor.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				showEditorToolbar(template, ruleEditor);
				if(!toolbar.isEnabled())
					toolbar.enable();
			}
		});
		
		// Warn before closing the editor window
		ruleEditor.addListener(Events.BeforeClose, new Listener<TabPanelEvent>() {

            boolean isConfirmedTabClose = false;

            public void handleEvent(final TabPanelEvent be) {

                if (isConfirmedTabClose) {
                    isConfirmedTabClose = false;
                } else {
                    be.setCancelled(true);

                    MessageBox box = new MessageBox();
                    box.setButtons(MessageBox.YESNO);
                    box.setIcon(MessageBox.QUESTION);
                    box.setTitle("Close the editor?");
                    box.setMessage("You will lose any unsaved changes. The rule will also be lost if it isn't saved.");
                    box.addCallback(new Listener<MessageBoxEvent>() {

                        public void handleEvent(MessageBoxEvent be) {
                            if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                isConfirmedTabClose = true;
                                ruleEditor.close();
                                isConfirmedTabClose = false;
                            }
                        }
                    });
                    box.show();
                }
            }
        });
	        
		showEditorToolbar(template, ruleEditor);
		if(!toolbar.isEnabled())
			toolbar.enable();
	}

	//================================================================================

	/**
	 * Show the editor for an existing rule
	 * 
	 * @param rc
	 */
	private void showEditor(RulesClient rc){
		
		final TabItem ruleEditor = new TabItem(rc.name);
		ruleEditor.setClosable(true);
		ruleEditor.setLayout(new FlowLayout());
		ruleEditor.setScrollMode(Style.Scroll.AUTO);
		
		TextField<Double> seqNo = new TextField<Double>(); 
		seqNo.setFieldLabel("Sequence Number");
		seqNo.setAllowBlank(true);
		seqNo.setWidth(50);
	    ContentPanel seqNumPanel = new ContentPanel();  
	    seqNumPanel.setFrame(false);  
	    seqNumPanel.setHeading("Sequence Number");  
	    seqNumPanel.setWidth(1130);  
	    seqNumPanel.setAutoHeight(true);  
	    seqNumPanel.setBodyBorder(false);  
	    seqNumPanel.add(seqNo);
	    seqNo.focus();
	    
	    ContentPanel cp = new ContentPanel();
		cp.setWidth(1130);
		final TextArea classEdit = new TextArea();  
		classEdit.setPreventScrollbars(false);  
		classEdit.setFieldLabel("Class");
		classEdit.setWidth(1130);
		classEdit.setHeight(370);
		cp.setHeading("Edit " + rc.name);
		cp.add(classEdit);
		
		ruleEditor.add(seqNumPanel);
		ruleEditor.add(cp);
		
		final RulesTemplate template = new RulesTemplate(RulesTemplate.CUSTOM);
		template.setName(rc.name);
		template.setObjType(RulesTemplate.CODE);
		template.setObj(classEdit);
		template.getRuleDef().setSeqNo(rc.seq_no);
		template.getRuleDef().setType(rc.type);
		template.setSeqNumber(seqNo);
		
		APConstants.apService.getRuleCode(rc.name, new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to fetch the code.", caught);
			}

			public void onSuccess(String result) {
				if(result == null){
					TbitsInfo.error("Error fetching the code.");
					return;
				}
				classEdit.setValue(result);
				classEdit.setOriginalValue(result);
				tabPanel.add(ruleEditor);
				tabPanel.setSelection(ruleEditor);
				showEditorToolbar(template, ruleEditor);
				if(!toolbar.isEnabled())
					toolbar.enable();
			}
		});
		
		ruleEditor.sinkEvents(Events.Select.getEventCode());
		ruleEditor.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				showEditorToolbar(template, ruleEditor);
				if(!toolbar.isEnabled())
					toolbar.enable();
			}
		});
		
		// Warn before closing the editor window
		ruleEditor.addListener(Events.BeforeClose, new Listener<TabPanelEvent>() {

            boolean isConfirmedTabClose = false;

            public void handleEvent(final TabPanelEvent be) {

                if (isConfirmedTabClose) {
                    isConfirmedTabClose = false;
                } else {
                    be.setCancelled(true);

                    MessageBox box = new MessageBox();
                    box.setButtons(MessageBox.YESNO);
                    box.setIcon(MessageBox.QUESTION);
                    box.setTitle("Close the editor?");
                    box.setMessage("You will lose any unsaved changes.");
                    box.addCallback(new Listener<MessageBoxEvent>() {

                        public void handleEvent(MessageBoxEvent be) {
                            if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                isConfirmedTabClose = true;
                                ruleEditor.close();
                                isConfirmedTabClose = false;
                            }
                        }
                    });
                    box.show();
                }
            }
        });
	}
	
	//================================================================================

	/**
	 * Display the toolbar for the listing view.
	 */
	private void showListingToolbar(){
		
		toolbar.removeAll();
		ToolBarButton btn = new ToolBarButton("New Rule", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				showNewRuleEditor();
			}
		});
		toolbar.add(btn);
		
		btn = new ToolBarButton("Edit", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				final RulesClient rc = grid.getSelectionModel().getSelectedItem();
				if(rc == null)
					return;
				showEditor(rc);
			}
		});
		toolbar.add(btn);
		
		btn = new ToolBarButton("Undeploy", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				final RulesClient rc = grid.getSelectionModel().getSelectedItem();
				if(rc == null)
					return;
				RuleDef rd = new RuleDef();
				rd.setName(rc.name);
				rd.setType(rc.type);
				rd.setSeqNo(rc.seq_no);
				APConstants.apService.undeployRule(rd, new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to undeploy rule!", caught);
					}

					public void onSuccess(Boolean result) {
						if(result){
							TbitsInfo.info("Successfully undeployed rule : " + rc.name + ". The changes will be affected on next system reboot.");
						}
						else{
							TbitsInfo.info("Could not find a deployed rule by name : " + rc.name);
						}
					}
					
				});
			}
		});
		toolbar.add(btn);
		
		btn = new ToolBarButton("Delete", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				
				MessageBox box = new MessageBox();
                box.setButtons(MessageBox.YESNO);
                box.setIcon(MessageBox.QUESTION);
                box.setTitle("Delete Rule?");
                box.setMessage("The rule will be lost permanently. If you only wish to unactivate the rule, UNDEPLOY it instead.");
                box.addCallback(new Listener<MessageBoxEvent>() {

                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                        	final RulesClient rc = grid.getSelectionModel().getSelectedItem();
            				if(rc == null)
            					return;
            				RuleDef rd = new RuleDef();
            				rd.setName(rc.name);
            				rd.setType(rc.type);
            				rd.setSeqNo(rc.seq_no);
            				APConstants.apService.deleteRule(rd, new AsyncCallback<Boolean>() {

            					public void onFailure(Throwable caught) {
            						TbitsInfo.error("Unable to remove rule!", caught);
            					}

            					public void onSuccess(Boolean result) {
            						if(result){
            							TbitsInfo.info("Successfully removed rule : " + rc.name + ". The changes will be affected on next system reboot.");
            							rulesListStore.remove(rc);
            						}
            						else{
            							TbitsInfo.info("Could not remove rule : " + rc.name);
            						}
            					}
            					
            				});
                        }
                    }
                });
                box.show();
                
				
			}
		});
		toolbar.add(btn);
	}
	
	//================================================================================

	/**
	 * Display the toolbar for the editor tab
	 * @param classDef
	 * @param template
	 */
	private void showEditorToolbar(final RulesTemplate template, final TabItem editor){
		
		toolbar.removeAll();
		
		ToolBarButton btn = new ToolBarButton("Save", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				
				if(!template.check()){
					TbitsInfo.error("Incorrect values! Check the form content!");
					return;
				}
					
				APConstants.apService.saveRule(template.prepareRuleDef(), new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to save the rule!", caught);
					}

					public void onSuccess(Boolean result) {
						TbitsInfo.info("Rule saved.");
					}
				});
			}

		});
		toolbar.add(btn);
		
		btn = new ToolBarButton("Compile", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				
				if(!template.check()){
					TbitsInfo.error("Incorrect values! Check the form content!");
					return;
				}
				
				APConstants.apService.compileRule(template.prepareRuleDef(), new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to compile the code!", caught);
					}

					public void onSuccess(String result) {
						if(result == null)
							result = "Unable to compile the code! \nPlease check the code. Probable reasons :" +
											"\n	* Use full class names (package.class)";
						editor.setData("compilationResult", result.replaceAll("\n", "<br>"));
						if(result.equals(""))
							TbitsInfo.info("Compilation successful!");
						else{
							compilationLog.removeAll();
							compilationLog.add(new Html(result.replaceAll("\n", "<br>")));
							compilationLog.show();
						}
					}
				});
			}
		});
		toolbar.add(btn);
		
		btn = new ToolBarButton("Deploy", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				
				if(!template.check()){
					TbitsInfo.error("Incorrect values! Check the form content!");
					return;
				}
				
				APConstants.apService.deployRule(template.prepareRuleDef(), new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to deploy the rule!", caught);
					}

					public void onSuccess(Boolean result) {
						if(!result){
							TbitsInfo.error("Unable to deploy the rule! Please run <b>Compile</b> and check for compilation errors.");
						}
						else{
							TbitsInfo.info("Your rule has been deployed.");
//							editor.close();
						}
					}
				});
			}
		});
		toolbar.add(btn);
		
		btn = new ToolBarButton("Compilation Log", new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				compilationLog.removeAll();
				String compilationResult = editor.getData("compilationResult");
				if(compilationResult != null)
					compilationLog.add(new Html(compilationResult));
				compilationLog.show();
			}
		});
		toolbar.add(btn);
	}

	//================================================================================

}
