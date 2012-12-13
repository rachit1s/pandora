IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_onbehalf_map'))
BEGIN

/*
   Monday, November 29, 201012:34:04 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_onbehalf_map
	(
	id int NOT NULL IDENTITY (1, 1),
	sys_prefix varchar(32) NOT NULL,
	user_login varchar(100) NOT NULL,
	onbehalf_type1 varchar(100) NULL,
	onbehalf_type2 varchar(100) NULL,
	onbehalf_type3 varchar(100) NULL,
	onbehalf_of_login varchar(100) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_onbehalf_map OFF


IF EXISTS(SELECT * FROM dbo.corr_onbehalf_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_onbehalf_map (sys_prefix, user_login, onbehalf_type1, onbehalf_type2, onbehalf_type3, onbehalf_of_login)
		SELECT sys_prefix, CONVERT(varchar(100), user_login), CONVERT(varchar(100), onbehalf_type1), CONVERT(varchar(100), onbehalf_type2), CONVERT(varchar(100), onbehalf_type3), CONVERT(varchar(100), onbehalf_of_login) FROM dbo.corr_onbehalf_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_onbehalf_map


EXECUTE sp_rename N'dbo.Tmp_corr_onbehalf_map', N'corr_onbehalf_map', 'OBJECT' 


COMMIT
END
