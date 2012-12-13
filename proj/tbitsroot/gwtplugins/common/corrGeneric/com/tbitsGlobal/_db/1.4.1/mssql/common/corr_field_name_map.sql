IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_field_name_map') and system_type_id = 56)
BEGIN
/*
   Monday, November 29, 20103:28:22 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_field_name_map
	(
	id bigint NOT NULL IDENTITY (1, 1),
	corr_field_name varchar(100) NOT NULL,
	sys_prefix varchar(32) NOT NULL,
	field_name varchar(100) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_field_name_map ON


IF EXISTS(SELECT * FROM dbo.corr_field_name_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_field_name_map (id, corr_field_name, sys_prefix, field_name)
		SELECT CONVERT(bigint, id), corr_field_name, sys_prefix, field_name FROM dbo.corr_field_name_map WITH (HOLDLOCK TABLOCKX)')


SET IDENTITY_INSERT dbo.Tmp_corr_field_name_map OFF


DROP TABLE dbo.corr_field_name_map


EXECUTE sp_rename N'dbo.Tmp_corr_field_name_map', N'corr_field_name_map', 'OBJECT' 


ALTER TABLE dbo.corr_field_name_map ADD CONSTRAINT
	uq_corr_field_name_entry UNIQUE NONCLUSTERED 
	(
	corr_field_name,
	sys_prefix,
	field_name
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]



COMMIT
END
