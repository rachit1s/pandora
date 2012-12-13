package com.tbitsGlobal.jaguar.client.widgets.advsearch;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;

public class TextParamsPanel extends AbstractSearchPanel{

	private ComboBox<BAField> fieldCombo;
	private SimpleComboBox<String> valCombo;
	
	private TextArea queryBox;
	
	private IValueCalculator valueCalculator;
	
	public TextParamsPanel(String sysPrefix) {
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
						if(!field.isCanViewInBA())
							continue;
						if(!(field instanceof BAFieldTextArea))
							continue;
						fieldStore.add(field);
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
				
//				if(field instanceof BAFieldTextArea){
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
//				}
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
