package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.tbitsGlobal.admin.client.events.OnPageRequest;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APPageLink;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.MainContainer;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;

/**
 * This class consists of the links which appear in the 'Correspondence' 
 * window of the left side of the admin panel. This is a singleton class.
 * @author devashish
 *
 */
public class CorrLinks{
	
	protected static CorrLinks self;
	
	protected TbitsObservable observable;
	
	protected static List<APPageLink> corrLinksList = new ArrayList<APPageLink>();
	
	CorrLinks() {

		observable = new BaseTbitsObservable();
		observable.attach();
		
		initCorrProperties();
		
		initCorrProtocolProperties();	
		
		initCorrReportMap();
		
		initCorrReportNameMap();
		
		initCorrReportParamsMap();
		
		initBAFieldMap();
		
		initFieldNameMap();
		
		initOnBehalfMap();
		
		initUserMap();
		
		initCorrNumberConfig();
		
	}
	
	private void initCorrNumberConfig() {
		APPageLink corrNumberConfigLink = new APPageLink(CorrConstants.CORR_NUMBER_CONFIG){
			public APTabItem getPage() {
				return new CorrNumberConfigView(linkIdentifier);
			}
		};
		corrLinksList.add(corrNumberConfigLink);
	}

	protected void initUserMap(){
		
		APPageLink userMapLink 	= new APPageLink(CorrConstants.USER_MAP){
			public APTabItem getPage() {
				return new UserMapView(linkIdentifier);
			}
		};
		corrLinksList.add(userMapLink);
	}
	
	/**
	 * Initializes the link for onbehalf map
	 */
	protected void initOnBehalfMap(){
		APPageLink onBehalfMapLink = new APPageLink(CorrConstants.ON_BEHALF_MAP){
			public APTabItem getPage() {
				return new OnBehalfMapView(linkIdentifier);
			}
		};
		corrLinksList.add(onBehalfMapLink);
	}
	
	/**
	 * Initializes the link for field name map
	 */
	protected void initFieldNameMap(){
		APPageLink fieldNameMapLink = new APPageLink(CorrConstants.FIELD_NAME_MAP){
			public APTabItem getPage() {
				return new FieldNameMapView(linkIdentifier);
			}
		};
		corrLinksList.add(fieldNameMapLink);
	}
	
	/**
	 * Initializes the link for BA field map
	 */
	protected void initBAFieldMap(){
		APPageLink baFieldMapLink = new APPageLink(CorrConstants.BA_FIELD_MAP){
			public APTabItem getPage() {
				return new BAFieldMapView(linkIdentifier);
			}
		};
		corrLinksList.add(baFieldMapLink);
	}
	
	/**
	 * Initializes the link for correspondence report params map
	 */
	protected void initCorrReportParamsMap(){
		APPageLink corrReportParamsLink = new APPageLink(CorrConstants.CORR_REPORT_PARAM_NAME_MAP){
			public APTabItem getPage() {
				return new ReportParamMapView(linkIdentifier);
			}
		};
		corrLinksList.add(corrReportParamsLink);
	}
	
	/**
	 * Initializes the link for correspondence report name map
	 */
	protected void initCorrReportNameMap(){
		APPageLink corrReportNameLink = new APPageLink(CorrConstants.CORR_REPORT_NAME_MAP){
			public APTabItem getPage() {
				return new ReportNameMapView(linkIdentifier);
			}
		};
		corrLinksList.add(corrReportNameLink);
	}
	
	/**
	 * Initializes the link for correspondence report map
	 */
	protected void initCorrReportMap(){
		
		APPageLink corrReportMapLink = new APPageLink(CorrConstants.CORR_REPORT_MAP){
			public APTabItem getPage() {
				return new ReportMapView(linkIdentifier);
			}
			
		};
		corrLinksList.add(corrReportMapLink);
	}
	
	/**
	 * Initialized the link for correspondence protocol properties
	 */
	protected void initCorrProtocolProperties(){
		
		APPageLink corrProtocolOptionLink = new APPageLink(CorrConstants.CORR_PROTOCOL_OPTIONS){
			public APTabItem getPage() {
				return new ProtocolOptionsView(linkIdentifier);
			}
		};
		corrLinksList.add(corrProtocolOptionLink);
	}
	
	/**
	 * Initializes the link for correspondence properties.
	 */
	protected void initCorrProperties(){
		
		APPageLink corrPropertiesLink = new APPageLink(CorrConstants.CORR_PROPERTIES){
			public APTabItem getPage() {
				return new CorrPropertiesView(linkIdentifier);
			}
			
		};
		corrLinksList.add(corrPropertiesLink);

	}
	
	public static CorrLinks getInstance(){
		if(self == null)
			self = new CorrLinks();
		return self;
	}
}
