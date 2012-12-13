package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.Date;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.services.JobActionService;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;


public class CronExpressionForm extends FormPanel{
	public static final String DAY_OF_THE_MONTH = "day of the month";
	public static final String DAY_OF_THE_WEEK = "day of the week";
	public static String repeats[] = new String[] {"Does not repeat",
									"Every n minutes",
									"Every n hour",
									"Daily", 
									"Every Weekday (Mon-Fri)",
									"Every Mon, Wed, Fri",
									"Every Tue, Thu, Sat", 
									"Weekly", 
									"Monthly",
									"Yearly"};
	
	//handlers defined in defineHandlers()
	private SelectionChangedListener<SimpleComboValue<String>> repeatComboListener;
	private Listener<BaseEvent> listener;
	private SelectionChangedListener<Time> timeChangeHandler;
	private SelectionChangedListener<SimpleComboValue<Integer>> intervalChangeHandler;
	
	//ui components
	protected SimpleComboBox<String> repeatCombo 	= new SimpleComboBox<String>();
	protected DateField startDateField 				= new DateField();
	protected TimeField startTimeField 				= new TimeField();
	protected DateField stopDateField	 			= new DateField();
	protected TimeField stopTimeField 				= new TimeField();
	protected SimpleComboBox<Integer> interval 		= new SimpleComboBox<Integer>();
	protected CheckBox[] dayOfWeek 					= new CheckBox[7];
	protected RadioGroup mRadio						= new RadioGroup("Repeat by"); 
	protected Radio rbMonthly1 						= new Radio();
	protected Radio rbMonthly2 						= new Radio();
	protected TextArea cronEvaluationField 		= new TextArea();
	protected TextField<String> cronField 		= new TextField<String>();
	
	//Containers
	private AdapterField l[] = new AdapterField[3];
	
	
	
	protected String mode;
	boolean disableHandler = true;
	
	String cronString;
	
	Date startDate , stopDate;
	Time startTime, stopTime;
	
	
	public CronExpressionForm(String mode){
		this.mode = mode;
		setBodyBorder(false);
		defineHandlers();		
	}
	
	private void defineHandlers(){
	
		timeChangeHandler = new SelectionChangedListener<Time>(){
			public void selectionChanged(SelectionChangedEvent<Time> se) {
				cronField.setValue(calculateCron());
			}};
		listener = new Listener<BaseEvent>(){
			public void handleEvent(BaseEvent be) {
				cronField.setValue(calculateCron());
			}};
		
		intervalChangeHandler = new SelectionChangedListener<SimpleComboValue<Integer>>(){
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<Integer>> se) {
				cronField.setValue(calculateCron());
			}};
			
