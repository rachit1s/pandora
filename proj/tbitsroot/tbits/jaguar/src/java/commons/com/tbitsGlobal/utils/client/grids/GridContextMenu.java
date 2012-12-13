package commons.com.tbitsGlobal.utils.client.grids;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToAddSubRequest;
import commons.com.tbitsGlobal.utils.client.Events.ToCustomizeColumns;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * 
 * @author sutta
 * 
 * Context Menu shown on the {@link RequestsViewGrid}
 */
public class GridContextMenu extends AbstractGridContextMenu implements IFixedFields {

	public GridContextMenu(RequestsViewGrid iGrid){
		super(iGrid);
		
		this.add(this.getViewRequestMenu());
		this.add(this.getUpdateRequestMenu());
		this.add(this.getSubRequestMenu());
		
		if(iGrid.isCustomizable())
			this.add(this.getConfigureColumnsMenu());
	}
	
	public GridMenuItem getViewRequestMenu(){
		GridMenuItem viewRequest = new GridMenuItem("View " + Captions.getRecordDisplayName()){
			@Override
			protected boolean validate() {
				if(selModel != null && (selModel.getPerms().get(REQUEST) & PermissionClient.VIEW) != 0)
					return true;
				else return false;
			}
		};
		viewRequest.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, selModel.getRequestId() + "", false));
			}});
		return viewRequest;
	}
	
	public GridMenuItem getUpdateRequestMenu(){
		GridMenuItem updateRequest = new GridMenuItem(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_UPDATE_REQUEST)){
			@Override
			protected boolean validate() {
				if(selModel != null && (selModel.getPerms().get(REQUEST) & PermissionClient.CHANGE) != 0)
					return true;
				else return false;
			}
		};
		updateRequest.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_UPDATE, selModel.getRequestId() + "", false));
			}});
		return updateRequest;
	}
	
	public GridMenuItem getSubRequestMenu(){
		GridMenuItem subRequest = new GridMenuItem(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_ADD_SUBREQUEST)){
			@Override
			protected boolean validate() {
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				BAField field = fieldCache.getObject(PARENT_REQUEST_ID);
				if(field != null && (field.getUserPerm() & PermissionClient.ADD) != 0)
					return true;
				else return false;
			}
		};
		subRequest.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				TbitsEventRegister.getInstance().fireEvent(new ToAddSubRequest(selModel.getRequestId()));
			}});
		return subRequest;
	}
	
	public GridMenuItem getConfigureColumnsMenu(){
		GridMenuItem confColumns = new GridMenuItem("Customize Columns");
		confColumns.addSelectionListener(new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				TbitsEventRegister.getInstance().fireEvent(new ToCustomizeColumns(iGrid.getGrid()));
			}});
		confColumns.setNeedsSeparator(true);
		return confColumns;
	}
}
