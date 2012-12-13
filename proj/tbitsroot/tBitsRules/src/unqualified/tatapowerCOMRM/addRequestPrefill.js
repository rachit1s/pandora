<script type="text/javascript">
function changeAssignee(){
	
	var assigneeMap = <%=assigneeMap_value%> ; 

	var packageCtrl =  document.getElementById('category_id');
	var disciplineCtrl = document.getElementById('request_type_id');
	var assigneeCtrl = document.getElementById('assignee_ids');

	if ((packageCtrl == null) || (disciplineCtrl == null) || (assigneeCtrl == null)){
		if (assignee != null)
			assignee.value = "";
		return false;
	}

	var packageOption = packageCtrl.options[packageCtrl.selectedIndex] ;
	var packageValue = packageOption.value ;

	var disciplineOption = disciplineCtrl.options[disciplineCtrl.selectedIndex] ;
	var disciplineValue = disciplineOption.value ;

	var assigneeList = null;
	if(assigneeMap != null)
	{
		for( var i = 0 ; i < assigneeMap.length ; i++ )
		{
			if( (assigneeMap[i].categoryName == packageValue) && (assigneeMap[i].requestTypeName == disciplineValue) )
			{
				if (assigneeList == null)
					assigneeList = assigneeMap[i].userLogin + "," ;
				else 
					assigneeList = assigneeList + assigneeMap[i].userLogin + "," ;
			}
		}
	}

	assigneeCtrl.readOnly = true ;
	if( null != assigneeList )
	{
		assigneeCtrl.value = assigneeList;
		return true ;
	}
	else
	{
		assigneeCtrl.value = "" ;
		return false ;
	}	
}

function initPreFillers(){
	var packageCtrl =  document.getElementById('category_id');
	var disciplineCtrl = document.getElementById('request_type_id');
	changeAssignee();
	YAHOO.util.Event.addListener(packageCtrl, 'change', changeAssignee) ;
	YAHOO.util.Event.addListener(disciplineCtrl, 'change', changeAssignee) ;
}

//register the window load event
YAHOO.util.Event.addListener(window, "load", initPreFillers);
</script>