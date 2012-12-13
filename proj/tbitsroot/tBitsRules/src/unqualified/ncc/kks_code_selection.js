<script type='text/javascript'>

function registerKKSOnChangeFunction()
{
	var equipmentCodeMap = <%=equipmentCodeMap%>;
	var componentCodeMap = <%=componentCodeMap%>;
	var SYSTEM_CODE = 'SystemCode';
	var EQUIPMENT_CODE = 'EquipmentUnitCode';
	var COMPONENT_CODE = 'ComponentCode';
	var systemCodeCtrl = document.getElementById(SYSTEM_CODE);
	var equipmentCodeCtrl = document.getElementById(EQUIPMENT_CODE);
	var componentCodeCtrl = document.getElementById(COMPONENT_CODE);
	if((systemCodeCtrl != null) && (equipmentCodeCtrl != null) && (componentCodeCtrl != null))
	{
		systemCodeCtrl.onchange = function(){hideKKSOptions(systemCodeCtrl, equipmentCodeCtrl, equipmentCodeMap);
												hideKKSOptions(equipmentCodeCtrl, componentCodeCtrl, componentCodeMap);}
		equipmentCodeCtrl.onchange = function(){hideKKSOptions(equipmentCodeCtrl, componentCodeCtrl, componentCodeMap);}
		hideKKSOptions(systemCodeCtrl, equipmentCodeCtrl, equipmentCodeMap);
		hideKKSOptions(equipmentCodeCtrl, componentCodeCtrl, componentCodeMap);
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

/*
 //var isFound = true;
		for(var j = 0; j < targetTypeNamesList.length; j++){
			
			if((targetCtrl.options[i].value == 'None') || (targetCtrl.options[i].value == targetTypeNamesList[j]))
			{
				isFound = true;
				break;
			}
			else
			{
				isFound = false;
			}
		}
		if (isFound == false)
		{
			targetCtrl.options[i].style.display = 'none' ;
		}
		else
		{
			targetCtrl.options[i].style.display = '';
			if(targetCtrl.options[i].value != 'None')
			{
				counter = counter + 1;
			}
		
			if (counter == targetCtrl.length)
			{
				targetCtrl.value = 'None';
				return;
			}
		}
 */

</script>