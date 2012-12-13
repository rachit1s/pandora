SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_field_property_delete] 
(
	@sys_id			INT,
	@field_id		INT,
	@property		NVARCHAR(50),
	@value			NVARCHAR(MAX),
	@description	NVARCHAR(500),
	@returnValue	INT OUTPUT
)
AS

DECLARE @delete int

IF exists(select * from field_properties where sys_id = @sys_id and field_id = @field_id)
BEGIN
	DELETE FROM field_properties 
	WHERE  
		sys_id = @sys_id AND 
		field_id = @field_id
	
	SELECT @returnValue = 1
END
ELSE
BEGIN
	SELECT @returnValue = 0
END

