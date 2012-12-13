var nearestPathServ;
function Dashboard( c)		// c->no. of cols.
{
	var gManDiv;	// Division in the Gadget Manager(Add Gadget on top) where
	// all the divisions
	// that can be added appear
	var already ; // this variale tells how many rows are already present in a
	// col. this
	// helps us to assign row no. to the next Gadget which needs
	// to be added
	var colno;	 // Total number of cols in the Dashboard
	this.makeDashboard(c);	// will make the basic outline of the Dashboard
	/*
	 * It consists of following functions: makeDashboard(c)... makes basic
	 * Dashboard outline with c number of cols addGadgetManager()...adds Gadget
	 * Manager( division on the top used to add Gadgets) to the Dashboard
	 * addGadget(..)....adds a Gadget with specified Parameters to the Dashboard
	 * getInfo()....returns the info of the Dashboard, all gadgets, which Gadget
	 * is minimised, closed etc...used to post the Dashboard info.
	 */
}

function GadgetManager()
{
	var TimeoutId;	// Array-Stores the refresh timer id fr each Gadget
	var fid;		// Array-Stores the file id fr file in each Gadget
	var divFrDialog;	// Division whr all the parameter dialogs are appended.
	var title;			// Array-Stores the headings of all the Gadgets.
	var dialog;			// Array-Stores the Parameter dialog divs fr all each of
	// the Gadgets
	var RefreshRate;	// Array- Stores the refresh Rates of each of the
	// Gadgets
	var height; // this Array stores the heights of all the Gadgets,
	// helps in animating minimise and maximise
	/*
	 * This consists of following functions: addPar(i,j,fileName)...adds the
	 * Parameters of the report(fileName) as a form InitPar(i,j)...initialises
	 * the dialog boxes fr Parameters, converts already created form to a Dialog
	 * box makeRefButton(i,j)...makes Refresh Button of a Gadget add(i,j)...this
	 * adds the closed Gadget given by (i,j) in the add gadget menu on the top,
	 * to the page cloose(i,j)...closes a Gadget minimise(i,j)...minimises a
	 * Gadget maximise(i,j)...Maximises a Gadget showinGadget()... makes the Add
	 * Gadget menu visible refreshGadget(i,j)... Refreshes a Gadget
	 * changeRefRate(i,j, newRR).... changes the refresh rate of a Gadget to
	 * newRR Note that here everywhere (i,j) refers to a Gadget i->initial col.
	 * j->initial row. So, every Gadget can be uniquely identified by its i,j
	 * values The Naming Convention of the divisions in the page also uses this.
	 */
}


var cb =
{
		success: function(o)
		{
		//alert("done!");
		},
		failure: function(o)
		{
		//alert("no");// document.write("failed!");
		}
};
			
var g;

function getInfoDash()			// gets info frm the Dashboard and posts it
{	
	//var info = [];
	var info = g.getinfo();
	var infojson = info.toJSONString();
 	var infojson1 = "persist_url=false&postType=0&id="+infojson;
	
 	var pageLink = document.location.href;			// to generate the sURL
	var linkParts = pageLink.split("/");				// removes part after
														// last / and
												// puts DashServlet instead to
												// generate apt url
																										
	var sUrlPrefix = "";
	var i = 0;
	for(i = 0;i<linkParts.length-1;i++)		
	{	
	sUrlPrefix = sUrlPrefix+linkParts[i]+"/";
	}
	var sUrl = nearestPathServ+"DashServlet";
	var request = YAHOO.util.Connect.asyncRequest('POST', sUrl,cb,infojson1);
	
}

var callback =   
{   		
	  success:
		function(o)
		{
			try
			{
				g = new Dashboard(2);	// create dashboard of 2 columns
	
				 var prod = YAHOO.lang.JSON.parse(o.responseText);
				var i = prod[0];
				var j,count = 7;
				var col;
				var visible;
				var name;
				var fname;
				var RRate;
				for(j = 0;j<i;j++)
				{
		
					col = prod[count*j+1];
					visible = prod[count*j+2];
					min = prod[count*j+3];
					name = prod[count*j+4];
					fname = prod[count*j+5];
					RRate = prod[count*j+6];
					fid = prod[count*j+7];
					g.addGadget(fid,col,name,fname,visible,min,RRate);
			
				}
				initDD();		// to initialise drag and drop
			}
			catch(e)
			{
				alert("Exception caught in callback success function of dashboard : e = " + e );
			}	
		},  
	failure: 
		function(o)
		{
		}
};

