package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Pattern;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.IWizardPage;

public class ILWizard extends AbstractWizard {

	private HashMap<String, String> contentStrings;
	
	private ILWizard() {
		super();

		this.setHeading("Invitation Letter Wizard");
		
		ILConstants.employees = new HashMap<Integer, TbitsTreeRequestData>();
		this.addBackButton();
		this.addNextButton();
		this.addFinishButton();
		this.addPreviewPDFButton();
		this.addPreviewDOCButton();
		
		ILConstants.dbService.getTextStrings(ClientUtils.getSysPrefix(), new AsyncCallback<HashMap<String, String>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to get Content //", caught);
				Log.error("Unable to get Content Strings", caught);
			}

			public void onSuccess(HashMap<String, String> result) {
				contentStrings = result;
			}});
	}

	public ILWizard(String idString) {
		this();
		String[] ids = idString.split(",");
		ArrayList<Integer> requestIds = new ArrayList<Integer>();
		for (String id : ids) {
			try {
				int requestId = Integer.parseInt(id.trim());
				requestIds.add(requestId);
			} catch (Exception e) {
				Log.error("Error processing tBits Ids. See logs for details....", e);
				TbitsInfo.error("Error processing tBits Ids. See logs for details....", e);	
			}
		}
		processIds(requestIds);
	}

	public ILWizard(ArrayList<Integer> requestIds) {
		this();
		processIds(requestIds);
	}

	protected void onSubmit() {
		final HashMap<String, String> paramTable = getParamTable();
		String inviType = paramTable.get(IFixedFields.REQUEST_TYPE);
		ILConstants.dbService.verifyEmployees(ClientUtils.getSysPrefix(), inviType, ILConstants.employees,
				new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						TbitsInfo.error("unable to verify selected employees", caught);
					}

					public void onSuccess(String result) {
						if (result.equals("OK")) {
							createTransmittal(paramTable);
						} else
							Window.alert(result);
					}
				});
	}

	private void createTransmittal(HashMap<String, String> paramTable) {
		ArrayList<String[]> schedule = getSchedule();

		final MessageBox messageBox = MessageBox.wait("Please Wait",
				"Invitation Letter is being created", "Please Wait...");

		ILConstants.dbService.createTransmittal(ClientUtils.getSysPrefix(), ILConstants.employees,
				schedule, paramTable, new AsyncCallback<Boolean>() {
					public void onFailure(Throwable caught) {
						messageBox.close();
						Log.error("Error in creating transmittal. See logs for details....", caught);
						TbitsInfo.error("Error in creating transmittal. See logs for details....", caught);
						Window.alert("Error in creating transmittal. See logs for details....");
					}

					public void onSuccess(Boolean result) {
						messageBox.close();
						if (result) {
							ILWizard.this.hide();
							Window.alert("Invitation Letter created Succesfully");
						} else{
							Log.error("Error in creating transmittal. See logs for details....");
							TbitsInfo.error("Error in creating transmittal. See logs for details....");
							Window.alert("Error in creating transmittal. See logs for details....");
						}
					}
				});
	}

	private void processIds(List<Integer> requestIds) {
		ILConstants.employees.clear();

		JaguarConstants.dbService.getDataByRequestIds(ClientUtils.getSysPrefix(), requestIds, 
				new AsyncCallback<HashMap<Integer, TbitsTreeRequestData>>() {
					public void onFailure(Throwable caught) {
						Log.error("Error while loading data for selected records...", caught);
						TbitsInfo.error("Error while loading data for selected records...", caught);
					}

					public void onSuccess(
							final HashMap<Integer, TbitsTreeRequestData> employees) {
						ILConstants.employees.putAll(employees);
						
						String ageVerify = ""; // removed the age verification
						String passExpiryVerify = verifyPassExpiry();
						String expatStatusVerify = verifyStatus();
						String error = ageVerify + passExpiryVerify + expatStatusVerify;
						if(error.equals("")){
							ILPage1 page1 = new ILPage1(context);
							ILWizard.this.addPage(page1);
							ILWizard.this.addPage(new ILPage2(context));
							ILWizard.this.addPage(new ILPage3(context));
							ILWizard.this.show();
							activePage = page1;
							activePage.onDisplay();
						}else{
							Window.alert(error);
						}
					}
				});
	}
	
	private String verifyAge(){
		String error = "";
		for(TbitsTreeRequestData employee : ILConstants.employees.values()){
			String name = employee.getAsString("name");
			POJO obj = employee.getAsPOJO("DOB");
			if(obj == null){
				error += "\nUnable to find Date of Birth for Employee : " + name;
			}else{
				if(obj instanceof POJODate){
					Date dob = ((POJODate)obj).getValue();
					if(dob == null){
						error += "\nUnable to find Date of Birth for Employee : " + name;
					}else{
						Date today = new Date();
						long diff = today.getTime() - dob.getTime();
						
						long milliSecsIn25Yrs = 1000L * 60 * 60 * 24 * 365 * 25;
						
						if(diff < milliSecsIn25Yrs)
							error += "\nAge of Employee : " + name + " is less than 25 Years";
					}
				}
			}
		}
		return error;
	}
	
	private String verifyPassExpiry(){
		String error = "";
		for(TbitsTreeRequestData employee : ILConstants.employees.values()){
			String name = employee.getAsString("name");
			POJO obj = employee.getAsPOJO("PPExpiryDate");
			if(obj == null){
				error += "\nUnable to find Passport Expiry Date for Employee : " + name;
			}else{
				if(obj instanceof POJODate){
					Date expDate = ((POJODate)obj).getValue();
					if(expDate == null){
						error += "\nUnable to find Passport Expiry Date for Employee : " + name;
					}else{
						Date today = new Date();
						long diff = expDate.getTime() - today.getTime();
						
						long milliSecsIn7months = 1000L * 60 * 60 * 24 * 30 * 7 ;
						
						if(diff < milliSecsIn7months)
							error += "\nPassport Expiry of Employee : " + name + " is nearer than 7 months";
					}
				}
			}
		}
		return error;
	}
	
	private String verifyStatus(){
		String error = "";
		for(TbitsTreeRequestData employee : ILConstants.employees.values()){
			String name = employee.getAsString("name");
			POJO obj = employee.getAsPOJO(IFixedFields.STATUS);
			if(obj == null){
				error += "\nUnable to find Expat Status for Employee : " + name;
			}else{
				if(obj instanceof POJOString){
					String expStatus = ((POJOString)obj).getValue();
					if(expStatus == null){
						error += "\nUnable to find Expat Status for Employee : " + name;
					}else{
						if(expStatus.equals("Inactive")){
							error += "\nExpat Status of Employee : " + name + " is InActive";
						}
					}
				}
			}
		}
		return error;
	}

	protected void addPreviewPDFButton() {
		previewPDFBtn = new Button("Preview as PDF", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				HashMap<String, String> paramTable = getParamTable();
				ArrayList<String[]> schedule = getSchedule();
				final MessageBox messageBox = MessageBox.wait(
						"Please Wait",
						"Invitation Letter Preview is being created",
						"Please Wait...");
				ILConstants.dbService.getPdfPreviewPath(ClientUtils.getSysPrefix(), ILConstants.employees, schedule, paramTable, true, "pdf", new AsyncCallback<ArrayList<String>>() {
					public void onFailure(Throwable caught) {
						messageBox.close();
						Log.error("Error in generating Preview. See logs for details....", caught);
						TbitsInfo.error("Error in generating Preview. See logs for details....", caught);
						Window.alert("Error in generating Preview. See logs for details....");
					}

					public void onSuccess(ArrayList<String> result) {
						messageBox.close();
						if (result != null) {
							for (String path : result){
								Window.open(path, "Preview", "");
//								ClientUtils.showPreview(path);
							}
						} else{
							Log.error("Error in generating Preview. See logs for details....");
							TbitsInfo.error("Error in generating Preview. See logs for details....");
							Window.alert("Error in generating Preview. See logs for details....");
						}
					}
				});
			}
		});
		this.addButton(previewPDFBtn);
	}

	protected void addPreviewDOCButton() {
//		previewDOCBtn = new Button("Preview as DOC",
//				new SelectionListener<ButtonEvent>() {
//
//					public void componentSelected(ButtonEvent ce) {
//						HashMap<String, String> paramTable = getParamTable();
//						ArrayList<String[]> schedule = getSchedule();
//						final MessageBox messageBox = MessageBox.wait(
//								"Please Wait",
//								"Invitation Letter Preview is being created",
//								"Please Wait...");
//						ILConstants.dbService.getPdfPreviewPath(ClientUtils.getSysPrefix(), 
//								ILConstants.employees, schedule, paramTable,
//								true, "doc",
//								new AsyncCallback<ArrayList<String>>() {
//
//									public void onFailure(Throwable caught) {
//										Log.error("Error in generating Preview", caught);
//										TbitsInfo.error("Error in generating Preview", caught);
//										Window.alert("Process did not succeed.");
//									}
//
//									public void onSuccess(
//											ArrayList<String> result) {
//										messageBox.close();
//										if (result != null) {
//											for (String path : result)
//												ClientUtils.showPreview(path);
//										} else{
//											Log.error("Error in generating Preview");
//											TbitsInfo.error("Error in generating Preview");
//											Window.alert("Process did not succeed.");
//										}
//									}
//								});
//					}
//				});
//		this.addButton(previewDOCBtn);
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> getParamTable() {
		HashMap<String, String> paramTable = new HashMap<String, String>();

		IWizardPage<? extends LayoutContainer, ?> page = pages.get(0);
		HashMap<String, String> values0 = (HashMap<String, String>) page
				.getValues();
		if (values0 != null) {
			paramTable.putAll(values0);
		}

		page = pages.get(1);
		HashMap<String, String> map = (HashMap<String, String>) page.getValues();
		if (map != null) {
			paramTable.putAll(map);
		}

		return paramTable;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<String[]> getSchedule() {
		IWizardPage<? extends LayoutContainer, ?> page = pages.get(2);
		ArrayList<String[]> schedule = (ArrayList<String[]>) page.getValues();
		return schedule;
	}
	
	public String getCompiledString(String property, String[] params) throws TbitsExceptionClient{
		if(contentStrings != null){
			String str = contentStrings.get(property);
			if(str != null){
				if(params == null)
					return str;
				Pattern pattern = new Pattern("\\{[0-9]*\\}");
				String[] tokens = pattern.split(str);
				if(tokens.length > params.length + 1 || tokens.length < params.length)
					throw new TbitsExceptionClient("Inappropriate number of parameters to fill in");
				
				String compiledString = "";
				for(int i = 0; i < params.length; i++){
					String token = tokens[i];
					compiledString += token + params[i];
				}
				if(tokens.length == params.length + 1){
					compiledString += tokens[tokens.length - 1];
				}
				return compiledString;
			}
		}
		return null;
	}

	@Override
	public void addBackButton() {
		backBtn = new Button("Back", new SelectionListener<ButtonEvent>(){
			@Override
				public void componentSelected(ButtonEvent ce) {
					try{
						int current = activePage.getDisplayOrder();
						if(pages.containsKey(current - 1)){
							IWizardPage<? extends LayoutContainer, ?> prePage = activePage.getPrevious();
							layout.setActiveItem(prePage.getWidget());
							activePage = prePage;
							activePage.onDisplay();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}});
			this.addButton(backBtn);
		
	}

	@Override
	public void addFinishButton() {
		finishBtn = new Button("Finish", new SelectionListener<ButtonEvent>(){
			@Override
		public void componentSelected(ButtonEvent ce) {
			try{
					if(activePage.onLeave())
						onSubmit();
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
		this.addButton(finishBtn);
		
	}

	@Override
	public void addNextButton() {
		nextBtn = new Button("Next", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				try{
					int current = activePage.getDisplayOrder();
					if(pages.containsKey(current + 1)){
						if(activePage.onLeave()){
							IWizardPage<? extends LayoutContainer, ?> nextPage = activePage.getNext();
							layout.setActiveItem(nextPage.getWidget());
							activePage = nextPage;
						activePage.onDisplay();
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
		this.addButton(nextBtn);
		
	}



	
	 	

}
