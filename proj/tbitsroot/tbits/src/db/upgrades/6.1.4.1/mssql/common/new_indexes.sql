IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[ba_menu_mapping]') AND name = N'idx_ba_menu_mapping_sys_id')
CREATE NONCLUSTERED INDEX [idx_ba_menu_mapping_sys_id] ON [dbo].[ba_menu_mapping] 
(
	[sys_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[types]') AND name = N'idx_types_isactive')
CREATE NONCLUSTERED INDEX [idx_types_isactive] ON [dbo].[types] 
(
	[is_active] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[business_areas]') AND name = N'idx_ba_isactive')
CREATE NONCLUSTERED INDEX [idx_ba_isactive] ON [dbo].[business_areas] 
(
	[is_active] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[user_passwords]') AND name = N'idx_user_passwords_login_password')
CREATE NONCLUSTERED INDEX [idx_user_passwords_login_password] ON [dbo].[user_passwords] 
(
	[user_login] ASC,
	[password] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO


IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[user_read_actions]') AND name = N'idx_ura_sysid_reqid_uid')
CREATE NONCLUSTERED INDEX [idx_ura_sysid_reqid_uid] ON [dbo].[user_read_actions] 
(
	[sys_id] ASC,
	[request_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND name = N'idx_users_email')
CREATE NONCLUSTERED INDEX [idx_users_email] ON [dbo].[users] 
(
	[email] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND name = N'idx_users_login')
CREATE NONCLUSTERED INDEX [idx_users_login] ON [dbo].[users] 
(
	[user_login] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_FIRED_TRIGGERS]') AND name = N'IDX_FIRED_TRIGGERS_NAME_GROUP')
CREATE NONCLUSTERED INDEX [IDX_FIRED_TRIGGERS_NAME_GROUP] ON [dbo].[QRTZ_FIRED_TRIGGERS] 
(
	[TRIGGER_NAME] ASC,
	[TRIGGER_GROUP] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[requests]') AND name = N'idx_requests_parent')
CREATE NONCLUSTERED INDEX [idx_requests_parent] ON [dbo].[requests] 
(
	[parent_request_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[requests]') AND name = N'idx_requests_sys_id_cat')
CREATE NONCLUSTERED INDEX [idx_requests_sys_id_cat] ON [dbo].[requests] 
(
	[sys_id] ASC,
	[category_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO


IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[requests]') AND name = N'idx_requests_sys_id_sev')
CREATE NONCLUSTERED INDEX [idx_requests_sys_id_sev] ON [dbo].[requests] 
(
	[sys_id] ASC,
	[severity_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[requests]') AND name = N'idx_requests_sys_id_type')
CREATE NONCLUSTERED INDEX [idx_requests_sys_id_type] ON [dbo].[requests] 
(
	[sys_id] ASC,
	[request_type_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[requests]') AND name = N'status+sys_id')
CREATE NONCLUSTERED INDEX [status+sys_id] ON [dbo].[requests] 
(
	[sys_id] ASC,
	[status_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[requests_ex]') AND name = N'idx_re_type')
CREATE NONCLUSTERED INDEX [idx_re_type] ON [dbo].[requests_ex] 
(
	[type_value] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
GO
