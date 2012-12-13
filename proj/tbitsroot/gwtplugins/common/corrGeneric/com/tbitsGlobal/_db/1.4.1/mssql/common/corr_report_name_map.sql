IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_report_name_map') and system_type_id = 56)
BEGIN
/*
   Monday, November 29, 20103:31:03 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_report_name_map
	(
	id bigint NOT NULL IDENTITY (1, 1),
	report_id int NOT NULL,
	report_file_name varchar(255) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_report_name_map ON


IF EXISTS(SELECT * FROM dbo.corr_report_name_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_report_name_map (id, report_id, report_file_name)
		SELECT CONVERT(bigint, id), report_id, report_file_name FROM dbo.corr_report_name_map WITH (HOLDLOCK TABLOCKX)')


SET IDENTITY_INSERT dbo.Tmp_corr_report_name_map OFF


DROP TABLE dbo.corr_report_name_map


EXECUTE sp_rename N'dbo.Tmp_corr_report_name_map', N'corr_report_name_map', 'OBJECT' 


COMMIT
END
