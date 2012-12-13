package commons.com.tbitsGlobal.utils.client.widgets.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TagRequests;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.tags.TagsMenuButton;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.tvn.TvnCheckoutButton;

/**
 * Abstract class for forms handling requests
 * 
 * @author sourabh
 *
 */
public abstract class AbstractRequestForm extends ContentPanel implements IRequestForm, IFixedFields{
	/**
	 * Configs to determine how a field is going to be displayed
	 */
	protected HashMap<String, IFieldConfig> fieldConfigs;
	
	/**
	 * Panel that is shown at the top of the form
	 */
	protected FormHeadingPanel headingPanel;
	
	/**
	 * The panel conatining all the display groups
	 */
	protected RequestFormPanel form;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	protected UIContext myContext;
	
	/**
	 * Constructor
	 * 
	 * @param parentContext
	 */
	protected AbstractRequestForm(UIContext parentContext){
		super();
		this.setBodyBorder(false);    
		this.setHeaderVisible(false);
	    this.setButtonAlign(HorizontalAlignment.CENTER);
		this.sinkEvents(Event.ONKEYPRESS);
		this.setScrollMode(Scroll.AUTO);
		this.setStyleAttribute("background", "#fff");
		
		this.myContext = parentContext;
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		fieldConfigs = new HashMap<String, IFieldConfig>();
		
		headingPanel = new FormHeadingPanel();
		
		form = new RequestFormPanel();
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
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		if(GlobalConstants.isTagsSupported){
			addTagsButton();
		}
		
		this.add(headingPanel);
		
		this.create();
		this.add(form);
		
		afterCreate();
	}
	
	/**
	 * Determines whether this field has the proper permissions to be displayed in this form
	 * @param bafield
	 * @return true is it has
	 */
	abstract protected boolean hasBAFieldPermission(BAField bafield);
	
	public Widget getWidget()
	{
		return this ;
	}
	
	/**
	 * Create the form.
	 * 
	 * @param requestModel
	 */
	protected void create(){
		this.setHeadingPanelText();
		
		form.removeAll();
		
		ListStore<DisplayGroupClient> displayGroups = this.getData().getDisplayGroups();
		 List<BAField> fields = this.getData().getBAFields().getModels();
		if(displayGroups != null && fields != null )
		{ // Nothing widout display groups.
		
			// get display groups in arraylist
			ArrayList<DisplayGroupClient> dgs = new ArrayList<DisplayGroupClient>(displayGroups.getModels());
			
			// remove inactive dgs
			for( Iterator<DisplayGroupClient> iter = dgs.iterator() ; iter.hasNext() ; )
			{
				DisplayGroupClient dg = iter.next();
				if( dg.getIsActive() == false )
					iter.remove();
			}
			
			// sort display groups
			Collections.sort(dgs, new Comparator<DisplayGroupClient>() {

				@Override
				public int compare(DisplayGroupClient o1, DisplayGroupClient o2) 
				{
					if( o1.getDisplayOrder() < o2.getDisplayOrder() )
						return -1;
					else
						return 1;
				}
			});
			
			// remove inactive fields
			for( Iterator<BAField> iter = fields.iterator() ; iter.hasNext() ; )
			{
				BAField field = iter.next();
				if( field.getIsActive() == false || field.getName().equals(IFixedFields.DESCRIPTION) )
					iter.remove();
			}
			
			// sort all the fields
			Collections.sort( fields, new Comparator<BAField>() {

				@Override
				public int compare(BAField o1, BAField o2) 
				{
					if(o1.getDisplayOrder() < o2.getDisplayOrder() )
						return -1 ;
					else
						return 1;
				}
			});
			
//			HashMap<Integer, DisplayGroupClient> dgMap = new HashMap<Integer, DisplayGroupClient>();
			HashMap<Integer, DisplayGroupContainer> dgContainerMap = new HashMap<Integer, DisplayGroupContainer>();
			DisplayGroupContainer defaultContainer = null; 
			for( DisplayGroupClient dgc : dgs )
			{
				// get displaygroup container and append it to the form
				DisplayGroupContainer displayGroupContainer = getDisplayGroupContainer(dgc, fields);
				if( null != displayGroupContainer )
				{
//					dgMap.put(dgc.getId(), dgc);
					if( dgc.getIsDefault() == true )
						defaultContainer = displayGroupContainer ;
					dgContainerMap.put(dgc.getId(), displayGroupContainer);
				}else if (dgc.getIsDefault() == true){
					DisplayGroupContainer defaultDisplayGroupContainer=getDefaultDisplayGroupContainer(dgc);
					defaultContainer = defaultDisplayGroupContainer ;
					dgContainerMap.put(dgc.getId(), defaultDisplayGroupContainer);
				}
			}
			
			// if defaultContainer != null put rest of the fields into it
			if( null != defaultContainer )
			{
				for(Iterator<BAField> iter = fields.iterator() ; iter.hasNext() ; )
				{
					BAField bafield = iter.next();
	
					if(hasBAFieldPermission(bafield)){
						LayoutContainer panel = createField(bafield, this.getConfig(bafield));
						if(panel != null){
							defaultContainer.add(bafield, panel);
						}
					}
				
				}
				
			}
			
			
			for( DisplayGroupClient dgc : dgs )
			{   
				DisplayGroupContainer dgContainer = dgContainerMap.get(dgc.getId());
				if(null != dgContainer )
					form.add(dgc, dgContainer);
				
			}
			
/*
			//List of groups whose display order is 0.
			ArrayList<DisplayGroupClient> groupsWithoutOrder = new ArrayList<DisplayGroupClient>();
			
			//Map of groups having some display order.
			HashMap<Integer, DisplayGroupClient> groupsWithOrder = new HashMap<Integer, DisplayGroupClient>();
			for(DisplayGroupClient d : displayGroups.getModels()){
				if(d.getDisplayOrder() == 0)
					groupsWithoutOrder.add(d);
				else
					groupsWithOrder.put(d.getDisplayOrder(), d);
			}
			
			//Sort the display orders in ascending order.
			//TODO: Looks buggy. Can be avoided with Collections.sort
			ArrayList<Integer> keySet = new ArrayList<Integer>(groupsWithOrder.keySet());
			int n = keySet.size();
		    for (int pass=1; pass < n; pass++) {  // count how many times
		        // This next loop becomes shorter and shorter
		        for (int i=0; i < n-pass; i++) {
		            if (keySet.get(i) > keySet.get(i+1)) {
		                // exchange elements
		                int temp = keySet.get(i);  
		                keySet.set(i, keySet.get(i+1));  
		                keySet.set(i+1, temp) ;
		            }
		        }
		    }
		    
		    //Array of groups in ascending order of their display order
		    DisplayGroupClient[] groupsInOrder = new DisplayGroupClient[groupsWithOrder.size()];
			int count = 0;
			for(Integer i:keySet){
				groupsInOrder[count++] = groupsWithOrder.get(i);
			}
			
			// Create groups having displayOrder == 0 first.
			for(DisplayGroupClient d : groupsWithoutOrder)
				createDisplayGroup(d);
			
			for(DisplayGroupClient d : groupsInOrder)
				createDisplayGroup(d);
				*/
			
			// get display group container
		}
	}
	
