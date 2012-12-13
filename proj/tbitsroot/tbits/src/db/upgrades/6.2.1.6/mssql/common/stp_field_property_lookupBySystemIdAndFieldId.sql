/****** Object:  StoredProcedure [dbo].[stp_field_property_lookupBySystemIdAndFieldId]    Script Date: 09/16/2010 03:07:14 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO



CREATE PROCEDURE [dbo].[stp_field_property_lookupBySystemIdAndFieldId] 
( 
        @systemId INT, 
        @fieldId  INT 
) 
AS 
SELECT 
        sys_id, 
        field_id, 
        property, 
        value,
		description
FROM 
        field_properties
WHERE 
        sys_id = @systemId AND 
        field_id = @fieldId 

