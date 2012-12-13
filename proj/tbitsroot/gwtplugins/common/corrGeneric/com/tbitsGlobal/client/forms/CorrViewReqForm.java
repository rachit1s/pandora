package corrGeneric.com.tbitsGlobal.client.forms;

import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitFieldNameMap;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitOptionsMap;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.SendMeEmailLinkName;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.corrDBService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;
import com.tbitsGlobal.jaguar.client.widgets.forms.RequestView;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;

import corrGeneric.com.tbitsGlobal.client.utils.ClientUtility;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;


public class CorrViewReqForm extends RequestView {

	HashMap<String,FieldNameEntry> fieldsMap = null;
	/**
	 * @return the fieldsMap
	 */
	private HashMap<String, FieldNameEntry> getFieldsMap() {
		return fieldsMap;
	}

	/**
	 * @param fieldsMap the fieldsMap to set
	 */
	private void setFieldsMap(HashMap<String, FieldNameEntry> fieldsMap) {
		this.fieldsMap = fieldsMap;
	}

	/**
	 * @return the optionMap
	 */
	private HashMap<String, ProtocolOptionEntry> getOptionMap() {
		return optionMap;
	}

	/**
	 * @param optionMap the optionMap to set
	 */
	private void setOptionMap(HashMap<String, ProtocolOptionEntry> optionMap) {
		this.optionMap = optionMap;
	}

	HashMap<String,ProtocolOptionEntry> optionMap = null;

	private boolean isInitialized = false ;
	public CorrViewReqForm(UIContext parentContext) {
		super(parentContext);
		log("CorrViewReqForm constructor called.");
	}

	private void error( String msg )
	{
		Log.error(msg);
		TbitsInfo.error(msg);
	}
	
	@Override
	protected void onAfterLayout() {
		super.onAfterLayout();
		
		if( isInitialized == true )
			return ;
		
		isInitialized = true ;

		CorrConst.corrDBService.getViewRequestParams(this.getData().getSysPrefix(), new AsyncCallback<HashMap<String,Object>>()
				{
					public void onFailure(Throwable caught) 
					{
						error("Error occured filling the form : " + " : for reason : " + caught.getMessage());
					}

					public void onSuccess(HashMap<String, Object> result) 
					{
						if( null == result )
						{
							error("Error occured filling the form.");
							return;
						}
						else
						{
							HashMap<String,FieldNameEntry> fieldsMap = (HashMap<String, FieldNameEntry>) result.get(InitFieldNameMap);
							if( null == fieldsMap )
							{
								error("Error occured filling the form : " + " unable to get the fieldsMap for ba : " + CorrViewReqForm.this.getData().getSysPrefix());
								return;
							}
							CorrViewReqForm.this.setFieldsMap(fieldsMap);
							
							HashMap<String,ProtocolOptionEntry> optionMap = (HashMap<String, ProtocolOptionEntry>) result.get(InitOptionsMap);
							if( null == optionMap )
							{
								// just create an empty optionMap
								optionMap = new HashMap<String, ProtocolOptionEntry>();
							}
							CorrViewReqForm.this.setOptionMap(optionMap);
							
							CorrViewReqForm.this.fillForm();
						}								
					}							
				}) ;

	}
	protected void fillForm() 
	{
		log( "start fillForm");
		if( (null == this.getOptionMap().get(GenericParams.ProtTransferTo_WithoutUpdate)) && ( null == this.getOptionMap().get(GenericParams.ProtTransferTo_WithUpdate)) )
		{
			log("No mapping found to create a Transfer link from this request.");
		}
		else
		{
			if( null != this.getFieldsMap().get(GenericParams.StatusFieldName) )
			{
				String sfn = this.getFieldsMap().get(GenericParams.StatusFieldName).getBaFieldName();
				TbitsTreeRequestData ttrd = this.getData().getRequestModel();
				String status = ttrd.getAsString(sfn);
				if( null != status && status.equals(GenericParams.StatusClosed))
				{
					log("status field was set to close so not creating transfer links");
				}
				else
				{
					createTransferLinks();
				}
			}
			else
			{
				createTransferLinks();
			}
		}
		
		if( null != this.getOptionMap().get(GenericParams.ProtSendMeEmail) && this.getOptionMap().get(GenericParams.ProtSendMeEmail).getValue().equals(GenericParams.ProtSendMeEmail_Yes))
		{
			
			createSendMeEmailLink();
		}
		
		log("finish fillForm");
	}

