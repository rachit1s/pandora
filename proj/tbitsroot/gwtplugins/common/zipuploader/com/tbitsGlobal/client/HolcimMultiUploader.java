package zipuploader.com.tbitsGlobal.client;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader.OnCancelUploaderHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStatusChangedHandler;
import gwtupload.client.IUploader.Utils;

import java.util.ArrayList;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import commons.com.tbitsGlobal.utils.client.Uploader.ITbitsUploadStatus;
import commons.com.tbitsGlobal.utils.client.Uploader.IUploaderProgressCount;
import commons.com.tbitsGlobal.utils.client.Uploader.TbitsUploadStatus;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class HolcimMultiUploader  extends MultiUploader implements IUploaderProgressCount{
	protected int requestId;
	protected int fieldId;
	public static int repoFileId;
	public static String ADD_FILES	= "Add Files...";
	public static String EDIT_FILES	= "Edit Files...";
	
	private String uploadButtonText = ADD_FILES;
	HolcimWizard parentWizard;
	
	public HolcimMultiUploader(String sysPrefix, int requestId, int actionId, TbitsUploadStatus uploadStatus) {
		super(FileInputType.BUTTON, uploadStatus);
		
		this.avoidRepeatFiles(false);
		
		this.setServletPath("/gwt-upload?folderhint=" + sysPrefix + "&requestid=" + requestId + "&actionid=" + actionId);
		
		this.addOnStatusChangedHandler(new OnStatusChangedHandler(){
			public void onStatusChanged(IUploader uploader) {
				uploader.getFileInput().setText(uploadButtonText);
			}});
		
		this.addOnFinishUploadHandler(new OnFinishUploaderHandler(){
			public void onFinish(IUploader uploader) {
		/*		HolcimMultiUploader.repoFileId = 1138;
					parentWizard.callNextButton();*/
				
				
				 
				 String response = uploader.getServerResponse();
				if(response != null){
					Log.info("**************************" +"/n"+ "Success"+ "/n" + "*****************");
					Log.info("File Name : " + uploader.getFileName() + "  File status : " + uploader.getStatus());
					Log.info(response);
					
					JSONValue val = JSONParser.parse(response);
					if(val != null){
						JSONArray json =  val.isArray();
						if(json != null){
							for(int i = 0; i < json.size(); i++){
								JSONObject attJSON = json.get(i).isObject();
								if(attJSON != null){
									int RepoFileId = Integer.parseInt(attJSON.get("repoFileId").isNumber().toString());
									int size = Integer.parseInt(attJSON.get("size").isNumber().toString());
									IUploadStatus uploadStatus = uploader.getStatusWidget();
									
									if(uploadStatus instanceof ITbitsUploadStatus){
										((ITbitsUploadStatus)uploadStatus).setFileSize(size);
										((ITbitsUploadStatus)uploadStatus).setRepoFileId(repoFileId);
										HolcimMultiUploader.repoFileId = RepoFileId;
										((ITbitsUploadStatus)uploadStatus).setStatus(Status.SUCCESS);
										parentWizard.callNextButton();
										
									}
								}
							}
						}
					}else{
						IUploadStatus uploadStatus = uploader.getStatusWidget();
						
						if(uploadStatus instanceof ITbitsUploadStatus){
							((ITbitsUploadStatus)uploadStatus).setFileSize(0);
						}
					}
				}else{
					IUploadStatus uploadStatus = uploader.getStatusWidget();
					
					if(uploadStatus instanceof ITbitsUploadStatus){
						((ITbitsUploadStatus)uploadStatus).setFileSize(0);
					}
				}
				
			}});
		
		this.addOnCancelUploadHandler(new OnCancelUploaderHandler(){
			public void onCancel(IUploader uploader) {
				uploader.cancel();
			}});
	
	}
	public HolcimMultiUploader(String sysPrefix, int requestId, int actionId, TbitsUploadStatus uploadStatus, String uploadButtonText, HolcimWizard parentWizard){
		this(sysPrefix, requestId, actionId, uploadStatus);
		this.parentWizard = parentWizard;
		this.uploadButtonText = uploadButtonText;
		repoFileId = -1;
		
	}
	
	public int getTotalVisibleUploads(){
		int ret = 0;
	    for (IUploader u : uploaders) {
	      if (u.getStatus() == Status.SUCCESS || u.getStatus() == Status.INPROGRESS || 
	    		  u.getStatus() == Status.QUEUED || u.getStatus() == Status.SUBMITING ||
	    		  u.getStatus() == Status.CANCELING || u.getStatus() == Status.ERROR) {
	        ret++;
	      }
	    }
	    return ret;
	}
	
	public int getQueuedUploads(){
		int ret = 0;
	    for (IUploader u : uploaders) {
	      if (u.getStatus() == Status.QUEUED) {
	        ret++;
	      }
	    }
	    return ret;
	}
	
	public int getInProgressUploads(){
		int ret = 0;
	    for (IUploader u : uploaders) {
	      if (u.getStatus() == Status.INPROGRESS) {
	        ret++;
	      }
	    }
	    return ret;
	}
	
	public String getCurrentFileName(){
		return this.getBasename();
	}
	
	public ArrayList<String> getQueuedNames(){
		ArrayList<String> queuedFiles = new ArrayList<String>();
		for (IUploader u : uploaders) {
			if (u.getStatus() == Status.QUEUED) {
				queuedFiles.add(Utils.basename(u.getFileName()));
			}
		}
		return queuedFiles;
	}
	
	public String getInProgressName(){
		for (IUploader u : uploaders) {
			if (u.getStatus() == Status.INPROGRESS) {
				return Utils.basename(u.getFileName());
			}
		}
		return null;
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		this.getFileInput().setText(uploadButtonText);
		this.getFileInput().setSize("70px", "30px");
	}
	
	@Override
	protected void newUploaderInstance() {
		super.newUploaderInstance();
		currentUploader.getFileInput().setText(uploadButtonText);
	}
}
