package imp;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Random;


import com.ibm.icu.text.SimpleDateFormat;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;


import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class QRCodeGeneration implements IRule{
	
	
	private static final String PRINT = "print";
	private static final String WATER_MARK = "WaterMark";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		// TODO Auto-generated method stub
		if(!isAddRequest){
			//---------------------Getting The Source Attachment Field-------------------
			Field sourceAttachmentField=null;
			try {
				sourceAttachmentField=getSourceAttachmentField(connection,ba);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (null == sourceAttachmentField){ 
				return new RuleResult(true, "Continuing without generation of QR Code generation as its not applicable to BA: "
						+ ba.getSystemPrefix() + ", as no field for source attachment field is configured.");
			}
			
			//---------------------Getting The Target Attachment Field-------------------

			Field targetAttachmentField=null;
			try {
				targetAttachmentField=getTargettargetAttachmentField(connection,ba);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			if (null == targetAttachmentField){ 
				return new RuleResult(true, "Continuing without generation of QR Code as its not applicable to BA: "
						+ ba.getSystemPrefix() + ", as no field for Target attachment field is configured.");
			}
			
			
			

			
			String location="";
			String name="";
			String sql="";
			String inputHash="`";
			String outputHash="";
			Collection<AttachmentInfo> allAtts=(Collection<AttachmentInfo>) currentRequest.getObject(sourceAttachmentField);

			try {
				ArrayList<AttachmentInfo> abc1=new ArrayList<AttachmentInfo>();
				for(AttachmentInfo ad:allAtts){
					sql="select location,name,hash from file_repo_index where id=?";
					PreparedStatement ps;
						ps = connection.prepareStatement(sql);
						ps.setInt(1, ad.repoFileId);
						ResultSet rs=ps.executeQuery();
						if(rs.next()){
							location=rs.getString(1);
							name=rs.getString(2);
							inputHash=rs.getString(3);
						}
						
						sql="select count(*) from QR_code_generation where output_hashcode=? and sys_id=? and request_id=?";
//						try{
							PreparedStatement ps2;
							ps2 = connection.prepareStatement(sql);
							ps2.setString(1, inputHash);
							ps2.setInt(2, currentRequest.getSystemId());
							ps2.setInt(3, currentRequest.getRequestId());
							ResultSet rs2=ps2.executeQuery();
							if(rs2.next()){
								if(rs2.getInt(1)>0)
								{
									abc1.add(ad);
									System.out.println("no qr generation for this attachments");
								}
							
//						}catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//						}
//					
//					catch (Exception e) {
//						// TODO: handle exception
//					}
						else
						{
				com.lowagie.text.pdf.PdfReader reader=new com.lowagie.text.pdf.PdfReader(APIUtil.getAttachmentLocation()+File.separator+location);
				File tempDir = Configuration.findPath("webapps/tmp");
				
				com.lowagie.text.pdf.PdfStamper stamp=new com.lowagie.text.pdf.PdfStamper(reader, new FileOutputStream(tempDir+File.separator+name));
			     String str=check4DigitsCode(connection);
				
				ByteArrayOutputStream out = QRCode.from(PropertiesHandler.getProperty("transbit.tbits.nearestInstance")+"/qr?Q="+str)
						.to(ImageType.PNG).withSize(85, 85).stream();
				FileOutputStream fout = new FileOutputStream(new File(
						tempDir+File.separator+"QRCode.PNG"));
				fout.write(out.toByteArray());
				fout.flush();
				fout.close();
				Image image = Image.getInstance(tempDir+File.separator+"QRCode.PNG");

				com.lowagie.text.pdf.PdfContentByte overContent = stamp.getOverContent( 1 );
				com.lowagie.text.Rectangle r1=reader.getPageSize(1);
				
				////////rotation///////////
				Rectangle cropBox = stamp.getReader().getCropBox(1);
	        	int rotation = reader.getPageRotation(1);
	        	float absoluteY = 0.0f;
	        	float absoluteX = 0.0f;
	        	switch( rotation ) {
	        	case 0:
	        		absoluteX = cropBox.getLeft(); 
	        		absoluteY = cropBox.getBottom();        	     	   
	        		break;
	        	case 90:
	        		absoluteX = cropBox.getBottom();
	        		absoluteY = cropBox.getLeft();        	
	        		break;
	        	case 180:
	        		absoluteX = cropBox.getTop() - (98*cropBox.getTop()/100);
	        		absoluteY = cropBox.getLeft();        	
	        		break;
	        	case 270:
	        		absoluteX = cropBox.getRight() - (98*cropBox.getRight()/100);
	        		absoluteY = 10.0f + cropBox.getLeft();        	
	        		break;
	        	};
				//////////////////////////

//				image.setAbsolutePosition(r1.getWidth()/2f, 7);
				image.setAbsolutePosition(absoluteX, absoluteY+10);
				///////////ankit////////////
		        //adding QR Code Layer
		        PdfLayer wmLayer = new PdfLayer(WATER_MARK, stamp.getWriter());
		        wmLayer.setOnPanel(true);

		        //set layer parameters
		        wmLayer.setPrint(PRINT, true);
		        wmLayer.setOn(true);
		        wmLayer.setView(true);
				overContent.beginLayer(wmLayer);
				/////////////krishna////////////////////
				overContent.addImage(image);
	            /////////////krishna////////////////////
				
				// adding current date
				BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
				overContent.beginText();
				overContent.setFontAndSize(bf, 105/10);
				overContent.setColorFill(Color.red);
//				overContent.showTextAligned(Element.ALIGN_MIDDLE, "RFC DATE: " + getCurrentDate(), (r1.getWidth()/2f)-25, 
//	        			10, 0);  
				overContent.showTextAligned(Element.ALIGN_LEFT, "RFC DATE: " + getCurrentDate(), absoluteX, 
	        			absoluteY + 4, 0);  
				overContent.endText();            
				overContent.stroke();
				overContent.endLayer();
				/////////ankit///////////
				
				stamp.close();
				reader.close();
			
				File f=new File(tempDir+File.separator+name);
				Uploader up=new Uploader();
				up.setFolderHint(ba.getSystemPrefix());
				AttachmentInfo uploaded=up.moveIntoRepository(f);
				sql="select hash from file_repo_index where id=?";
				PreparedStatement ps1;
				try {
					ps1 = connection.prepareStatement(sql);
					ps1.setInt(1, uploaded.getRepoFileId());
					ResultSet rs1=ps1.executeQuery();
					while(rs1.next()){
						outputHash=rs1.getString(1);
					}
					System.out.println();
					int sysid=ba.getSystemId();
					int requestid=currentRequest.getRequestId();
					int actionid=0;
					sql="select max(action_id) from actions_ex where sys_id=? and request_id=?";
					Connection con=DataSourcePool.getConnection();
					ps1=con.prepareStatement(sql);
					ps1.setInt(1, sysid);
					ps1.setInt(2, requestid);
					rs1=ps1.executeQuery();
					if(rs1.next())
						actionid=rs1.getInt(1)+1;
					sql="insert into QR_code_generation values("+sysid+","+requestid+","+actionid+",'"+inputHash+"','"+outputHash+"','"+str+"')";
					ps1=connection.prepareStatement(sql);
					ps1.executeUpdate();
					con.close();
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				abc1.add(uploaded);
				}
			}
			}
				currentRequest.setObject(targetAttachmentField, abc1);

			} catch (Exception e) {
				// Do Logging
				e.printStackTrace();
			} 



		}

		return null;
	}

	

	private String check4DigitsCode(Connection connection) {
		// TODO Auto-generated method stub
		char a[]={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G'
				,'H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
		Random r=new Random();
		String sql="select count(*) from qr_code_generation where code like ?";
		PreparedStatement ps=null;
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while(true){
		String str="";
		System.out.println(a.length);
		for(int i=0;i<4;i++){
			str+=a[r.nextInt(62)];
			
		}
		try {
			
			ps.setString(1, str);
			ResultSet r1=ps.executeQuery();
			if(r1.next()){
				if(r1.getInt(1)==0)
					return str;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return str;
		}
	}



	private Field getTargettargetAttachmentField(Connection connection,
			BusinessArea ba) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = connection.prepareStatement("select output_field_id from QR_code_configuration where sys_id = ?");
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if ((rs != null) && rs.next()){
			int srcFieldId = rs.getInt(1);
			Field srcTargetField = null;
			try {
				srcTargetField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), srcFieldId);
			} catch (DatabaseException e) {
				e.printStackTrace();
				return null;
			}
			return (srcTargetField == null) ? null : srcTargetField;
		}
		return null;
		
	}

	private Field getSourceAttachmentField(Connection connection,
			BusinessArea ba) throws SQLException{
		// TODO Auto-generated method stub
		PreparedStatement ps = connection.prepareStatement("select input_field_id from QR_code_configuration where sys_id = ?");
		ps.setInt(1, ba.getSystemId());
		ResultSet rs = ps.executeQuery();
		if ((rs != null) && rs.next()){
			int srcFieldId = rs.getInt(1);
			Field srcDrawingField = null;
			try {
				srcDrawingField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), srcFieldId);
			} catch (DatabaseException e) {
				e.printStackTrace();
				return null;
			}
			return (srcDrawingField == null) ? null : srcDrawingField;
		}
		return null;
		
	}
	///////////ankit////////////////
	//current date
	public static String getCurrentDate(){
		Calendar cal = Calendar.getInstance();
		String curDate = "";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		curDate = sdf.format(date);
		return sdf.format(cal.getTime());
	}
   ///////////ankit////////////////

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
