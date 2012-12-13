// JScript source code for admin
 
 var sysPrefix;
 var userPage;
 var formChanged = false;

//This method extracts sysPrefix variable for the properties,roles and roles page. 
function onLoadBody()
{
//	var selIndex = document.getElementById("sys_ids").options.selectedIndex;
//	var baName = new String(document.getElementById("sys_ids").options[selIndex].text);
//	var index = baName.indexOf("[");
//	sysPrefix = baName.substring(index + 1, baName.lastIndexOf("]"));
	sysPrefix = window.parent.getSysPrefix();
}

//This method extracts sysPrefix variable for the fields page.
function onLoadBodyFields()
{
	onLoadBody();
}

//This method extracts sysPrefix variable for the users page.
function onLoadBodyUsers()
{
	onLoadBody();
	userPage = "categories";
}

//This method redirects to the properties page in the selected business area.
function onPropertyChangeSysIdProperties()
{
	onPropertyChangeSysId("admin-properties");
}

//This method redirects to the fields page in the selected business area.
function onPropertyChangeSysIdFields()
{
	onPropertyChangeSysId("admin-fields");
}

//This method redirects to the roles page in the selected business area.
function onPropertyChangeSysIdRoles()
{
	onPropertyChangeSysId("admin-roles");
}

//This method redirects to the users page in the selected business area.
function onPropertyChangeSysIdUsers()
{
	onPropertyChangeSysId("admin-users");
}

//This method redirects to the manage type fields page in the selected business area.
function onPropertyChangeSysIdManage()
{
	onPropertyChangeSysId("admin-manage");
}

function onPropertyChangeSysIdCaptions()
{
	onPropertyChangeSysId("admin-captions");
}

function onPropertyChangeSysIdApp()
{
	onPropertyChangeSysId("admin-app");
}


//This method redirects to the manage type fields page in the selected business area.
function onPropertyChangeSysId(redirectionTab)
{
	var selIndex = document.getElementById("sys_ids").options.selectedIndex;
	var baName = new String(document.getElementById("sys_ids").options[selIndex].text);
	var index = baName.indexOf("[");
	sysPrefix = baName.substring(index + 1, baName.lastIndexOf("]"));
	var redirection = getRelativePath(window.location, redirectionTab) + "/" + sysPrefix;
	window.location = redirection;
}


function frmChanged()
{
	formChanged = true;
}

/***************************************************************************************
                          Methods for admin-properties page.
 ***************************************************************************************/
 
//This function controls when to display the due_date text box.
function onChangeDueDateOptions()
{
    var selIndex = document.getElementById("due_by_field").options.selectedIndex;
    var currentOption = new String(document.getElementById("due_by_field").options[selIndex].value);
    formChanged = true;
    
    if(currentOption == "disabled")
    {
        document.getElementById("default_due_date").style.cssText = "DISPLAY: none";
        document.getElementById("is_time_enabled").style.cssText = "DISPLAY: none";
        document.getElementById("is_time_enabled_label").style.cssText = "DISPLAY: none";
    }
    if(currentOption == "allownull")
    {
        document.getElementById("default_due_date").style.cssText = "";
        document.getElementById("is_time_enabled").style.cssText = "";
        document.getElementById("is_time_enabled_label").style.cssText = "";
        dueDateValue = document.getElementById("default_due_date").value;
        if(trim(dueDateValue) == "")
        {
		   document.getElementById("default_due_date").value = "0";
		}
        //document.getElementById("default_due_date").value = "0";
    }
    if(currentOption == "mandatory")
    {
        document.getElementById("default_due_date").style.cssText = "";
        dueDateValue = document.getElementById("default_due_date").value;
        document.getElementById("is_time_enabled").style.cssText = "";
        document.getElementById("is_time_enabled_label").style.cssText = "";
        if(dueDateValue != 0)
        {
          document.getElementById("default_due_date").value = dueDateValue;
		}
		else
		{
		   document.getElementById("default_due_date").value = "90";
		}
        //document.getElementById("default_due_date").value = "90";
    }
}

function onClickEnableTime()
{
   if(document.getElementById("is_time_enabled").checked == false)
   {
      document.getElementById("default_due_date").value =  "0";
   }
}

//This function controls when to enable/disable the notify-request check boxes.
function onClickRequestEmail()
{
    if(document.getElementById("send_email_request").checked == false)
    {
        document.getElementById("notify_addRequest").checked = false;
        document.getElementById("notify_addRequest").disabled = true;
        document.getElementById("notify_loggers_addRequest").checked = false;
        document.getElementById("notify_loggers_addRequest").disabled = true;
        document.getElementById("notify_addAction").checked = false;
        document.getElementById("notify_addAction").disabled = true;
        document.getElementById("notify_loggers_addAction").checked = false;
        document.getElementById("notify_loggers_addAction").disabled = true;
        document.getElementById("send_email_action").checked = false;
        document.getElementById("send_email_action").disabled = true;
    }
    
    if(document.getElementById("send_email_request").checked == true)
    {
       document.getElementById("notify_addRequest").disabled = false;
       document.getElementById("notify_loggers_addRequest").disabled = false;
       document.getElementById("notify_addRequest").disabled = false;
       document.getElementById("notify_loggers_addRequest").disabled = false;
       document.getElementById("send_email_action").disabled = false;
       document.getElementById("notify_addAction").disabled = false;
       document.getElementById("notify_loggers_addAction").disabled = false;
    }
}

