package com.tbitsGlobal.admin.client.permTool;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;

/**
 * Tab Item to display the various request forms a specified user and request.
 * 
 * @author Karan Gupta
 *
 */
public class UserFormView extends TabItem implements PTConstants{

	// The content panel to diaplay the user forms
	private ContentPanel viewPanel;
	
	/**
	 * Constructor
	 * 
	 * @param heading
	 */
	public UserFormView(String heading){
		
		super(heading);
		
		viewPanel = new ContentPanel(new FitLayout());
		viewPanel.setHeaderVisible(false);
		viewPanel.setBorders(false);
		viewPanel.setWidth(950);
		viewPanel.setHeight(370);
		viewPanel.setScrollMode(Scroll.AUTO);
		this.add(viewPanel);
	}
	
	/**
	 * Show the requested type of form for the given user and request.
	 * 
	 * @param requestId
	 * @param userId
	 * @param type : PTConstants.(VIEW/UPDATE/EMAIL)
	 */
	public void showForm(TextField<String> requestId, final int userId, final int type){
		try{
			final int reqId = Integer.parseInt(requestId.getValue());
			
			APConstants.apService.getDataByRequestId(ClientUtils.getSysPrefix(), userId, reqId,  new AsyncCallback<TbitsTreeRequestData>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error(caught.getMessage(), caught);
					Log.error("Error while loading data for tBits Id : " + reqId, caught);
				}

				 
				public void onSuccess(TbitsTreeRequestData result) {
					if(result == null){
						TbitsInfo.error("No " + Captions.getRecordDisplayName() + " fetched for tBits Id : " + reqId);
						return;
					}
					TbitsTreeRequestData data = result;
					data.setRequestId(reqId);
					
					DefaultUIContext mainContext = new DefaultUIContext();
					mainContext.setValue(REQUEST_MODEL, data);
					
					ContentPanel view = null;
					switch (type){
						case VIEW :
							view = new RequestViewPT(mainContext);
							break;
						case UPDATE :
							view = new RequestUpdatePT(mainContext);
							break;
						case EMAIL :
							showEmailForm(userId, reqId);
							break;
					}
					
					viewPanel.removeAll();
					if(view != null){
						view.setAutoWidth(true);
						viewPanel.add(view);
					}
					viewPanel.layout();
				}});

			
		}
		catch(NumberFormatException e){
			TbitsInfo.error("Incorrect request id format. Enter a valid Integer as an id.");
			return;
		}
	}
	
	/**
	 * Display the email layout for the given user and request.
	 * 
	 * @param userId
	 * @param reqId
	 */
	private void showEmailForm(int userId, int reqId) {

		GlobalConstants.utilService.getEmailHtml(ClientUtils.getCurrentBA().getSystemId() ,userId, reqId, new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to fetch the mail format.", caught);
			}

			public void onSuccess(String result) {
				if(result == null)
					TbitsInfo.error("No mail content fetched!");
				else{
					ContentPanel view = new ContentPanel(new FlowLayout());
					view.setHeaderVisible(false);
					view.setBodyBorder(false);
					view.add(new Html(result));
					view.setScrollMode(Scroll.AUTO);
					viewPanel.removeAll();
					viewPanel.add(view);
					viewPanel.layout();
				}
			}
		});
	}

	/**
	 * Form the model data for add request and display the form.
	 * 
	 * @param userId
	 */
	public void showAddForm(int userId) {
		DefaultUIContext context = new DefaultUIContext();
		TbitsTreeRequestData model = new TbitsTreeRequestData();
		model.set(IFixedFields.PARENT_REQUEST_ID, new POJOInt(0));
		context.setValue(PTConstants.REQUEST_MODEL, model);
		
		ContentPanel view = new RequestAddPT(context);
		view.setAutoWidth(true);
		
		viewPanel.removeAll();
		viewPanel.add(view);
		viewPanel.layout();
	}
}
