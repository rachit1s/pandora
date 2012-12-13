
function getUserReportTableData(report_table_url){
	var rTable = executeHttpRequest("POST", report_table_url , false);
	return YAHOO.lang.JSON.parse(rTable);	
}

//Not required unless we decided to revert back to use GroupedDataTable
function callbackFunc(a,b){

	if(a.Group == b.Group)
	    {
          return 0;
		}
        return (a.Group < b.Group) ? -1 : 1;
	
}

function createUserReportsTable(nearestPath, reportUserId, userReportsData){
	 
	 //userReportsData.sort(callbackFunc);
	 
	 var myColumnDefs = [
	 					 {key:"report_id",label:"Report Id", sortable:true},						 
						 {key:"name", label:"Name", sortable:true},
						 {key:"filename",label:"Filename", sortable:true},
						 {key:"description", label:"Description", sortable:true},
						 {key:"Group",label:"Group", sortable:true},
						// {key:"urlParamPart",label:"urlParamPart", sortable:true},
						 
						 {key:"view", label:"View", sortable:true, formatter:YAHOO.widget.DataTable.formatButton}];		
		
        this.myDataSource = new YAHOO.util.DataSource(userReportsData);
        this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.myDataSource.responseSchema = {
            fields: ["report_id","name","filename","description","Group","urlParamPart","view"]
        };		
		
		
        /*this.myDataTable = new YAHOO.widget.GroupedDataTable("userTable",
                myColumnDefs, this.myDataSource,{groupBy:"Group"}); */
        this.myDataTable = new YAHOO.widget.DataTable("userTable",
                myColumnDefs, this.myDataSource);
		
	    
		
		this.myDataTable.subscribe("dropdownChangeEvent", function(oArgs){
            var elDropdown = oArgs.target;
            var oRecord = this.getRecord(elDropdown);
            oRecord.setData("format", elDropdown.options[elDropdown.selectedIndex].value);
        });
		
		this.myDataTable.showColumn("filename");
		this.myDataTable.hideColumn("filename");
		//this.myDataTable.showColumn("report_id");
		//this.myDataTable.hideColumn("report_id");
		
		this.myDataTable.subscribe("buttonClickEvent", function(oArgs){			
            var oRecord = this.getRecord(oArgs.target);
			var column = this.getColumn(oArgs.target);
						
			if (column.key == "view"){
				var report_abs_url = nearestPath + "reports/frameset?__report=" + oRecord.getData("filename") + "&user_id=" + reportUserId 
					+ oRecord.getData("urlParamPart");
				window.open(report_abs_url,'_blank','left=20, top=20, height=600, width=900,toolbar=yes, location=yes,status=yes, resize=yes, menubar=yes, scrollbars=yes,copyhistory=yes');
			}
		});
}
