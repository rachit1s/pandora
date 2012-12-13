package transbit.tbits.autovue.connector;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.DocID;

/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class TbitsDocID extends DocID implements DMSDefs
{
	/**
	 * @param path : request_id/action_id/field_id/request_file_id
	 */
	public TbitsDocID(String path)
	{
		m_id = path;
	}
	public TbitsDocID() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String DocID2String() {
		return m_id;
	}

	@Override
	public DocID String2DocID(String arg0) {
		return new TbitsDocID(arg0);
	}
	
	public String getName()
	{
		String [] parts = this.DocID2String().split("/");
		return parts[parts.length-1];
	}
	
	public String toString()
	{
		return this.DocID2String();
	}
}
