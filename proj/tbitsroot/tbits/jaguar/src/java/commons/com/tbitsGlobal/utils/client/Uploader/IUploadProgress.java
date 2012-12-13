package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.IUploadStatus.UploadCancelHandler;

import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public interface IUploadProgress {
	public void setProgress(FileClient att, int done, int total);
	public void addCancelHandler(FileClient att, final UploadCancelHandler handler);
	public FileClient addAttachment();
	public void updateAttachment(FileClient att);
	public void updateAttachment(FileClient att, boolean callListeners);
}