//This function controls when to enable/disable notify-action checkboxes.
function onClickActionEmail()
{
    /* if(document.getElementById("send_email_action").checked == false)
    {
        document.getElementById("notify_addAction").checked = false;
        document.getElementById("notify_addAction").disabled = true;
        document.getElementById("notify_loggers_addAction").checked = false;
        document.getElementById("notify_loggers_addAction").disabled = true;
    }
    
    if(document.getElementById("send_email_action").checked == true)
    {
       document.getElementById("notify_addAction").disabled = false;
       document.getElementById("notify_loggers_addAction").disabled = false;
    }*/
}

//This function performs some basic validations before submitting the admin-properties page.
function onSubmitProperties()
{
	var name = document.getElementById("ba_name").value;
	var displayName = document.getElementById("display_name").value;
	var prefix = document.getElementById("request_prefix").value;
	var email = document.getElementById("email").value;
	var due_time = document.getElementById("default_due_date");
	if( document.getElementById("ba_name").disabled == true)
	{
		document.getElementById("action").value = "onlySevLists";
	    document.getElementById("adminPropertiesForm").submit();
	}
	if ((document.getElementById("ba_name").disabled == false) && 
		(trim(name) != "") && (trim(displayName) != "") && 
		(trim(prefix) != "") && (trim(email) != "") &&
		((due_time == null) || (due_time.value != "")) && (!trim(prefix).match(/\W/)))
	{
	    document.getElementById("adminPropertiesForm").submit();
	}
	else
	{
	    if(trim(name) == "")
           alert(EMPTY_BUSINESS_AREA_NAME);
        if(trim(displayName) == "")
           alert(EMPTY_BUSINESS_AREA_DISPLAY_NAME);
        if(trim(prefix) == "")
           alert(EMPTY_BUSINESS_AREA_PREFIX);
        if(trim(prefix).match(/\W/))
           alert(INVALID_BUSINESS_AREA_PREFIX);
        if(trim(email) == "")
           alert(EMPTY_BUSINESS_AREA_EMAIL);
        if(trim(due_time.value) == "")
           alert(EMPTY_DUE_TIME);
	}
	//setTimeout(window.location.reload(), 100);
}

function createBusinessArea()
{
    var nearestPath = document.getElementById("nearestPath").value;
	var location = YAHOO.transbit.tbits.contextPath + "admin-util/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&action=add-ba&now=" + getCurrentTime();
	var features = "status:no; dialogWidth:400px; dialogHeight:150px;resizable: yes";
    var child = new Object();
    child.sysPrefix = sysPrefix;
    child.returnValue = false;
    if(window.showModalDialog)
	{
	 window.showModalDialog(location, child, features);
	}
	else
	 {
	    window.open(location,child,"status:no; dialogWidth:400px; dialogHeight:150px;resizable: yes;modal: yes");
	 }
    if (child.returnValue == true)
    {
        window.location = window.location;
    }
    return;
}

//This function makes an xmlhttp call after clicking the submit button in the dialog
// box for adding a type.
function onSubmitAddBusinessArea()
{
   	var newBAName = document.getElementById("new_ba_name").value; 
   	var newBAPrefix = document.getElementById("new_ba_prefix").value;
   	var sysPrefix = obj.sysPrefix;
	var url = getRelativePath(window.location, "admin-properties/") + sysPrefix +
	 		  "?new_ba=true" +
			  "&new_ba_name=" + encodeURIComponent(newBAName) +
		      "&new_ba_prefix=" + encodeURIComponent(newBAPrefix);
		             
	var output = executeHttpRequest("POST", url, false);

	if(output.indexOf("Success") >= 0)
	{
		// Action Success.
		window.close();
		if (obj != null) obj.returnValue = true;
		return //prefix+output;
	}
	else 
	{
		alert(output);
		//window.close();
		if (obj != null) obj.returnValue = true;
		//return;
	}
}
 

/***************************************************************************************
                          Methods for admin-fields page.
 ***************************************************************************************/

//This method reloads the page on changing the field in the list box.
function onChangeField()
{
	var selIndex = document.getElementById("field_ids").options.selectedIndex;
	var fieldName = new String(document.getElementById("field_ids").options[selIndex].value);
	var redirection = getRelativePath(window.location, "admin-fields") + "/" + sysPrefix + "/" + encodeURIComponent(fieldName);

	window.location = redirection;
}

//This function displays the attributes of the newly selected type.
function onChangeTypes()
{
 	var selIndex1 = document.getElementById("field_ids").options.selectedIndex;
 	var selIndex2 = document.getElementById("field_types").options.selectedIndex;
	var fieldName = new String(document.getElementById("field_ids").options[selIndex1].value);
	var typeName = new String(document.getElementById("field_types").options[selIndex2].value);
	//var redirection = getRelativePath(window.location, "admin-fields") + "/" + sysPrefix + "/" + escape(fieldName);
	
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix +
		      "?newType=" + encodeURIComponent(typeName) + 
		      "&currentField=" + encodeURIComponent(fieldName) + 
		      "&date=" + (new Date());
    var output = executeHttpRequest("GET", url, false);
    output = trim(output);
    document.getElementById("typeReplacement").innerHTML = output;
}

