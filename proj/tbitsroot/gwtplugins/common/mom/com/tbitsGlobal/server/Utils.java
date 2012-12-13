package mom.com.tbitsGlobal.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mom.com.tbitsGlobal.client.DraftData;
import mom.com.tbitsGlobal.client.MeetingDraft;
import mom.com.tbitsGlobal.client.PrintData;
import transbit.tbits.common.DataSourcePool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gwt.json.client.JSONArray;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class Utils {
	public static MeetingDraft meetingDraftFromString(String headerString,
			String actionsString) {
		Gson gson = new Gson();
		
		TbitsTreeRequestData prefilledData = TbitsTreeRequestData.fromString(headerString);
		
		Type actionsType = new TypeToken<List<TbitsTreeRequestData>>() {}.getType();
		List<TbitsTreeRequestData> actions = gson.fromJson(actionsString, actionsType);

		MeetingDraft draft = new MeetingDraft(prefilledData, actions);

		return draft;
	}

	public static ArrayList<String> MeetingDraftToJSON(TbitsTreeRequestData headerModel, List<TbitsTreeRequestData> actions) {
		ArrayList<String> jsons = new ArrayList<String>();
		
//		jsons.add(headerModel.stringify());
		
		JSONArray actionArr = new JSONArray();
		for(TbitsTreeRequestData action : actions){
//			actionArr.set(actionArr.size(), new JSONString(action.stringify()));
		}
		
		jsons.add(actionArr.toString());
		return jsons;
	}
	
	/**
	 * Serializes and inserts PrintData Object into the database as blob
	 * @param draftId
	 * @param userId
	 * @param pd
	 * @throws Exception
	 */
	public static int savePrintData( int draftId, int userId, DraftData pd) throws Exception
	{
		if(draftId == 0){
			String sql = "select max(meeting_id) from mom_drafts";
			
			Connection con = null ;
			try
			{
				con = DataSourcePool.getConnection();
				
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				if(rs != null && rs.next()){
					Integer maxMeetingId = rs.getInt(1);
					if(maxMeetingId != null)
						draftId = maxMeetingId.intValue() + 1;
					else
						draftId = 1;
					
					rs.close();
				}else{
					draftId = 1;
				}
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new Exception("Exception occured while saving the data.");
			}
			finally
			{
				try {
					if( null != con && con.isClosed() == false)
					{
						con.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		//TODO: Wrong sql. It will overwrite all the mom_drafts if a draft already exists
		String sql = " declare @meetingId int \n" +
				"	declare @userId int \n" +
				"	declare @dataBlob varbinary(MAX) \n" +
				"	set @meetingId = ? \n" +
				"	set @userId = ? \n" +
				"	set @dataBlob = ? \n" +
				"	if exists (select * from mom_drafts where meeting_id=@meetingId and user_id=@userId)  \n" +
				"		begin	\n" +
				"			update mom_drafts \n" +
				"			set data_blob = @dataBlob where meeting_id = @meetingId and user_id = @userId \n" +
				"		end \n" +
				"	else \n" +
				"		begin \n" +
				"			insert into mom_drafts (meeting_id,user_id,data_blob) values (@meetingId,@userId,@dataBlob) \n" +
				"		end \n";

		System.out.println("My sql to insert draft : \n " + sql);
		// create the input stream from the pd object.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos) ;
		oos.writeObject(pd);
		byte[] byteArray = baos.toByteArray();
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection();
			
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, draftId);
			ps.setInt(2, userId);
			ps.setBytes(3, byteArray);
			ps.execute();
			ps.close();
			
			return draftId;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Exception occured while saving the data.");
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Retrives the PrintData Blob from database and deserialize 
	 * @param meetingId
	 * @param userId
	 * @return : PrintData / null
	 * @throws Exception
	 */
	public static DraftData retrivePrintData(int meetingId, int userId) throws Exception
	{
		String sql = "select meeting_id,user_id,data_blob from mom_drafts where meeting_id=? and user_id=? ";
		DraftData pd = null;
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, meetingId);
			ps.setInt(2, userId);
			ResultSet rs = ps.executeQuery();
			if( rs != null)
			{
				if( rs.next() )
				{
					Blob pdBlob = rs.getBlob("data_blob");
					InputStream stream = pdBlob.getBinaryStream();
					
					ObjectInputStream ois = new ObjectInputStream(stream);
					pd = (DraftData) ois.readObject();
				}
			}
			
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Exception occured while retriving data.");
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return pd;
	}
	
	public static void main(String argv[])
	{
		try
		{
			PrintData pd = new PrintData();
			int meetingId = 1 ;
			int userId = 1 ;
			
//			savePrintData(meetingId, userId, pd);
			
//			PrintData pd1 = retrivePrintData(meetingId, userId);
			
//			System.out.println("PD1 = " + pd1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
