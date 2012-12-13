package com.tbitsGlobal.admin.client.widgets.pages;

import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader.OnFinishUploaderHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Window which shows after clicking "Edit" link in front of a report in the
 * reports tab.
 *
 */
public class ReportFormWindow extends Window {

	/**
	 * Form Fields
	 */
	public static final String FILE_UPLOAD_STATUS_SUCCESS = "success";
	public static final String FILE_UPLOAD_STATUS_FAILURE	= "failure";
	
	private LabelField reportIDField;
	private TextField<String> nameField;
	private TextField<String> groupField;
	private TextField<String> descriptionField;
	private LabelField oldFileName;
	private SimpleComboBox<String> fileNameField;
	private CheckBox isEnabled;
	private CheckBox isPrivate;
	/** ----------------- */
	protected MultiUploader rptFileUploader;
	protected String reportFileName;
	
	private ReportClient workingReport;
	
	private ReportRolesAndUsersContainer rolesAndUsersContainer;
	
	public ReportFormWindow() {
		super();
		
		this.setHeading("Add/Edit Report");
		this.setModal(true);
		this.setClosable(true);
		
		this.setWidth(850);
		this.setHeight(com.google.gwt.user.client.Window.getClientHeight() - 100);
		
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formLayout.setLabelWidth(120);
		this.setLayout(formLayout);
		this.setScrollMode(Scroll.AUTO);
		rptFileUploader = new MultiUploader();
		
		/**
		 * "Save" button at the bottom of the window
		 */
		Button saveButton = new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				String fileNameFieldValue = fileNameField.getSimpleValue();
				
				if(fileNameFieldValue.toLowerCase().equals("upload file")){
					
					ReportClient report = generateReportFromForm();
					report.setReportName(nameField.getValue());
					
					if (workingReport == null) {
						addReport(report, isPrivate.getValue());
					} else {
						updateReport(report, isPrivate.getValue());
					}
					
				}else{
					oldFileName.setValue(fileNameFieldValue);
					if (workingReport == null) {
						addReport(generateReportFromForm(), isPrivate.getValue());
					} else {
						updateReport(generateReportFromForm(), isPrivate.getValue());
					}
				}
			}
		});

		this.addButton(saveButton);
	}

	private IUploader.OnFinishUploaderHandler onFinishUploaderHander = new OnFinishUploaderHandler(){
		public void onFinish(IUploader uploader) {
			
			if (uploader.getStatus() == Status.SUCCESS) {
				rptFileUploader.submit();
				System.out.println("Server response is  " + rptFileUploader.getServerResponse());
				if(rptFileUploader.getServerResponse().equals(FILE_UPLOAD_STATUS_SUCCESS)){
					
					reportFileName = removeExtension(uploader.getFileName());
					com.google.gwt.user.client.Window.alert("Report File uploaded successfully");
				}else{
					com.google.gwt.user.client.Window.alert("Error uploading report file... See logs for more information");
				}
				
				uploader.clear();
				uploader.cancel();
				rptFileUploader.cancel();
				
				postFileUploadChanges();
			}
		}
	};
	
	/**
	 * Remove the extension from filepath
	 * @param - filepath with filename
	 * @return - filename
	 */
	protected String removeExtension(String filepath) {

//		String title_part = filepath;
//		int i;
//		for(i=title_part.length()-1;i>=0 && title_part.charAt(i)!='.';i--);
//		title_part = title_part.substring(0,i);
//		
//		return title_part;
		
//	    String separator = File.separator;  //.getProperty("file.separator");
	    String filenameUnix;
	    String filenameWin;

	    // Remove the path upto the filename.
	    boolean isSeparatorUnix = false;
	    
	    String separatorUnix = "/";
	    String separatorWin  = "\\";
	    int lastSeparatorIndexUnix = filepath.lastIndexOf(separatorUnix);
	    int lastSeparatorIndexWin = filepath.lastIndexOf(separatorWin);
	    
	    if (lastSeparatorIndexUnix == -1) {
	    	filenameUnix = filepath;
	    } else {
	    	filenameUnix =filepath.substring(lastSeparatorIndexUnix + 1);
	    	isSeparatorUnix = true;
	    }
	    
	    if(lastSeparatorIndexWin == -1) {
	    	filenameWin = filepath;
	    }else {
	    	filenameWin = filepath.substring(lastSeparatorIndexWin + 1);
	    	isSeparatorUnix = false;
	    }

	    if(isSeparatorUnix)
	    	return filenameUnix;
	    return filenameWin;
	}
	
	/**
	 * Default constructor
	 * @param workingReport
	 */
	public ReportFormWindow(ReportClient workingReport) {
		this();
		this.workingReport = workingReport;
	}

	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		reportIDField = new LabelField();
		reportIDField.setFieldLabel("Report ID :");
		reportIDField.setName("report_id");
		this.add(reportIDField);

		nameField = new TextField<String>();
		nameField.setFieldLabel("Report Name");
		this.add(nameField);

		groupField = new TextField<String>();
		groupField.setFieldLabel("Group Name");
		this.add(groupField);

		descriptionField = new TextField<String>();
		descriptionField.setFieldLabel("Description");
		this.add(descriptionField);

		oldFileName = new LabelField();
		oldFileName.setFieldLabel("Stored File Name :");
		oldFileName.setWidth(125);
		this.add(oldFileName);

		final HorizontalPanel fileUploadBox = new HorizontalPanel();
		fileUploadBox.setStyleAttribute("margin-bottom", "3");
		

		final ContentPanel container = new ContentPanel();
		container.setHeaderVisible(false);
		
		rptFileUploader.setWidth("50%"); 

		rptFileUploader.addOnFinishUploadHandler(onFinishUploaderHander);
		container.add(rptFileUploader);
		container.hide();
	
		fileNameField = new SimpleComboBox<String>();
		fileNameField.setTriggerAction(TriggerAction.ALL);
		APConstants.apService.getReportFileNames(new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not retrieve Report file names ", new TbitsExceptionClient(caught));
			}

			public void onSuccess(String[] result) {
				if (result != null && result.length != 0) {
					for (int i = 0; i < result.length; i++) {
						if (result[i].trim() == "")
							continue;
						fileNameField.add(result[i]);
					}
					if (oldFileName.getValue() != null && !oldFileName.getValue().toString().trim().equals(""))
						setSimpleComboField(oldFileName.getValue().toString(), fileNameField);
				}
			}
		});
		fileNameField.add("Upload file");
		fileNameField.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
				if (se.getSelectedItem() == null) 
					setSimpleComboField("upload file", fileNameField);
				
				if ((se.getSelectedItem() == null ) || (se.getSelectedItem().getValue().toLowerCase().equals("upload file"))) {
					container.show();
				} else {
					container.hide();
				}
			}

		});
		
		fileNameField.setFieldLabel("Report File");
		this.add(fileNameField);
		fileNameField.setValue(fileNameField.getStore().getAt(0));

		this.add(container);
		
		CheckBoxGroup checkBoxGroup = new CheckBoxGroup();
		isEnabled = new CheckBox();
		isEnabled.setBoxLabel("Enable");
		checkBoxGroup.add(isEnabled);
		isEnabled.setValue(true);

		isPrivate = new CheckBox();
		isPrivate.setBoxLabel("Private");
		isPrivate.setFireChangeEventOnSetValue(true);
		
		checkBoxGroup.add(isPrivate);
		this.add(checkBoxGroup);
		
		rolesAndUsersContainer = new  ReportRolesAndUsersContainer();
		this.add(rolesAndUsersContainer);
		isPrivate.addListener(Events.Change, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (!((CheckBox) be.getSource()).getValue()) {
					rolesAndUsersContainer.hide();
				} else {
					rolesAndUsersContainer.show();
				}
			}
		});

		rolesAndUsersContainer.hide();
		if(this.workingReport != null){
			oldFileName.show();
			reportIDField.show();
			fillForm(this.workingReport);
		}else{
			reportIDField.hide();
			oldFileName.hide();
		}
	}
	
	/**
	 * Changes to be done after the file has been successfully uploaded into the directory
	 */
	protected void postFileUploadChanges(){
		fileNameField.add(reportFileName);
		fileNameField.setValueField(reportFileName);
		oldFileName.setValue(reportFileName);
		ReportClient report = generateReportFromForm();
		
		if (workingReport == null) {
			addReport(report, isPrivate.getValue());
		} else {
			updateReport(report, isPrivate.getValue());
		}
	}

	/**
	 * Add a new report to database.
	 * @param savePrivateSettings - true if Private checkbox is marked
	 */
	private void addReport(final ReportClient report, final boolean savePrivateSettings) {
		TbitsInfo.info("Adding Report, please wait ....");
		
		APConstants.apService.addReport(report, new AsyncCallback<Integer>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Call failed on server", new TbitsExceptionClient(caught));
				Log.error("Cannot fetch report from database...", caught);
			}

			public void onSuccess(Integer result) {
				report.setReportId(result);
				if (savePrivateSettings) {
					TbitsInfo.info("Report Added (with reportID : " + result + "), now updating private settings");
					updateReportRoles(report);
					updateReportUsers(report);
				} else {
					TbitsInfo.info("Report added Successfully with reportID :" + result);
				}
			}
		});
	}

	private void updateReport(final ReportClient report, final boolean savePrivateSettings) {
		TbitsInfo.info("Updating Report (Report ID : " + report.getReportId() + "), please wait ....");
		APConstants.apService.updateReport(report, new AsyncCallback<ReportClient>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Call failed on server", caught);
				Log.error("Could not update existing report...", caught);
			}

			public void onSuccess(ReportClient result) {
				if (savePrivateSettings) {
					TbitsInfo.info("Report Updated (reportID : "	+ result + "), now updating private settings");
					updateReportRoles(report);
					updateReportUsers(report);
				} else {
					TbitsInfo.info("Report updated Successfully, reportID :"	+ result);
				}
			}

		});
	}
	
	/**
	 * Generate a Report from the values filled up in the report form
	 * @return - Generated report
	 */
	private ReportClient generateReportFromForm(){
		ReportClient report = new ReportClient();
		
		if(reportIDField.getValue() != null){
			report.setReportId(Integer.valueOf((String) reportIDField.getValue()));
		}
		
		report.setReportName(nameField.getValue());
		report.setGroup(groupField.getValue());
		report.setDescription(descriptionField.getValue());
		report.setFileName((String) oldFileName.getValue());
		report.setIsEnabled(isEnabled.getValue());
		report.setIsPrivate(isPrivate.getValue());
		
		return report;
	}
	
	/**
	 * Update the Roles asscotiated with the report
	 * @param report - report for which the Roles have to be updated
	 */
	private void updateReportRoles(ReportClient report) {
		HashMap<String, ArrayList<RoleClient>> rolesMap = rolesAndUsersContainer.getRolesMap();
		APConstants.apService.updateReportRoles(rolesMap, report.getReportId(), new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Call failed on server", new TbitsExceptionClient(caught));
			}

			public void onSuccess(Boolean result) {
				if (result) {
					TbitsInfo.info("Report Roles saved successfully");
				} else {
					TbitsInfo.warn("Can not update report roles");
				}
			}
		});
	}
	
	/**
	 * update the Users associated with the report
	 * @param report - report for which the Users have to be updated
	 */
	private void updateReportUsers(ReportClient report) {
		List<String> includedUserLogins = rolesAndUsersContainer.getIncludedUserLogins();
		List<String> excludedUserLogins = rolesAndUsersContainer.getExcludedUserLogins();
		
		APConstants.apService.updateReportSpecificUsers(includedUserLogins, report.getReportId(), true, new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to update Report specific included users", new TbitsExceptionClient(caught));
			}

			public void onSuccess(Boolean result) {
				if (result) {
					
				} else {
					TbitsInfo.warn("Can not update included users");
				}
			}
		});
		
		APConstants.apService.updateReportSpecificUsers(excludedUserLogins, report.getReportId(), false, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to update Report specific included users", new TbitsExceptionClient(caught));
			}

			public void onSuccess(Boolean result) {
				if (result) {
					
				} else {
					TbitsInfo.warn("Can not update excluded Users");
				}
			}

		});
	}

	private void fillForm(ReportClient reportClient) {
		if (reportClient == null) {
			TbitsInfo.error("Please select a report");
			return;
		}
		
		reportIDField.setValue(reportClient.getReportId());
		nameField.setValue(reportClient.getReportName());
		groupField.setValue(reportClient.getGroup());
		descriptionField.setValue(reportClient.getDescription());
		oldFileName.setValue(reportClient.getFileName());
		fileNameField.setSimpleValue(reportClient.getFileName());
		isEnabled.setValue(reportClient.getIsEnabled());
		isPrivate.setValue(reportClient.getIsPrivate());
		
		if (reportClient.get(ReportClient.IS_PRIVATE) != null && reportClient.getIsPrivate()) {
			getReportSpecificUsers(reportClient.getReportId());
			getReportRoles(reportClient.getReportId());
		}
	}

	/**
	 * Set the value of file selection combo box
	 * @param fileName
	 * @param simpleCombo
	 */
	private void setSimpleComboField(String fileName, SimpleComboBox<String> simpleCombo) {
		if (fileName == null || fileName.trim().equals(""))
			fileName = "Upload file";

		for (SimpleComboValue<String> s : simpleCombo.getStore().getModels()) {
			if (s.getValue().toLowerCase().equals(fileName.toLowerCase())) {
				simpleCombo.setValue(s);
			}
		}
	}

	private void getReportSpecificUsers(final int reportId) {
		if (!isRendered())
			return;

		APConstants.apService.getReportSpecificUsers(reportId,
				new AsyncCallback<HashMap<Integer, Boolean>>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to load Report Specific Users for ReportID : " + reportId, new TbitsExceptionClient(caught));
			}

			public void onSuccess(HashMap<Integer, Boolean> result) {
				rolesAndUsersContainer.fillReportSpecificUsers(result);
			}
		});
	}

	private void getReportRoles(final int reportId) {
		APConstants.apService.getReportRoles(reportId,
				new AsyncCallback<HashMap<Integer, ArrayList<Integer>>>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Faild to load Report Roles for ReportID : " + reportId, new TbitsExceptionClient(caught));
			}

			public void onSuccess(HashMap<Integer, ArrayList<Integer>> result) {
				HashMap<Integer, ArrayList<Integer>> permittedRoles = result;
				if (permittedRoles != null) {
					rolesAndUsersContainer.setPermittedRoles(permittedRoles);
				}
			}

		});
	}
}
