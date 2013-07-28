insert into features (feature_id,name,description,date_added) values ('2','CO_BROWSE','co browse', now() );

insert into features (feature_id,name,description,date_added) values ('1','CO_VIEW','co view', now() );

insert into feature_configs (feature_config_id,client_id,account_id,queue_id,feature_id,active,date_added) values ('1','nemo-client-lnd',null,'lnd-queue-customer-support','1',true,now());

insert into feature_configs (feature_config_id,client_id,account_id,queue_id,feature_id,active,date_added) values ('2','nemo-client-lnd',null,'lnd-queue-customer-support','2',false,now());

insert into cobrowse_configuration (cobrowse_config_id,client_id,account_id,queue_id,settings_key,settings_value, date_added)
values ( '1', 'nemo-client-lnd',null,'lnd-queue-customer-support','cobrowseApiKey','510771e0e25171a26f000002', now());

insert into cobrowse_configuration (cobrowse_config_id,client_id,account_id,queue_id,settings_key,settings_value, date_added)
values ( '2', 'nemo-client-lnd',null,'lnd-queue-customer-support','cobrowseFieldMasking','{\"http://bestbuy1.com/order.html\" : [ \"#cc\",\"#pin\" ], \"http://bestbuy1.com/confirm.html\":[ \"#address\",\"#couponcode\" ]}', now())

insert into client_startup_configs (csc_id,csc_client_id,csc_scope_type,csc_scope_value,csc_variable_name,csc_variable_value,date_added)
values ('lnd_csc_cobrowse1', 'nemo-client-lnd','client','nemo-client-lnd','Cobrowse_Invite_Timeout_In_Seconds',60,now())