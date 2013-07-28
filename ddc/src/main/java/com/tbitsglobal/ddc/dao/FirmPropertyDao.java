package com.tbitsglobal.ddc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import transbit.tbits.common.DataSourcePool;

import com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import com.tbitsglobal.ddc.dao.exception.FailedToUpdate;
import com.tbitsglobal.ddc.domain.FirmProperty;
import com.tbitsglobal.ddc.domain.FirmPropertyBuilder;
import com.tbitsglobal.ddc.exception.FailedToDelete;
import com.tbitsglobal.ddc.exception.FailedToFindObject;
import com.tbitsglobal.ddc.rest.DDCHelper;
import static com.tbitsglobal.ddc.rest.DDCHelper.*;

public class FirmPropertyDao 
{
	private static final Logger logger = Logger.getLogger(FirmPropertyDao.class);
	private static FirmPropertyDao fpDao = new FirmPropertyDao();;
	
	// no need of synch etc.. 
	public static FirmPropertyDao getInstance()
	{
		return fpDao;
	}

	private static final String TableName = "firm_property";
	private static final String Id = "id";
	private static final String LoggingBAName = "loggingBaName";
	private static final String DocControllerUserLogin = "docControllerUserLogin";
	private static final String EmailPattern                      = "emailPattern";
	private static final String DocControlUserLogin              = "docControlUserLogin";
	private static final String ExpectedDTNFileName              = "expectedDTNFileName";
	private static final String FromFieldAlgoId                  = "fromFieldAlgoId"; 
	private static final String ToFieldAlgoId                    = "toFieldAlgoId";
	private static final String DtnNumberAlgoId                  = "dtnNumberAlgoId";
	private static final String OwnerDocumentNumberAlgoId        = "ownerDocumentNumberAlgoId";
	private static final String ContractorDocumentNumberAlgoId   = "contractorDocumentNumberAlgoId";
	private static final String VendorDocumentNumberAlgoId       = "vendorDocumentNumberAlgoId";
	private static final String RevisionAlgoId                   = "revisionAlgoId";
	private static final String SubmissionOrDecisionCodeAlgoId   = "submissionOrDecisionCodeAlgoId";
	private static final String DtnDateTimeInDTNNoteAlgoId       = "dtnDateTimeInDTNNoteAlgoId";
	private static final String ProjectCodeAlgoId                = "projectCodeAlgoId";
	private static final String SubmissionCodeAlgoId             = "submissionCodeAlgoId";
	private static final String ExpectedTypeOfTransaction        = "expectedTypeOfTransaction";
	private static final String PrimaryRecordSearchFieldID       = "primaryRecordSearchFieldID";
	private static final String OwnerDocumentNumberFieldID       = "ownerDocumentNumberFieldID";
	private static final String ContractorDocumentNumberFieldID  = "contractorDocumentNumberFieldID";
	private static final String VendorDocumentNumberFieldID      = "vendorDocumentNumberFieldID";
	private static final String RevisionFieldID                  = "revisionFieldID";
	private static final String ReceivedFileUpdateFieldID        = "receivedFileUpdateFieldID";
	private static final String DecisionFieldID                  = "decisionFieldID";
	private static final String DtnDateTimeAsPerTBits            = "dtnDateTimeAsPerTBits";
	private static final String DtnProcessId                    = "dtnProcessId";
	private static final String DtnKeywordsId                      = "dtnKeywordsId";

	/**
	 * will search for email id pattern in the firm
	 * @param emailId
	 * @return
	 */
	
	private static final String Search_By_EmailId_Pattern = new StringBuffer().append("select * from ").append(TableName)
							.append(" where ").append(EmailPattern).append( " = ?").toString();
	
	private static final String Search_By_Doc_Controller = new StringBuffer().append("select * from ").append(TableName)
			.append(" where ").append(DocControllerUserLogin).append( " = ?").toString();