var divtoAddDash;
function getInfoServer(divId, nearestPath)	// gets info frm the Server and	loads the page
{
	divtoAddDash = document.getElementById(divId);
	var uid = 1;
	var pageLink = document.location.href;
	var linkParts = pageLink.split("/");
	// whats the use of this var?
	var sUrlPrefix = "";
	var i = 0;
	for(i = 0;i<linkParts.length-1;i++)
	{	
		sUrlPrefix = sUrlPrefix+linkParts[i]+"/";
	}
	nearestPathServ = nearestPath;
	sUrl = nearestPath + "DashServlet?persist_url=false&id=0&uid=1";
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback,null);
}



//Initialises the Parameter Dialog, converts the already made form to a dialog.
GadgetManager.initPar = function(i,j) {
	var handleSubmit = function() {
		this.submit();
	};
	var handleCancel = function() {
		this.cancel();
	};

	var handleSuccess = function(o) {
		var prod = YAHOO.lang.JSON.parse(o.responseText);
		var response = prod[1];
		if(prod[0] == 1)
		{
			alert(response);
			GadgetManager.dialog[i][j].show();
		}
		else{
			GadgetManager.refreshGadget(i, j);
		}
	};
	var handleFailure = function(o) {
		alert("Submission failed: " + o.status);
	};
	GadgetManager.dialog[i][j] = new YAHOO.widget.Dialog("dialog"+i+"_"+j, 
			{ width : "30em",
		fixedcenter : true,
		visible : false, 
		constraintoviewport : true,
		buttons : [ { text:"Submit", handler:handleSubmit, isDefault:true },
		            { text:"Cancel", handler:handleCancel } ]
			});
	GadgetManager.dialog[i][j].validate = function() {
		return true;
	};

	// Wire up the success and failure handlers
	GadgetManager.dialog[i][j].callback = { success: handleSuccess,
			failure: handleFailure };

	// Render the Dialog
	GadgetManager.dialog[i][j].render();
};


var callbackPar =   
{   
		success:function(o)
		{	
	var prod = YAHOO.lang.JSON.parse(o.responseText);
	var x = document.createElement("div");
	GadgetManager.divFrDialog.appendChild(x);

	var iBody = prod[0];
	var jBody = prod[1];
	x.id = "dialog"+iBody+"_"+jBody;
	x.className = "dialog";
	var y = document.createElement("div");
	y.className = "hd";
	y.innerHTML = "Parameters-"+GadgetManager.title[iBody][jBody];
	x.appendChild(y);
	var z =  document.createElement("div");
	z.className = "bd";
	x.appendChild(z);
	var form = document.createElement("form");
	form.method = "POST";
	var a = document.getElementById("li"+iBody+"_"+jBody);
	var b = a.firstChild.nextSibling;
	var c = b.firstChild.nextSibling.nextSibling.nextSibling.nextSibling;
	c.style.display = "";
	var d = c.nextSibling;
	d.style.display = "none";
	body = b.nextSibling;
	c = body.nextSibling;
	var fname = c.innerHTML;
	var pageLink = document.location.href;
	var linkParts = pageLink.split("/");
	var sUrlPrefix = "";
	var k = 0;
	for(k = 0;k < linkParts.length-1;k++)
	{	
		sUrlPrefix = sUrlPrefix+linkParts[k]+"/";
	}
	form.action = nearestPathServ +"DashServlet?persist_url=false&postType=2&fname="+fname;


	z.appendChild(form);
	var currstring;
	var j = 0;
	var i = 2;
	var isGroup = 0;
	currstring = prod[0];
	while(prod[i]!= "$")
	{
		j++;
		if(prod[i] == "#")
		{
			if(isGroup === 0)
			{
				isGroup = 1;
				i++;
				var label = document.createElement("label");
				label.innerHTML = prod[i++]+":<br><br>";
				form.appendChild(label);

			}
			else
			{
				isGroup = 0;
				i++;
				if(prod[i] == "$")
				{	break;}
			}
		}

		label = document.createElement("label");
		if(isGroup === 0)
			{label.innerHTML = prod[i++]+":<br> &nbsp &nbsp &nbsp";}
		else
			{label.innerHTML = "&nbsp &nbsp &nbsp "+prod[i++]+":<br>&nbsp&nbsp &nbsp&nbsp &nbsp &nbsp &nbsp ";}

		form.appendChild(label);
		if(prod[i] == "TextB")
		{
		var	ip = document.createElement("input");
		ip.type = "text";
		ip.name = prod[i-1];
		ip.value = prod[i+1];
		i = i+3;
		// i=i+2;
		form.appendChild(ip);
		label = document.createElement("label");
		label.innerHTML = "<br>";
		form.appendChild(label);

		} 

		else if(prod[i] == "RadioB")
		{
			i++;
			var name = prod[i-2];
			while(prod[i] != '$')
			{
				ip = document.createElement("input");
				ip.type = "radio";
				ip.name = name;
				ip.value = prod[i];
				i++;
				if(prod[i]=="1")
					{ip.checked = "checked";}
				form.appendChild(ip);


				var label = document.createElement("label");
				if(isGroup == 0)
					label.innerHTML = prod[i-1]+"<br> &nbsp &nbsp &nbsp";
				else
					label.innerHTML = prod[i-1]+"<br> &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp ";
				form.appendChild(label);
				i++;
			}

			i++;
		}
		else if(prod[i] == "ListB")
		{
			i++;
			var	ipSel = document.createElement("select");
			ipSel.name = prod[i-2];
			form.appendChild(ipSel); 
			while(prod[i] != '$')
			{
				var	ip = document.createElement("option");
				ip.value = prod[i];
				ip.innerHTML = prod[i];
				i++;
				if(prod[i]=="1")
				ip.selected = "selected";
				ipSel.appendChild(ip);
				i++;			
				
			}

			i++;
			var label = document.createElement("label");
			label.innerHTML = "<br>";
			form.appendChild(label);
		}

		var label = document.createElement("label");
		label.innerHTML = "<br>";
		form.appendChild(label);

	}
	YAHOO.util.Event.onContentReady("dialog"+i+"_"+j,GadgetManager.initPar(iBody,jBody));
		},  
		failure: function(o)
		{
		}
}

