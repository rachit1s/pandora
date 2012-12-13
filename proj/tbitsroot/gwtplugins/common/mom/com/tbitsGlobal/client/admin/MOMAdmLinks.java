package mom.com.tbitsGlobal.client.admin;

import java.util.ArrayList;
import java.util.List;

import mom.com.tbitsGlobal.client.MOMAdminConstants;

import com.tbitsGlobal.admin.client.widgets.APPageLink;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

/**
 * Initialize the links for the pages in mom admin panel
 * @author devashish
 *
 */
public class MOMAdmLinks {
	protected static MOMAdmLinks self;
	
	private static List<APPageLink> momLinkList = new ArrayList<APPageLink>();
	
	private MOMAdmLinks(){
		initMomTemplateLink();
	}
	
	protected void initMomTemplateLink(){
		APPageLink momTemplateLink = new APPageLink(MOMAdminConstants.MOM_TEMPLATES){
			public APTabItem getPage() {
				return new MOMTemplatesView(MOMAdminConstants.MOM_TEMPLATES);
			}
		};
		momLinkList.add(momTemplateLink);
	}
	
	public static MOMAdmLinks getInstance(){
		if(self == null)
			self = new MOMAdmLinks();
		return self;
	}
	
	public List<APPageLink> getPageList(){
		return momLinkList;
	}
}
