IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_protocol_options') and system_type_id = 56)
BEGIN
/*
   Monday, November 29, 20103:29:57 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_protocol_options
	(
	id bigint NOT NULL IDENTITY (1, 1),
	sys_prefix varchar(32) NOT NULL,
	option_name varchar(400) NOT NULL,
	option_value varchar(4000) NULL,
	option_description varchar(4000) NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_protocol_options ON


IF EXISTS(SELECT * FROM dbo.corr_protocol_options)
	 EXEC('INSERT INTO dbo.Tmp_corr_protocol_options (id, sys_prefix, option_name, option_value, option_description)
		SELECT CONVERT(bigint, id), sys_prefix, option_name, option_value, option_description FROM dbo.corr_protocol_options WITH (HOLDLOCK TABLOCKX)')


SET IDENTITY_INSERT dbo.Tmp_corr_protocol_options OFF


DROP TABLE dbo.corr_protocol_options


EXECUTE sp_rename N'dbo.Tmp_corr_protocol_options', N'corr_protocol_options', 'OBJECT' 


COMMIT
END
