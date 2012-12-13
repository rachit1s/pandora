<script type='text/javascript'>

function registerKKSOnChangeFunction()
{
	var equipmentCodeMap = <%=documentCodeMap%>;
	var DOCUMENT_CATEGORY = 'status_id';
	var DOCUMENT_CODE = 'request_type_id';
	var systemCodeCtrl = document.getElementById(DOCUMENT_CATEGORY);
	var equipmentCodeCtrl = document.getElementById(DOCUMENT_CODE);
	if((systemCodeCtrl != null) && (equipmentCodeCtrl != null))
	{
		systemCodeCtrl.onchange = function(){hideKKSOptions(systemCodeCtrl, equipmentCodeCtrl, equipmentCodeMap);}
		hideKKSOptions(systemCodeCtrl, equipmentCodeCtrl, equipmentCodeMap);
	}	
}

registerKKSOnChangeFunction();

function appendOptionLast(elSel, name, displayName)
{  
  var elOptNew = document.createElement('option');
  elOptNew.text = displayName;
  elOptNew.value = name;
  if (name == 'None')
  {
	  elOptNew.selected = true;
  }
  try 
  {
    elSel.add(elOptNew, null); // standards compliant; doesn't work in IE
  }
  catch(ex) 
  {
    elSel.add(elOptNew); // IE only
  }
}

function removeAll(elSel)
{
	for (i = elSel.length - 1; i>=0; i--)
	{
	      elSel.remove(i);
	}
	appendOptionLast(elSel, 'None', 'None');
}

function hideKKSOptions(srcCtrl, targetCtrl, map){	
	var srcVal = srcCtrl.value;
	var targetTypeNamesList = eval(map[srcCtrl.value]);
	//alert("target-list:\n" + targetTypeNamesList + ', length: ' + targetTypeNamesList.length);
	
	removeAll(targetCtrl);
	if ((targetTypeNamesList == 'undefined') || (targetTypeNamesList.length == 0))
	{
		return;
	}
		
	for( var i = 0 ; i < targetTypeNamesList.length ; i++ )
	{		
		appendOptionLast(targetCtrl, targetTypeNamesList[i].name, targetTypeNamesList[i].displayName);
	}
}
</script>