	private DisplayGroupContainer getDefaultDisplayGroupContainer(
			DisplayGroupClient dgc) {
		DisplayGroupContainer displayGroupContainer = new DisplayGroupContainer(dgc);
		return displayGroupContainer;
		
	}

	/**
	 * removes the field from fields and adds it to the displaygroup container.
	 * if no field belong to this group then return null.
	 * @param dgc
	 * @param fields
	 * @return
	 */
	
	private DisplayGroupContainer getDisplayGroupContainer(
			DisplayGroupClient dgc, List<BAField> fields) 
	{
		DisplayGroupContainer displayGroupContainer = new DisplayGroupContainer(dgc);
		boolean foundField = false;
		for( Iterator<BAField> iter = fields.iterator() ; iter.hasNext() ; )
		{
			BAField bafield = iter.next();
			if( null != bafield)
			{
				if(bafield.getDisplayGroup() == dgc.getId() )
				{
					iter.remove();
					if(hasBAFieldPermission(bafield)){
						LayoutContainer panel = createField(bafield, this.getConfig(bafield));
						if(panel != null){
							displayGroupContainer.add(bafield, panel);
							foundField = true;
						}
					}
				}
			}
		}
		
		if( foundField == false)
			return null;
		
		return displayGroupContainer;
	}

	protected void afterCreate(){
		
	}
	
