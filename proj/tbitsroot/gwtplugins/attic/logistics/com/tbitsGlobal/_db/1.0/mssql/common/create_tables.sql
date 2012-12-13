CREATE TABLE logistics_stages(
	[stage_id] [int] NOT NULL,
	[source_sys_prefix] [varchar](max) NOT NULL,
	[pre_sys_prefix] [varchar](max) NOT NULL
)

CREATE TABLE logistics_stage_params(
	[stage_id] [int] NOT NULL,
	[property] [nvarchar](max) NOT NULL,
	[value] [nvarchar](max) NULL
)

CREATE TABLE logistics_request_heirarchy(
	[stage_id] [int] NOT NULL,
	[source_request_id] [int] NOT NULL,
	[prev_request_id] [int] NOT NULL
)
