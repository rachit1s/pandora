/****** Object:  StoredProcedure [dbo].[stp_ba_caption_insert]    Script Date: 11/04/2008 18:20:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_ba_caption_insert]
(
	@sys_id			INT,
	@name			VARCHAR(150),
	@value			TEXT
)
AS
INSERT INTO CAPTIONS_PROPERTIES 
(
	sys_id,
	name,
	value
) 
VALUES
(
	@sys_id ,
	@name,
	@value
)