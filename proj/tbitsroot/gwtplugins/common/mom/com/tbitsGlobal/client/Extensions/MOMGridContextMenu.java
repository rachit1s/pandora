package mom.com.tbitsGlobal.client.Extensions;

import java.util.ArrayList;
import java.util.List;

import mom.com.tbitsGlobal.client.MOMConstants;
import mom.com.tbitsGlobal.client.Meeting;
import mom.com.tbitsGlobal.client.PrintData;
import mom.com.tbitsGlobal.client.Events.ToAddAgendaTab;
import mom.com.tbitsGlobal.client.Events.ToAddMeetingTab;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.grids.GridContextMenu;
import commons.com.tbitsGlobal.utils.client.grids.GridMenuItem;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;

public class MOMGridContextMenu extends GridContextMenu implements MOMConstants {
	public MOMGridContextMenu(RequestsViewGrid iGrid) {
		super(iGrid);
		
		this.add(this.getViewAgendaMenu());
		this.add(this.getInitMeetingMenu());
	}
	
	public GridMenuItem getViewAgendaMenu(){
		GridMenuItem viewAsAgenda = new GridMenuItem("View Agenda"){
			@Override
			protected boolean validate() {
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				BAField field = fieldCache.getObject(REQUEST);
				if((field.getUserPerm() & PermissionClient.ADD) != 0 || (field.getUserPerm() & PermissionClient.CHANGE) != 0)
					return true;
				else return false;
			}
		};
		viewAsAgenda.addValue(RECORDTYPE, "Agenda");
		viewAsAgenda.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				List<TbitsTreeRequestData> actions = new ArrayList<TbitsTreeRequestData>();
				if(selModel.getChildCount() > 0){
					for(ModelData model : selModel.getChildren()){
						actions.add((TbitsTreeRequestData) model);
					}
				}
				TbitsEventRegister.getInstance().fireEvent(new ToAddAgendaTab(new PrintData(Meeting.CAPTION_AGENDA, selModel, actions, null)));
			}
		});
		return viewAsAgenda;
	}
	
	public GridMenuItem getInitMeetingMenu(){
		GridMenuItem initMeeting = new GridMenuItem("Initiate Meeting"){
			@Override
			protected boolean validate() {
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				BAField field = fieldCache.getObject(REQUEST);
				if(((field.getUserPerm() & PermissionClient.ADD) != 0 || (field.getUserPerm() & PermissionClient.CHANGE) != 0) && 
						selModel != null && selModel.getChildCount() > 0)
					return true;
				else return false;
			}
		};
		initMeeting.addValue(RECORDTYPE, "Agenda");
		initMeeting.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				List<TbitsTreeRequestData> actions = new ArrayList<TbitsTreeRequestData>();
				if(selModel.getChildCount() > 0){
					for(ModelData model : selModel.getChildren()){
						actions.add((TbitsTreeRequestData) model);
					}
				}
				TbitsEventRegister.getInstance().fireEvent(new ToAddMeetingTab(new PrintData(Meeting.CAPTION_MEETING, selModel, actions, null)));
			}
		});
		return initMeeting;
	}

}
