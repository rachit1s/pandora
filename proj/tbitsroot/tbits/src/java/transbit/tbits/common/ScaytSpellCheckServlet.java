package transbit.tbits.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.swabunga.spell.engine.Word;

public class ScaytSpellCheckServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet( HttpServletRequest aRequest, HttpServletResponse aResponse )
	{
		try {
			handleRequest(aRequest,aResponse) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void doPost( HttpServletRequest aRequest, HttpServletResponse aResponse )
	{
		try {
			handleRequest(aRequest,aResponse) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void handleRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws IOException 
	{	
		PrintWriter pw = aResponse.getWriter();
		String words = aRequest.getParameter("words");
		if(words == null)
			words = "";
		
		ArrayList<String> inputWords = new ArrayList<String>();
		String[] wordsArray = words.split(",");
		for (String s : wordsArray) {
			inputWords.add(s);
		}

		Hashtable<String, List> retSuggestions = new Hashtable<String, List>();
		ArrayList<String> retCorrectWords = new ArrayList<String>();

		TBitsSpellChecker.getInstance().spellCheck(inputWords, retSuggestions,
				retCorrectWords);
		String prefix = aRequest.getParameter("c");
		
		String json = getJSON(retSuggestions, retCorrectWords);
		
		String output = prefix + "(" + json + ");";
						 
		pw.println(output);
		
		pw.flush();
		pw.close();
	}
	
	private String getJSON(Hashtable<String, List> retSuggestions,
			ArrayList<String> retCorrectWords) {
		JsonObject jsonObj = new JsonObject();
		
		JsonArray incorrectArray = new JsonArray();

		for(String incorrectWord:retSuggestions.keySet())
		{
			JsonArray incorrectLine = new JsonArray();
			
			incorrectLine.add(new JsonPrimitive(incorrectWord));
			
			JsonArray suggestions = new JsonArray();
			ArrayList<Word> suggestionsList = (ArrayList<Word>)retSuggestions.get(incorrectWord);
			for(Word suggestionWord: suggestionsList)
			{
				suggestions.add(new JsonPrimitive(suggestionWord.getWord()));
			}
			incorrectLine.add(suggestions);

			incorrectArray.add(incorrectLine);
		}

		JsonArray correctsArray = new JsonArray();
		for(String correctWord:retCorrectWords)
		{
			correctsArray.add(new JsonPrimitive(correctWord));
		}

		jsonObj.add("incorrect", incorrectArray);
		jsonObj.add("correct", correctsArray);
		
		return jsonObj.toString();
	}
	public static void main(String[] args) {
		GsonBuilder gsonBuilder = new GsonBuilder();
	}
}
