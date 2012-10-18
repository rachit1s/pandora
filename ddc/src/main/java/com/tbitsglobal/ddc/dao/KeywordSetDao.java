package com.tbitsglobal.ddc.dao;

import java.util.ArrayList;

import com.tbitsglobal.ddc.domain.KeywordSet;

public class KeywordSetDao
{
	private static KeywordSetDao dao = new KeywordSetDao();
	
	private static KeywordSet keywordSet ;
	
	static
	{
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("FMG DTN Number:");
		keywordSet = new KeywordSet(0, keywords);
	}
	
	public static KeywordSetDao getInstance()
	{
		return dao;
	}
	
	public KeywordSet getById(int id)
	{
		return keywordSet;
	}

}
