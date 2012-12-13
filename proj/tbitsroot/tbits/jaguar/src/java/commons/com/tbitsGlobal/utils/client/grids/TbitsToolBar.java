package commons.com.tbitsGlobal.utils.client.grids;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.tags.TagsMenuButton;

/**
 * 
 * @author sourabh
 * 
 * Tool bar for the grids
 */
public abstract class TbitsToolBar extends ToolBar implements IFixedFields{
	public static String CONTEXT_GRID =	"grid";
	
	protected String sysPrefix;
	
	/**
	 * The Tags Menu Button
	 */
	private final TagsMenuButton TAGS_BUTTON = new TagsMenuButton("Tag", true);
	
	protected UIContext myContext;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	/**
	 * Constructor. Intializes the buttons.
	 * @param parentContext
	 */
	public TbitsToolBar(String sysPrefix, UIContext parentContext) {
		super();
		
		this.sysPrefix = sysPrefix;
		this.myContext = parentContext;
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	
	protected void beforeRender() {
		super.beforeRender();
		
		initializeButtons();
	}
	
	
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}

	/**
	 * Add the "Tags" button to the toolbar. Subscribe to the OnApplyTag and OnRemoveTag events.
	 */
	protected void addTagsButton(){
		
		TAGS_BUTTON.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
				if(grid == null)
					return;
				List<TbitsTreeRequestData> selectedItems = getGridSelectedItems();
				TAGS_BUTTON.setTagsCheckStatus(selectedItems);
			}
		});
		this.add(TAGS_BUTTON);
	}
	
	/**
	 * Copies the selected items to the clipboard.
	 * 
	 * @param removeId. If true the requestId is cleared.
	 */
	protected void copyToClipboard(boolean removeId){
		List<TbitsTreeRequestData> selectedItems = this.getGridSelectedItems();
		if(GlobalConstants.requestClipboard == null)
			GlobalConstants.requestClipboard = new TbitsTreeRequestData();
		GlobalConstants.requestClipboard.removeAll();
		int count  = 0;
		for(TbitsTreeRequestData d:selectedItems){
			TbitsTreeRequestData temp = d.clone();
			if(removeId){
				ClientUtils.clearRequestId((TbitsTreeRequestData) temp);
			}
			GlobalConstants.requestClipboard.add(temp);
			count++;
		}
		TbitsInfo.info(count + " Items copied to clipboard");
	}
	
	/**
	 * Get the checked items in the grid.
	 * @return
	 */
	protected List<TbitsTreeRequestData> getGridSelectedItems(){
		RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
		if(grid == null){
			TbitsInfo.error("No grid set for the action");
			return null;
		}
		List<TbitsTreeRequestData> selectedItems = grid.getSelectionModel().getSelectedItems();
		
		return selectedItems;
	}
	
	/**
	 * Initailize default buttons.
	 */
	protected abstract void initializeButtons();
}