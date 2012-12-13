/**
 * 
 */
package pdf.com.tbitsGlobal.client;

import java.util.ArrayList;

import pdf.com.tbitsGlobal.shared.PDFConfig;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

/**
 * @author Lokesh
 *
 */
public class ViewRequestFormPlugin implements EntryPoint, IViewRequestFormPlugin {
	
	ArrayList<PDFConfig> pdfConfigList = null;
	
	/* (non-Javadoc)
	 * @see commons.com.tbitsGlobal.utils.client.plugins.IGWTPlugin#getWidget(java.lang.Object)
	 */
	@Override
	public AbstractViewRequestForm getWidget(UIContext param) {	
		return new ViewRequestForm(param);
	}

	/* (non-Javadoc)
	 * @see commons.com.tbitsGlobal.utils.client.plugins.IGWTPlugin#shouldExecute(java.lang.String)
	 */
	@Override
	public boolean shouldExecute(String sysPrefix) {
		return isExistsInPdfConfigList (sysPrefix);
	}	

	private boolean isExistsInPdfConfigList(String sysPrefix) {
		BusinessAreaClient bac = ClientUtils.getBAbySysPrefix(sysPrefix);
		if ((bac != null) && (pdfConfigList != null)){
			for (PDFConfig pdfConfig : pdfConfigList){
				if (bac.getSystemId() == pdfConfig.getSystemId())
					return true;
			}
		}
		return false;
	}

	@Override
	public void onModuleLoad() {
		//Register service
		((ServiceDefTarget)PdfConstants.pdfService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		//Add plugin
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IViewRequestFormPlugin.class, this);
		
		PdfConstants.pdfService.getPdfConfigList(new AsyncCallback<ArrayList<PDFConfig>>(){

			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.info("Error occurred while fecthing Pdf Config list. " + caught.getMessage());
			}

			@Override
			public void onSuccess(ArrayList<PDFConfig> result) {
				if (result != null){
					pdfConfigList = result;
				}
			}
			
		});
		
	}
	
	
}
