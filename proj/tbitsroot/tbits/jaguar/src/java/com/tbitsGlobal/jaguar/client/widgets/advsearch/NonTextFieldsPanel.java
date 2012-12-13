package com.tbitsGlobal.jaguar.client.widgets.advsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

public class NonTextFieldsPanel extends AbstractSearchPanel{

	private ComboBox<BAField> fieldCombo;
	private SimpleComboBox<String> valCombo;
	
	private TextArea queryBox;
	
	private final String DATE_FORMAT 	= "dd/MM/yyyy";
	private static final int DAY_INTERVAL = (24*60*60*1000);
	
	private IValueCalculator valueCalculator;
	
	public NonTextFieldsPanel(String sysPrefix) {
		super(sysPrefix);

		fieldCombo = new ComboBox<BAField>();
		fieldCombo.setDisplayField(BAField.DISPLAY_NAME);
		fieldCombo.setFieldLabel("Field");
		fieldCombo.setStore(new ListStore<BAField>());
		
		if(this.sysPrefix.equals(ClientUtils.getSysPrefix())){
			observable.subscribe(OnFieldsReceived.class, new ITbitsEventHandle<OnFieldsReceived>(){
				public void handleEvent(OnFieldsReceived event) {
					FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
					HashMap<String, BAField> fieldMap = cache.getMap();
					
					ListStore<BAField> fieldStore = fieldCombo.getStore();
					fieldStore.removeAll();
					for(BAField field : fieldMap.values()){
						if(!field.isCanViewInBA() || field instanceof BAFieldAttachment || field instanceof BAFieldTextArea)
							continue;
						fieldStore.add(field);
					}
					if(GlobalConstants.isTagsSupported){
						BAField privateTagsField = new BAField();
						privateTagsField.setDisplayName("Private Tag");
						privateTagsField.setName(TagsUtils.PRIVATE_TAGS_FIELD_FILTER);
						fieldStore.add(privateTagsField);
						BAField publicTagsField = new BAField();
						publicTagsField.setDisplayName("Public Tag");
						publicTagsField.setName(TagsUtils.PUBLIC_TAGS_FIELD_FILTER);
						fieldStore.add(publicTagsField);
					}
					
					fieldCombo.setValue(null);
				}
			});
		}
		this.setHeaderVisible(false);
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		queryBox = new TextArea(){
			@Override
			public String getValue() {
				String value = super.getValue();
				if(value == null)
					value = "";
				return value;
			}
		};
		queryBox.setWidth("100%");
		queryBox.setHeight(200);
		queryBox.setValue("");
		queryBox.setStyleAttribute("marginBottom", "5px");
		
		buildPanel();
	}
	
