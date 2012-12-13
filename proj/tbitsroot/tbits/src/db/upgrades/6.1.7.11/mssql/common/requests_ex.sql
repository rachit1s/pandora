/* To prevent any potential data loss issues, you should review this script in detail before running it outside the context of the database designer.*/
BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT
BEGIN TRANSACTION
GO
CREATE TABLE dbo.Tmp_requests_ex
	(
	sys_id int NOT NULL,
	request_id int NOT NULL,
	field_id int NOT NULL,
	bit_value bit NULL,
	datetime_value datetime NULL,
	int_value int NULL,
	real_value real NULL,
	varchar_value nvarchar(3500) NULL,
	text_value ntext NULL,
	text_content_type int NULL,
	type_value int NULL
	)  ON [PRIMARY]
	 TEXTIMAGE_ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.requests_ex)
	 EXEC('INSERT INTO dbo.Tmp_requests_ex (sys_id, request_id, field_id, bit_value, datetime_value, int_value, real_value, varchar_value, text_value, type_value)
		SELECT sys_id, request_id, field_id, bit_value, datetime_value, int_value, real_value, varchar_value, text_value, type_value FROM dbo.requests_ex WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.requests_ex
GO
EXECUTE sp_rename N'dbo.Tmp_requests_ex', N'requests_ex', 'OBJECT' 
GO
ALTER TABLE dbo.requests_ex ADD CONSTRAINT
	PK_requests_ex PRIMARY KEY CLUSTERED 
	(
	sys_id,
	request_id,
	field_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
CREATE NONCLUSTERED INDEX idx_re_type ON dbo.requests_ex
	(
	type_value
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
COMMIT
