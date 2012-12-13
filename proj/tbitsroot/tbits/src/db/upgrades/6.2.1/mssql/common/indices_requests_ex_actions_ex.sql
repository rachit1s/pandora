CREATE NONCLUSTERED INDEX [idx_actions_ex_sys_id_field_id_int_value] ON [dbo].[actions_ex] 
(
	[sys_id] ASC,
	[field_id] ASC,
	[int_value] ASC
)WITH (STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = OFF) ON [PRIMARY]
CREATE NONCLUSTERED INDEX [idx_actions_ex_sys_id_field_id_real_value] ON [dbo].[actions_ex] 
(
	[sys_id] ASC,
	[field_id] ASC,
	[real_value] ASC
)WITH (STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = OFF) ON [PRIMARY]

CREATE NONCLUSTERED INDEX [idx_requests_ex_sys_id_real_value] ON [dbo].[requests_ex] 
(
	[sys_id] ASC,
	[real_value] ASC
)WITH (STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = OFF) ON [PRIMARY]
CREATE NONCLUSTERED INDEX [idx_requests_ex_sys_id_int_value] ON [dbo].[requests_ex] 
(
	[sys_id] ASC,
	[int_value] ASC
)WITH (STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = OFF) ON [PRIMARY]

/****** Object:  Index [idx_sys_id_request_id]    Script Date: 09/04/2010 13:12:09 ******/
CREATE NONCLUSTERED INDEX [idx_sys_id_request_id] ON [dbo].[related_requests] 
(
	[primary_sys_id] ASC,
	[primary_request_id] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]
