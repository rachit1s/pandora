package yokogawa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class YEngines 
{
	
	private DocumentBuilder svgBuilder = null ;
	private Transformer transformer = null ;
	private PDFTranscoder transcoder = null ;
	private  static YEngines yengine = null ; 
	
	private YEngines() throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError
	{
			DocumentBuilderFactory docBuF  = DocumentBuilderFactory.newInstance();
			svgBuilder = docBuF.newDocumentBuilder();
			TransformerFactory tf = TransformerFactory.newInstance();
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.STANDALONE,"no");
			transcoder = new PDFTranscoder();
	}
    
    public static synchronized YEngines getYEngine() throws TransformerConfigurationException, ParserConfigurationException, TransformerFactoryConfigurationError
    {
    	if( null == yengine )
    	{
    		yengine = new YEngines() ;
    	}
    	
    	System.out.println("Engines initialized.");
    	return yengine ;
    }
	
	public Document getSVGDoc(String filePath) throws SAXException, IOException
	{
		Document svgDoc = svgBuilder.parse(filePath);
		return svgDoc ;
	}
	
	public void transform(Document svgDoc, String outFilePath ) throws TransformerException, IOException
	{
      DOMSource source = new DOMSource(svgDoc);
      File outFile = new File( outFilePath );
      if( !outFile.exists() )
    	  outFile.createNewFile() ;
      
      PrintWriter outStream = new PrintWriter(outFile);
      StreamResult fileResult = new StreamResult(outStream);
      transformer.transform(source,fileResult);
	}
	
	public void getPDF(String inFilePath, String outFilePath ) throws FileNotFoundException, MalformedURLException, TranscoderException
	{
		File optFile = new File(inFilePath);
	  	FileInputStream fin1 = new FileInputStream(optFile);
	  	String svgURI = optFile.toURL().toString();
	  	
	  	
	  	TranscoderInput input = new TranscoderInput(svgURI);
	  	OutputStream ostream = new FileOutputStream(outFilePath);
	  	TranscoderOutput output = new TranscoderOutput(ostream);
      
	  	transcoder.transcode(input, output);
	}
		
		public static String replaceTags( Hashtable<String,String> map , String svgtext )
		{
			for(Enumeration<String> keys = map.keys() ; keys.hasMoreElements() ;)
			{				
				String key = keys.nextElement() ;
				String value = map.get(key);
				svgtext = svgtext.replace("%=" + key + "%",value);
			}
			return svgtext ;
		}
}
