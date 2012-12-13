package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel.IValueCalculator;

/**
 * 
 * @author sutta
 * 
 * Box to search on date fields
 */
public class DateSearchBox extends LayoutContainer implements ISearchBox {
	
	private final String DATE_FORMAT 	= "dd/MM/yyyy";
	private static final int DAY_INTERVAL = (24*60*60*1000);
	
	private List<BAField> fields;
	
	private HashMap<String, IValueCalculator> valueCalculators;

	// constructor
	public DateSearchBox(List<BAField> fields) {
		super();
		
		this.setStyleAttribute("padding", "5px");
		this.setStyleAttribute("borderBottom", "2px solid #99BBE8");
		
		this.fields = fields;
	}

	
	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		resetToDefault();
	 }

	
	private void createField(BAFieldDate baField) {
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(baField.getDisplayName());
        
        SimpleComboBox<String> combo = new SimpleComboBox<String>();
        combo.setStyleAttribute("marginBottom", "5px;");
        addElementsComboBox(combo);
        
		fieldSet.add(combo);
		this.add(fieldSet);
		
		addComboChangeHandler(baField, combo, fieldSet);
	}
		
	
	// Adding Elements to SimpleComboBox
	private void addElementsComboBox(SimpleComboBox<String> combo) {
		combo.add("On");
		combo.add("Before");
		combo.add("After");
		combo.add("Between");
		combo.add("Today");
		combo.add("Yesterday");
		combo.add("Tomorrow");

		String[] arr = { "day", "week", "month", "year" };
		for (String s : arr) {
			if (s.equals("day "))
				continue;
			combo.add("Last " + s);
		}

		for (String s : arr) {
			if (s.equals("day"))
				continue;
			combo.add("Next " + s);
		}

		for (String s : arr) {
			combo.add("Last 'n' " + s + "s");
		}

		for (String s : arr) {
			combo.add("Next 'n' " + s + "s");
		}

		combo.add("is empty");
		combo.add("is not empty");
	}

	
	// Changing Acc. to Selected Item
	private void addComboChangeHandler(final BAFieldDate baField, final SimpleComboBox<String> combo,final LayoutContainer fieldSet) {
		 final LayoutContainer datecontainer= new LayoutContainer();	   	    	  
		 combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
			   public void selectionChanged(final SelectionChangedEvent<SimpleComboValue<String>> se) {
				   final String value = se.getSelectedItem().getValue();
				   IValueCalculator valCal = null;
				   datecontainer.removeAll();
	
				   if (value.equals("On") || value.equals("Before") || value.equals("After")) {
	                   final DateField on = new DateField();           
	                   datecontainer.add(on);
	                   fieldSet.add(datecontainer);
			           fieldSet.layout();
			           valCal = new IValueCalculator(){
							public String getValue() {
								if(on.getValue() == null)
									return null;
								String onDate = DateTimeFormat.getFormat(DATE_FORMAT).format(on.getValue());
								if(value.equals("On")){
									String nextDate = DateTimeFormat.getFormat(DATE_FORMAT).format(new Date((on.getValue().getTime() + DAY_INTERVAL)));
									return (baField.getName() + ":(>="+onDate+" AND <"+nextDate+")");
								}else if(value.equals("Before"))
									return(baField.getName() + ":<" + onDate);
				                else 
				                	return(baField.getName() + ":>" + onDate);
				          	}
					   };
	  				}
	  				
				   else if(value.equals("Between")){
				    	final DateField from = new DateField();
					    datecontainer.add(from);
					    LabelField label = new LabelField("And");
	                    datecontainer.add(label);
					    final DateField to = new DateField();
			            datecontainer.add(to);       
				        fieldSet.add(datecontainer);
					    fieldSet.layout();
					    valCal = new IValueCalculator(){
					    	public String getValue() {
					    		if(from.getValue() == null || to.getValue() == null)
									return null;
					    		String fromDate = DateTimeFormat.getFormat(DATE_FORMAT).format(from.getValue());		
  							    String toDate = DateTimeFormat.getFormat(DATE_FORMAT).format(new Date(to.getValue().getTime()+DAY_INTERVAL));
	    						return (baField.getName() + ":(>=" + fromDate + " AND <" + toDate + "+1d)");
						    }
					    };
				    }
				
			   	   else  if(value.equals("Today") || value.equals("Yesterday") || value.equals("Tomorrow")){
				    	valCal = new IValueCalculator(){
				    		public String getValue() {
								return (baField.getName() + ":(>=" + value.toLowerCase() + " AND <" + value.toLowerCase() + "+1d)");
						    }
						};
				    }
				
				   else if(!value.contains("'n'") && (value.startsWith("Last") || value.startsWith("Next"))){
				        valCal = new IValueCalculator(){
							public String getValue() {
								String[] arr = value.split(" ");
								if(arr.length < 2)
									return null;	
								String toRet = baField.getName() + ":(";
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
							}
						};
				    }
				
				   else if(value.contains("'n'")){
				    	final TextField<String> nValue = new TextField<String>();
					    datecontainer.add(nValue);
					    fieldSet.add(datecontainer);
					    fieldSet.layout();
					    valCal = new IValueCalculator(){
							public String getValue() {
								if(nValue.getValue() == null)
									return null;
								String[] arr = value.split(" ");	
	               		        String value= nValue.getValue();
	               		        
	               		     String toRet = baField.getName() + ":(";
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
									toRet += value + "d";
								else if (arr[2].toLowerCase().equals("weeks"))
									toRet += (7*Integer.parseInt(value)) + "d";
								else if (arr[2].toLowerCase().equals("months"))
									toRet += value + "M";
								else if (arr[2].toLowerCase().equals("years"))
									toRet += value + "y";
								toRet += ")";
								
								return toRet;
							}
	               	    };
	                }
		           else if(value.equals("is empty")){
		            	valCal = new IValueCalculator(){
							public String getValue() {
								return (baField.getName() + ":NULL");
							}
						};
				    }
				
			       else if(value.equals("is not empty")){
				       	valCal = new IValueCalculator(){
							public String getValue() {
								return ("NOT (" + baField.getName() + ":NULL)");
							}
						};
				   }
				   
				   
				   if(valCal != null)
					   valueCalculators.put(baField.getName(), valCal);
			}
		});
	}


	public HashMap<String, List<String>> getSearchParams(SearchParamType spt) {
		HashMap<String, List<String>> searchParams = new HashMap<String, List<String>>();
	    if(spt.equals(SearchParamType.NON_TEXT)){
			for(BAField  field : fields){
			    if(field instanceof BAFieldDate){
			    	ArrayList<String> params = new ArrayList<String>();
			    	IValueCalculator calculator = valueCalculators.get(field.getName());
			   	    if(calculator != null && calculator.getValue() != null)
			   	    	params.add(calculator.getValue()); 
			   	    searchParams.put(field.getName(), params);
			    }
			}
	    }
       return searchParams; 
	}


	public HashMap<SearchParamType, String> getDQL() {
		String dql = "";
		for(String key : valueCalculators.keySet()){
			IValueCalculator calculator = valueCalculators.get(key);
	   	    if(calculator != null && calculator.getValue() != null){
	   	    	if(!dql.equals(""))
	   	    		dql += " OR ";
	   	    	dql += calculator.getValue(); 
	   	    }
		}
		
		HashMap<SearchParamType, String> dqlMap = new HashMap<SearchParamType, String>();
		dqlMap.put(SearchParamType.NON_TEXT, dql);
		return dqlMap;
	}
	
	public void resetToDefault() {
		valueCalculators = new HashMap<String, IValueCalculator>();
		
		this.removeAll();
		
		for(BAField baField : fields){
			if(baField instanceof BAFieldDate && baField.isCanViewInBA() && baField.isCanSearch()){
				createField((BAFieldDate) baField);
 			}
		 }
		
		this.layout();
	}
}
