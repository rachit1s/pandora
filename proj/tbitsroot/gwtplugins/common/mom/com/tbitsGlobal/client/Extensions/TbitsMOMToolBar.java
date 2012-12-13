package mom.com.tbitsGlobal.client.Extensions;

import java.util.Date;

import mom.com.tbitsGlobal.client.MOMConstants;
import mom.com.tbitsGlobal.client.Meeting;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.google.gwt.user.client.Window;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.grids.TbitsToolBar;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class TbitsMOMToolBar extends TbitsToolBar implements MOMConstants {
	public static String CONTEXT_PARENT_TAB = "context_parent_tab";

	private String caption;

	public TbitsMOMToolBar(String sysPrefix, UIContext parentContext) {
		super(sysPrefix, parentContext);

		try {
			this.caption = myContext.getValue(MOMGridTab.CONTEXT_CAPTION, String.class);
		} catch (NullPointerException e) {
			this.caption = "";
		}
	}

	public void addSaveMeetingButton() {
		ToolBarButton endMeeting = new ToolBarButton("End " + caption,
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (Window.confirm("Are you sure you want to end the current " + caption + "?\n" +
								"This action would commit the current state to the database")) {
							Meeting meeting = myContext.getValue(MOMGridTab.CONTEXT_MEETING, Meeting.class);
							Time time = new Time(new Date());
							meeting.getMomGridTab().getMeetingHeader().setEndTime(time);
							meeting.saveMeeting();
						}
					}
				});
		endMeeting.setToolTip("Ends the " + caption + " and commits all the data to the database");
		this.add(endMeeting);
	}

	public void addDiscardMeetingButton() {
		ToolBarButton discardMeeting = new ToolBarButton("Discard " + caption,
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (Window.confirm("Are you sure you want to discard the current " + caption + "?")) {
							(myContext.getValue(MOMGridTab.CONTEXT_MEETING, Meeting.class)).discardMeeting();
						}
					}
				});
		discardMeeting.setToolTip("Discards the current meeting/agenda");
		this.add(discardMeeting);
	}

	public void addMOMPrintPreviewButton() {
		MOMPrintPreviewButton printPreBtn = new MOMPrintPreviewButton(myContext.getValue(MOMGridTab.CONTEXT_MEETING, Meeting.class));
		this.add(printPreBtn);
	}

//	public void addDraftMeetingButton() {
//		DraftMeetingButton draftMeeting = new DraftMeetingButton(getUIContext());
//		this.add(draftMeeting);
//	}

	public void addPasteButton() {
		ToolBarButton pasteBtn = new ToolBarButton("Paste " + ACTION_ITEM + "s", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (GlobalConstants.requestClipboard != null) {
							MOMGridTab tab = myContext.getValue(CONTEXT_PARENT_TAB, MOMGridTab.class);
							int count = 0;
							for (ModelData model : GlobalConstants.requestClipboard.getChildren()) {
								if (!tab.getBulkUpdatePanel().getSingleGridContainer().getModels().contains(model)) {
									if (((TbitsTreeRequestData) model).getChildCount() != 0) {
										for (ModelData childModel : ((TbitsTreeRequestData) model).getChildren()) {
											if (!tab.getBulkUpdatePanel().getSingleGridContainer().getModels().contains(childModel)) {
												if (((TbitsTreeRequestData) childModel).getAsString(RECORDTYPE).equals(AGENDA_ITEM) 
														|| ((TbitsTreeRequestData) childModel).getAsString(RECORDTYPE).equals(ACTION_ITEM)) {
													tab.getBulkUpdatePanel().getSingleGridContainer().addModel((TbitsTreeRequestData) childModel);
													count++;
												}
											}
										}
									} else {
										if (((TbitsTreeRequestData) model).getAsString(RECORDTYPE).equals(AGENDA_ITEM)
												|| ((TbitsTreeRequestData) model).getAsString(RECORDTYPE).equals(ACTION_ITEM)) {
											tab.getBulkUpdatePanel().getSingleGridContainer().addModel((TbitsTreeRequestData) model);
											count++;
										}
									}
								}
							}
							if (count > 0)
								TbitsInfo.info(count + " items copied to the grid");
							else
								TbitsInfo.info("0 Action items / Agenda Items present in clipboard");
						} else
							TbitsInfo.info("Clipboard is empty");
					}
				});
		pasteBtn.setToolTip("Pastes " + ACTION_ITEM + " present in the clipbord to the grid");
		this.add(pasteBtn);
	}
	
	public void addDraftsButton(){
		ToolBarButton btn = new ToolBarButton("Save Draft", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				Meeting meeting = myContext.getValue(MOMGridTab.CONTEXT_MEETING, Meeting.class);
				if(meeting != null)
					meeting.draftMeeting();
			}});
		this.add(btn);
	}

	@Override
	protected void initializeButtons() {
		
	}

}