//this function performs all the basic validations in the admin-fields page and submits the form data.
function onSubmitFields()
{
	//"Basic Validations"
	var displayName = document.getElementById("field_displayName").value;
	if(trim(displayName) == "")
	{
		alert(EMPTY_FIELD_DISPLAY_NAME);
		return false;
	}
	
	var fieldDescriptors = trim(document.getElementById("field_descriptors").value);
	if((fieldDescriptors != null) && (trim(fieldDescriptors).match(/ +/)))
	{
		alert(FIELD_DESCRIPTOR_SPACES);
		return false;
	}
	if(document.getElementById("field_description").disabled == false)
	{
		document.getElementById("action").value = "fieldsSuperAdmin";
	}
	document.getElementById("field_ids").disabled = false;
	document.getElementById("adminFieldsForm").submit();
}

//This function pops up a dialog box for adding extended-fields.
function addExtendedField()
{
    var nearestPath = document.getElementById("nearestPath").value;
	var location = YAHOO.transbit.tbits.contextPath + "admin-util/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&action=add-ext-field&now=" + getCurrentTime();
	 var field_name = document.all.field_ids.value;
     var features = "status:no; dialogWidth:400px; dialogHeight:145px;resizable: yes;modal: yes";
     var child = new Object();
     child.returnValue = false;
     child.fieldName = field_name;
     child.sysPrefix = sysPrefix;
     if(window.showModalDialog)
     {
        var returnValue = window.showModalDialog(location, child, features);
        if (child.returnValue == true)
        {
            window.location = window.location;
        }
     }
	 else
	 {
	    window.open(location,child,"status:no; dialogWidth:400px; dialogHeight:150px;resizable: yes;modal: yes");
	 }
     return;
}


//This function makes an xml-http call on clicking 'Submit' in add-extended-field dialog box.
function onSubmitAddExtendedField()
{
   	var newField = document.getElementById("new_field_name").value; 
    var newFieldType = document.getElementById("new_field_type").value;
	var sysPrefix = obj.sysPrefix;
	var fieldName = obj.fieldName;
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix + 
			  "?field_ids=" + encodeURIComponent(fieldName) +
		      "&new_field_name=" + encodeURIComponent(newField) +
			  "&new_field_data_type=" + encodeURIComponent(newFieldType);
			  		             
	var output = executeHttpRequest("POST", url, false);
	
	alert( output );
	window.close() ;
	return false ;
}

// This function deletes the selected extended-field provided there are no references to the field
// in actions table.
function deleteField()
{  
	var result = confirm("Do you wish to delete the extended field?");
	if (result == false)
	{
		return;
	}
	var selIndex = document.getElementById("field_ids").options.selectedIndex;
	var fieldName = new String(document.getElementById("field_ids").options[selIndex].value);
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix + "?field_ids="+encodeURIComponent(fieldName)
																+"&action=deleteField";
	var output = executeHttpRequest("POST", url, false);
	output = trim(output);
	alert(output);
//	var newFieldType = document.getElementById("field_datatypes").value;
//	if(newFieldType=="10")
//	{
//	  deleteDynamicRole(fieldName,sysPrefix);	
//	}
	
    if(output.indexOf("Sorry") < 0)
	{
       var redirection = getRelativePath(window.location, "admin-fields") + "/" + sysPrefix;
	   window.location = redirection;
	}
	
   //document.getElementById("action").value = "deleteField";
   //document.getElementById("adminFieldsForm").submit();
}

//This function opens up a dialog box for adding a type.
function addType()
{
    var nearestPath = document.getElementById("nearestPath").value;
	var location = YAHOO.transbit.tbits.contextPath + "admin-util/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&action=add-type&now=" + getCurrentTime();
	
    var field_name = document.getElementById("field_ids").value;
    // Set the features of the Modal dialog 
    var features = "status:no; dialogWidth:400px; dialogHeight:120px;resizable: yes";
    var child = new Object();
    child.returnValue = false;
    child.fieldName = field_name;
    child.sysPrefix = sysPrefix;
    
    if(window.showModalDialog)
    {
       var returnValue = window.showModalDialog(location, child, features);
       if (child.returnValue == true)
       {
           window.location = window.location;
       }
    }
	 else
	 {
	    window.open(location,child,"status:no; dialogWidth:400px; dialogHeight:150px;resizable: yes;modal: yes");
	 }
    return;
}

//This function makes an xmlhttp call after clicking the submit button in the dialog
// box for adding a type.
function onSubmitAddType()
{
   	var newType = document.getElementById("new_type_name").value; 
    var fieldName = obj.fieldName;
	var sysPrefix = obj.sysPrefix;
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix + 
			  "?field_ids=" + encodeURIComponent(fieldName) +
		      "&new_type_name=" + encodeURIComponent(newType);
	var output = executeHttpRequest("POST", url, false);
	var prefix = "<SELECT id='field_types' size='11' name='field_types' onchange='javascript:onChangeTypes()'>"

	if(output.indexOf(newType) >= 0)
	{
		if(isIE == true)
		{
		   // Action Success.
		   window.close();
		   if (obj != null) obj.returnValue = true;
		   return prefix + output;;
		}
		else
		{
		  this.opener.location = this.opener.location;
		  window.close();
		}
		// Action Success.
		//window.close();
		//if (obj != null) obj.returnValue = true;
		//return prefix+output;
	}
	else if(output.indexOf("exists") >= 0)
	{
		alert(output);
		//window.close();
		if (obj != null) obj.returnValue = true;
		//return;
	}
}

