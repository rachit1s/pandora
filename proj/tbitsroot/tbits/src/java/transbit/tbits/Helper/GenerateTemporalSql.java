package transbit.tbits.Helper;

import java.io.FileWriter;
import java.sql.*;
import java.util.*;
import transbit.tbits.common.ConnectionProperties;
import transbit.tbits.common.DataSourcePool;
/**
 * code for generating the Temporal sql 
 * generate Temoral  sql does not support text,image kind of fields & table should have  primary key.
 * @author paritosh
 * 
 */
public class GenerateTemporalSql {

	/**
	 * @param args
	 * @throws SQLException
	 * 
	 */
	public static void main(String[] args) throws SQLException {

		Connection conn = null;

		if (args.length == 0) {
			System.err.println("tables name is not provided by user");
		}

		else {

			try {

				conn = DataSourcePool.getConnection();

				// list of table to create their temporal table.
				List tableNames = new ArrayList<String>();
				Statement stmt = null;
				stmt = conn.createStatement();
				// add all to create temporal table.

				for (String tableName : args)
					tableNames.add(tableName);

				// Create Temporal tables with same name add temporal as Prefix
				// with 2 additional columns strat_time & end_time

				int i;
				for (i = 0; i < tableNames.size(); i++) {

					// check table name is DB is exists

					boolean tableIsExist = true;
					tableIsExist = ValidateTableName((String) tableNames.get(i));

					if (tableIsExist == false) {
						System.out.println("--" + tableNames.get(i)
								+ ": table name is not valid");
					} else {
						System.out.println("--" + tableNames.get(i)
								+ " : table name is valid");
						String table = (String) tableNames.get(i);
						StringBuilder temporalTableName = new StringBuilder();
						temporalTableName.append("temporal_").append(table);

						String copyTable = "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='"
								+ temporalTableName
								+ "') BEGIN select * into "
								+ temporalTableName
								+ " from "
								+ table
								+ " where 1 = 1";

						String addColumn = "ALTER TABLE  "
								+ temporalTableName
								+ " add  audit_StartDateTime datetime, audit_EndDateTime datetime  END";

						String updateColumn = "update "
								+ temporalTableName
								+ " set audit_StartDateTime = getdate() ;\n   update "
								+ temporalTableName
								+ " set audit_EndDateTime = '9/9/9999' ";

						stmt.addBatch(copyTable);
						stmt.addBatch(addColumn);
						stmt.addBatch(updateColumn);
						// print Query
						System.out.println(copyTable);
						System.out.println(addColumn);
						System.out.println(updateColumn);

						System.out.println("--" + temporalTableName
								+ ": table is created \n-------------");
					}
				}

				// stmt.executeBatch();

				// create trigger for filling data from table into Temporal
				// tables.....

				// get the all columns name from table

				ResultSet rs = null;
				int k;
				for (k = 0; k < tableNames.size(); k++) {
					boolean tableIsExist = ValidateTableName((String) tableNames
							.get(k));
					if (tableIsExist == false) {
						System.out.println(tableNames.get(k) + ": for creating trigger table name is not valid");
					} else {
						StringBuilder columnList = new StringBuilder();
						String aTable = (String) tableNames.get(k);
						String triggerName = "audit_" + aTable;
						String temporalTable = "temporal_" + aTable;

						String query = null;
						query = "SELECT t.name AS tblName,tc.name "
								+ "FROM sys.tables AS t"
								+ " join sys.columns tc on t.object_id = tc.object_id"
								+ " where t.name = '" + aTable + "'";

						rs = stmt.executeQuery(query);
						while (rs.next()) {
							columnList.append(rs.getString(2)).append(",");
						}
						columnList.deleteCharAt(columnList.lastIndexOf(","));
						// System.out.println("column List from table:" + aTable
						// + " = "+ columnList.toString());

						// get the Primary key id columns from tables...

						StringBuilder pkId = new StringBuilder();

						String pkQuery = null;
						pkQuery = "SELECT t.name AS tblName,tc.name ColumnName "
								+ "FROM sys.tables AS t "
								+ "join sys.columns tc on t.object_id = tc.object_id "
								+ "join sys.index_columns ic on tc.object_id = ic.object_id and tc.column_id = ic.column_id "
								+ "join sys.indexes ti on ic.object_id = ti.object_id and ic.index_id = ti.index_id "
								+ "where ti.is_primary_key = 1 and t.name = '"
								+ aTable + "'";

						rs = stmt.executeQuery(pkQuery);
						;
						while (rs.next()) {
							pkId.append(temporalTable + ".").append(
									rs.getString(2)).append(" = deleted.")
									.append(rs.getString(2));
							if (rs.isLast() == false) {
								pkId.append(" AND ");
							}
						}

						// System.out.println("primary key: "+pkId);

						// query for create Trigger

						String trigQuery = null;
						trigQuery = "IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'"
							    + triggerName + "')\n BEGIN \n DECLARE @sqlcommand nvarchar(2000)"
							    +"\n SET @sqlcommand = '"
							    +" CREATE TRIGGER [dbo].["
								+ triggerName
								+ "] ON [dbo].["
								+ aTable
								+ "] "
								+ "\n FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION "
								+ "\n As" + " \nDECLARE "
								+ " \n @TrigTime DateTime "
								+ " \n set @TrigTime = getDate() "
								+ "\n UPDATE temporal_" + aTable
								+ " \n SET  audit_EndDateTime = (@TrigTime) "
								+ "\n FROM" + "\n deleted," + temporalTable
								+ " WHERE " + pkId + " \n AND"
								+ "\n audit_EndDateTime = ''9/9/9999''"
								+ "\n INSERT INTO " + temporalTable + "("
								+ columnList.toString()
								+ "\n ,audit_StartDateTime, audit_EndDateTime"
								+ ")" + " SELECT " + columnList.toString()
								+ ", @TrigTime , ''9/9/9999''" + "FROM "
								+ "INSERTED '"
								+" \n EXEC SP_EXECUTESQL  @sqlcommand \n END \n ELSE \n print'"+ triggerName +"  trigger already Exist'";

						// System.out.println(" trigger Query :"+trigQuery);

						String isExists = "SELECT 1 FROM dbo.sysobjects  WHERE Name = '"
								+ triggerName
								+ "' AND OBJECTPROPERTY(id, 'IsTrigger') = 1";
						// print Trigger query with exist condition
						// System.out.println("IF NOT EXISTS ("+isExists+")");
						System.out.println(trigQuery + "\n GO");

						// -----------------------------
						System.out.println();
						rs = stmt.executeQuery(isExists);
						if (rs.next() == true) {
							// System.err.println("--"+triggerName+
							// ":trigger already exists in database");
						} else {
							// stmt.execute(trigQuery);
							// System.out.println(triggerName+
							// ":trigger has been created now.");
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {

				conn.close();
			}
		}

	}
	public static  boolean ValidateTableName(String arg)
	{   
		
		Connection conn = null;
		Statement stmt = null; 
		boolean  isExist = true;
		try {
			conn = DataSourcePool.getConnection();
			stmt = conn.createStatement();
			String tableIsExist = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE'  AND TABLE_Name = '" +arg+"'";
            ResultSet rsTableExist = null;
            
            rsTableExist = stmt.executeQuery(tableIsExist);
            if(rsTableExist.next()== false)
               {
            	isExist = false;
               }
            else
            	isExist = true;
            
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isExist;
		
		
	}
	

}
