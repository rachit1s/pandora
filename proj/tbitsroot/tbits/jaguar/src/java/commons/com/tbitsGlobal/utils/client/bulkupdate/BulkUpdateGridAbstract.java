package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.TbitsGridView;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRendererPlugin;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow.Parser;

/**
 * 
 * @author sourabh
 * 
 * Abstract Bulk Grid
 * @param <M>
 */
public abstract class BulkUpdateGridAbstract<M extends ModelData> extends EditorGrid<M>{
	public static String CONTEXT_SINGLE_GRID_CONTAINER = "single_grid_container";
	
	/**
	 * 
	 * @author sourabh
	 * 
	 * Mode in which the grid is working
	 */
	public enum BulkGridMode{
		SINGLE, COMMON
	}
	
	/**
	 * 
	 * @author sourabh
	 *
	 * Validates a value
	 */
	public interface Validator{
		/**
		 * @param value
		 * @return null if valid value else error message
		 */
		public String validate(Object value);
	}
	
	private HashMap<String, Validator> validatorMap;
	
	protected BulkGridMode gridMode = BulkGridMode.SINGLE;
	
	/**
	 * true to show numberer
	 */
	protected boolean showNumberer = true;
	
	/**
	 * true to show check box selection model
	 */
	protected boolean showSelectionModel = true;
	
	/**
	 * true to show remove link
	 */
	protected boolean canRemoveRow = true;
	
	/**
	 * true to show update status
	 */
	protected boolean showStatus = true;
	
	protected TbitsGridView gridView;
	
