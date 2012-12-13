package com.tbitsGlobal.admin.client.widgets;


import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.APLinks;
import com.tbitsGlobal.admin.client.widgets.pages.BAPropertiesView;
import com.tbitsGlobal.admin.client.widgets.pages.BAUserView;
import com.tbitsGlobal.admin.client.widgets.pages.CaptionsView;
import com.tbitsGlobal.admin.client.widgets.pages.CategoriesView;
import com.tbitsGlobal.admin.client.widgets.pages.DisplayGroupView;
import com.tbitsGlobal.admin.client.widgets.pages.EscalationView;
import com.tbitsGlobal.admin.client.widgets.pages.FieldControlView;
import com.tbitsGlobal.admin.client.widgets.pages.FieldPermissionsView;
import com.tbitsGlobal.admin.client.widgets.pages.FieldPropertiesView;
import com.tbitsGlobal.admin.client.widgets.pages.RolesView;

/**
 * Container under the 'BA Specific' heading in the left panel
 *
 */
public class BAPageContainer extends ContentPanel {
	public BAPageContainer() {
		super();
		
		this.setHeaderVisible(false);
		this.setLayout(new RowLayout());
		this.setScrollMode(Scroll.AUTOY);
	}

	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		fill();
	}

	// Method to put all fields in the 'containers' variable
	private void fill() {

		RowData rowData = new RowData();
		rowData.setWidth(1);
		
		// Drop down Combo for selecting Business Area
		BACombo comboWrapper = new BACombo();
//		add(comboWrapper, rowData);
		comboWrapper.setExpanded(true);
		
		
		ContentPanel cp = new ContentPanel();
		cp.setBodyBorder(false);
		cp.setHeaderVisible(false);
		cp.setExpanded(true);
		cp.setLayout(new RowLayout());
		
		cp.add(comboWrapper, rowData);
//		this.add(cp);
		

		// BA properties : link for properties tab of the selected BA
		APPageLink baPropertiesLink = new APPageLink(APLinks.BA_PROPERTIES){
			public APTabItem getPage() {
				return new BAPropertiesView(linkIdentifier);
			}};
		add(baPropertiesLink, rowData);

		APPageLink fieldWrapper = new APPageLink(APLinks.ADMIN_FIELD){
			public APTabItem getPage() {
				return new FieldPropertiesView(linkIdentifier);
			}};
		add(fieldWrapper, rowData);

		APPageLink fieldPermissions = new APPageLink(APLinks.FIELD_PERMISSIONS){
			public APTabItem getPage() {
				return new FieldPermissionsView(linkIdentifier);
			}};
		add(fieldPermissions, rowData);

		APPageLink fieldControls	= new APPageLink(APLinks.FIELD_CONTROLS){
			public APTabItem getPage() {
				return new FieldControlView(linkIdentifier);
			}
		};
		add(fieldControls, rowData);
		
		APPageLink DisplayGroupWrapper = new APPageLink(APLinks.DISPLAY_GROUP){
			public APTabItem getPage() {
				return new DisplayGroupView(linkIdentifier);
			}};
		add(DisplayGroupWrapper, rowData);

		APPageLink BAUserWrapper = new APPageLink(APLinks.BA_USER){
			public APTabItem getPage() {
				return new BAUserView(linkIdentifier);
			}};
		add(BAUserWrapper, rowData);
		
		APPageLink RolesWrapper = new APPageLink(APLinks.ROLES){
			public APTabItem getPage() {
				return new RolesView(linkIdentifier);
			}};
		add(RolesWrapper, rowData);

		APPageLink categoriesWrapper = new APPageLink(APLinks.CATEGORIES){
			public APTabItem getPage() {
				return new CategoriesView(linkIdentifier);
			}};
		add(categoriesWrapper, rowData);

		APPageLink escalationWrapper = new APPageLink(APLinks.ESCALATIONS){
			public APTabItem getPage() {
				return new EscalationView(linkIdentifier);
			}};
		add(escalationWrapper, rowData);

		APPageLink captionsWrapper = new APPageLink(APLinks.CAPTIONS){
			public APTabItem getPage() {
				return new CaptionsView(linkIdentifier);
			}};
		add(captionsWrapper, rowData);
	}
}
