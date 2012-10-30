import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class FileClassification {
	
	public void process(List<File> files) throws Exception{
		
		
		
		
		
		List<File> dwgFiles = new ArrayList<File>();
		String transmitalText = "";
		for(File f: files){
			System.out.println("file:"+f.getAbsolutePath());
			String text = FileContentExtracter.extractContent(f).toString();
			if(text.trim().length() == 0){
				dwgFiles.add(PDF2ImageConverter.getImage(f).get(0));
			}
			else{
				transmitalText = text;
			}
		}
		
//		List<DocumentsData> transmitalData = TransmitalProcessor.process(transmitalText);
		
		List<DocumentsData> ocrData = new ArrayList<DocumentsData>();
		
		for(File f:dwgFiles){
			System.out.println("fileName"+f.getAbsolutePath());
			DocumentsData data = FindText.getData(f);
			ocrData.add(data);
		}
		
//		List<DocumentsData> common = new ArrayList<DocumentsData>(transmitalData);
		
//		common.retainAll(ocrData);
		
//		System.out.println(" transmittalData ");
//		
//		for(DocumentsData data: transmitalData){
//			data.print();
//		}
		
		System.out.println(" ocrData ");
		
		for(DocumentsData data: ocrData){
			data.print();
		}
		
//		System.out.println(" common ");
//		
//		for(DocumentsData data: common){
//			data.print();
//		}
//		
		
		
		
	}

	public static void main(String[] args) throws Exception {
		Date start = new Date();
		File f1 = new File("D:\\0-0-0-0-510CCP0036_TR_IMP_EPCM_00018.pdf");
		File f2 = new File("D:/CLBJ1542D010014_2_AF.pdf");
		List<File> files=new ArrayList<File>();
		File f = new File("D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335");
		File fi [] = f.listFiles();
		for(int i=0;i<fi.length;i++){
			files.add(fi[i]);
		}
		
		
//		files.add(f1);
//		files.add(f2);
		
		FileClassification fC = new FileClassification();
		fC.process(files);
		
		Date end = new Date();
		System.out.println("time:"+(end.getTime()-start.getTime()));
	}
}
