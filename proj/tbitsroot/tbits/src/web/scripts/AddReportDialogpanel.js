YAHOO.namespace("tbits.reports");
var tree;
var nodes = [];
var nodeIndex;
				
function OnClose(){
		var e = document.getElementById('addReportsPage');
		e.style.display = "none";
		document.getElementById('is_private_table').style.display = "none";
		document.addReportForm.reset();
}

function openAddReportWizard(){
	document.addReportForm.requestType.value = "add";
	var e = document.getElementById('addReportsPage');
	e.style.display = "block";
}

function EditReport()
{ 
    
	   if(YAHOO.tbits.reports.selectedReportid == "")
	   {
	   alert("Please select the Report");
	   }
	   else 
	   {
		    var callback = 
			{ 
			  success: function(o) {
		    	var values = YAHOO.lang.JSON.parse(o.responseText);
	    	 	document.addReportForm.elements['report-name'].value = values[0];
				document.addReportForm.elements['group-name'].value = values[8];
	    	 	document.addReportForm.elements['report-description'].value = values[1];
	    	 	document.getElementById('old-file-name').innerHTML = values[2];
	    	 	selectedBARoles = values[3];
	    	 	treeInit();
	    	 	document.getElementById('includeUsers').innerHTML = values[4];
	    	 	document.getElementById('excludeUsers').innerHTML = values[5];
	    	 	
	    	 	if(values[6] == "true"){
	    	 		document.addReportForm.elements['is_enable'].checked = true;
	    	 	}
	    	 	
	    	 	if(values[7] == "true"){
	    	 		document.addReportForm.elements['is_private'].checked = true;
	    	 		document.getElementById('is_private_table').style.display = "block";
	    	 	}
		      }, 
			  failure: function(o) {alert(o.statusText);}
			} 
		    
		    var url1 = YAHOO.transbit.tbits.contextPath + "addreportwizard?requestType=edit&reportId="+YAHOO.tbits.reports.selectedReportid;
		    var cObj = YAHOO.util.Connect.asyncRequest('GET', url1, callback);
		    
		    document.addReportForm.requestType.value = "edit";
		    document.addReportForm.reportId.value = YAHOO.tbits.reports.selectedReportid;
			var e = document.getElementById('addReportsPage');
			e.style.display = "block";
	   }	
 }
	
function ScheduleReport()
{
  if(YAHOO.tbits.reports.selectedName == "")
	    {
		alert("Please select the Report");
	    }
	 else
    {
	   var url2 = YAHOO.transbit.tbits.contextPath + "reportcirculation?&actionType=edit&reportName="+YAHOO.tbits.reports.selectedName+"&reportfile="+YAHOO.tbits.reports.selectedFilename;
	         // window.open(url2,'_blank','left=20, top=20, height=600, width=900,toolbar=yes, location=yes,status=yes,                       // resize=yes, menubar=yes, scrollbars=yes,copyhistory=yes');
		  //showReportPanel(null, url2);	 
	   }
}	   
	 
function SaveReport()

{   
  if(YAHOO.tbits.reports.selectedReportid == "" )
	  {
	   alert("Please select the Report");
	  }
	else	
	  {	
		 var query = "?action=update&reportId=" + YAHOO.tbits.reports.selectedReportid 
			+ "&reportName=" + YAHOO.tbits.reports.selectedName + "&description=" + YAHOO.tbits.reports.selectedDescription
			+ "&fileName=" + YAHOO.tbits.reports.selectedFilename + "&isPrivate=" + YAHOO.tbits.reports.selectedIsprivate 
			+ "&isEnabled=" + YAHOO.tbits.reports.selectedEnabled +"&group="+YAHOO.tbits.reports.selectedGroup;
		 var resp = executeHttpRequest ("GET",YAHOO.transbit.tbits.contextPath + "reportstablemodifier" + query ,false);	
		  if (eval (resp) == true) 
		       {							
			   alert ("Saved");
			   location.reload(true);						
			    }
		   else {
			    alert ("Could not save");
			    } 
		}			
				 
}   


function DeleteReport()
{   
	if(YAHOO.tbits.reports.selectedReportid == "")
	{
	 alert("Please select the Report");
	}
	else    
	{
	    if (confirm("Are you sure you want to delete the report: \"" +  YAHOO.tbits.reports.selectedName + "\"?")) 
	     {
	         var query = "?action=delete&reportId=" + YAHOO.tbits.reports.selectedReportid;
	         var resp = executeHttpRequest ("GET",YAHOO.transbit.tbits.contextPath + "reportstablemodifier" + query ,false);
		     location.reload(true); 	
	            if (eval (resp) == true) 
		        {							
		          this.deleteRow(target);													
		        }
	     }
	}	 
		 
}	
	
