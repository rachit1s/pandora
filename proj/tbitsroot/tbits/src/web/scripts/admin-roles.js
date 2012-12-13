function initRolePanel(){
    YAHOO.namespace('tbits.rolePanel');
    
    YAHOO.tbits.rolePanel.dialog = new YAHOO.widget.Dialog('test', {
        height: '150px',
        width: '500px',
        visible: false,
        fixedcenter: false,
        position: 'absolute'
    });
    YAHOO.tbits.rolePanel.dialog.setHeader('Add Role');
    YAHOO.tbits.rolePanel.dialog.setBody(
		    '<form name="addRoleForm" id="addRoleForm">' + 
		    	'<input type="hidden" id="actionTypeId" name="actionType" value="create"/>'+ 
		    	'<table><tr>' + 
		    		'<td>Role Name:</td><td>' + 
		    			'<input type="text" id="roleNameId" name="roleName" style="width:350px;" value=""></input>' + 
		    		'</td></tr>' + 
		    		'<tr><td>Description: </td>' + 
		    		'<td><input type="text" id="descriptionId" name="description" style="width:350px" value=""></input></td>' +					    		
		    	'</tr></table>'+
		    	'<input id="saveRole" name="Save" type="button" value="Save Role" onClick="onRoleSubmit(YAHOO.transbit.tbits.contextPath, sysPrefix)"/></form>');
  
    YAHOO.tbits.rolePanel.dialog.render(document.body);
}

function openRoleDialog(){
	YAHOO.tbits.rolePanel.dialog.show();
}

function onRoleSubmit(nearestPath, sysPrefix){
	var formObject = document.getElementById('addRoleForm');
	YAHOO.util.Connect.setForm(formObject); 
	var url = nearestPath + 'rolehandler/' + sysPrefix;
	var callback = 
	{ 
	  success: function(o) {
		alert(o.responseText);
        YAHOO.tbits.rolePanel.dialog.hide();
        window.location.reload();
      }, 
	  failure: function(o) {
    	  alert(o.responseText);
    	  YAHOO.tbits.rolePanel.dialog.hide();
          window.location.reload();}
	} 
	var cObj = YAHOO.util.Connect.asyncRequest('GET', url, callback);	  
}

function deleteRole(nearestPath, sysPrefix){
	var roleElement = document.getElementById("roles_list");
	var roleName = roleElement.value;
	if (confirm("Do you want to delete the role: \"" + roleName + "\" permanently.")){
		url = nearestPath + "rolehandler/" + sysPrefix +"?actionType=delete"
			+ "&roleName=" + roleName;
		var response = executeHttpRequest('GET', url, true);
		alert(response);
		//Reload after deleting the role
		roleElement.options[0].selected=true;
		onPropertyChangeRoles();
	}
}
