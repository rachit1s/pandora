/****** Object:  StoredProcedure [dbo].[stp_tags_create_public_tag]    Script Date: 11/16/2010 17:45:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		Karan Gupta
-- Create date: 11 Oct 2010
-- Description:	Create a public tag with the specified name
-- =============================================
CREATE PROCEDURE [dbo].[stp_tags_create_public_tag] 
	-- Add the parameters for the stored procedure here
	@name varchar(50)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	
	--////////////////////////////////////////////////////////////
	declare @tag_id int
	set @tag_id=0
	
	if exists(select * from tags_definitions)
		select @tag_id=max(tag_id) from tags_definitions
	
	set @tag_id = @tag_id+1
	insert into tags_definitions values (@tag_id, -1, @name)

	--////////////////////////////////////////////////////////////
    -- Insert statements for procedure here
	SELECT @tag_id, @name
END

