 CREATE TABLE addon_info
           (
				jar_id bigint IDENTITY (1,1) NOT NULL,
				jar_name varchar(255) NOT NULL,
				status int NOT NULL,
				addon_name varchar(255),
				addon_description varchar(3999),
				addon_author varchar(255),
				jar_bytes varbinary(MAX) NOT NULL,
				PRIMARY KEY (jar_id),
				unique(addon_name)
            )
