BEGIN TRANSACTION
CREATE TABLE dbo.drawing_number_generation_config
	(
	sys_id int NOT NULL,			-- The BA for which the Doc/Dwg number is to be generated
	field_id int NOT NULL,			-- Field that determines if the number has to be generated
	fields varchar(50) NOT NULL,		-- Comma separated field names
	formatter varchar(50) NOT NULL, 	-- Formatter 
	padding int NOT NULL			-- Padding for the running number
	)
GO
ALTER TABLE dbo.drawing_number_generation_config ADD CONSTRAINT
	PK_Table1 PRIMARY KEY CLUSTERED 
	(
	sys_id
	)
GO
COMMIT
