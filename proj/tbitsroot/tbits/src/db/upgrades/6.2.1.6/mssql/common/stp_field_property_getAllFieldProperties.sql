/****** Object:  StoredProcedure [dbo].[stp_field_property_getAllFieldProperties]    Script Date: 09/16/2010 02:54:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_field_property_getAllFieldProperties]
AS

SELECT 
	* 
FROM 
	field_properties

