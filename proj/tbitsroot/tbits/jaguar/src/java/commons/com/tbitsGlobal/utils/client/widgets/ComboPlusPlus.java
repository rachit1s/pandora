/**
 * 
 */
package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionProvider;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
/**
 * A text Area widget like the one you use in Gmail's 'Compose To' box
 * One can use displayTemplate method to set template which will be used
 * to define how selected
 * models will be represented in text area
 * 
 * @param <D> the model data type
 */
public class ComboPlusPlus<D extends ModelData> extends TextField<String> implements SelectionProvider<D> {

	/**
	 * ComboBox error messages.
	 */
	public class ComboBoxMessages extends TextFieldMessages {

		private String valueNoutFoundText;

		/**
		 * Returns the value not found error text.
		 * 
		 * @return the error text
		 */
		public String getValueNoutFoundText() {
			return valueNoutFoundText;
		}

		/**
		 * When using a name/value combo, if the value passed to setValue is not
		 * found in the store, valueNotFoundText will be displayed as the field text
		 * if defined.
		 * 
		 * @param valueNoutFoundText
		 */
		public void setValueNoutFoundText(String valueNoutFoundText) {
			this.valueNoutFoundText = valueNoutFoundText;
		}

	}

	/**
	 * TriggerAction enum.
	 */
	public enum TriggerAction {
		QUERY, ALL;
	}

	protected ListStore<D> store;
	protected boolean autoComplete = false;

	private String listStyle = "x-combo-list";
	private String selectedStyle = "x-combo-selected";
	private boolean forceSelection;
	private String listAlign = "tl-bl?";
	private int maxHeight = 300;
	private int minListWidth = 70;
	private BaseEventPreview eventPreview;
	private boolean expanded;
	private D selectedItem;
	private StoreListener<D> storeListener;
	private XTemplate template;
	private ListView<D> listView;
	private LayoutContainer list;
	//	private boolean typeAhead;
	private int queryDelay = 500;
	private int typeAheadDelay = 250;
	private int minChars = 4;
	private String lastQuery, allQuery = "";
	//	private DelayedTask taTask, dqTask;
	private DelayedTask dqTask;
	private TriggerAction triggerAction = TriggerAction.QUERY;
	private String mode = "remote";
	private String itemSelector;
	private String loadingText = "Loading...";
	private int pageSize;
	private int assetHeight;
	private El footer;
	private PagingToolBar pageTb;
	private boolean lazyRender, initialized;

	private boolean isEditable;
	private String displayField;
	private InputElement hiddenInput;
	private String valueField;

	private StringBuffer rawValueSnapshot = new StringBuffer();
	private ArrayList<String> filterProperties = new ArrayList<String>();
	private ListStorePropertyEditor<D> myPropertyEditor;

	/**
	 * Creates a combo box.
	 */
	public ComboPlusPlus() {
		messages = new ComboBoxMessages();
		listView = new ListView<D>();
		isEditable = true;
		myPropertyEditor = new ListStorePropertyEditor<D>();
		myPropertyEditor.setDelimeter(",");		//Key code of "," is hard-coded in onKeyUp method
		//take care of that when changing this
		monitorWindowResize = true;
		windowResizeDelay = 0;
		initComponent();
	}

	public void setMyPropertyEditor(ListStorePropertyEditor<D> listStorePropertyEditor) {
		this.myPropertyEditor = listStorePropertyEditor;
	}

	public ListStorePropertyEditor<D> getMyPropertyEditor() {
		return this.myPropertyEditor;
	}

	public void setNeedsSeparator(boolean needsSeparator) {
		this.myPropertyEditor.getModelPropertyEditor().setNeedsSeparator(needsSeparator);
	}

	public boolean getNeedsSeparator() {
		return this.myPropertyEditor.getModelPropertyEditor().getNeedsSeparator();
	}
	/**
	 * The underlying data field name to bind to this ComboBox (defaults to
	 * 'text').
	 * 
	 * @param displayField
	 *            the display field
	 */
	public void setDisplayField(String displayField) {
		if (displayField == null || displayField.trim().equals(""))
			return;
		this.displayField = displayField;
		filterProperties.add(displayField);
		myPropertyEditor.setDisplayProperty(displayField);
	}

	public void addFilterField(String filterField) {
		if (filterField == null || filterField.trim().equals(""))
			return;
		filterProperties.add(filterField);
	}

	public String[] getFilterFields() {
		if (this.filterProperties == null)
			return null;
		return this.filterProperties.toArray(new String[0]);
	}

	public void addSelectionChangedListener(SelectionChangedListener<D> listener) {
		addListener(Events.SelectionChange, listener);
	}

