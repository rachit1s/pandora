package mom.com.tbitsGlobal.client.Extensions;

import mom.com.tbitsGlobal.client.MOMConstants;
import mom.com.tbitsGlobal.client.Meeting;
import mom.com.tbitsGlobal.client.PrintData;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class MOMPrintPreviewButton extends ToolBarButton implements
		MOMConstants {
	private Meeting meeting;

	public MOMPrintPreviewButton(Meeting meeting) {
		super("Print Preview");
		this.meeting = meeting;
		this.setToolTip("Generates the Meeting PDF for preview");

		this.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				PrintData printData = MOMPrintPreviewButton.this.meeting.getPrintData();				
				momService.preview(ClientUtils.getSysPrefix(), printData, new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Error generating preview... Please try again!!!", caught);
						Log.error("Error generating preview... Please try again!!!", caught);
					}

					public void onSuccess(String result) {
						String url = ClientUtils.getUrlToFilefromBase(result);
						Log.info("Preview available at : " + url);
						ClientUtils.showPreview(url);
//						Window.open(result, "Preview", "");
//						ClientUtils.showPreview(result);
					}
				});
			}

		});
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}

	public Meeting getMeeting() {
		return meeting;
	}
}
