IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_ba_field_map'))
BEGIN

/*
   Monday, November 29, 201012:21:54 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_ba_field_map
	(
	id int NOT NULL IDENTITY (1, 1),
	from_sys_prefix varchar(32) NOT NULL,
	from_field_name varchar(100) NOT NULL,
	to_sys_prefix varchar(32) NOT NULL,
	to_field_name varchar(100) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_ba_field_map OFF


IF EXISTS(SELECT * FROM dbo.corr_ba_field_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_ba_field_map (from_sys_prefix, from_field_name, to_sys_prefix, to_field_name)
		SELECT from_sys_prefix, CONVERT(varchar(100), from_field_name), to_sys_prefix, CONVERT(varchar(100), to_field_name) FROM dbo.corr_ba_field_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_ba_field_map


EXECUTE sp_rename N'dbo.Tmp_corr_ba_field_map', N'corr_ba_field_map', 'OBJECT' 


COMMIT


END