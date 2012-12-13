IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_field_name_map'))
BEGIN
/*
   Monday, November 29, 201012:29:24 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_field_name_map
	(
	id int NOT NULL IDENTITY (1, 1),
	corr_field_name varchar(100) NOT NULL,
	sys_prefix varchar(32) NOT NULL,
	field_name varchar(100) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_field_name_map OFF


IF EXISTS(SELECT * FROM dbo.corr_field_name_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_field_name_map (corr_field_name, sys_prefix, field_name)
		SELECT CONVERT(varchar(100), corr_field_name), sys_prefix, CONVERT(varchar(100), field_name) FROM dbo.corr_field_name_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_field_name_map


EXECUTE sp_rename N'dbo.Tmp_corr_field_name_map', N'corr_field_name_map', 'OBJECT' 


COMMIT

END