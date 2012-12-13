package transbit.tbits.domain;

import java.util.Date;

public class FileRepoIndexObject 
{
	private int id;
	private String location;
	private String name;
	private Date createDate ;
	private long size;
	private String hash;
	private int securityCode;
	
	public FileRepoIndexObject()
	{
	}
	
	public FileRepoIndexObject(int id, String location, String name,
			Date createDate, long size, String hash, int securityCode) {
		super();
		this.id = id;
		this.location = location;
		this.name = name;
		this.createDate = createDate;
		this.size = size;
		this.hash = hash;
		this.securityCode = securityCode;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public int getSecurityCode() {
		return securityCode;
	}
	public void setSecurityCode(int securityCode) {
		this.securityCode = securityCode;
	}

	@Override
	public String toString() {
		return "FileRepoIndexObject [id=" + id + ", name=" + name + ", size="
				+ size + ", createDate=" + createDate + ", hash=" + hash
				+ ", location=" + location + ", securityCode=" + securityCode
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		FileRepoIndexObject other = (FileRepoIndexObject) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
