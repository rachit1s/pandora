//This function makes an xml-http call and gets the atributes of the newly selected user.
function getUserDetails()
{
	var userConfirmed = false;
	if(formChanged == true)
		 var userConfirmed =  confirm(NAVIGATION_ALERT);
	if( formChanged && !userConfirmed)
	{
		//formChanged = false;
		return false;
	} 

	else if((formChanged == false) || userConfirmed )
	{
	var newUserId = document.getElementById("user_ids").value;
	var url = getRelativePath(window.location, "admin-allusers")  
		+ "?user_id=" + newUserId 
		+ "&userPage=details"
		+ "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
		return true;
	}  
}

function newUser()
{
 if((formChanged == true) && (confirm(NAVIGATION_ALERT) == false))
  {
     //formChanged = false;
     return false;
  } 
  
 else if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	var url = getRelativePath(window.location, "admin-allusers")  
	    + "?userPage=newuserpage"
	    + "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
  }  
} 

function chagePasswordView()
{
 if((formChanged == true) && (confirm(NAVIGATION_ALERT) == false))
  {
     //formChanged = false;
     return false;
  } 
  
 else if(((formChanged == true) && (confirm(NAVIGATION_ALERT) == true)) || (formChanged == false))
 {
	var userId = document.getElementById("user_ids").value;
	var url = getRelativePath(window.location, "admin-allusers")  
		+ "?user_id=" + userId 
	    + "&userPage=changepasswordpage"
	    + "&date=" + (new Date());
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	document.getElementById("divReplacement").innerHTML = output;
	formChanged = false;
  } 
} 

function selectFirstUser()
{
var userComboBox = document.getElementById('user_ids');
if(userComboBox.length > 0)
	userComboBox.selectedIndex = 0;
}

function toggleMoreOptions()
{
 var moreOptionsDiv = document.getElementById('more_options_div')
 var moreP = document.getElementById('moreP') ;
 if( moreOptionsDiv != null && moreP != null )
 {
	 if( moreOptionsDiv.style.display == 'none' )
	 {
		 moreOptionsDiv.style.display = 'block' ;
		 moreP.innerHTML = '<u>Less << </u>' ;
	 }	
	 else
	 {		
		 moreOptionsDiv.style.display = 'none' ;
		 moreP.innerHTML = '<u>More >> </u>' ;
	 }
 }
}

function onUpdateUser()
{
	//var formBlock = document.getElementById('user_update_form');
	var url = getRelativePath(window.location, 'admin-allusers');
	
	var userId = document.getElementById('selected_user_id').value;
	var pageName = document.getElementById('page_name').value;
	var firstName = document.getElementById('first_name').value;
	var lastName = document.getElementById('last_name').value;
	var displayName = document.getElementById('display_name').value;
	var emailId = document.getElementById('email_id').value;
	var isActive = document.getElementById('is_active').checked;
	var userType = document.getElementById('user_type').value;
	var user_login = document.getElementById('user_login').value;
	var user_login = document.getElementById('user_login').value;
	var mobile_number = document.getElementById('mobile_number').value;
	var location = document.getElementById('location').value ;
	var firm_code = document.getElementById('firm_code').value ;
	var designation = document.getElementById('designation').value ;
	var firm_address = document.getElementById('firm_address').value ;
	var sex = document.getElementById('sex').value ;
	var full_firm_name = document.getElementById('full_firm_name').value ;
	var data = "selected_user_id=" + userId + "&page_name=" + pageName + "&first_name=" 
			+ firstName + "&last_name=" + lastName
			+ "&display_name=" + displayName + "&email_id=" + emailId + "&is_active=" + isActive
			+ "&user_type=" + userType + "&user_login=" + user_login + "&mobile_number=" + mobile_number
			+"&location=" + location + "&firm_code=" + firm_code + "&designation=" + designation + "&firm_address=" + firm_address
			+"&sex=" + sex + "&full_firm_name=" + full_firm_name ;
	//alert("data: " + data);
	var response = xmlHttpPostFormData(url, data);
	document.getElementById("message").innerHTML = response;
	if(response.indexOf("OK:") == 0)
	{
		formChanged = false;
		window.location = window.location;
		//selectUserByLogin(user_login);
	}
}
function selectUserByLogin(user_login)
{
	var myUserLogin = user_login.toLowerCase();
	var userComboBox = document.adminUsersForm.user_ids; //document.getElementById('user_ids');
	alert('trying to select user');
	for(var i =0; i < userComboBox.options.length; i++)
	{
		var option = userComboBox.options[i];
		if(option.text.toLowerCase() == myUserLogin)
		{
			option.selected = true;
			alert('selected');
			break;
		}
	}
	//if(userComboBox.length > 0)
	//userComboBox.selectedIndex = 0;
}

function onChangeUserPassword()
{
	var password = document.getElementById('password').value;
	var confirmPassword = document.getElementById('confirm_password').value;
	if(password != confirmPassword )
	{
		document.getElementById("message").innerHTML = "Passwords do not match.";
		return;
	}	
	var url = getRelativePath(window.location, 'admin-allusers');	
	var user_login = document.getElementById('user_login').value;
	var pageName = document.getElementById('page_name').value;
	
	var data = "user_login=" + user_login + "&page_name=" + pageName + "&password=" + password;
	//alert("data: " + data);
	var response = xmlHttpPostFormData(url, data);
	document.getElementById("message").innerHTML = response;
	if(response.indexOf("OK:") == 0)
	{
		formChanged = false;
	}
}