	public void log(String msg)
	{
		Log.info(msg);
	}
	private void createSendMeEmailLink() 
	{
		log("start createSendMeEmailLink");
		MenuItem sendEmail = new MenuItem(SendMeEmailLinkName, new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) 
			{
				boolean send = Window.confirm("You have selected to get an Email to yourself (" + ClientUtils.getCurrentUser().getUserLogin() +") on EmailId = " + ClientUtils.getCurrentUser().getEmail() + "" +
						"\n for " + CorrViewReqForm.this.getData().getSysPrefix() + "#" + CorrViewReqForm.this.getData().getRequestModel().getRequestId() + ".\n Click Ok to confirm." );
				
				if( send )
				{
					Window.alert("This might take some time. Please click Ok and wait till we process your request.");
					corrDBService.sendMeEmail(ClientUtils.getCurrentUser(), CorrViewReqForm.this.getData().getSysPrefix(), CorrViewReqForm.this.getData().getRequestModel().getRequestId(), new AsyncCallback<Void>() {
						
						public void onSuccess(Void result) 
						{
							Window.alert("An Email was sent to you successfully.");
						}
						
						public void onFailure(Throwable caught) 
						{
							Window.alert("Sending Email Failed with following message :\n" + caught.getMessage());
						}
					});
				}
			}
		}
		);
		
		actionsButton.getMenu().add(sendEmail);
		log("finish createSendMeEmailLink");
	}

	private void createTransferLinks()
	{
		log("start createTransferLinks");
		ProtocolOptionEntry withUpdate = this.getOptionMap().get(GenericParams.ProtTransferTo_WithUpdate);
		if( null != withUpdate )
		{
			// assuming that the value will have comma separated ba's for which the links are to be created.
			// then creating dummy ProtocolEntries for each such ba and then creating links.
			String bas = withUpdate.getValue();
			ArrayList<String> baList = ClientUtility.splitToArrayList(bas);
			if( null != baList )
			{
				for( String ba : baList )
				{
					ProtocolOptionEntry dummy = new ProtocolOptionEntry(-1,withUpdate.getSysPrefix(),withUpdate.getName(),ba,withUpdate.getDescription());
					createMenuItem(dummy);
				}
			}
		}
		
		ProtocolOptionEntry withoutUpdate = this.getOptionMap().get(GenericParams.ProtTransferTo_WithoutUpdate);
		if( null != withoutUpdate )
		{
			// assuming that the value will have comma separated ba's for which the links are to be created.
			// then creating dummy ProtocolEntries for each such ba and then creating links.
			String bas = withoutUpdate.getValue();
			ArrayList<String> baList = ClientUtility.splitToArrayList(bas);
			if( null != baList )
			{
				for( String ba : baList )
				{
					ProtocolOptionEntry dummy = new ProtocolOptionEntry(-1,withoutUpdate.getSysPrefix(),withoutUpdate.getName(),ba,withoutUpdate.getDescription());
					createMenuItem(dummy);
				}
			}
		}
		log("finish createTransferLinks");
	}
	private void createMenuItem(final ProtocolOptionEntry poe) 
	{
		log("start createMenuItem for entry : " + poe);
		String buttonName = (poe.getName().equals(GenericParams.ProtTransferTo_WithUpdate) ? "*" :"") + "Transfer-To " + poe.getValue() ;
		MenuItem tbutton = new MenuItem(buttonName, new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) 
			{
				TbitsTreeRequestData myRequestModel = CorrViewReqForm.this.getData().getRequestModel();
				// myRequestModel,
				POJOInt pi = (POJOInt) myRequestModel.getAsPOJO(IFixedFields.BUSINESS_AREA);
				if( null == pi )
				{
					TbitsInfo.write("Cannot find the sysId associated with request.", TbitsInfo.ERROR);
					return;
				}
				
				int sysId = pi.getValue(); 
				int reqId = myRequestModel.getRequestId();
				
				CorrConst.corrDBService.getRequestDataForTransferRequest(ClientUtils.getCurrentUser().getUserLogin(),sysId, reqId , myRequestModel, poe.getName(), poe.getValue() , new AsyncCallback<HashMap<String,? extends Serializable >>() {
					
					public void onSuccess(HashMap<String,? extends Serializable > result) 
					{
						RequestData rdt = (RequestData) result.get(CorrConst.REQUEST_DATA);
						if( null != rdt )
						{
							UIContext uic = new DefaultUIContext();
							ArrayList<String> prefillFields = (ArrayList<String>) result.get(CorrConst.PREFILL_FIELDS);
							if( null != prefillFields )
								uic.setValue(CorrConst.PREFILL_FIELDS, prefillFields);
							
							TbitsMainTabPanel panel = JaguarConstants.jaguarTabPanel ;
							if( poe.getName().equals(GenericParams.ProtTransferTo_WithoutUpdate))
							{
								panel.addNewRequestFormTab(uic,rdt);
							}
							else
							{
								// now check if this request actually is update or add
								TbitsTreeRequestData ttrd = rdt.getModel();
								if( null == ttrd )
								{
									panel.addNewRequestFormTab(uic,rdt);
									return;
								}
								
								Integer reqId = ttrd.getRequestId() ;
								if( null == reqId || reqId == 0)
								{
									panel.addNewRequestFormTab(uic,rdt);
									return;
								}
								
								// else it is an update request.
								panel.addUpdateTab(uic,rdt);
							}
						}
						else
						{
							TbitsInfo.write("Empty data received. Cannot open request. Please try again", TbitsInfo.ERROR);
							Log.error("The received request_data was null. Cannot open request.");
						}
					}
					
					public void onFailure(Throwable caught) 
					{
						TbitsInfo.write("Exception occured while transfering request from " + poe.getSysPrefix() + " to " + poe.getValue() + ". Error Msg : " + caught.getMessage(), TbitsInfo.ERROR);
					}
				});
			}
		});
		actionsButton.getMenu().add(tbutton);
		log("finish createMenuItem for entry : " + poe);
	}
}