		repeatComboListener = new SelectionChangedListener<SimpleComboValue<String>>(){public void selectionChanged(
							SelectionChangedEvent<SimpleComboValue<String>> se) {
							((SimpleComboBox)se.getSource()).collapse();
							showSubOptions(repeatCombo.getSelectedIndex());
							cronField.setValue(calculateCron());
							}
				};
		
		
	}
	public void attachHandlers(){
		repeatCombo.addSelectionChangedListener(repeatComboListener);
		startTimeField.addSelectionChangedListener(timeChangeHandler);
		startDateField.getDatePicker().addListener(Events.Select, listener);
		stopTimeField.addSelectionChangedListener(timeChangeHandler);
		stopDateField.getDatePicker().addListener(Events.Select, listener);
		interval.addSelectionChangedListener(intervalChangeHandler);
		rbMonthly1.addListener(Events.OnClick, listener);
		rbMonthly2.addListener(Events.OnClick, listener);
		for(int i=0; i<7; i++){
			dayOfWeek[i].addListener(Events.OnClick, listener);
		}
	}
	public void detachHandlers(){
		repeatCombo.removeSelectionListener(repeatComboListener);
		startTimeField.removeSelectionListener(timeChangeHandler);
		startDateField.getDatePicker().removeListener(Events.Select, listener);
		stopTimeField.removeSelectionListener(timeChangeHandler);
		stopDateField.getDatePicker().removeListener(Events.Select, listener);
		interval.removeSelectionListener(intervalChangeHandler);
		rbMonthly1.removeListener(Events.OnClick, listener);
		rbMonthly2.removeListener(Events.OnClick, listener);
		for(int i=0; i<7; i++){
			dayOfWeek[i].removeListener(Events.OnClick, listener);
		}
	}
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		setWidth(700);
		setBodyBorder(false);
		setHeading("Enter Job Schedule");
		
		setValueInRepeatCombo();
		l[0] = new AdapterField(makeInterval());
		l[0].setFieldLabel("Repeat in Every");
		l[1] = new AdapterField(makeCheckBoxes());
		l[1].setFieldLabel("Select Day(s)");
		l[2] = new AdapterField(makeRadioButtons());
		l[2].setFieldLabel("Select Day mode");

		startDateField.setFieldLabel("Start Date");
		startDateField.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/y"));
		
		startTimeField.setFieldLabel("Start Time");
		
		stopDateField.setFieldLabel("Stop Date");
		stopDateField.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/y"));
		
		stopTimeField.setFieldLabel("Stop Time");
		
		repeatCombo.setFieldLabel("Repeat");
		
		cronField.setFieldLabel("Cron Expression");
		
		cronEvaluationField.setFieldLabel("Evaluation");
		cronEvaluationField.setHeight(200);
		
		
		add(startDateField , new FormData("100%"));
		add(startTimeField , new FormData("100%"));
		add(stopDateField , new FormData("100%"));
		add(stopTimeField , new FormData("100%"));
		add(repeatCombo , new FormData("100%"));
		
		add(l[0], new FormData("100%"));
		add(l[1], new FormData("100%"));
		add(l[2], new FormData("100%"));
		l[0].hide();l[1].hide();l[2].hide();
		
		ButtonBar buttonBar = new ButtonBar();
		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		buttonBar.add(new ToolBarButton("Calculate Cron", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				cronField.setValue(calculateCron());
				evaluateCron(cronField.getValue());
			}
		}));
		add(buttonBar);
		KeyListener keyListener = new KeyListener() {
		      public void componentKeyUp(ComponentEvent event) {
		    	  if(event.getKeyCode() == KeyCodes.KEY_ENTER)
		       evaluateCron(cronField.getValue());
		      }

		    };
		cronField.addKeyListener(keyListener);
		add(cronField,new FormData("100%"));
		add(cronEvaluationField,new FormData("100%"));
		
		
		
		if(mode == JobActionService.CREATE_JOB){
			startDateField.setValue(new Date());
			stopDateField.setValue(new DateWrapper().addYears(10).asDate());
			DateWrapper wrap = new DateWrapper();
			wrap = wrap.clearTime();
			wrap.addHours(10);
			startTimeField.setDateValue(wrap.asDate());
			stopTimeField.setDateValue(wrap.asDate());
			repeatCombo.setValue(repeatCombo.getStore().getAt(0));
			attachHandlers();
		}
	}
	
	private void setValueInRepeatCombo(){
		for(String s: repeats){
			repeatCombo.add(s);
		}
		repeatCombo.setEditable(false);
	}
	private HorizontalPanel makeInterval(){
		HorizontalPanel intervalPanel = new HorizontalPanel();
		for(int i=1; i<31; i++){
	    	interval.add(i);
	    }
		interval.setEditable(false);
		interval.setValue(interval.getStore().getAt(0));
		intervalPanel.add(interval);
//		AdapterField intervalField = new AdapterField(intervalPanel);
//		intervalField.setFieldLabel("Repeat Every");
		return intervalPanel;
	}
	private HorizontalPanel makeCheckBoxes(){
		HorizontalPanel checkBoxPanel = new HorizontalPanel();
		checkBoxPanel.setSpacing(10);
		for(int i=0; i<7; i++){
			dayOfWeek[i] = new CheckBox();
		}
		dayOfWeek[0].setName("SUN");
		dayOfWeek[0].setBoxLabel("S");
		dayOfWeek[1].setName("MON");
		dayOfWeek[1].setBoxLabel("M");
		dayOfWeek[2].setName("TUE");
		dayOfWeek[2].setBoxLabel("T");
		dayOfWeek[3].setName("WED");
		dayOfWeek[3].setBoxLabel("W");
		dayOfWeek[4].setName("THU");
		dayOfWeek[4].setBoxLabel("T");
		dayOfWeek[5].setName("FRI");
		dayOfWeek[5].setBoxLabel("F");
		dayOfWeek[6].setName("SAT");
		dayOfWeek[6].setBoxLabel("S");
		for(int i=0; i<7; i++){
			checkBoxPanel.add(dayOfWeek[i]);
		}
		return checkBoxPanel;
	}
	private HorizontalPanel makeRadioButtons(){
		HorizontalPanel radioPanel = new HorizontalPanel();
		radioPanel.setSpacing(10);
		rbMonthly1 = new Radio();
		rbMonthly1.setBoxLabel(DAY_OF_THE_MONTH);
		rbMonthly1.setValue(true);
		
		rbMonthly2 = new Radio();
		rbMonthly2.setBoxLabel(DAY_OF_THE_WEEK);
		
		mRadio.add(rbMonthly1);
		mRadio.add(rbMonthly2);
		radioPanel.add(mRadio);
		return radioPanel; 
	}
	public void showSubOptions(int repeatIndex){
		switch(repeatIndex){
		case 0 : l[0].hide();l[1].hide();l[2].hide();break;
		case 1 : l[0].show();l[1].hide();l[2].hide();break;
		case 2 : l[0].show();l[1].hide();l[2].hide();break;
		case 3 : l[0].show();l[1].hide();l[2].hide();break;
		case 4 : l[0].hide();l[1].hide();l[2].hide();break;
		case 5 : l[0].hide();l[1].hide();l[2].hide();break;
		case 6 : l[0].hide();l[1].hide();l[2].hide();break;
		case 7 : l[0].hide();l[1].show();l[2].hide();break;
		case 8 : l[0].show();l[1].hide();l[2].show();break;
		case 9 : l[0].show();l[1].hide();l[2].hide();break;
		}
	}
	/*
	 * method called in edit mode
	 * values extracted from cron expression
	 * and set into fields 
	 */
	public void setField(JobDetailClient jobDetail){
		
		detachHandlers();
		
		String cron = jobDetail.getCronExpression();
		cronField.setValue(cron);
		if(jobDetail.getEndDate() != null){
			startDateField.setValue(jobDetail.getStartDate());
			startTimeField.setDateValue(jobDetail.getStartDate());
		}
		else{
			startDateField.setValue(new Date());
			startTimeField.setDateValue(new Date());
		}
		
		if(jobDetail.getEndDate() != null){
			stopDateField.setValue(jobDetail.getEndDate());
			stopTimeField.setDateValue(jobDetail.getEndDate());
		}
		else{
			stopDateField.setValue(new DateWrapper().addYears(10).asDate());
			stopTimeField.setDateValue(new DateWrapper().addYears(10).asDate());
		}
		int repeatIndex = findRepeatIndex(cron);
		if(repeatIndex != -1){
			SimpleComboValue<String> val = repeatCombo.getStore().getAt(repeatIndex);
			repeatCombo.setValue(val);
			showSubOptions(repeatIndex);
		}
		if( ! calculateCron().equalsIgnoreCase(cron)){
			TbitsInfo.warn("Unable to set values in Fields...Displaying Default values");
		}
		
		attachHandlers();
	}
	
	/* this  method calculate the repeat index
	 * and also set values of sub-option of
	 * the calculated repeat option
	 */
	public int findRepeatIndex(String cron){
		int rIndex = 0;
		String ca[] = cron.split(" ");
		if(cron.contains("/")){
			for(int i = 0; i < ca.length ; i++){
				if(ca[i].contains("/")){
					rIndex = i == 1 ? 1 :
						     i == 2 ? 2 :
							 i == 3 ? 3 :
							 i == 4 ? 8 :
							 i == 6 ? 9 : -1;
					int ii = Integer.parseInt(ca[i].substring(ca[i].indexOf("/")+1));
					interval.setValue(interval.getStore().getAt(ii-1));
					return rIndex;
				}
			}
		}
		else if(!ca[5].equals("?")){
			if(ca[5].contains("#")){
				rIndex = 8;
				rbMonthly2.setValue(true);
			}
			else if(ca[5].contains("TUE,THU,SAT")){
				rIndex = 6;
			}
			else if(ca[5].contains("MON,WED,FRI")){
				rIndex = 5;
			}
			else if(ca[5].contains("MON-FRI")){
				rIndex = 4;
			}
			else{
				rIndex = 7;
				if(ca[5].contains("SUN"))
					dayOfWeek[0].setValue(true);
				if(ca[5].contains("MON"))
					dayOfWeek[1].setValue(true);
				if(ca[5].contains("TUE"))
					dayOfWeek[2].setValue(true);
				if(ca[5].contains("WED"))
					dayOfWeek[3].setValue(true);
				if(ca[5].contains("THU"))
					dayOfWeek[4].setValue(true);
				if(ca[5].contains("FRI"))
					dayOfWeek[5].setValue(true);
				if(ca[5].contains("SAT"))
					dayOfWeek[6].setValue(true);
			}
		}
		return rIndex;
	}
	
	public String getCronExpression(){
		return cronField.getValue();
		
	}
	public Date getEndDate(){
		if(stopDateField.getValue() == null)
			return null;
		if(stopTimeField.getValue() == null)
			return null;
		DateWrapper date = new DateWrapper(stopDateField.getValue());
		Time time = stopTimeField.getValue();
		date.clearTime();
		date.addMinutes(time.getMinutes());
		date.addHours(time.getHour());
		return date.asDate();
	}
	public Date getStartDate(){
		if(startDateField.getValue() == null)
			return null;
		if(startTimeField.getValue() == null)
			return null;
		DateWrapper date = new DateWrapper(startDateField.getValue());
		Time time = startTimeField.getValue();
//		date.clearTime();
		
		date.addMinutes(time.getMinutes());
		date.addHours(time.getHour());
		//TbitsInfo.warn(date.toString());
		return date.asDate();
	}
	
	
	public String calculateCron(){
//		if(disableHandler)
//			return;
		String[] cronArr = new String[7];
		String CRON = "";
		
		DateWrapper startDate = new DateWrapper(startDateField.getValue());
		Time startTime = startTimeField.getValue();
		
		cronArr[0] = "0";  //onTime.getSeconds() + "";
		cronArr[1] = startTime.getMinutes() + "";
		cronArr[2] = startTime.getHour() + "";
		cronArr[3] = startDate.getDate() + "";
		cronArr[4] = (startDate.getMonth() + 1) + "";
		cronArr[6] = startDate.getFullYear() + "";
		cronArr[5] = "?";
				
		int n = 0;
		boolean gotOne;
		
		switch(repeatCombo.getSelectedIndex()){
			case 0:
				
				cronArr[0] = "0";  //onTime.getSeconds() + "";
				cronArr[1] = startTime.getMinutes() + "";
				cronArr[2] = startTime.getHour() + "";
				cronArr[3] = startDate.getDate() + "";
				cronArr[4] = (startDate.getMonth() + 1) + "";
				cronArr[5] = "?";
				cronArr[6] = (startDate.getFullYear()) + "";

				break;
			case 1:														//every n minutes
				n = interval.getSelectedIndex() + 1;
				cronArr[1] += "/" + n;
				cronArr[2] = "*";
				break;
			case 2:														//every n hours
				n = interval.getSelectedIndex() + 1;
				cronArr[2]+= "/" + n;
				cronArr[3] = "*";
				break;
			case 3:														//daily
				n = interval.getSelectedIndex() + 1;
				cronArr[3] += "/" + n;
				cronArr[4] = "*";
				cronArr[6] = "";
				break;
			case 4:														//weekdays
				cronArr[3] = "?";
				cronArr[4] = "*";
				cronArr[5] = "MON-FRI";
				cronArr[6] = "";
				break;
			case 5:														//mwf
				cronArr[3] = "?";
				cronArr[4] = "*";
				cronArr[5] = "MON,WED,FRI";
				cronArr[6] = "";
				break;
			case 6:														//tts
				cronArr[3] = "?";
				cronArr[4] = "*";
				cronArr[5] = "TUE,THU,SAT";
				cronArr[6] = "";
				break;
			case 7:
				gotOne = false;											//weekly
				n = interval.getSelectedIndex() + 1;
				cronArr[3] = "?";
				cronArr[4] = "*";
				cronArr[5] = "";
				for(int i = 0; i < 7; i++){
					if(dayOfWeek[i].getValue() == true){
						cronArr[5] += dayOfWeek[i].getName() + ",";
						gotOne = true;
					}
				}
				if(gotOne)
					cronArr[5] = cronArr[5].substring(0, cronArr[5].length() - 1);
				else{
					dayOfWeek[startDate.getDay()].setValue(true);
					cronArr[5] = dayOfWeek[startDate.getDay()].getName();
				}
				cronArr[6] = "";
				break;
			case 8:														//monthly
				gotOne = false;
				n = interval.getSelectedIndex() + 1;
//				cronArr[3] = startDate.getDate() + "";
				cronArr[4] += "/" + n;
				cronArr[5] = "?";
				if(rbMonthly2.getValue() == true){
					float x = startDate.getDate()/7;
					for(int i = 0; i<5; i++){
						if(i < x) continue;
						cronArr[5] = startDate.getDay() + 1 + "#" + (i + 1);
						cronArr[3] = "?";
						break;
					}
				}
				cronArr[6] = "";
				break;
			case 9:													//yearly
				n = interval.getSelectedIndex() + 1;
//				cronArr[3] = startDate.getDate() + "";
				//cronArr[4] += "*";
//				cronArr[5] = "?";
				cronArr[6] += "/" + n;
				break;
		}
		for(int i = 0; i<7; i++){
			CRON += cronArr[i] + " ";
		}
		return CRON;		
	}
	public void evaluateCron(String CRON){
		APConstants.apService.getNextExecutions(CRON, getStartDate(),getEndDate(),10, new AsyncCallback<ArrayList<Date>>(){
			public void onFailure(Throwable caught) {
				cronEvaluationField.setValue(caught.getMessage());
			}

			public void onSuccess(ArrayList<Date> result) {
				if(result == null || result.isEmpty()){
					cronEvaluationField.setRawValue("No future schedule");
					return;
				}
				cronEvaluationField.setRawValue("Next triggers of this job are-\n");
				for(Date d : result){
					cronEvaluationField.setRawValue(cronEvaluationField.getRawValue() +  "\n" + d);
				}
			}
			
		});
	}
	
}



