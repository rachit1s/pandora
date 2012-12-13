-- ================================================
-- Template generated from Template Explorer using:
-- Create Procedure (New Menu).SQL
--
-- Use the Specify Values for Template Parameters 
-- command (Ctrl-Shift-M) to fill in the parameter 
-- values below.
--
-- This block of comments will not be included in
-- the definition of the procedure.
-- ================================================
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Sourabh Agrawal
-- Description:	STP to update role
-- =============================================
CREATE PROCEDURE [dbo].[stp_roles_update]  
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@role_id int,
	@rolename nvarchar(50),
	@description nvarchar(250),
    @field_id int,
    @can_be_deleted int
AS
BEGIN
	UPDATE [dbo].[roles]
    SET rolename = @rolename,
	description = @description,
	field_id = @field_id,
	can_be_deleted = @can_be_deleted
	WHERE sys_id = @sys_id AND role_id = @role_id
	
	select @role_id
END
GO

