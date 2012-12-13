package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class AttachmentInfoClient extends TbitsModelData {

	public static String SIZE 				= "size";
	public static String FILE_NAME 			= "file_name";
	public static String REPO_FILE_ID 		= "repo_file_id";
	public static String REQUEST_FILE_ID	= "request_file_id";

	public AttachmentInfoClient() {
		super();
		
		this.setSize(0);
		this.setFileName("");
		this.setRepoFileId(0);
		this.setRequestFileId(0);
	}

	public int getSize() {
		return (Integer) this.get(SIZE);
	}

	public void setSize(int size) {
		this.set(SIZE, size);
	}

	public String getFileName() {
		return (String) this.get(FILE_NAME);
	}

	public void setFileName(String fileName) {
		this.set(FILE_NAME, fileName);
	}

	public int getRepoFileId() {
		return (Integer) this.get(REPO_FILE_ID);
	}

	public void setRepoFileId(int repoFileId) {
		this.set(REPO_FILE_ID, repoFileId);
	}

	public int getRequestFileId() {
		return (Integer) this.get(REQUEST_FILE_ID);
	}

	public void setRequestFileId(int requestFileId) {
		this.set(REQUEST_FILE_ID, requestFileId);
	}
	
	public String getFormattedSize(){
		int size = this.getSize();
		if(size < 1024){
			return size + " Bytes";
		}else if(size < 1024 * 1024){
			double d = new Double(size)/1024;
			d = ClientUtils.round(d, 2);
			return d + " KB";
		}else if(size < 1024 * 1024 * 1024){
			double d = new Double(size)/(1024 * 1024);
			d = ClientUtils.round(d, 2);
			return d + " MB";
		}else{
			double d = new Double(size)/(1024 * 1024 * 1024);
			d = ClientUtils.round(d, 2);
			return d + " GB";
		}
	}
}
