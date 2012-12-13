IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_report_map'))
BEGIN
/*
   Monday, November 29, 201012:59:20 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_report_map
	(
	id int NOT NULL IDENTITY (1, 1),
	sys_prefix varchar(32) NOT NULL,
	report_type1 varchar(100) NULL,
	report_type2 varchar(100) NULL,
	report_type3 varchar(100) NULL,
	report_type4 varchar(100) NULL,
	report_type5 varchar(100) NULL,
	report_id int NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_report_map OFF


IF EXISTS(SELECT * FROM dbo.corr_report_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_report_map (sys_prefix, report_type1, report_type2, report_type3, report_type4, report_type5, report_id)
		SELECT sys_prefix, CONVERT(varchar(100), report_type1), CONVERT(varchar(100), report_type2), CONVERT(varchar(100), report_type3), CONVERT(varchar(100), report_type4), CONVERT(varchar(100), report_type5), report_id FROM dbo.corr_report_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_report_map


EXECUTE sp_rename N'dbo.Tmp_corr_report_map', N'corr_report_map', 'OBJECT' 


COMMIT
END