YAHOO.namespace("tbits.transmittalpage");

//Input the IDs of the IFRAMES you wish to dynamically resize to match its content height:
//Separate each ID with a comma. Examples: ["myframe1", "myframe2"] or ["myframe"] or [] for none:
var iframeids=["tranmsittalpage"];

//Should script hide iframe from browsers that don't support this script (non IE5+/NS6+ browsers. Recommended):
var iframehide="yes";

var getFFVersion=navigator.userAgent.substring(navigator.userAgent.indexOf("Firefox")).split("/")[1]
var FFextraHeight=parseFloat(getFFVersion)>=0.1? 16 : 0; //extra height in px to add to iframe in FireFox 1.0+ browsers

function resizeCaller() {
var dyniframe=new Array();
	for (i=0; i<iframeids.length; i++){
		if (document.getElementById)
			resizeIframe(iframeids[i]);
		//reveal iframe for lower end browsers? (see var above):
		if ((document.all || document.getElementById) && iframehide=="no"){
			var tempobj=document.all? document.all[iframeids[i]] : document.getElementById(iframeids[i]);
			tempobj.style.display="block";
		}
	}
}

function resizeIframe(frameid){
	var currentfr=document.getElementById(frameid);
	if (currentfr && !window.opera){
		currentfr.style.display="block";
		if (currentfr.contentDocument && currentfr.contentDocument.body.offsetHeight) //ns6 syntax
			currentfr.height = currentfr.contentDocument.body.offsetHeight+FFextraHeight; 
		else if (currentfr.Document && currentfr.Document.body.scrollHeight) //ie5+ syntax
			currentfr.height = currentfr.Document.body.scrollHeight;
		if (currentfr.addEventListener)
			currentfr.addEventListener("load", readjustIframe, false)
		else if (currentfr.attachEvent){
			currentfr.detachEvent("onload", readjustIframe); // Bug fix line
			currentfr.attachEvent("onload", readjustIframe);
		}
	}
}

function readjustIframe(loadevt) {
	var crossevt=(window.event)? event : loadevt;
	var iframeroot=(crossevt.currentTarget)? crossevt.currentTarget : crossevt.srcElement;
	if (iframeroot)
		resizeIframe(iframeroot.id);
}

function loadintoIframe(iframeid, url){
	if (document.getElementById)
		document.getElementById(iframeid).src=url;
}

if (window.addEventListener)
	window.addEventListener("load", resizeCaller, false);
else if (window.attachEvent)
	window.attachEvent("onload", resizeCaller);
else
	window.onload=resizeCaller();

function showTransmittalPanel(transmittalUrl){
	
	var iframeParentEl = document.getElementById("transmittalpanel");
	iframeParentEl.style.display='block';
	
	var iFrame = document.getElementById("transmittalPage");	
	iFrame.src = transmittalUrl;
	
	var tPanel = YAHOO.tbits.transmittalpage.transmittalpanel;
	tPanel.cfg.queueProperty("visible", true);
	tPanel.render();
	tPanel.center();
}

function initTransmittalPage(){
	// Create a panel Instance, from the 'resizablepanel' DIV standard module markup
	var ht = 551;//document.documentElement.clientHeight - 40;
	var wd = document.documentElement.clientWidth - 40;
	
	// QUIRKS FLAG, FOR BOX MODEL
	var IE_QUIRKS = (YAHOO.env.ua.ie && document.compatMode == "BackCompat");
	
	// UNDERLAY/IFRAME SYNC REQUIRED
	var IE_SYNC = (YAHOO.env.ua.ie == 6 || (YAHOO.env.ua.ie == 7 && IE_QUIRKS));
	
	YAHOO.tbits.transmittalpage.transmittalpanel = new YAHOO.widget.Panel('transmittalpanel', {
		draggable: true,
		width: wd + "px",
		height: ht + "px",
		autofillheight: "body",
		constraintoviewport:true,
		fixedcenter: false,
		visible: false,
		modal:true,
		close:true
	});	


	// Create Resize instance, binding it to the 'resizablepanel' DIV 
	var resize = new YAHOO.util.Resize('transmittalpanel', {
		handles: ['br'],
		autoRatio: false,
		minWidth: 300,
		minHeight: 100,
		status: false 
	});

	var tPanel = YAHOO.tbits.transmittalpage.transmittalpanel;
	// Setup startResize handler, to constrain the resize width/height
	// if the constraintoviewport configuration property is enabled.
	resize.on("startResize", function(args) {
								if (this.cfg.getProperty('constraintoviewport')) {
									var D = YAHOO.util.Dom;

									var clientRegion = D.getClientRegion();
									var elRegion = D.getRegion(this.element);

									resize.set("maxWidth", clientRegion.right - elRegion.left - YAHOO.widget.Overlay.VIEWPORT_OFFSET);
									resize.set("maxHeight", clientRegion.bottom - elRegion.top - YAHOO.widget.Overlay.VIEWPORT_OFFSET);
								} else {
									resize.set("maxWidth", null);
									resize.set("maxHeight", null);
								}
	}, tPanel, true);

	// Setup resize handler to update the Panel's 'height' configuration property 
	// whenever the size of the 'transmittalpanel' DIV changes.

	// Setting the height configuration property will result in the 
	// body of the Panel being resized to fill the new height (based on the
	// autofillheight property introduced in 2.6.0) and the iframe shim and 
	// shadow being resized also if required (for IE6 and IE7 quirks mode).
	resize.on("resize", function(args) {
			var panelHeight = args.height;
			this.cfg.setProperty("height", panelHeight + "px");
		},tPanel, true);
	
	tPanel.render();
	
	var closeEl = YAHOO.util.Dom.getElementsByClassName(
			"container-close", null, tPanel.element)[0];
	YAHOO.util.Event.on(closeEl, "click", function(){
		var iframeEl = document.getElementById("transmittalpanel");
		iframeEl.style.display='none';
	}); 
}