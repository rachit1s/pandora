/**
 * 
 */
package dcn.com.tbitsGlobal.client.plugins;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

import dcn.com.tbitsGlobal.client.ChangeNoteConstants;
import dcn.com.tbitsGlobal.client.utils.ChangeNoteClientUtils;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author lokesh
 *
 */
public class ChangeNoteWizardPlugin implements IWizardPlugin {
		 
	String baType;
	String caption;
	ChangeNoteConfig changeNoteConfig;
	ArrayList<ChangeNoteConfig> changeNoteConfigList;

	public ChangeNoteWizardPlugin(String baType, String caption){
		super();
		this.baType = baType;
		this.caption = caption;
	}
	
	public ChangeNoteWizardPlugin(String baType, String caption, ArrayList<ChangeNoteConfig> changeNoteConfigList){
		this.baType = baType;
		this.caption = caption;
		this.changeNoteConfigList = changeNoteConfigList;
	}

	@Override
	public String getButtonCaption() {
		return caption;
	}

	@Override
	public AbstractWizard getWidget(ArrayList<Integer> param) {
		
		final MessageBox messageBox = MessageBox.wait("Please Wait", 
				"Request is being processed", "Please Wait...");		
		
		ChangeNoteConstants.dcnService.getDCNRequest(param, changeNoteConfig, new AsyncCallback<RequestData>(){

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				System.out.println(caught.getMessage());
				Window.alert("Some error occurred while creating " + baType);
				messageBox.close();					
			}

			@Override
			public void onSuccess(RequestData requestData) {
				TbitsMainTabPanel tb = JaguarConstants.jaguarTabPanel;
				messageBox.close();
				tb.addNewRequestFormTab(requestData);						
			}				
		});

		return null;
	}

	@Override
	public boolean shouldExecute(final String sysPrefix) {
		
		ChangeNoteConfig cnc = ChangeNoteClientUtils.getChangeNoteConfigFromListUsingSrcSysPrefixAndBAType(
				ChangeNoteConstants.changeNoteConfigList, sysPrefix, baType);
		if (cnc != null){
			changeNoteConfig = cnc;
			return true;
		}
		return false;
	}
}
