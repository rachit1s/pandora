package commons.com.tbitsGlobal.utils.client.Uploader.gears;

import com.tbitsGlobal.uploader.gears.client.GearsUploader;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentGrid;
import commons.com.tbitsGlobal.utils.client.Uploader.IUploaderProgressCount;

public class TbitsGearsUploader extends GearsUploader implements IUploaderProgressCount{
	
	public TbitsGearsUploader(String sysPrefix, int requestId, UploadProgessAttachmentGridGears grid) throws Exception {
		super();
		
		listenerWidget = grid;
		uploader = new TbitsUploader(sysPrefix, requestId, listenerWidget);
	}
	
	@Override
	protected void init() {}
	
	public AttachmentGrid getAttachmentGrid(){
		return (AttachmentGrid) listenerWidget.getWidget();
	}

	public int getInProgressUploads() {
		return uploader.getInProgressUploads();
	}

	public int getQueuedUploads() {
		return uploader.getQueuedUploads();
	}
}