	protected void buildPanel(){		
		ContentPanel fieldsContainer = new ContentPanel();
		fieldsContainer.setStyleAttribute("padding", "5px");
		fieldsContainer.setBodyBorder(false);
		fieldsContainer.setHeaderVisible(false);
		
		ContentPanel fieldsComboContainer = getFieldsComboContainer();
		fieldsContainer.add(fieldsComboContainer);
		
		ContentPanel valComboContainer = new ContentPanel();
		valComboContainer.setBodyBorder(false);
		valComboContainer.setHeaderVisible(false);
		valComboContainer.setStyleAttribute("paddingLeft", "40px");
		fieldsContainer.add(valComboContainer);
		
		ContentPanel selectorContainer = new ContentPanel();
		selectorContainer.setBodyBorder(false);
		selectorContainer.setHeaderVisible(false);
		selectorContainer.setStyleAttribute("paddingLeft", "40px");
		selectorContainer.setStyleAttribute("marginTop", "5px");
		selectorContainer.setStyleAttribute("marginBottom", "5px");
		fieldsContainer.add(selectorContainer);
		
		Button addToQuery = new Button("Add to Query", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(valueCalculator != null){
					String value = valueCalculator.getValue();
					if(value == null)
						return;
					String currentValue = queryBox.getValue();
					if(currentValue == null)
						currentValue = "";
					queryBox.setValue(currentValue + " " + value);
				}
			}});
		fieldsContainer.add(addToQuery);

		this.addFieldComboChangeHandler(valComboContainer, selectorContainer);
		
		ContentPanel opContainer = getOpContainer();
		fieldsContainer.add(opContainer);
		
		fieldsContainer.add(queryBox);
		
		Button reset = new Button("Clear", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				queryBox.setValue("");
			}});
		fieldsContainer.add(reset);
		
		this.add(fieldsContainer);
	}
	
	private void addFieldComboChangeHandler(final ContentPanel valComboContainer, final ContentPanel selectorContainer){
		fieldCombo.addSelectionChangedListener(new SelectionChangedListener<BAField>(){
			@Override
			public void selectionChanged(SelectionChangedEvent<BAField> se) {
				valComboContainer.removeAll();
				selectorContainer.removeAll();
				
				final BAField field = se.getSelectedItem();
				if(field instanceof BAFieldAttachment)
					return;
				
				valCombo = new SimpleComboBox<String>();
				valComboContainer.add(valCombo);
				valComboContainer.layout();
				
				if(field.getName().equals(TagsUtils.PUBLIC_TAGS_FIELD_FILTER)){
					List<String> publicTags = TagsUtils.getExistingTagsBy(TagsUtils.PUBLIC_TAGS_USER);
					for(String tag : publicTags){
						valCombo.add(tag);
					}
					valCombo.setSimpleValue(publicTags.get(0));
					valueCalculator = new IValueCalculator(){
						public String getValue() {
							return field.getName() + ":\"" + valCombo.getValue().getValue() + "\"";
						}};
				}
				else if(field.getName().equals(TagsUtils.PRIVATE_TAGS_FIELD_FILTER)){
					List<String> publicTags = TagsUtils.getExistingTagsBy(ClientUtils.getCurrentUser().getUserId());
					for(String tag : publicTags){
						valCombo.add(tag);
					}
					valCombo.setSimpleValue(publicTags.get(0));
					valueCalculator = new IValueCalculator(){
						public String getValue() {
							return field.getName() + ":\"" + valCombo.getValue().getValue() + "\"";
						}};
				}
				else if(field instanceof BAFieldCheckBox){
					valCombo.add("true");
					valCombo.add("false");
					
					valCombo.setSimpleValue("true");
					
					valueCalculator = new IValueCalculator(){
						public String getValue() {
							return field.getName() + ":" + valCombo.getValue().getValue();
						}};
				}else if(field instanceof BAFieldDate){
					valCombo.add("On");
					valCombo.add("Before");
					valCombo.add("After");
					valCombo.add("Between");
					valCombo.add("Today");
					valCombo.add("Yesterday");
					valCombo.add("Tomorrow");
					
					String[] arr = {"day","week","month","year"};
					for(String s : arr){
						if(s.equals("day"))
							continue;
						valCombo.add("Last " + s);
					}
					
					for(String s : arr){
						if(s.equals("day"))
							continue;
						valCombo.add("Next " + s);
					}
					
					for(String s : arr){
						valCombo.add("Last 'n' " + s + "s");
					}
					
					for(String s : arr){
						valCombo.add("Next 'n' " + s + "s");
					}
					
					valCombo.add("is empty");
					valCombo.add("is not empty");
					
					valCombo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>(){
						@Override
						public void selectionChanged(
								SelectionChangedEvent<SimpleComboValue<String>> se) {
							selectorContainer.removeAll();
							
							final String value = se.getSelectedItem().getValue();
							if(value.equals("On") || value.equals("Before") || value.equals("After")){
								final DateField dateField = new DateField();
								selectorContainer.add(dateField);
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										Date date = dateField.getValue();
										if(date == null)
											return "";
										String dateString = DateTimeFormat.getFormat(DATE_FORMAT).format(date);
										if(value.equals("On")){
											String nextDateString = DateTimeFormat.getFormat(DATE_FORMAT).format(new Date((date.getTime() + DAY_INTERVAL)));
											return field.getName() + ":(>="+dateString+" AND <"+nextDateString+")";
										}
										else if(value.equals("Before"))
											return field.getName() + ":<" + dateString;
										else
											return field.getName() + ":>" + dateString;
									}};
							}else if(value.equals("Between")){
								final DateField from = new DateField();
								selectorContainer.add(from);
								
								LabelField label = new LabelField("And");
								selectorContainer.add(label);
								
								final DateField to = new DateField();
								selectorContainer.add(to);
								
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										String fromDate =  DateTimeFormat.getFormat(DATE_FORMAT).format(from.getValue());
										String toDate =  DateTimeFormat.getFormat(DATE_FORMAT).format(to.getValue());
										return field.getName() + ":(>=" + fromDate + " AND <" + toDate + "+1d)";
									}};
							}else if(value.equals("Today") || value.equals("Yesterday") || value.equals("Tomorrow")){
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										return field.getName() + ":(>=" + value.toLowerCase() + " AND <" + value.toLowerCase() + "+1d)";
									}};
							}else if(!value.contains("'n'") && (value.startsWith("Last") || value.startsWith("Next"))){
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										String[] arr = value.split(" ");
										if(arr.length < 2)
											return null;
										
										String toRet = field.getName() + ":(";
										if(value.startsWith("Last"))
											toRet += "<today AND >=";
										else
											toRet += ">today+1d AND <=";
										toRet += "today";
										if(value.startsWith("Last"))
											toRet += "-";
										else
											toRet += "+";
										if(arr[1].toLowerCase().equals("day"))
											toRet += "1d";
										else if (arr[1].toLowerCase().equals("week"))
											toRet += "7d";
										else if (arr[1].toLowerCase().equals("month"))
											toRet += "1M";
										else if (arr[1].toLowerCase().equals("year"))
											toRet += "1y";
										toRet += ")";
										
										return toRet;
									}};
							}else if(value.contains("'n'")){
								final TextField<String> nValue = new TextField<String>();
								nValue.setEmptyText("n");
								selectorContainer.add(nValue);
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										String[] arr = value.split(" ");
										if(arr.length < 3)
											return null;
										try{
											int n = Integer.parseInt(nValue.getValue());
											String toRet = field.getName() + ":(";
											if(arr[0].toLowerCase().equals("last"))
												toRet += "<today AND >=";
											else
												toRet += ">today+1d AND <=";
											toRet += "today";
											if(arr[0].toLowerCase().equals("last"))
												toRet += "-";
											else
												toRet += "+";
											if(arr[2].toLowerCase().equals("days"))
												toRet += n + "d";
											else if (arr[2].toLowerCase().equals("weeks"))
												toRet += (7*n) + "d";
											else if (arr[2].toLowerCase().equals("months"))
												toRet += n + "M";
											else if (arr[2].toLowerCase().equals("years"))
												toRet += n + "y";
											toRet += ")";
											
											return toRet;
										}catch(Exception e){
											Window.alert("Specify a valid integer value");
											return null;
										}
									}};
							}else if(value.equals("is empty")){
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										return (field.getName() + ":NULL");
									}};
							}else if(value.equals("is not empty")){
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										return ("NOT (" + field.getName() + ":NULL)");
									}};
							}
							selectorContainer.layout();
						}});
					
					valCombo.setSimpleValue("On");
				}else if(field instanceof BAFieldCombo){
					valCombo.add("including");
					valCombo.add("excluding");

					valCombo.setSimpleValue("including");
					
					selectorContainer.removeAll();
					
					List<TypeClient> types = ((BAFieldCombo) field).getTypes();
					ListStore<TypeClient> store = new ListStore<TypeClient>();
					store.add(types);
					
					final ListField<TypeClient> list = new ListField<TypeClient>();
					list.setStore(store);
					list.setDisplayField(TypeClient.DISPLAY_NAME);					
					selectorContainer.add(list);
					
					valueCalculator = new IValueCalculator(){
						public String getValue() {
							String value = valCombo.getValue().getValue();
							List<TypeClient> selected = list.getSelection();
							String query = "";
							for(TypeClient model : selected){
								if(!query.equals(""))
									query += " OR ";
								
								query += "\"" + model.getName() + "\"";
							}
							if(value.equals("including"))
								return field.getName() + ":" + "(" + query + ")";
							else
								return "NOT (" + field.getName() + ":" + "(" + query + "))";
						}};
					
					selectorContainer.layout();
				}else if(field instanceof BAFieldInt){
					valCombo.add("Equals");
					valCombo.add("Not equal to");
					valCombo.add("Greater than");
					valCombo.add("Less than");
					valCombo.add("Greater than or equal to");
					valCombo.add("Less than or equal to");
					valCombo.add("Between");
					valCombo.add("including");
					valCombo.add("excluding");

					valCombo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>(){
						@Override
						public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
							selectorContainer.removeAll();
							
							final String value = se.getSelectedItem().getValue();
							if(value.equals("Between")){
								final TextField<String> from = new TextField<String>();
								selectorContainer.add(from);
								
								LabelField label = new LabelField("And");
								selectorContainer.add(label);
								
								final TextField<String> to = new TextField<String>();
								selectorContainer.add(to);
								
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										String fromVal = from.getValue();
										String toVal = to.getValue();
										return field.getName() + ":(>=" + fromVal + " AND <=" + toVal + ")";
									}};
							}else{
								final TextField<String> nValue = new TextField<String>();
								selectorContainer.add(nValue);
								
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										if(value.equals("including")){
											String values = nValue.getValue();
											if(values != null){
												String[] valuesArr = values.trim().split(",");
												try{
													for(String s : valuesArr){
														Integer.parseInt(s);
													}
												}catch (Exception e){
													Window.alert("Specify a valid integer value");
													return null;
												}
											}
											return field.getName() + ":(" + nValue.getValue() + ")";
										}
										else if(value.equals("excluding")){
											String values = nValue.getValue();
											if(values != null){
												String[] valuesArr = values.trim().split(",");
												try{
													for(String s : valuesArr){
														Integer.parseInt(s);
													}
												}catch (Exception e){
													Window.alert("Specify a valid integer value");
													return null;
												}
											}
											return "NOT (" + field.getName() + ":(" + nValue.getValue() + "))";
										}else {
											if(value.equals("Equals"))
												return field.getName() + ":" + nValue.getValue();
											else if(value.equals("Not equal to"))
												return field.getName() + ":<>" + nValue.getValue();
											else if(value.equals("Greater than"))
												return field.getName() + ":>" + nValue.getValue();
											else if(value.equals("Less than"))
												return field.getName() + ":<" + nValue.getValue();
											else if(value.equals("Greater than or equal to"))
												return field.getName() + ":>=" + nValue.getValue();
											else if(value.equals("Less than or equal to"))
												return field.getName() + ":<=" + nValue.getValue();
//											else if(value.equals("Between"))
//												return field.getName() + ":<=" + nValue.getValue();
										}
										return null;
									}};
							}
							selectorContainer.layout();
						}
					});
					valCombo.setSimpleValue("Equals");
				}else if(field instanceof BAFieldMultiValue){
					valCombo.add("including");
					valCombo.add("excluding");
					valCombo.add("is empty");
					valCombo.add("is not empty");
					valCombo.add("including members of");
					valCombo.add("excluding members of");
					
					valCombo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>(){
						@Override
						public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
							selectorContainer.removeAll();
							
							final String value = se.getSelectedItem().getValue();
							if(value.equals("including") || value.equals("excluding") 
									|| value.equals("including members of") || value.equals("excluding members of")){
							    final UserPicker userField = new UserPicker((BAFieldMultiValue) field);
							    selectorContainer.add(userField);
							    
							    valueCalculator = new IValueCalculator(){
									public String getValue() {
										String v = userField.getStringValue();
										// Tokenise the values on the basis of "," and replace them with " OR " and add quotes to the values
										ArrayList<String> values = new ArrayList<String>();
										while(v.length() > 0){
											int index = v.indexOf(",", 0);
											if(index > 0){
												values.add((v.substring(0, index)).trim());
												v = v.substring(index+1).trim();
											}
										}
										v = "";
										for(String value : values){
											if(!v.equals(""))
												v += " OR ";
											v += "\""+value+"\"";
										}
										
										if(value.equals("including") || value.equals("including members of")){
											return field.getName() + ":(" + v + ")";
										}else if(value.equals("excluding") || value.equals("excluding members of")){
											return "NOT (" + field.getName() + ":(" + v + "))";
										}
										return null;
									}};
							}
							else{
								valueCalculator = new IValueCalculator(){
									public String getValue() {
										if(value.equals("is empty")){
											return  field.getName() + ":NULL";
										}else if(value.equals("is not empty")){
											return "NOT (" + field.getName() + ":NULL)";
										}
										return null;
									}};
							}
							selectorContainer.layout();
						}
					});
					valCombo.setSimpleValue("including");
				}
				else if(field instanceof BAFieldString){
					valCombo.add("Exact phrase");
					valCombo.add("All the words");
					valCombo.add("None of the words");
					valCombo.add("Any of the words");
					
					valCombo.setSimpleValue("Exact phrase");
					
					selectorContainer.removeAll();
					
					final TextArea textValue = new TextArea();
					textValue.setHeight(70);
					textValue.setWidth("100%");
					selectorContainer.add(textValue);
					selectorContainer.layout();
					
					valueCalculator = new IValueCalculator(){
						public String getValue() {
							String value = valCombo.getValue().getValue();
							if(value == null)
								return null;
							
							String query = textValue.getValue();
							if(query == null)
								query = "";
							if(value.equals("Exact phrase")){
								return field.getName() + ":" + "\"" + query + "\"";
							}else if(value.equals("All the words")){
								String[] arr = query.split(" |\\n|\\r|\\t");
								String str = "";
								for(String s : arr){
									if(s.trim().equals(""))
										continue;
									if(!str.equals(""))
										str += " AND ";
									str += "\"" + s + "\"";
								}
								return field.getName() + ":" + "(" + str + ")";
							}else if(value.equals("None of the words")){
								String[] arr = query.split(" |\\n|\\r|\\t");
								String str = "";
								for(String s : arr){
									if(s.trim().equals(""))
										continue;
									if(!str.equals(""))
										str += " OR ";
									str += "\"" + s + "\"";
								}
								return "NOT (" + field.getName() + ":" + "(" + str + "))";
							}else if(value.equals("Any of the words")){
								String[] arr = query.split(" |\\n|\\r|\\t");
								String str = "";
								for(String s : arr){
									if(s.trim().equals(""))
										continue;
									if(!str.equals(""))
										str += " OR ";
									str += "\"" + s + "\"";
								}
								return field.getName() + ":" + "(" + str + ")";
							}
							
							return null;
						}};
				}
			}});
	}
	
	private ContentPanel getFieldsComboContainer(){
		ContentPanel fieldsComboContainer = new ContentPanel();
		fieldsComboContainer.setBodyBorder(false);
		fieldsComboContainer.setHeaderVisible(false);
		
		FormLayout layout = new FormLayout();
		layout.setLabelWidth(35);
		fieldsComboContainer.setLayout(layout);
		
		fieldsComboContainer.add(fieldCombo, new FormData());
		
		return fieldsComboContainer;
	}
	
	private ContentPanel getOpContainer(){
		ContentPanel opContainer = new ContentPanel();
		opContainer.setStyleAttribute("marginTop", "5px");
		opContainer.setStyleAttribute("marginBottom", "5px");
		opContainer.setBodyBorder(false);
		opContainer.setHeaderVisible(false);
		
		ColumnLayout layout = new ColumnLayout();
		opContainer.setLayout(layout);
		
		Button and = new Button("AND", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				queryBox.setValue(queryBox.getValue() + " AND");
			}});
		opContainer.add(and);
		
		Button or = new Button("OR", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				queryBox.setValue(queryBox.getValue() + " OR");
			}});
		or.setStyleAttribute("marginLeft", "5px");
		opContainer.add(or);
		
		Button not = new Button("NOT", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				queryBox.setValue(queryBox.getValue() + " NOT");
			}});
		not.setStyleAttribute("marginLeft", "5px");
		opContainer.add(not);
		
		Button pOpen = new Button("(", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				queryBox.setValue(queryBox.getValue() + " (");
			}});
		pOpen.setStyleAttribute("marginLeft", "5px");
		opContainer.add(pOpen);
		
		Button pClose = new Button(")", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				queryBox.setValue(queryBox.getValue() + ")");
			}});
		pClose.setStyleAttribute("marginLeft", "5px");
		opContainer.add(pClose);
		
		return opContainer;
	}

	protected DQL getDQL() {
		return new DQL(queryBox.getValue());
	}

	protected void saveSearch(Dialog dialog, String searchName,
			HashMap<String, String> params) {
		// TODO Auto-generated method stub
		
	}
}
