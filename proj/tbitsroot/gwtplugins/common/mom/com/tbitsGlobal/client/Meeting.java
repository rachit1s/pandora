package mom.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mom.com.tbitsGlobal.client.Extensions.MOMGridTab;
import mom.com.tbitsGlobal.client.Extensions.TbitsMOMToolBar;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

public class Meeting implements MOMConstants, IFixedFields, IFormConstants {
	private int draftId;
	private String caption;
	private MOMGridTab momGridTab;
	private Timer draftTimer;

	public Meeting(int draftId, PrintData data) {
		this.draftId = draftId;
		this.caption = data.getCaption();
		
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(MOMGridTab.CONTEXT_CAPTION, caption);
		context.setValue(MOMGridTab.CONTEXT_MEETING, this);
		momGridTab = new MOMGridTab(context) {
			@Override
			protected void onUnload() {
				super.onUnload();
				disableDraft();
			}
		};
		
		if(data.getHeaderModel() != null)
			momGridTab.fillHeader(data.getHeaderModel());
		
		if(data.getActions() != null)
			momGridTab.fillActions(data.getActions());

		this.setStatus();
	}

	public MOMGridTab getMomGridTab() {
		return momGridTab;
	}
	
	public void discardMeeting() {
		this.deleteDraft();
		this.momGridTab.close(true);
	}