	public boolean isEditable(){
		return isEditable;
	}

	public List<D> getSelection() {
		assert this.store != null : "Store can not be null at this point";
		if (this.store.isFiltered())
			this.store.clearFilters();
		this.myPropertyEditor.setList(this.store.getModels());
		ListStore<D> temp = this.myPropertyEditor.convertStringValue(this.getValue());
		if (temp != null)
			return temp.getModels();
		return null;
	}

	/**
	 * Clears any text/value currently set in the field.
	 */
	public void clearSelections() {
		addToRawValue("");
		applyEmptyText();
		value = null;
	}

	/**
	 * Hides the dropdown list if it is currently expanded. Fires the
	 * <i>Collapse</i> event on completion.
	 */
	public void collapse() {
		if (!expanded) {
			return;
		}
		eventPreview.remove();
		expanded = false;
		list.hide();
		RootPanel.get().remove(list);

		fireEvent(Events.Collapse, new FieldEvent(this));
	}

	/**
	 * Execute a query to filter the dropdown list. Fires the BeforeQuery event
	 * prior to performing the query allowing the query action to be canceled if
	 * needed.
	 * 
	 * @param q the query
	 * @param forceAll true to force the query to execute even if there are
	 *          currently fewer characters in the field than the minimum specified
	 *          by the minChars config option. It also clears any filter
	 *          previously saved in the current store
	 */
	public void doQuery(String q, boolean forceAll) {
		if (q == null) {
			q = "";
		}

		FieldEvent fe = new FieldEvent(this);
		fe.setValue(q);
		if (!fireEvent(Events.BeforeQuery, fe)) {
			return;
		}

		if (forceAll || q.length() >= minChars) {
			if (lastQuery == null || !lastQuery.equals(q)) {
				lastQuery = q;
				if (mode.equals("local")) {
					selectedItem = null;
					if (forceAll) {
						store.clearFilters();
					} else {
						store.filter(getDisplayField(), q);
					}
					onLoad(null);
				} else {
					store.getLoader().load(getParams(q));
					expand();
				}
			} else {
				selectedItem = null;
				onLoad(null);
			}
		}
	}

	/**
	 * Expands the dropdown list if it is currently hidden. Fires the
	 * <i>expand</i> event on completion.
	 */
	public void expand() {
		if (expanded || !hasFocus) {
			return;
		}
		expanded = true;

		if (!initialized) {
			createList(false);
		} else {
			RootPanel.get().add(list);
		}

		list.show();
		list.layout();
		list.el().updateZIndex(0);
		restrict();

		eventPreview.add();

		fireEvent(Events.Expand, new FieldEvent(this));
	}

	/**
	 * Returns the all query.
	 * 
	 * @return the all query
	 */
	public String getAllQuery() {
		return allQuery;
	}

	/**
	 * Returns the display field.
	 * 
	 * @return the display field
	 */
	public String getDisplayField() {
		return displayField;
	}

	/**
	 * Returns true if the field's value is forced to one of the value in the
	 * list.
	 * 
	 * @return the force selection state
	 */
	public boolean getForceSelection() {
		return forceSelection;
	}

	/**
	 * Returns the item selector.
	 * 
	 * @return the item selector
	 */
	public String getItemSelector() {
		return itemSelector;
	}

	/**
	 * Returns the list's list align value.
	 * 
	 * @return the list align value
	 */
	public String getListAlign() {
		return listAlign;
	}

	/**
	 * Returns the list style.
	 * 
	 * @return the list style
	 */
	public String getListStyle() {
		return listStyle;
	}

	/**
	 * Returns the combo's list view.
	 * 
	 * @return the view
	 */
	public ListView<D> getListView() {
		return listView;
	}

	/**
	 * Returns the loading text.
	 * 
	 * @return the loading text
	 */
	public String getLoadingText() {
		return loadingText;
	}

	/**
	 * Returns the dropdown list's max height.
	 * 
	 * @return the max height
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	@Override
	public ComboBoxMessages getMessages() {
		return (ComboBoxMessages) messages;
	}

	/**
	 * Returns the min characters used for autocompete and typeahead.
	 * 
	 * @return the minimum number of characters
	 */
	public int getMinChars() {
		return minChars;
	}

	/**
	 * Returns the dropdown list's min width.
	 * 
	 * @return the min width
	 */
	public int getMinListWidth() {
		return minListWidth;
	}

	/**
	 * Returns the page size.
	 * 
	 * @return the page size
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Returns the combo's paging tool bar.
	 * 
	 * @return the tool bar
	 */
	public PagingToolBar getPagingToolBar() {
		return pageTb;
	}
	//
	//  
	//  public ListModelmyPropertyEditor<D> getmyPropertyEditor() {
	//    return (ListModelmyPropertyEditor<D>) myPropertyEditor;
	//  }

