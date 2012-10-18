package com.tbitsglobal.ddc.domain;

public class FirmProperty 
{
	private long id;
	
	private String loggingBAName;
	private String emailPattern;
	private String docControlUserLogin;
	private Integer number1AlgoId;
	private String number1Field;
	private Integer number2AlgoId;
	private String number2Field;
	private Integer number3AlgoId;
	private String number3Field;
	private Integer dtnKeywordsId;
	
	public FirmProperty(long id, String loggingBAName, String emailPattern,
			String docControlUserLogin, Integer number1AlgoId,
			String number1Field, Integer number2AlgoId, String number2Field,
			Integer number3AlgoId, String number3Field, Integer dtnKeywordId) {
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
		this.dtnKeywordsId = dtnKeywordId;
	}

	
	public Integer getDtnKeywordsId() {
		return dtnKeywordsId;
	}


	public void setDtnKeywordsId(Integer dtnKeywordsId) {
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
	public Integer getNumber1AlgoId() {
		return number1AlgoId;
	}
	public void setNumber1AlgoId(Integer number1AlgoId) {
		this.number1AlgoId = number1AlgoId;
	}
	public String getNumber1Field() {
		return number1Field;
	}
	public void setNumber1Field(String number1Field) {
		this.number1Field = number1Field;
	}
	public Integer getNumber2AlgoId() {
		return number2AlgoId;
	}
	public void setNumber2AlgoId(Integer number2AlgoId) {
		this.number2AlgoId = number2AlgoId;
	}
	public String getNumber2Field() {
		return number2Field;
	}
	public void setNumber2Field(String number2Field) {
		this.number2Field = number2Field;
	}
	public Integer getNumber3AlgoId() {
		return number3AlgoId;
	}
	public void setNumber3AlgoId(Integer number3AlgoId) {
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
