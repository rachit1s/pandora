set ANSI_NULLS OFF
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_user_draft_update]
(
	@userId         INT,
	@timeStamp  	DATETIME,
	@sysId          INT,
	@requestId      INT,
	@draft          NTEXT,
	@draftId	INT
)
AS

update user_drafts 
set  
	draft = @draft
where
	user_id 	= @userId And
	sys_id 		= @sysId And 
	draft_id 	= @draftId



GO
