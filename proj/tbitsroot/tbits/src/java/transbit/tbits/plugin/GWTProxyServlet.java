package transbit.tbits.plugin;

import javax.servlet.ServletException;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GWTProxyServlet extends RemoteServiceServlet{
	
	@Override
	public String processCall(String payload) throws SerializationException {
		Thread.currentThread().setContextClassLoader(PluginManager.getInstance().cl); 
		RPCRequest rpcRequest = RPC.decodeRequest(payload, null, this);
		onAfterRequestDeserialized(rpcRequest);
	     
		String clazz = rpcRequest.getMethod().getDeclaringClass().getName();
		
		Class<? extends TbitsRemoteServiceServlet> servletClazz = GWTProxyServletManager.getInstance().getServlet(clazz);
		TbitsRemoteServiceServlet servlet = null;
		try {
			servlet = servletClazz.newInstance();
			servlet.init(this.getServletConfig());
			servlet.setRequest(this.getThreadLocalRequest());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		
		return RPC.invokeAndEncodeResponse(servlet, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest.getSerializationPolicy());
	}
}