	/**
	 * Creates a display Group in the form.
	 * 
	 * @param group
	 */
	/*
	@Deprecated
	protected void createDisplayGroup(DisplayGroupClient group){
	 int fieldCount = 0;
		
		// Fields having displayOrder == 0;
		HashMap<String, BAField> fieldsWithoutOrder = new HashMap<String, BAField>();
		
		//Fields having some displayOrder.
		ArrayList<BAField> fieldsWithOrder = new ArrayList<BAField>();
		
		for(BAField field : this.getData().getBAFields().getModels()){
			if(field.getName().equals(DESCRIPTION)) 
				continue;
			
			if(field.getDisplayGroup() == group.getId()){
				if(field.getDisplayOrder() != 0){
					fieldsWithOrder.add(field);
				}else 
					fieldsWithoutOrder.put(field.getName(), field);
				fieldCount++;
			}
		}
		if(fieldCount == 0) 
			return;
		
		int n = fieldsWithOrder.size();
	    for (int pass=1; pass < n; pass++) {  // count how many times
	        // This next loop becomes shorter and shorter
	        for (int i=0; i < n-pass; i++) {
	            if (fieldsWithOrder.get(i).getDisplayOrder() > fieldsWithOrder.get(i+1).getDisplayOrder()) {
	                // exchange elements
	                BAField temp = fieldsWithOrder.get(i);
	                fieldsWithOrder.remove(i);
	                fieldsWithOrder.add(i, fieldsWithOrder.get(i));
	                fieldsWithOrder.remove(i + 1);
	                fieldsWithOrder.add(i + 1, temp) ;
	            }
	        }
	    }
		
		DisplayGroupContainer displayGroupContainer = new DisplayGroupContainer(group);

		boolean hasAtleastOneField = false;
		
		// Add fields without display order first
		for(String f : fieldsWithoutOrder.keySet()){
			BAField baField = fieldsWithoutOrder.get(f);
			if(baField.getDisplayGroup() == group.getId() && hasBAFieldPermission(baField)){
				LayoutContainer panel = createField(baField, this.getConfig(baField));
				if(panel != null){
					displayGroupContainer.add(baField, panel);
					hasAtleastOneField = true;
				}
			}
		}
		
		for(BAField baField : fieldsWithOrder){
			if(hasBAFieldPermission(baField)){
				LayoutContainer panel = createField(baField, this.getConfig(baField));
				if(panel != null){
					displayGroupContainer.add(baField, panel);
					hasAtleastOneField = true;
				}
			}
		}
	    
		if(hasAtleastOneField)
			form.add(group, displayGroupContainer);
	}
*/	
	protected void setHeadingPanelText(){
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null){
			this.headingPanel.setRequestHeader(this.getData().getSysPrefix(), requestModel.getRequestId(), requestModel.getAsString(IFixedFields.SUBJECT));
			if(GlobalConstants.isTagsSupported){
				this.headingPanel.displayTags((String) requestModel.get("request_tags"));
			}
			this.headingPanel.layout();
		}
	}

	protected FormHeadingPanel getHeadingPanel()
	{
		return headingPanel;
	}
	
	/**
	 * Creates a field in the form
	 * 
	 * @param baField. 
	 */
	protected abstract LayoutContainer createField(BAField field, IFieldConfig config);
	
	/**
	 * Returns the {@link IFieldConfig} for a field
	 * @param baField
	 * @return
	 */
	public abstract IFieldConfig getConfig(BAField baField);
	
	public abstract IRequestFormData getData();
	
	/**
	 * Recreates the form with a new model
	 */
	public void reCreate(TbitsTreeRequestData requestModel) {
		this.getData().setRequestModel(requestModel);
		this.create();
		this.layout();
	}
	
	/**
	 * Redraws a field taking the {@link IFieldConfig} from {@link #getConfig(BAField)}
	 * @param field
	 * @return true is the field was redrawn
	 */
	public boolean reDrawField(BAField field){
		LayoutContainer panel = createField(field, this.getConfig(field));
		
		return reDrawField(field, panel);
	}
	
	/**
	 * Redraws a field using the config
	 * @param field
	 * @param config
	 * @return true is the field was redrawn
	 */
	public boolean reDrawField(BAField field, IFieldConfig config){
		LayoutContainer panel = createField(field, config);
		
		return reDrawField(field, panel);
	}
	
	/**
	 * Redraws a field putting the panel in place of the field
	 * @param field
	 * @param panel
	 * @return true is the field was redrawn
	 */
	private boolean reDrawField(BAField field, LayoutContainer panel){
		int groupId = form.hasField(field);
		if(groupId > 0){
			return form.reDrawField(groupId, field, panel);
		}
		
		return false;
	}
	
	/**
	 * Add the tags button and subscribe to the events
	 */
	private void addTagsButton(){
		final TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null){
			headingPanel.setTagsButton(new TagsMenuButton("Tag", false), requestModel);
			observable.subscribe(TagRequests.class, new ITbitsEventHandle<TagRequests>(){
				public void handleEvent(TagRequests event) {
					if(!isVisible())
						return;
					
					ArrayList<Integer> requests = new ArrayList<Integer>();
					requests.add(requestModel.getRequestId());
					if(event.getAction() == TagRequests.APPLY)
						TagsUtils.applyTag(ClientUtils.getCurrentUser(), ClientUtils.getCurrentBA(), requests, event.getTag(), event.getTagType());
					else
						TagsUtils.removeTagFromRequests(ClientUtils.getCurrentUser(), ClientUtils.getCurrentBA(), requests, event.getTag(), event.getTagType());
				}});
		}
	}
	
}
