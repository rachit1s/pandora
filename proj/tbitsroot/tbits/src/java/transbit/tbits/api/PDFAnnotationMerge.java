package transbit.tbits.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentCatalog;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

/**
*
* @author Sourabh
* @version
*/

public class PDFAnnotationMerge implements TBitsPropEnum{
	private String baseFilePath;
	private LinkedList<String[]> fileQueue;
	private PDDocument outputFile;
	
	public PDFAnnotationMerge(String baseFilePath) throws APIException, IOException{
		if (isPDF(baseFilePath))
			this.baseFilePath = baseFilePath;
		else{ 
			APIException ex  = new APIException();
			ex.addException(new TBitsException("Invalid File Type"), 4);
			throw ex;
		}
		//else throw new Exception("Invalid File Type");
		this.fileQueue = new LinkedList<String[]>();
		//this.outputFile = new PDDocument();
	}
	
	public PDDocument getOutputFile(){
		return this.outputFile;
	}
	
	private boolean isPDF(String path){
		int pos = path.lastIndexOf(".") + 1;
		String ext = path.substring(pos).toLowerCase();
		if(ext.equals("pdf"))
			return true;
		else return false;
	}
	
	public boolean queueFile(String file, String author) throws APIException {
		String[] arr = {file, author};
		if(!isPDF(file)){
			APIException ex  = new APIException();
			ex.addException(new TBitsException("Invalid File Type"), 4);
			throw ex;
		}
		try{
		if(this.fileQueue.isEmpty() || this.fileQueue.contains(arr) == false){
			this.fileQueue.addLast(arr);
			return true;
		}}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean mergeFiles() throws IOException{
		ArrayList pageListBase, pageList;
		PDDocument tempDoc;
		
		this.outputFile = PDDocument.load(this.baseFilePath);
		PDPage tempPage;
		PDDocument inputDoc;
		String[] file;
		while(this.fileQueue.isEmpty() == false){
			tempDoc = this.outputFile;
			PDDocumentCatalog fileCatalogue = tempDoc.getDocumentCatalog();
			pageListBase = new ArrayList(fileCatalogue.getAllPages());
			file = (String[])this.fileQueue.poll();
			try{
				inputDoc = PDDocument.load(file[0]);
				inputDoc.getDocumentInformation().setAuthor(file[1]);
				
				fileCatalogue = inputDoc.getDocumentCatalog();
				pageList = new ArrayList(fileCatalogue.getAllPages());
			}
			catch(IOException e){
				System.out.println("Error while reading the Queued File");
				return false;
			}
			
			for(int i = 0; i < pageListBase.size(); i++){
				ArrayList annotationListBase = new ArrayList(((PDPage)pageListBase.get(i)).getAnnotations());
				ArrayList annotationList = null;
				if(pageList.size() > i)
					annotationList = new ArrayList(((PDPage)pageList.get(i)).getAnnotations());
				else 
					continue;

				ArrayList tempList = new ArrayList();
				tempList.addAll(annotationListBase);
				for(Object obj : annotationList){
					PDAnnotation ann1 = (PDAnnotation) obj;
					try{
						boolean foundSame = false;
						for(Object obj2 : annotationListBase){
							PDAnnotation ann2 = (PDAnnotation) obj2;
							if(ann1.getModifiedDate().equals(ann2.getModifiedDate()) 
									&& ann1.getAnnotationName().equals(ann2.getAnnotationName())){
								foundSame = true;
							}
						}
						if(!foundSame)
							tempList.add(ann1);
					}catch(NullPointerException ex){
						continue;
					}
				}
				tempPage = (PDPage)pageListBase.get(i);
				tempPage.setAnnotations(tempList);
			}
			this.outputFile = tempDoc;
		}
		this.removeClashes();
		return true;
	}
	
	public void saveToFile(String outputFilePath) throws COSVisitorException, IOException{
		try{
			this.outputFile.save(outputFilePath);
		}
		catch(COSVisitorException e){
			System.out.println("Error while saving the output PDF Document");
		}
	}
	
	private void removeClashes(){
		PDDocument tempDoc = this.outputFile;
		PDDocumentCatalog fileCatalogue = tempDoc.getDocumentCatalog();
		ArrayList pageList = new ArrayList(fileCatalogue.getAllPages());
		for(int i = 0; i < pageList.size(); i++){
			try{
				getClashIndex((PDPage)pageList.get(i));
			}catch(IOException ex){
				System.out.println("Error While Reading Page No. " + i);
				System.out.println(ex.getMessage());
			}
		}
		this.outputFile = tempDoc;
	}
	
	@SuppressWarnings("unchecked")
	private void getClashIndex(PDPage page) throws IOException{
		ArrayList annotationList = new ArrayList(page.getAnnotations());
		Collections.sort(annotationList, new Comparator<PDAnnotation>() {
			public int compare(PDAnnotation o1, PDAnnotation o2) {
				try{
					return o1.getModifiedDate().compareTo(o2.getModifiedDate());
				}catch(NullPointerException ex){
					return 1;
				}
			}
		});
		
		List<String> annotationNameList = new ArrayList<String>();
		
		for(int i = 0; i < annotationList.size(); i++){
			PDAnnotation annotation1 = (PDAnnotation) annotationList.get(i);
			for(int j = i + 1; j < annotationList.size(); j++){
				PDAnnotation annotation2 = (PDAnnotation) annotationList.get(j);
				try{
					if(annotation1.getAnnotationName().equals(annotation2.getAnnotationName()))
					{
						if(!annotationNameList.contains(annotation1.getAnnotationName()))
							annotationNameList.add(annotation1.getAnnotationName());
					}
				}catch(NullPointerException ex){
					continue;
				}
			}
		}
		
		int index = 1;
		for(String annotationName : annotationNameList){
			int j = 1;
			for(int i = 0; i < annotationList.size(); i++){
				PDAnnotation annotation = (PDAnnotation) annotationList.get(i);
				try{
					if(annotation.getAnnotationName().equals(annotationName)){
						String html = "[conflict " + index + "#" + j + "]  " + annotation.getContents();
						annotation.setContents(html);
						j++;
					}
				}catch(NullPointerException ex){
					continue;
				}
			}
			index++;
		}
	}
	
	private PDAnnotationRubberStamp makeRubberStamp(){
		PDAnnotationRubberStamp stamp = new PDAnnotationRubberStamp();
		stamp.setName("conflicts");
		return stamp;
	}
	
	public static File getMergedFile(TBitsFileInfo parentFile, List<TBitsFileInfo> files) throws IOException, APIException, COSVisitorException{
		String ourAttachmentLocation = APIUtil.getAttachmentLocation();//Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));
		String outputDir = APIUtil.getTMPDir();//Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
		
		String filePath = ourAttachmentLocation + "/" + parentFile.getFileLocation();
		String fileName = parentFile.getFileName();
		
		PDFAnnotationMerge mergePDF = new PDFAnnotationMerge(filePath);
		
		for(TBitsFileInfo fi : files){	
			filePath = ourAttachmentLocation + "/" + fi.getFileLocation();
			mergePDF.queueFile(filePath, "Tbits");
		}
		
		if(mergePDF.mergeFiles()){
			String outFileName = "merged_" + fileName;
			File of = new File(outputDir + "/" + outFileName);
			FileOutputStream fos = new FileOutputStream(of);
			mergePDF.getOutputFile().save(fos);
			
			return of;
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		try {
			PDFAnnotationMerge merge = new PDFAnnotationMerge("/home/sourabh/Desktop/automaticaggregationofcomments/test1.pdf");
			merge.queueFile("/home/sourabh/Desktop/automaticaggregationofcomments/test2.pdf", "sourabh");
			if(merge.mergeFiles()){
				try {
					merge.saveToFile("/home/sourabh/Desktop/automaticaggregationofcomments/result.pdf");
				} catch (COSVisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
