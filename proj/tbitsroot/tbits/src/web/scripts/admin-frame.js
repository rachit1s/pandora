YAHOO.namespace("example.container");
var allToBeWaited = [ 'settings', 'bas', 'menu' ];
var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;

function showLoadingPanel() {
	if (!YAHOO.example.container.wait) {

		// Initialize the temporary Panel to display while waiting for external content to load

		YAHOO.example.container.wait = new YAHOO.widget.Panel("wait", {
			width :"250px",
			fixedcenter :true,
			close :false,
			draggable :false,
			zindex :4,
			modal :true,
			visible :false
		});

		YAHOO.example.container.wait.setHeader("Loading tBits, please wait...");
		YAHOO.example.container.wait
				.setBody("<img src=\"http://l.yimg.com/a/i/us/per/gr/gp/rel_interstitial_loading.gif\"/>");
		YAHOO.example.container.wait.render(document.body);
	}
	// Show the Panel
	YAHOO.example.container.wait.show();
}

function getUrlParams(href) {
	var urlParts = new Object();

	var hashIndex = href.indexOf('#');
	if (hashIndex > -1) {
		var subUrl = href.slice(hashIndex + 1);
		var parts = subUrl.split('?');
		var pageName = parts[0];
		var queryString = parts[1];
		var vars = [], hash;
		if (queryString != undefined) {
			var hashes = queryString.split('&');
			for ( var i = 0; i < hashes.length; i++) {
				hash = hashes[i].split('=');
				vars.push(hash[0]);
				vars[hash[0]] = hash[1];
			}
		}
		urlParts.vars = vars;
		urlParts.pageName = pageName;
	}
	return urlParts;
}

function selectBA(ba) {
	var baElement = document.getElementById('baselect');
	var found = -1;
	for ( var i = 0; i < baElement.options.length; i++) {
		var opt = baElement.options[i];
		if (opt.value == ba) {
			found = i;
			break;
		}
	}
	if (found > -1) {
		baElement.selectedIndex = found;
	}
}

function selectFirstBA() {
	var baElement = document.getElementById('baselect');
	if (baElement.options.length > 1) {
		baElement.selectedIndex = 1;
	}
}

function getSysPrefix() {
	var baElement = document.getElementById('baselect');
	if (baElement.selectedIndex == 0) {
		return null;
	}
	var selectedBA = baElement.options[baElement.selectedIndex].value;
	return selectedBA;
}

function selectPage(pageName) {
	var f = ( function(e) {
		if (e.name == pageName)
			return true;
		return false;
	});
	var menus = YAHOO.util.Dom.getElementsBy(f, 'a', 'toc');
	if(menus.length > 0)
	{
		var aMenu = menus[0];
		openMenuItem(aMenu);
		return true;
	}
	return false;
}

function reopenCurrentPage()
{
	var fn = function(el){
		if(Dom.hasClass(el, 'selected'))
		{
			return true;
		}
		return false;
	}
	var currentMenu = Dom.getElementsBy(fn, 'li', 'toc');
	if( (currentMenu != undefined) && (currentMenu.length > 0))
	{
		var a = currentMenu[0].firstChild;
		if(a != undefined) 
		{
			openMenuItem(a);
		}
	}
}
function selectFirstPage() {
	// TODO Auto-generated function stub
	var elems = Dom.getElementsBy(function(e){return true;}, 'a', 'toc');
	if( (elems != undefined) && (elems.length > 0))
	{
		openMenuItem(elems[0]);
	}
}
function loadDefaultSettings() {
	//URL: http://localhost:8080/app/web/index.html#page.name?q=ba
	var href = window.location.href;
	var urlParts = getUrlParams(href);
	var pageName = urlParts.pageName;
	var ba;
	if(urlParts.vars)
		ba = urlParts.vars['q'];
	
	if (ba != undefined) {
		selectBA(ba);
	} else
		selectFirstBA();

	if ((pageName == undefined) || !selectPage(pageName)) 
		selectFirstPage();

	//Now tie the 
	//change BA event to page selection refresh
	Event.on('baselect', 'change', reopenCurrentPage);
	//if page is null use cookies

	//if page is still null
	//get the first menu item and load it if there are some business areas
}


