set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Sandeep Giri
-- Create date: 14 Feb 2009
-- Description:	increments the requestId value and gets the previous value
-- =============================================
CREATE PROCEDURE [dbo].[stp_ba_incrAndGetRequestId] 
	@systemId INT
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	--- Read the max request id from business areas and add one to it.
	--SET TRANSACTION ISOLATION LEVEL READ COMMITTED
	BEGIN TRANSACTION
	UPDATE business_areas 
	SET 
			max_request_id = ISNULL(max_request_id, 0) + 1 
	WHERE 
			sys_id = @systemId 
	
	SELECT 
			max_request_id
	FROM 
			business_areas 
	WHERE 
			sys_id = @systemId 
	--- We cannot hold this value of max_request_id with us till the end of this transaction 
	--- as other processes might be interested in inserting requests. 
	--- So, update the business_areas table with this new max_request_id value. 
	commit TRAN
END


set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_request_insert] 
( 
        @systemId               INT, 
        @requestId              BIGINT, 
        @categoryId             INT, 
        @statusId               INT, 
        @severityId             INT, 
        @requestTypeId          INT, 
        @subject                NVARCHAR(4000), 
        @description            NTEXT, 
        @IsPrivate              BIT, 
        @parentId               BIGINT, 
        @userId                 INT, 
        @maxActionId            INT, 
        @dueDate                DATETIME, 
        @loggedDate             DATETIME, 
        @updatedDate            DATETIME, 
        @headerDesc             NTEXT, 
        @attachments            NTEXT, 
        @summary                NTEXT, 
        @memo                   NTEXT, 
        @append         INT, 
        @notify                 INT, 
        @notifyLoggers          BIT, 
        @repliedToAction        INT, 
        @officeId                         INT 
) 
AS 
--- Max Action Id is always 1 when inserting the request. 
SELECT @maxActionId = 1 
--- Now insert the request. 
INSERT INTO requests 
( 
        sys_id,                 request_id,             category_id, 
        status_id,              severity_id,            request_type_id, 
        subject,                description,            is_private, 
        parent_request_id,      user_id,                max_action_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,            summary, 
        memo,                   append_interface,       notify, 
        notify_loggers, replied_to_action,office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @categoryId, 
        @statusId,              @severityId,            @requestTypeId, 
        @subject,               @description,           @IsPrivate, 
        @parentId,              @userId,                @maxActionId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,           @summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,          @officeId 
)
---- Insert the corresponding record into actions table. 
INSERT INTO actions 
( 
        sys_id,                 request_id,             action_id, 
        category_id,            status_id,              severity_id, 
        request_type_id,        subject,                description, 
        is_private,             parent_request_id,      user_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,            summary, 
        memo,                   append_interface,       notify, 
        notify_loggers,         replied_to_action,      office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @maxActionId, 
        @categoryId,            @statusId,              @severityId, 
        @requestTypeId,         @subject,               @description, 
        @IsPrivate,             @parentId,              @userId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,           @summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,       @officeId 
)
GO

ALTER TABLE business_areas ADD  max_version_no int
GO

-- update the max_version_no with the current max
UPDATE business_areas
set max_version_no = (select max(version_no) from versions where versions.sys_id = business_areas.sys_id)
where exists (select 1 from versions where versions.sys_id = business_areas.sys_id)
GO

-- TODO: Alter the procedure to delete_all_requests and delete_all_requests_in_ba such that it resets the max_version_no too.
CREATE PROCEDURE [dbo].[stp_ba_incrAndGetVersionNumber] 
	@systemId INT
AS
BEGIN
	SET NOCOUNT ON;
	--- Read the max request id from business areas and add one to it.
	--SET TRANSACTION ISOLATION LEVEL READ COMMITTED
	BEGIN TRANSACTION
	UPDATE business_areas 
	SET 
			max_version_no = ISNULL(max_version_no, 0) + 1 
	WHERE 
			sys_id = @systemId 
	
	SELECT 
			max_version_no
	FROM 
			business_areas 
	WHERE 
			sys_id = @systemId 
	commit TRAN
END
GO




