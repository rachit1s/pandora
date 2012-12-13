package commons.com.tbitsGlobal.utils.client.Uploader;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public interface IUploadContainerCallback {
	public void onAttsChanged(List<FileClient> atts);
	public void onStatusChanged(FileClient att);
}
