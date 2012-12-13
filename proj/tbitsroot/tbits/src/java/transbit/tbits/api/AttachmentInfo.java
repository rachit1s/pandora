package transbit.tbits.api;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class AttachmentInfo implements Comparable<AttachmentInfo>, Serializable{
	public AttachmentInfo() {
	}
	public int repoFileId;
	public String name;
	public int requestFileId;
	public int size;

	public static String toJson(Collection<AttachmentInfo> attachments)
	{
		Gson gson = new Gson();
		return gson.toJson(attachments);
	}
	
	public AttachmentInfo(String name, int repoFileId, int requestFileId,
			int size) {
		super();
		this.name = name;
		this.repoFileId = repoFileId;
		this.requestFileId = requestFileId;
		this.size = size;
	}

	public int getRepoFileId() {
		return repoFileId;
	}

	public void setRepoFileId(int repoFileId) {
		this.repoFileId = repoFileId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRequestFileId() {
		return requestFileId;
	}

	public void setRequestFileId(int requestFileId) {
		this.requestFileId = requestFileId;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public static Collection<AttachmentInfo> fromJson(String json)
	{
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<AttachmentInfo>>(){}.getType();
		try{
			if(json == null)
				return new ArrayList<AttachmentInfo>();
			return gson.fromJson(json,	collectionType);
		}catch(JsonParseException pe){
			return null;
		}
	}
	
	public static void main(String[] args) {
		String str = "[{repoFileId:1, name:\"50_Guarantees.pdf\", requestFileId:\"0\", size:385178}]";
		Collection<AttachmentInfo> files = AttachmentInfo.fromJson(str);
		System.out.println(files);
		System.out.println(AttachmentInfo.toJson(files));
	}

	public int compareTo(AttachmentInfo o) {
		// TODO Auto-generated method stub
		return name.compareTo(o.name);
	}
	
	public String toString()
	{
		return "{name=" + this.name + "  repoFileId=" + this.repoFileId + "  requestFileId=" + this.requestFileId + "  size=" + this.size + "}";
	}
}
