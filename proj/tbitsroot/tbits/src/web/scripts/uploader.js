/**
 * TODO: 
 * *. HIGH: The non-flash version should be there when browser doesnt have the flash installed.
 * *. Medium: The call on 'remove' or 'cancel' shouldn't be string based. 
 * 	  This would remove the object name parameter in constructor of tbitsUploader.
 * *. Low: Remove passing of the attaching the extra variables to uploader. The 'caller' is sufficient.
 * *. Low: See if the number of storage variables (filesList, filesQueue, fileProgress) can be reduced.
 * *. Low: Substitute 'Cancel' with img of cross and 'Remove' with img of 'Trash'
 */

/**
 * 
 * @param addDiv The id of div in which the add button is to be availed.
 * @param addDivOverlay The uploader overlay.
 * @param editDiv 
 * @param editDivOverlay
 * @param tableContainer
 * @param requestFieldFiles
 * @param objName The name of the object. This is used in 'Cancel' buttons. Since creating cancel buttons isnt possible using createElement()
 * @return
 */
function TbitsUploader(caption, addDiv, addDivOverlay, editDiv, editDivOverlay,
		tableContainer, requestFieldFiles, objName, imageHome, uploaderUrl,
		yuiHome, canAdd, canChange, flashAvailable) {
	this.caption = caption;
	this.addDiv = addDiv;
	this.addDivOverlay = addDivOverlay;
	this.editDiv = editDiv;
	this.editDivOverlay = editDivOverlay;
	this.tableContainer = tableContainer;
	this.requestFieldFiles = requestFieldFiles;
	this.imageHome = imageHome;
	this.objName = objName;
	this.uploaderUrl = uploaderUrl;
	this.yuiHome = yuiHome;
	this.canAdd = canAdd;
	this.canChange = canChange;
	this.flashAvailable = flashAvailable;

	this.mySetInterval = setInterval;
	this.isChanged = false;

	this.init = function() {
		// var uploader;
		// var editUploader;
		// var singleSelectDataTable;

		/* It is an array of file objects to be uploaded by the uploader */
		this.filesQueue = [];
		this.underProcess = [];
		this.errorQueue = [];

		/*
		 * It is a hash table (id => record). Where id = uploader Id (0 incase
		 * of add uploader and 1 in case of edir uploader. and record is the
		 * datatable's record.
		 */
		this.filesList = [];
		this.isInCall = false;

		this.initTable(tableContainer, requestFieldFiles);
		this.singleSelectDataTable.parent = this;
		
		if(flashAvailable)
		YAHOO.widget.Uploader.SWFURL = this.yuiHome
				+ "/yui/build/uploader/assets/uploader.swf";

		
		if(this.canAdd && (this.canAdd == true))
		{
			this.initUploader(addDivOverlay);
			if(flashAvailable)
				this.resizeUploadButton(addDiv, addDivOverlay);
		}
		else
		{
			if(flashAvailable)this.disableUploader(addDiv);
		}
		
		if(this.canChange && (this.canChange == true))
		{
			this.initEditUploader(editDivOverlay);
			if(flashAvailable)
				this.resizeUploadButton(editDiv, editDivOverlay);
		}
		else 
		{
			if(flashAvailable)this.disableUploader(editDiv);
		}

		// this.singleSelectDataTable.editUploader = this.editUploader;

		// Now bind the radio event with editUploader.lastSelectedRecord
		// this.subsTableRadioSelect();
	}

	this.resizeUploadButtons = function() {
		this.resizeUploadButton(this.addDiv, this.addDivOverlay);
		this.resizeUploadButton(this.editDiv, this.editDivOverlay);
	}
	var sizeFormatter = function(elCell, oRecord, oColumn, oData) {
		if (oData >(1024*1024))
			elCell.innerHTML = (oData/(1024*1024)).toFixed(0) + " MB";
		else if (oData < (1024*1024))			
			elCell.innerHTML = (oData/1024).toFixed(0) + " KB";
		if (oData < 1024)
			elCell.innerHTML = oData + " B";		
	};
	this.initTable = function(divId, requestFieldFiles) {
		//create the skelton
		myColumnDefs = [ {
			key : "isSelected",
			label : "",
			sortable : true,
			formatter : "radio"
		}, {
			key : "name",
			label : "File Name",
			sortable : true
		}, {
			key : "size",
			label : "Size",
			sortable : true,
			formatter : sizeFormatter
		}, {
			key : "progress",
			label : "Upload progress",
			sortable : true
		} ];

		myDataSource = new YAHOO.util.DataSource( []);
		myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		myDataSource.responseSchema = {
			fields : [ "isSelected", "id", "name", "created", "modified",
					"type", "size", "progress", "status", "requestFileId",
					"repoFileId" ]
		};

		this.singleSelectDataTable = new YAHOO.widget.DataTable(divId,
				myColumnDefs, myDataSource, {
					caption : null,
					selectionMode : "single",
					MSG_EMPTY : "Click on add files to attach files."
				});
		this.singleSelectDataTable.subscribe("radioClickEvent",
				function(oArgs) {
					if (this.lastSelectedRadioRecord) {
						this.lastSelectedRadioRecord.setData("isSelected",
								false);
					}
					var elRadio = oArgs.target;
					var oRecord = this.getRecord(elRadio);
					oRecord.setData("isSelected", true);
					this.lastSelectedRadioRecord = oRecord;
				});
		
		if (requestFieldFiles != null) {
			
			//Load request fields files
			for ( var i = 0; i < requestFieldFiles.length; i++) {
				var file = requestFieldFiles[i];
				var entry = {};
				entry.newId = "-1-" + file.requestFileId;
				entry["isSelected"] = false;
				var removeStr = "";
				if(this.canChange || file.requestFileId == 0 )
					removeStr = "<a href='#' name='remove'>Remove</a>";
				entry["progress"] = removeStr;

				if ((file.name == null) || (file.name.length == 0))
					continue;

				entry.name = file.name;
				if (file.size == null)
					continue;
				entry.size = file.size;
				entry.requestFileId = file.requestFileId;
				entry.repoFileId = file.repoFileId;

				var record = this.singleSelectDataTable.getRecordSet()
						.addRecord(entry);
				this.filesList[entry.newId] = record;
			}
			this.singleSelectDataTable.render();
		}
		
		this.singleSelectDataTable.subscribe("linkClickEvent", function(oArgs) {
			var e = oArgs;
			var el = oArgs.target;
			var record = this.getRecord(el);
			var name = e.target.name;
			var row = record.getData('newId');
			if(name == 'remove')
			{
				this.parent.removeFromTable(row);
			}
			else if(name == 'cancel')
			{
				this.parent.cancelUpload(row);
			}
			else if(name == 'tryagain')
			{
				this.parent.tryAgain(row);
			}
		});
	}

	this.resizeUploadButton = function(link, overlay) {
		var uiLayer = YAHOO.util.Dom.getRegion(link);
		var overlay = YAHOO.util.Dom.get(overlay);
		YAHOO.util.Dom.setStyle(overlay, 'width', uiLayer.right - uiLayer.left
				+ "px");
		YAHOO.util.Dom.setStyle(overlay, 'height', uiLayer.bottom - uiLayer.top
				+ "px");
	}
	this.disableUploader = function(link)
	{
		try
		{
			//alert(link);
			var button = YAHOO.util.Dom.get(link);
			button.disabled = true
		}
		catch(o)
		{}
		
		try
		{
			YAHOO.util.Dom.setAttibute(link, "disabled", "true");
		}
		catch(o)
		{}
	}

	this.initUploader = function(divOverlay) {

		// Instantiate the uploader and write it to its placeholder div.
		if(this.flashAvailable)
				this.uploader = new YAHOO.widget.Uploader(divOverlay);
		else
			this.uploader = new AjaxUploader();
		
		this.uploader.caller = this;
		with (this) {
			uploader.filesList = filesList;
			uploader.filesQueue = filesQueue;
			uploader.singleSelectDataTable = singleSelectDataTable;
			uploader.objName = objName;
			uploader.flashAvailable = flashAvailable;
			uploader.imageHome = imageHome;
			// Add event listeners to various events on the uploader.
			if(this.flashAvailable){
				uploader.addListener('contentReady', handleContentReady);
				uploader.addListener('fileSelect', onFileSelect);
				uploader.addListener('uploadProgress', onUploadProgress);
				uploader.addListener('uploadComplete', onUploadComplete);
				uploader.addListener('uploadCompleteData', onUploadCompleteData);
				uploader.addListener('uploadError', onUploadError);
			}else{
				uploader.fileSelect.subscribe(this.onAjaxFileSelect,this);
				uploader.completeUpload.subscribe(this.onUploadCompleteDataAjax, this);
			}
		}
	}

	this.onAjaxFileSelect = function(event, o){
	
	}
	
	this.handleContentReady = function() {
		try {
			this.setAllowMultipleFiles(true);
		} catch (e) {
		}
	}

	this.onFileSelect = function(event) {
		// add the files to queue
		if(this.flashAvailable){
			if ('fileList' in event && event.fileList != null) {
				fileList = event.fileList;
			}
			for ( var i in fileList) {
				if (typeof (i) == 'function')
					continue;
	
				var entry = fileList[i];
				if (entry.id == null)
					continue;
				entry.newId = "0-" + entry.id;
	
				var existingEntry = this.filesList[entry.newId];
				if (!existingEntry) {
					this.filesQueue.push(entry);
					// add-in-table
					entry["isSelected"] = false;
					entry["progress"] = 
						"<div><div style='height:15px;width:100px;background-color:#CCC;float:left'></div><a href='#' class=\"withspace\" name='cancel'>Cancel</a></div>";
					var record = this.singleSelectDataTable.getRecordSet()
							.addRecord(entry);
					this.filesList[entry.newId] = record;
				}
			}
		}else{
			var count = 0;
			for(var i in this.filesList)
				count++;
			var entry = new Array();
			entry["formObject"] = event;
			entry["name"] = event.elements[0].value;
			entry["id"] = "file" + count;
			entry["newId"] = "0-" + entry.id;
			var existingEntry = this.filesList[entry.newId];
			if (!existingEntry) {
				this.filesQueue.push(entry);
				// add-in-table
				entry["isSelected"] = false;
				entry["progress"] = 
					"<div><img src='" + this.imageHome + "/loader2.gif' alt='uploading...' title='uploading...' /><a href='#' class=\"withspace\" name='cancel'>Cancel</a></div>";
				var record = this.singleSelectDataTable.getRecordSet().addRecord(entry);
				this.filesList[entry.newId] = record;
			}
			//this.uploader.Upload();
		}
		this.singleSelectDataTable.render();
	}

	this.onUploadProgressBase = function(event, prefix) {
		var newId = prefix + event["id"];
		currentEntry = this.filesList[newId];

		prog = Math.round(100 * (event["bytesLoaded"] / event["bytesTotal"]));
		progbar = "<div><div style='height:15px;width:100px;background-color:#CCC;float:left'><div style='height:15px;background-color:#F00;width:"
				+ prog
				+ "px;'></div></div><a href='#' class=\"withspace\" name='cancel'>Cancel</a></div>";
		currentEntry.setData("progress", progbar);
		this.singleSelectDataTable.render();
	}
	//Do something on each file's upload progress event.
	this.onUploadProgress = function(event) {
		this.caller.onUploadProgressBase(event, "0-");
	}

	this.onUploadCompleteBase = function(event, prefix) {
		var newId = prefix + event["id"];
		currentEntry = this.filesList[newId];

		progbar = 
					"<div><div style='height:15px;width:100px;background-color:#CCC;float:left'><div style='height:15px;background-color:#F00;width:100px;'></div></div>"
						+ "<a href='#' class=\"withspace\" name='remove'>Remove</a></div>";

		currentEntry.setData("progress", progbar);
		this.singleSelectDataTable.render();
	}
	//Do something when each file's upload is complete.
	this.onUploadComplete = function(event) {
		this.caller.onUploadCompleteBase(event, "0-");
	}

	this.onUploadCompleteDataBase = function(event, prefix) {
		var newId = prefix + event["id"];
		currentEntry = this.filesList[newId];
		progbar = '<img src="' + this.imageHome
				+ '/icon_done_small.gif" alt="Upload Finished."/>'
				+ '<a href="#" class="withspace" name="remove">Remove</a>';
		currentEntry.setData("progress", progbar);

		//Remove from underProgress queue
		var i = this.getQueueFileIndexByNewId(this.underProcess, newId);
		if (i != -1) {
			this.underProcess.splice(i, 1);
		}
		var repoFileId;
		try {
			if(!this.flashAvailable){
				currentEntry.setData("repoFileId", parseInt(event.data));
				currentEntry.setData("size", event.size);
				currentEntry.setData("name", event.name);
				currentEntry.setData("isSelected", false);
			}
			else{
				repoFileId = parseInt(event.data.split('-')[1]);
				currentEntry.setData("repoFileId", repoFileId);
			}
		} catch (e) {
			this.onUploadErrorBase(event, prefix);
		}
		this.isChanged = true;
		this.filesList[newId] = currentEntry;
		this.singleSelectDataTable.render();
	}
	this.onUploadCompleteData = function(event) {
			var newId = "0-" + event["id"];
			currentEntry = this.filesList[newId];
			currentEntry.setData("requestFileId", "0");
			this.caller.onUploadCompleteDataBase(event, "0-");
	}
	
	this.onUploadCompleteDataAjax = function(event, file) {
		var newId = "0-" + file[0]["data"]["id"];
		currentEntry = this.filesList[newId];
		currentEntry.setData("requestFileId", "0");
		this.caller.onUploadCompleteDataBase(file[0]["data"], "0-");
	}
	
	this.onEditUploadCompleteDataAjax = function(event, file) {
		this.caller.onUploadCompleteDataBase(file[0]["data"], "1-");
	}

	/**
	 * Returns the index of file in queue based on newId.
	 * In case its not found, it returns -1
	 */
	this.getQueueFileIndexByNewId = function(queue, newId) {
		var found = false;
		var i = 0;
		for (; i < queue.length; i++) {
			if (newId == queue[i].newId) {
				found = true;
				break;
			}
		}
		if (found)
			return i;
		else
			return -1;
	}
	this.onUploadError = function(event) {
		this.caller.onUploadErrorBase(event, "0-");
	}
	this.onUploadErrorBase = function(event, prefix) {
		var newId = prefix + event["id"];
		currentEntry = this.filesList[newId];

		//Move the file from uploadProgress to errorQueue
		var i = this.getQueueFileIndexByNewId(this.underProcess, newId);
		if (i != -1) {
			//Move the file from error queue to upload queue
			var file = this.underProcess[i];
			this.underProcess.splice(i, 1);
			this.errorQueue.push(file);
		}

		//Update UI
		progbar = '<img src="'
				+ this.imageHome
				+ '/icon_error_small.gif" title="Error occured while uploading. You can try again or cancel." />'
				+ '<a class="withspace"  href="#" title="Try again." name="tryagain">Try Again</a>'
				+ '<a class="withspace"  href="#" name="cancel">Cancel</a>';

		currentEntry.setData("progress", progbar);
		this.singleSelectDataTable.render();
	}
	this.tryAgain = function(fUId) {
		var i = this.getQueueFileIndexByNewId(this.errorQueue, fUId);
		if (i != -1) {
			//Move the file from error queue to upload queue
			var file = this.errorQueue[i];
			this.errorQueue.splice(i, 1);
			this.filesQueue.push(file);
			//Update the UI.
			var currentEntry = this.filesList[fUId];
			currentEntry["progress"] = "<div><div style='height:15px;width:100px;background-color:#CCC;float:left'></div><a href='#' class=\"withspace\"  name='cancel'>Cancel</a></div>";
			this.singleSelectDataTable.render();
		}
	}
	this.initEditUploader = function(divOverlay) {
		if(this.flashAvailable)
			this.editUploader = new YAHOO.widget.Uploader(divOverlay);
		else
			this.editUploader = new AjaxUploader();
		
		this.editUploader.caller = this;

		with (this) {
			// This can be removed and call.x can be used instead.
			editUploader.filesList = filesList;
			editUploader.filesQueue = filesQueue;
			editUploader.singleSelectDataTable = singleSelectDataTable;
			editUploader.objName = objName;
			editUploader.flashAvailable = flashAvailable;
			editUploader.imageHome = imageHome;
			if(this.flashAvailable){
				editUploader.addListener('fileSelect', onFileSelectEdit);
				editUploader.addListener('uploadProgress', onUploadProgressEdit);
				editUploader.addListener('uploadComplete', onUploadCompleteEdit);
				editUploader.addListener('uploadCompleteData', onUploadCompleteDataEdit);
				editUploader.addListener('uploadError', onUploadErrorEdit);
			}else{
				editUploader.completeUpload.subscribe(this.onEditUploadCompleteDataAjax, this);
			}
		}
	}

	this.onFileSelectEdit = function(event) {
		if (!this.singleSelectDataTable.lastSelectedRadioRecord) {
			alert('You need to select a record first.');
			event.elements[0].value = "";
			return;
		}
		if(this.flashAvailable){
			if ('fileList' in event && event.fileList != null) {
				var entry = event.fileList.file0;
	
				if (entry != null) {
					var requestFileId = this.singleSelectDataTable.lastSelectedRadioRecord.getData("requestFileId");
					entry.requestFileId = requestFileId;
					entry.newId = "1-" + entry.id;
					entry["isSelected"] = false;
					entry["progress"] = "<div><div style='height:15px;width:100px;background-color:#CCC;float:left'></div><a href='#' class='withspace' name='cancel'>Cancel</a></div>";
					var rs = this.singleSelectDataTable.getRecordSet();
	
					var idx = rs
							.getRecordIndex(this.singleSelectDataTable.lastSelectedRadioRecord);
					var record = rs.setRecord(entry, idx);
	
					this.singleSelectDataTable.render();
	
					var oldId = this.singleSelectDataTable.lastSelectedRadioRecord
							.getData("newId");
	
					// remove
					var i = this.caller.getQueueFileIndexByNewId(this.filesQueue,
							oldId);
					if (i != -1)
						this.filesQueue.splice(i, 1);
					this.filesQueue.push(entry);
					this.filesList[entry.newId] = record;
					
					this.singleSelectDataTable.lastSelectedRadioRecord = null;
				}
			}
		}else{
			var count = 0;
			for(var i in this.filesList)
				count++;
			var entry = new Array();
			var requestFileId = this.singleSelectDataTable.lastSelectedRadioRecord.getData("requestFileId");
			entry.requestFileId = requestFileId;
			entry["id"] = "file" + count;
			entry.newId = "1-" + entry.id;
			entry["isSelected"] = false;
			entry["progress"] = "<div><div style='height:15px;width:100px;background-color:#CCC;float:left'></div><a href='#' class='withspace' name='cancel'>Cancel</a></div>";
			entry["formObject"] = event;
			entry["name"] = event.elements[0].value;
			var rs = this.singleSelectDataTable.getRecordSet();
			
			var idx = rs.getRecordIndex(this.singleSelectDataTable.lastSelectedRadioRecord);
			var record = rs.setRecord(entry, idx);
			
			this.singleSelectDataTable.render();
			var oldId = this.singleSelectDataTable.lastSelectedRadioRecord.getData("newId");
			// remove
			var i = this.getQueueFileIndexByNewId(this.filesQueue,
					oldId);
			if (i != -1)
				this.filesQueue.splice(i, 1);
			
			this.filesQueue.push(entry);
			this.filesList[entry.newId] = record;
			
			this.singleSelectDataTable.lastSelectedRadioRecord = null;
			//this.uploader.Upload();
		}
	}

	this.onUploadProgressEdit = function(event) {
		this.caller.onUploadProgressBase(event, "1-");
	}

	this.onUploadCompleteEdit = function(event) {
		this.caller.onUploadCompleteBase(event, "1-");
	}

	this.onUploadCompleteDataEdit = function(event) {
		this.caller.onUploadCompleteDataBase(event, "1-");
	}

	this.onUploadErrorEdit = function(event) {
		this.caller.onUploadErrorBase(event, "1-");
	}

	/**
	 * 1. Cancel Upload
	 * 2. Remove from Queue
	 * 3. Remove from under process queue
	 * 4. Call removeFromTable
	 * @param fUId
	 * @return
	 */
	this.cancelUpload = function(fUId) {
		var entry = this.filesList[fUId];

		/* Cancel Uploading */
		this.getUploaderWrtId(fUId).cancel(entry.id);

		/* Remove from the files queue */
		var i = this.getQueueFileIndexByNewId(this.filesQueue, fUId);
		if (i != -1)
			this.filesQueue.splice(i, 1);

		// Remove from under process list
		i = this.getQueueFileIndexByNewId(this.underProcess, fUId);
		if (i != -1)
			this.underProcess.splice(i, 1);

		/* Remove from table */
		this.removeFromTable(fUId);
	}

	/**
	 * Removes from Table:
	 * 1. Remove from Uploader
	 * 2. Remove from FilesList
	 * 3. Remove from Table and render the table
	 * @param fUId
	 * @return
	 */
	this.removeFromTable = function(fUId) {
		var entry = this.filesList[fUId];

		// Remove file from uploader
		if(this.flashAvailable){
			var uploader = this.getUploaderWrtId(fUId);
			if (uploader)
				uploader.removeFile(this.getUploaderFileId(fUId));
		}
		if (entry == null) {
			return;
		}
		this.filesList[fUId] = null;

		var rs = this.singleSelectDataTable.getRecordSet();
		var ri = rs.getRecordIndex(entry);
		rs.deleteRecord(ri);
		this.isChanged = true;
		this.singleSelectDataTable.render();
	}

	this.doUpload = function() {
		if (!this.isInCall) {
			this.isInCall = true;

			try {
				// Process Queue
				while (this.filesQueue.length != 0) {
					var file = this.filesQueue.shift();
					this.underProcess.push(file);
					if(this.flashAvailable){
						var folderHintStr = encodeURI(document.getElementById('sysPrefix').value);
						var newUrl = this.uploaderUrl+"?folderhint=" + folderHintStr;
						var requestId = document.getElementById('requestId').value;
						if(requestId && (requestId.length != 0))
						{
							newUrl += "&requestid="+ requestId;
						}
						this.getUploaderWrtId(file.newId).upload(file["id"], newUrl, "POST", null, "Filedata");
					}else{
						this.getUploaderWrtId(file.newId).upload(file);
						//this.onUploadCompleteData(file);
					}
				}
			} finally {
				this.isInCall = false;
			}
		}
	}
	this.getUploaderWrtId = function(fUId) {
		if (fUId.indexOf('0-') == 0)
			return this.uploader;
		else if (fUId.indexOf('1-') == 0)
			return this.editUploader;
	}

	this.getUploaderFileId = function(fUId) {
		return fUId.substring(2);
	}

	this.getSelectedFiles = function() {
		var selectedFiles = [];
		//selectedFiles.added = [];
		//selectedFiles.updated = new Object();

		var rs = this.singleSelectDataTable.getRecordSet().getRecords();
		for ( var i = 0; i < rs.length; i++) {
			var record = rs[i];

			var requestFileId = record.getData("requestFileId");
			var repoFileId = record.getData("repoFileId");
			if ((requestFileId != null) && (repoFileId != null)) {
				var fileDetails = new Object();
				fileDetails.repoFileId = repoFileId;
				fileDetails.name = record.getData("name");
				fileDetails.requestFileId = requestFileId;
				try {
					fileDetails.size = parseInt(record.getData("size"));
				} catch (e) {
				}
				selectedFiles.push(fileDetails);
			}
		}
		return selectedFiles;
	}

	this.isCompleted = function() {
		if ((this.filesQueue.length == 0) && (this.underProcess.length == 0)) {
			return true;
		}
		return false;
	}

	this.init();
}

