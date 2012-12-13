package com.tbitsGlobal.admin.client.widgets;



import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.APLinks;
import com.tbitsGlobal.admin.client.widgets.pages.AppPropertiesView;
import com.tbitsGlobal.admin.client.widgets.pages.EscalationConditionMapView;
import com.tbitsGlobal.admin.client.widgets.pages.EscalationHierarchyMapView;
import com.tbitsGlobal.admin.client.widgets.pages.HolidayListView;
import com.tbitsGlobal.admin.client.widgets.pages.JobListView;
import com.tbitsGlobal.admin.client.widgets.pages.MailingListView;
import com.tbitsGlobal.admin.client.widgets.pages.PermissioningToolView;
import com.tbitsGlobal.admin.client.widgets.pages.PublicTagsView;
import com.tbitsGlobal.admin.client.widgets.pages.ReportsView;
import com.tbitsGlobal.admin.client.widgets.pages.RulesEditorView;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.AllUsersViewBulkUpdate;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.BAMenuView;

/**
 * Container under the 'tBits Administration' heading in the left panel
 * This contains the links for editing global options for tbits application
 *
 */
public class UniversalPageContainer extends ContentPanel {
	public UniversalPageContainer() {
		super();
		this.setHeading("tBits Administration");
	}

	public void onRender(Element parent, int pos){
		super.onRender(parent, pos);

		APPageLink baMenuLink = new APPageLink(APLinks.BA_MENU){
			public APTabItem getPage() {
				return new BAMenuView(linkIdentifier);
			}};
		this.add(baMenuLink);
		
		APPageLink reportLink = new APPageLink(APLinks.REPORTS){
			public APTabItem getPage() {
				return new ReportsView(linkIdentifier);
			}};
		this.add(reportLink);

		APPageLink propertiesLink = new APPageLink(APLinks.APP_PROPERTIES){
			public APTabItem getPage() {
				return new AppPropertiesView(linkIdentifier);
			}};
		this.add(propertiesLink);
		
		APPageLink jobLink = new APPageLink(APLinks.JOB_LIST){
			public APTabItem getPage() {
				return new JobListView(linkIdentifier);
			}};
		this.add(jobLink);
	
/* removeing the incomplete escalation as pre request from GMR		
		APPageLink escHrLink = new APPageLink(APLinks.ESCALATION_HIERARCHY){
			public APTabItem getPage() {
				return new EscalationHierarchyMapView(linkIdentifier);
			}};
		this.add(escHrLink);
		
		APPageLink escCondink = new APPageLink(APLinks.ESCALATION_CONDITIONS){
			public APTabItem getPage() {
				return new EscalationConditionMapView(linkIdentifier);
			}};
		this.add(escCondink);
		*/

		APPageLink mailListLink = new APPageLink(APLinks.MAILING_LIST){
			public APTabItem getPage() {
				return new MailingListView(linkIdentifier);
			}};
		this.add(mailListLink);

		APPageLink allUsersLink = new APPageLink(APLinks.ALL_USERS){
			public APTabItem getPage() {
				return new AllUsersViewBulkUpdate(linkIdentifier);
			}};
		this.add(allUsersLink);
		
		APPageLink rulesEditorLink = new APPageLink(APLinks.RULES_EDITOR){
			public APTabItem getPage() {
				return new RulesEditorView(linkIdentifier);
			}};
		this.add(rulesEditorLink);
		
		APPageLink permissioningToolLink = new APPageLink(APLinks.PERM_TOOL){
			public APTabItem getPage() {
				return new PermissioningToolView(linkIdentifier);
			}};
		this.add(permissioningToolLink);
		
		APPageLink publicTagsLink = new APPageLink(APLinks.PUBLIC_TAGS){
			public APTabItem getPage() {
				return new PublicTagsView(linkIdentifier);
			}};
		this.add(publicTagsLink);
		
		APPageLink holidayListLink = new APPageLink(APLinks.HOLIDAY_LIST){
			public APTabItem getPage() {
				return new HolidayListView(linkIdentifier);
			}};
		this.add(holidayListLink);
	}
}
