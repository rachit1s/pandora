IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_report_params_map'))
BEGIN
/*
   Monday, November 29, 20101:06:44 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_report_params_map
	(
	id int NOT NULL IDENTITY (1, 1),
	report_id int NOT NULL,
	param_type varchar(32) NOT NULL,
	param_name varchar(511) NOT NULL,
	param_value_type varchar(32) NOT NULL,
	param_value varchar(1024) NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_report_params_map OFF


IF EXISTS(SELECT * FROM dbo.corr_report_params_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_report_params_map (report_id, param_type, param_name, param_value_type, param_value)
		SELECT CONVERT(int, report_id), param_type, param_name, param_value_type, param_value FROM dbo.corr_report_params_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_report_params_map


EXECUTE sp_rename N'dbo.Tmp_corr_report_params_map', N'corr_report_params_map', 'OBJECT' 


COMMIT
END