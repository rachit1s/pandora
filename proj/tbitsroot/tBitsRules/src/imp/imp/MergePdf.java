package imp;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.report.TBitsReportEngine;

public class MergePdf
  implements IRule
{
	private static String rev;
  public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest)
  {
	  rev = currentRequest.get("Revision");
    ArrayList otherAttList = new ArrayList();
    String typevalue = currentRequest.get("DelCategory");
//    if (!typevalue.equalsIgnoreCase("document")) {
//      return new RuleResult(true, "As the Deliverable category is not document it should not generate cover page");
//    }
  
    Field sourceAttachmentField = null;
    try {
      sourceAttachmentField = getSourceAttachmentField(connection, ba);
    }
    catch (SQLException e1) {
      e1.printStackTrace();
      return new RuleResult(true, "Error while fetching the Source Attachment field from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "]");
    }

    if (sourceAttachmentField == null) {
      return new RuleResult(true, "Continuing without generation of cover sheet as its not applicable to BA: " + 
        ba.getSystemPrefix() + ", as no field for source attachment field is configured.");
    }

    if ((oldRequest == null) || (checkForModificationinAttachments(oldRequest, currentRequest, sourceAttachmentField)))
    {
      Field targetAttachmentField = null;
      try {
        targetAttachmentField = getTargettargetAttachmentField(connection, ba);
      }
      catch (SQLException e1) {
        e1.printStackTrace();
        return new RuleResult(true, "Error while fetching the Target Attachment field from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "]");
      }

      if (targetAttachmentField == null) {
        return new RuleResult(true, "Continuing without generation of cover sheet as its not applicable to BA: " + 
          ba.getSystemPrefix() + ", as no field for Target attachment field is configured.");
      }
      if(typevalue.equalsIgnoreCase("drawing")){
    	  Collection<AttachmentInfo> src_attachments = (Collection)currentRequest.getObject(sourceAttachmentField);
    	  if(src_attachments.isEmpty())
    		  return new RuleResult(true,"There is no attachments for rename");
    	  else if(src_attachments.size()==1){
    		  for(AttachmentInfo att : src_attachments){
    		  try {
				String fileLocation=Uploader.getFileLocation(att.getRepoFileId());
				File attachment=new File(APIUtil.getAttachmentLocation());
				String attachmentbase=attachment.getPath();
				String Exactpath=attachmentbase+"/"+fileLocation;
				String Name = att.getName();
				String ExtensionName = Name.substring(Name.indexOf("."));
				File file=new File(Exactpath);
				Uploader a=new Uploader();
				AttachmentInfo newAttachment=null;
				try {
					newAttachment=a.copyIntoRepository(file, currentRequest.get(getdocNumber(connection, ba).getName()) + "_" + currentRequest.get("Revision") + ExtensionName);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<AttachmentInfo> ab=new ArrayList<AttachmentInfo>();
				ab.add(newAttachment);
				currentRequest.setObject(targetAttachmentField, ab);
				return new RuleResult(true, "Rule executed succsfully for renaming file for drawings");
				
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		  }
    				  
    	  }
    	  else{
    		  src_attachments = (Collection)currentRequest.getObject(sourceAttachmentField);
    		  String zipLocation=zip(src_attachments);
    		  File f=new File(zipLocation);
    		  Uploader a=new Uploader();
    		  AttachmentInfo att=null;
    		  try {
				att=a.copyIntoRepository(f, currentRequest.get(getdocNumber(connection, ba).getName()) + "_" + currentRequest.get("Revision") + ".Zip");
				ArrayList<AttachmentInfo> abc=new ArrayList<AttachmentInfo>();
				abc.add(att);
				currentRequest.setObject(targetAttachmentField, abc);
				return new RuleResult(true, "Rule executed succsfully for renaming file for drawings");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		  
    		    		  
    		  
    	  }
      }

      String templateName = null;
      try {
        templateName = getTemplateName(connection, ba);
      }
      catch (SQLException e1) {
        e1.printStackTrace();
        return new RuleResult(true, "Error while fetching the Report Name from database for BA : " + ba.getSystemId() + "[" + ba.getSystemPrefix() + "]");
      }

      if (templateName == null) {
        return new RuleResult(true, "Continuing without generation of cover sheet as its not applicable to BA: " + 
          ba.getSystemPrefix() + ", as no template name is configured.");
      }

      Collection<AttachmentInfo> src_attachments = (Collection)currentRequest.getObject(sourceAttachmentField);
      if ((src_attachments.size() == 0) || (checkForPDF(src_attachments))) {
        return new RuleResult(true, "As there is no pdf files in the " + sourceAttachmentField.getDisplayName() + ".It cannot generate cover sheet");
      }

      int totalpages = 0;
      PdfReader pdf = null;
      String sql = "";
      String docName = "";
      String docLocation = "";
      for (AttachmentInfo att : src_attachments)
        if (att.getName().endsWith(".pdf")) {
          sql = "select location,name from file_repo_index where id=?";
          try
          {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, att.repoFileId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
              docLocation = rs.getString(1);
              docName = rs.getString(2);
              pdf = new PdfReader(APIUtil.getAttachmentLocation() + File.separator + docLocation);
              totalpages += pdf.getNumberOfPages();
            }
          }
          catch (InvalidPdfException e) {
            e.printStackTrace();
            return new RuleResult(false, "There is a Problem With the Document named as " + docName + ".Please remove it and then try again");
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        } else {
          otherAttList.add(att);
        }
      try
      {
        TBitsReportEngine tbits = TBitsReportEngine.getInstance();
        Object reportVariables = new HashMap();
        Map reportParams = new HashMap();
        reportParams.put("request_id", currentRequest.getRequestId());
        reportParams.put("sys_id", Integer.valueOf(currentRequest.getSystemId()));
        ((Map)reportVariables).put("businessarea", ba);
        ((Map)reportVariables).put("request", currentRequest);
        ((Map)reportVariables).put("nop", totalpages + 1);
        ((Map)reportVariables).put("otherAtts", otherAttList);
        File tempDir = Configuration.findPath("webapps/tmp");
        String coversheetlocation = tempDir + File.separator + "coversheet.pdf";
        File coversheet = new File(coversheetlocation);
        tbits.generatePDFFile(templateName, (Map)reportVariables, reportParams, coversheet);

        List pdfs = new ArrayList();
        pdfs.add(new FileInputStream(coversheet));
        for (AttachmentInfo att : src_attachments) {
          if (att.getName().endsWith(".pdf")) {
            sql = "select location from file_repo_index where id=?";
            try
            {
              PreparedStatement ps = connection.prepareStatement(sql);
              ps.setInt(1, att.repoFileId);
              ResultSet rs = ps.executeQuery();
              while (rs.next())
                pdfs.add(new FileInputStream(APIUtil.getAttachmentLocation() + File.separator + rs.getString(1)));
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
          }
        }

        Field docNumber = null;
        try {
          docNumber = getdocNumber(connection, ba);
        }
        catch (Exception e) {
          e.printStackTrace();
          return new RuleResult(true, "There is a Problem With the doument number generation.Pease Once Check Configurations");
        }
        Object output = new FileOutputStream(tempDir + File.separator + currentRequest.get(docNumber.getName()) + "_" + currentRequest.get("Revision") + ".pdf");
        concatPDFs(pdfs, (OutputStream)output, true);
        File f = new File(tempDir + File.separator + currentRequest.get(docNumber.getName()) + "_" + currentRequest.get("Revision") + ".pdf");
        Uploader up = new Uploader();
        up.setFolderHint(ba.getSystemPrefix());
        AttachmentInfo uploaded = up.moveIntoRepository(f);
        ArrayList targetattachments = new ArrayList();
        targetattachments.add(uploaded);
        for (AttachmentInfo att : src_attachments) {
          if (!att.getName().endsWith(".pdf")) {
            targetattachments.add(att);
          }
        }
        currentRequest.setObject(targetAttachmentField, targetattachments);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    return new RuleResult(true, "Rule Executed Successfully", true);
  }

  private Field getdocNumber(Connection connection, BusinessArea ba)
    throws SQLException
  {
    PreparedStatement ps = connection.prepareStatement("select field_name from trn_drawing_number_field where sys_id=?");
    ps.setInt(1, ba.getSystemId());
    ResultSet rs = ps.executeQuery();
    if ((rs != null) && (rs.next())) {
      String docNumberFieldName = rs.getString(1);
      Field docNumberField = null;
      try {
        docNumberField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), docNumberFieldName);
      } catch (DatabaseException e) {
        e.printStackTrace();
        return null;
      }
      return docNumberField == null ? null : docNumberField;
    }
    return null;
  }

  private boolean checkForModificationinAttachments(Request oldRequest,
			Request currentRequest, Field sourceAttachmentField) {
		// TODO Auto-generated method stub
		Collection<AttachmentInfo> oldatts=(Collection<AttachmentInfo>) oldRequest.getObject(sourceAttachmentField);
		Collection<AttachmentInfo> curatts=(Collection<AttachmentInfo>) currentRequest.getObject(sourceAttachmentField);
		if(oldatts.size()!=curatts.size())
			return true;
		for(AttachmentInfo old_att:oldatts)
			for(AttachmentInfo cur_att:curatts)
				if(old_att.getRepoFileId()!=cur_att.getRepoFileId())
					return true;
		return false;
  }

  private boolean checkForPDF(Collection<AttachmentInfo> src_attachments)
  {
    for (AttachmentInfo att : src_attachments) {
      if (att.getName().endsWith(".pdf"))
        return false;
    }
    return true;
  }

  private String getTemplateName(Connection connection, BusinessArea ba) throws SQLException
  {
    String templateName = null;
    
    int j =0;
//  for(int i=0;i<20;i++)
//  {
  	try
  	{
  	if(Integer.parseInt(rev)==0)
  	{
  	j =1;
//  	break;
  	}
  	}
  	catch(Exception e)
  	{
  		System.out.print(e);
  	}
//  }
  	
  	String ro_active = "";
  	
  	if(j==1)
  		ro_active="true";
  	else
  		ro_active="false";
    
    PreparedStatement ps = connection.prepareStatement("select report_file_name from cover_page_generation where sys_id = ? and ro_active=?");
    
    ps.setInt(1, ba.getSystemId());
    ps.setString(2, ro_active);
    ResultSet rs = ps.executeQuery();
    if ((rs != null) && (rs.next())) {
//    	if(j==1)
//    	{
//    		while(rs.next())
//    			templateName = rs.getString(1);
//    	}
//    	else
      templateName = rs.getString(1);
      return templateName == null ? null : templateName;
    }
    return null;
  }

  private Field getTargettargetAttachmentField(Connection connection, BusinessArea ba)
    throws SQLException
  {
    PreparedStatement ps = connection.prepareStatement("select target_field_id from cover_page_generation where sys_id = ?");
    ps.setInt(1, ba.getSystemId());
    ResultSet rs = ps.executeQuery();
    if ((rs != null) && (rs.next())) {
      int srcFieldId = rs.getInt(1);
      Field srcTargetField = null;
      try {
        srcTargetField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), srcFieldId);
      } catch (DatabaseException e) {
        e.printStackTrace();
        return null;
      }
      return srcTargetField == null ? null : srcTargetField;
    }
    return null;
  }

  private Field getSourceAttachmentField(Connection connection, BusinessArea ba)
    throws SQLException
  {
    PreparedStatement ps = connection.prepareStatement("select src_field_id from cover_page_generation where sys_id = ?");
    ps.setInt(1, ba.getSystemId());
    ResultSet rs = ps.executeQuery();
    if ((rs != null) && (rs.next())) {
      int srcFieldId = rs.getInt(1);
      Field srcDrawingField = null;
      try {
        srcDrawingField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), srcFieldId);
      } catch (DatabaseException e) {
        e.printStackTrace();
        return null;
      }
      return srcDrawingField == null ? null : srcDrawingField;
    }
    return null;
  }

  private static void concatPDFs(List<InputStream> streamOfPDFFiles, OutputStream outputStream, boolean paginate)
  {
    try
    {
      List pdfs = streamOfPDFFiles;
      List readers = new ArrayList();
      int totalPages = 0;
      Iterator iteratorPDFs = pdfs.iterator();
      File tempDir = Configuration.findPath("webapps/tmp");
      PdfCopyFields pf = new PdfCopyFields(new FileOutputStream(tempDir + File.separator + "tmp.pdf"));

      BaseFont font = BaseFont.createFont();
      while (iteratorPDFs.hasNext()) {
        InputStream pdf = (InputStream)iteratorPDFs.next();
        PdfReader reader = new PdfReader(pdf);
        pf.addDocument(reader);

        readers.add(reader);
        totalPages += reader.getNumberOfPages();
      }

      pf.close();

      PdfReader r = new PdfReader(tempDir + File.separator + "tmp.pdf");
      PdfStamper stamper = new PdfStamper(r, outputStream);
      for (int i = 1; i < r.getNumberOfPages(); i++) {
        PdfContentByte overContent = stamper.getOverContent(i + 1);
        Rectangle r1 = r.getPageSize(i + 1);
        overContent.saveState();
        overContent.beginText();
        overContent.setFontAndSize(font, 10.0F);
        overContent.setTextMatrix(r1.getWidth() / 1.1F, 10.0F);
        overContent.showText("Page " + (i + 1) + " of " + r.getNumberOfPages());
        overContent.endText();
        overContent.restoreState();
      }
      stamper.close();

      outputStream.close();
    }
    catch (InvalidPdfException e)
    {
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  public String zip(Collection<AttachmentInfo> dtnatts) {// These are the
		// files to
// include in the ZIP file
byte[] buf = new byte[1024];
String pathOfTempFile = "";

try {

File outFile = File.createTempFile("zipfile", "zip");
pathOfTempFile = outFile.getAbsolutePath();
////LOG.info("**********************************" + "\n"
//+ "File path of the temp file = " + pathOfTempFile
//+ "*****************************" + "\n");
ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
outFile));
for (AttachmentInfo att : dtnatts) {
//
//LOG.info("**********************************" + "\n"
//+ "Working on the  file = " + att.getName()
//+ "*****************************" + "\n");
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

  public double getSequence()
  {
    return 5.0D;
  }

  public String getName()
  {
    return getClass().getSimpleName() + ": Generate Coversheet after fetching corresponding properties from DB";
  }
}