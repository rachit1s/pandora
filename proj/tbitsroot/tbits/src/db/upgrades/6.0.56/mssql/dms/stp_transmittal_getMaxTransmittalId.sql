set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Lokesh
-- Create date: 16-Apr-2009
-- Description:	Fetch max transmittal id.
-- =============================================
CREATE PROCEDURE [dbo].[stp_transmittal_getMaxTransmittalId]
	-- Add the parameters for the stored procedure here
	@sysId INT
AS

DECLARE @max_id int
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	BEGIN TRANSACTION
	SELECT @max_id = ISNULL(max_transmittal_id, 0) + 1
	FROM ba_max_transmittal_ids
	WHERE sys_id=@sysId

	UPDATE ba_max_transmittal_ids
	SET max_transmittal_id = @max_id
	WHERE sys_id=@sysId

	SELECT max_transmittal_id from ba_max_transmittal_ids
	COMMIT TRAN
END


