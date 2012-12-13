package pdf.com.tbitsGlobal.client;

import java.util.ArrayList;

import pdf.com.tbitsGlobal.shared.PDFConfig;

import com.google.gwt.user.client.rpc.RemoteService;

public interface PdfService extends RemoteService {
	
	String generatePdf(int systemId, int requestId);
	
	ArrayList<PDFConfig> getPdfConfigList();

}
