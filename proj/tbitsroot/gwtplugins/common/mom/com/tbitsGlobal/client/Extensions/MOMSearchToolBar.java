package mom.com.tbitsGlobal.client.Extensions;

import java.util.HashMap;

import mom.com.tbitsGlobal.client.DraftData;
import mom.com.tbitsGlobal.client.MOM;
import mom.com.tbitsGlobal.client.MOMConstants;
import mom.com.tbitsGlobal.client.Meeting;
import mom.com.tbitsGlobal.client.PrintData;
import mom.com.tbitsGlobal.client.Events.OnGetDraftMeeting;
import mom.com.tbitsGlobal.client.Events.ToAddAgendaTab;
import mom.com.tbitsGlobal.client.Events.ToAddMeetingTab;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.searchgrid.SearchToolBar;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class MOMSearchToolBar extends SearchToolBar implements MOMConstants {

	protected ITbitsEventHandle<OnFieldsReceived> onFieldsReceivedHandle = new ITbitsEventHandle<OnFieldsReceived>(){
		public void handleEvent(OnFieldsReceived event) {
			if(MOM.validBAList != null && MOM.validBAList.contains(ClientUtils.getSysPrefix())){
				MOMSearchToolBar.this.add(new SeparatorToolItem());
				
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				BAField field = fieldCache.getObject(REQUEST);
				
				if((field.getUserPerm() & PermissionClient.ADD) != 0){
					MOMSearchToolBar.this.addMOMCopyButton();
				}
				
				if((field.getUserPerm() & PermissionClient.CHANGE) != 0){
					MOMSearchToolBar.this.addCutButton();
				}
				
				if((field.getUserPerm() & PermissionClient.ADD) != 0 || (field.getUserPerm() & PermissionClient.CHANGE) != 0){
					momService.getDrafts(new AsyncCallback<HashMap<Integer,DraftData>>() {
						public void onFailure(Throwable caught) {
							Log.error("Could not get MOM drafts", caught);
						}
			
						public void onSuccess(HashMap<Integer,DraftData> result) {
							if (result != null && result.size() > 0) {
								add(new SeparatorToolItem());
								addGetMOMDraftsButton(result);
							}
						}
					});
				}
			}
		}};
	
	public MOMSearchToolBar(String sysPrefix, UIContext parentContext) {
		super(sysPrefix, parentContext);
		
		observable.subscribe(OnFieldsReceived.class, onFieldsReceivedHandle);
	}

	protected void addNewRequestButton() {
		if(MOM.validBAList != null && MOM.validBAList.contains(ClientUtils.getSysPrefix())){
			ToolBarButton newItem = new ToolBarButton("New");
			Menu newMenu = new Menu();
	
			MenuItem agendaItem = new MenuItem(Meeting.CAPTION_AGENDA,
					new SelectionListener<MenuEvent>() {
						@Override
						public void componentSelected(MenuEvent ce) {
							ToAddAgendaTab event = new ToAddAgendaTab(new PrintData(Meeting.CAPTION_AGENDA, null, null, null));
							TbitsEventRegister.getInstance().fireEvent(event);
						}
					});
			newMenu.add(agendaItem);
			
			MenuItem meetingItem = new MenuItem(Meeting.CAPTION_MEETING,
					new SelectionListener<MenuEvent>() {
						@Override
						public void componentSelected(MenuEvent ce) {
							ToAddMeetingTab event = new ToAddMeetingTab(new PrintData(Meeting.CAPTION_MEETING, null, null, null));
							TbitsEventRegister.getInstance().fireEvent(event);
						}
					});
			newMenu.add(meetingItem);
	
			newItem.setMenu(newMenu);
			this.add(newItem);
		}else
			super.addNewRequestButton();
		
	}

	/**
	 * Gets the saved drafts in MOM
	 * 
	 * @param number
	 *            . Number of drafts.
	 */
	public void addGetMOMDraftsButton(final HashMap<Integer,DraftData> drafts) {
		ToolBarButton getDraftButton = new ToolBarButton("View MOM Drafts (" + drafts.size() + ")");
		Menu menu = new Menu();
		
		for(final int draftId : drafts.keySet()){
			final DraftData draft = drafts.get(draftId);
			MenuItem menuItem = new MenuItem(draft.getCaption() + " - " + draft.getPrintData().getHeaderModel().getAsString(SUBJECT), new SelectionListener<MenuEvent>(){
				@Override
				public void componentSelected(MenuEvent ce) {
					TbitsEventRegister.getInstance().fireEvent(new OnGetDraftMeeting(draftId, draft));
				}});
			menu.add(menuItem);
		}
		
		getDraftButton.setMenu(menu);
		getDraftButton.setToolTip("Retreives MOM drafts");
		this.add(getDraftButton);
	}

	/**
	 * Copies selected records to the clipboard along with their request Id.
	 */
	public void addCutButton() {
		ToolBarButton cutBtn = new ToolBarButton("Cut " + ACTION_ITEM + "s to Meeting/Agenda",
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						copyToClipboard(false);
					}
				});
		cutBtn.setToolTip("Copies selected rows along with tBits Id to the clipboard");
		this.add(cutBtn);
	}
	
	protected void addMOMCopyButton() {
		ToolBarButton COPY = new ToolBarButton("Copy " + ACTION_ITEM + "s to Meeting/Agenda", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				copyToClipboard(true);
			}});
		COPY.setToolTip("Copies selected records in the grid to the clipboard while clearing their tBits Id");
		this.add(COPY);
	}
}