//Fetches the parameter of the Reports of a Gadget and adds it to a form.
GadgetManager.addPar = function (i,j,fname)
{
	var pageLink = document.location.href;
	var linkParts = pageLink.split("/");
	var sUrlPrefix = "";
	var k = 0;
	for(k = 0;k < linkParts.length-1;k++)
	{	
		sUrlPrefix = sUrlPrefix+linkParts[k]+"/";
	}
	var sUrl = nearestPathServ+"DashServlet?persist_url=false&id=2&i="+i+"&j="+j+"&fname="+fname;

	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackPar,null);
};


//initialises the refresh buttons of the Gadgets, the divisions of menu are made along with
// the Gadget itself, this function just initialises it as a division.
GadgetManager.MakeRefButton = function(i,j)
{	
	var oMenuButton;
	YAHOO.util.Event.onContentReady("menuButton"+i+"_"+j, function () {
		oMenuButton = new YAHOO.widget.Button("menuButton"+i+"_"+j, {    
			type: "menu",    
			menu: "menuButton"+i+"_"+j+"select" });  

		var onMenuClick = function (p_sType, p_aArgs) {
			var oEvent = p_aArgs[0],
			oMenuItem = p_aArgs[1];
			if(oMenuItem.value != -1)
				{GadgetManager.changeRefRate(i,j,oMenuItem.value);}
			else
			{
				GadgetManager.dialog[i][j].show();
			}

		};

		oMenuButton.getMenu().subscribe("click", onMenuClick);

	});

};



var gcount = 0;	// no.of Gadgets in the Dashboard.

GadgetManager.add = function(i,j)		// this adds the closed Gadget given by
// (i,j) in the add gadget menu on the top, to the page
{	

	var e = document.getElementById('li' + i + '_' + j);
	e.style.display = "";
	var list = Dashboard.gManDiv.childNodes;
	var p,k;

	for(p = 0;p < 2*gcount+1;p++)
	{
		if(list.item(p).id == 'g' + i + '_' + j)
		{
			k = p;
			break;
		}
	}
	var e1 = list.item(k);
	e1.style.display = "none";
	Dashboard.gManDiv.removeAttribute('style');
	GadgetManager.refreshGadget(i, j);		// periodic refresh of Gadget must
	// be started
	// as it is added to Dashboard
};


// this function closes a Gadget and puts it in the Add Gadget menu on top
GadgetManager.cloose =  function (i, j) { 

	var r = confirm("Are you sure you want to close this Gadget?");
	if (r == 1) {
		var e4 = document.getElementById('li' + i + '_' + j);
		e4.style.display = "none";
		var list = Dashboard.gManDiv.childNodes;
		var p,k;

		for(p = 0;p < 2*gcount+1;p++)
		{
			if(list.item(p).id == 'g' + i + '_' + j)
			{
				k = p;
				break;
			}
		}
		var e1 = list.item(k);

		// var e1 = document.getElementById('g' + i + '_' + j);
		e1.style.display = "";
		if(GadgetManager.TimeoutId[i][j] != undefined)
			window.clearTimeout(GadgetManager.TimeoutId[i][j]);	// if a window
																// is
		// closed no need
		// to refresh it.

	}
};

