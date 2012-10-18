package com.tbitsglobal.ddc.dao;

import com.tbitsglobal.ddc.domain.SearchAlgo;

public class SearchAlgoDao 
{
	private static SearchAlgoDao sad = new SearchAlgoDao();

	private static SearchAlgo sa ;
	static
	{
		String firstWord = "";
		String secondWord = "";
		sa = new SearchAlgo(0, SearchAlgo.SearchType_After, SearchAlgo.SearchWhat_Exact, "[-A-Za-z0-9]+", firstWord,secondWord);
	}
	
	public static SearchAlgoDao getInstance()
	{
		return sad;
	}
	
	public SearchAlgo getById(long id)
	{
		return sa;
	}
}