//TextField<String>  secField			= new TextField<String>();
//TextField<String>  minField			= new TextField<String>();
//TextField<String>  hrsField			= new TextField<String>();
//TextField<String>  dayOfMonthField	= new TextField<String>();
//TextField<String>  monthField		= new TextField<String>();
//TextField<String>  dayOfWeekField	= new TextField<String>();
//TextField<String>  yearField		= new TextField<String>();

//FieldSet fs = new FieldSet();
//fs.setCheckboxToggle(true);
//fs.setHeading("Enter Cron Field(for advance users)");
//fs.setLayout(new FormLayout());
//secField.setFieldLabel("sec");
//fs.add(secField , new FormData("100%"));
//minField.setFieldLabel("min");
//fs.add(minField, new FormData("100%"));
//hrsField.setFieldLabel("hours");
//fs.add(hrsField , new FormData("100%"));
//dayOfMonthField.setFieldLabel("day of month");
//fs.add(dayOfMonthField , new FormData("100%"));
//monthField.setFieldLabel("month");
//fs.add(monthField , new FormData("100%"));
//dayOfWeekField.setFieldLabel("day of week");
//fs.add(dayOfWeekField , new FormData("100%"));
//yearField.setFieldLabel("year");
//fs.add(yearField , new FormData("100%"));
//final TextArea res = new TextArea();
//res.setFieldLabel("Errors");
//res.setEnabled(false);
//fs.add(res,new FormData("100%"));
//Button b = new Button("validate");
//b.addSelectionListener(new SelectionListener<ButtonEvent>(){
//	public void componentSelected(ButtonEvent ce) {
//	}
//});
//fs.add(b);
//Margins m = new Margins(15,0,15,0);
//FormData formData = new FormData("100%");
//formData.setMargins(m);
//add(fs, formData);
//fs.collapse();




