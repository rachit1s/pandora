AjaxUploader = function(){
	this.fileSelect = new YAHOO.util.CustomEvent("fileSelect", this); 
	this.completeUpload = new YAHOO.util.CustomEvent("completeUpload", this); 
	this.upload = function(file){
		//uploader.onFileSelect();
		formObject = file["formObject"];
		//file["size"] = this.getFileSize(formObject);
		var setform = YAHOO.util.Connect.setForm(formObject, true, true);
		var callback = { 
			argument: "res",
			upload: function(o){this.argument = o.responseText;}
		}
		var sysPrefix = document.getElementById('sysPrefix').value;
		var requestId = document.getElementById('requestId').value;
		var servlet = "/uploader?folderhint=" + sysPrefix + "&requestId=" + requestId;
		var cObj = YAHOO.util.Connect.asyncRequest('POST', servlet, callback);
		var caller = this;
		var a = new TestData(file); 
		function TestData(data) { 
		    this.data = data; 
		} 
		//alert(callback.argument);
		checkInterval = setInterval(checkSuccess, 100);
		function checkSuccess() {
			if(cObj.isUpload && callback.argument!= "res"){	
				if(callback.argument.substr(0,1) == "<"){
					var data = callback.argument.substring(5).split('<')[0];
				}
				else{ 
					var data = callback.argument;
				}
				if(data){
					a["data"]["size"] = data.split('-')[0];
					a["data"]["data"] = data.split('-')[1];
					var name = formObject.elements[0].value;
					if(name.lastIndexOf('\\') != -1)
						name = name.substr(name.lastIndexOf('\\') + 1);
					a["data"]["name"] = name;
					clearInterval(checkInterval);
					formObject.elements[0].value = "";
					caller.completeUpload.fire(a);
				}
			}
		}
	}
}