	public FirmProperty findFirmPropertyByEmailId(String emailId) throws FailedToFindObject
	{
		String pattern = emailId.substring(emailId.indexOf('@'));
		Connection con = null;
		FirmProperty fp = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(Search_By_EmailId_Pattern);
			ps.setString(1, pattern);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				fp = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with email id pattern : " + pattern);
		}
		finally
		{
			if( null != con )
				try {
					if( con.isClosed() == false )
						con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
		}
		if( null == fp )
			throw new FailedToFindObject("Firm not found with email id pattern : " + pattern);

		return fp;
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private FirmProperty createFromResultSet(ResultSet rs) throws SQLException {
 		long id                    =rs.getLong(Id);
 		String loggingBAName       =rs.getString(LoggingBAName);
 		String emailPattern        =rs.getString(EmailPattern);
 		String docControlUserLogin =rs.getString(DocControllerUserLogin);
 		String expectedDTNFileName             = rs.getString(ExpectedDTNFileName            );
 		String fromFieldAlgoId                 = rs.getString(FromFieldAlgoId                );
 		String toFieldAlgoId                   = rs.getString(ToFieldAlgoId                  );
 		String dtnNumberAlgoId                 = rs.getString(DtnNumberAlgoId                );
 		String ownerDocumentNumberAlgoId       = rs.getString(OwnerDocumentNumberAlgoId      );
 		String contractorDocumentNumberAlgoId  = rs.getString(ContractorDocumentNumberAlgoId );
 		String vendorDocumentNumberAlgoId      = rs.getString(VendorDocumentNumberAlgoId     );
 		String revisionAlgoId                  = rs.getString(RevisionAlgoId                 );
 		String submissionOrDecisionCodeAlgoId  = rs.getString(SubmissionOrDecisionCodeAlgoId );
 		String dtnDateTimeInDTNNoteAlgoId      = rs.getString(DtnDateTimeInDTNNoteAlgoId     );
 		String projectCodeAlgoId               = rs.getString(ProjectCodeAlgoId              );
 		String submissionCodeAlgoId            = rs.getString(SubmissionCodeAlgoId           );
 		String expectedTypeOfTransaction       = rs.getString(ExpectedTypeOfTransaction      );
 		String primaryRecordSearchFieldID      = rs.getString(PrimaryRecordSearchFieldID     );
 		String ownerDocumentNumberFieldID      = rs.getString(OwnerDocumentNumberFieldID     );
 		String contractorDocumentNumberFieldID = rs.getString(ContractorDocumentNumberFieldID);
 		String vendorDocumentNumberFieldID     = rs.getString(VendorDocumentNumberFieldID    );
 		String revisionFieldID                 = rs.getString(RevisionFieldID                );
 		String receivedFileUpdateFieldID       = rs.getString(ReceivedFileUpdateFieldID      );
 		String decisionFieldID                 = rs.getString(DecisionFieldID                );
 		String dtnDateTimeAsPerTBits           = rs.getString(DtnDateTimeAsPerTBits          );
 		  Long dtnProcessId                    = rs.getLong(DtnProcessId                   );
 		  Long dtnKeywordsId                   = rs.getLong(DtnKeywordsId                  );

 		return new FirmPropertyBuilder().setId(id).setContractorDocumentNumberAlgoId(contractorDocumentNumberAlgoId).setContractorDocumentNumberFieldID(contractorDocumentNumberFieldID)
 				.setDecisionFieldID(decisionFieldID).setDocControlUserLogin(docControlUserLogin).setDtnDateTimeAsPerTBits(dtnDateTimeAsPerTBits).setDtnDateTimeInDTNNoteAlgoId(dtnDateTimeInDTNNoteAlgoId)
 				.setDtnKeywordsId(dtnKeywordsId).setDtnNumberAlgoId(dtnNumberAlgoId).setDtnProcessId(dtnProcessId).setEmailPattern(emailPattern).setExpectedDTNFileName(expectedDTNFileName)
 				.setExpectedTypeOfTransaction(expectedTypeOfTransaction).setFromFieldAlgoId(fromFieldAlgoId).setOwnerDocumentNumberAlgoId(ownerDocumentNumberAlgoId).setOwnerDocumentNumberFieldID(ownerDocumentNumberFieldID)
 				.setPrimaryRecordSearchFieldID(primaryRecordSearchFieldID).setProjectCodeAlgoId(projectCodeAlgoId).setReceivedFileUpdateFieldID(receivedFileUpdateFieldID).setRevisionAlgoId(revisionAlgoId)
 				.setRevisionFieldID(revisionFieldID).setSubmissionCodeAlgoId(submissionCodeAlgoId).setSubmissionOrDecisionCodeAlgoId(submissionOrDecisionCodeAlgoId).setVendorDocumentNumberAlgoId(vendorDocumentNumberAlgoId)
 				.setVendorDocumentNumberFieldID(vendorDocumentNumberFieldID).build();
	}

	private static final String InsertSQL = "insert into " + TableName + "emailPattern,"                   
			+ "docControlUserLogin,"            
			+ "expectedDTNFileName,"            
			+ "fromFieldAlgoId,"                
			+ "toFieldAlgoId,"                  
			+ "dtnNumberAlgoId,"                
			+ "ownerDocumentNumberAlgoId,"      
			+ "contractorDocumentNumberAlgoId," 
			+ "vendorDocumentNumberAlgoId,"     
			+ "revisionAlgoId,"                 
			+ "submissionOrDecisionCodeAlgoId," 
			+ "dtnDateTimeInDTNNoteAlgoId,"     
			+ "projectCodeAlgoId,"              
			+ "submissionCodeAlgoId,"           
			+ "expectedTypeOfTransaction,"      
			+ "primaryRecordSearchFieldID,"     
			+ "ownerDocumentNumberFieldID,"     
			+ "contractorDocumentNumberFieldID,"
			+ "vendorDocumentNumberFieldID,"    
			+ "revisionFieldID,"                
			+ "receivedFileUpdateFieldID,"      
			+ "decisionFieldID,"                
			+ "dtnDateTimeAsPerTBits,"          
			+ "dtnProcessId,"                   
			+ "dtnKeywordsId,"                  
 + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
	private static final String UpdateSQL = "update " + TableName + " set " 
			+ EmailPattern + "=?,"                   
			+ DocControlUserLogin + "=?,"            
			+ ExpectedDTNFileName + "=?,"            
			+ FromFieldAlgoId + "=?,"                
			+ ToFieldAlgoId + "=?,"                  
			+ DtnNumberAlgoId + "=?,"                
			+ OwnerDocumentNumberAlgoId + "=?,"      
			+ ContractorDocumentNumberAlgoId + "=?," 
			+ VendorDocumentNumberAlgoId + "=?,"     
			+ RevisionAlgoId + "=?,"                 
			+ SubmissionOrDecisionCodeAlgoId + "=?," 
			+ DtnDateTimeInDTNNoteAlgoId + "=?,"     
			+ ProjectCodeAlgoId + "=?,"              
			+ SubmissionCodeAlgoId + "=?,"           
			+ ExpectedTypeOfTransaction + "=?,"      
			+ PrimaryRecordSearchFieldID + "=?,"     
			+ OwnerDocumentNumberFieldID + "=?,"     
			+ ContractorDocumentNumberFieldID + "=?,"
			+ VendorDocumentNumberFieldID + "=?,"    
			+ RevisionFieldID + "=?,"                
			+ ReceivedFileUpdateFieldID + "=?,"      
			+ DecisionFieldID + "=?,"                
			+ DtnDateTimeAsPerTBits + "=?,"          
			+ DtnProcessId + "=?,"                   
			+ DtnKeywordsId + "=?"                  
			+ " where " + Id + "=?";
	private static final String DeleteSQL = "delete from " + TableName + " where " + Id + "=?";
	private static final String GetAllSQL = "select * from " + TableName;
	private static final String GetByIdSQL = "select * from " + TableName + " where " + Id+  "=?";
	
	public void delete(FirmProperty fp) throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DeleteSQL);
			ps.setLong(1, fp.getId());
			
