 CREATE TABLE dbo.event_registry
(
event_id bigint IDENTITY (1,1) NOT NULL,
source_id varchar(255) NOT NULL,
event_class varchar(255) NOT NULL,
event_handler_class varchar(255) NOT NULL,
is_enabled bit NOT NULL,
event_order int NOT NULL,
description varchar(1023),
PRIMARY KEY (event_id),
UNIQUE (event_class, event_handler_class)
)
