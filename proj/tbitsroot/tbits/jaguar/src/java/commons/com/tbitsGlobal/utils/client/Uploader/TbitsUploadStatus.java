package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.IUploadStatus;

import java.util.ArrayList;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public abstract class TbitsUploadStatus implements ITbitsUploadStatus {
	protected IUploadProgress progress;
	
	private UploadStatusChangedHandler onUploadStatusChangedHandler = null;
	private IUploadStatus.Status status = Status.UNINITIALIZED;
	
	private ArrayList<UploadCancelHandler> cancelhandlers = new ArrayList<UploadCancelHandler>();
	
	protected FileClient att = null;
	
	public TbitsUploadStatus(){
		
	}
	
	public TbitsUploadStatus(IUploadProgress progress){
		this();
		
		this.progress = progress;
	}
	
	public void setUploadProgress(IUploadProgress progress){
		this.progress = progress;
	}

	public HandlerRegistration addCancelHandler(UploadCancelHandler handler) {
		if(progress == null)
			return null;
		cancelhandlers.add(handler);
		return null;
	}

	public Status getStatus() {
		return status;
	}

	public void setCancelConfiguration(Set<CancelBehavior> config) {
	}

	public void setError(String msg) {
		setStatus(Status.ERROR);
		att.setError(msg);
		progress.updateAttachment(att, true);
	    Window.alert(msg.replaceAll("\\\\n", "\\n"));
	}

	public void setFileName(String name) {
		assert progress != null;
		att = getAttachment();
		
		if(att == null)
			return;
		att.setFileName(name);
		progress.updateAttachment(att, true);
		
		for(UploadCancelHandler handler : cancelhandlers)
			progress.addCancelHandler(att, handler);
	}

	public void setI18Constants(UploadStatusConstants strs) {
	}

	public void setStatus(Status stat) {
		if (status != stat && onUploadStatusChangedHandler != null) {
	      status = stat;
	      onUploadStatusChangedHandler.onStatusChanged(this);
	    }
	    status = stat;
	    switch (stat) {
	      case QUEUED: case SUBMITING: case INPROGRESS:
	    	  att.setUploading();
	    	  break;
	      case SUCCESS: case REPEATED:
	    	  if(att.getRepoFileId() != 0)
	    		  att.setUploaded();
	    	  break;
	      case ERROR:
	    	  att.setFailed();
	    	  break;
	      case CANCELING: case CANCELED: case DELETED:
	    	  att.setCancelled();
	    	  break;
	    }
	    progress.updateAttachment(att, true);
	}

	public void setStatusChangedHandler(UploadStatusChangedHandler handler) {
		onUploadStatusChangedHandler = handler;
	}

	public abstract IUploadStatus newInstance();

	public void setProgress(int done, int total) {
		int percent = total > 0 ? done * 100 / total : 0;
	    setPercent(percent);
	    progress.setProgress(att, done, total);
	}

	public void setPercent(int percent) {
	    setStatus(status);
	}

	public void setFileSize(int size) {
		if(size == 0){
			att.setFailed();
			att.setError("Unknown Error");
		}else
			att.setSize(size);
		progress.updateAttachment(att);
	}

	public void setRepoFileId(int repoFileId) {
		att.setRepoFileId(repoFileId);
		progress.updateAttachment(att);
	}
	
	protected abstract FileClient getAttachment();
}
