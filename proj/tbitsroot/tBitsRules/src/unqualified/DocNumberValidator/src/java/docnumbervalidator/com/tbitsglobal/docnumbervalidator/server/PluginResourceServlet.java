package docnumbervalidator.com.tbitsglobal.docnumbervalidator.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.Configuration;

public class PluginResourceServlet implements IProxyServlet {
	public static String URL = "get_docnumbervalidator_plugin_res";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if(pathInfo.contains(URL + "/"))
			pathInfo = pathInfo.split(URL + "/")[1];
		else
			return;
		if(pathInfo == null || pathInfo.trim().equals(""))
			return;
		if(pathInfo.indexOf('?') >= 0){
			pathInfo = pathInfo.substring(0, pathInfo.indexOf('?'));
		}
		if(pathInfo == null || pathInfo.trim().equals(""))
			return;
		String fileName = pathInfo;
		File dir = Configuration.findPath("plugins");
		dir = new File(dir.getPath() + "/docnumbervalidator/war");
		if(dir.exists()){
			File file = new File(dir.getPath() + "/" + fileName);
			if(file.exists()){
				ServletOutputStream os = response.getOutputStream();
				FileInputStream fis = new FileInputStream(file);
				readFile(fis, os);
				System.out.println("Found file : " + file.getPath());
			}else{
				dir = new File(dir.getPath() + "/docnumbervalidator");
				file = new File(dir.getPath() + "/" + fileName);
				if(file.exists()){
					ServletOutputStream os = response.getOutputStream();
					FileInputStream fis = new FileInputStream(file);
					readFile(fis, os);
					System.out.println("Found file : " + file.getPath());
				}else
					System.out.println("Not Found Dir : " + file.getPath());
			}
		}else
			System.out.println("Not Found Dir : " + dir.getPath());
	}
	
	private void readFile(InputStream fis, OutputStream os) throws IOException{
		byte[] b = new byte[1024];
		int bytesRead = 0;
		while((bytesRead = fis.read(b)) != -1){
			os.write(b, 0, bytesRead);
		}
		os.close();
		fis.close();
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	
	public String getName() {
		return URL;
	}
}
