package ddc.com.tbitsglobal.ddc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import transbit.tbits.common.DataSourcePool;


import ddc.com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import ddc.com.tbitsglobal.ddc.dao.exception.FailedToUpdate;
import ddc.com.tbitsglobal.ddc.domain.FirmProperty;
import ddc.com.tbitsglobal.ddc.domain.FirmPropertyBuilder;
import ddc.com.tbitsglobal.ddc.exception.FailedToDelete;
import ddc.com.tbitsglobal.ddc.exception.FailedToFindObject;
import ddc.com.tbitsglobal.ddc.rest.DDCConstant;
import ddc.com.tbitsglobal.ddc.rest.DDCHelper;
import static ddc.com.tbitsglobal.ddc.rest.DDCHelper.*;

public class FirmPropertyDao 
{
	private static final Logger logger = Logger.getLogger(FirmPropertyDao.class);
	private static FirmPropertyDao fpDao = new FirmPropertyDao();;
	
	// no need of synch etc.. 
	public static FirmPropertyDao getInstance()
	{
		return fpDao;
	}

	public static final String TableName = "firm_property";
	public static String Id = "id" ;
	public static String FromAgencyEmailAddress = "fromAgencyEmailAddress" ;
	public static String DtnExpected = "dtnExpected";
	public static String ExpectedDTNFileName = "expectedDTNFileName" ;
	public static String DtnLetterIdentificationString = "dtnLetterIdentificationString" ;
	public static String FromFieldAlgoId = "fromFieldAlgoId" ;
	public static String ToFieldAlgoId = "toFieldAlgoId" ;
	public static String DtnNumberAlgoId = "dtnNumberAlgoId" ;
	public static String OwnerDocumentNumberAlgoId = "ownerDocumentNumberAlgoId" ;
	public static String ContractorDocumentNumberAlgoId = "contractorDocumentNumberAlgoId" ;
	public static String VendorDocumentNumberAlgoId = "vendorDocumentNumberAlgoId" ;
	public static String RevisionAlgoId = "revisionAlgoId" ;
	public static String SubmissionOrDecisionCodeAlgoId = "submissionOrDecisionCodeAlgoId" ;
	public static String DtnDateTimeInDTNNoteAlgoId = "dtnDateTimeInDTNNoteAlgoId" ;
	public static String ProjectCodeAlgoId = "projectCodeAlgoId" ;
	public static String SubmissionCodeAlgoId = "submissionCodeAlgoId" ;
	public static String ExpectedTypeOfTransaction = "expectedTypeOfTransaction" ;
	public static String BaPrefix = "baPrefix" ;
	public static String PrimaryKeyForSearch = "primaryKeyForSearch"; 
	public static String PrimaryRecordSearchFieldId = "primaryRecordSearchFieldId" ;
	public static String OwnerDocumentNumberFieldID = "ownerDocumentNumberFieldID" ;
	public static String ContractorDocumentNumberFieldID = "contractorDocumentNumberFieldID" ;
	public static String VendorDocumentNumberFieldID = "vendorDocumentNumberFieldID" ;
	public static String RevisionFieldID = "revisionFieldID" ;
	public static String ReceivedFileUpdateFieldID = "receivedFileUpdateFieldID" ;
	public static String DecisionFieldID = "decisionFieldID" ;
	public static String DtnDateTimeAsPerTBits = "dtnDateTimeAsPerTBits" ;
	public static String DtnDateTimeToBeUsedInTransaction = "dtnDateTimeToBeUsedInTransaction" ;
	public static String DtnProcessId = "dtnProcessId" ;
	public static String DtnKeywordSetId = "dtnKeywordSetId";

	/**
	 * will search for email id pattern in the firm
	 * @param emailId
	 * @return
	 */
	
	private static final String Search_By_EmailId_Pattern = new StringBuffer().append("select * from ").append(TableName)
							.append(" where ").append(FromAgencyEmailAddress).append( " = ?").toString();
	
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