//This function deletes the selected type.
function deleteType()
{
	var result = confirm("Do you wish to delete the type?");
	if (result == false)
	{
		return;
	}
	var selIndex = document.getElementById("field_ids").options.selectedIndex;
	var fieldName = new String(document.getElementById("field_ids").options[selIndex].value);
	selIndex = document.getElementById("field_types").options.selectedIndex;
	var typeName = new String(document.getElementById("field_types").options[selIndex].value);
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix 
	    + "?field_ids=" + encodeURIComponent(fieldName) 
	    + "&field_types=" + encodeURIComponent(typeName)
        + "&action=typesDelete";
	var output = executeHttpRequest("POST", url, false);
	output = trim(output);
	alert(output);
	window.location = window.location;
    //document.getElementById("typeReplacement").innerHTML = output;

}

//This function makes an xml http call to post all the attributes of the types.
function onSubmitTypes()
{
	var displayName = document.getElementById("type_displayName").value;
	if(trim(displayName) == "")
	{
	     alert("The display name of the type cannot be empty");
	     return false;
	}
	var selIndex = document.getElementById("field_ids").options.selectedIndex;
	var fieldName = new String(document.getElementById("field_ids").options[selIndex].value);
	selIndex = document.getElementById("field_types").options.selectedIndex;
	var typeName = new String(document.getElementById("field_types").options[selIndex].value);
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix 
	    + "?field_ids=" + encodeURIComponent(fieldName) 
	    + "&field_types=" + encodeURIComponent(typeName)
		+ "&action=typesUpdate";
	var addSelected = document.getElementById("type_addSelected").checked;
	var searchSelected = document.getElementById("type_searchSelected").checked;
	var orderValuePairs = document.getElementById("orderValuePairs").value;
	url = url 
	        + "&type_displayName=" + encodeURIComponent(displayName)
	        + "&type_addSelected=" + encodeURIComponent(addSelected)
	        + "&type_searchSelected=" + encodeURIComponent(searchSelected)
	        + "&orderValuePairs=" + encodeURIComponent(orderValuePairs);
	
	var description = document.getElementById("type_description").value;
	if(description != null)
	{
		url = url + "&type_description=" + encodeURIComponent(description) + "&adminType=super";
	}
	var active = document.getElementById("type_active").checked;
	if(active != null)
	{
		url = url + "&type_active=" + active;
	}
	var isPrivate = document.getElementById("type_private").checked;
	if(isPrivate != null)
	{
		url = url + "&type_private=" + isPrivate;
	}
	var isFinal = document.getElementById("type_final").checked;
	if(isFinal != null)
	{
		url = url + "&type_final=" + isFinal;
	}
    var typeDescriptors = document.getElementById("type_descriptors").value;
    if(typeDescriptors != null)
    {
        url = url + "&type_descriptors=" + encodeURIComponent(typeDescriptors);
	}
			  		             
	var output = executeHttpRequest("POST", url, false);
}


//The function rearranges the type-order.
function onUpType()
{
	var ctrl = document.getElementById("field_types");
	var len = ctrl.options.length;
    
	var tempValue, tempText, tempSelected;
    
	for(var i = len-1; i > 0; i--) 
	{
		if (ctrl.options[i].selected == true)
		{
			var startSelection = i;
			while (i >= 0 && ctrl.options[i].selected == true ) 
			{
				i--;
			}
			if(i < 0) break;
			var endSelection = i;
			for (var j=i;j < startSelection && j < len;j++)
			{
				swap(ctrl, j, j+1);
			}
		}
	}
	
	arrangeTypeOrder();
}

//The function rearranges the type-order.
function onDownType()
{
	var ctrl = document.getElementById("field_types");
	var len = ctrl.options.length;
    
	var tempValue, tempText, tempSelected;
    
	for(var i = 0; i < len - 1; i++) 
	{
		if (ctrl.options[i].selected == true)
		{
			var startSelection = i;
			while (i < len && ctrl.options[i].selected == true) 
			{
				i++;
			}
			if(i == len) break;
			var endSelection = i;
			for (var j=i;j>startSelection && j > 0;j--)
			{
				swap(ctrl, j, j-1);
			}
		}
	}
	arrangeTypeOrder();
}


/***************************************************************************************
                          Methods for admin-roles page.
 ***************************************************************************************/
 
//This function reloads the page and displays the attributes of the newly selected role.
function onPropertyChangeRoles()
{
	var selIndex = document.getElementById("roles_list").options.selectedIndex;
	var roleName = new String(document.getElementById("roles_list").options[selIndex].text);
	var redirection = getRelativePath(window.location, "admin-roles") + "/" + sysPrefix + "/" + roleName;
	window.location = redirection;
}

/*
 * This sets the value of the element
 */
function setValue(element, val)
{
	element.attributes["value"].value = val;
}

/*
 * Gets the value of an element
 */