// this function minimises the Gadget
GadgetManager.minimise = function(i, j) {
	var Dom = YAHOO.util.Dom;
	var e = document.getElementById('li' + i + '_' + j);
	var e1 = e.firstChild.nextSibling.nextSibling;
	var e2 = e.firstChild.nextSibling.firstChild.nextSibling.nextSibling;
	var e3 = e.firstChild.nextSibling.firstChild.nextSibling.nextSibling.nextSibling;	
	GadgetManager.height[i] = [];
	GadgetManager.height[i][j] = e1.offsetHeight;
	var attributes = {   
			height: { to: 0 }   
	};

	var anim = new YAHOO.util.Anim(e1, attributes,0.75,YAHOO.util.Easing.backBoth);
	anim.animate();

	Dom.setStyle(e2, "display", "none");
	Dom.setStyle(e3, "display", "");
};

// it maximises a Gadget
GadgetManager.maximise = function(i, j) {

	var Dom = YAHOO.util.Dom;
	var e = document.getElementById('li' + i + '_' + j);
	var e1 = e.firstChild.nextSibling.nextSibling;
	var e2 = e.firstChild.nextSibling.firstChild.nextSibling.nextSibling;
	var e3 = e.firstChild.nextSibling.firstChild.nextSibling.nextSibling.nextSibling;	
	var attributes = {   
			height: { to: GadgetManager.height[i][j] } };  
	var anim = new YAHOO.util.Anim(e1, attributes, 0.75, YAHOO.util.Easing.backBoth);  
	anim.animate();
	anim.onComplete.subscribe(function() {
		e1.removeAttribute('style');	
		Dom.setStyle(e2, "display", "");
		Dom.setStyle(e3, "display", "none");
		if(e1.offsetHeight > 300)			// ht. of a Gadget must be less than
											// 300px.
		{
			e1.style.height = "300px";

		}

	});
};


// makes the basic structure of the Dashboard
Dashboard.prototype.makeDashboard = function(cols) // cols: number of columns
{	
	GadgetManager.divFrDialog = document.createElement("div");
	GadgetManager.divFrDialog.className = "yui-skin-sam";
	divtoAddDash.appendChild(GadgetManager.divFrDialog);
	var Dom = YAHOO.util.Dom;
	var i = 0;
	var w = 97/cols;
	Dashboard.colno = cols;
	var divTagMain = []; 
	GadgetManager.TimeoutId = [];
	GadgetManager.fid = [];
	GadgetManager.title = [];
	GadgetManager.dialog = [];
	GadgetManager.RefreshRate = [];
	GadgetManager.height = [];
	Dashboard.already = [];
	var divTagUl = [];
	for(i = 0;i <= cols;i++)
	{
		divTagMain[i] = document.createElement("div");
		divTagMain[i].className = "workarea";
		divtoAddDash.appendChild(divTagMain[i]);
		divTagUl[i] = document.createElement("ul");
		divTagUl[i].className = "draglist";
		if(i == 0)
			divTagMain[i].style.width = "98%";
		else
			divTagMain[i].style.width = w+"%";
		divTagUl[i].id = "ul"+i;
		divTagMain[i].appendChild(divTagUl[i]);
		Dashboard.already[i] = 0;

		GadgetManager.TimeoutId[i] = [];
	} 
	this.addGadgetManager();
};

// This function adds the Add Gadget functionality on the top of the page.
Dashboard.prototype.addGadgetManager = function()
{	
	this.addGadget(0,0,"","",1,0,0);
	var clsmgr = document.getElementById("cls0_1");
	clsmgr.style.display = "none";
	var minmgr = document.getElementById("min0_1");
	minmgr.style.display = "none";
	var hmgr = document.getElementById("h0_1");
	hmgr.style.cursor = "auto";
	var maxmgr = document.getElementById("max0_1");
	maxmgr.style.display = "none";
	var lmgr = document.getElementById("l0_1");
	lmgr.style.display = "none";

	var gmanL = document.getElementById("uptxt0_1");
	var gmanH = document.createElement("a");

	var gmanDiv = document.createElement("div");
	gmanDiv.id = "gmanDiv";

	gmanH.id = "gmanH";
	gmanH.setAttribute("href", "#");
	gmanH.innerHTML = "<img src = " + nearestPathServ + "web/images/plusbiggest.gif class = plus><div class = buttonText> &nbsp  ADD GADGET -></div>";
	gmanL.appendChild(gmanH);
	gmanL.appendChild(gmanDiv);
	gmanH.onclick = function(){GadgetManager.showinGadget();};
	gmanH.onmouseover = function()
	{
		gmanH.bgcolor = "#000000";
	};
	Dashboard.gManDiv = gmanDiv;
	GadgetManager.showinGadget();

};
var frgadShow = 0;

