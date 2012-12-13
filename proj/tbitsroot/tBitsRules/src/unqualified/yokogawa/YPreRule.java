package yokogawa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.w3c.dom.Document;

import com.sun.org.apache.bcel.internal.generic.RET;

//import com.google.gwt.gears.client.desktop.File;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import static yokogawa.YConst.* ;

public class YPreRule implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		// ba 
		if( ba == null || ba.getSystemPrefix() == null || !ba.getSystemPrefix().equalsIgnoreCase(privBA) )
			return new RuleResult(true,"Not valid for this ba.", true);
		
		try
		{
			if( same( oldRequest, currentRequest) )
			{
				return new RuleResult(true, "The considered values are not changed.", true);
			}
			// make the diff between curr and old and then create the pdf
			Hashtable<String,String> params = getSVGMap( currentRequest );
			
			YEngines yengine = YEngines.getYEngine();
			
			URL fileURL = getClass().getResource("overlay.svg");
			
			String filePath = fileURL.getFile() ;
			 BufferedReader fr    = null;

            fr = new BufferedReader(new FileReader(filePath));

            StringBuilder content = new StringBuilder();
            String        aLine   = "";

            while ((aLine = fr.readLine()) != null) {
                content.append(aLine).append("\n");
            }

            String myXmlFileContent = content.toString();

            String newContent = YEngines.replaceTags(params, myXmlFileContent);
            
            File oFile = File.createTempFile("tempOverlay", ".svg");
            if(!oFile.exists())
            {
            	oFile.createNewFile();
            }

            System.out.println("oFile : " + oFile.getPath());
            
            BufferedWriter bw = new BufferedWriter( new FileWriter(oFile));
            
            bw.write(newContent, 0, newContent.length());
            bw.flush();
            bw.close() ;
            
            File oPdf = File.createTempFile("OverLayOutput", ".pdf");

            System.out.println("oPdf : " + oPdf.getPath());
			yengine.getPDF(oFile.getPath(), oPdf.getPath());
			
			Uploader uploader = new Uploader() ;
			
			AttachmentInfo attInfo = uploader.copyIntoRepository(oPdf, "OverLayOutput.pdf");
			
			System.out.println("Uploading the file finished.");
			Collection<AttachmentInfo> attInfos = (Collection<AttachmentInfo>) currentRequest.getObject(Field.ATTACHMENTS);
			if( null == attInfo )
				attInfos = new ArrayList<AttachmentInfo>() ;
			
			attInfos.add(attInfo);

			currentRequest.setObject(Field.ATTACHMENTS, attInfos);
			
			return new RuleResult(true,"RuleResultSuccess.",true);
		}		
		catch(Exception e)
		{
			e.printStackTrace();
			return new RuleResult(false,e.getMessage(), false);
		}
	}

	public String getName() {
		return "creates and adds the svg from pdf";
	}

	public double getSequence() {
		return 0;
	}

}
