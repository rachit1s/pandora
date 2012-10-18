package com.tbitsglobal.ddc.domain;

import java.util.ArrayList;

public class KeywordSet 
{
	private Integer id;
	
	/**
	 * These are the key words which if found would be used to identify 
	 */
	private ArrayList<String> keyWords;

	public KeywordSet(Integer id, ArrayList<String> keyWords) {
		super();
		this.id = id;
		this.keyWords = keyWords;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ArrayList<String> getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(ArrayList<String> keyWords) {
		this.keyWords = keyWords;
	}

	@Override
	public String toString() {
		return "KeywordSet [id=" + id + ", keyWords=" + keyWords
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		KeywordSet other = (KeywordSet) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
