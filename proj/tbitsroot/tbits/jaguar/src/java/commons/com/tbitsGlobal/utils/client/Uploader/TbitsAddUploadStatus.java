package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.IUploadStatus;

import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public class TbitsAddUploadStatus extends TbitsUploadStatus{

	public TbitsAddUploadStatus(IUploadProgress progress) {
		super(progress);
	}

	@Override
	protected FileClient getAttachment() {
		return progress.addAttachment();
	}

	@Override
	public IUploadStatus newInstance() {
		return new TbitsAddUploadStatus(this.progress);
	}

}