			int count = ps.executeUpdate();
			logger.debug(count + " rows affected.");
		} catch (SQLException e) {
			logger.error("Error occured while deleting FirmProperty", e);
			throw new FailedToDelete("Error occured while deleting FirmProperty", e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public FirmProperty insert(FirmProperty fp) throws FailedToInsert
	{
		Connection con = null;
		try
		{
			int[] idKeyColumn = new int[1];
			idKeyColumn[0] = 1;
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(InsertSQL, idKeyColumn);
			ps.setString(1, fp.getLoggingBAName());
			ps.setString(2, fp.getEmailPattern());
			DDCHelper.setNotNull(ps,3,fp.getDocControlUserLogin());
			DDCHelper.setNotNull(ps, 4, fp.getNumber1AlgoId());
			DDCHelper.setNotNull(ps,5,fp.getNumber1Field());
			DDCHelper.setNotNull(ps,6,fp.getNumber2AlgoId());
			DDCHelper.setNotNull(ps,7,fp.getNumber2Field());
			DDCHelper.setNotNull(ps,8,fp.getNumber3AlgoId());
			DDCHelper.setNotNull(ps, 9, fp.getNumber3Field());
			DDCHelper.setNotNull(ps, 10, fp.getDtnKeywordsId());
			
			int count = ps.executeUpdate();
			if( count != 0 )
			{
				ResultSet rs = ps.getGeneratedKeys();
				if( rs.next() )
				{
					long id = rs.getLong(1);
					fp.setId(id);
				}
			}
			logger.debug(count + " rows affected. Newly generated id : " + idKeyColumn[0]);
			
			return fp;
		} catch (SQLException e) {
			logger.error("Error occured while inserting FirmProperty", e);
			throw new FailedToInsert("Error occured while inserting FirmProperty", e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public FirmProperty update(FirmProperty fp) throws FailedToUpdate
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(UpdateSQL);
			ps.setString(1, fp.getLoggingBAName());
			ps.setString(2, fp.getEmailPattern());
			DDCHelper.setNotNull(ps,3,fp.getDocControlUserLogin());
			DDCHelper.setNotNull(ps, 4, fp.getNumber1AlgoId());
			DDCHelper.setNotNull(ps,5,fp.getNumber1Field());
			DDCHelper.setNotNull(ps,6,fp.getNumber2AlgoId());
			DDCHelper.setNotNull(ps,7,fp.getNumber2Field());
			DDCHelper.setNotNull(ps,8,fp.getNumber3AlgoId());
			DDCHelper.setNotNull(ps, 9, fp.getNumber3Field());
			DDCHelper.setNotNull(ps, 10, fp.getDtnKeywordsId());
			ps.setLong(11, fp.getId());
			
			int count = ps.executeUpdate();
			logger.debug(count + " rows affected.");
			
			return fp;
		} catch (SQLException e) {
			logger.error("Error occured while updating FirmProperty", e);
			throw new FailedToUpdate("Error occured while updating FirmProperty", e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public List<FirmProperty> getAll() throws FailedToFindObject
	{
		Connection con = null;
		List<FirmProperty> fpList = new ArrayList<FirmProperty>(); 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetAllSQL);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs  )
			{
				while(rs.next()){
					FirmProperty fp = createFromResultSet(rs);
					fpList.add(fp);
				}
				
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while fetching all firm-properties");
		}
		finally
		{
			if( null != con )
				try {
					if( con.isClosed() == false )
						con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
		}

		return fpList;
	}
	
	public FirmProperty findFirmPropertyByDocController(String userLogin) throws FailedToFindObject
	{
		Connection con = null;
		FirmProperty fp = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(Search_By_Doc_Controller);
			ps.setString(1, userLogin);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				fp = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with doc_controller : " + userLogin);
		}
		finally
		{
			if( null != con )
				try {
					if( con.isClosed() == false )
						con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
		}
		if( null == fp )
			throw new FailedToFindObject("Firm not found with doc controller : " + userLogin);

		return fp;
	}
	
	public FirmProperty getById(Long id) throws FailedToFindObject
	{
		Connection con = null;
		FirmProperty fp = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetByIdSQL);
			ps.setLong(1, id);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				fp = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with Id = " + id);
		}
		finally
		{
			if( null != con )
				try {
					if( con.isClosed() == false )
						con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
		}
		if( null == fp )
			throw new FailedToFindObject("Exception occured while finding firm with Id = " + id);

		return fp;
	}
}