function onFinishedLoadingMainPage() {
	YAHOO.example.container.wait.hide();
	loadDefaultSettings();
}
function hideLoading(name) {
	var found = -1;
	for ( var i = 0; i < allToBeWaited.length; i++) {
		if (allToBeWaited[i] == name) {
			found = i;
			break
		}
	}
	if (found > -1) {
		allToBeWaited.splice(found, 1);
	}
	if (allToBeWaited.length == 0) {
		onFinishedLoadingMainPage();
	}
}

function loadBAs(bas) {
	var dropDown = document.getElementById("baselect");
	dropDown.options.length = 0;
	dropDown.options[0] = new Option("Select a BA");
	for ( var i = 0, len = bas.length; i < len; ++i) {
		var ba = bas[i];
		var name = ba.name;
		var text = ba.displayName + ' [' + name + ']';
		dropDown.options[i + 1] = new Option(text, name, true, true);
	}
}
function loadSettings(settings) {
	var miniinfo = document.getElementById('miniinfo');
	var infoStr = settings.userLogin;
	if (settings.displayLogout) {
		infoStr += ' | ' + '<a href="' + settings.contextPath + '/logout">Logout</a>';
	}
	miniinfo.innerHTML = infoStr;
}
function retrieveAndLoadSettings() {
	var handleSuccess = function(o) {
		if (o.responseText !== undefined) {
			var jsonString = o.responseText;
			try {
				var settings = YAHOO.lang.JSON.parse(jsonString);
				loadSettings(settings);
			} catch (e) {
				//alert("Invalid product data");
			} finally {
				hideLoading('settings');
			}
		}
	}

	var handleFailure = function(o) {
		if (o.responseText !== undefined) {
			//			div.innerHTML = "<li>Transaction id: " + o.tId + "</li>";
			//			div.innerHTML += "<li>HTTP status: " + o.status + "</li>";
			//			div.innerHTML += "<li>Status code message: " + o.statusText + "</li>";
		}
		hideLoading('settings');
	}

	var callback = {
		success :handleSuccess,
		failure :handleFailure,
		argument : {
			foo :"foo",
			bar :"bar"
		}
	};
	var sUrl = "../getsettings.adminwebutils";
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
}
function retrieveAndloadBAs() {
	var handleSuccess = function(o) {
		if (o.responseText !== undefined) {
			var jsonString = o.responseText;
			try {
				var bas = YAHOO.lang.JSON.parse(jsonString);
				loadBAs(bas);
			} catch (e) {
				//alert("Invalid product data");
			} finally {
				hideLoading('bas');
			}
		}
	}

	var handleFailure = function(o) {
		if (o.responseText !== undefined) {
			//			div.innerHTML = "<li>Transaction id: " + o.tId + "</li>";
			//			div.innerHTML += "<li>HTTP status: " + o.status + "</li>";
			//			div.innerHTML += "<li>Status code message: " + o.statusText + "</li>";
		}
		hideLoading('bas');
	}

	var callback = {
		success :handleSuccess,
		failure :handleFailure,
		argument : {
			foo :"foo",
			bar :"bar"
		}
	};
	var sUrl = "../getbalist.adminwebutils";
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
}
function loadMenuInDiv(menu, div, isAppMenu) {
	if (div.hasChildNodes()) {
		while (div.childNodes.length >= 1) {
			div.removeChild(div.firstChild);
		}
	}

	for ( var i = 0; i < menu.length; i++) {
		var menuItem = menu[i];
		var li = document.createElement("li");
		if (menuItem.isSelected == true) {
			YAHOO.util.Dom.addClass(li, 'selected');
		} else {
			YAHOO.util.Dom.addClass(li, 'item');
		}

		var a = document.createElement("a");
		a.title = menuItem.description;
		a.href = '#' + menuItem.url;
		a.innerHTML = menuItem.caption;
		a.name = menuItem.url;
		
		li.appendChild(a);
		div.appendChild(li);
		if (isAppMenu) {
			YAHOO.util.Event.on(a, "click", menuClickEventHandler);
		} else {
			YAHOO.util.Event.on(a, "click", menuClickEventHandler);
		}
		var img = document.createElement("img");
		img.src = 'images/loading.gif';
		Dom.setStyle(img, 'display', 'none');
		Dom.setStyle(img, 'height', '12px');
		Dom.setStyle(img, 'width', '12px');
		Dom.setStyle(img, 'margin-left', '5px');
		Dom.setStyle(img, 'vertical-align', 'middle');
		a.appendChild(img);
	}
}

