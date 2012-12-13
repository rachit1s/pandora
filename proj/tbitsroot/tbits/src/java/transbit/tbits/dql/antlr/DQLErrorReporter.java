package transbit.tbits.dql.antlr;

import java.util.ArrayList;
import java.util.List;

public class DQLErrorReporter {

	private List<String> errors;
	
	public DQLErrorReporter() {
		errors = new ArrayList<String>();
	}
	
	public void addError(String error){
		errors.add(error);
	}
	
	public List<String> getErrors(){
		return errors;
	}
	
	public void reportErrors() throws Exception{
		if(errors.size() >= 1){
			String errorMessage = "";
			for(String error : errors){
				errorMessage += error + "\n";
			}
			throw new Exception(errorMessage);
		}
	}
}
