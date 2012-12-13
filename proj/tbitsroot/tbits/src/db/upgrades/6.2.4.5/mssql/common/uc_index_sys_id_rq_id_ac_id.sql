/****** Object:  Index [versions_idx_sysid_rid_aid]    Script Date: 06/01/2011 17:06:12 ******/
IF  EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[versions]') AND name = N'versions_idx_sysid_rid_aid')
DROP INDEX [versions_idx_sysid_rid_aid] ON [dbo].[versions] WITH ( ONLINE = OFF )
GO

/****** Object:  Index [versions_idx_sysid_rid_aid]    Script Date: 06/01/2011 17:06:12 ******/
CREATE CLUSTERED INDEX [versions_idx_sysid_rid_aid] ON [dbo].[versions] 
(
	[sys_id] ASC,
	[request_id] ASC,
	[action_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO
