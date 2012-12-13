IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_ba_field_map') and system_type_id = 56)
BEGIN

/*
   Monday, November 29, 20103:25:50 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_ba_field_map
	(
	id bigint NOT NULL IDENTITY (1, 1),
	from_sys_prefix varchar(32) NOT NULL,
	from_field_name varchar(100) NOT NULL,
	to_sys_prefix varchar(32) NOT NULL,
	to_field_name varchar(100) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_ba_field_map ON


IF EXISTS(SELECT * FROM dbo.corr_ba_field_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_ba_field_map (id, from_sys_prefix, from_field_name, to_sys_prefix, to_field_name)
		SELECT CONVERT(bigint, id), from_sys_prefix, from_field_name, to_sys_prefix, to_field_name FROM dbo.corr_ba_field_map WITH (HOLDLOCK TABLOCKX)')


SET IDENTITY_INSERT dbo.Tmp_corr_ba_field_map OFF


DROP TABLE dbo.corr_ba_field_map


EXECUTE sp_rename N'dbo.Tmp_corr_ba_field_map', N'corr_ba_field_map', 'OBJECT' 


ALTER TABLE dbo.corr_ba_field_map ADD CONSTRAINT
	uq_row UNIQUE NONCLUSTERED 
	(
	from_sys_prefix,
	from_field_name,
	to_sys_prefix,
	to_field_name
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]



COMMIT
END
