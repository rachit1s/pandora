
public class DocumentsData {
		private String docNo1;
		
		private String docNo2;
		
		private String docNo3;
		
		

		private String rev;
		
		private String cat;
		
		public DocumentsData(){
			
		}

		public String getDocNo3() {
			return docNo3;
		}

		public void setDocNo3(String docNo3) {
			this.docNo3 = docNo3;
		}
		
		public String getDocNo1() {
			return docNo1;
		}

		public void setDocNo1(String docNo1) {
			this.docNo1 = docNo1;
		}

		public String getDocNo2() {
			return docNo2;
		}

		public void setDocNo2(String docNo2) {
			this.docNo2 = docNo2;
		}

		public String getRev() {
			return rev;
		}

		public void setRev(String rev) {
			this.rev = rev;
		}

		public String getCat() {
			return cat;
		}

		public void setCat(String cat) {
			this.cat = cat;
		}
		
		public void print(){
			System.out.println("document:"+docNo1+","+docNo2+","+docNo3+","+rev+","+cat);
		}
		
		public boolean equals(Object data){
			DocumentsData d = (DocumentsData) data;
				
			if(!d.getDocNo1().equals(docNo1)){
				return false;
			}
			
			if(!d.getRev().equals(rev)){
				return false;
			}
			
			return true;
		}
}