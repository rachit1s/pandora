SET QUOTED_IDENTIFIER ON
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON


IF EXISTS(
SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME LIKE 'update_sys_prefix'
AND TABLE_NAME LIKE 'trn_change_note_configuration')

BEGIN
PRINT 'executed'   --- this is test line  -- remove it before comitting
RETURN             --- this is the way how you come out of execution if column already exits

END
ELSE
BEGIN

ALTER TABLE dbo.trn_change_note_configuration 
ADD update_sys_prefix nvarchar (2048)


IF EXISTS(   -- here we are testing if the column is added successfully with required constraint
SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME LIKE 'update_sys_prefix'
AND TABLE_NAME LIKE 'trn_change_note_configuration')
BEGIN

PRINT 'update_sys_prefix column added successfully'  --- printing message
END

END