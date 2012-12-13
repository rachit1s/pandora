package commons.com.tbitsGlobal.utils.server.plugins;


import transbit.tbits.plugin.TbitsRemoteServiceServlet;

public class GWTProxyServletKey {
	
	private String serviceClassName;
	private String moduleName;
	private Class<? extends TbitsRemoteServiceServlet> serverClass;
	
	/**
	 * Default Constructor.
	 * @param serviceClass - The name of the Service Class 
	 * @param moduleName - The name of the module
	 * @param serverClass - The server class which is going to use the service class
	 */
	public GWTProxyServletKey(String serviceClass, String moduleName, Class<? extends TbitsRemoteServiceServlet> serverClass){
		this.serviceClassName 	= serviceClass;
		this.moduleName 		= moduleName;
		this.serverClass		= serverClass;
	}
	
	public String getServiceClassName(){
		return this.serviceClassName;
	}
	
	public String getModuleName(){
		return this.moduleName;
	}
	
	public Class<? extends TbitsRemoteServiceServlet> getServerClass(){
		return this.serverClass;
	}
	
}
