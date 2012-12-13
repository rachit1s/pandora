package ddc.com.tbitsglobal.ddc.domain;

public class SearchAlgo 
{
	public static String SearchType_After = "Search_After";
	public static String SearchType_Before = "Search_Before";
	public static String SearchType_Between = "Search_Between";
	public static String SearchType_Anywhere = "Search_Anywhere";

//	public static String SearchWhat_Pattern = "Pattern";
//	public static String SearchWhat_Exact = "Exact";
	
	private long id;
	private String searchType;
	// TODO : not used. Always all the numbers will be returned.
	private Boolean searchAll;
	private String pattern;
	private String firstKeyword;
	private String secondKeyword;
	
	public SearchAlgo(long id, String searchType, Boolean searchAll,
			String pattern, String firstKeyword, String secondKeyword) {
		super();
		this.id = id;
		this.searchType = searchType;
		this.searchAll = searchAll;
		this.pattern = pattern;
		this.firstKeyword = firstKeyword;
		this.secondKeyword = secondKeyword;
	}
	/**
	 * 
	 */
	public SearchAlgo() {
		// TODO Auto-generated constructor stub
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getFirstKeyword() {
		return firstKeyword;
	}
	public void setFirstKeyword(String firstKeyword) {
		this.firstKeyword = firstKeyword;
	}
	public String getSecondKeyword() {
		return secondKeyword;
	}
	public void setSecondKeyword(String secondKeyword) {
		this.secondKeyword = secondKeyword;
	}
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
	public Boolean isSearchAll() {
		return searchAll;
	}
	public void setSearchAll(Boolean searchAll) {
		this.searchAll = searchAll;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
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
		SearchAlgo other = (SearchAlgo) obj;
		if (id != other.id)
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SearchAlgo [id=" + id + ", searchType=" + searchType
				+ ", searchAll=" + searchAll + ", pattern=" + pattern
				+ ", firstKeyword=" + firstKeyword + ", secondKeyword="
				+ secondKeyword + "]";
	}
}
