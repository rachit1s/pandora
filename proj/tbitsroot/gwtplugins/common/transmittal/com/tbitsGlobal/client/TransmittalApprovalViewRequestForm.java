/**
 * 
 */
package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;

//import transbit.tbits.common.DatabaseException;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.widgets.forms.RequestView;

import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToAddSubRequest;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

/**
 * @author lokesh
 * 
 */
public class TransmittalApprovalViewRequestForm extends RequestView {

	private static final String SUBMITTED_FOR_APPROVAL = "SubmittedForApproval";
	int currentSysId = 0;
	Integer requestId = 0;
	ArrayList<TbitsModelData> appCycleTransDataList = new ArrayList<TbitsModelData>();
	ArrayList<TbitsModelData> appCycleTransDataAttList = new ArrayList<TbitsModelData>();
	protected SplitButton actionsButton;
	
	protected TransmittalApprovalViewRequestForm(UIContext parentContext) {
		super(parentContext);

		final TbitsTreeRequestData requestModel = this.getData()
				.getRequestModel();
		if (requestModel != null) {
			currentSysId = (Integer) requestModel.get(BUSINESS_AREA);
			requestId = (Integer) requestModel.get(REQUEST);
			String status = requestModel.get(IFixedFields.STATUS);

			if ((status != null)
					&& (status.trim().equalsIgnoreCase(SUBMITTED_FOR_APPROVAL))) {

				TransmittalConstants.dbService.checkUserExistsInRole(
						currentSysId, requestId, ClientUtils.getCurrentUser()
								.getUserId(), new AsyncCallback<Boolean>() {

							public void onFailure(Throwable caught) {
								caught.printStackTrace();
								TbitsInfo
										.warn("Could not retrieve user permissions to load \"CLick to Approve(Approval)\" button."
												+ " Hence not loading it.\n"
												+ caught.getMessage());
								Window
										.alert("Could not retrieve user permissions to load \"CLick to Approve(Approval)\" button."
												+ " Hence not loading it.");
							}

							public void onSuccess(Boolean result) {
								if (result) {
									Menu actionsMenu = new Menu();
									
									if(actionsButton == null){
										actionsButton = new SplitButton("Start Approval Wizard", new SelectionListener<ButtonEvent>(){
											@Override
											public void componentSelected(ButtonEvent ce) {
												
												

											new TransmittalWizard(
													requestModel,true);

										
											}});
									}else{
										actionsMenu.add(new MenuItem("Start Approval Wizard", new SelectionListener<MenuEvent>(){
											@Override
											public void componentSelected(MenuEvent ce) {
												
									
										
											new TransmittalWizard(
													requestModel,true);

										
											}}));
									}
									actionsMenu.add(new MenuItem("Create Transmittal Without any change", new SelectionListener<MenuEvent>(){
										@Override
										public void componentSelected(MenuEvent ce) {
											new TransmittalWizard(
													requestModel,false);

										}}));
									if(actionsButton != null && actionsMenu.getItemCount() > 0)
										actionsButton.setMenu(actionsMenu);
									headingPanel.addExtButton(actionsButton);
								}
							}
						});
			}
		}
	}
}
