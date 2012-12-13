/**
 * 
 */
package dcn.com.tbitsGlobal.client.plugins.form;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.widgets.forms.RequestView;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import dcn.com.tbitsGlobal.client.ChangeNoteConstants;
import dcn.com.tbitsGlobal.client.utils.ChangeNoteClientUtils;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteViewRequestForm extends RequestView {

	private static final String GENERATE_PDF = "Generate PDF";
	private Integer currentSysId = 0;
	private Integer requestId = 0;
	MessageBox waitBox;
	
	/**
	 * @param parentContext
	 */
	public ChangeNoteViewRequestForm(UIContext parentContext) {		
		super(parentContext);
		insertGeneratePDFButtonAsMenuItem();
	}
	
	private void insertGeneratePDFButtonAsMenuItem() {
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if (requestModel != null){
			
			currentSysId  = (Integer)requestModel.get(BUSINESS_AREA);
			requestId = (Integer)requestModel.get(REQUEST);
						
			MenuItem pdfMenuItem = new MenuItem(GENERATE_PDF, new SelectionListener<MenuEvent>(){
				@Override
				public void componentSelected(MenuEvent ce) 
				{
					boolean isGenerate = Window.confirm( "Do you want to generate pdf?" );
					if( isGenerate )
					{	
						waitBox = MessageBox.wait("Please wait", "Generating pdf", "Please wait...");			
						waitBox.show();
						ChangeNoteConfig changeNoteConfig = ChangeNoteClientUtils.getChangeNoteConfigFromListUsingTargetSysPrefix(
												ChangeNoteConstants.changeNoteConfigList, ChangeNoteViewRequestForm.this.getData().getSysPrefix());
						
						generatePdf(currentSysId, requestId, changeNoteConfig);	
						waitBox.close();
					}
				}				
			});
			actionsButton.getMenu().add(pdfMenuItem);
		}
	}
	
	private void generatePdf ( Integer currentSysId,
			Integer requestId, final ChangeNoteConfig cnc ) {

		ChangeNoteConstants.dcnService.generatePdf(currentSysId, requestId, cnc, 
				new AsyncCallback<String>(){

			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.info("Error while generating pdf in : " + cnc.getTargetSysPrefix());			
			}
			@Override
			public void onSuccess(String pdfUrl) {
				if ((pdfUrl != null) && (pdfUrl.trim().length() != 0)){							
					Window.open(pdfUrl , "_blank", "");									
				}
				else
					Window.alert("Could not generate pdf file.");
			}

		});
	}	
}
