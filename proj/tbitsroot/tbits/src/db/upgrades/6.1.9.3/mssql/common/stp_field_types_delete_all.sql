set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

-- it will delete all types values of the fields 

CREATE PROCEDURE [dbo].[stp_field_types_delete_all] 
(
	@sys_id INT,
	@field_id INT
	
)
AS

DECLARE @fieldName varchar(255)
DECLARE @Delete INT

SELECT @fieldName = name FROM fields WHERE field_id = @field_id and sys_id = @sys_id
SET @Delete = 0

IF(@field_id < 29) 

    BEGIN
        IF NOT EXISTS(SELECT * FROM actions a WHERE a.sys_id = @sys_id )
        BEGIN
           SET @Delete = 1
        END
    END
   

IF(@field_id > 29) 
BEGIN
    IF NOT EXISTS(SELECT * FROM actions_ex  WHERE sys_id = @sys_id AND field_id = @field_id)
    BEGIN
       SET @Delete = 1
    END
END

IF(@Delete = 1)
 BEGIN
    DELETE types 
    WHERE  
        sys_id = @sys_id AND 
        field_id = @field_id 
        
   
    print 'types deleted successfully'
END
ELSE
BEGIN
    print 'there are some requests related this field so we can not delete all types'
END






