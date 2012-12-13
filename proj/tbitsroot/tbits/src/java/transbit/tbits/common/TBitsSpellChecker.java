package transbit.tbits.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.WordTokenizer;
import com.swabunga.spell.event.XMLWordFinder;

class WordSuggestions
{
	String word ;
	ArrayList<String> suggestions ;
	public WordSuggestions( String word, ArrayList<String> suggestions )
	{
		this.word = word ;
		this.suggestions = suggestions == null ? new ArrayList<String>() : suggestions ;
	}
	
	public WordSuggestions(String word)
	{
		this.word = word ;
		this.suggestions = new ArrayList<String>() ;
	}
	
	public void addSuggestion( String sug )
	{
		if( null != sug )
			suggestions.add(sug) ;
	}
//	
//	public int hashCode()
//	{
//		return 
//	}
	
	public String toString()
	{
		return "word = " + word + " : suggestions = " + suggestions ;
	}
	public boolean equals( WordSuggestions ws )
	{
		if( word.equalsIgnoreCase(ws.word))
			return true ;
		else
			return false ;
	}
}

public class TBitsSpellChecker 
{
//	public static final String DICT_FILE = "enlish.0" ; 
	
	private static TBitsSpellChecker instance = null ;
	
	private SpellDictionary dictionary = null ;
	
	private TBitsSpellChecker() throws FileNotFoundException, IOException
	{		// initialize the dict
		ClassLoader myLoader = this.getClass().getClassLoader();
		dictionary = new SpellDictionaryHashMap(new InputStreamReader( myLoader.getResourceAsStream("spellcheck-english.0" ) ) );
		if(dictionary == null )
			throw new IOException("Cannot Initialize the dictionary.") ;
	}
	
	public static synchronized TBitsSpellChecker getInstance() throws FileNotFoundException, IOException
	{
		if( instance == null )
		{
			instance = new TBitsSpellChecker() ;
		}
		
		return instance ;
	}
	
	/**
	 * Returns the suggestions and correct words for a list of words
	 * @param inputWords
	 * @param retSuggestions
	 * @param retCorrectWords
	 */
	public void spellCheck(List<String> inputWords, Hashtable<String, List> retSuggestions, ArrayList<String> retCorrectWords)
	{
		final Hashtable<String, List> suggestions = new Hashtable<String, List>();
		final ArrayList<String> inCorrectWords = new ArrayList<String>();
		final ArrayList<String> correctWords = new ArrayList<String>();
		
		SpellChecker spellCheck = new SpellChecker(dictionary);		
			spellCheck.addSpellCheckListener(new SpellCheckListener() {

				@Override
				public void spellingError(SpellCheckEvent event) {
					List wordSuggestions = event.getSuggestions();
					if(wordSuggestions.size() > 0)
					{
						suggestions.put(event.getInvalidWord(), wordSuggestions);
						inCorrectWords.add(event.getInvalidWord());
					}
					else
					{
						correctWords.add(event.getInvalidWord());
					}
				}
			});
		WordTokenizer wordTokenizer = new MyWordTokenizer(inputWords);
		spellCheck.checkSpelling(wordTokenizer);
		

		retSuggestions.putAll(suggestions);
		
		inputWords.removeAll(inCorrectWords);
		inputWords.removeAll(correctWords);
		retCorrectWords.addAll(correctWords);
		retCorrectWords.addAll(inputWords);
	}
	
	public ArrayList<WordSuggestions> getErrors( String string)
	{
		final ArrayList<WordSuggestions> errors = new ArrayList<WordSuggestions>() ; 
		final ArrayList<String> allMisWords = new ArrayList<String>() ;
		SpellChecker spellCheck = new SpellChecker(dictionary);		
		spellCheck.addSpellCheckListener(new SpellCheckListener()
											{
												public void spellingError(SpellCheckEvent event) 
												{
													List suggestions = event.getSuggestions();
												    if (suggestions.size() > 0) 
												    {			      
												      String misWord = event.getInvalidWord() ;
												      System.out.println("MISSPELT WORD: " + misWord );
												      
												      if( allMisWords.contains(misWord))
												    	  return ;
												      else
												    	  allMisWords.add(misWord) ;
												      
												      WordSuggestions ws = new WordSuggestions( misWord ) ;
												      for( Iterator iter = suggestions.iterator() ; iter.hasNext() ; )
												      {
												    	  Word sugWord = (Word) iter.next() ;
												    	  ws.addSuggestion(sugWord.getWord()) ;
												      }
												      
												      errors.add(ws) ;
												    } else 
												    {
												      System.out.println("MISSPELT WORD: " + event.getInvalidWord());
												      System.out.println("\tNo suggestions");
												    }		    
												}
												
											}
										) ;
		
		XMLWordFinder xmlWordFinder = new XMLWordFinder(string) ;
		StringWordTokenizer swt = new StringWordTokenizer( xmlWordFinder )  ;
		spellCheck.checkSpelling(swt);
		
		return errors ;
	}
	public static void main(String[] args) {
		

		ArrayList<String> inputWords = new ArrayList<String>();
		
		String[] str = "Hey you looking through the eye getting loney getting.".split(" ");
		for(String s: str)
			inputWords.add(s);
		
		Hashtable<String, List> retSuggestions = new  Hashtable<String, List>();
		ArrayList<String> retCorrectWords = new ArrayList<String>();
		
		try {
			TBitsSpellChecker.getInstance().spellCheck(inputWords, retSuggestions, retCorrectWords);
			
			for(String key:retSuggestions.keySet())
			{
				System.out.println("Suggestions for " + key + ": ");
				System.out.println(retSuggestions.get(key) ) ;
			}
			System.out.println("Correct Workds: ");
			System.out.println(retCorrectWords);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
