set ANSI_NULLS OFF
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_user_draft_insert]
(
	@userId           INT,
	@timeStamp  datetime,
	@sysId              INT,
	@requestId      INT,
	@draft               ntext,
	@draftid	INT OUT
)
AS
declare @maxDraftId INT;
select @maxDraftId=ISNULL(max(draft_id),0) from user_drafts;
select @draftId = @maxDraftId + 1;
--delete from  user_drafts 
--where
--	user_id = @userId And
--	sys_id=@sysId And 
--	request_id = @requestId And
--	convert(smallDateTime, time_stamp) = convert(smallDateTime, @timeStamp) AND
--	abs(Datepart(millisecond,time_stamp) - Datepart(millisecond,@timeStamp)) < 10
	

INSERT INTO user_drafts
(
	user_id,
	time_stamp,
	sys_id, 
	request_id,
	draft,
	draft_id
)
VALUES
(
	@userId,
	@timeStamp,
	@sysId,
	@requestId,
	@draft,
	@draftId
)
GO
