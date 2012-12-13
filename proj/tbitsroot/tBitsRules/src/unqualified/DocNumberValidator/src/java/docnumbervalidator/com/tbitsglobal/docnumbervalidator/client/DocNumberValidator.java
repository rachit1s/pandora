package docnumbervalidator.com.tbitsglobal.docnumbervalidator.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsUncaughtExceptionHandler;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DocNumberValidator implements EntryPoint {
	private static String BULK_UPDATE_CLOSE_CAPTION = "Close and Proceed with DTN";
	
	private DocValidatorServiceAsync docService;
	
	private String sysPrefix;
	
	private String fieldName;
	
	private String fieldDisplayName;
	
	private Window window;
	
	private TextArea textArea;
	
	private Grid<TbitsModelData> grid;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new TbitsUncaughtExceptionHandler());
		
		docService = GWT.create(DocValidatorService.class);
		((ServiceDefTarget)docService).setServiceEntryPoint("/gwtproxy/docdb");
		
		sysPrefix = getSysPrefix();
		fieldName = "SEPCODocumentNumber";
		fieldDisplayName = "SEPCO Number";
		
		com.google.gwt.user.client.ui.Button btn = new com.google.gwt.user.client.ui.Button("Create DTN from document numbers", new ClickHandler(){
			public void onClick(ClickEvent event) {
				if(sysPrefix == null || sysPrefix.trim().equals("")){
					com.google.gwt.user.client.Window.alert("Unable to determine Business Area");
					return;
				}
				init();
			}});
		
		try{
			RootPanel.get("docValidatorButtonHolder").add(btn);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void init(){
		window = new Window();
		window.setWidth(700);
		window.setHeight(400);
		window.setHeading("Document Number Validator");
		
		HBoxLayout layout = new HBoxLayout();
		layout.setHBoxLayoutAlign(HBoxLayoutAlign.STRETCH);
		window.setLayout(layout);
		
		LayoutContainer textAreaContainer = new LayoutContainer(new FitLayout());
		textArea = new TextArea();
		textArea.setWidth(300);
		textAreaContainer.add(textArea, new FitData(5));
		window.add(textAreaContainer, new HBoxLayoutData());
		
		LayoutContainer buttonContainer = new LayoutContainer(new CenterLayout());
		Button button = new Button(" Test >> ", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				onTest();
			}});
		buttonContainer.add(button);
		window.add(buttonContainer, new HBoxLayoutData());
		
		LayoutContainer gridContainer = new LayoutContainer(new FitLayout());
		ListStore<TbitsModelData> store = new ListStore<TbitsModelData>();
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		ColumnConfig documentNumber = new ColumnConfig(fieldName, 200);
		documentNumber.setHeader(fieldDisplayName);
		columns.add(documentNumber);
		ColumnConfig status = new ColumnConfig("status", 115);
		status.setHeader("Found");
		columns.add(status);
		ColumnModel colModel = new ColumnModel(columns);
		grid = new Grid<TbitsModelData>(store, colModel);
		grid.setTrackMouseOver(false);
		gridContainer.add(grid, new FitData(5));
		window.add(gridContainer, new HBoxLayoutData());
		
		window.addButton(new Button("Bulk Update", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				JSONArray json = getJSON();
				window.hide();
				callBulkUpdate(json.toString(), BULK_UPDATE_CLOSE_CAPTION);
			}}));
		
		window.show();
	}
	
	private void onTest(){
		grid.getStore().removeAll();
		grid.getView().refresh(false);
		
		final ArrayList<String> numbers = getNumberListFromString(textArea.getValue());
		ArrayList<String> temp = new ArrayList<String>();
		temp.addAll(numbers);
		for(String number : temp){
			if(!validateNumber(number)){
				numbers.remove(number);

				TbitsModelData data = new TbitsModelData();
				data.set(fieldName, number);
				data.set("status", "INVALID");
			
				grid.getStore().add(data);
			}
		}
		docService.testNumbers(sysPrefix, fieldName, numbers, new AsyncCallback<HashMap<String,Integer>>(){
			public void onFailure(Throwable caught) {
				com.google.gwt.user.client.Window.alert(caught.getMessage());
			}

			public void onSuccess(HashMap<String, Integer> result) {
				for(String number : numbers){
					TbitsModelData data = new TbitsModelData();
					data.set(fieldName, number);
					if(result.containsKey(number)){
						int id = result.get(number);
						if(id  != 0){
							data.set("status", "YES");
							data.set("id", id);
						}else{
							data.set("status", "NO");
							data.set("id", 0);
						}
					}else{
						data.set("status", "NO");
						data.set("id", 0);
					}
					
					grid.getStore().add(data);
				}
			}});
		
		
	}
	
	private String replaceStrangeDash(String str){
        String out = "" ;
        if( null == str ) 
            return out ;
        
        char strangeDash1 = (char)150 ;
        char strangeDash2 = (char)8211 ;
        char validDash = '-' ;
//        String regex = "[" + strangeDash1 + strangeDash2 + "]" ;
//        String replacement = validDash+"" ;
        
        for( int i = 0 ; i < str.length() ; i++ )
        {
            if( str.charAt(i) == strangeDash1 || str.charAt(i) == strangeDash2 )
                out += validDash ;
            else
                out += str.charAt(i) ;
        }
        
        return out ;
    }
	
	private boolean validateNumber(String number){
		return true;
//		return number.matches("^WCG-[0-9]-[A-Z][A-Z][A-Z]-[A-Z][0-9][0-9][0-9]-[A-Z]-[0-9]+$");
	}
	
	private ArrayList<String> getNumberListFromString(String val){
		ArrayList<String> numbers = new ArrayList<String>();
		
		if(val == null)
			return numbers;
		
		String[] arr = val.split("\n");
		for(String number : arr){
			number = number.trim();
			number = this.replaceStrangeDash(number);
			number = number.replace("\n", "");
			number = number.replace("\r", "");
			number = number.replace("\t", "");
			if(!number.equals(""))
				numbers.add(number);
		}
		
		return numbers;
	}
	
	private JSONArray getJSON(){
		JSONArray jsonArr = new JSONArray();
		
		List<TbitsModelData> list = grid.getStore().getModels();
		for(TbitsModelData model : list){
			JSONObject obj = new JSONObject();
			
			JSONString str = new JSONString((String) model.get(fieldName));
			obj.put(fieldName, str);
			
			Object idObj = model.get("id");
			Object statusObj = model.get("status");
			if(idObj != null && idObj instanceof Integer && 
					statusObj != null && statusObj instanceof String && 
					(statusObj.equals("YES") || statusObj.equals("NO"))){
				int id = (Integer) idObj;
				if(id != 0)
					obj.put("id", new JSONString(id + ""));
				else
					obj.put("id", new JSONString("0"));
				
				jsonArr.set(jsonArr.size(), obj);
			}
		}
		return jsonArr;
	}
	
	private native void callBulkUpdate(String json, String bulkUpdateCloseCaption)/*-{
		$wnd.bulkUpdate(json, bulkUpdateCloseCaption);
	}-*/;
	
	private native String getSysPrefix()/*-{
		var sysPrefix = "";
		try{
			sysPrefix = new String($wnd.getValue("sysPrefix"));
		}catch(e){
			sysPrefix = "SEPCO";
		}
		return sysPrefix;
	}-*/;
}
