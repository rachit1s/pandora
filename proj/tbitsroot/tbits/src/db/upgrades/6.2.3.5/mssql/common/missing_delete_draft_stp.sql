IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndDraftId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndDraftId]
(
	@userId	INT,
	@systemId 	INT,
	@draftId	INT
	
)
AS
SELECT * FROM user_drafts
WHERE
	sys_id 		= @systemId AND
	draft_id = @draftId AND
	user_id		= @userId

' 
END
GO
