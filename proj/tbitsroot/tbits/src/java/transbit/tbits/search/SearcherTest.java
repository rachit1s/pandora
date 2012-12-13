package transbit.tbits.search;

import java.util.ArrayList;

import transbit.tbits.indexer.LuceneSearcher;

public class SearcherTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Searcher searcher = new Searcher(1, 7, "");
		searcher.search();
        for(String s: searcher.getAllRequestIdList())
        {
        	System.out.println(s);
        }
        //ArrayList<String> requestIDs = LuceneSearcher.search(path, strquery,false);
	}

}