function treeInit() {						
	buildRandomTaskNodeTree();
}							
								
YAHOO.util.Event.on("getchecked", "click", function(e) {							
	YAHOO.util.Event.preventDefault(e);
});
								
//Function  creates the tree and 
//builds between 3 and 7 children of the root node:
function buildRandomTaskNodeTree() {								
	//instantiate the tree:
	tree = new YAHOO.widget.TreeView("treeDiv1");
	for (var i = 0; i < ba.length ; i++){
		var isSelected = false;								
		if (ba[i].roles != null){	
			var bizArea = ba[i].baName;
			selectedRoles = selectedBARoles[bizArea];
			var tmpNode = new YAHOO.widget.TaskNode(bizArea, tree.getRoot(), false);
			buildRandomTaskBranch(tmpNode, ba[i].roles.split(","), selectedRoles);
		}
	}
tree.draw();
}
								
//var callback = null;
								
function buildRandomTaskBranch (node, rolesList, selectedRoles) {
	var selected = false;
	for (var i = 0; i < rolesList.length; i++) {
		if ((selectedRoles !== "undefined") && (selectedRoles != null)){				
			for (var k=0; k<selectedRoles.length; k++){
				if (selectedRoles[k] == rolesList[i]){
					selected = true; 
					break;
				}
				else
					continue;	
			}
		}
		var tmpNode = new YAHOO.widget.TaskNode(rolesList[i], node, false, selected);
		selected = false;
	}
}
								
function onCheckClick(node) {
	/*YAHOO.log(node.label + " check was clicked, new state: " + 
	node.checkState, "info", "example");*/
}
								
function checkAll() {
	var topNodes = tree.getRoot().children;
	for(var i=0; i<topNodes.length; ++i) {
		topNodes[i].check();
	}
}
								
function uncheckAll() {
	var topNodes = tree.getRoot().children;
	for(var i=0; i<topNodes.length; ++i) {
		topNodes[i].uncheck();
	}
}
								
function onLabelClick(node) {
   new YAHOO.widget.TaskNode("new", node, false);
   node.refresh();
   return false;
}
															
// Gets the labels of all of the fully checked nodes
// Could be updated to only return checked leaf nodes by evaluating
// the children collection first.
function getCheckedNodes(nodes) {
	nodes = nodes||tree.getRoot().children;
	checkedNodes = [];
		
	for(var i=0, l=nodes.length; i<l; i=i+1) {			
		var n = nodes[i];
		
		if (n.checkState > 0) { // if we were interested in the nodes that have some but not all children checked
		//if (n.checkState === 2) {	//if interested in the nodes that have all children checked			
			checkedNodes.push(n.label); // just using label for simplicity				
		}
								
		if (n.hasChildren()) {		
			checkedNodes = checkedNodes.concat(getCheckedNodes(n.children));				
		}			
	}									
	return checkedNodes;
}
				
YAHOO.util.Event.onDOMReady(treeInit);

function getBARoleMap(){
	var baRoles = getCheckedNodes (""); 
	var baRolesMap = [];
	for (var i=0; i<baRoles.length; i++){						
		if (isBA(baRoles[i])){
			var newObj = new Object();
			newObj.ba = baRoles[i];
			newObj.roles = getRoles (baRoles[i], baRoles, i);				
			baRolesMap.push(newObj);
		}
	}
	return baRolesMap;
}
	
function getRoles (sysPrefix, baRoles, index){
	var rList = [];	
	index = index + 1;	//should start after the current index
	for (var j=index; j < baRoles.length; j++){	
		if (isBA (baRoles[j])){
			return rList;							
		}
		else{
			//rList = (rList == "")? baRoles[j] : rList + "," + baRoles[j];
			rList.push (baRoles[j]);	
		}
	}			
	return rList;
}	

function isBA (name){
	for (var i=0 ; i<ba.length; i++){
		if (ba[i].baName == name)
			return true;
	}
	return false;
}	

function optionsList (elemId){
	var list = [];
	var selectEl = document.getElementById (elemId);
	if (selectEl.options.length > 0)	
		for (var i=0; i<selectEl.options.length; i++){
			var temp = selectEl.options[i].value; 
			if ((temp != null) || (temp != "") || (temp != " "))
				list.push (temp);
		}
	return list; 
}	