function getValue(element)
{
	return element.attributes["value"].value;
}
//This function is an implementation of the three state check box in admin-roles page.
function onClickImage(imageId)
{
	formChanged = true;
	var currentImageId = new String(imageId);
	var currentPermissionImageId = currentImageId+"p";
	var currentImageElem = document.getElementById(currentImageId);
	var currentPermissionImageElem = document.getElementById(currentPermissionImageId);
		
	var selImage = currentImageElem.src;
	var value = getValue(currentImageElem);
	var val;
	var newImage;
	if(value == "grant")
	{
	  val = "deny";
	  newImage = getImageRelativePath(selImage,"ckdeny.gif");
	}
	
	if(value == "deny")
	{
	  val = "empty";
	  newImage = getImageRelativePath(selImage,"ckempty.gif");
	}
	
	if(value == "empty")
	{
	  val = "grant";
	  newImage = getImageRelativePath(selImage,"ckgrant.gif");
	}
	  currentImageElem.src = newImage;
	  setValue(currentImageElem, val);
	  setValue(currentPermissionImageElem, val);
	
}

//This function is an implemntation of two state check box for the roles-users
// in the admin roles page.
function onClickRoleUserImage(imageId)
{
	formChanged = true;
	var currentImageId = new String(imageId);
	var currentPermissionImageId = currentImageId+"p";
	var selImage = document.getElementById(currentImageId).src;
	var value = document.getElementById(currentImageId).value;
	var val;
	var newImage;
	if(value == "grant")
	{
	  val = "deny";
	  newImage = getImageRelativePath(selImage,"ckdeny.gif");
	  document.getElementById(currentImageId).src = newImage;
	  document.getElementById(currentImageId).value = val;
	  document.getElementById(currentPermissionImageId).value = val;	  
	}
	
	if(value == "deny")
	{
	  val = "grant";
	  newImage = getImageRelativePath(selImage,"ckgrant.gif");
	  document.getElementById(currentImageId).src = newImage;
	  document.getElementById(currentImageId).value = val;
	  document.getElementById(currentPermissionImageId).value = val;	  
	}
	
}

//This function gets the relative path of the pseudo check box images in admin-roles page.
function getImageRelativePath(location, target)
{
	var parentLocation = new String(location);
	var arrParts = parentLocation.split("/");
	var action = "";
	action = arrParts[0] + "//" + arrParts[2] + "/" +arrParts[3] + "/" +arrParts[4] + "/" +target;
	return action;
}

function onRevertRolePermissions()
{
    var result = confirm(REVERT_BACK_TO_DEFAULT_SETTINGS);
	if (result == true)
	{
	    document.getElementById("adminRolesForm").action = getRelativePath(window.location,"admin-roles")+"/"+sysPrefix+"/"+document.getElementById("roles_list").value;
		document.getElementById("adminRolesForm").revert.value='true';
		document.getElementById("adminRolesForm").submit();
	}
}

//This function posts the form-data in the admin-roles page. 
function onSubmitRoles()
{
	document.getElementById("adminRolesForm").action = getRelativePath(window.location,"admin-roles") + "/" 
	                                                   + sysPrefix + "/" + document.getElementById("roles_list").value;
	document.getElementById("adminRolesForm").submit();
}


/***************************************************************************************
                          Methods for admin-users page.
 ***************************************************************************************/
//This function makes an xml-http call and gets the atributes of the newly selected user.
function onUserChange()
{
 if((formChanged == true) && (confirm(NAVIGATION_ALERT) == false))
  {
     formChanged = false;
     return false;
  } 
  
 else if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	var newUserId = document.getElementById("user_ids").value;
	var url = getRelativePath(window.location, "admin-users/") + sysPrefix 
	    + "?user_id=" + newUserId 
	    + "&userPage="+ userPage 
	    + "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
  }
  
}

//This function opens the categories sub-section of the selected user.
function openUserCategories()
{
 if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	document.getElementById("categorySpan").className = "b cb";
	document.getElementById("roleSpan").className = "l cb";
	document.getElementById("profileSpan").className = "l cb";
	var newUserId = document.getElementById("user_ids").value;
	userPage = "categories";
	var url = getRelativePath(window.location, "admin-users/") + sysPrefix 
	        + "?user_id="+newUserId 
			+ "&userPage=" + userPage 
			+ "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
 }
}

//This function opens the roles sub-section of the selected user.
function openUserRoles()
{
  if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	document.getElementById("categorySpan").className = "l cb";
	document.getElementById("roleSpan").className = "b cb";
	document.getElementById("profileSpan").className = "l cb";
	var newUserId = document.getElementById("user_ids").value;
	userPage = "roles";
	var url = getRelativePath(window.location, "admin-users/") + sysPrefix 
	    + "?user_id="+newUserId 
	    + "&userPage=" + userPage 
	    + "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
  }
}

//This function opens the hierarchy for the selected user.
function openEscalationHierarchy()
{
	if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false)){
		var userId = document.getElementById("user_ids").value;
		var url = getRelativePath(window.location, "admin-user-escalation/"  + sysPrefix)
			+ "?pageType=hierarchy"
			+ "&sub_action=view"
			+ "&user_id="+userId;
		var output = executeHttpRequest("GET", url, false);
		output = trim(output);
		document.getElementById("divReplacement").innerHTML = output;
		formchanged = false;
	}	
}


