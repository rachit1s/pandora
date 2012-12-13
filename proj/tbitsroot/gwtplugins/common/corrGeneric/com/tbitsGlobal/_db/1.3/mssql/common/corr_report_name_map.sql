IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_report_name_map'))
BEGIN
/*
   Monday, November 29, 20101:02:19 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_report_name_map
	(
	id int NOT NULL IDENTITY (1, 1),
	report_id int NOT NULL,
	report_file_name varchar(255) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_report_name_map OFF


IF EXISTS(SELECT * FROM dbo.corr_report_name_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_report_name_map (report_id, report_file_name)
		SELECT report_id, report_file_name FROM dbo.corr_report_name_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_report_name_map


EXECUTE sp_rename N'dbo.Tmp_corr_report_name_map', N'corr_report_name_map', 'OBJECT' 


COMMIT
END
