/****** Object:  StoredProcedure [dbo].[stp_request_lookupBySystemIdAndParentId]    Script Date: 10/14/2008 11:24:21 ******/
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[stp_request_lookupBySystemIdAndParentId]
(
	@systemId int,
	@parentId int
)
AS
SELECT 
	count(*) requestCount
FROM 
	requests 
WHERE
	sys_id = @systemId and 
	parent_request_id = @parentId 

