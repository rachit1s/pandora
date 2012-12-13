function getDisplayGroupTableData(display_group_url){
	var rTable = executeHttpRequest("POST", display_group_url, false);
	return YAHOO.lang.JSON.parse(rTable);
}

function createDisplayGroupTable(nearestPath, sysPrefix, dGReportsData){
	var imgSrc = nearestPath + 'web/images/ckdeny.gif';
	var myColumnDefs = [{key:"id", label:"Id", sortable:true},
	    				{key:"name", label:"Name", sortable:true, editor:"textbox"},
						{key:"displayOrder", label:"Display Order", sortable:true, editor:"textbox"},
						{key:"is_active", label:"Active",formatter:"checkbox"},	
						{key:"save", label:"Save", formatter:YAHOO.widget.DataTable.formatButton},
						{key:"delete",label:"Delete", formatter:function(elCell) {
							elCell.innerHTML = "<img src=\"" + imgSrc + "\" title= \"delete row\"/>";
							elCell.style.cursor = "pointer";
						}}];							
	this.myDataSource = new YAHOO.util.DataSource(dGReportsData);
	this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	this.myDataSource.responseSchema = {
		fields: ["id","name","displayOrder","is_active","save"]
	};

	this.myDataTable = new YAHOO.widget.DataTable("displayGroupTable",
			myColumnDefs, this.myDataSource, {caption:""});
	
	this.myDataTable.subscribe("checkboxClickEvent", function(oArgs){	
		var elCheckbox = oArgs.target;
		var oRecord = this.getRecord(elCheckbox);
		var column = this.getColumn(elCheckbox);	
		if (column.key == "is_active")	
			oRecord.setData("is_active", elCheckbox.checked);	
	});

	this.myDataTable.subscribe("buttonClickEvent", function(oArgs){			
		var oRecord = this.getRecord(oArgs.target);
		var column = this.getColumn(oArgs.target);
		if (column.key == "save"){
			var query = "?actionType=save&displayGroupId="+ oRecord.getData ("id") 
						+ "&name="+ oRecord.getData ("name") 
						+ "&displayOrder=" + oRecord.getData ("displayOrder")
						+ "&isActive=" + oRecord.getData("is_active");
			var resp = executeHttpRequest ("GET", nearestPath + "displaygrouphandler/" + sysPrefix + query, false);	
			if (eval (resp) == true) {							
				alert ("Saved display group: " +  oRecord.getData ("name"));						
			}
			else{
				alert ("Could not save display group: " +  oRecord.getData ("name"));
			} 
		}
	});				
			
	this.myDataTable.subscribe("cellDblclickEvent",this.myDataTable.onEventShowCellEditor);
	this.myDataTable.subscribe("cellClickEvent",function(ev) {	
		var target = YAHOO.util.Event.getTarget(ev); 
		var column = this.getColumn(target);
		var arecord = this.getRecord(target);
		if (column.key == "delete") {
			if (confirm("Are you sure you want to delete the display group: \"" + arecord.getData ("name") + "\"?")) {
				var query = "?actionType=delete&name=" + arecord.getData ("name");
				var resp = executeHttpRequest ("GET",nearestPath + "displaygrouphandler/" + sysPrefix  + query ,false);							
				if (eval (resp) == true) {							
					this.deleteRow(target);													
				}
			}
		}
	});
}

function openAddDisplayGroup(nearestPath, sysPrefix)
{
	var url = nearestPath + "displaygrouphandler/" + sysPrefix + "?actionType=getAddPage";

	var callback = 
	{ 
		success: function(o) {
			var dGObj = YAHOO.lang.JSON.parse(o.responseText);
			document.DisplayGroupForm.actionType.value = dGObj.actionType;
			document.DisplayGroupForm.displayGroupId.value = dGObj.displayGroupId;
			document.DisplayGroupForm.name.value = dGObj.name;
			document.DisplayGroupForm.displayOrder.value = dGObj.displayOrder;
			document.DisplayGroupForm.isActive.value = dGObj.isActive;
		}, 
		failure: function(o) {alert(o.statusText);}
	} 
	var cObj = YAHOO.util.Connect.asyncRequest('GET', url, callback);

	var e = document.getElementById('addDisplayGroupPage');
	e.style.display = "block"; 
}

function dGWindowClose(){
	document.DisplayGroupForm.reset();
	var e = document.getElementById('addDisplayGroupPage');
	e.style.display = "none";
}

function onDGSubmit(nearestPath, sysPrefix){
	  var formObject = document.getElementById('DisplayGroupForm');
	  YAHOO.util.Connect.setForm(formObject); 
	  var url = nearestPath + 'displaygrouphandler/' + sysPrefix;
	  var callback = 
		{ 
		  success: function(o) {
	        alert("Display group created.");
	        location.reload(true);
	      }, 
		  failure: function(o) {alert(o.statusText);}
		} 
	  var cObj = YAHOO.util.Connect.asyncRequest('GET', url, callback);
}


function onClickIsActive(){
	var isChecked = document.getElementById("is_active");
	isChecked.value = (isChecked.value == "off")? "on" : "off";
}