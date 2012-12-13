IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_report_params_map') and system_type_id = 56)
BEGIN
/*
   Monday, November 29, 20103:31:34 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_report_params_map
	(
	id bigint NOT NULL IDENTITY (1, 1),
	report_id int NOT NULL,
	param_type varchar(32) NOT NULL,
	param_name varchar(511) NOT NULL,
	param_value_type varchar(32) NOT NULL,
	param_value varchar(1024) NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_report_params_map ON


IF EXISTS(SELECT * FROM dbo.corr_report_params_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_report_params_map (id, report_id, param_type, param_name, param_value_type, param_value)
		SELECT CONVERT(bigint, id), report_id, param_type, param_name, param_value_type, param_value FROM dbo.corr_report_params_map WITH (HOLDLOCK TABLOCKX)')


SET IDENTITY_INSERT dbo.Tmp_corr_report_params_map OFF


DROP TABLE dbo.corr_report_params_map


EXECUTE sp_rename N'dbo.Tmp_corr_report_params_map', N'corr_report_params_map', 'OBJECT' 


COMMIT
END
