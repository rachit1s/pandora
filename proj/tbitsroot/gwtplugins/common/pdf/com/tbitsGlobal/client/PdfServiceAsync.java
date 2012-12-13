package pdf.com.tbitsGlobal.client;

import java.util.ArrayList;

import pdf.com.tbitsGlobal.shared.PDFConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PdfServiceAsync {

	void generatePdf(int systemId, int requestId, AsyncCallback<String> callback);

	void getPdfConfigList(AsyncCallback<ArrayList<PDFConfig>> callback);

}
