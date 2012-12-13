/**
 * 
 */
package pdf.com.tbitsGlobal.client;

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

/**
 * @author Lokesh
 *
 */
public class ViewRequestForm extends RequestView {

	private static final String GENERATE_PDF = "Generate PDF";
	private Integer currentSysId = 0;
	private Integer requestId = 0;
	MessageBox waitBox;
	
	/**
	 * @param parentContext
	 */
	public ViewRequestForm(UIContext parentContext) {		
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
							
						generatePdf(currentSysId, requestId);	
						waitBox.close();
					}
				}				
			});
			actionsButton.getMenu().add(pdfMenuItem);
		}
	}
	
	private void generatePdf ( final Integer currentSysId,
			final Integer requestId ) {

		PdfConstants.pdfService.generatePdf(currentSysId, requestId,
				new AsyncCallback<String>(){

			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.info("Error while generating pdf for request: " + requestId + " in BA: " + currentSysId );			
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