	protected HashMap<String, Parser> parserMap;
	protected HashMap<String, ExcelImportDataType> dataTypeMap;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	private BulkUpdateGridAbstract() {
		super(new ListStore<M>(), new ColumnModel(new ArrayList<ColumnConfig>()));
		
		this.setStripeRows(true);
		this.setTrackMouseOver(true);
		this.setColumnLines(true);
		
		this.validatorMap = new HashMap<String, Validator>();
		this.parserMap = new HashMap<String, Parser>();
		this.dataTypeMap = new HashMap<String, ExcelImportDataType>();
		
		gridView = new TbitsGridView(){
			@Override
			protected void onColumnWidthChange(int column, int width) {
				super.onColumnWidthChange(column, width);
				this.refresh(false);
			}
		};
		
		this.setView(gridView);
		this.getView().setShowInvalidCells(true);
		
		// Register LinkCellRenderers
		this.addPlugin(new LinkCellRendererPlugin());
	    
	    // Validate models whenever they are added to the grid
	    this.getStore().addStoreListener(new StoreListener<M>(){
	    	@Override
	    	public void storeAdd(StoreEvent<M> se) {
	    		super.storeAdd(se);
	    		// Validate Models added
	    		List<M> models = se.getModels();
	    		for(M model : models){
	    			validateModel(model);
	    		}
	    	}
	    });
	    
	    observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	public BulkUpdateGridAbstract(BulkGridMode mode){
		this();
		
		this.gridMode = mode;
	}
	
	/**
	 * Adds the row number column
	 */
	protected void addRowNumberer(){
		if(showNumberer){
			RowNumberer rowNum = new RowNumberer();
			cm.getColumns().add(rowNum);
			if(!rendered)
				this.addPlugin(rowNum);
		}
	}
	
	/**
	 * Adds the selection model to the grid.
	 */
	protected void addSelectionModel(){
		if(showSelectionModel){
			CheckBoxSelectionModel<M> sm = new CheckBoxSelectionModel<M>();
			cm.getColumns().add(sm.getColumn());
			this.setSelectionModel(sm);
			sm.setSelectionMode(SelectionMode.MULTI);
			if(!rendered)
				this.addPlugin(sm);
		}
	}
	
	/**
	 * Adds the Update Status Column
	 */
	protected void addStatusColumn(){
		if(showStatus){
			ColumnConfig status = new ColumnConfig();
			status.setId(IBulkUpdateConstants.RESPONSE_STATUS);
			status.setWidth(100);
		    status.setFixed(true);
		    status.setMenuDisabled(true);
			if(gridMode == BulkGridMode.SINGLE){
				status.setHeader("Update Status");
				status.setRenderer(new GridCellRenderer<M>(){
					public String render(M model, String property,
							ColumnData config, int rowIndex, int colIndex,
							ListStore<M> store,
							Grid<M> grid) {
						Object o = model.get(property);
						String value = "";
						String color = "#000";
						if(o == null)
							value = "";
						else{
							if(o instanceof String){
								value = (String) o;
								if(value.startsWith(IBulkUpdateConstants.UPDATED) || value.startsWith(IBulkUpdateConstants.ADDED)){
									color = "#0f0";
								}else{
									color = "#f00";
								}
							}else{
								value = "Unknown Error";
								color = "#f00";
							}
						}
						return "<div style='display:block; white-space:normal; overflow:hidden; width:100px; color:" 
							+ color + ";'>" + value + "</div>";
					}});
			}else{
				status.setRenderer(new GridCellRenderer<M>(){
					public Object render(M model, String property,
							ColumnData config, int rowIndex, int colIndex,
							ListStore<M> store,
							Grid<M> grid) {
						ToolButton btn = new ToolButton("x-tool-help", new SelectionListener<IconButtonEvent>(){
							@Override
							public void componentSelected(IconButtonEvent ce) {
								Window window = new Window();
								window.setStyleAttribute("background", "#fff");
								window.setHeading("Bulk Add/Update");
								window.addText("<div style=\"margin:5px;\"><p>This control is used to Add/Update more than one records at a time.</p>" +
										"<ul><li>The Upper pane can be used to edit each record individually.</li>" +
										"<li>The Lower pane can be used to edit multiple records present in upper pane.</li>" +
										"<li>If you want to edit records selectively then select the intended records in the " +
										"upper pane and then make the change in lower pane.</li>" +
										"<li>To add a new record click \"Add Row\" Button at the Top Left</li></ul></div>");
								window.show();
							}});
						return btn;
					}});
			}
			cm.getColumns().add(status);
		}
	}
	
	/**
	 * Adds the column having "Remove links"
	 */
	protected void addRemoveColumn(){
		if(canRemoveRow){
			ColumnConfig remove = getRemoveColumn();
			if(remove != null)
				cm.getColumns().add(remove);
		}
	}
	
	/**
	 * Called when "Remove" link is clicked before actually removing the model
	 * @param model
	 * @return True if has to be removed
	 */
	protected boolean beforeRemoveRow(M model){
		return true;
	}
	
	/**
	 * Called when "Remove" link is clicked
	 * @param model
	 */
	protected void onRemove(M model){
		this.getStore().remove(model);
	}
	
	/**
	 * Adds all the default columns
	 */
	protected void addDefaltColumns(){
		this.addRowNumberer();
		this.addSelectionModel();
		this.addStatusColumn();
		this.addRemoveColumn();
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		this.addDefaltColumns();
		this.createColumns();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	/**
	 * @return Convenience method to get a column with checkbox. 
	 * It fires AfterEdit event when value is changed
	 */
	protected CheckColumnConfig getCheckColumn(){
		CheckColumnConfig col = new BulkGridCheckColumnConfig(gridMode);
		
		return col;
	}
	
	/**
	 * Create columns of the grid
	 */
	protected abstract void createColumns();
	
	/**
	 * Sets {@link Validator} for a property
	 * @param property
	 * @param validator
	 */
	public void setValidator(String property, Validator validator){
		if(validatorMap != null)
			validatorMap.put(property, validator);
	}

	public HashMap<String, Validator> getValidatorMap() {
		return validatorMap;
	}
	
	/**
	 * Validate a model
	 * @param model
	 * @return Blank string when no error else error text;
	 */
	private String validateModel(M model){
		String cummulativeMsg = "";
		if(validatorMap != null){
			for(String property : validatorMap.keySet()){
				String msg = validatorMap.get(property).validate(model.get(property));
				store.getRecord(model).setValid(property, msg == null);
				if(msg != null){
					cummulativeMsg += msg + " ";
				}
			}
		}
		return cummulativeMsg;
	}
	
	/**
	 * Validates models and give the errors if any
	 * @param models
	 * @return Error string. Empty String when there are no errors
	 */
	public String getValidationResults(List<M> models){
		String cummulativeMsg = "";
		for(M model : models){
			String msg = validateModel(model);
			if(msg != null && !msg.equals("")){
				int index = this.getStore().indexOf(model);
				cummulativeMsg += "\nRow " + ++index + " : " + msg;
			}
		}
		
		return cummulativeMsg;
	}
	
	public TbitsGridView getTbitsGridView(){
		return gridView;
	}
	
	public HashMap<String, Parser> getParserMap() {
		return parserMap;
	}

	public HashMap<String, ExcelImportDataType> getDataTypeMap() {
		return dataTypeMap;
	}
	
	protected ColumnConfig getRemoveColumn(){
		ColumnConfig remove = new ColumnConfig();
		remove.setId("grid_remove");
		remove.setFixed(true);
		if(gridMode == BulkGridMode.SINGLE){
			remove.setRenderer(new LinkCellRenderer<M>(){
				public Object render(final M model, String property,
						ColumnData config, int rowIndex, int colIndex,
						final ListStore<M> store, Grid<M> grid) {
					ClickableLink link = new ClickableLink("Remove", new ClickableLinkListener<GridEvent<M>>(){
						public void onClick(GridEvent<M> e) {
							if(beforeRemoveRow(model)){
								onRemove(model);
							}
					}});
					addLink(link);
					return link.getHtml();
				}});
			remove.setWidth(70);
		}else{
			remove.setWidth(220);
		}
		
		return remove;
	}
}