	/**
	 * Returns the query delay.
	 * 
	 * @return the query delay
	 */
	public int getQueryDelay() {
		return queryDelay;
	}

	/**
	 * Returns the selected style.
	 * 
	 * @return the selected style
	 */
	public String getSelectedStyle() {
		return selectedStyle;
	}

	/**
	 * Returns the combo's store.
	 * 
	 * @return the store
	 */
	public ListStore<D> getStore() {
		return store;
	}

	/**
	 * Returns the custom template.
	 * 
	 * @return the template
	 */
	public XTemplate getTemplate() {
		return template;
	}

	/**
	 * Returns the trigger action.
	 * 
	 * @return the trigger action
	 */
	public TriggerAction getTriggerAction() {
		return triggerAction;
	}

	/**
	 * Returns the type ahead delay in milliseconds.
	 * 
	 * @return the type ahead delay
	 */
	public int getTypeAheadDelay() {
		return typeAheadDelay;
	}

	@Override
	public String getValue() {
		if (!initialized) {
			return value;
		}

		doForce();

		String v = super.getValue();
		// a value was set directly and there is not a
		// matching value in the drop down list
		String rv = getRawValue();
		boolean empty = rv == null || rv.equals("");
		if (rendered && !empty && v == null && value != null && !forceSelection) {
			return value;
		}
		return super.getValue();
	}

	/**
	 * Returns the combo's list view.
	 * 
	 * @return the view
	 */
	public ListView<D> getView() {
		return listView;
	}

	/**
	 * Returns the value field name.
	 * 
	 * @return the value field name
	 */
	public String getValueField() {
		return valueField;
	}

	/**
	 * Returns <code>true</code> if the panel is expanded.
	 * 
	 * @return the expand state
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * Returns true if lazy rendering is enabled.
	 * 
	 * @return true of lazy rendering
	 */
	public boolean isLazyRender() {
		return lazyRender;
	}

	//	/**
	//	 * Returns true if type ahead is enabled.
	//	 * 
	//	 * @return the type ahead state
	//	 */
	//	public boolean isTypeAhead() {
	//		return typeAhead;
	//	}

	public void removeSelectionListener(SelectionChangedListener<D> listener) {
		removeListener(Events.SelectionChange, listener);
	}

	/**
	 * Select an item in the dropdown list by its numeric index in the list. This
	 * function does NOT cause the select event to fire. The list must expanded
	 * for this function to work, otherwise use #setValue.
	 * 
	 * @param index the index of the item to select
	 */
	public void select(int index) {
		select(store.getAt(index));
	}

	public void select(D sel) {
		if (listView != null) {
			if (sel != null) {
				int index = store.indexOf(sel);
				selectedItem = sel;
				if (index < listView.getElements().size()) {
					listView.getSelectionModel().select(sel, false);
					fly(listView.getElement(index)).scrollIntoView(listView.getElement(), false);
				}
			}
		}
	}

	/**
	 * The text query to send to the server to return all records for the list
	 * with no filtering (defaults to '').
	 * 
	 * @param allQuery the all query
	 */
	public void setAllQuery(String allQuery) {
		this.allQuery = allQuery;
	}

	/**
	 * Sets the model field used to retrieve the "value" from the model. If
	 * specified, a hidden form field will contain the value. The hidden form
	 * field name will be the combo's field name plus "-hidden".
	 * 
	 * @param valueField the value field name
	 */
	public void setValueField(String valueField) {
		myPropertyEditor.setHiddenProperty(valueField);
		this.valueField = valueField;
	}


	/**
	 * Sets the panel's expand state.
	 * 
	 * @param expand <code>true<code> true to expand
	 */
	public void setExpanded(boolean expand) {
		this.expanded = expand;
		if (isRendered()) {
			if (expand) {
				expand();
			} else {
				collapse();
			}
		}
	}

	/**
	 * Sets whether the combo's value is restricted to one of the values in the
	 * list, false to allow the user to set arbitrary text into the field
	 * (defaults to false).
	 * 
	 * @param forceSelection true to force selection
	 */
	public void setForceSelection(boolean forceSelection) {
		this.forceSelection = forceSelection;
	}

	/**
	 * This setting is required if a custom XTemplate has been specified.
	 * 
	 * @param itemSelector the item selector
	 */
	public void setItemSelector(String itemSelector) {
		this.itemSelector = itemSelector;
	}

	/**
	 * True to lazily render the combo's drop down list (default to false,
	 * pre-render).
	 * 
	 * @param lazyRender true to lazy render the drop down list
	 */
	public void setLazyRender(boolean lazyRender) {
		this.lazyRender = lazyRender;
	}