function openPanel(url) {
	/*
	 * The basic idea is to convert 
	 * 		http://server/webcontext/index.html#subref -> http://server/webcontext/subref
	 * 		http://server/webcontext/servlet/#subref -> http://server/webcontext/servlet/subref
	 */
	
	var hashIdx = url.indexOf('#');
	var slashIdx = url.lastIndexOf('/', hashIdx);
	var finalUrl = url.substring(0, slashIdx + 1) + url.substring(hashIdx + 1);
	
	var frame = document.getElementById("subpageframe");
	frame.src = finalUrl;
	return frame;
}

function menuClickEventHandler(e)
{
	var menuItem = YAHOO.util.Event.getTarget(e);
	openMenuItem(menuItem);
}

/*
 * Opens the link corresponding to the menu item clicked. 
 * It takes care of the switching between the ba or app menu.
 */
function openMenuItem(menuItem)
{
	showLoadingOnly(menuItem.parentNode);
	
	var suffix = '';
	var isBAMenu = Dom.isAncestor(document.getElementById('bamenu'), menuItem);
	if(isBAMenu)
	{
		var selectedBA = getSysPrefix();
		if (selectedBA == null) {
			alert('Please select a business area.');
			var baElement = document.getElementById('baselect');
			baElement.focus();
			return;
		}
		suffix = "?q=" + selectedBA;
	}
	
	var url = menuItem.name + suffix;
	menuItem.href = '#' + menuItem.name +  suffix;
	var frame = openPanel(url);
	
	selectOnlyMenuItem(menuItem.parentNode);
	document.getElementById('pagetitle').text = 'tBits - Administration - ' + menuItem.text;
}

function showLoadingOnlyOnMenu(node, menu) {
	for ( var i = 0; i < menu.childNodes.length; i++) {
		var li = menu.childNodes[i];
		var imgMatcher = function(img) {
			return true;
		};
		var img = Dom.getElementsBy(imgMatcher, 'img', li);

		if (li == node) {
			Dom.setStyle(img, 'display', 'inline');
		} else {
			Dom.setStyle(img, 'display', 'none');
		}
	}
}

function showLoadingOnly(node) {
	var appMenu = document.getElementById('appmenu');
	var baMenu = document.getElementById('bamenu');
	showLoadingOnlyOnMenu(node, appMenu);
	showLoadingOnlyOnMenu(node, baMenu);
}

function hideSubPageWait() {
	showLoadingOnly(null);
}

function selectOnlyChildren(node, menu) {
	for ( var i = 0; i < menu.childNodes.length; i++) {
		var li = menu.childNodes[i];
		if (li == node) {
			YAHOO.util.Dom.replaceClass(li, 'item', 'selected');
		} else {
			YAHOO.util.Dom.replaceClass(li, 'selected', 'item');
		}
	}
}
function selectOnlyMenuItem(node) {
	var appMenu = document.getElementById('appmenu');
	var baMenu = document.getElementById('bamenu');
	selectOnlyChildren(node, appMenu);
	selectOnlyChildren(node, baMenu);
}