function addUser(textCtrlId, selectCtrlId){	
	var textCtrl = document.getElementById (textCtrlId);
	var userLogin = textCtrl.value;
	if (!isExistsInAnyUserList (userLogin)){
		if (userLogin != ""){
			var elem = document.createElement('Option');
			elem.value = userLogin;	
			elem.setAttribute('id',textCtrl.value);
			elem.innerHTML = textCtrl.value;				
			var selectCtrl= document.getElementById(selectCtrlId)
			selectCtrl.appendChild(elem);					
		}
	}	
	else{
		alert ("User: " + "\"" + userLogin + "\" already exists in include/exclude list");
	}			
	textCtrl.value = "";
}	

function isExistsInAnyUserList (loginName){
	return (isExists (loginName, "includeUsers") || isExists (loginName, "excludeUsers"));
}

function isExists (loginName, ctrlId){
	var selectEl = document.getElementById (ctrlId);		
	if (selectEl.options.length > 0){	
		for (var i=0; i < selectEl.options.length; i++){
			if (selectEl.options[i].value == loginName)
				return true;
			else 
				continue;
		} 
	}
	else{
		return false;
	}
}

function removeUser(selectCtrlId){
	var selectCtrl = document.getElementById (selectCtrlId);
	var optionElem = selectCtrl.options[selectCtrl.selectedIndex];
	selectCtrl.removeChild(optionElem);
}

function onIsPrivate(ctrlId){
	toggleElement(ctrlId);
	var isChecked = document.getElementById("is_private");
	isChecked.value = (isChecked.value == "") ? "on" : "";		
}

function toggleElement (ctrlId){
	var ctrl = document.getElementById(ctrlId);
	if (ctrl.style.display == 'none')
		ctrl.style.display = 'block'; 
	else 
		ctrl.style.display = 'none';
}

function revertToOldFile(){
	document.getElementById("isChangedId").value=false;
	toggleBrowse();
}

function toggleBrowse(){
	
}

function setChanged(){	
	if (document.getElementById("requestTypeId").value == "edit"){	
		document.getElementById("isChangedId").value=true;
	}		
}

function setAction(){
	var x = document.getElementById("selectedBARoleMap");
	var tempBARoleMap = getBARoleMap();
	var tempIncludeUsers = optionsList("includeUsers");
	var tempExcludeUsers = optionsList("excludeUsers");
	
	if ((tempBARoleMap.length<1) && (tempIncludeUsers<1) && (tempExcludeUsers<1))
		document.getElementById("is_private_id").value = "";
		
	x.value = JSON.stringify(tempBARoleMap); 
	document.getElementById("includeUsersList").value = JSON.stringify(tempIncludeUsers);
	document.getElementById("excludeUsersList").value = JSON.stringify(tempExcludeUsers);
	document.addReportForm.action = YAHOO.transbit.tbits.contextPath + "addreport";
	document.addReportForm.submit();
}	


