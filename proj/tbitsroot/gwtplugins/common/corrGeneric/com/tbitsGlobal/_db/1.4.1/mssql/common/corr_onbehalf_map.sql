IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_onbehalf_map') and system_type_id = 56)
BEGIN
/*
   Monday, November 29, 20103:28:50 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/
BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_onbehalf_map
	(
	id bigint NOT NULL IDENTITY (1, 1),
	sys_prefix varchar(32) NOT NULL,
	user_login varchar(100) NOT NULL,
	onbehalf_type1 varchar(100) NULL,
	onbehalf_type2 varchar(100) NULL,
	onbehalf_type3 varchar(100) NULL,
	onbehalf_of_login varchar(100) NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_onbehalf_map ON


IF EXISTS(SELECT * FROM dbo.corr_onbehalf_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_onbehalf_map (id, sys_prefix, user_login, onbehalf_type1, onbehalf_type2, onbehalf_type3, onbehalf_of_login)
		SELECT CONVERT(bigint, id), sys_prefix, user_login, onbehalf_type1, onbehalf_type2, onbehalf_type3, onbehalf_of_login FROM dbo.corr_onbehalf_map WITH (HOLDLOCK TABLOCKX)')


SET IDENTITY_INSERT dbo.Tmp_corr_onbehalf_map OFF


DROP TABLE dbo.corr_onbehalf_map


EXECUTE sp_rename N'dbo.Tmp_corr_onbehalf_map', N'corr_onbehalf_map', 'OBJECT' 


ALTER TABLE dbo.corr_onbehalf_map ADD CONSTRAINT
	uq_corr_onbehalf_entry UNIQUE NONCLUSTERED 
	(
	sys_prefix,
	user_login,
	onbehalf_type1,
	onbehalf_type2,
	onbehalf_type3,
	onbehalf_of_login
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]



COMMIT
END
