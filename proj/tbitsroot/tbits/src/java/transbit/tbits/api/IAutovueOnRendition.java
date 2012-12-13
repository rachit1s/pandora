package transbit.tbits.api;

import java.io.InputStream;

import transbit.tbits.autovue.connector.ActionContext;

import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;

public interface IAutovueOnRendition 
{
	public Object execute(final ActionContext    context,
            final DMSSession    session,
            final DMSQuery      query,
            final DMSArgument[] args, InputStream fIn) throws Exception;
	
	public double getOrder();
	public String getName();
	public String getDescription();
}
