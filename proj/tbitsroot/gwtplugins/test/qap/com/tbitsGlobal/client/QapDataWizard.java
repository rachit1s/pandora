package qap.com.tbitsGlobal.client;

import java.util.HashMap;

public class QapDataWizard {

	public HashMap<String, Object> hm;
	public HashMap<String, Object> hm1;

	public QapDataWizard() {
		hm = new HashMap<String, Object>();
		hm1 = new HashMap<String, Object>();
	}
		
		public void setData(HashMap<String, Object> hm) {
			this.hm = hm;
}
		public HashMap<String, Object> getData() {
			return hm;

		}

        public void setData1(HashMap<String, Object> hm) {
	       this.hm1 = hm;
}
        public HashMap<String, Object> getData1() {
	       return hm1;

}
}