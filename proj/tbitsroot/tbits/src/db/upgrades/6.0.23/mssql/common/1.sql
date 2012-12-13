SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[tmp_ba_mail_accounts]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	drop table tmp_ba_mail_accounts
END

CREATE TABLE [dbo].[tmp_ba_mail_accounts](
	[email_id] [varchar](50) NOT NULL,
	[mail_server] [varchar](100) NOT NULL,
	[ba_prefix] [varchar](100) NOT NULL,
	[passward] [varchar](50) NOT NULL,
	[protocol] [varchar](50) NOT NULL,
	[port] [int] NULL,
	[is_active] [bit] NULL,
	[ba_mail_ac_id] int identity(1,1)
)


insert into tmp_ba_mail_accounts (email_id, mail_server, ba_prefix, passward, protocol, port, is_active) 
select email_id, mail_server, ba_prefix, passward, protocol, port, is_active from ba_mail_accounts

alter table ba_mail_accounts add ba_mail_ac_id int,[category_id] [int] NULL,[email_address] [varchar](200) NULL

delete from ba_mail_accounts

insert into ba_mail_accounts (email_id, mail_server, ba_prefix, passward, protocol, port, is_active, ba_mail_ac_id) 
select email_id, mail_server, ba_prefix, passward, protocol, port, is_active, ba_mail_ac_id from tmp_ba_mail_accounts

drop table tmp_ba_mail_accounts
GO