// makes the Add Gadget menu visible
GadgetManager.showinGadget = function()
{

	if(frgadShow == 1)
	{

		Dashboard.gManDiv.style.display = "";
		frgadShow = 0;
	}
	else
	{
		Dashboard.gManDiv.style.display = "none";
		frgadShow = 1;

	}	
};
// Adds a Gadget to the Dashboard
Dashboard.prototype.addGadget = function(fid,col, title, body,vis,min,refreshRate)
{	
	gcount++;
	var i,j;
	i = col;
	j = Dashboard.already[i]+1;
	Dashboard.already[i] = j;

	if(j == 1){
		GadgetManager.RefreshRate[i] = [];
		GadgetManager.fid[i] = [];
		GadgetManager.dialog[i] = [];	
		GadgetManager.title[i] = [];
	}
	GadgetManager.RefreshRate[i][j] = refreshRate;
	GadgetManager.fid[i][j] = fid;
	GadgetManager.title[i][j] = title;

	var liTag;
	liTag = document.createElement("li");
	liTag.className = "list";
	liTag.id = "li"+i+"_"+j;
	var ul = document.getElementById("ul"+i)
	ul.appendChild(liTag);
	var hTag;
	hTag = document.createElement("div");
	hTag.className = "upper";
	hTag.id = "h"+i+"_"+j;

	var roundHelpTag;
	roundHelpTag = document.createElement("div");
	roundHelpTag.className = "RoundHelp";		// basically 2 new divisions for
	// a rounding corners effect on Gadgets
	liTag.appendChild(roundHelpTag);
	liTag.appendChild(hTag);

	var roundHelpTag1;
	roundHelpTag1 = document.createElement("div");
	roundHelpTag1.className = "RH1";
	roundHelpTag.appendChild(roundHelpTag1);

	var roundHelpTag2;
	roundHelpTag2 = document.createElement("div");
	roundHelpTag2.className = "RH2";
	roundHelpTag.appendChild(roundHelpTag2);

	var roundHelpTag3;
	roundHelpTag3 = document.createElement("div");
	roundHelpTag3.className = "RH3";
	roundHelpTag.appendChild(roundHelpTag3);



	var uptxtTag;
	uptxtTag = document.createElement("div");
	uptxtTag.className = "uptxt";
	uptxtTag.id = "uptxt"+i+"_"+j;
	uptxtTag.innerHTML = title;
	hTag.appendChild(uptxtTag);

	var clsDivTag;
	clsDivTag = document.createElement("div");
	clsDivTag.className = "cls";
	clsDivTag.id = "cls"+i+"_"+j;
	hTag.appendChild(clsDivTag);

	var clsTag;
	clsTag = document.createElement("a");
	clsTag.setAttribute("href", "#");

	clsTag.onclick = function(){GadgetManager.cloose(i,j);};
	clsTag.innerHTML = "<img src = " + nearestPathServ + "web/images/clstry2.gif />";
	clsDivTag.appendChild(clsTag);

	var minDivTag;
	minDivTag = document.createElement("div");
	minDivTag.className = "min";
	minDivTag.id = "min"+i+"_"+j;
	hTag.appendChild(minDivTag);

	var minTag;
	minTag = document.createElement("a");
	minTag.setAttribute("href", "#");

	minTag.onclick = function(){GadgetManager.minimise(i,j);};
	minTag.innerHTML = "<img src =" + nearestPathServ + "web/images/min6.gif />";;
	minDivTag.appendChild(minTag);

	var maxDivTag;
	maxDivTag = document.createElement("div");
	maxDivTag.className = "max";
	maxDivTag.id = "max"+i+"_"+j;
	hTag.appendChild(maxDivTag);

	var maxTag;
	maxTag = document.createElement("a");
	maxTag.setAttribute("href", "#");
	maxTag.onclick = function(){GadgetManager.maximise(i,j);};
	maxTag.innerHTML = "<img src = " + nearestPathServ + "web/images/max5.gif>";
	maxDivTag.appendChild(maxTag);

	var rTag;
	rTag = document.createElement("div");
	rTag.className = "refresh";
	rTag.id = "r"+i+"_"+j;
	rTag.innerHTML = "<img src = " + nearestPathServ + "web/images/loader2.gif>";
	rTag.style.display = "none";
	hTag.appendChild(rTag);

	var rBDivTag;
	rBDivTag = document.createElement("div");
	rBDivTag.className = "refreshb";
	rBDivTag.id = "rB"+i+"_"+j;

	var rBTag;
	rBTag = document.createElement("a");
	rBTag.setAttribute("href", "#");
	rBTag.innerHTML = "<img src = " + nearestPathServ + "web/images/reftry8.gif >";
	rBTag.onclick = function(){GadgetManager.refreshGadget(i,j);};
	rBDivTag.appendChild(rBTag);
	hTag.appendChild(rBDivTag);
	rBDivTag.style.display = "none";

	if(i != 0)
	{
		RefreshMenuDiv = document.createElement("div");
		RefreshMenuDiv.className = "RMenu";	

		var RefreshMenuTag;
		RefreshMenuTag = document.createElement("input");
		RefreshMenuTag.id = "menuButton"+i+"_"+j;

		RefreshMenuTag.type = "button";
		RefreshMenuTag.className = "MenuButton";
		RefreshMenuTag.setAttribute("value","<img src=" + nearestPathServ + "web/images/pref2.gif />");

		var RefreshMenuSelect;
		RefreshMenuSelect = document.createElement("select");
		RefreshMenuSelect.id = "menuButton"+i+"_"+j+"select";

		var RefreshMenuOptPar;
		RefreshMenuOptPar = document.createElement("option");
		RefreshMenuOptPar.className = "menuitem";
		RefreshMenuOptPar.value = "-1";
		RefreshMenuOptPar.innerHTML = "Change Report Parameters";
		GadgetManager.addPar(i,j,body);

		var RefreshMenuOpt0;
		RefreshMenuOpt0 = document.createElement("option");
		RefreshMenuOpt0.className = "menuitem";
		RefreshMenuOpt0.value = "0";
		RefreshMenuOpt0.innerHTML = "Remove auto Refresh";

		var RefreshMenuOpt1;
		RefreshMenuOpt1 = document.createElement("option");
		RefreshMenuOpt1.className = "menuitem";
		RefreshMenuOpt1.value = "20000";
		RefreshMenuOpt1.innerHTML = "Refresh every 20s";

		var RefreshMenuOpt2;
		RefreshMenuOpt2 = document.createElement("option");
		RefreshMenuOpt2.className = "menuitem";
		RefreshMenuOpt2.value = "30000";
		RefreshMenuOpt2.innerHTML = "Refresh every 30s";

		var RefreshMenuOpt3;
		RefreshMenuOpt3 = document.createElement("option");
		RefreshMenuOpt3.className = "menuitem";
		RefreshMenuOpt3.value = "300000";
		RefreshMenuOpt3.innerHTML = "Refresh every 5 mins";

		RefreshMenuSelect.appendChild(RefreshMenuOpt3);
		RefreshMenuSelect.appendChild(RefreshMenuOpt2);
		RefreshMenuSelect.appendChild(RefreshMenuOpt1);
		RefreshMenuSelect.appendChild(RefreshMenuOpt0);
		RefreshMenuSelect.appendChild(RefreshMenuOptPar);

		RefreshMenuDiv.appendChild(RefreshMenuTag);
		RefreshMenuDiv.appendChild(RefreshMenuSelect);
		hTag.appendChild(RefreshMenuDiv);
		GadgetManager.MakeRefButton(i,j);

	}

	var lTag;
	lTag = document.createElement("div");
	lTag.className = "lower";;
	lTag.id = "l"+i+"_"+j;
	lTag.innerHTML = "";
	liTag.appendChild(lTag);

	var fNameTag;
	fNameTag = document.createElement("div");
	fNameTag.className = "fname";;
	fNameTag.id = "f"+i+"_"+j;
	fNameTag.innerHTML = body;
	fNameTag.style.display = "none";
	liTag.appendChild(fNameTag);



	var gTag;

	if(i != 0 || j != 1)
	{

		gTag = document.createElement("a");
		gTag.setAttribute("href", "#");
		gTag.onclick = function(){GadgetManager.add(i,j);};

		gTag.className = "gelem";;
		gTag.id = "g"+i+"_"+j;
		gTag.innerHTML = title+"<br>";
		Dashboard.gManDiv.appendChild(gTag);

		if(vis == 0)
		{
			liTag.style.display = "none";
			gTag.style.display = "";

		}
		else 
		{

			liTag.style.display = "";
			gTag.style.display = "none";
		}
		if(min == 1)
		{	
			GadgetManager.height[i] = [];
			GadgetManager.height[i][j] = lTag.offsetHeight;
			lTag.style.height = 0;
			maxDivTag.style.display = "";
			minDivTag.style.display = "none";

		}
		else
		{
			maxDivTag.style.display = "none";
		}

		if(vis != 0)
			GadgetManager.refreshGadget(i,j);	// if Gadget is closed no need
												// to
		// refresh it.
	}
};