/****** Object:  StoredProcedure [dbo].[stp_display_group_insert_sys_id_column]    Script Date: 04/13/2009 15:43:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

create procedure [dbo].[stp_display_group_insert_sys_id_column]
AS
Declare @sys_id int
Declare @display_group int
Declare @NewDisplay_group int

update fields set display_group = 1 where display_name = 'SMS Id'
update fields set display_group = 1 where display_name = 'send SMS'

update fields Set display_group = 1 where display_group is null or display_group = 0

update fields set display_order = (select max(display_order) from fields where display_group = 1) where display_name = 'SMS Id'
update fields set display_order = (select max(display_order) from fields where display_group = 1) where display_name = 'Send SMS'


----change in the display_group add the sys_id and 

alter table display_groups add  sys_id int

---
begin

--------------------------------

select id,display_name,display_order,is_active  
into #tmp 
from display_groups 
----------------------------------
select distinct f.sys_id,display_group ,dg.display_name
into #tmp1
from fields f 
join display_groups dg on dg.id = f.display_group
where is_extended = 1 and display_group not in ( 0,1)
----------------------------------

delete from display_groups where id in (select display_group from #tmp1)

insert into display_groups (display_name,display_order,is_active,sys_id )
( select t.display_name,t.display_order,t.is_active,t1.sys_id 
from #tmp t 
join #tmp1 t1 on t.id = t1.display_group )
----------------------------------
--- now update fields with new display_id
select dg.id,dg.sys_id,t1.display_group
into #tmp2
from display_groups dg 
join #tmp1 t1 on dg.sys_id = t1.sys_id and dg.display_name = t1.display_name
------------------------------------
while(exists(select * from #tmp2))
begin
select 
   @NewDisplay_group = id,
   @sys_id = sys_id,
   @display_group = display_group
from #tmp2
--update field
update fields 
 set display_group = @NewDisplay_group
 where sys_id = @sys_id 
 and display_group = @display_group
--delete from #tmp
delete from #tmp2
 where sys_id = @sys_id
 and id = @NewDisplay_group
 and display_group = @display_group

select * from #tmp2
END

-------------------
drop table #tmp
drop table #tmp1
drop table #tmp2
END
GO
EXECUTE stp_display_group_insert_sys_id_column
GO

/****** Object:  StoredProcedure [dbo].[stp_display_group_reorder_sys_id_column]    Script Date: 04/13/2009 15:48:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[stp_display_group_reorder_sys_id_column]
as
Begin
ALTER TABLE [dbo].[display_groups] DROP CONSTRAINT [DF_display_groups_is_active]
ALTER TABLE [dbo].[display_groups] DROP CONSTRAINT [DF_display_groups_display_order]

CREATE TABLE dbo.Tmp_display_groups
	(
	 sys_id int NULL,
	 id int NOT NULL IDENTITY (1, 1),
	 display_name varchar(50) NULL,
	 display_order int NULL CONSTRAINT DF_display_groups_display_order DEFAULT ((0)),
	 is_active bit NOT NULL CONSTRAINT DF_display_groups_is_active DEFAULT ((1))
	)
SET IDENTITY_INSERT dbo.Tmp_display_groups ON

IF EXISTS(SELECT * FROM dbo.display_groups)
	 EXEC('INSERT INTO dbo.Tmp_display_groups (sys_id, id, display_name, display_order, is_active)
		SELECT sys_id, id, display_name, display_order, is_active FROM dbo.display_groups WITH (HOLDLOCK TABLOCKX)')

SET IDENTITY_INSERT dbo.Tmp_display_groups OFF

DROP TABLE dbo.display_groups

EXECUTE sp_rename N'dbo.Tmp_display_groups', N'display_groups', 'OBJECT'

ALTER TABLE dbo.display_groups ADD CONSTRAINT
	PK_display_groups PRIMARY KEY CLUSTERED 
	(
	id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) 
END
GO
EXECUTE stp_display_group_reorder_sys_id_column
GO
update display_groups set sys_id  = 0 where  sys_id is null 
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author: Lokesh
-- Create date: 4 APR 2009
-- Description:	Inserts the display group
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_insert]
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit,
	@new_id int output
AS
BEGIN
	insert into display_groups (sys_id, display_name, display_order, is_active) 
	Values(@sys_id, @display_name, @display_order, @is_active);
	select @new_id = @@IDENTITY;
END

/****** Object:  StoredProcedure [dbo].[stp_display_group_update]    Script Date: 04/10/2009 17:28:19 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_update]
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit
AS
BEGIN
	update display_groups set display_name = @display_name, display_order = @display_order, is_active = @is_active
	where id = @id
END
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[stp_display_group_lookupBySystemIdAndDisplayName] 
	-- Add the parameters for the stored procedure here
	@sys_id			INT,
	@displayName 	VARCHAR(128)
AS
BEGIN
	select sys_id, id, display_name, display_order, is_active 
	from display_groups
	where display_name = @displayName and sys_id = @sys_id
END
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_delete]
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit,
	@ret_value int output
AS
BEGIN
	delete from display_groups where id = @id;
	select @ret_value = 1;
END