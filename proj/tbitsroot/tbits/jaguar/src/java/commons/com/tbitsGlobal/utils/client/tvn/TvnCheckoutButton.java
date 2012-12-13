package commons.com.tbitsGlobal.utils.client.tvn;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class TvnCheckoutButton extends ToolBarButton{

	public TvnCheckoutButton(String name, final BusinessAreaClient ba) {
		super(name);
		this.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				handleCall(ba.getSystemId(), 0, 0);
			}
		});
	}
	
	public TvnCheckoutButton(String name, final TbitsTreeRequestData requestModel) {
		super(name);
		this.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				handleCall(requestModel.getSystemId(), requestModel.getRequestId(), 0);
			}
		});
	}
	
	public TvnCheckoutButton(String name, final TbitsTreeRequestData requestModel, final FieldClient field) {
		super(name);
		this.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				handleCall(requestModel.getSystemId(), requestModel.getRequestId(), field.getFieldId());
			}
		});
	}
	
	public void handleCall(int sysId, int requestId, int attFieldId){
		String server = ClientUtils.getUrlToFilefromBase("");
		GlobalConstants.utilService.getTvnProtocolUrl(server, sysId, requestId, attFieldId, new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while getting url for Tvn.", caught);
				Log.error("Error while getting url for Tvn.", caught);
			}

			public void onSuccess(String result) {
				if(result == null){
					TbitsInfo.info("Could not recieve the requested URL from server. Check log for more details.");
					Log.info("Could not recieve the requested URL from server. Check log for more details.");
				}
				else{
					final Dialog complex = new Dialog();  
				    complex.setBodyBorder(false);  
				    complex.setHeading("Checkout URL");  
				    complex.setWidth(400);  
				    complex.setHeight(225);  
				    complex.setModal(true);
				    complex.setHideOnButtonClick(true);  
				  
				    complex.setLayout(new FitLayout());  
				  
				    ContentPanel panel = new ContentPanel();  
				    panel.setHeading("URL");
				    panel.add(new Html("Click on the URL below to checkout using TortoiseSVN : <br><br>"));
				    Html href = new Html("<a href='"+result+"' target='_blank'><b>"+result+"</b></a>");
				    panel.add(href);
				    complex.add(panel);  
				  
					complex.show();
				}
			}
		});
	}

}