var FlashDetect = new function(){
    var self = this;
    self.installed = false;
    self.raw = "";
    self.major = -1;
    self.minor = -1;
    self.revision = -1;
    self.revisionStr = "";
    var activeXDetectRules = [
        {
            "name":"ShockwaveFlash.ShockwaveFlash.7",
            "version":function(obj){
                return getActiveXVersion(obj);
            }
        },
        {
            "name":"ShockwaveFlash.ShockwaveFlash.6",
            "version":function(obj){
                var version = "6,0,21";
                try{
                    obj.AllowScriptAccess = "always";
                    version = getActiveXVersion(obj);
                }catch(err){}
                return version;
            }
        },
        {
            "name":"ShockwaveFlash.ShockwaveFlash",
            "version":function(obj){
                return getActiveXVersion(obj);
            }
        }
    ];
    /**
     * Extract the ActiveX version of the plugin.
     * 
     * @param {Object} The flash ActiveX object.
     * @type String
     */
    var getActiveXVersion = function(activeXObj){
        var version = -1;
        try{
            version = activeXObj.GetVariable("$version");
        }catch(err){}
        return version;
    };
    /**
     * Try and retrieve an ActiveX object having a specified name.
     * 
     * @param {String} name The ActiveX object name lookup.
     * @return One of ActiveX object or a simple object having an attribute of activeXError with a value of true.
     * @type Object
     */
    var getActiveXObject = function(name){
        var obj = -1;
        try{
            obj = new ActiveXObject(name);
        }catch(err){
            obj = {activeXError:true};
        }
        return obj;
    };
    /**
     * Parse an ActiveX $version string into an object.
     * 
     * @param {String} str The ActiveX Object GetVariable($version) return value. 
     * @return An object having raw, major, minor, revision and revisionStr attributes.
     * @type Object
     */
    var parseActiveXVersion = function(str){
        var versionArray = str.split(",");//replace with regex
        return {
            "raw":str,
            "major":parseInt(versionArray[0].split(" ")[1], 10),
            "minor":parseInt(versionArray[1], 10),
            "revision":parseInt(versionArray[2], 10),
            "revisionStr":versionArray[2]
        };
    };
    /**
     * Parse a standard enabledPlugin.description into an object.
     * 
     * @param {String} str The enabledPlugin.description value.
     * @return An object having raw, major, minor, revision and revisionStr attributes.
     * @type Object
     */
    var parseStandardVersion = function(str){
        var descParts = str.split(/ +/);
        var majorMinor = descParts[2].split(/\./);
        var revisionStr = descParts[3];
        return {
            "raw":str,
            "major":parseInt(majorMinor[0], 10),
            "minor":parseInt(majorMinor[1], 10), 
            "revisionStr":revisionStr,
            "revision":parseRevisionStrToInt(revisionStr)
        };
    };
    /**
     * Parse the plugin revision string into an integer.
     * 
     * @param {String} The revision in string format.
     * @type Number
     */
    var parseRevisionStrToInt = function(str){
        return parseInt(str.replace(/[a-zA-Z]/g, ""), 10) || self.revision;
    };
    /**
     * Is the major version greater than or equal to a specified version.
     * 
     * @param {Number} version The minimum required major version.
     * @type Boolean
     */
    self.majorAtLeast = function(version){
        return self.major >= version;
    };
    /**
     * Is the minor version greater than or equal to a specified version.
     * 
     * @param {Number} version The minimum required minor version.
     * @type Boolean
     */
    self.minorAtLeast = function(version){
        return self.minor >= version;
    };
    /**
     * Is the revision version greater than or equal to a specified version.
     * 
     * @param {Number} version The minimum required revision version.
     * @type Boolean
     */
    self.revisionAtLeast = function(version){
        return self.revision >= version;
    };
    /**
     * Is the version greater than or equal to a specified major, minor and revision.
     * 
     * @param {Number} major The minimum required major version.
     * @param {Number} (Optional) minor The minimum required minor version.
     * @param {Number} (Optional) revision The minimum required revision version.
     * @type Boolean
     */
    self.versionAtLeast = function(major){
        var properties = [self.major, self.minor, self.revision];
        var len = Math.min(properties.length, arguments.length);
        for(i=0; i<len; i++){
            if(properties[i]>=arguments[i]){
                if(i+1<len && properties[i]==arguments[i]){
                    continue;
                }else{
                    return true;
                }
            }else{
                return false;
            }
        }
    };
    /**
     * Constructor, sets raw, major, minor, revisionStr, revision and installed public properties.
     */
    self.FlashDetect = function(){
        if(navigator.plugins && navigator.plugins.length>0){
            var type = 'application/x-shockwave-flash';
            var mimeTypes = navigator.mimeTypes;
            if(mimeTypes && mimeTypes[type] && mimeTypes[type].enabledPlugin && mimeTypes[type].enabledPlugin.description){
                var version = mimeTypes[type].enabledPlugin.description;
                var versionObj = parseStandardVersion(version);
                self.raw = versionObj.raw;
                self.major = versionObj.major;
                self.minor = versionObj.minor; 
                self.revisionStr = versionObj.revisionStr;
                self.revision = versionObj.revision;
                self.installed = true;
            }
        }else if(navigator.appVersion.indexOf("Mac")==-1 && window.execScript){
            var version = -1;
            for(var i=0; i<activeXDetectRules.length && version==-1; i++){
                var obj = getActiveXObject(activeXDetectRules[i].name);
                if(!obj.activeXError){
                    self.installed = true;
                    version = activeXDetectRules[i].version(obj);
                    if(version!=-1){
                        var versionObj = parseActiveXVersion(version);
                        self.raw = versionObj.raw;
                        self.major = versionObj.major;
                        self.minor = versionObj.minor; 
                        self.revision = versionObj.revision;
                        self.revisionStr = versionObj.revisionStr;
                    }
                }
            }
        }
    }();
};

