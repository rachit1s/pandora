package corrGeneric.com.tbitsGlobal.shared.domain;

import corrGeneric.com.tbitsGlobal.shared.key.CorrNumberKey;

public class CorrNumberEntry 
{
	
	public static final String TableName = "corr_number_config" ;
	public static final String Id = "id";
	public static final String SysPrefix = "sys_prefix";
	public static final String NumType1 = "num_type1";
	public static final String NumType2 = "num_type2";
	public static final String NumType3 = "num_type3";
	public static final String NumFormat = "num_format";
	public static final String NumFields = "num_fields";
	public static final String MaxIdFormat = "max_id_format";
	public static final String MaxIdFields = "max_id_fields";
	
	
	
	public CorrNumberEntry() {
		super();
	}
	
	public CorrNumberEntry(int id, String sysPrefix, String numType1, String numType2, String numType3,
							String numberFormat, String numberFields, String maxIdFormat, String maxIdFields) {
		super();
		this.id = id;
		cnk = new CorrNumberKey();
		this.setSysPrefix(sysPrefix);
		this.setNumType1(numType1);
		this.setNumType2(numType2);
		this.setNumType3(numType3);
		this.numberFormat = numberFormat;
		this.numberFields = numberFields;
		this.maxIdFormat = maxIdFormat;
		this.maxIdFields = maxIdFields;
	}
	
	public CorrNumberEntry(int id, CorrNumberKey cnk, String numberFormat,
			String numberFields, String maxIdFormat, String maxIdFields) {
		super();
		this.id = id;
		this.cnk = cnk;
		this.numberFormat = numberFormat;
		this.numberFields = numberFields;
		this.maxIdFormat = maxIdFormat;
		this.maxIdFields = maxIdFields;
	}

	private int id;
	private CorrNumberKey cnk ;
	private String numberFormat;
	private String numberFields;
	private String maxIdFormat;
	private String maxIdFields;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getSysPrefix() {
		return this.cnk.getSysPrefix();
	}
	public void setSysPrefix(String sysPrefix) {
		this.cnk.setSysPrefix(sysPrefix);
	}
	public String getNumType1() {
		return this.cnk.getNumType1();
	}
	public void setNumType1(String numType1) {
		this.cnk.setNumType1(numType1);
	}
	public String getNumType2() {
		return this.cnk.getNumType2();
	}
	public void setNumType2(String numType2) {
		this.cnk.setNumType2(numType2);
	}
	public String getNumType3() {
		return this.cnk.getNumType3();
	}
	public void setNumType3(String numType3) {
		this.cnk.setNumType3(numType3);
	}
	
	public CorrNumberKey getCnk() {
		return cnk;
	}
	public void setCnk(CorrNumberKey cnk) {
		this.cnk = cnk;
	}
	public String getNumberFormat() {
		return numberFormat;
	}
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}
	public String getNumberFields() {
		return numberFields;
	}
	public void setNumberFields(String numberFields) {
		this.numberFields = numberFields;
	}
	public String getMaxIdFormat() {
		return maxIdFormat;
	}
	public void setMaxIdFormat(String maxIdFormat) {
		this.maxIdFormat = maxIdFormat;
	}
	public String getMaxIdFields() {
		return maxIdFields;
	}
	public void setMaxIdFields(String maxIdFields) {
		this.maxIdFields = maxIdFields;
	}
	@Override
	public String toString() {
		return "CorrNumberEntry [id=" + id + ", cnk=" + cnk + ", numberFormat="
				+ numberFormat + ", numberFields=" + numberFields
				+ ", maxIdFormat=" + maxIdFormat + ", maxIdFields="
				+ maxIdFields + "]";
	}
	
	
}
