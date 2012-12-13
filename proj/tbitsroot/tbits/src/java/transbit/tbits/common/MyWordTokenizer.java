package transbit.tbits.common;

import java.util.List;

import com.swabunga.spell.event.WordTokenizer;


public class MyWordTokenizer implements WordTokenizer {

	private List<String> baseList;
	int index = -1;
	public MyWordTokenizer(List<String> inputWords)
	{
		this.baseList = inputWords;
	}
	
	@Override
	public String getContext() {
		if(index > -1 && index < baseList.size())
			return baseList.get(index);
		return null;
	}

	@Override
	public int getCurrentWordCount() {
		// TODO Auto-generated method stub
		return index+1;
	}

	@Override
	public int getCurrentWordEnd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentWordPosition() {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public boolean isNewSentence() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasMoreWords() {
		// TODO Auto-generated method stub
		return index < (baseList.size() - 1);
	}

	@Override
	public String nextWord() {
		// TODO Auto-generated method stub
		index++;
		return getContext();
	}

	@Override
	public void replaceWord(String newWord) {
	}

}