function loadMenu(menu) {
	var appMenu = document.getElementById("appmenu");
	var baMenu = document.getElementById("bamenu");
	loadMenuInDiv(menu.AppMenu, appMenu, true);
	loadMenuInDiv(menu.BAMenu, baMenu, false);
}

function retrieveAndLoadMenu() {
	var handleSuccess = function(o) {
		if (o.responseText !== undefined) {
			var jsonString = o.responseText;
			try {
				var menu = YAHOO.lang.JSON.parse(jsonString);
				loadMenu(menu);

			} catch (e) {
				//alert("Invalid product data: " + e);
			} finally {
				hideLoading('menu');
			}
		}
	}

	var handleFailure = function(o) {
		if (o.responseText !== undefined) {
			//			div.innerHTML = "<li>Transaction id: " + o.tId + "</li>";
			//			div.innerHTML += "<li>HTTP status: " + o.status + "</li>";
			//			div.innerHTML += "<li>Status code message: " + o.statusText + "</li>";
		}
		hideLoading('menu');
	}

	var callback = {
		success :handleSuccess,
		failure :handleFailure,
		argument : {
			foo :"foo",
			bar :"bar"
		}
	};
	var sUrl = "../getmenu.adminwebutils";
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
}

var iframeids = [ "subpageframe" ]

//Should script hide iframe from browsers that don't support this script (non IE5+/NS6+ browsers. Recommended):
var iframehide = "yes"

var getFFVersion = navigator.userAgent.substring(
		navigator.userAgent.indexOf("Firefox")).split("/")[1]
var FFextraHeight = parseFloat(getFFVersion) >= 0.1 ? 16 : 0
//extra height in px to add to iframe in FireFox 1.0+ browsers

function resizeCaller() {
	var dyniframe = new Array();
	for (i = 0; i < iframeids.length; i++) {
		if (document.getElementById)
			resizeIframe(iframeids[i])
			//reveal iframe for lower end browsers? (see var above):
		if ((document.all || document.getElementById) && iframehide == "no") {
			var tempobj = document.all ? document.all[iframeids[i]] : document
					.getElementById(iframeids[i])
			tempobj.style.display = "block"
		}
	}
}

function resizeIframe(frameid) {
	var currentfr = document.getElementById(frameid);
	docBody.style.width = initDocBodyWidth + 'px';
	var docBodyWidth = 0;
	if (currentfr && !window.opera) {
		currentfr.style.display = "block";
		if(currentfr.contentDocument){
			docBodyWidth = 400 + parseInt(currentfr.contentDocument.body.offsetWidth);
		}else{
			docBodyWidth = 250 + currentfr.Document.body.scrollWidth;
		}
		docBody.style.width = docBodyWidth + 'px';
		
		if (currentfr.contentDocument && currentfr.contentDocument.body.offsetHeight){ //ns6 syntax
			currentfr.height = currentfr.contentDocument.body.offsetHeight
					+ FFextraHeight;
		}
		else if (currentfr.Document && currentfr.Document.body.scrollHeight){ //ie5+ syntax
			currentfr.height = currentfr.Document.body.scrollHeight;
		}
		if (currentfr.addEventListener)
			currentfr.addEventListener("load", readjustIframe, false)
		else if (currentfr.attachEvent) {
			currentfr.detachEvent("onload", readjustIframe) // Bug fix line
			currentfr.attachEvent("onload", readjustIframe)
		}
	}
}

function readjustIframe(loadevt) {
	var crossevt = (window.event) ? event : loadevt
	var iframeroot = (crossevt.currentTarget) ? crossevt.currentTarget
			: crossevt.srcElement
	if (iframeroot)
		resizeIframe(iframeroot.id);
}

function subPageFrameLoaded() {
	hideSubPageWait();
	resizeCaller();
}