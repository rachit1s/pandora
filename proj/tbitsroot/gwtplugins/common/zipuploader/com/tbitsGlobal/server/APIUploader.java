package zipuploader.com.tbitsGlobal.server;

import java.io.File;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Uploader;
import transbit.tbits.common.Utilities;

public class APIUploader extends Uploader {
	public APIUploader(String sysPrefix) {
		super();

		folderHint = sysPrefix;
	}

	public AttachmentInfo upload(File file) {
		if (file == null || !file.exists())
			return null;

		File attachmentBase = new File(APIUtil.getAttachmentLocation()); 
		File parentDir = attachmentBase;
		if (!parentDir.exists())
			parentDir.mkdirs();

		String fileName = file.getName();

		parentDir = prepareAttachmentFolder(this.folderHint, parentDir);

		String proposedfileName = requestId + "-" + actionId + "-"
				+ fileName.replaceAll("[^A-Za-z0-9-\\._\\+]+", "_");

		File fTarget = generateUniqTargetFile(parentDir, proposedfileName);

		try {
			// Utilities.copyFile(file, fTarget);
			Utilities.copyFileSlow(file, fTarget);
			// Now put it in DB
			String relative = attachmentBase.toURI()
					.relativize(fTarget.toURI()).getPath();
			int id = writeFilePropsIntoDB(relative, fileName, fTarget);
			AttachmentInfo attInfo = new AttachmentInfo();
			attInfo.name = fileName;
			attInfo.repoFileId = id;
			attInfo.size = (int) fTarget.length();
			return attInfo;

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Actual File: " + fTarget.getAbsolutePath());

		return null;
	}

}
