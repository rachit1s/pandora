IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_user_map'))
BEGIN
/*
   Monday, November 29, 20101:09:58 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/

BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_user_map
	(
	id int NOT NULL IDENTITY (1, 1),
	sys_prefix varchar(32) NOT NULL,
	user_login varchar(100) NOT NULL,
	user_map_type1 varchar(100) NULL,
	user_map_type2 varchar(100) NULL,
	user_map_type3 varchar(100) NULL,
	user_type_field_name varchar(100) NOT NULL,
	user_login_value varchar(100) NOT NULL,
	strictness int NOT NULL
	)  ON [PRIMARY]


SET IDENTITY_INSERT dbo.Tmp_corr_user_map OFF


IF EXISTS(SELECT * FROM dbo.corr_user_map)
	 EXEC('INSERT INTO dbo.Tmp_corr_user_map (sys_prefix, user_login, user_map_type1, user_map_type2, user_map_type3, user_type_field_name, user_login_value, strictness)
		SELECT sys_prefix, CONVERT(varchar(100), user_login), CONVERT(varchar(100), user_map_type1), CONVERT(varchar(100), user_map_type2), CONVERT(varchar(100), user_map_type3), CONVERT(varchar(100), user_type_field_name), CONVERT(varchar(100), user_login_value), strictness FROM dbo.corr_user_map WITH (HOLDLOCK TABLOCKX)')


DROP TABLE dbo.corr_user_map


EXECUTE sp_rename N'dbo.Tmp_corr_user_map', N'corr_user_map', 'OBJECT' 


COMMIT
END
