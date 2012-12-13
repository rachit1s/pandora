PluginName : corrGeneric
Plugin class Name : corrGeneric.rule.TargetDateRule
PluginFolder : common (tbitsplugins / tbitsbeta plugins)

Entries to be made in corr_protocol_options

1. target_date_field_name : name of the datetime field which needs to be updated. [ Required. ]

2. holiday_calendar_office : the name of the office in holidays_list to be followed for calculating next due date. [ Required. ]

3. is_span_diff_for_add_and_update : if there are different spans for add request and update request then the value of this options should be "yes" else "No". Default is no. [ Optional]

4. target_date_dependent_type : the span to be decided can depend on one of the type values of the request. This option captures the field name of that dropdown field. [ Optional ]

5. target_date_span : this / these are the set of keys defined depending on whether 3 and 4 are defined or not. their general option name form is

[ add_ | update_ ] [ dependentTypeValue_ ] target_date_span

and their option value is an integer that defines the span for that target from today.

Note : this rule is applicable only if user does not provide the due-date by himself.
