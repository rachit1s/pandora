/****** Object:  StoredProcedure [dbo].[stp_field_property_insert]    Script Date: 09/16/2010 02:10:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[stp_field_property_insert]
(
	@sys_id			INT,
	@field_id		INT,
	@property		NVARCHAR(50),
	@value			NVARCHAR(MAX),
	@description	NVARCHAR(500)
)
AS

INSERT INTO field_properties
(
	sys_id,
	field_id,
	property,
	value,
	description
)
VALUES
(
	@sys_id,
	@field_id,
	@property,
	@value,
	@description
)

