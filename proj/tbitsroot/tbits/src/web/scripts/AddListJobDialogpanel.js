function initAddJobPage(){
	 var url = YAHOO.transbit.tbits.contextPath + 'all-job-actions?action=get-classes';
	 var callback = 
		{ 
		  success: function(o) {
		  	var classes = YAHOO.lang.JSON.parse(o.responseText);
		  	var e = document.CaptionsForm.elements['className'];
		  	var anOption;
		  	anOption = document.createElement('option');
	  		anOption.text = "Others";
	  		anOption.value = "";
	  		anOption.selected = "true";
	  		try{
	  			document.CaptionsForm.className.add(anOption,null);
	  		}
	  		catch(ex){
	  			document.CaptionsForm.className.add(anOption); // IE only
	  		}
		  	for(var i in classes){
		  		if(typeof(classes[i]) == "function")
		  			continue;
		  		anOption = document.createElement('option');
		  		anOption.text = classes[i][0];
		  		anOption.value = classes[i][1];
		  		try{
		  			document.CaptionsForm.className.add(anOption,null);
		  		}
		  		catch(ex){
		  			document.CaptionsForm.className.add(anOption); // IE only
		  		}
		  	}
	      }, 
		  failure: function(o) {alert(o.statusText);}
		} 
	 var cObj = YAHOO.util.Connect.asyncRequest('GET', url, callback);
}

function getJobParams(){
	var jobText = document.CaptionsForm.className.options[document.CaptionsForm.className.selectedIndex].text;
	var jobClass = document.CaptionsForm.className.options[document.CaptionsForm.className.selectedIndex].value;
	
	var arr = [];
	arr.push(jobText);
	arr.push(jobClass);
	var jsonarr = YAHOO.lang.JSON.stringify(arr);
	var url = YAHOO.transbit.tbits.contextPath + 'all-job-actions?action=get-job-params&job-class=' + jsonarr;
	var e = document.getElementById('parameterTable');
	var count = e.rows.length;
	while(count > 2){
		e.deleteRow(count - 2);
		count--;
	}
	if(jobClass == null || jobClass == ''){
		AddParamRow();
		AddParamRow();
		AddParamRow();
		AddParamRow();
		document.getElementById('otherTR').disabled = false;
		return;
	}
	document.getElementById('otherTR').disabled = true;
	var callback = 
	{ 
	  success: function(o) {
		var params = YAHOO.lang.JSON.parse(o.responseText);
	  	var anOption;
	  	for(var i in params){
	  		if(typeof(params[i]) == "function")
	  			continue;
	  		AddFilledParamRow(params[i]);
	  	}
      }, 
	  failure: function(o) {alert(o.statusText);}
	} 
 var cObj = YAHOO.util.Connect.asyncRequest('GET', url, callback);
}

function AddFilledParamRow(params){
	var e = document.getElementById('parameterTable');
	var x = e.insertRow(1);
	var y=x.insertCell(0);
	var z=x.insertCell(1);
	
	y.innerHTML = "<input type='text' name='parameterNames' value='" + params['name'] + "' readOnly=true/>";
	if(params['mandatory'] == true)
		y.innerHTML = "<input type='text' name='parameterNames' value='" + params['name'] + "' readOnly=true/><font style='color:#f00;'>*</font>";
	
	//alert(params['defaultValue']);
	if(params['type'] == "Text"){
		if(params['defaultValue'] != null)
			z.innerHTML = "<input type='text' name='parameterValues' value='" + params['defaultValue'] + "' />";
		else
			z.innerHTML = "<input type='text' name='parameterValues' value='' />";
	}
	else if(params['type'] == "TextArea"){
		if(params['defaultValue'] != null)
			z.innerHTML = "<textArea name='parameterValues'>" + params['defaultValue'] + "</textarea>";
		else
			z.innerHTML = "<textArea name='parameterValues'></textarea>";
	}else if(params['type'] == "Select"){
		if(params['values'] != null){
			var a = document.createElement("select");
			a.name = "parameterValues";
			for(var j in params['values']){
				var anOption = document.createElement('option');
				if(typeof(params['values'][j]) == "function") continue;
				if(params['defaultValue'] != null && params['defaultValue'] ==  params['values'][j]){
					anOption.selected = "true";
					anOption.value = params['values'][j];
					anOption.text = params['values'][j];
				}
				else{
					anOption.Value = params['values'][j];
					anOption.text = params['values'][j];
				}
				try{
	  			a.add(anOption,null);
		  		}
		  		catch(ex){
		  			a.add(anOption); // IE only
		  		}
			}
			z.appendChild(a);
		}
	}else if(params['type'] == "CheckBox"){
		if(params['defaultValue'] != null && (params['defaultValue'] == "true")){
			z.innerHTML = "<input type='checkbox' name='parameterValues' checked='checked' value='true' />";
		}
		else
			z.innerHTML = "<input type='checkbox' name='parameterValues' value='false' />";
	}
}

