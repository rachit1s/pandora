function onLoadBody()
{

	var action		= document.getElementById("action").value;
	var query		= document.getElementById("queryString").value;
	var today		= new Date();
	var clientOffset	= today.getTimezoneOffset();
	var redirection	= action;
	if (query != "")	redirection = redirection + "?" + query + "&clientOffset=" + clientOffset;
	else			redirection = redirection + "?clientOffset=" + clientOffset;
				   
	window.location = redirection;
}