var menuValue = 0;
function displayAttEditorInTable() {
	flashAvailable = FlashDetect.installed;
	var attsContainer = document.getElementById('filesEditorContainer');
	var val;	
	for ( var name in requestFiles) {
		val = requestFiles[name];
		
		if (typeof (val) == 'function')
			continue;
		var fieldEnvelope = document.createElement('div');
		
		// assemble
		attsContainer.appendChild(fieldEnvelope);

		// ids
		var labelEnvelop = name + 'labelEnvelop';
		var attachmentsNodeImg = name + 'attachmentsNodeImg';
		var uploaderContainer = name + 'uploaderContainer';
		var uploaderOverlay = name + 'uploaderOverlay';
		var selectFilesLink = name + 'selectFilesLink';
		var selectLink = name + 'selectLink';
		var uploaderEditOverlay = name + 'uploaderEditOverlay';
		var selectEditFilesLink = name + 'selectEditFilesLink';
		var selectEditLink = name + 'selectEditLink';
		var dataTableContainer = name + 'dataTableContainer';
		var formId = name + 'form';
		var fileInput = name + 'fileInput';
		var selectInput = name + 'selectInput';
		var selectButton = name + 'selectButton';
		var caption = val.fieldDisplayName;
		var pasteSelButton = name + "pasteSelButton";
		
		if(flashAvailable){
			var html = '<span id="' + labelEnvelop + '"><img src="/web/images/plus.gif" id="' + attachmentsNodeImg+ '" name="' + attachmentsNodeImg + '"> ' + caption + '</span>'
				+ '<div style="display: none;" class="filestable" id="'+ uploaderContainer + '">' 
				+ '	<div id="' + dataTableContainer
				+ '"></div>' + '<span id="' + uploaderOverlay
				+ '" style="position: absolute; z-index: 2"></span>'
				+ '	<span id="' + selectFilesLink + '" style="z-index: 1">'
				+ '<button id="' + selectLink
				+ '" href="#">Add Files...</button>' + '</span>'
				+ '<span id="' + uploaderEditOverlay
				+ '" style="position: absolute; z-index: 2"></span>'
				+ '	<span id="' + selectEditFilesLink + '" style="z-index: 1">'
				+ '<button id="' + selectEditLink
				+ '" href="#">Update Selected File...</button>'
				+ '</span>'
				+ '<input type="button" id="' + pasteSelButton + '" class="downloadbutton" style="margin-left:0px;" value="Paste Files" />'
				+ '</div>';
			fieldEnvelope.innerHTML = html;
		}else{
			var html = '<span id="' + labelEnvelop + '"><img src="/web/images/plus.gif" id="' + attachmentsNodeImg+ '" name="' + attachmentsNodeImg + '"> ' + caption + '</span>'
				+ '<div style="display: none;" class="filestable" id="'+ uploaderContainer + '">' 
				+ '<div id="' + dataTableContainer + '" style="margin:5px"></div>' 
				+ '<div id="'+ name +'menubuttons" style="display:inline; margin:5px;"></div>'
				+ '<span id="' + uploaderOverlay
				+ '" style="display:none; position: absolute; z-index: 2"></span>'
				+ '<span id="' + selectFilesLink + '" style="z-index: 1">'
				+ '</span>'
				+ '<input type="button" id="' + pasteSelButton + '" class="downloadbutton" style="margin-left:0px;" value="Paste Files" />'
				+ '</div>';
			fieldEnvelope.innerHTML = html;
			
			if(val.canAdd && val.canChange){
				menuValue = 0;
				YAHOO.util.Event.onContentReady(name + "menubuttons", function () { 
				    var onMenuItemClick = function (p_sType, p_aArgs, p_oItem) { 
				        var sText = p_oItem.cfg.getProperty("text"); 
				        menuValue = p_oItem.value;
				        oMenuButton.set("label", sText);
				    };
			    	var aMenuButtonMenu = [ 
				        { text: "Add Files", value: 0, onclick: { fn: onMenuItemClick } }, 
				        { text: "Edit Files", value: 1, onclick: { fn: onMenuItemClick } }
				    ]; 
			    	var oMenuButton = new YAHOO.widget.Button({ type: "menu", label: "Add Files", name: "menubutton", menu: aMenuButtonMenu, container: this }); 
				});  
			}
			else if(val.canAdd && !val.canChange){
				menuValue = 0;
				YAHOO.util.Event.onContentReady(name + "menubuttons", function () { 
				    var onMenuItemClick = function (p_sType, p_aArgs, p_oItem) { 
				        var sText = p_oItem.cfg.getProperty("text"); 
				        menuValue = p_oItem.value;
				        oMenuButton.set("label", sText);
				    };
			    	var aMenuButtonMenu = [ 
				        { text: "Add Files", value: 0, onclick: { fn: onMenuItemClick } }
				    ]; 
			    	var oMenuButton = new YAHOO.widget.Button({ type: "menu", label: "Add Files", name: "menubutton", menu: aMenuButtonMenu, container: this }); 
				});  
			}
			else if(!val.canAdd && val.canChange){
				menuValue = 1;
				YAHOO.util.Event.onContentReady(name + "menubuttons", function () { 
				    var onMenuItemClick = function (p_sType, p_aArgs, p_oItem) { 
				        var sText = p_oItem.cfg.getProperty("text"); 
				        menuValue = p_oItem.value;
				        oMenuButton.set("label", sText);
				    };
			    	var aMenuButtonMenu = [ 
				        { text: "Edit Files", value: 1, onclick: { fn: onMenuItemClick } } 
				    ]; 
			    	var oMenuButton = new YAHOO.widget.Button({ type: "menu", label: "Edit Files", name: "menubutton", menu: aMenuButtonMenu, container: this }); 
				});  
			}
			else if(!val.canAdd && !val.canChange){
				menuValue = -1;
			}
			
			var spanObject = document.getElementById(selectFilesLink);
			var form = document.createElement('form');
			
			form.id = formId;
			YAHOO.util.Dom.setStyle(form, "display", "inline");
			form.innerHTML = '<input id = "'+ selectLink +'" name="'+ fileInput + '" type = "file" />';
			spanObject.appendChild(form);
		}
		
		var uploader = tbitsUploader = new TbitsUploader(caption, selectLink,
				uploaderOverlay, selectEditLink, uploaderEditOverlay,
				dataTableContainer, val.files, 'tbitsUploader',
				YAHOO.transbit.tbits.contextPath + "web/images",
				YAHOO.transbit.tbits.contextPath + "uploader",
				YAHOO.transbit.tbits.contextPath + "web", val.canAdd, 
				val.canChange, flashAvailable);
		
		if(!flashAvailable){
			Event.on(selectLink, 'change', fileChangeHandler,{
				'uploader' : uploader,
				'formObject' : document.getElementById(formId)
			});
		}
		Event.on(labelEnvelop, 'click', labelClickhandler, {
			'img' : Dom.get(attachmentsNodeImg),
			'container' : Dom.get(uploaderContainer),
			'uploader' : uploader
		});
		uploaders[name] = uploader;
		
		if(val.canAdd){
			Event.on(pasteSelButton, 'click', onPasteClick, {
				'uploader' : uploader
			});
		}else document.getElementById(pasteSelButton).disabled = true;
	}
	setInterval(uploadCaller, 100);
	function uploadCaller() {
		for ( var name in uploaders) {
			var val = uploaders[name];
			if (typeof (val) == 'function')
				continue;
			val.doUpload();
		}
	}
}

