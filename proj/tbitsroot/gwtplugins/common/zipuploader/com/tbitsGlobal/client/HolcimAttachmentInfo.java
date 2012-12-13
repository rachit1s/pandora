package zipuploader.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class HolcimAttachmentInfo extends TbitsModelData {

	public static String REPO_FILE_ID = "repoFileId";
	public static String NAME = "name";
	public static String REQUEST_FILE_ID = "requestFileId";
	public static String SIZE = "size";

	public Integer getSIZE() {
		return (Integer) this.get(SIZE);
	}

	public void setSIZE(Integer Size) {
		this.set(SIZE, Size);

	}

	public String getNAME() {
		return (String) this.get(NAME);
	}

	public void setName(String name) {
		this.set(NAME, name);

	}

	public Integer getRequestFileID() {
		return (Integer) this.get(REQUEST_FILE_ID);
	}

	public void setRequestFileID(Integer request_file_id) {
		this.set(REQUEST_FILE_ID, request_file_id);

	}

	public Integer getRepoFileID() {
		return (Integer) this.get(REPO_FILE_ID);
	}

	public void setRepotFileID(Integer repoFileId) {
		this.set(REPO_FILE_ID, repoFileId);

	}

}
