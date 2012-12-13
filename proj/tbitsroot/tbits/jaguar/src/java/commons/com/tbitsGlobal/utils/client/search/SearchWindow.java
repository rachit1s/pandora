package commons.com.tbitsGlobal.utils.client.search;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.widgets.BAMenuButton;

/**
 * 
 * @author sutta
 * 
 * A window that contains a {@link BasicSearchContainer}
 */
public class SearchWindow extends Window{
	/**
	 * @author sourabh
	 *
	 * Invoked when search results are submitted
	 */
	public interface ISubmitHandler{
		public void onSubmit(List<TbitsTreeRequestData> models);
	}
	
	private ISubmitHandler submitHandler;
	
	private BasicSearchContainer searchContainer;
	
	public SearchWindow() {
		super();
		
		this.setClosable(false);
		this.setModal(true);
		this.setLayout(new FitLayout());
		this.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 100);
		this.setHeight(com.google.gwt.user.client.Window.getClientHeight() - 100);
		
		this.setHeading("Search Window");
		
		this.addButton(new Button("Submit Selected", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<TbitsTreeRequestData> models = searchContainer.getGridContainer().getGrid().getSelectionModel().getSelectedItems();
				if(models.size() == 0){
					com.google.gwt.user.client.Window.alert("No records selected");
				}else{
					if(com.google.gwt.user.client.Window.confirm("Are you sure you want to submit " + models.size() + " records?"))
						onSubmit(models);
				}
			}}));
		
		this.addButton(new Button("Submit All", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<TbitsTreeRequestData> models = searchContainer.getGridContainer().getGrid().getStore().getModels();
				if(models.size() == 0){
					com.google.gwt.user.client.Window.alert("No records present in results");
				}else{
					if(com.google.gwt.user.client.Window.confirm("Are you sure you want to submit " + models.size() + " records?"))
						onSubmit(models);
				}
			}}));
		
		this.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(com.google.gwt.user.client.Window.confirm("Are you sure you want to cancel the operation?"))
						hide();
			}}));
		
		ToolBar toolBar = new ToolBar();
		toolBar.add(new BAMenuButton(){
			@Override
			public void onSelect(BusinessAreaClient baClient) {
				super.onSelect(baClient);
				
				this.setText(baClient.getDisplayText());
				this.setToolTip(baClient.getDescription());
				
				createSearchContainer(baClient.getSystemPrefix());
			}
		});
		this.setTopComponent(toolBar);
	}
	
	protected void createSearchContainer(String sysPrefix){
		this.removeAll();
		
		this.searchContainer = new BasicSearchContainer(sysPrefix);
		this.add(searchContainer, new FitData());
		this.layout();
	}
	
	/**
	 * Called before the {@link ISubmitHandler} is invoked.
	 * Default implementation does nothing.
	 * Can be overridden by extending classes
	 * @param models
	 * @return true is the {@link ISubmitHandler} has to be invoked
	 */
	protected boolean beforeSubmit(List<TbitsTreeRequestData> models){
		return true;
	}
	
	/**
	 * Called when the user submits
	 * @param models
	 */
	protected void onSubmit(List<TbitsTreeRequestData> models){
		if(beforeSubmit(models) && submitHandler != null){
			submitHandler.onSubmit(models);
			hide();
		}
	}

	public void setSubmitHandler(ISubmitHandler submitHandler) {
		this.submitHandler = submitHandler;
	}

	public ISubmitHandler getSubmitHandler() {
		return submitHandler;
	}
}