	private FirmProperty createFromResultSet(ResultSet rs) throws SQLException {
		Long id = DDCHelper.getNotNULL(rs, rs.getLong(Id));
		String fromAgencyEmailAddress = DDCHelper.getNotNULL(rs, rs.getString(FromAgencyEmailAddress));
		Boolean dtnExpected = DDCHelper.getNotNULL(rs, rs.getBoolean(DtnExpected));
		String expectedDTNFileName = DDCHelper.getNotNULL(rs, rs.getString(ExpectedDTNFileName));
		String dtnLetterIdentificationString = DDCHelper.getNotNULL(rs, rs.getString(DtnLetterIdentificationString));
		Long fromFieldAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(FromFieldAlgoId));
		Long toFieldAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(ToFieldAlgoId));
		Long dtnNumberAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(DtnNumberAlgoId));
		Long ownerDocumentNumberAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(OwnerDocumentNumberAlgoId));
		Long contractorDocumentNumberAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(ContractorDocumentNumberAlgoId));
		Long vendorDocumentNumberAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(VendorDocumentNumberAlgoId));
		Long revisionAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(RevisionAlgoId));
		Long submissionOrDecisionCodeAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(SubmissionOrDecisionCodeAlgoId));
		Long dtnDateTimeInDTNNoteAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(DtnDateTimeInDTNNoteAlgoId));
		Long projectCodeAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(ProjectCodeAlgoId));
		Long submissionCodeAlgoId = DDCHelper.getNotNULL(rs, rs.getLong(SubmissionCodeAlgoId));
		String expectedTypeOfTransaction = DDCHelper.getNotNULL(rs, rs.getString(ExpectedTypeOfTransaction));
		String baPrefix = DDCHelper.getNotNULL(rs, rs.getString(BaPrefix));
		String primaryKeyForSearch = DDCHelper.getNotNULL(rs, rs.getString(PrimaryKeyForSearch));
		Integer primaryRecordSearchFieldId = DDCHelper.getNotNULL(rs, rs.getInt(PrimaryRecordSearchFieldId));
		Integer ownerDocumentNumberFieldID = DDCHelper.getNotNULL(rs, rs.getInt(OwnerDocumentNumberFieldID));
		Integer contractorDocumentNumberFieldID = DDCHelper.getNotNULL(rs, rs.getInt(ContractorDocumentNumberFieldID));
		Integer vendorDocumentNumberFieldID = DDCHelper.getNotNULL(rs, rs.getInt(VendorDocumentNumberFieldID));
		Integer revisionFieldID = DDCHelper.getNotNULL(rs, rs.getInt(RevisionFieldID));
		Integer receivedFileUpdateFieldID = DDCHelper.getNotNULL(rs, rs.getInt(ReceivedFileUpdateFieldID));
		Integer decisionFieldID = DDCHelper.getNotNULL(rs, rs.getInt(DecisionFieldID));
		String dtnDateTimeAsPerTBits = DDCHelper.getNotNULL(rs, rs.getString(DtnDateTimeAsPerTBits));
		String dtnDateTimeToBeUsedInTransaction = DDCHelper.getNotNULL(rs, rs.getString(DtnDateTimeToBeUsedInTransaction));
		Long dtnProcessId = DDCHelper.getNotNULL(rs, rs.getLong(DtnProcessId));
		Long dtnKeywordSetId = DDCHelper.getNotNULL(rs, rs.getLong(DtnKeywordSetId));
		return new FirmPropertyBuilder()
		.setId(id)
		.setFromAgencyEmailAddress(fromAgencyEmailAddress)
		.setDtnExpected(dtnExpected)
		.setExpectedDTNFileName(expectedDTNFileName)
		.setDtnLetterIdentificationString(dtnLetterIdentificationString)
		.setFromFieldAlgoId(fromFieldAlgoId)
		.setToFieldAlgoId(toFieldAlgoId)
		.setDtnNumberAlgoId(dtnNumberAlgoId)
		.setOwnerDocumentNumberAlgoId(ownerDocumentNumberAlgoId)
		.setContractorDocumentNumberAlgoId(contractorDocumentNumberAlgoId)
		.setVendorDocumentNumberAlgoId(vendorDocumentNumberAlgoId)
		.setRevisionAlgoId(revisionAlgoId)
		.setSubmissionOrDecisionCodeAlgoId(submissionOrDecisionCodeAlgoId)
		.setDtnDateTimeInDTNNoteAlgoId(dtnDateTimeInDTNNoteAlgoId)
		.setProjectCodeAlgoId(projectCodeAlgoId)
		.setSubmissionCodeAlgoId(submissionCodeAlgoId)
		.setExpectedTypeOfTransaction(expectedTypeOfTransaction)
		.setBaPrefix(baPrefix)
		.setPrimaryKeyForSearch(primaryKeyForSearch)
		.setPrimaryRecordSearchFieldId(primaryRecordSearchFieldId)
		.setOwnerDocumentNumberFieldID(ownerDocumentNumberFieldID)
		.setContractorDocumentNumberFieldID(contractorDocumentNumberFieldID)
		.setVendorDocumentNumberFieldID(vendorDocumentNumberFieldID)
		.setRevisionFieldID(revisionFieldID)
		.setReceivedFileUpdateFieldID(receivedFileUpdateFieldID)
		.setDecisionFieldID(decisionFieldID)
		.setDtnDateTimeAsPerTBits(dtnDateTimeAsPerTBits)
		.setDtnDateTimeToBeUsedInTransaction(dtnDateTimeToBeUsedInTransaction)
		.setDtnProcessId(dtnProcessId)
		.setDtnKeywordSetId(dtnKeywordSetId)
		.build();
		}
	
	private static final String DropTableSQL = "drop table " + TableName ;
	private static final String CreationSQL = " create table " + TableName + " ( " +
			" id BIGINT identity, " +
			" fromAgencyEmailAddress varchar(256), " +
			" dtnExpected BIT, " +
			" expectedDTNFileName varchar(256), " +
			" dtnLetterIdentificationString varchar(256), " +
			" fromFieldAlgoId BIGINT, " +
			" toFieldAlgoId BIGINT, " +
			" dtnNumberAlgoId BIGINT, " +
			" ownerDocumentNumberAlgoId BIGINT, " +
			" contractorDocumentNumberAlgoId BIGINT, " +
			" vendorDocumentNumberAlgoId BIGINT, " +
			" revisionAlgoId BIGINT, " +
			" submissionOrDecisionCodeAlgoId BIGINT, " +
			" dtnDateTimeInDTNNoteAlgoId BIGINT, " +
			" projectCodeAlgoId BIGINT, " +
			" submissionCodeAlgoId BIGINT, " +
			" expectedTypeOfTransaction varchar(256), " +
			" baPrefix varchar(256), " +
			" primaryKeyForSearch varchar(256), " +
			" primaryRecordSearchFieldId INT, " +
			" ownerDocumentNumberFieldID INT, " +
			" contractorDocumentNumberFieldID INT, " +
			" vendorDocumentNumberFieldID INT, " +
			" revisionFieldID INT, " +
			" receivedFileUpdateFieldID INT, " +
			" decisionFieldID INT, " +
			" dtnDateTimeAsPerTBits varchar(256), " +
			" dtnDateTimeToBeUsedInTransaction varchar(256), " +
			" dtnProcessId BIGINT, " +
			" dtnKeywordSetId BIGINT " +
			" ) ";

	private static final String InsertSQL = "insert into " + TableName + " ( " 
			+ FromAgencyEmailAddress +","
			+ DtnExpected +","
			+ ExpectedDTNFileName +","
			+ DtnLetterIdentificationString +","
			+ FromFieldAlgoId +","
			+ ToFieldAlgoId +","
			+ DtnNumberAlgoId +","
			+ OwnerDocumentNumberAlgoId +","
			+ ContractorDocumentNumberAlgoId +","
			+ VendorDocumentNumberAlgoId +","
			+ RevisionAlgoId +","
			+ SubmissionOrDecisionCodeAlgoId +","
			+ DtnDateTimeInDTNNoteAlgoId +","
			+ ProjectCodeAlgoId +","
			+ SubmissionCodeAlgoId +","
			+ ExpectedTypeOfTransaction +","
			+ BaPrefix +","
			+ PrimaryKeyForSearch +","
			+ PrimaryRecordSearchFieldId +","
			+ OwnerDocumentNumberFieldID +","
			+ ContractorDocumentNumberFieldID +","
			+ VendorDocumentNumberFieldID +","
			+ RevisionFieldID +","
			+ ReceivedFileUpdateFieldID +","
			+ DecisionFieldID +","
			+ DtnDateTimeAsPerTBits +","
			+ DtnDateTimeToBeUsedInTransaction +","
			+ DtnProcessId +","
			+ DtnKeywordSetId 
			 + " ) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    private static final String UpdateSQL = "update " + TableName + " set " 
		+ FromAgencyEmailAddress + "=?,"
		+ DtnExpected + "=?,"
		+ ExpectedDTNFileName + "=?,"
		+ DtnLetterIdentificationString + "=?,"
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
		+ BaPrefix + "=?,"
		+ PrimaryKeyForSearch + "=?,"
		+ PrimaryRecordSearchFieldId + "=?,"
		+ OwnerDocumentNumberFieldID + "=?,"
		+ ContractorDocumentNumberFieldID + "=?,"
		+ VendorDocumentNumberFieldID + "=?,"
		+ RevisionFieldID + "=?,"
		+ ReceivedFileUpdateFieldID + "=?,"
		+ DecisionFieldID + "=?,"
		+ DtnDateTimeAsPerTBits + "=?,"
		+ DtnDateTimeToBeUsedInTransaction + "=?,"
		+ DtnProcessId + "=?,"
		+ DtnKeywordSetId + "=? "
		 + " where " + Id + "=?";


	private static final String DeleteSQL = "delete from " + TableName + " where " + Id + "=?";
	private static final String GetAllSQL = "select * from " + TableName;
	private static final String GetByIdSQL = "select * from " + TableName + " where " + Id+  "=?";
	private static final String GetAllByEmailIdSQL = "select * from " + TableName + " where " + FromAgencyEmailAddress + "=?";
	

	public void createTable() throws SQLException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(CreationSQL);
			boolean out = ps.execute();
			logger.info("created ...");
		} catch (SQLException e) {
			logger.error("Error occured while creating  table", e);
			throw e;
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

	public void dropTable() throws SQLException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DropTableSQL);
			ps.execute();
			logger.info("dropped....");
		} catch (SQLException e) {
			logger.error("Error occured while dropping  table", e);
			throw e;
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

	
	public void delete(FirmProperty fp) throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DeleteSQL);
			ps.setLong(1, fp.getId());
			
			int count = ps.executeUpdate();
			logger.info(count + " rows affected.");
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
	DDCHelper.setNotNull(ps, 1 ,fp.getFromAgencyEmailAddress());
	DDCHelper.setNotNull(ps, 2 ,fp.isDtnExpected());
	DDCHelper.setNotNull(ps, 3 ,fp.getExpectedDTNFileName());
	DDCHelper.setNotNull(ps, 4 ,fp.getDtnLetterIdentificationString());
	DDCHelper.setNotNull(ps, 5 ,fp.getFromFieldAlgoId());
	DDCHelper.setNotNull(ps, 6 ,fp.getToFieldAlgoId());
	DDCHelper.setNotNull(ps, 7 ,fp.getDtnNumberAlgoId());
	DDCHelper.setNotNull(ps, 8 ,fp.getOwnerDocumentNumberAlgoId());
	DDCHelper.setNotNull(ps, 9 ,fp.getContractorDocumentNumberAlgoId());
	DDCHelper.setNotNull(ps, 10 ,fp.getVendorDocumentNumberAlgoId());
	DDCHelper.setNotNull(ps, 11 ,fp.getRevisionAlgoId());
	DDCHelper.setNotNull(ps, 12 ,fp.getSubmissionOrDecisionCodeAlgoId());
	DDCHelper.setNotNull(ps, 13 ,fp.getDtnDateTimeInDTNNoteAlgoId());
	DDCHelper.setNotNull(ps, 14 ,fp.getProjectCodeAlgoId());
	DDCHelper.setNotNull(ps, 15 ,fp.getSubmissionCodeAlgoId());
	DDCHelper.setNotNull(ps, 16 ,fp.getExpectedTypeOfTransaction());
	DDCHelper.setNotNull(ps, 17 ,fp.getBaPrefix());
	DDCHelper.setNotNull(ps, 18 ,fp.getPrimaryKeyForSearch());
	DDCHelper.setNotNull(ps, 19 ,fp.getPrimaryRecordSearchFieldId());
	DDCHelper.setNotNull(ps, 20 ,fp.getOwnerDocumentNumberFieldID());
	DDCHelper.setNotNull(ps, 21 ,fp.getContractorDocumentNumberFieldID());
	DDCHelper.setNotNull(ps, 22 ,fp.getVendorDocumentNumberFieldID());
	DDCHelper.setNotNull(ps, 23 ,fp.getRevisionFieldID());
	DDCHelper.setNotNull(ps, 24 ,fp.getReceivedFileUpdateFieldID());
	DDCHelper.setNotNull(ps, 25 ,fp.getDecisionFieldID());
	DDCHelper.setNotNull(ps, 26 ,fp.getDtnDateTimeAsPerTBits());
	DDCHelper.setNotNull(ps, 27 ,fp.getDtnDateTimeToBeUsedInTransaction());
	DDCHelper.setNotNull(ps, 28 ,fp.getDtnProcessId());
	DDCHelper.setNotNull(ps, 29 ,fp.getDtnKeywordSetId());
	            int count = ps.executeUpdate();
	            if( count != 0 )
	            {
	                ResultSet rs = ps.getGeneratedKeys();
	                if( rs.next() )
	                {
	                    long id = rs.getLong(1);
	                    logger.info("Newly generated id : " + id );
	                    fp.setId(id);
	                }
	            }
	            logger.info(count +  " rows affected.");

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
					DDCHelper.setNotNull(ps, 1 ,fp.getFromAgencyEmailAddress());
					DDCHelper.setNotNull(ps, 2 ,fp.isDtnExpected());
					DDCHelper.setNotNull(ps, 3 ,fp.getExpectedDTNFileName());
					DDCHelper.setNotNull(ps, 4 ,fp.getDtnLetterIdentificationString());
					DDCHelper.setNotNull(ps, 5 ,fp.getFromFieldAlgoId());
					DDCHelper.setNotNull(ps, 6 ,fp.getToFieldAlgoId());
					DDCHelper.setNotNull(ps, 7 ,fp.getDtnNumberAlgoId());
					DDCHelper.setNotNull(ps, 8 ,fp.getOwnerDocumentNumberAlgoId());
					DDCHelper.setNotNull(ps, 9 ,fp.getContractorDocumentNumberAlgoId());
					DDCHelper.setNotNull(ps, 10 ,fp.getVendorDocumentNumberAlgoId());
					DDCHelper.setNotNull(ps, 11 ,fp.getRevisionAlgoId());
					DDCHelper.setNotNull(ps, 12 ,fp.getSubmissionOrDecisionCodeAlgoId());
					DDCHelper.setNotNull(ps, 13 ,fp.getDtnDateTimeInDTNNoteAlgoId());
					DDCHelper.setNotNull(ps, 14 ,fp.getProjectCodeAlgoId());
					DDCHelper.setNotNull(ps, 15 ,fp.getSubmissionCodeAlgoId());
					DDCHelper.setNotNull(ps, 16 ,fp.getExpectedTypeOfTransaction());
					DDCHelper.setNotNull(ps, 17 ,fp.getBaPrefix());
					DDCHelper.setNotNull(ps, 18 ,fp.getPrimaryKeyForSearch());
					DDCHelper.setNotNull(ps, 19 ,fp.getPrimaryRecordSearchFieldId());
					DDCHelper.setNotNull(ps, 20 ,fp.getOwnerDocumentNumberFieldID());
					DDCHelper.setNotNull(ps, 21 ,fp.getContractorDocumentNumberFieldID());
					DDCHelper.setNotNull(ps, 22 ,fp.getVendorDocumentNumberFieldID());
					DDCHelper.setNotNull(ps, 23 ,fp.getRevisionFieldID());
					DDCHelper.setNotNull(ps, 24 ,fp.getReceivedFileUpdateFieldID());
					DDCHelper.setNotNull(ps, 25 ,fp.getDecisionFieldID());
					DDCHelper.setNotNull(ps, 26 ,fp.getDtnDateTimeAsPerTBits());
					DDCHelper.setNotNull(ps, 27 ,fp.getDtnDateTimeToBeUsedInTransaction());
					DDCHelper.setNotNull(ps, 28 ,fp.getDtnProcessId());
					DDCHelper.setNotNull(ps, 29 ,fp.getDtnKeywordSetId());
                    ps.setLong(30, fp.getId());

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
			logger.error("",e);
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

	public List<FirmProperty> getAllByEmailId(String emailId) throws FailedToFindObject
	{
		Connection con = null;
		List<FirmProperty> fps = new ArrayList<FirmProperty>();
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetAllByEmailIdSQL);
			ps.setString(1, emailId);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs )
			{
				while(rs.next())
					fps.add(createFromResultSet(rs));
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with Id = " + emailId);
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

		return fps;
	}

	
	public static void main(String[] args) {
//		FirmProperty fp = new FirmProperty();
//		fp.setBaPrefix("ba1");
//		fp.setFromAgencyEmailAddress("root@localhost");
//		fp.setContractorDocumentNumberAlgoId(5L);
//		fp.setFromFieldAlgoId(6L);
//		fp.setOwnerDocumentNumberAlgoId(7L);
//		
//		fp.setDtnExpected(true);
//		
//		try {
////			FirmPropertyDao.getInstance().dropTable();
////			FirmPropertyDao.getInstance().createTable();
////			for(int i = 0 ; i < 10 ; i++ )
//			FirmProperty inserted = FirmPropertyDao.getInstance().insert(fp);
//			logger.info("inserted firm property : " + inserted);
////			List<FirmProperty> list = FirmPropertyDao.getInstance().getAll();
////			logger.info("number of FPs : " + list.size());
////			logger.info("All FPs : " + list);
//			
////			FirmProperty fp1 = FirmPropertyDao.getInstance().getById(1L);
////			logger.info("FP 1 : " + fp1);
//		} catch (FailedToInsert e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
////		} catch (SQLException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		} catch (FailedToFindObject e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
//		}
		
//		try {
//			FirmProperty fp = FirmPropertyDao.getInstance().getById(11L);
//			logger.info("fp = " + fp);
////			fp.setExpectedDTNFileName(".*\\.pdf");
////			fp.setFromFieldAlgoId(2L);
////			fp.setOwnerDocumentNumberAlgoId(1L);
////			fp.setContractorDocumentNumberAlgoId(3L);
////			fp.setPrimaryRecordSearchFieldId(32);
////			fp.setBaPrefix("tbits_supports");
//			fp.setDtnNumberAlgoId(1L);
//			FirmProperty updated = FirmPropertyDao.getInstance().update(fp);
//			logger.info("fp = " + updated);
//		} catch (FailedToFindObject e) {
//			e.printStackTrace();
//		} catch (FailedToUpdate e) {
//			e.printStackTrace();
//		}
		
//		System.out.println(SearchAlgoDao.);
		
	}
}
