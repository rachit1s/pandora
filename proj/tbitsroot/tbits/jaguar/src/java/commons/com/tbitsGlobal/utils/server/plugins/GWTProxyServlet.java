package commons.com.tbitsGlobal.utils.server.plugins;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import transbit.tbits.plugin.TbitsRemoteServiceServlet;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GWTProxyServlet extends RemoteServiceServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public String processCall(String payload) throws SerializationException {
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
	
	@Override
	protected String readContent(HttpServletRequest request)
			throws ServletException, IOException {
		String content = super.readContent(request);
		return content;
	}
}
