SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Karan Gupta
-- Create date: 11 Oct 2010
-- Description:	Delete a public tag with the specified name
-- =============================================
CREATE PROCEDURE stp_tags_delete_public_tag 
	-- Add the parameters for the stored procedure here
	@name varchar(50)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	
	--////////////////////////////////////////////////////////////
	declare @tag_id int
	select @tag_id=tag_id from tags_definitions where name=@name and user_id=-1
	if(@@rowcount=1)
		delete from tags_definitions where tag_id=@tag_id
	--////////////////////////////////////////////////////////////
    -- Insert statements for procedure here
	SELECT @tag_id, @name
END
GO