////////////
//int next = 0;
//if(startTime.getMinutes() <= stopTime.getMinutes()){
//	cronArr[1] = startTime.getMinutes() + "-" + stopTime.getMinutes();
//	next = 0;
//}
//else{
//	next = 1;
//	cronArr[1] = startTime.getMinutes() + "";
//}
//
//if(startTime.getHour() <= (stopTime.getHour() - next)){
//	cronArr[2] = startTime.getHour() + "-" + (stopTime.getHour() - next);
//	next = 0;
//}
//else{
//	next = 1;
//	cronArr[2] = startTime.getHour() + "";
//}
//
//
//if(startDate.getDate() <= stopDate.getDate() - next){
//	cronArr[3] = startDate.getDate() + "-" + (stopDate.getDate() - next);
//	next = 0;
//}
//else{
//	next = 1;
//	cronArr[3] = startDate.getDate() + "";
//}
//if(startDate.getMonth() <= stopDate.getMonth() - next){
//	cronArr[4] = (startDate.getMonth() + 1) + "-" + (stopDate.getMonth() + 1 - next);
//	next = 0 ;
//}
//else{
//	next = 1;
//	cronArr[4] = (startDate.getMonth() + 1) + "";
//}
//if(startDate.getFullYear() <= stopDate.getFullYear() - next){
//	cronArr[6] = startDate.getFullYear() + "-" + (stopDate.getFullYear() - next);
//	next = 0;
//}
//else{
//	next = 1;
//	cronArr[6] = startDate.getFullYear() + "";
//}
