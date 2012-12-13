package transbit.tbits.webapps;

import java.util.Collection;

import com.google.gson.Gson;

import transbit.tbits.api.AttachmentInfo;

public class AttachmentFieldInfo {
	public AttachmentFieldInfo(String fieldName,
			Collection<AttachmentInfo> files) {
		super();
		this.fieldName = fieldName;
		this.files = files;
	}

	String fieldName;
	Collection<AttachmentInfo> files;

	public static String toJson(Collection<AttachmentFieldInfo> attachmentFields) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		return gson.toJson(attachmentFields);
	}
}
