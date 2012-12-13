package com.tbitsGlobal.jaguar.client.widgets.forms;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.events.ToUpdateRequestOtherBA;
import com.tbitsGlobal.jaguar.client.widgets.ActionHistoryPanel;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToAddSubRequest;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentFieldContainer;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.AttachmentFieldConfig;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.tvn.TvnCheckoutButton;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

/**
 * Form used to view request.
 * 
 * @author sourabh
 *
 */
public class RequestView extends AbstractViewRequestForm {
	
	protected ActionHistoryPanel actionPanel;
	
	/**
	 * Button for actions like Refresh, Update , etc
	 */
	protected SplitButton actionsButton;
	
	private IRequestFormData data;
	
	/**
	 * Constructor
	 * 
	 * @param parentContext
	 */
	protected RequestView(UIContext parentContext){
		super(parentContext);
		
		data = new DefaultRequestFormData(parentContext);
		
		final TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null){
		
			Menu actionsMenu = new Menu();
			
			/*
			 * TODO Creating the actions button. A clumsy thing... needs a second thought
			 */
			if((requestModel.getPerms().get(REQUEST) & PermissionClient.CHANGE) != 0){
				if(actionsButton == null){
					actionsButton = new SplitButton(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_UPDATE_REQUEST), new SelectionListener<ButtonEvent>(){
						@Override
						public void componentSelected(ButtonEvent ce) {
							updateRequest();
						}});
				}else{
					actionsMenu.add(new MenuItem(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_UPDATE_REQUEST), new SelectionListener<MenuEvent>(){
						@Override
						public void componentSelected(MenuEvent ce) {
							updateRequest();
						}}));
				}
			}
			
			FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
			BAField field = fieldCache.getObject(PARENT_REQUEST_ID);
			if(field != null && (field.getUserPerm() & PermissionClient.ADD) != 0){
				if(actionsButton == null){
					actionsButton = new SplitButton(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_ADD_SUBREQUEST), new SelectionListener<ButtonEvent>(){
						@Override
						public void componentSelected(ButtonEvent ce) {
							TbitsEventRegister.getInstance().fireEvent(new ToAddSubRequest(requestModel.getRequestId()));
						}});
				}else{
					actionsMenu.add(new MenuItem(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_ADD_SUBREQUEST), new SelectionListener<MenuEvent>(){
						@Override
						public void componentSelected(MenuEvent ce) {
							TbitsEventRegister.getInstance().fireEvent(new ToAddSubRequest(requestModel.getRequestId()));
						}}));
				}
			}
			
			if(actionsButton == null){
				actionsButton = new SplitButton("Refresh", new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						refresh();
					}});
			}else{
				actionsMenu.add(new MenuItem("Refresh", new SelectionListener<MenuEvent>(){
					@Override
					public void componentSelected(MenuEvent ce) {
						refresh();
					}}));
			}
			
			if((requestModel.getPerms().get(REQUEST) & PermissionClient.CHANGE) != 0){
				actionsMenu.add(new MenuItem("Send Mails Again", new SelectionListener<MenuEvent>() {
					@Override
					public void componentSelected(MenuEvent ce) {
						sendMailAgain();
					}
				}));
			}
			actionsMenu.add(new MenuItem("Merge Annotations of PDF Files", new SelectionListener<MenuEvent>(){
				@Override
				public void componentSelected(MenuEvent ce) {
					List<FileClient> fileClients = new ArrayList<FileClient>();
					for(IFieldConfig config : fieldConfigs.values()){
						if(config instanceof AttachmentFieldConfig){
							AttachmentFieldConfig attachmentConfig  = (AttachmentFieldConfig) config;
							AttachmentFieldContainer attachmentFieldContainer = attachmentConfig.getWidget();
							fileClients.addAll(attachmentFieldContainer.getUploadProgressGrid().getSelectionModel().getSelectedItems());
						}
					}
					if(fileClients.size() > 1){
						GlobalConstants.utilService.mergePDF(getData().getSysPrefix(), getData().getRequestModel().getRequestId(), 
								getData().getRequestModel().getMaxActionId(), fileClients, new AsyncCallback<String>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Error while merging PDF Files... Please see logs for details", caught);
										Log.error("Error while merging PDF Files... Please see logs for details", caught);
									}
	
									public void onSuccess(String result) {
										String url = ClientUtils.getUrlToFilefromBase(result);
										Log.info("Merged PDF available at : " + url);
										ClientUtils.showPreview(url);
									}});
					}else{
						Window.alert("Please select more that one PDF files to merge");
					}
				}}));
			
			actionsMenu.add(new MenuItem("Print", new SelectionListener<MenuEvent>(){

				@Override
				public void componentSelected(MenuEvent ce) {
					showEmailForm(ClientUtils.getCurrentBA().getSystemId(), 
							ClientUtils.getCurrentUser().getUserId(), getData().getRequestModel().getRequestId());
				}
			}));

			if(actionsButton != null && actionsMenu.getItemCount() > 0)
				actionsButton.setMenu(actionsMenu);
		}
		
	}
	

	
	/**
	 * Display the email layout for the given user and request.
	 * 
	 * @param userId
	 * @param reqId
	 */
	public void showEmailForm(int sysId, int userId, int reqId) {

		GlobalConstants.utilService.getHTMLForRequestPrint(sysId ,userId, reqId, new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to fetch the mail format.", caught);
			}

			public void onSuccess(String result) {
				if(result == null)
					TbitsInfo.error("No mail content fetched!");
				else{
					String title = "Untitled - tBits";
					try
					{
						String subject = getData().getRequestModel().get(SUBJECT);
						int requestId = getData().getRequestModel().getRequestId();
						String prefix = ClientUtils.getCurrentBA().getSystemPrefix();
						title = prefix + "#" + requestId + " " + subject + " - tBits";
					}
					catch(Exception exp)
					{
						Log.error("Unable to prepare the title.");
					}
					openContentInNewWindow(result, title.replaceAll("'", "\\'"));
				}
			}
		});
	}
	
	public static native void openContentInNewWindow(String documentContent, String title) /*-{
		var wnd1 =	$wnd.open("", "EmailPreview", "");
		wnd1.document.write(documentContent);
		wnd1.document.close();
		wnd1.document.title = title;
		wnd1.print();
	}-*/;

	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		headingPanel.setActionsButton(actionsButton);
		
		if(GlobalConstants.isTvnSupported){
			TbitsTreeRequestData requestModel = this.getData().getRequestModel();
			headingPanel.setTvnButton(new TvnCheckoutButton("Checkout", requestModel));
		}
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		String sysPrefix = this.getData().getSysPrefix();
		if(requestModel != null && sysPrefix != null){
			actionPanel = new ActionHistoryPanel(sysPrefix, requestModel.getRequestId(), this.getData().getBAFields());
			this.add(actionPanel);
		}
		
		this.registerReadAction();
	}
	
	/**
	 * Updates the form with new data
	 */
	protected void updateRequest(){
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		String sysPrefix = this.getData().getSysPrefix();
		if(requestModel != null && sysPrefix != null){
			if(sysPrefix.equals(ClientUtils.getSysPrefix())){
				TbitsURLManager.getInstance().removeToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, requestModel.getRequestId() + "", false));
				TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_UPDATE, requestModel.getRequestId() + "", false));
			}else
				TbitsEventRegister.getInstance().fireEvent(new ToUpdateRequestOtherBA(sysPrefix, requestModel.getRequestId()));
		}
	}
	
	public void refresh(){
		final TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		final String sysPrefix = this.getData().getSysPrefix();
		if(requestModel != null && sysPrefix != null){
			TbitsInfo.info("Updating Request : " + sysPrefix +  "#" + requestModel.getRequestId() + ", please wait");
			JaguarConstants.dbService.getDataByRequestId(sysPrefix, requestModel.getRequestId(), new AsyncCallback<TbitsTreeRequestData>(){
				public void onFailure(Throwable arg0){
					TbitsInfo.error("Update Failed");
				}
				public void onSuccess(TbitsTreeRequestData result){
					if(result != null){
						reCreate(result);
						
						actionPanel.fillActions(sysPrefix, requestModel.getRequestId());
					}
					else{
						TbitsInfo.error("Request is empty");
					}
				}
			});
		}
	}
	
	public void sendMailAgain() {
		final TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		final String sysPrefix = this.getData().getSysPrefix();
		int reqId = requestModel.getRequestId();
		
		if(!Window.confirm("You are about to send the emails for the request " + sysPrefix + "#" + reqId + " again. Are you sure you want to continue?"))
			return;
		
		if(requestModel != null && sysPrefix != null){
			TbitsInfo.info("Sending Email for : " + sysPrefix +  "#" + requestModel.getRequestId() + ", please wait");
			JaguarConstants.dbService.sendEmailAgain(sysPrefix, reqId, new AsyncCallback<Void>() {
				
				@Override
				public void onSuccess(Void result) {
					TbitsInfo.info("The emails have been successfully sent.");
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Log.error("Error occurred while sending the emails. Please check logs", caught);
				}
			});
		}
	}
	
	public void registerReadAction()
	{
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		String sysPrefix = this.getData().getSysPrefix();
		
		if(requestModel != null && sysPrefix != null)
			this.registerReadAction(sysPrefix, requestModel.getRequestId(), requestModel.getMaxActionId());
	}
	
	private void registerReadAction(String sysPrefix, int requestId, int actionId){
		final int userId = ClientUtils.getCurrentUser().getUserId();
		int sysId=(ClientUtils.getBAbySysPrefix(sysPrefix)).getSystemId();
			JaguarConstants.dbService.registerReadAction(sysId,requestId,actionId,userId, new AsyncCallback<Boolean>(){

				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
				}
				public void onSuccess(Boolean result) {
				}
			});
	}
	
	@Override
	public IRequestFormData getData() {
		return data;
	}
}
