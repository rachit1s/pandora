
/****** Object:  StoredProcedure [dbo].[stp_field_insertWithExistingFieldId]    Script Date: 04/19/2012 10:40:35 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[stp_field_insertWithExistingFieldId]
(
	@sys_id			INT,
	@field_id		INT,
	@name			NVARCHAR(255),
	@display_name		NVARCHAR(255),
	@description		NVARCHAR(255),
	@data_type_id		INT,
	@is_active		BIT,
	@is_extended		BIT,
	@is_private		BIT,
	@tracking_option 	INT,
	@permission		INT,
	@regex			VARCHAR(7999),
	@is_dependent	BIT,
	@display_order	INT,
	@display_group	INT,
	@error  VARCHAR(7999)
)
AS
INSERT INTO fields
(
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
)
VALUES
(
	@sys_id,
	@field_id,
	@name,
	@display_name,
	@description,
	@data_type_id,
	@is_active,
	@is_extended,
	@is_private,
	@tracking_option,
	@permission,
	@regex,
	@is_dependent,
	@display_order,
	@display_group,
	@error
)

SET ANSI_NULLS ON