//This function opens the web-profile sub-section of the selected user.
function openUserWebProfile()
{
  if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	document.getElementById("categorySpan").className = "l cb";
	document.getElementById("roleSpan").className = "l cb";
	document.getElementById("profileSpan").className = "b cb";
	var newUserId = document.getElementById("user_ids").value;
	userPage = "webProfile";
	var url = getRelativePath(window.location, "admin-users/") + sysPrefix 
	    + "?user_id=" + newUserId 
	    + "&userPage=" + userPage 
	    + "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
 }
}

//This function pops up a dialog box to add a Business-Area user.
function addUser()
{
   // var nearestPath = document.getElementById("nearestPath").value;
	var location = YAHOO.transbit.tbits.contextPath + "admin-util/" + sysPrefix 
	    + "?sysPrefix=" + encodeURIComponent(sysPrefix)
	    + "&action=add-user&now=" 
	    + getCurrentTime();
    var field_name = document.getElementById("user_ids").value;
    // Set the features of the Modal dialog 
    var features = "status:no; dialogWidth:400px; dialogHeight:120px;resizable: yes";
     
    if(isIE == true)
    {
       var child = new Object();
       child.returnValue = false;
       child.sysPrefix = sysPrefix;
       window.showModalDialog(location, child, features);
        if (child.returnValue == true)
        {
            window.location = window.location;
        }
     }
     else
     {
        window.returnValue = false;
        window.sysPrefix = sysPrefix; 
        var newWindow = window.open(location, "name" , features);
		newWindow.opener = window;
     }
    return;
}

function onSubmitAddUser()
{
   	var newUser = document.getElementById("new_user_name").value; 
	var sysPrefix = obj.sysPrefix;
	var url = getRelativePath(window.location, "admin-users/") + sysPrefix 
	    + "?new_user=true" 
	    + "&user_login=" + newUser;
			  		             
	var output = executeHttpRequest("POST", url, false);
	
	if(output.indexOf("success") >= 0)
	{
		if(isIE == true)
		{
		   // Action Success.
		   window.close();
		   if (obj != null) obj.returnValue = true;
		   return;
		}
		else
		{
		  this.opener.location = this.opener.location;
		  window.close();
		}
	}
	else
	{
		alert(output);
		if(isIE == true)
		{
		   // Action Success.
		   window.close();
		   if (obj != null) obj.returnValue = true;
		   return;
		}
		else
		{
		  this.opener.location = this.opener.location;
		  window.close();
		}
		//if (obj != null) obj.returnValue = true;
		//return;
	}
	window.opener = window.opener;
}

//This function deletes a selected Business-Area user.
function deleteUser()
{
	var result = confirm("Do you wish to delete the user?");
	if (result == false)
	{
		return;
	}
	var selIndex = document.getElementById("user_ids").options.selectedIndex;
	var userId = new String(document.getElementById("user_ids").options[selIndex].value);
    var url = getRelativePath(window.location, "admin-users/") + sysPrefix + 
			  "?new_user=false" + "&user_id=" + userId;
	var output = executeHttpRequest("POST", url, false);
	if(output.indexOf("success") >= 0)
		window.location = window.location;
	else
		alert(output);	
}

//This function posts the data based on the current sub-section in the admin-users page. 
function onSubmitUsers()
{
    formChanged = false;
	var currentUserId = document.getElementById("user_ids").value;
	if(userPage == "categories")
	{
	    document.getElementById("adminUsersForm").action = getRelativePath(window.location,"admin-users")+"/"+sysPrefix;
	   	var assignee;
		var notification;
		var volunteer;
		var url = getRelativePath(window.location, "admin-users/") + sysPrefix ;
		var data = "user_id="+currentUserId + "&date=" + (new Date()) + "&userPage=categories";
		var categoryList = document.getElementById("categoryList").value;	
		var strList = categoryList.split(',');		
	    for(var i=0 ;i< strList.length; i++)
	     {
			 if((strList[i]!= ""))
			 {
				assignee = document.all.item(strList[i] + "_ass").checked;
				notification = document.all.item(strList[i] + "_select").value;
				volunteer = document.all.item(strList[i] + "_vol").checked;
				if(assignee != null)
				{
				  data = data + "&" + (strList[i] + "_ass") + "=" + assignee;
				}
				if(notification != null)
				{
				  data = data + "&" + (strList[i] + "_select") + "=" + notification;
				}
				if(volunteer != null)
				{
				  data = data + "&" + (strList[i] + "_vol") + "=" + volunteer;
				}
             }
         } 
		xmlHttpPostFormData(url, data);
	   
	}
	if(userPage == "roles")
	{
		var assignee;
		var notification;
		var volunteer;
		var url = getRelativePath(window.location, "admin-users/") + sysPrefix +
				"?user_id="+currentUserId + "&date=" + (new Date()) + "&userPage=roles";
		var rolesList = document.getElementById("rolesList").value;	
		var strList = rolesList.split(',');		
	    for(var i=0 ;i< strList.length; i++)
	     {
			 if((strList[i]!= ""))
			 {
				isChecked = document.all.item(strList[i]);
				if(isChecked != null)
				{
				  url = url+"&"+(strList[i])+"="+isChecked.checked;
				}
             }
         }
		var output = executeHttpRequest("POST", url, false);
	}
	if(userPage == "webProfile")
	{
	    var webConfig = document.getElementById("webConfig").value;
	    if(webConfig == undefined)
	    {
	    	document.getElementById("webConfig").innerText;
	    }
		//var url = getRelativePath(window.location, "admin-users/") + sysPrefix +
		//		"?user_id="+currentUserId + "&webConfig="+webConfig+"&date=" + (new Date());
		//var output = executeHttpRequest("POST", url, false);
		var url = getRelativePath(window.location, "admin-users/")+sysPrefix;
		var data = "user_id="+currentUserId + "&webConfig=" + encodeURIComponent(webConfig) + "&date=" + (new Date());
		xmlHttpPostFormData(url, data);
	}
	window.location.reload();
}

