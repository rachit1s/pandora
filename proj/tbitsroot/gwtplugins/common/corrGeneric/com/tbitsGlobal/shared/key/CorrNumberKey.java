package corrGeneric.com.tbitsGlobal.shared.key;

public class CorrNumberKey 
{
	private String sysPrefix;
	private String numType1;
	private String numType2;
	private String numType3;
	public String getSysPrefix() {
		return sysPrefix;
	}
	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}
	public String getNumType1() {
		return numType1;
	}
	public void setNumType1(String numType1) {
		this.numType1 = numType1;
	}
	public String getNumType2() {
		return numType2;
	}
	public void setNumType2(String numType2) {
		this.numType2 = numType2;
	}
	public String getNumType3() {
		return numType3;
	}
	public void setNumType3(String numType3) {
		this.numType3 = numType3;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((numType1 == null) ? 0 : numType1.hashCode());
		result = prime * result
				+ ((numType2 == null) ? 0 : numType2.hashCode());
		result = prime * result
				+ ((numType3 == null) ? 0 : numType3.hashCode());
		result = prime * result
				+ ((sysPrefix == null) ? 0 : sysPrefix.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CorrNumberKey other = (CorrNumberKey) obj;
		if (numType1 == null) {
			if (other.numType1 != null)
				return false;
		} else if (!numType1.equals(other.numType1))
			return false;
		if (numType2 == null) {
			if (other.numType2 != null)
				return false;
		} else if (!numType2.equals(other.numType2))
			return false;
		if (numType3 == null) {
			if (other.numType3 != null)
				return false;
		} else if (!numType3.equals(other.numType3))
			return false;
		if (sysPrefix == null) {
			if (other.sysPrefix != null)
				return false;
		} else if (!sysPrefix.equals(other.sysPrefix))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CorrNumberKey [sysPrefix=" + sysPrefix + ", numType1="
				+ numType1 + ", numType2=" + numType2 + ", numType3="
				+ numType3 + "]";
	}
	public CorrNumberKey(String sysPrefix, String numType1, String numType2,
			String numType3) {
		super();
		this.sysPrefix = sysPrefix;
		this.numType1 = numType1;
		this.numType2 = numType2;
		this.numType3 = numType3;
	}
	
	public CorrNumberKey(String sysPrefix) {
		super();
		this.sysPrefix = sysPrefix;
		
	}
	
	public CorrNumberKey() {
		super();
	}
}
