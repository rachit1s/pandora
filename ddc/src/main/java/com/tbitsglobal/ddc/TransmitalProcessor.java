import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class TransmitalProcessor {
	
	public static List<DocumentsData> process(String result){
		
//		 System.out.println("result:"+result);
		 String projectNo = result.substring(result.indexOf("TRANSMITTAL NO: " )+"TRANSMITTAL NO: ".length(),result.indexOf('-', result.indexOf("TRANSMITTAL NO: " )+"TRANSMITTAL NO: ".length())); 
		 System.out.println("projectNo:"+projectNo);
		 
		 
		 String table = result.substring(result.indexOf("here below"),result.indexOf( "Comments"));
//		 System.out.println("table:"+table);
		 StringTokenizer token = new StringTokenizer(table);
		 List<DocumentsData> data = new ArrayList<DocumentsData>();
		 while(token.hasMoreElements()){
			 
			 
			 String ele = String.valueOf(token.nextElement());
//			 System.out.println("ele:"+ele);
			  if(ele.startsWith(projectNo)){
				 DocumentsData record = new DocumentsData();
				 record.setDocNo1(ele);
				 
				 ele = String.valueOf(token.nextElement());
				 if(ele.startsWith(projectNo) ){
					 record.setDocNo2(ele);
					 
					 ele = String.valueOf(token.nextElement());
					 while(ele.length() != 1){
						 record.setDocNo2(record.getDocNo2().concat(ele));
						 ele = String.valueOf(token.nextElement());
					 }
					 						 
					 if(ele.length() == 1){
						 record.setRev(ele);
					 }
					 
					 ele = String.valueOf(token.nextElement());
					 if(ele.length() == 1){
						 record.setCat(ele);
						 data.add(record);
					 }
				 }
				
				 
			 }
		 }
		 
		 for(int i=0;i<data.size();i++){
			 data.get(i).print();
		 }
		 
		 return data;
		 
	}

}
