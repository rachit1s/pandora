package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.IUploadStatus;

public interface ITbitsUploadStatus extends IUploadStatus{
	public void setUploadProgress(IUploadProgress progress);
	public void setFileSize(int size);
	public void setRepoFileId(int repoFileId);
}