var getBody = 
{
		success: function(o)
		{
	var prod = YAHOO.lang.JSON.parse(o.responseText);
	var i = prod[0];
	var j = prod[1];
	var a = document.getElementById("li"+i+"_"+j);
	var b = a.firstChild.nextSibling;
	var c = b.firstChild.nextSibling.nextSibling.nextSibling.nextSibling;
	c.style.display = "none";
	var d = c.nextSibling;

	d.style.display = "";	
	var body = b.nextSibling;
	var c = body.nextSibling;
	var RefRate = GadgetManager.RefreshRate[i][j];

	body.innerHTML = prod[2];

	if(body.offsetHeight > 300)
	{
		body.style.height = "300px";

	}
	if(GadgetManager.TimeoutId[i][j] != undefined)
		window.clearTimeout(GadgetManager.TimeoutId[i][j]);
	if(RefRate != 0 && RefRate != undefined)
	{
		GadgetManager.TimeoutId[i][j] = window.setTimeout("GadgetManager.refreshGadget("+i+","+j+")", RefRate);
	}

		},

		failure: function(o)
		{
		}
};

// used to refresh a Gadget given by (i,j)
GadgetManager.refreshGadget = function(i,j)
{

	var a = document.getElementById("li"+i+"_"+j);
	var b = a.firstChild.nextSibling;
	var c = b.firstChild.nextSibling.nextSibling.nextSibling.nextSibling;
	c.style.display = "";
	var d = c.nextSibling;
	d.style.display = "none";
	body = b.nextSibling;
	var c = body.nextSibling;
	var fname = c.innerHTML;
	var pageLink = document.location.href;
	var linkParts = pageLink.split("/");
	var sUrlPrefix = "";
	var x = 0;
	for(x = 0;x < linkParts.length-1;x++)		
	{	
		sUrlPrefix = sUrlPrefix+linkParts[x]+"/";
	}
	var sUrl = nearestPathServ+"DashServlet?persist_url=false&id=1&fname="+fname+"&i="+i+"&j="+j;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl,getBody,null);	

};




