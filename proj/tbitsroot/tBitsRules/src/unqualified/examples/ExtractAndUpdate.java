package examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
*
* @author Utkarsh
* This class Processes all the uncompressed attachments and uploads decompressed attachments
* For a Specific BA
*/
public class ExtractAndUpdate implements IRule {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger("examples");
	public static String extractTempDir;
	public static String extractAttachmentDir;

	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Post Rule for Extracting Compressed Attachments";
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	public static final void copyInputStream(InputStream in, OutputStream out)
	  throws IOException
	  {
	    byte[] buffer = new byte[1024];
	    int len;

	    while((len = in.read(buffer)) >= 0)
	      out.write(buffer, 0, len);

	    in.close();
	    out.close();
	  }

	 
	
    /**
     * This method decompresses the zip attachment 
     *
     * @param zipAttachment              uncompressed attachment
     * @param up                         uploader for moving files into repository
     * @param deAttachArray  list of attachments after decompression
     */
    
    public void processZipAttachment(AttachmentInfo zipAttachment,Uploader up,ArrayList<AttachmentInfo> deAttachArray){
    	
    	
    	String zipLocation=null;
		try {
			zipLocation= Uploader.getFileLocation(zipAttachment.repoFileId);
			//System.out.println(rarLocation);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		String filename=extractAttachmentDir +"/"+zipLocation;
		File f = new File(filename);
		ZipFile zipFile=null;
		Enumeration<? extends ZipEntry> entries=null;
		try {
		      zipFile = new ZipFile(f);

		      entries = zipFile.entries();

		      while(entries.hasMoreElements()) {
		        ZipEntry entry = (ZipEntry)entries.nextElement();

		        if(entry.isDirectory()) {
		          // Assume directories are stored parents first then children.
		          System.err.println("Extracting directory: " + entry.getName());
		          // This is not robust, just for demonstration purposes.
		          (new File(entry.getName())).mkdir();
		          continue;
		        }
		        try {
					File out = new File(extractTempDir+"/"+entry.getName().trim());
					FileOutputStream os = new FileOutputStream(out);
					
					 copyInputStream(zipFile.getInputStream(entry),os);

					deAttachArray.add(up.moveIntoRepository(out));
					os.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ZipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      }

		      zipFile.close();
		    } catch (IOException ioe) {
		      ioe.printStackTrace();
		      return;
		    }
		
		}
		
		
    /**
     * This method decompresses the rar attachment 
     *
     * @param rarAttachment              uncompressed attachment
     * @param up                         uploader for moving files into repository
     * @param deAttachArray  list of attachments after decompression
     */
    
    
	public void processRarAttachment(AttachmentInfo rarAttachment,Uploader up,ArrayList<AttachmentInfo> deAttachArray){
		String rarLocation=null;
		try {
			rarLocation= Uploader.getFileLocation(rarAttachment.repoFileId);
			//System.out.println(rarLocation);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		String filename=extractAttachmentDir +"/"+rarLocation;
		File f = new File(filename);
		Archive a=null;
		try {
			a = new Archive(f);
		} catch (RarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(a!=null){
			a.getMainHeader().print();
			FileHeader fh = a.nextFileHeader();
			if(fh.isDirectory()) fh=a.nextFileHeader();
			while(fh!=null){
				try {
					File out = new File(extractTempDir+"/"+fh.getFileNameString().trim());
					System.out.println(out.getAbsolutePath());
					FileOutputStream os = new FileOutputStream(out);
					a.extractFile(fh, os);
					os.close();
					deAttachArray.add(up.moveIntoRepository(out));
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RarException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fh=a.nextFileHeader();
				
			    
				
				
				
		        
			}
				
		}
		
		
	}
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		int requestId = currentRequest.getRequestId() ;
		int actionId = currentRequest.getMaxActionId() ;
		String prefix = ba.getSystemPrefix() ;
		if( ! prefix.equals("mail"))
			return new RuleResult(true,"Rule ignored as the ba is not mailba" , true ) ;
		
		Uploader deUploader = new Uploader(requestId, actionId, prefix);
		
		Collection<AttachmentInfo> attachList = currentRequest.getAttachments();
		
		try {
            extractAttachmentDir = Configuration.findAbsolutePath
            								(PropertiesHandler.getProperty(TBitsPropEnum.KEY_ATTACHMENTDIR)).toString();
            extractTempDir = Configuration.findAbsolutePath
			(PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR)).toString();
        } catch (IllegalArgumentException e) {
            LOG.severe(e.toString(), e);
        }
        Iterator<AttachmentInfo> attachListIterator=attachList.iterator();	
		AttachmentInfo rarAttachment=null;
		
		Field field = null;
		try {
			field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),"DecompressedAttachments");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String json=null;
		ArrayList<AttachmentInfo> deAttachArray = new ArrayList<AttachmentInfo>() ;// arrayList is a Collection
		while(attachListIterator.hasNext()){
			rarAttachment=attachListIterator.next();
			if(rarAttachment.name.contains(".zip")){
			processZipAttachment(rarAttachment,deUploader,deAttachArray);
			}
			else{
				processRarAttachment(rarAttachment,deUploader,deAttachArray);
			}
			
		RequestEx requestex = extendedFields.get(field) ;
		json = AttachmentInfo.toJson(deAttachArray);
		requestex.setTextValue(json);
		
	}
		return new RuleResult( true , "ExtractAndUpdate finished" , true ) ;
}
}