function onPasteClick(event, args)
{
	var uploader = args.uploader;
	var responseSuccess = function(o){ 
		this.argument = o.responseText;
		if(this.argument.substr(0,6) == "Record")
			addToFileList(uploader, this.argument);
	}
	
	var responseFailure = function(o){ 
		alert("Could not paste");
	}
	
	var callback = 
	{ 
	  success:responseSuccess, 
	  failure:responseFailure,
	  argument:""
	}; 
	
	var sysPrefix = document.getElementById('sysPrefix').value;
	var url = YAHOO.transbit.tbits.contextPath + 'copy-paste/?sys_prefix=' + sysPrefix;
	var transaction = YAHOO.util.Connect.asyncRequest('POST', url, callback, null); 
}

function addToFileList(uploader, response){
	var records = response.split("Record");
	var i = 1;
	var max = 0;
	var temp;
	for(var i in uploader.filesList){
		if(i.substr(0,4) != "file")
			continue;
		temp = uploader.filesList[i].getData()["id"].substr(4)*1;
		if(temp > max)
			max = temp;
	}
	var entry = new Array();
	i = 1;
	while( i <= records.length) {
		var frags = records[i].split(',');
		var name = frags[1].split(':')[1];
		var size = frags[2].split(':')[1];
		var repoFileId = frags[3].split(':')[1];
		entry["id"] = "file" + (max + i-1);
		entry.newId = "0-" + entry.id;
		
		var existingEntry = uploader.filesList[entry.newId];
		if (!existingEntry) {
			entry["isSelected"] = false;
			entry["name"] = name;
			entry["size"] = size;
			entry["repoFileId"] = repoFileId;
			var progbar = '<img src="' + uploader.imageHome
				+ '/icon_done_small.gif" alt="Upload Finished."/>'
				+ '<a href="#" class="withspace" name="remove">Remove</a>';
			entry["progress"] = progbar;
			entry["requestFileId"] = "0";
			var record = uploader.singleSelectDataTable.getRecordSet().addRecord(entry);
			uploader.filesList[entry.newId] = record;
			uploader.singleSelectDataTable.render();
		}
		i++;
	}
}

