---Authors: Raghu & MM

---This script implements temporal DB structure in mail_list_users table of tBits database 
---




--STEP 1: CREATING TEMPORAL_MAIL_LIST_USERS TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_mail_list_users')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_mail_list_users](
	[mail_list_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_mail_list_users]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_mail_list_users]'))
DROP TRIGGER [dbo].[audit_mail_list_users]
GO

/****** Object:  Trigger [dbo].[audit_mail_list_users]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_mail_list_users] ON [dbo].[mail_list_users] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_mail_list_users 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_mail_list_users 
 WHERE temporal_mail_list_users.mail_list_id=deleted.mail_list_id and temporal_mail_list_users.user_id=deleted.user_id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_mail_list_users(mail_list_id,user_id,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT mail_list_id,user_id, @TrigTime , '9/9/9999'FROM INSERTED

GO
