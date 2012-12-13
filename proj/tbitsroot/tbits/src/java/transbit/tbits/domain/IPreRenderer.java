package transbit.tbits.domain;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.exception.TBitsException;

public interface IPreRenderer {

	void process(HttpServletRequest request, HttpServletResponse response,
			Hashtable<String, Object> tagTable, ArrayList<String> tagList) throws TBitsException;
	double getSequence();

}