function fileChangeHandler(e, o){
	if(o.formObject.elements[0].value != ""){
		if(menuValue == 0)
			o.uploader.onFileSelect(o.formObject);
		else o.uploader.onFileSelectEdit(o.formObject);
	}
}

function labelClickhandler(e, o)
{
	showHideNode(e, o);
	o.uploader.resizeUploadButtons();
}

function setTheAttachmentField()
{
	for(var name in requestFiles)
	{
		var val = requestFiles[name];
		if(typeof(val) == 'function')
			continue;	
		if( uploaders && uploaders[name])
		{
			var newFiles = uploaders[name].getSelectedFiles();
			val.files = newFiles;
		}
	}
	var fixedAttachmentsInput = document.getElementById("attachments");
	if(fixedAttachmentsInput != null)
	{
		var json = YAHOO.lang.JSON.stringify(requestFiles);
		//json = json.substring(1,json.length - 1)		
		fixedAttachmentsInput.setAttribute('value', json);
	}
}
function isUploading()
{
	var isUploadingFlag = false;
	for(var name in uploaders)
	{
		var uploader = uploaders[name];
		if(typeof(uploader) == 'function')
			continue;
				
		if(!uploader.isCompleted())
		{
			isUploadingFlag = true;
			break;
		}
	}
	return isUploadingFlag;
}
