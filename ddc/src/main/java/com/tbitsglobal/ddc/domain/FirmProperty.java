package com.tbitsglobal.ddc.domain;

public class FirmProperty 
{
	// non null
	private long id;
	// non null
	private String loggingBAName;
	// may be null
	private String emailPattern;
	// may be null
	private String docControlUserLogin;
	
	// may be null
	private Long number1AlgoId;
	// may be null
	private String number1Field;
	// may be null
	private Long number2AlgoId;
	// may be null
	private String number2Field;
	// may be null
	private Long number3AlgoId;
	// may be null
	private String number3Field;
	// non null
	private Long dtnKeywordsId;
	
	public FirmProperty(long id, String loggingBAName, String emailPattern,
			String docControlUserLogin, Long number1AlgoId,
			String number1Field, Long number2AlgoId, String number2Field,
			Long number3AlgoId, String number3Field, Long dtnKeywordsId) {
		super();
		this.id = id;
		this.loggingBAName = loggingBAName;
		this.emailPattern = emailPattern;
		this.docControlUserLogin = docControlUserLogin;
		this.number1AlgoId = number1AlgoId;
		this.number1Field = number1Field;
		this.number2AlgoId = number2AlgoId;
		this.number2Field = number2Field;
		this.number3AlgoId = number3AlgoId;
		this.number3Field = number3Field;
		this.dtnKeywordsId = dtnKeywordsId;
	}

	
	public Long getDtnKeywordsId() {
		return dtnKeywordsId;
	}


	public void setDtnKeywordsId(Long dtnKeywordsId) {
		this.dtnKeywordsId = dtnKeywordsId;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLoggingBAName() {
		return loggingBAName;
	}
	public void setLoggingBAName(String loggingBAName) {
		this.loggingBAName = loggingBAName;
	}
	public String getEmailPattern() {
		return emailPattern;
	}
	public void setEmailPattern(String emailPattern) {
		this.emailPattern = emailPattern;
	}
	public String getDocControlUserLogin() {
		return docControlUserLogin;
	}
	public void setDocControlUserLogin(String docControlUserLogin) {
		this.docControlUserLogin = docControlUserLogin;
	}
	public Long getNumber1AlgoId() {
		return number1AlgoId;
	}
	public void setNumber1AlgoId(Long number1AlgoId) {
		this.number1AlgoId = number1AlgoId;
	}
	public String getNumber1Field() {
		return number1Field;
	}
	public void setNumber1Field(String number1Field) {
		this.number1Field = number1Field;
	}
	public Long getNumber2AlgoId() {
		return number2AlgoId;
	}
	public void setNumber2AlgoId(Long number2AlgoId) {
		this.number2AlgoId = number2AlgoId;
	}
	public String getNumber2Field() {
		return number2Field;
	}
	public void setNumber2Field(String number2Field) {
		this.number2Field = number2Field;
	}
	public Long getNumber3AlgoId() {
		return number3AlgoId;
	}
	public void setNumber3AlgoId(Long number3AlgoId) {
		this.number3AlgoId = number3AlgoId;
	}
	public String getNumber3Field() {
		return number3Field;
	}
	public void setNumber3Field(String number3Field) {
		this.number3Field = number3Field;
	}

	@Override
	public String toString() {
		return "FirmProperty [id=" + id + ", loggingBAName=" + loggingBAName
				+ ", emailPattern=" + emailPattern + ", docControlUserLogin="
				+ docControlUserLogin + ", number1AlgoId=" + number1AlgoId
				+ ", number1Field=" + number1Field + ", number2AlgoId="
				+ number2AlgoId + ", number2Field=" + number2Field
				+ ", number3AlgoId=" + number3AlgoId + ", number3Field="
				+ number3Field + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		FirmProperty other = (FirmProperty) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