	public void saveMeeting() {
		final PrintData printData = getPrintData();
		final List<TbitsTreeRequestData> models = printData.getActions();
		final TbitsTreeRequestData prefilledData = printData.getHeaderModel();
		if (prefilledData == null || prefilledData.getAsPOJO(SUBJECT) == null) {
			Window.alert("Please provide a title for the " + caption);
			return;
		}
		
		if(caption.equals(CAPTION_MEETING)){
			String message = "";
			for(TbitsTreeRequestData model : models){
				POJO assignees = model.getAsPOJO(IFixedFields.CATEGORY);
				if(assignees == null){
					message += "Agency is blank for item : " + (models.indexOf(model) + 1) + "\n";
				}
				
				POJO dueDate = model.getAsPOJO(IFixedFields.DUE_DATE);
				if(dueDate == null){
					message += "Target Date is blank for item : " + (models.indexOf(model) + 1) + "\n";
				}
			}
			if(!Window.confirm(message + "Do you wish to continue submission?"))
				return;
		}
		
		if(models.size() == 0){
			Window.alert("The " + caption + " has no items.");
			return;
		}else{
			final MessageBox messageBox = MessageBox.wait("Meeting Progress", "saving " + caption + " contents ...", "Saving...");
			momService.addMeeting(ClientUtils.getSysPrefix(), printData, new AsyncCallback<Integer>() {
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Creation of " + caption + " failed. Please see logs for details", caught);
					Log.error("Creation of " + caption + " failed. Please see logs for details", caught);
					messageBox.close();
				}

				public void onSuccess(Integer result) {
					messageBox.close();
					if(result > 0){
						deleteDraft();
						disableDraft();
						momGridTab.close(true);
						TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, result + "", false));
					}else{
						TbitsInfo.error("Error : " + result);
					}
				}

			});
		}
	}
	
	public PrintData getPrintData(){
		List<TbitsTreeRequestData> models = momGridTab.getBulkUpdatePanel().getSingleGridContainer().getModels();
		TbitsTreeRequestData headerModel = momGridTab.getMeetingHeader().getValues();
		
		headerModel.set(BUSINESS_AREA, ClientUtils.getSysPrefix());
		headerModel.set(RECORDTYPE, caption);
		
		HashMap<String, String> params = new HashMap<String, String>();
		
		JSONArray attendees = new JSONArray();
		String[] formAttendees = headerModel.getAsString(FORM_EXT_ATTENDEE).split(",");
		HashMap<String, ArrayList<String>> mapAttendees = new HashMap<String, ArrayList<String>>();
		if (formAttendees != null) {
			for (String s : formAttendees) {
				if (s.equals(""))
					continue;
				String firm = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
				String attendee = s.substring(0, s.indexOf('['));
				if (mapAttendees.containsKey(firm))
					mapAttendees.get(firm).add(attendee);
				else {
					ArrayList<String> arr = new ArrayList<String>();
					arr.add(attendee);
					mapAttendees.put(firm, arr);
				}
			}
			int count = 0;
			for (String firm : mapAttendees.keySet()) {
				JSONObject o = new JSONObject();
				o.put("firm", new JSONString(firm));
				String attendee = "";
				for (String s : mapAttendees.get(firm)) {
					if (attendee.equals(""))
						attendee = s;
					else
						attendee += ", " + s;
				}
				o.put("attendee", new JSONString(attendee));
				attendees.set(count, o);
				count++;
			}
		}
		params.put("attendees", attendees.toString());

		JSONArray organizations = new JSONArray();
		int count = 0;
		for (String firm : mapAttendees.keySet()) {
			JSONObject o = new JSONObject();
			o.put("organization", new JSONString(firm));
			organizations.set(count, o);
			count++;
		}
		params.put("organizations", organizations.toString());

		List<TbitsTreeRequestData> newModels = new ArrayList<TbitsTreeRequestData>();
		for (TbitsTreeRequestData model : models) {
			TbitsTreeRequestData newModel = model.clone();
			int requestId = newModel.getRequestId();
			String recordtype = caption.equals(CAPTION_MEETING) ? ACTION_ITEM : AGENDA_ITEM;

			for (String f : headerModel.getPropertyNames()) {
				if (f.equals(SUBJECT) || f.equals(ASSIGNEE) || f.equals(SUBSCRIBER) || f.equals(FORM_ACCESS_TO))
					continue;
				POJO pojo = headerModel.getAsPOJO(f);
				if (newModel.getAsPOJO(f) == null && pojo != null && !(pojo instanceof POJOAttachment))
					newModel.set(f, pojo.clone());
			}
			newModel.set(BUSINESS_AREA, ClientUtils.getSysPrefix());
			newModel.set(RECORDTYPE, recordtype);
			newModel.setRequestId(requestId);
			
			newModels.add(newModel);
		}
		
		return new PrintData(caption, headerModel, newModels, params);
	}
	
	private void setStatus() {
		TbitsMOMToolBar bar = momGridTab.getMomToolBar();
		
		bar.removeAll();
		bar.addPasteButton();
		bar.add(new SeparatorToolItem());
		bar.addDraftsButton();
		bar.addSaveMeetingButton();
		bar.addDiscardMeetingButton();
		
		enableDraft();
		
		bar.add(new SeparatorToolItem());
		bar.addMOMPrintPreviewButton();
	}

	private void enableDraft() {
		draftTimer = new Timer() {
			@Override
			public void run() {
				draftMeeting();
			}
		};

		draftTimer.scheduleRepeating(5 * 60 * 1000);
	}

	private void disableDraft() {
		if (draftTimer != null)
			draftTimer.cancel();
	}

	private void deleteDraft() {
		if (draftId == 0)
			return;
		momService.deleteDraft(draftId, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to delete draft" + caught.getMessage(), caught);
				Log.error("Unable to delete draft", caught);
			}

			public void onSuccess(Boolean result) {
				if(result)
					TbitsInfo.info("Draft Removed");
			}

		});
		draftTimer.cancel();
	}
	
	public void draftMeeting(){
		PrintData printData = this.getPrintData();
		momService.saveDraft(draftId, new DraftData(printData), new AsyncCallback<Integer>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to draft the " + caption, caught);
				Log.error("Unable to draft the " + caption, caught);
			}

			public void onSuccess(Integer result) {
				TbitsInfo.info("Draft saved for this " + caption);
				draftId = result;
			}});
	}

	public void setDraftId(int draftId) {
		this.draftId = draftId;
	}

	public int getDraftId() {
		return draftId;
	}

//	private void checkUnsigned(List<TbitsTreeRequestData> models) {
//		String unsigned = collectUnsigned(models);
//		if (!unsigned.equals("")) {
//			MessageBox.confirm("Save " + caption + "?",
//					"The following action items have not been signed yet : <br /><br />"
//							+ unsigned + " Do you really wish to end the meeting?",
//					new Listener<MessageBoxEvent>() {
//						public void handleEvent(MessageBoxEvent be) {
//							if (be.getButtonClicked().getText().toLowerCase().equals("yes")) {
//								validatePrefilledData();
//							}
//						}
//					});
//		} else
//			validatePrefilledData();
//	}
//
//	private String collectUnsigned(List<TbitsTreeRequestData> models) {
//		String unsigned = "";
//		for (int i = 0; i < models.size(); i++) {
//			String signed = models.get(i).getAsString(SIGNED);
//			if (signed.toLowerCase().equals("false") || signed.equals(""))
//				unsigned += "<li>" + (models.get(i).get(SUBJECT)).toString() + "</li>";
//		}
//
//		return unsigned;
//	}
}
