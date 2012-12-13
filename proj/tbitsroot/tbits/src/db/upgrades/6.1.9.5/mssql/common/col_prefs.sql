IF EXISTS (SELECT 1 
    FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_TYPE='BASE TABLE' 
    AND TABLE_NAME='user_grid_col_prefs') 
	DROP TABLE user_grid_col_prefs
ELSE
	CREATE TABLE [dbo].[user_grid_col_prefs](
		[user_id] [int] NOT NULL,
		[view_id] [int] NULL,
		[sys_id] [int] NULL,
		[field_id] [int] NULL,
		[col_size] [int] NULL
	)
