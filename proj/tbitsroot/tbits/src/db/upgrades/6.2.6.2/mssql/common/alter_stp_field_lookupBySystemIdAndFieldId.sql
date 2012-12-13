
/****** Object:  StoredProcedure [dbo].[stp_field_lookupBySystemIdAndFieldId]    Script Date: 04/19/2012 10:42:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
ALTER PROCEDURE [dbo].[stp_field_lookupBySystemIdAndFieldId] 
( 
        @systemId INT, 
        @fieldId  INT 
) 
AS 
SELECT 
        sys_id, 
        field_id, 
        name, 
        display_name, 
        description, 
        data_type_id, 
        is_active, 
        is_extended, 
        is_private, 
        tracking_option, 
        permission, 
        regex, 
        is_dependent,
        display_order,
	display_group,
	error
FROM 
        fields 
WHERE 
        sys_id = @systemId AND 
        field_id = @fieldId