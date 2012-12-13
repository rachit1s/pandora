SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_actionex_lookupById]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- ================================================
-- Template generated from Template Explorer using:
-- Create Procedure (New Menu).SQL
--
-- Use the Specify Values for Template Parameters 
-- command (Ctrl-Shift-M) to fill in the parameter 
-- values below.
--
-- This block of comments will not be included in
-- the definition of the procedure.
-- ================================================

CREATE PROCEDURE [dbo].[stp_actionex_lookupById] 
( 
        @systemId 	INT, 
        @requestId 	INT, 
        @actionId 	INT,
	@fieldId	INT
) 
as 

SELECT 
    sys_id, 
    request_id, 
    action_id,
    field_id,
    bit_value,
    datetime_value,
    int_value,
    real_value,
    varchar_value,
    text_value,
    type_value    
FROM 
        actions_ex act_ex 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId AND 
        action_id       = @actionId AND
	field_id	= @fieldId 



' 
END