	/**
	 * Sets a valid anchor position value. See {@link El#alignTo} for details on
	 * supported anchor positions (defaults to 'tl-bl?').
	 * 
	 * @param listAlign the new list align value
	 */
	public void setListAlign(String listAlign) {
		this.listAlign = listAlign;
	}

	/**
	 * Sets the style for the drop down list (defaults to 'x-combo-list');
	 * 
	 * @param listStyle the list style
	 */
	public void setListStyle(String listStyle) {
		this.listStyle = listStyle;
	}

	/**
	 * Sets the loading text.
	 * 
	 * @param loadingText the loading text
	 */
	public void setLoadingText(String loadingText) {
		this.loadingText = loadingText;
	}

	/**
	 * Sets the maximum height in pixels of the dropdown list before scrollbars
	 * are shown (defaults to 300).
	 * 
	 * @param maxHeight the max hieght
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 * Sets the minimum number of characters the user must type before
	 * autocomplete and typeahead active (defaults to 4 if remote, or 0 if local).
	 * 
	 * @param minChars
	 */
	public void setMinChars(int minChars) {
		this.minChars = minChars;
	}

	/**
	 * Sets the minimum width of the dropdown list in pixels (defaults to 70, will
	 * be ignored if listWidth has a higher value).
	 * 
	 * @param minListWidth the min width
	 */
	public void setMinListWidth(int minListWidth) {
		this.minListWidth = minListWidth;
	}

	/**
	 * Sets the page size. Only applies when using a paging toolbar.
	 * 
	 * @param pageSize the page size
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}


	/**
	 * The length of time in milliseconds to delay between the start of typing and
	 * sending the query to filter the dropdown list.
	 * 
	 * @param queryDelay the query delay
	 */
	public void setQueryDelay(int queryDelay) {
		this.queryDelay = queryDelay;
	}

	@Override
	public void setRawValue(String text) {
		if (rendered) {
			if (text == null) {
				String msg = getMessages().getValueNoutFoundText();
				text = msg != null ? msg : "";
			}
			getInputEl().setValue(text);
			updateSnapShot();
		}
	}

	/**
	 * Sets the CSS style name to apply to the selected item in the dropdown list
	 * (defaults to 'x-combo-selected').
	 * 
	 * @param selectedStyle the selected style
	 */
	public void setSelectedStyle(String selectedStyle) {
		this.selectedStyle = selectedStyle;
	}

	/**
	 *@param selection list of models which will be shown in text field 
	 */
	public void setSelection(List<D> selection) {
		if (selection.size() > 0) {
			ListStore<D> temp = new ListStore<D>();
			temp.add(selection);
			setValue(myPropertyEditor.getStringValue(temp));
		} else {
			setValue(null);
		}
	}

	/**
	 * @param model model to add to ComboPlusPlus text box
	 */
	public void addSelection(D model) {
		if (model == null)
			return;
		addToValue(model);
	}

	/**
	 * Sets the template fragment to be used for the text of each combo list item.
	 * 
	 * <pre>
	 * 
	 * &lt;code&gt; combo.setSimpleTemplate(&quot;{abbr} {name}&quot;); &lt;/code&gt;
	 * 
	 * </pre>
	 * 
	 * @param html the html used only for the text of each item in the list
	 */
	public void setSimpleTemplate(String html) {
		assertPreRender();
		html = "<tpl for=\".\"><div class=x-combo-list-item>" + html + "</div></tpl>";
		template = XTemplate.create(html);
	}

	/**
	 * Sets the combo's store.
	 * 
	 * @param store the store
	 */
	public void setStore(ListStore<D> store) {
		this.store = store;
	}

	/**
	 * Sets the custom template used to render the combo's drop down list.Use this
	 * to create custom UI layouts for items in the list.
	 * <p>
	 * If you wish to preserve the default visual look of list items, add the CSS
	 * class name 'x-combo-list-item' to the template's container element.
	 * 
	 * @param html the html
	 */
	public void setTemplate(String html) {
		assertPreRender();
		template = XTemplate.create(html);
	}

	/**
	 * Sets the custom template used to render the combo's drop down list.
	 * 
	 * @param template the template
	 */
	public void setTemplate(XTemplate template) {
		assertPreRender();
		this.template = template;
	}

	/**
	 * The action to execute when the trigger field is activated. Use
	 * {@link TriggerAction#ALL} to run the query specified by the allQuery config
	 * option (defaults to {@link TriggerAction#QUERY}).
	 * 
	 * @param triggerAction the trigger action
	 */
	public void setTriggerAction(TriggerAction triggerAction) {
		this.triggerAction = triggerAction;
	}

