/****** Object:  StoredProcedure [dbo].[stp_scurve_generate_curve]    Script Date: 09/06/2010 14:59:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Karan Gupta
-- Create date: 6 Sept 2010
-- Description:	
-- =============================================
CREATE PROCEDURE [dbo].[stp_scurve_generate_curve] 
	-- Add the parameters for the stored procedure here
	@curve_id int = 0
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	-- \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	declare @currentdate datetime
	declare @startdate datetime
	declare @enddate datetime
	declare @lastdate datetime
	declare @cumulative_a real
	declare @cumulative_e real
	declare @cumulative_l real
	declare @sys_id int

	set @cumulative_a = 0
	set @cumulative_e = 0
	set @cumulative_l = 0

	create table #values(
		request_id int,
		weightage real,
		actual_factor real,
		early_factor real,
		late_factor real
	)

	select @sys_id=sys_id from scurve_curves where curve_id=@curve_id

	select weights_table.request_id, weights_table.action_id, weights_table.real_value as weightage, 
				factors_table.real_value as factor, factors_table.revision_datetime
	into #revisions
	from
		(select ae.sys_id, ae.request_id, ae.action_id, ae.real_value
		from actions_ex ae 
		join scurve_curve_requests scr on ae.request_id=scr.request_id and scr.curve_id=@curve_id
		join fields f on f.name='Weightage' and ae.sys_id=f.sys_id and ae.field_id=f.field_id) weights_table
	join
		(select ae.sys_id, ae.request_id, ae.action_id, ae.real_value, a.lastupdated_datetime as revision_datetime
		from actions_ex ae 
		join scurve_curve_requests scr on ae.request_id=scr.request_id and scr.curve_id=@curve_id
		join fields f on f.name='factors' and ae.sys_id=f.sys_id and ae.field_id=f.field_id
		join actions a on a.sys_id=ae.sys_id and a.request_id=ae.request_id and a.action_id=ae.action_id) factors_table
	on weights_table.sys_id=factors_table.sys_id and weights_table.request_id=factors_table.request_id 
					and weights_table.action_id=factors_table.action_id


	select ae.request_id, aex.real_value as weightage, sf.factor, 
			dateadd(d, sf.turn_around_time, aex1.datetime_value) as latedate, 
			dateadd(d,sf.turn_around_time,ae.datetime_value) as earlydate
	into #predictions
	from actions_ex ae
	join fields f on ae.sys_id=f.sys_id and ae.field_id=f.field_id and f.name='early_start_datetime'
	join actions_ex aex on ae.sys_id=aex.sys_id and ae.request_id=aex.request_id and ae.action_id=aex.action_id
	join fields f1 on aex.sys_id=f1.sys_id and aex.field_id=f1.field_id and f1.name='Weightage'
	join actions_ex aex1 on ae.sys_id=aex1.sys_id and ae.request_id=aex1.request_id and ae.action_id=aex1.action_id
	join fields f2 on aex1.sys_id=f2.sys_id and aex1.field_id=f2.field_id and f2.name='late_start_datetime'
	join scurve_factors sf on sf.curve_id=@curve_id
	join scurve_curve_requests scr on ae.request_id=scr.request_id
	where ae.action_id=1 and scr.curve_id=@curve_id

	declare @tri int
	declare @tw real
	declare fill_values cursor for
	select distinct request_id, weightage from #predictions
	open fill_values
	fetch next from fill_values into @tri, @tw
	while(@@fetch_status <> -1)
	begin
	if(@@fetch_status <> -2)
	insert into #values values (@tri, @tw, 0, 0, 0)
	fetch next from fill_values into @tri, @tw
	end
	close fill_values
	deallocate fill_values

	select @startdate=start_datetime from scurve_curves where curve_id=@curve_id
	select @enddate=end_datetime from scurve_curves where curve_id=@curve_id

	set @lastdate=@startdate
	set @currentdate=dateadd(d,1,@lastdate)
	while @currentdate<=@enddate
	begin
		select request_id, action_id, weightage, factor into #temp_table from #revisions where revision_datetime between @lastdate and @currentdate
		
		declare updated_revisions cursor for
			select tt1.request_id, tt1.factor, tt1.weightage
			from (select request_id, max(action_id) as max_action from #temp_table group by request_id) tt
			join #temp_table tt1 on tt.max_action=tt1.action_id and tt.request_id=tt1.request_id
		declare early_prediction cursor for
			select request_id, factor, weightage from #predictions where earlydate between @lastdate and @currentdate
		declare late_prediction cursor for
			select request_id, factor, weightage from #predictions where latedate between @lastdate and @currentdate
		declare @req_id int
		declare @fctr real
		declare @wght real
		declare @value real
		declare @tmp_wght real
		declare @tmp_fctr real
		
		open updated_revisions
		fetch next from updated_revisions into @req_id, @fctr, @wght
		while(@@fetch_status <> -1)
		begin
		if(@@fetch_status <> -2)
		select @tmp_wght=weightage, @tmp_fctr=actual_factor from #values where request_id=@req_id
		set @value = @tmp_wght*@tmp_fctr
		set @cumulative_a = @cumulative_a - @value
		update #values set actual_factor=@fctr where request_id=@req_id
		set @cumulative_a = @cumulative_a + @fctr*@wght
		fetch next from updated_revisions into @req_id, @fctr, @wght
		end
		close updated_revisions
		deallocate updated_revisions

		open early_prediction
		fetch next from early_prediction into @req_id, @fctr, @wght
		while(@@fetch_status <> -1)
		begin
		if(@@fetch_status <> -2)
		select @tmp_wght=weightage, @tmp_fctr=early_factor from #values where request_id=@req_id
		set @value = @tmp_wght*@tmp_fctr
		set @cumulative_e = @cumulative_e - @value
		update #values set early_factor=@fctr where request_id=@req_id
		set @cumulative_e = @cumulative_e + @fctr*@wght
		fetch next from early_prediction into @req_id, @fctr, @wght
		end
		close early_prediction
		deallocate early_prediction

		open late_prediction
		fetch next from late_prediction into @req_id, @fctr, @wght
		while(@@fetch_status <> -1)
		begin
		if(@@fetch_status <> -2)
		select @tmp_wght=weightage, @tmp_fctr=late_factor from #values where request_id=@req_id
		set @value = @tmp_wght*@tmp_fctr
		set @cumulative_l = @cumulative_l - @value
		update #values set late_factor=@fctr where request_id=@req_id
		set @cumulative_l = @cumulative_l + @fctr*@wght
		fetch next from late_prediction into @req_id, @fctr, @wght
		end
		close late_prediction
		deallocate late_prediction

		insert into scurve_curve_points values (@curve_id, @currentdate, @cumulative_e, @cumulative_a, @cumulative_l)
		
		delete from #temp_table
		drop table #temp_table
		set @lastdate=dateadd(d,1,@lastdate)
		set @currentdate=dateadd(d,1,@currentdate)
	end

	-- \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	SELECT @sys_id, @curve_id
END