function viewEmailOptionCalendar()
{
    var features = "status:no; dialogWidth:842px; dialogHeight: 142px;resizable: yes;";
    var nearestPath = document.getElementById("nearestPath").value;
    var url = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&notification=true&now=" + getCurrentTime();

    var status = "no";
    var width = "842";
    var height = "880";
    var resize = "yes";
    var obj = new Object();
    openModalDialog(url, obj, 
                    "status", status,
                    "width", width,
                    "height", height,
                    "resize", resize);
    
}

/***************************************************************************************
                          Methods for admin-manage type fields page.
 ***************************************************************************************/
//This function pops up a dialog box to add a field-type from the manage categories page.
function addFieldType()
{
    var nearestPath = document.getElementById("nearestPath").value;
	var location = YAHOO.transbit.tbits.contextPath + "admin-util/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&action=add-type&now=" + getCurrentTime();
    var field_name = document.getElementById("field_name").value;
    // Set the features of the Modal dialog 
    var features = "status:no; dialogWidth:400px; dialogHeight:150px;resizable: yes";
    
    if( isIE == true)
    {
		var child = new Object();
		child.returnValue = false;
		child.fieldName = field_name;
		child.sysPrefix = sysPrefix;
     
		window.showModalDialog(location, child, features);
		window.location = window.location;
	}
	else
	{
	    window.returnValue = false;
        window.sysPrefix = sysPrefix; 
        window.fieldName = field_name;
        var newWindow = window.open(location, "name" , features);
		newWindow.opener = window;
	}
    return;
}

//This function deletes a selected field-type from the manage  categories page.
function deleteFieldType()
{
	var result = confirm("Do you wish to delete the type?");
	if (result == false)
	{
		return;
	}
	var fieldName = document.getElementById("field_name").value;
	selIndex = document.getElementById("field_types").options.selectedIndex;
	var typeName = new String(document.getElementById("field_types").options[selIndex].value);
	var url = getRelativePath(window.location, "admin-fields/") + sysPrefix 
	    + "?field_ids=" + encodeURIComponent(fieldName) 
	    + "&field_types=" + encodeURIComponent(typeName)
	    +"&action=typesDelete";														
	var output = executeHttpRequest("POST", url, false);
	output = trim(output);
	alert(output);

}

function onFieldTypeChange()
{
   if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	var newTypeId = document.all.field_types.value;
	var fieldName = document.getElementById("field_name").value;
	var url = getRelativePath(window.location, "admin-manage/") + sysPrefix 
	        + "?type_id=" + newTypeId 
			+ "&field_name=" + encodeURIComponent(fieldName) 
			+ "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
 }
 
  if((formChanged == true) && (confirm(NAVIGATION_ALERT) == false))
  {
     return false;
  } 
}

function onSubmitManage()
{
  formChanged = false;
  var currentTypeId = document.getElementById("field_types").value;
  var fieldName = document.getElementById("field_name").value;
  var orderValuePairs = document.getElementById("orderValuePairs").value;
  document.getElementById("adminManageForm").action = getRelativePath(window.location,"admin-manage")+"/"+sysPrefix;
  var url = getRelativePath(window.location, "admin-manage/") + sysPrefix ;
  var data = "type_id=" + currentTypeId 
        + "&field_name="+  encodeURIComponent(fieldName) 
        + "&date=" + (new Date()) 
        + "&action=updateTypeUsers" 
        + "&orderValuePairs=" + encodeURIComponent(orderValuePairs);
  var assignee;
  var notification;
  var volunteer;
  var usersList = document.getElementById("usersList").value;	
  var strList = usersList.split(',');		
  for(var i=0 ;i< strList.length; i++)
	  {
		 if((strList[i]!= ""))
			{
				if(fieldName == "category_id")
				{
				    assignee = document.all.item(strList[i] + "_ass").checked;
				    volunteer = document.all.item(strList[i] + "_vol").checked;
				     if(assignee != null)
				     {
				        data = data + "&" + (strList[i] + "_ass") + "=" + assignee;
				     }
				     if(volunteer != null)
				     {
				        data = data + "&" + (strList[i] + "_vol") + "=" + volunteer;
				     }
				}
				
				notification = document.all.item(strList[i] + "_select").value;
				if(notification != null)
				{
				  data = data + "&" + (strList[i] + "_select") + "=" + notification;
				}
            }
      } 
		//var output = executeHttpRequest("POST", url, false);
		xmlHttpPostFormData(url, data);	
}