function AddParamRow(){
	var e = document.getElementById('parameterTable');
	var count = e.rows.length - 1;
	var x = e.insertRow(count);
	var y = x.insertCell(0);
	var z = x.insertCell(1);
	var w = x.insertCell(2);
	y.innerHTML = "<input type='text' name='parameterNames' value='' />";
	z.innerHTML = "<input type='text' name='parameterValues' value='' />";
	w.innerHTML = "<input type='button' value='X' onclick='deleteTR(this.parentNode.parentNode);'/>";
}

function deleteTR(e){
	var n = e.rowIndex;
	e.parentNode.deleteRow(n);
}

function initForm(){
	document.CaptionsForm.reset();
	var x = document.getElementById('cronTestResult');
	x.style.background = "none";
	x.innerHTML = "";
	var f = document.getElementById('parameterTable');
	var count = f.rows.length;
	while(count > 2){
		f.deleteRow(count - 2);
		count--;
	}
	document.getElementById('otherTR').disabled = false;
	document.getElementById('otherTR').value = '';
	var e = document.CaptionsForm.className.options[document.CaptionsForm.className.selectedIndex];
	e.value = '';
}

function onTestCron()
{
	var e = document.getElementById('cronTestResult');
    e.style.background = "#FF8822";
    var expression = document.getElementById("cronPanel").innerHTML;
    expression = encodeURIComponent(expression);
    var resultsDiv = document.getElementById("cronTestResult");
	var url = YAHOO.transbit.tbits.contextPath + "all-job-actions?action=testcron&expression=" + expression;
	var response = executeHttpRequest("get", url, null);
	resultsDiv.innerHTML = response;
}

function OnClose(){
	var e = document.getElementById('addJobPage');
	e.style.display = "none";
	initForm();
}
	
function onSubmit(){
	var formObject = document.getElementById('CaptionsForm');
	if(formObject.parameterValues)
	{
		var n = formObject.parameterValues.length - 1;
		while(n >= 0){
			var e = formObject.parameterValues[n];
			if(e.type == "checkbox"){
				if(e.checked == false){
					e.checked = "true";
					e.value = false;
				}else e.value = true;
			}
			n--;
		}
	}
//	else
//		formObject.parameterValues = [];
	
	var n = formObject.className.selectedIndex;
	var jobClass = formObject.className.options[n].value;
	if(jobClass == null || jobClass == '') 
		formObject.className.options[n].value = formObject.otherJobClass.value;
	
	formObject.cronExpression.value = document.getElementById("cronPanel").innerHTML;
	
  YAHOO.util.Connect.setForm(formObject); 
  var url = YAHOO.transbit.tbits.contextPath + 'all-job-actions?action=save-job'; 
  var callback = 
	{ 
	  success: function(o) 
	  {	
		alert( o.responseText ) ;
        location.reload(true);
      }, 
	  failure: function(o){    	 		
    	alert( "Adding job failed." + "\nCause : " + decodeURI(o.statusText).replace('/', "" ) ) ;   
    /*
    	var num = formObject.parameterValues.length - 1;
    	alert( "num = " + num ) ;
    		while(num >= 0){	
    			
    			var e1 = formObject.parameterValues[num];
    			
    			
    			if(e1.type == "checkbox"){
    				alert( "check box = " + e1 + "\n checked = " + e1.checked + "\n e1.value = " + e1.value);
    				if(e1.value == false){
    					e1.checked = "false";    					
    				}else e1.checked = true;
    			}   			
    			num--;
    		}
    	*/
      }
	}
  
    var cObj = YAHOO.util.Connect.asyncRequest('GET', url, callback); 
}

function CreateJob(){
	document.CaptionsForm.subaction.value = "create";
	var e = document.getElementById('addJobPage');
	e.style.display = "block";
	AddParamRow();
	AddParamRow();
	AddParamRow();
	AddParamRow();
}

