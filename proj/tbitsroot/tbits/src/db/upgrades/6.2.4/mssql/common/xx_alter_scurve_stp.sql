SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		Karan Gupta
-- Create date: 13 Sept 2010
-- Description:	
-- =============================================
ALTER PROCEDURE [dbo].[stp_scurve_generate_curve] 
	-- Add the parameters for the stored procedure here
	@curve_id int,
	@weightageField varchar(50),
	@factorField varchar(50),
	@earlydateField varchar(50),
	@latedateField varchar(50)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	-- \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	declare @sys_id int

	select @sys_id=sys_id from scurve_curves where curve_id=@curve_id

	-- Revisions table will contain the actual revison dates and the factors that the requests are revised to
	select ae.request_id, ae.real_value/100 as factor, a.lastupdated_datetime as revision_datetime
	into #revisions
	from actions_ex ae 
	join scurve_curve_requests scr on ae.request_id=scr.request_id
	join fields f on ae.sys_id=f.sys_id and ae.field_id=f.field_id
	join actions a on a.sys_id=ae.sys_id and a.request_id=ae.request_id and a.action_id=ae.action_id
	where scr.curve_id=@curve_id and ae.sys_id=@sys_id and f.name=@factorField

	-- Predictions table are :
	--							>> Early Prediction table that will contain the early dates for factor progression
	--							   of each request given in scurve_curve_requests
	--							>> Late Prediction table that will contain the late dates for factor progression
	--							   of each request given in scurve_curve_requests
	select scr.request_id, sf.factor/100 as factor, sf.turn_around_time
	into #temp_factors_table
	from scurve_factors sf
	join scurve_curve_requests scr on scr.curve_id=sf.curve_id
	where sf.curve_id=@curve_id
	
	select tft.request_id, tft.factor, 
			dateadd(d,tft.turn_around_time,re.datetime_value) as earlydate
	into #early_predictions
	from requests_ex re
	join fields f on re.sys_id=f.sys_id and re.field_id=f.field_id
	join #temp_factors_table tft on re.request_id=tft.request_id
	where f.name=@earlydateField and re.sys_id=@sys_id

	select tft.request_id, tft.factor, 
			dateadd(d,tft.turn_around_time,re.datetime_value) as latedate
	into #late_predictions
	from requests_ex re
	join fields f on re.sys_id=f.sys_id and re.field_id=f.field_id
	join #temp_factors_table tft on re.request_id=tft.request_id
	where f.name=@latedateField and re.sys_id=@sys_id

	-- Values table that will contain all the current values actual early and late factors
	-- corresponding to every request_id and its weightage
	create table #values(
		request_id int,
		weightage real,
		actual_factor real,
		early_factor real,
		late_factor real
	)

	declare @tri int
	declare @tw real
	declare fill_values cursor for
		select re.request_id, re.real_value as weightage 
		from requests_ex re 
		join scurve_curve_requests scr on re.request_id=scr.request_id
		join fields f on f.sys_id=re.sys_id and f.field_id=re.field_id
		where re.sys_id=@sys_id and scr.curve_id=@curve_id and f.name=@weightageField
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

-- Completed table construction
--^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v
-- Start the algorithm for getting the points 

	declare @currentdate datetime
	declare @startdate datetime
	declare @enddate datetime
	declare @lastdate datetime
	declare @cumulative_a real
	declare @cumulative_e real
	declare @cumulative_l real
	
	set @cumulative_a = 0
	set @cumulative_e = 0
	set @cumulative_l = 0
	select @startdate=start_datetime from scurve_curves where curve_id=@curve_id
	select @enddate=end_datetime from scurve_curves where curve_id=@curve_id
	set @lastdate=@startdate
	set @currentdate=dateadd(d,1,@lastdate)
	
	while @currentdate<=@enddate
	begin
		select request_id, factor, revision_datetime into #temp_table from #revisions where revision_datetime between @lastdate and @currentdate
		
		-- Cursor for revisions updated between @lastdate and @currentdate
		declare updated_revisions cursor for
			select request_id, factor from #revisions where revision_datetime between @lastdate and @currentdate
			--select tt1.request_id, tt1.factor
			--from (select request_id, max(revision_datetime) as max_revision from #temp_table group by request_id) tt
			--join #temp_table tt1 on tt.max_revision=tt1.revision_datetime and tt.request_id=tt1.request_id
		-- Cursors for predictions that change between @lastdate and @currentdate		
		declare early_prediction cursor for
			select request_id, factor from #early_predictions where earlydate between @lastdate and @currentdate
		declare late_prediction cursor for
			select request_id, factor from #late_predictions where latedate between @lastdate and @currentdate
		
		declare @req_id int
		declare @fctr real
		declare @wght real
		declare @value real
		declare @old_fctr real
		
		-- Find changes in @cumulative_a
		open updated_revisions
		fetch next from updated_revisions into @req_id, @fctr
		while(@@fetch_status <> -1)
		begin
			if(@@fetch_status <> -2)
			select @wght=weightage, @old_fctr=actual_factor from #values where request_id=@req_id
			set @value = @wght*@old_fctr
			set @cumulative_a = @cumulative_a - @value
			update #values set actual_factor=@fctr where request_id=@req_id
			set @cumulative_a = @cumulative_a + @fctr*@wght
			fetch next from updated_revisions into @req_id, @fctr
		end
		close updated_revisions
		deallocate updated_revisions
	
		-- Find changes in @cumulative_e
		open early_prediction
		fetch next from early_prediction into @req_id, @fctr
		while(@@fetch_status <> -1)
		begin
			if(@@fetch_status <> -2)
			select @wght=weightage, @old_fctr=early_factor from #values where request_id=@req_id
			set @value = @wght*@old_fctr
			set @cumulative_e = @cumulative_e - @value
			update #values set early_factor=@fctr where request_id=@req_id
			set @cumulative_e = @cumulative_e + @fctr*@wght
			fetch next from early_prediction into @req_id, @fctr
		end
		close early_prediction
		deallocate early_prediction

		-- Find changes in @cumulative_l
		open late_prediction
		fetch next from late_prediction into @req_id, @fctr
		while(@@fetch_status <> -1)
		begin
			if(@@fetch_status <> -2)
			select @wght=weightage, @old_fctr=late_factor from #values where request_id=@req_id
			set @value = @wght*@old_fctr
			set @cumulative_l = @cumulative_l - @value
			update #values set late_factor=@fctr where request_id=@req_id
			set @cumulative_l = @cumulative_l + @fctr*@wght
			fetch next from late_prediction into @req_id, @fctr
		end
		close late_prediction
		deallocate late_prediction

		-- Add the point for current date into the table
		insert into scurve_curve_points values (@curve_id, @currentdate, @cumulative_e, @cumulative_a, @cumulative_l)
		
		-- sanitise
		delete from #temp_table
		drop table #temp_table
		-- increment
		set @lastdate=dateadd(d,1,@lastdate)
		set @currentdate=dateadd(d,1,@currentdate)
	end

	-- \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	SELECT @sys_id as sys_id, @curve_id as curve_id
END
GO

SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER OFF
GO