/*function showReportPanel(e, page)
{
	var panel = YAHOO.tbits.reports.dialogPanel;
	if(!panel.visible)
	{
		var frame = document.getElementById("AddReportPage");
		frame.src = page;
		panel.cfg.queueProperty("visible", true);
		panel.render();
		panel.center();
	}
	else
	{
		panel.close();
	}
}
function initPanel() {
	// Setup constants
	
	// QUIRKS FLAG, FOR BOX MODEL
	var IE_QUIRKS = (YAHOO.env.ua.ie && document.compatMode == "BackCompat");
	
	// UNDERLAY/IFRAME SYNC REQUIRED
	var IE_SYNC = (YAHOO.env.ua.ie == 6 || (YAHOO.env.ua.ie == 7 && IE_QUIRKS));
	
	// PADDING USED FOR BODY ELEMENT (Hardcoded for example)
	var PANEL_BODY_PADDING = (10*2) // 10px top/bottom padding applied to Panel body element. The top/bottom border width is 0
	
	// Create a panel Instance, from the 'resizablepanel' DIV standard module markup
	YAHOO.tbits.reports.dialogPanel = new YAHOO.widget.Panel('resizablepanel', {
	    draggable: true,
	    width: '500px',
	    context: ["page", "tl", "bl"],
	    visible: false,
	    modal: true
	});
	//panel.render();
	
	// Create Resize instance, binding it to the 'resizablepanel' DIV 
	var resize = new YAHOO.util.Resize('resizablepanel', {
	    handles: ['br'],
	    autoRatio: false,
	    minWidth: 342,
	    minHeight: 337,
	    status: true
	});
	var panel = YAHOO.tbits.reports.dialogPanel;
	// Setup resize handler to update the size of the Panel's body element
	// whenever the size of the 'resizablepanel' DIV changes
	resize.on('resize', function(args) {
	    //alert('resizing');
	    var panelHeight = args.height;
	
	    var headerHeight = this.header.offsetHeight; // Content + Padding + Border
	    var footerHeight = this.footer.offsetHeight; // Content + Padding + Border
	
	    var bodyHeight = (panelHeight - headerHeight - footerHeight);
	    var bodyContentHeight = (IE_QUIRKS) ? bodyHeight : bodyHeight - PANEL_BODY_PADDING;
	
	    YAHOO.util.Dom.setStyle(this.body, 'height', bodyContentHeight + 'px');
	
	    if (IE_SYNC) {
	
	        // Keep the underlay and iframe size in sync.
	
	        // You could also set the width property, to achieve the 
	        // same results, if you wanted to keep the panel's internal
	        // width property in sync with the DOM width. 
	
	        this.sizeUnderlay();
	
	        // Syncing the iframe can be expensive. Disable iframe if you
	        // don't need it.
	
	        this.syncIframe();
	    }
	}, panel, true);
	
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },  							
										  { fn: panel.hide,
											scope:panel,
											correctScope:true }, "keyup" ); 
											// keyup is used here because Safari won't recognize the ESC
											// keydown event, which would normally be used by default
	
	panel.cfg.queueProperty("keylisteners", kl);
}
function showLetterPanel(e, page,divId)
{
	var lpanel = YAHOO.tbits.reports.letterDialogPanel;
	if(!lpanel.visible)
	{
		var letterFrame = document.getElementById(divId);		
		letterFrame.src = page;
		lpanel.cfg.queueProperty("visible", true);		
		lpanel.render();
		lpanel.center();
	}
	else
	{
		lpanel.close();
	}
}

function reloadLetterPanel (e,page,divId)
{
	var lpanel = YAHOO.tbits.reports.letterDialogPanel;
	var letterFrame = document.getElementById(divId);		
	letterFrame.src = page;	
}
function createLetterPanel()
{
	// QUIRKS FLAG, FOR BOX MODEL
	var IE_QUIRKS = (YAHOO.env.ua.ie && document.compatMode == "BackCompat");
	
	// UNDERLAY/IFRAME SYNC REQUIRED
	var IE_SYNC = (YAHOO.env.ua.ie == 6 || (YAHOO.env.ua.ie == 7 && IE_QUIRKS));
	
	// PADDING USED FOR BODY ELEMENT (Hardcoded for example)
	var PANEL_BODY_PADDING = (10*2) // 10px top/bottom padding applied to Panel body element. The top/bottom border width is 0
	

	YAHOO.tbits.reports.letterDialogPanel = new YAHOO.widget.Panel('letterpanel', { 
    	draggable: true, 
    	width: '900px',		
   		context: ["page","tl", "bl"] ,
		visible: false,
	    modal: true
	}); 
		
	var resizePanel = new YAHOO.util.Resize('letterpanel', { 
	      handles: ['br'], 
	      autoRatio: false, 
	      minWidth: 342, 
	      minHeight: 424, 
	      status: true 
	}); 
	
	resizePanel.on('resize', function(args) { 
	 
	    var panelHeight = args.height; 
	 
	    var headerHeight = this.header.offsetHeight; // Content + Padding + Border 
	    var footerHeight = this.footer.offsetHeight; // Content + Padding + Border 
	 
	    var bodyHeight = (panelHeight - headerHeight - footerHeight); 
	    var bodyContentHeight = (IE_QUIRKS) ? bodyHeight : bodyHeight - PANEL_BODY_PADDING; 
	 
	    YAHOO.util.Dom.setStyle(this.body, 'height', bodyContentHeight + 'px'); 
	 
	    if (IE_SYNC) { 	     
	        this.sizeUnderlay(); 
	        this.syncIframe(); 
	    } 
	}, YAHOO.tbits.reports.letterDialogPanel, true); 
	
	var kl2 = new YAHOO.util.KeyListener(document, { keys:27 },  							
										  { fn: YAHOO.tbits.reports.letterDialogPanel.hide,
											scope:YAHOO.tbits.reports.letterDialogPanel,
											correctScope:true }, "keyup" ); 
											// keyup is used here because Safari won't recognize the ESC
											// keydown event, which would normally be used by default
		
	YAHOO.tbits.reports.letterDialogPanel.cfg.queueProperty("keylisteners", kl2);	
	//kl2.enable();
}
*/