	//	/**
	//	 * True to populate and autoselect the remainder of the text being typed after
	//	 * a configurable delay ({@link #typeAheadDelay}) if it matches a known value
	//	 * (defaults to false)
	//	 * 
	//	 * @param typeAhead
	//	 */
	//	public void setTypeAhead(boolean typeAhead) {
	//		this.typeAhead = typeAhead;
	//	}
	//	
	public void setRawValueTemplate(String template) {
		this.myPropertyEditor.setTemplate(template);
	}

	public String getRawValueTemplate() {
		return this.myPropertyEditor.getTemplate();
	}
	/**
	 * The length of time in milliseconds to wait until the typeahead text is
	 * displayed if typeAhead = true (defaults to 250).
	 * 
	 * @param typeAheadDelay the type ahead delay
	 */
	public void setTypeAheadDelay(int typeAheadDelay) {
		this.typeAheadDelay = typeAheadDelay;
	}


	@Override
	public void setValue(String value) {
		super.setValue(value);
		updateHiddenValue();
		SelectionChangedEvent<D> se = new SelectionChangedEvent<D>(this, getSelection());
		fireEvent(Events.SelectionChange, se);
	}

	/**
	 * Sets the combo's view.
	 * 
	 * @param view the view
	 */
	public void setView(ListView<D> view) {
		this.listView = view;
	}

	protected void collapseIf(PreviewEvent pe) {
		if (!list.el().isOrHasChild(pe.getTarget())) {
			collapse();
		}
	}

	@Override
	protected void doAttachChildren() {
		super.doAttachChildren();
		if (pageTb != null && pageTb.isRendered()) {
			ComponentHelper.doAttach(pageTb);
		}
	}

	@Override
	protected void doDetachChildren() {
		super.doDetachChildren();
		if (list.isRendered()) {
			RootPanel.get().remove(list);
		}
		if (pageTb != null && pageTb.isRendered()) {
			ComponentHelper.doDetach(pageTb);
		}
	}

	protected void doForce() {
		if (forceSelection) {
			boolean f = forceSelection;
			forceSelection = false;
			String rv = getRawValue();
			if (getAllowBlank() && (rv == null || rv.equals(""))) {
				return;
			}
			forceSelection = f;
		}
	}

	protected D findModel(String property, String value) {
		if (value == null) return null;
		for (D model : store.getModels()) {
			if (value.equals(model.get(displayField).toString())) {
				return model;
			}
		}
		return null;
	}

	protected boolean delayedCheck;

