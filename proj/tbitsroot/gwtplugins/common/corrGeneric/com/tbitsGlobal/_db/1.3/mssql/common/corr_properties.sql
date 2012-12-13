IF NOT EXISTS ( SELECT * from sys.columns where Name = N'id' and Object_ID = Object_ID(N'corr_properties'))
BEGIN

/*
   Monday, November 29, 201012:53:42 PM
   User: 
   Server: NUTZWIN\SQLEXPRESS
   Database: tbits
   Application: 
*/
BEGIN TRANSACTION


CREATE TABLE dbo.Tmp_corr_properties
	(
	id int NOT NULL IDENTITY (1, 1),
	property_name varchar(400) NOT NULL,
	property_value varchar(4000) NULL,
	property_description varchar(4000) NULL
	)  ON [PRIMARY]




IF EXISTS(SELECT * FROM dbo.corr_properties)
	 EXEC('INSERT INTO dbo.Tmp_corr_properties (property_name, property_value, property_description)
		SELECT  property_name, property_value, property_description FROM dbo.corr_properties WITH (HOLDLOCK TABLOCKX)')




DROP TABLE dbo.corr_properties


EXECUTE sp_rename N'dbo.Tmp_corr_properties', N'corr_properties', 'OBJECT' 


COMMIT

END
