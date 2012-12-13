package commons.com.tbitsGlobal.utils.client.Uploader.gears;

import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.tbitsGlobal.uploader.gears.client.AbstractUploader;
import com.tbitsGlobal.uploader.gears.client.implementations.BaseUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListener;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListenerCallBack;
import commons.com.tbitsGlobal.utils.client.ClientUtils;

public class TbitsUploader extends AbstractUploader{
	protected String folderHint;
	protected int requestId;
	
	public TbitsUploader(String folderHint, int requestId, IUploadListener uploadListener) {
		super(uploadListener);
		
		this.folderHint = folderHint;
		this.requestId = requestId;
		servletUrl = ClientUtils.getUrlToFilefromBase("/uploader");
	}

	@Override
	protected String getQuery() {
		return "?requestid=" + requestId + "&folderhint=" + folderHint;
	}

	@Override
	protected IUploadHandler getUploadHandler(int uniqueId, File file,
			HttpRequest request) {
		IUploadHandler uploadHandler = new BaseUploadHandler(uniqueId, file, request);
		uploadHandler.setCallBack(new IUploadListenerCallBack(){
			public void onCancel(IUploadHandler uploadHandler) {
				uploadHandler.getRequest().abort();
			}

			public void onRepeat(IUploadHandler uploadHandler) {
				queue(uploadHandler.getFile());
			}});
		return new BaseUploadHandler(uniqueId, file, request);
	}

}