	protected void unsetDelayCheck() {
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				delayedCheck = false;

			}

		});
	}

	protected void fireKey(FieldEvent fe) {
		if (fe.isNavKeyPress() && !isExpanded() && !delayedCheck) {
			fireEvent(Events.SpecialKey, fe);
		}
	}

	protected PagingLoadConfig getParams(String query) {
		BasePagingLoadConfig config = new BasePagingLoadConfig();
		config.setLimit(pageSize);
		config.setOffset(0);
		config.set("query", query);
		return config;
	}

	protected boolean hasFocus() {
		return hasFocus || expanded;
	}

	@SuppressWarnings("unchecked")
	protected void initComponent() {
		storeListener = new StoreListener<D>() {

			@Override
			public void storeBeforeDataChanged(StoreEvent<D> se) {
				onBeforeLoad(se);
			}

			@Override
			public void storeDataChanged(StoreEvent<D> se) {
				onLoad(se);
			}

		};

		eventPreview = new BaseEventPreview() {
			@Override
			protected boolean onPreview(PreviewEvent pe) {
				switch (pe.getType().getEventCode()) {
				case Event.ONSCROLL:
				case Event.ONMOUSEDOWN:
					collapseIf(pe);
				}
				return true;
			}
		};
		eventPreview.setAutoHide(false);

		new KeyNav(this) {
			public void onDown(ComponentEvent ce) {
				ce.cancelBubble();
				if (!isExpanded()) {
					onTriggerClick(ce);
				} else {
					selectNext();
				}
			}

			@Override
			public void onEnter(ComponentEvent ce) {
				if (expanded) {
					ce.cancelBubble();
					onViewClick(ce, false);
					//					delayedCheck = true;
					//					unsetDelayCheck();
				}
			}

			@Override
			public void onEsc(ComponentEvent ce) {
				if (expanded) {
					ce.cancelBubble();
					collapse();
				}
			}

			@Override
			public void onTab(ComponentEvent ce) {
				if (expanded) {
					onViewClick(ce, false);
				}
			}

			@Override
			public void onUp(ComponentEvent ce) {
				if (expanded) {
					ce.cancelBubble();
					selectPrev();
				}
			}

		};
	}

	@SuppressWarnings("unchecked")
	protected void initList() {
		if (listView == null) {
			listView = new ListView<D>();
		}

		String style = listStyle;
		listView.setStyleAttribute("overflowX", "hidden");
		listView.setStyleName(style + "-inner");
		listView.setStyleAttribute("padding", "0px");
		listView.setItemSelector(itemSelector != null ? itemSelector : "." + style + "-item");
		listView.setSelectOnOver(true);
		listView.setBorders(false);
		listView.setLoadingText(loadingText);
		listView.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<D>>() {
			public void handleEvent(SelectionChangedEvent<D> se) {
				selectedItem = listView.getSelectionModel().getSelectedItem();
			}
		});

		listView.addListener(Events.Select, new Listener<ListViewEvent>() {
			public void handleEvent(ListViewEvent le) {
				onViewClick(le, true);
			}
		});
		if (template == null) {
			String html = "<tpl for=\".\"><div class=\"" + style + "-item\">{" + getDisplayField() + "}</div></tpl>";
			template = XTemplate.create(html);
		}

		assetHeight = 0;

		list = new LayoutContainer() {
			@Override
			protected void onRender(Element parent, int index) {
				super.onRender(parent, index);
				eventPreview.getIgnoreList().add(getElement());
			}
		};
		list.setScrollMode(Scroll.NONE);
		list.setShim(true);
		list.setShadow(true);
		list.setBorders(true);
		list.setStyleName(style);
		list.hide();

		assert store != null : "ComboBox needs a store";

		list.add(listView);

		if (pageSize > 0) {
			pageTb = new PagingToolBar(pageSize);
			pageTb.bind((PagingLoader) store.getLoader());
		}

		if (!lazyRender) {
			createList(true);
		}

		listView.setStyleAttribute("backgroundColor", "white");
		listView.setTemplate(template);
		listView.setSelectStyle(selectedStyle);

		bindStore(store, true);
	}

	protected void onBeforeLoad(StoreEvent<D> se) {
		if (!hasFocus()) {
			return;
		}
		if (expanded) {
			//			this.el().dom.
			restrict();
		}
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		if (eventPreview != null) {
			eventPreview.remove();
		}
	}

	protected void onEmptyResults() {
		collapse();
	}

	@Override
	protected void onKeyDown(FieldEvent fe) {
		if (!fe.isSpecialKey()) {
			updateSnapShot();
		}
		if (fe.getKeyCode() == KeyCodes.KEY_TAB) {
			if (expanded) {
				onViewClick(fe, false);
			}
		} 
		if (fe.getKeyCode() == KeyCodes.KEY_ENTER || fe.getKeyCode() == KeyCodes.KEY_DOWN || fe.getKeyCode() == KeyCodes.KEY_UP) {
			if (expanded)
				fe.preventDefault();
		} 
		super.onKeyDown(fe);
	}

	@Override
	protected void onKeyUp(FieldEvent fe) {
		super.onKeyUp(fe);
		if (isEditable() && !fe.isSpecialKey()) {
			// last keys
			dqTask.delay(queryDelay);
		}
	}

	private void updateSnapShot() {
		rawValueSnapshot = new StringBuffer();
		rawValueSnapshot.append(getRawValue());
	}


	protected void onLoad(StoreEvent<D> se) {
		if (!isAttached() || !hasFocus()) {
			return;
		}
		if (store.getCount() > 0) {
			if (expanded) {
				restrict();
			} else {
				expand();
			}

			if (lastQuery != null && lastQuery.equals(allQuery)) {
				if (isEditable()) {
					selectTypeAhead(0);
				}
				//			} else {
				//				if (typeAhead) {
				//					taTask.delay(typeAheadDelay);
				//				}
			}
			if (!selectByValue(getEditingRawValue())) {
				select(0);
			}
		} else {
			onEmptyResults();
		}
	}

	protected void onRender(Element parent, int index) {
		setElement(DOM.createTextArea(), parent, index);
		super.onRender(parent, index);
		el().addEventsSunk(Event.KEYEVENTS);
		initList();
		if (!autoComplete) {
			getInputEl().dom.setAttribute("autocomplete", "off");
		}

		if (mode.equals("local")) {
			minChars = 0;
		}

		dqTask = new DelayedTask(new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				initQuery();
			}
		});

		if (valueField != null) {
			hiddenInput = Document.get().createHiddenInputElement().cast();
			hiddenInput.setName(getName() + "-hidden");
			getElement().appendChild(hiddenInput);
		}
		//
		//		if (typeAhead) {
		//			taTask = new DelayedTask(new Listener<BaseEvent>() {
		//				public void handleEvent(BaseEvent be) {
		//					onTypeAhead();
		//				}
		//			});
		//		}

		eventPreview.getIgnoreList().add(getElement());
	}

	protected void onSelect(D model, int index) {
		FieldEvent fe = new FieldEvent(this);
		if (fireEvent(Events.BeforeSelect, fe)) {
			addToValue(model);
			collapse();
			fireEvent(Events.Select, fe);
		}
	}

	public void addToValue(D model) {
		int i = getEditingRawValuePos();
		if (getRawValue().indexOf(this.myPropertyEditor.getDelimeter(), i) == -1) {
			setValue(getRawValue().substring(0, i) + this.myPropertyEditor.getModelPropertyEditor().getStringValue(model) + this.myPropertyEditor.getDelimeter());
		} else {
			String append = getRawValue().substring(getRawValue().indexOf(this.myPropertyEditor.getDelimeter(),i));
			setValue(getRawValue().substring(0, i) + this.myPropertyEditor.getModelPropertyEditor().getStringValue(model) + append);
		}
	}

	protected void onTriggerClick(ComponentEvent ce) {
		if (isReadOnly()) {
			return;
		}
		if (expanded) {
			collapse();
		} else {
			onFocus(null);
			if (triggerAction == TriggerAction.ALL) {
				doQuery(allQuery, true);
			} else {
				doQuery(getEditingRawValue(), true);
			}

		}
		getInputEl().focus();
	}

	//	protected void onTypeAhead() {
	//		if (store.getCount() > 0) {
	//			D m = store.getAt(0);
	//			String newValue = myPropertyEditor.getModelPropertyEditor().getStringValue(m);
	//			int len = newValue.length();
	//			int selStart = getEditingRawValue().length();
	//			if (selStart != len) {
	//				addToRawValue(newValue);
	//				selectTypeAhead(selStart);
	//			}
	//		}
	//	}

	protected void onViewClick(DomEvent de, boolean focus) {
		int idx = -1;
		// when testing in selenium the items will not be selected as the mouse
		// is not moved during the test
		Element elem = listView.findElement(de.getTarget());
		if (elem != null) {
			idx = listView.indexOf(elem);
		} else {
			D sel = listView.getSelectionModel().getSelectedItem();
			if (sel != null) {
				idx = store.indexOf(sel);
			}
		}
		if (idx != -1) {
			D sel = store.getAt(idx);
			onSelect(sel, idx);
		}

		if (focus) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					focus();
				}
			});
		}
	}

	protected void onWindowResize(int width, int height) {
		collapse();
	}

	@Override
	protected boolean validateValue(String value) {
		doForce();
		return super.validateValue(value);
	}

	private void bindStore(ListStore<D> store, boolean initial) {
		if (this.store != null && !initial) {
			this.store.removeStoreListener(storeListener);
			if (store == null) {
				this.store = null;
				if (listView != null) {
					listView.setStore(null);
				}
			}
		}
		if (store != null) {
			this.store = store;
			if (store.getLoader() == null) {
				mode = "local";
			}
			if (listView != null) {
				listView.setStore(store);
			}
			store.addStoreListener(storeListener);
		}
	}

	private void createList(boolean remove) {
		RootPanel.get().add(list);

		initialized = true;

		if (pageTb != null) {
			footer = list.el().createChild("<div class='" + listStyle + "-ft'></div>");
			pageTb.setBorders(false);
			pageTb.render(footer.dom);
			assetHeight += footer.getHeight();
		}

		if (remove) {
			RootPanel.get().remove(list);
		}
	}

	private void initQuery() {
		doQuery(getEditingRawValue(), false);
	}

	private void restrict() {
		list.el().setVisibility(false);
		listView.setHeight("auto");
		list.setHeight("auto");

		int fh = footer != null ? footer.getHeight() : 0;
		int fw = list.el().getFrameWidth("tb") + fh;

		int h = listView.getHeight() + fw;
		int w = Math.max(getWidth(), minListWidth);

		list.setSize(w, h);
		h = Math.min(h, maxHeight - fw);
		list.el().makePositionable(true);
		list.el().alignTo(getElement(), listAlign, null);
		h -= fh;
		listView.el().setStyleAttribute("overflow", "hidden");
		listView.syncSize();
		listView.setSize(w - list.el().getFrameWidth("lr"), h - list.el().getFrameWidth("tb"));

		int y = list.el().getY();
		int b = y + h;
		int vh = XDOM.getViewportSize().height + XDOM.getBodyScrollTop();
		if (b > vh) {
			y = y - (b - vh) - 5;
			list.el().setTop(y);
		}
		list.el().setVisibility(true);
	}

	private boolean selectByValue(String value) {
		D r = findModel(getDisplayField(), value);
		if (r != null) {
			select(r);
			return true;
		}
		return false;
	}

	private void selectNext() {
		int count = store.getCount();
		if (count > 0) {
			int selectedIndex = store.indexOf(selectedItem);
			if (selectedIndex == -1) {
				select(0);
			} else if (selectedIndex < count - 1) {
				select(selectedIndex + 1);
			}
		}
	}

	private void selectPrev() {
		int count = store.getCount();
		if (count > 0) {
			int selectedIndex = store.indexOf(selectedItem);
			if (selectedIndex == -1) {
				select(0);
			} else if (selectedIndex != 0) {
				select(selectedIndex - 1);
			}
		}
	}

	private void updateHiddenValue() {
		if (hiddenInput != null) {
			assert this.store != null : "Store can not be null at this point";
			if (this.store.isFiltered())
				this.store.clearFilters();
			this.myPropertyEditor.setList(this.store.getModels());

			String hiddenValue = "";
			String val = getValue();
			if (myPropertyEditor.getHiddenProperty() != null &&	!myPropertyEditor.getHiddenProperty().trim().equals("")) {
				hiddenValue = myPropertyEditor.getHiddenStringValue(myPropertyEditor.convertStringValue(val));
			} else {
				myPropertyEditor.setHiddenProperty(valueField);
				hiddenValue = myPropertyEditor.getHiddenStringValue(myPropertyEditor.convertStringValue(val));
			}
			hiddenInput.setValue(hiddenValue);
		}
	}

	public ArrayList<String> getRawValues() {
		String rawValue = this.getRawValue();
		String values[] = rawValue.split(myPropertyEditor.getDelimeter());
		ArrayList<String> trimmed = new ArrayList<String>();
		if (values == null || values.length == 0)
			return null;
		for (String v : values){
			if (v != null && !v.trim().equals("")) {
				trimmed.add(v.trim());
			}
		}
		return trimmed;
	}

	public String getEditingRawValue() {
		String rawValue = this.getRawValue();
		int i;
		for (i = 0; i < this.rawValueSnapshot.length(); i++) {
			if (rawValue.length() <= i)
				break;
			if (rawValue.charAt(i) == this.rawValueSnapshot.charAt(i))
				continue;
			break;
		}
		String subStr = this.rawValueSnapshot.substring(0, i);
		int lastDelimeter = subStr.lastIndexOf(this.myPropertyEditor.getDelimeter());
		if (rawValue.indexOf(this.myPropertyEditor.getDelimeter(), lastDelimeter + 1) == -1) {
			return rawValue.substring(lastDelimeter + 1).trim();
		} else {
			return rawValue.substring(lastDelimeter + 1, rawValue.indexOf(this.myPropertyEditor.getDelimeter(), lastDelimeter + 1)).trim();
		}
	}

	/**
	 * @return returns position of the start of editing rawValue 
	 */
	public int getEditingRawValuePos() {
		String rawValue = this.getRawValue();
		int i;
		for (i = 0; i < this.rawValueSnapshot.length(); i++) {
			if (rawValue.length() <= i)
				break;
			if (rawValue.charAt(i) == this.rawValueSnapshot.charAt(i))
				continue;
			break;
		}
		String subStr = this.rawValueSnapshot.substring(0, i);
		int beginIndex = subStr.lastIndexOf(this.myPropertyEditor.getDelimeter());
		while (rawValue.length() > ++beginIndex) {
			if ((((Character) rawValue.charAt(beginIndex)).toString().matches("[ \n]"))) {
				continue;
			}
			break;
		}
		return beginIndex;
	}

	private void selectTypeAhead(int selStart) {
		int i = getEditingRawValuePos();
		if (i + selStart >= getRawValue().length())
			return;
		if (getRawValue().indexOf(myPropertyEditor.getDelimeter(), i) == -1) {
			select(i + selStart, getRawValue().length() - i - selStart);
			return;
		} else {
			select(i + selStart, getRawValue().indexOf(myPropertyEditor.getDelimeter(), i) - i - selStart);
		}
	}

	//after adding must reset raw value
	public void addToRawValue(String newValue) {
		int begin = getEditingRawValuePos();
		if (getRawValue().indexOf(myPropertyEditor.getDelimeter(), begin) == -1) {
			String rawValue = getRawValue().substring(0, begin) + newValue;
			setRawValue(rawValue);
		} else {
			String rawValue = getRawValue().substring(0, begin) + newValue + getRawValue().substring(getRawValue().indexOf(myPropertyEditor.getDelimeter(), begin));
			setRawValue(rawValue);
		}
	}
}
