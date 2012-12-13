package admin.com.tbitsglobal.admin.client;

import admin.com.tbitsglobal.admin.client.trn.tabs.AttachmentListTab;
import admin.com.tbitsglobal.admin.client.trn.tabs.CreateModifyProcessTab;
import admin.com.tbitsglobal.admin.client.trn.tabs.DistListTab;
import admin.com.tbitsglobal.admin.client.trn.tabs.MappingTab;
import admin.com.tbitsglobal.admin.client.trn.tabs.PostProcessValuesTab;
import admin.com.tbitsglobal.admin.client.trn.tabs.ProcessParamsTab;

import com.extjs.gxt.ui.client.widget.TabPanel;

public class MainTabPanel extends TabPanel {
//	private HashMap<Class<? extends TabItem>, TabItem> tabMap;
	
	public MainTabPanel() {
		super();
		this.setTabScroll(true);
		
//		tabMap = new HashMap<Class<? extends TabItem>, TabItem>();
		
		this.addCreateProcessTab();
		this.addProcessParamsTab();
		this.addPostTrnValuesTab();
		this.addSrcTargetFieldMappingTab();
		this.addDistributionListTab();
		this.addAttachmentListTab();
	}
	
	public void addCreateProcessTab(){
		this.add(new CreateModifyProcessTab());
	}
	
	public void addPostTrnValuesTab(){
		this.add(new PostProcessValuesTab());
	}
	
	public void addSrcTargetFieldMappingTab(){
		this.add(new MappingTab());
	}
	
	public void addProcessParamsTab(){
		this.add(new ProcessParamsTab());
	}
	
	public void addDistributionListTab(){
		this.add(new DistListTab());
	}
	
	public void addAttachmentListTab(){
		this.add(new AttachmentListTab());
	}
	
//	private TabItem displayTab(Class<? extends TabItem> clazz, TabItem tab){
//		if(tabMap.containsKey(clazz)){
//			tab = tabMap.get(clazz);
//			if(tab != null){
//				if(!tab.isAttached())
//					this.add(tab);
//				
//				this.setSelection(tab);
//				return tab;
//			}
//		}
//		return null;
//	}
//	
//	private void addNewTab(Class<? extends TabItem> clazz, TabItem tab){
//		tabMap.put(clazz, tab);
//		this.add(tab);
//		this.setSelection(tab);
//	}
}
