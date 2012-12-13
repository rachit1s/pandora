package hse.com.tbitsGlobal.client.forms;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_LINKED_REQUEST;
import static hse.com.tbitsGlobal.shared.HSEConstants.hseService;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;
import com.tbitsGlobal.jaguar.client.widgets.forms.RequestView;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;



public class PARViewRequestForm extends RequestView {

	String errMsg="Some Error Occurred while loading PAR Request Form";	
	String caption=null;
	TbitsTreeRequestData ct = null;

	public PARViewRequestForm(UIContext parentContext) {
		super(parentContext);
		ct = PARViewRequestForm.this.getData().getRequestModel() ;
		final String relReq=ct.getAsString(PAR_LINKED_REQUEST);
		if(relReq.isEmpty())
			caption="Generate AIR";
		else
			caption="Update AIR";

		actionsButton.getMenu().add(new MenuItem(caption, new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				hseService.getTransferRequestData(ClientUtils.getCurrentUser().getUserLogin(),ct,new AsyncCallback<RequestData>() {

					@Override
					public void onFailure(Throwable arg0) {
						Log.error(arg0.getMessage());
					}

					@Override
					public void onSuccess(RequestData rd) {
						// TODO Auto-generated method stub
						TbitsMainTabPanel tmtp = JaguarConstants.jaguarTabPanel;
						UIContext uic=new DefaultUIContext();
						if(relReq.isEmpty()){											
							tmtp.addNewRequestFormTab(uic,rd);
						}else{							
							tmtp.addUpdateTab(uic,rd);


						}


					}
				});

			}

		}));

	}
}




