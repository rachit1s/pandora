package transbit.tbits.domain;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface TransmittalHandler {
	public void doGet(HttpServletRequest req,  HttpServletResponse res) throws ServletException, IOException;
	public void doPost(HttpServletRequest req,  HttpServletResponse res) throws ServletException, IOException;
}
