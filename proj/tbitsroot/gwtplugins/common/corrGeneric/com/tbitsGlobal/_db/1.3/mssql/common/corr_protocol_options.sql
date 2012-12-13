IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_protocol_options'))
BEGIN
/*
   Monday, November 29, 201012:57:53 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_protocol_options
	(
	id int NOT NULL IDENTITY (1, 1),
	sys_prefix varchar(32) NOT NULL,
	option_name varchar(400) NOT NULL,
	option_value varchar(4000) NULL,
	option_description varchar(4000) NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_protocol_options OFF


IF EXISTS(SELECT * FROM dbo.corr_protocol_options)
	 EXEC('INSERT INTO dbo.Tmp_corr_protocol_options (sys_prefix, option_name, option_value, option_description)
		SELECT sys_prefix, option_name, option_value, option_description FROM dbo.corr_protocol_options WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_protocol_options


EXECUTE sp_rename N'dbo.Tmp_corr_protocol_options', N'corr_protocol_options', 'OBJECT' 


COMMIT
END