// Returns the Info of each Gadget on the Dashboard, this info is later posted
// to the server.
Dashboard.prototype.getinfo = function()
{	
	/*
	 * Here, col-> in which column a Gadget is in
	 * vis-> 0=>closed 1=>visible
	 * min-> 1=>minimised 0=?maximised
	 * ht-> It tells us the location of the Gadget on the page, the height at which it 
	 * 		starts, note that it is not the height of the Gadget
	 *heightLower-> The height of the lower part of the Gadget,
	 *				it helps in determining if the Gadget is minimised or maximised
	 */
	var i,j,curr,k = 0;
	var col,vis = 1,min,ht;
	var heightLower;
	var info =  new Array();
	for (i = 1;i <= Dashboard.colno;i = i+1) {
		for (j = 1;j <= Dashboard.already[i];j = j+1) {
			vis = 1;
			curr = document.getElementById("li"+i+"_"+j);
			var ListName = curr.parentNode.id; // eg. ListName=ul2, we want to
			// get 2 since this Gadget is in 2nd column
			var ListParts = ListName.split("");
			
			col = ListParts[2];

			k = curr.firstChild.nextSibling;
			m = k.firstChild;
			heightLower = k.nextSibling.offsetHeight;	
				ht = m.offsetTop;
				if(ht == 0 && curr.style.display == "" )
				{
					ht = k.offsetParent.offsetTop;
				}
				if(ht == 0)// if gadget is closed
				{

					vis = 0;
					curr.style.display = "";
					ht = m.offsetTop;
					heightLower = k.nextSibling.offsetHeight;
					curr.style.display = "none";
				}
				//if height of lower div. >2 it is maximised,we are nt using 0 since
				// offsetHeight doesnt return exact 0
				if(heightLower>2)	
				min = 0;
				else 
				min = 1;

				/* info.push(m.innerHTML); */
				info.push(col);
				info.push(ht);
				info.push(vis);
				info.push(min);
				info.push(GadgetManager.fid[i][j]);
			}
		
	}
	return info;
};
var cb1 = 
{
		success: function(o)
		{
		},
		failure: function(o)
		{
		}
};

//Used for changing the Refresh Rate of a Gadget
GadgetManager.changeRefRate = function(i,j,newRR)
{

	var a = document.getElementById("li"+i+"_"+j);
	var b = a.firstChild.nextSibling;
	var c = b.firstChild.nextSibling.nextSibling.nextSibling.nextSibling;
	var d = c.nextSibling;
	body = b.nextSibling;
	var c = body.nextSibling;
	var fname = c.innerHTML;


	var pageLink = document.location.href;			// to generate the sURL
	var linkParts = pageLink.split("/");				// removes part after
														// last /
	// and puts DashServlet instead to generate apt url

	var sUrlPrefix = "";
	var k = 0;
	for(k = 0;k < linkParts.length-1;k++)		
	{	
		sUrlPrefix = sUrlPrefix+linkParts[k]+"/";
	}


	var sUrl = nearestPathServ+"DashServlet";
	var toPost = "persist_url=false&postType=1&fid="+GadgetManager.fid[i][j]+"&rr="+newRR;
	if(GadgetManager.TimeoutId[i][j] != undefined)
		window.clearTimeout(GadgetManager.TimeoutId[i][j]);
	GadgetManager.RefreshRate[i][j] = newRR;

	if(newRR != 0)
	{
		GadgetManager.TimeoutId[i][j] = window.setTimeout("GadgetManager.refreshGadget("+i+","+j+")", newRR);
	}

	var requestRRchange = YAHOO.util.Connect.asyncRequest('POST', sUrl,cb1,toPost);
};