function onSelectAll()
{
	   formChanged = true;
	   var categoryList = document.getElementById("categoryList").value;	
	   var strList = categoryList.split(',');		
	   for(var i=0 ;i< strList.length; i++)
	    {
	       	if((strList[i]!= ""))
			 {
			    assignee = document.all.item(strList[i] + "_ass").checked = true;
				volunteer = document.all.item(strList[i] + "_vol").disabled = false;
			 }
	    }

}

function onSelectAllUsers()
{
	   formChanged = true;
	   var usersList = document.getElementById("usersList").value;	
	   var fieldName = document.getElementById("field_name").value;
	   var strList = usersList.split(',');		
	   for(var i=0 ;i< strList.length; i++)
	    {
	       	if((strList[i]!= "") && (fieldName == "category_id"))
			 {
				assignee = document.all.item(strList[i] + "_ass").checked = true;
				volunteer = document.all.item(strList[i] + "_vol").disabled = false;
			 }
	    }

}

function onUnSelectAll()
{
	   formChanged = true;
	   var categoryList = document.getElementById("categoryList").value;	
	   var strList = categoryList.split(',');		
	   for(var i=0 ;i< strList.length; i++)
	    {
	       	if((strList[i]!= ""))
			 {
			    document.all.item(strList[i] + "_ass").checked = false;
			    document.all.item(strList[i] + "_vol").checked = false;
			 }
	    }

}

function onUnSelectAllUsers()
{
	   formChanged = true;
	   var usersList = document.getElementById("usersList").value;	
	   var fieldName = document.getElementById("field_name").value;
	   var strList = usersList.split(',');		
	   for(var i=0 ;i< strList.length; i++)
	    {
	       	if((strList[i]!= "") && (fieldName == "category_id"))
			 {
			    document.all.item(strList[i] + "_ass").checked = false;
			    document.all.item(strList[i] + "_vol").checked = false;
				document.all.item(strList[i] + "_vol").disabled = true;
			 }
	    }

}

function onClickAssigneeBox(category)
{
     formChanged = true;
     var cat = new String(category);
     var catChecked = document.getElementById(cat+"_ass");
     if(catChecked.checked == false)
      {
         document.getElementById(cat + "_vol").checked = false;
      }

}

function onClickVolunteerBox(category)
{
     formChanged = true;
     var cat = new String(category);
     var catChecked = document.getElementById(cat+"_vol");
     if(catChecked.checked == true)
      {
         document.getElementById(cat + "_ass").checked = true;
      }

}

function arrangeTypeOrder()
{
   var orderValuePairs = "";
   var ctrl = document.getElementById("field_types");
   var len = ctrl.options.length;
   for(var i = 0; i <= len - 1; i++) 
   {
       var typeValue = ctrl.options[i].value;
       orderValuePairs = orderValuePairs+typeValue
       if(i != len -1)
       orderValuePairs = orderValuePairs + ",";
   }
   document.getElementById("orderValuePairs").value = orderValuePairs;
   
}

function swap(ctrl, i, j)
{
	tempValue = ctrl.options[i].value;
	tempText = ctrl.options[i].text;
	tempSelected = ctrl.options[i].selected;
    
	ctrl.options[i].value = ctrl.options[j].value;
	ctrl.options[i].text = ctrl.options[j].text;
	ctrl.options[i].selected = ctrl.options[j].selected;
    
	ctrl.options[j].value = tempValue;
	ctrl.options[j].text = tempText;
	ctrl.options[j].selected = tempSelected;
}

function onRevertLastSavedSettings()
{
    window.location = window.location;
}

/*******************************************************************************************
                   Functions to redirect to a different admin page.
********************************************************************************************/
function onClickProperties()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-properties") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-properties") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickFields()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-fields") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-fields") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickCaptions()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-captions") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-captions") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickApp()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-app")+ "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-app") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickRoles()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-roles") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-roles") + "/" + sysPrefix;
	     window.location = redirection;
	}
}
function onClickAllUsers()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-allusers") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-allusers") + "/" + sysPrefix;
	     window.location = redirection;
	}
}
function onClickUsers()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-users") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-users") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickListJobs()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "all-job-actions") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "all-job-actions") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickReports()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "adminreports") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "adminreports") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickEscalations()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-escalations") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-escalations") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickDisplayGroups()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admindisplaygroups") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admindisplaygroups") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickTransmittals()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-transmittals") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-transmittals") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onClickSMSLogs()
{
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-smslogs") + "/" + sysPrefix;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-smslogs") + "/" + sysPrefix;
	     window.location = redirection;
	}
}

function onManage(e)
{
    domTT_maxWidth = 100; 
    var event = (!document.all) ? e : window.event;
    domTT_activate(this, event,
                    'caption','Type &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',
                    'content',document.getElementById('allTypeFields').innerHTML, 
                    'type','greasy',
                    'lifetime', '3000',
                    'statusText','Type Fields',
                    'offsetY', '5'
                    );
    domTT_maxWidth = 300;               
}

function openManagePage(fieldName)
{
  var field_name = new String(fieldName);
   	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == true) 
		{
   	        var redirection = getRelativePath(window.location, "admin-manage") + "/" + sysPrefix + "/" + field_name;
	        window.location = redirection;
			return;
		}
	}
	else
	{
	   	 var redirection = getRelativePath(window.location, "admin-manage") + "/" + sysPrefix + "/" + field_name;
	     window.location = redirection;
	}
}

