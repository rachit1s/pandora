package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrNumberConfigClient extends TbitsModelData {

	public static String Id = "id";
	public static String SysPrefix = "sys_prefix";

	public static String NumType1 = GenericParams.NumType1;
	public static String NumType2 = GenericParams.NumType2;
	public static String NumType3 = GenericParams.NumType3;

	public static String NumFormat = "num_format";
	public static String NumFields = "num_fields";
	public static String MaxIdFormat = "max_id_format";
	public static String MaxIdFields = "max_id_fields";
	public static String STATUS = "status";

	public CorrNumberConfigClient() {
		super();
	}

	// --------------get/set report id-----------------------------//
	public String getId() {
		return (String) this.get(Id);
	}

	public void setId(String id) {
		this.set(Id, id);
	}

	// ----------------get/set sysPrefix------------------//
	public String getSysPrefix() {
		return (String) this.get(SysPrefix);
	}

	public void setSysPrefix(String sysPrefix) {
		this.set(SysPrefix, sysPrefix);
	}

	// ---------------get/set NumType1 -----------------//

	public TypeClient getNumType1() {
		return (TypeClient) this.get(NumType1);
	}

	public void setNumType1(TypeClient numType1) {
		this.set(NumType1, numType1);
	}

	// ---------------get/set NumType2 -----------------//

	public TypeClient getNumType2() {
		return (TypeClient) this.get(NumType2);
	}

	public void setNumType2(TypeClient numType2) {
		this.set(NumType2, numType2);
	}

	// ---------------get/set NumType3 -----------------//

	public TypeClient getNumType3() {
		return (TypeClient) this.get(NumType3);
	}

	public void setNumType3(TypeClient numType3) {
		this.set(NumType3, numType3);
	}

	// ---------------get/set NumFormat -----------------//

	public String getNumFormat() {
		return (String) this.get(NumFormat);
	}

	public void setNumFormat(String numFormat) {
		this.set(NumFormat, numFormat);
	}

	// ---------------get/set NumFields -----------------//

	public String getNumFields() {
		return (String) this.get(NumFields);
	}

	public void setNumFields(String numFields) {
		this.set(NumFields, numFields);
	}

	// ---------------get/set MaxIdFormat -----------------//

	public String getMaxIdFormat() {
		return (String) this.get(MaxIdFormat);
	}

	public void setMaxIdFormat(String maxIdFormat) {
		this.set(MaxIdFormat, maxIdFormat);
	}

	// ---------------get/set MaxIdFields -----------------//

	public String getMaxIdFields() {
		return (String) this.get(MaxIdFields);
	}

	public void setMaxIdFields(String maxIdFields) {
		this.set(MaxIdFields, maxIdFields);
	}

	// ------------getter/setter for status-----------------//
	public String getStatus() {
		return (String) this.get(STATUS);
	}

	public void setStatus(String status) {
		this.set(STATUS, status);
	}

}
