package imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gwt.dev.util.collect.HashMap;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class ImpPostTransmittalZipFile implements IRule {
	public static final TBitsLogger LOG = TBitsLogger.getLogger("Zip_Rule");

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub

		
		ArrayList<Field> fieldList = null;
		try {
			fieldList = Field.lookupBySystemId(ba.getSystemId());
		} catch (DatabaseException e1) {
			LOG.error("**********************************" + "\n"
					+ "Cant fetch fields" + "*****************************"
					+ "\n");
			e1.printStackTrace();
		}
		Boolean flag = false;

		ArrayList<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();

		if (oldRequest == null && ba.getDescription().equals("DTN") ) {

			for (Field field : fieldList) {
				if (field.getName().equals("DTNZippedFile"))
					
				{
					LOG.error("**********************************" + "\n"
							+ "Field has been found" + "*****************************"
							+ "\n");
					flag = true;
				
				}

			}
			if (flag) {
				String dtnNo = (String) currentRequest.getObject("DTNNumber");
				String dtnNote = currentRequest.get("DTNFile");
				String dtnAtts = currentRequest.get("CommentedFiles");

				Collection<AttachmentInfo> dtnnotes = AttachmentInfo
						.fromJson(dtnNote);
				Collection<AttachmentInfo> dtnatts = AttachmentInfo
						.fromJson(dtnAtts);

				dtnatts.addAll(dtnnotes);

				String filepath = zip(dtnatts);

				if (filepath == null) {
					LOG.error("**********************************" + "\n"
							+ "Zipping has been unsuccessful"
							+ "*****************************" + "\n");
					RuleResult rr = new RuleResult(false,
							"Zipping has been unsuccessful");
					return rr;
				} else {

					File file1 = new File(filepath);

					if (!file1.exists()) {// inner if
						/**
						 * TODO: flag an error
						 */
						LOG.error("**********************************" + "\n"
								+ "Temp Zip file does not exist"
								+ "*****************************" + "\n");
						RuleResult rr = new RuleResult(false,
								"Temp Zip file does not exist");

					} else {// inner else
						if (!file1.isDirectory()) {// 2-inner if
							// AttachmentInfo att1 = uploader.upload(file1);
							AttachmentInfo att1 = new Uploader()
									.copyIntoRepository(file1,
											dtnNo + ".zip");
							LOG.error("**********************************"
									+ "\n" + "Size of the zip file is = "
									+ att1.getSize()
									+ "*****************************" + "\n");

							if (att1 == null) {// 3 inner if
								/**
								 * Flag error
								 */
							}
							atts.add(att1);
						}// 2 inner if
						else {// 2-inner else
							// atts.addAll(uploadFolder(file, errorWriter,
							// uploader, trnProcessId, action));
						}
					}// inner else

					String jsonString = AttachmentInfo.toJson(atts);

					try {
						setAttachmentInfo(ba.getSystemId(), currentRequest,
								oldRequest, atts);
						LOG.error("**********************************" + "\n"
								+ "Attachment has been added successfuly"
								+ "*****************************" + "\n");

					} catch (NumberFormatException e) {
						RuleResult rr = new RuleResult(false,
								"Error Occured in setting attachment");

						e.printStackTrace();

						return rr;
					}
					// currentRequest.set(fieldName, jsonString);
					// currentRequest.setAttachments(aAttachments)
				}// if

			}

		}

		RuleResult rr = new RuleResult(true,
				"First rule,request can be created");
		rr.setCanContinue(true);
		return rr;

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String zip(Collection<AttachmentInfo> dtnatts) {// These are the
															// files to
		// include in the ZIP file
		byte[] buf = new byte[1024];
		String pathOfTempFile = "";

		try {

			File outFile = File.createTempFile("zipfile", "zip");
			pathOfTempFile = outFile.getAbsolutePath();
			LOG.info("**********************************" + "\n"
					+ "File path of the temp file = " + pathOfTempFile
					+ "*****************************" + "\n");
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					outFile));
			for (AttachmentInfo att : dtnatts) {

				LOG.info("**********************************" + "\n"
						+ "Working on the  file = " + att.getName()
						+ "*****************************" + "\n");
				String filePath = Uploader.getFileLocation(att.getRepoFileId());

				File attachmentBase;
				attachmentBase = new File(APIUtil.getAttachmentLocation());

				String exactpath = attachmentBase.getPath();
				exactpath = exactpath + "/" + filePath;

				File tempfile = new File(exactpath);
				// Create a buffer for reading the files

				// Create the ZIP file

				// Compress the files
				// for (int i=0; i<filenames.length; i++) {
				FileInputStream in = new FileInputStream(tempfile);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(tempfile.getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}

			// Complete the ZIP file
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		} catch (DatabaseException e) {

			e.printStackTrace();
			return null;
		}
		return pathOfTempFile;
	}

	/**
	 * Checks the attachments in the current request and the old request and
	 * corrects the information to be updated based on the attachment infos and
	 * the carryover permission of the field.
	 * 
	 * @param sysid
	 * @param currentRequest
	 * @param oldRequest
	 * @param atts
	 * @throws APIException
	 */
	private void setAttachmentInfo(int sysid, Request currentRequest,
			Request oldRequest, ArrayList<AttachmentInfo> atts) {

		String fieldName = "DTNZippedFile";
		currentRequest.setObject(fieldName, atts);
		// Extract the attachment type fields and corresponding attinfo objects
		ArrayList<Field> attFields = new ArrayList<Field>();
		try {
			attFields = Field.lookupBySystemId(sysid, DataType.ATTACHMENTS);
		} catch (DatabaseException e) {
			e.printStackTrace();

		}

		for (Field atf : attFields) {

			Collection<AttachmentInfo> oldAttachments = (Collection<AttachmentInfo>) oldRequest
					.getObject(atf);
			if (oldAttachments == null)
				oldAttachments = (Collection<AttachmentInfo>) new ArrayList<AttachmentInfo>();

			Collection<AttachmentInfo> newAttachments = (Collection<AttachmentInfo>) currentRequest
					.getObject(atf);

			// Check for carry over
			if (newAttachments == null
					&& (atf.getPermission() & Permission.SET) != 0) {
				// Add all the old attachments to new attachments
				newAttachments = (Collection<AttachmentInfo>) new ArrayList<AttachmentInfo>();
				newAttachments.addAll(oldAttachments);
			}

			if (newAttachments == null)
				newAttachments = (Collection<AttachmentInfo>) new ArrayList<AttachmentInfo>();

			// Add the attachment info back to the old and new requests
			oldRequest.setObject(atf, oldAttachments);
			currentRequest.setObject(atf, newAttachments);
		}

	}

}
