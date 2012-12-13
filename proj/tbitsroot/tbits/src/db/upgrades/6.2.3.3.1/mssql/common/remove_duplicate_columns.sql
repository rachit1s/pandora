------------commons tables
  select distinct * 
    into tmp_escalation_heirarchy
    from dbo.escalation_heirarchy

    delete from escalation_heirarchy

    insert into escalation_heirarchy
    select * from tmp_escalation_heirarchy

    drop table tmp_escalation_heirarchy
-------------------ba_menu_table

    select distinct * 
    into tmp_ba_menu_table
    from dbo.ba_menu_table

    delete from ba_menu_table

    insert into ba_menu_table
    select * from tmp_ba_menu_table

    drop table tmp_ba_menu_table
--------------------captions_properties

    select distinct sys_id,name, convert(varchar(1000), value) value
    into tmp_captions_properties
    from dbo.captions_properties

    delete from captions_properties

    insert into captions_properties
    select * from tmp_captions_properties

    drop table tmp_captions_properties

---------------------- escalation_history

select distinct * 
    into tmp_escalation_history
    from dbo.escalation_history

    delete from escalation_history

    insert into escalation_history
    select * from tmp_escalation_history

    drop table tmp_escalation_history

--------------gadget_user_config
select distinct * 
    into tmp_gadget_user_config
    from dbo.gadget_user_config

    delete from gadget_user_config

    insert into gadget_user_config
    select * from tmp_gadget_user_config

    drop table tmp_gadget_user_config


-------------------gadget_user_params
  select distinct * 
    into tmp_gadget_user_params
    from dbo.gadget_user_params

    delete from gadget_user_params

    insert into gadget_user_params
    select * from tmp_gadget_user_params

    drop table tmp_gadget_user_params



-------------------mail_list_users

   select distinct * 
    into tmp_mail_list_users
    from dbo.mail_list_users

    delete from mail_list_users

    insert into mail_list_users
    select * from tmp_mail_list_users

    drop table tmp_mail_list_users


------------------------report_params
select distinct * 
    into tmp_report_params
    from dbo.report_params

    delete from report_params

    insert into report_params
    select * from tmp_report_params

    drop table tmp_report_params

--------------------------report_roles
 select distinct * 
    into tmp_report_roles
    from dbo.report_roles

    delete from report_roles

    insert into report_roles
    select * from tmp_report_roles

    drop table tmp_report_roles

--------------------------report_specific_users

select distinct * 
    into tmp_report_specific_users
    from dbo.report_specific_users

    delete from report_specific_users

    insert into report_specific_users
    select * from tmp_report_specific_users

    drop table tmp_report_specific_users

----------------------reports

 /*  select distinct * 
    into tmp_reports
    from dbo.reports

    delete from reports

    insert into reports
    select * from tmp_reports

    drop table tmp_reports
*/
---------------------------tbits_properties
/*
       select distinct * 
    into tmp_tbits_properties
    from dbo.tbits_properties

    delete from tbits_properties

    insert into tbits_properties
    select * from tmp_tbits_properties

    drop table tmp_tbits_properties
*/
---------------------------------