//initialises DragDrop
function initDD() {
	var dd;
	var Dom = YAHOO.util.Dom;
	var ul;
	var dummyElem;
	var DDM = YAHOO.util.DragDropMgr;
	DDAppl = 
	{
		initDD: function() {
			var i,j;
			for (i = 1;i <= Dashboard.colno;i = i+1) {
				new YAHOO.util.DDTarget("ul"+i);
				var ul = document.getElementById("ul"+i);
				ul.style.paddingBottom = "100px";
			}
			for (i = 1;i <= Dashboard.colno;i = i+1) {
				for (j = 1;j <= Dashboard.already[i];j = j+1) {
					var oMenuButton;
					dd = new DDList("li" + i + "_" + j);
					dd.setHandleElId("h" + i + "_" + j);
	
				}
			}
		}
	};

	DDList = function(id, sGroup, config) {

		DDList.superclass.constructor.call(this, id, sGroup, config);

		var el = this.getDragEl();
		Dom.setStyle(el, "opacity", .5); // The proxy is slightly transparent
		Dom.setStyle(el, "visibility", "hidden");

		this.goingUp = false;
		this.lastY = 0;
	};

	YAHOO.extend(DDList, YAHOO.util.DDProxy, {

		startDrag: function(x, y) {

		// make the proxy look like the source element
		var dragEl = this.getDragEl();
		var clickEl = this.getEl();
		Dom.setStyle(clickEl, "visibility", "hidden");
		dragEl.innerHTML = clickEl.innerHTML;
		Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color"));
		Dom.setStyle(dragEl, "backgroundColor", Dom.getStyle(clickEl, "backgroundColor"));
		Dom.setStyle(dragEl, "border", "0px solid gray");

	},

	endDrag: function(e) {
		var srcEl = this.getEl();
		var proxy = this.getDragEl();

		// Show the proxy element and animate it to the src element's location
		Dom.setStyle(proxy, "visibility", "visible");

		var a = new YAHOO.util.Motion(
				proxy, {
					points: {
					to: Dom.getXY(srcEl)
				}
				},
				0.2,
				YAHOO.util.Easing.easeOut
		)
		var proxyid = proxy.id;
		var thisid = this.id;
		a.onComplete.subscribe(function() {
			Dom.setStyle(proxyid, "visibility", "hidden");

			Dom.setStyle(thisid, "visibility", "visible");

			var e2 = proxy.firstChild.nextSibling.firstChild.nextSibling.nextSibling;
			var e3 = proxy.firstChild.nextSibling.firstChild.nextSibling.nextSibling.nextSibling;	

			Dom.setStyle(e2,"visibility","hidden");           
			Dom.setStyle(e2,"display","none");           

			Dom.setStyle(e3,"visibility","hidden");           
			Dom.setStyle(e3,"display","none");           

		});
		a.animate();

	},

	onDragDrop: function(e, id) {

		// If there is one drop interaction, the li was dropped either on the list,
		// or it was dropped on the current location of the source element.
		if (DDM.interactionInfo.drop.length === 1) {

			// The position of the cursor at the time of the drop (YAHOO.util.Point)
			var pt = DDM.interactionInfo.point;

			// The region occupied by the source element at the time of the drop
			var region = DDM.interactionInfo.sourceRegion;

			// Check to see if we are over the source element's location. We
			// will
			// append to the bottom of the list once we are sure it was a drop
			// in
			// the negative space (the area of the list without any list items)
			if (!region.intersect(pt)) {
				var destEl = Dom.get(id);
				var destDD = DDM.getDDById(id);
				destEl.appendChild(this.getEl());
				destDD.isEmpty = false;
			}

		}


	},

	onDrag: function(e) {


		// Keep track of the direction of the drag for use during onDragOver
		var y = Event.getPageY(e);

		if (y < this.lastY) {
			this.goingUp = true;
		} else if (y > this.lastY) {
			this.goingUp = false;
		}

		this.lastY = y;
	},

	onDragOver: function(e, id) {

		var srcEl = this.getEl();
		var destEl = Dom.get(id);

		// We are only concerned with list items, we ignore the dragover
		// notifications for the list.
		if (destEl.nodeName.toLowerCase() == "li") {
			var orig_p = srcEl.parentNode;
			var p = destEl.parentNode;

			if (this.goingUp) {
				p.insertBefore(srcEl, destEl); // insert above
			} else {
				p.insertBefore(srcEl, destEl.nextSibling); // insert below
			}

		}
	}
	});
	var Event = YAHOO.util.Event;
	Event.onDOMReady(DDAppl.initDD, DDAppl, true);
}