function EditJob()
{
	 if(YAHOO.tbits.ListJob.selectedJob == "" )
	  {
	    alert("Please select the job");
	  }
 else
     {
	 	document.CaptionsForm.subaction.value = "edit"; 
	 	var url1 = YAHOO.transbit.tbits.contextPath + "all-job-actions?action=create-job&jobName="+YAHOO.tbits.ListJob.selectedJob+
	          "&jobGroup="+YAHOO.tbits.ListJob.selectedJobGroup;  
	     
	     var callback = 
			{ 
			  success: function(o) {
	    	 	var values = YAHOO.lang.JSON.parse(o.responseText);
	    	 	document.CaptionsForm.preJobName.value = values[0];
	    	 	document.CaptionsForm.preJobGroup.value = values[1];
		        document.CaptionsForm.jobName.value = values[0];
	    	 	document.CaptionsForm.jobGroup.value = values[1];
	    	 	document.CaptionsForm.className.value = values[2];
	    	 	document.CaptionsForm.description.value = values[3];
	    	 	document.CaptionsForm.cronExpression.value = values[5];
	    	 	document.CaptionsForm.otherJobClass.disabled = true;
	    	 	
	    	 	var jobText = document.CaptionsForm.className.options[document.CaptionsForm.className.selectedIndex].text;
	    		var jobClass = document.CaptionsForm.className.options[document.CaptionsForm.className.selectedIndex].value;
	    		
	    		var arr = [];
	    		arr.push(jobText);
	    		arr.push(jobClass);
	    		var jsonarr = YAHOO.lang.JSON.stringify(arr);
	    		var url = YAHOO.transbit.tbits.contextPath + 'all-job-actions?action=get-job-params&job-class=' + jsonarr;
	    	 	
	    	 	var keys = [];
	    	 	for(var i in values[4])
	    	 		keys.push(i);
	    	 	jsonarr = YAHOO.lang.JSON.stringify(keys);
	    	 	url += "&job-params=" + jsonarr;
	    	 	
	    	 	var callback2 = 
		 			{ 
	    	 			success: function(o) {
	    	 				var pre_values = YAHOO.lang.JSON.parse(o.responseText);
	    	 				var new_values = values[4];
	    	 				var count = 0;
	    	 				for(var i in pre_values){
	    	 					if(typeof(pre_values[i]) == "function")continue;
	    	 					if(values[4][pre_values[i]['name']] != null){
	    	 						pre_values[i]['defaultValue'] = values[4][pre_values[i]['name']];
	    	 						delete values[4][pre_values[i]['name']];
	    	 					}
	    	 					AddFilledParamRow(pre_values[i]);
	    	 					count++;
	    	 				}
	    	 				
	    	 				var newCount = count;
				    	 	for(var i in values[4]){
				    	 		if(typeof(values[4][i]) == "function")continue;
				    	 		AddParamRow();
				    	 		newCount++;
				    	 	}
				    	 	var n = count;
				    	 	for(var i in values[4]){
				    	 		if(typeof(values[4][i]) == "function")continue;
				    	 		if(newCount > 1){
					    	 		document.CaptionsForm.parameterNames[n].value = i;
					    	 		document.CaptionsForm.parameterValues[n].value = values[4][i];
					    	 		n++;
				    	 		}else{
				    	 			document.CaptionsForm.parameterNames.value = i;
					    	 		document.CaptionsForm.parameterValues.value = values[4][i];
				    	 		}
				    	 	}
		    	 		}, 
		    	 		failure: function(o) {alert(o.statusText);}
		 			}
	    	 	var cObj2 = YAHOO.util.Connect.asyncRequest('GET', url, callback2);
		      }, 
			  failure: function(o) {alert(o.statusText);}
			} 
		 var cObj = YAHOO.util.Connect.asyncRequest('GET', url1, callback);
		 
	     var e = document.getElementById('addJobPage');
		 e.style.display = "block"; 
	 }
}
	
function DeleteJob()
{
     if(YAHOO.tbits.ListJob.selectedJob == "")
	  {
	  alert("please select the job");
	  } 
	 else
	   {
         if (confirm("Are you sure you want to delete this job: \"" + YAHOO.tbits.ListJob.selectedJob + "\"?"))
            {
               var Query =  "?action=delete-job&jobName="+YAHOO.tbits.ListJob.selectedJob+
	                  "&jobGroup="+YAHOO.tbits.ListJob.selectedJobGroup;
  	           var resp = executeHttpRequest ("GET",YAHOO.transbit.tbits.contextPath + "all-job-actions" + Query ,false);
		       location.reload(true);
               if (eval (resp) == true) 
		           {	
				   this.deleteRow(target);						
				   }
		  }
    }	
}	

function ExecuteJob()
 {	 
    if(YAHOO.tbits.ListJob.selectedJob == "")
	{
	  alert("please select the job");
	}
    else
      {
        var Query =   "?action=execute-job&jobName="+YAHOO.tbits.ListJob.selectedJob +
	                  "&jobGroup="+YAHOO.tbits.ListJob.selectedJobGroup; 
					  
					  //alert(YAHOO.tbits.ListJob.selectedJob);
        var resp = executeHttpRequest ("GET",YAHOO.transbit.tbits.contextPath + "all-job-actions" + Query ,false);
	     alert("job Executed.");
              if (eval (resp) == true) 
		            {							
					 alert("job Executed.");											
					}
	 }				
 }
