package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class RulesClient extends TbitsModelData{

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String SEQ_NO = "seq_no";
	public static final String IS_DEPLOYED = "isDeployed";

	public int id;
	public String name;
	public double seq_no;
	public String type;
	
	public void setName(String name) {
		this.name = name;
		this.set(NAME, name);
	}

	public void setType(String type) {
		this.type = type;
		this.set(TYPE, type);
	}

	public void setSeq(float seq_no) {
		this.seq_no = seq_no;
		this.set(SEQ_NO, seq_no);
		this.set(IS_DEPLOYED, (seq_no<0)?"no":"yes");
	}

	public void setId(int id) {
		this.id = id;
	}

}
