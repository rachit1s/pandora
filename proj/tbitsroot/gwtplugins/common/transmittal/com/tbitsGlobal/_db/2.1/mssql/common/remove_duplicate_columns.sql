-----------------remove the duplicate columns

------------trn_attachment_selection_table_columns
  select distinct * 
    into tmp_trn_attachment_selection_table_columns
    from dbo.trn_attachment_selection_table_columns

    delete from trn_attachment_selection_table_columns

    insert into trn_attachment_selection_table_columns
    select * from tmp_trn_attachment_selection_table_columns

    drop table tmp_trn_attachment_selection_table_columns

    

-------trn_distribution_table_column_config havethe text fields
/*	select distinct * 
		into tmp_trn_distribution_table_column_config
		from dbo.trn_distribution_table_column_config

		delete from trn_distribution_table_column_config

		insert into trn_distribution_table_column_config
		select * from tmp_trn_distribution_table_column_config

		drop table tmp_trn_distribution_table_column_config

*/
-----------------------trn_drawing_number_field
  select distinct * 
		into tmp_trn_drawing_number_field
		from dbo.trn_drawing_number_field

		delete from trn_drawing_number_field

		insert into trn_drawing_number_field
		select * from tmp_trn_drawing_number_field

		drop table tmp_trn_drawing_number_field
--------------trn_max_serial
IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_max_serial]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
select distinct * 
		into tmp_trn_max_serial
		from dbo.trn_max_serial

		delete from trn_max_serial

		insert into trn_max_serial
		select * from tmp_trn_max_serial

		drop table tmp_trn_max_serial
END 
GO
-----------------------trn_post_transmittal_field_values

select distinct * 
		into tmp_trn_post_transmittal_field_values
		from dbo.trn_post_transmittal_field_values

		delete from trn_post_transmittal_field_values

		insert into trn_post_transmittal_field_values
		select * from tmp_trn_post_transmittal_field_values

		drop table tmp_trn_post_transmittal_field_values

------------------trn_process_parameters
    /*     
        select distinct * 
		into tmp_trn_process_parameters
		from dbo.trn_process_parameters

		delete from trn_process_parameters

		insert into trn_process_parameters
		select * from tmp_trn_process_parameters

		drop table tmp_trn_process_parameters
*/

-------------------trn_processes
        select distinct * 
		into tmp_trn_processes
		from dbo.trn_processes

		delete from trn_processes

		insert into trn_processes
		select * from tmp_trn_processes

		drop table tmp_trn_processes

-------------------------trn_src_target_field_mapping

         select distinct * 
		into tmp_trn_src_target_field_mapping
		from dbo.trn_src_target_field_mapping

		delete from trn_src_target_field_mapping

		insert into trn_src_target_field_mapping
		select * from tmp_trn_src_target_field_mapping

		drop table tmp_trn_src_target_field_mapping
-------------------------