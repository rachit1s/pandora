// JScript source code

function onBodyLoad()
{
    hideAll();
    //disable("fieldType");
    /*
    setValue("fieldType", "dropdown");
    setValue("fieldId", "");
    showComponents();
    */
}

var HEADING_BULLETS_REQUIRED = "Specify atleast on bullet point."
var FIELD_NAME_RULE = "Field name can contain alphanumeric characters and underscore only.";
var FIELD_NAME_MANDATORY = "Specify a name for the field.";
var FIELD_NAME_EXISTS = "A field with the specified name already exists. \nDo you want to override?";

var FIELD_LABEL_MANDATORY = "Specify a label for the field.";

var GROUP_NAME_RULE = "Group name can contain alphanumeric characters and underscore only.";
var GROUP_NAME_REQUIRED = "Specify a name for the radio button group.";
var OPTIONS_REQUIRED = "Atleast two options are required.";

var PARAGRAPH_CONTENT_REQUIRED = "Content required.";
var TYPE_NAME_REQUIRED = "Specify a name for the type.";
var TYPE_EXISTS = "A type with this name already exists.";


var fieldMap = new Object();
var fieldCtr = 0;
var typeMap = new Object();
var cMap = 
{
    "checkbox": 
    [
        "tr_fieldName",
        "tr_fieldLabel",
        "tr_toolTip",
        "tr_saveField",
        "div_fieldName",
        "div_fieldLabel",
        "div_toolTip",
        "div_saveField"
    ],
    "date":
    [
        "tr_fieldName",
        "tr_fieldLabel",
        "tr_toolTip",
        "tr_saveField",
        "div_fieldName",
        "div_fieldLabel",
        "div_toolTip",
        "div_saveField"
    ],
    "datetime":
    [
        "tr_fieldName",
        "tr_fieldLabel",
        "tr_toolTip",
        "tr_saveField",
        "div_fieldName",
        "div_fieldLabel",
        "div_toolTip",
        "div_saveField"
    ],
    "dropdown":
    [
        "typeList",
        "tr_fieldName",
        "tr_fieldLabel",
        "tr_toolTip",
        "tr_types",
        "tr_space",
        "tr_saveField",
        "div_fieldName",
        "div_fieldLabel",
        "div_toolTip",
        "div_types",
        "div_typeList",
        "div_saveField"
    ],
    "paragraph":
    [
        "tr_paragraph_label",
        "tr_paragraph",
        "tr_saveField",
        "div_paragraph",
        "div_saveField"
    ],
    "points":
    [
        "heading",
        "tr_heading",
        "tr_bullets_label",
        "tr_bullets",
        "tr_saveField",
        "div_bullets",
        "div_heading",
        "div_saveField"
    ],
    "textbox":
    [
        "tr_fieldName",
        "tr_fieldLabel",
        "tr_toolTip",
        "tr_saveField",
        "div_fieldName",
        "div_fieldLabel",
        "div_toolTip",
        "div_saveField"
    ],
    "textarea":
    [
        "tr_fieldName",
        "tr_fieldLabel",
        "tr_toolTip",
        "tr_saveField",
        "div_fieldName",
        "div_fieldLabel",
        "div_toolTip",
        "div_saveField"
    ],
    "radio":
    [
        "groupName",
        "tr_groupName",
        "tr_radioList_label",
        "tr_radioList",
        "tr_saveField",
        "div_radioList",
        "div_groupName",
        "div_saveField"
    ]
};

var cList = 
[
    "tr_fieldName", "div_fieldName",
    "tr_fieldLabel", "div_fieldLabel",
    "tr_toolTip", "div_toolTip",
    "tr_paragraph_label", "tr_paragraph", "div_paragraph",
    "tr_heading", "div_heading",
    "tr_bullets_label", "tr_bullets", "div_bullets",
    "tr_groupName", "div_groupName",
    "tr_radioList_label", "tr_radioList", "div_radioList",
    "tr_space", 
    "tr_types", "div_types", "div_typeList", "typeList",
    "tr_saveField", "div_saveField"
];


function onAddType()
{
    setValue("typeName", "");
    document.getElementById("isDefault").checked = false;
    document.getElementById("typeName").focus();
    setInnerHTML("typeHelp", "Enter the name in 'Type Name' box.");
    document.getElementById("typeList").value = "";
}

function onRemoveType()
{
}

function onSaveType()
{
    var typeId = getValue("typeId");
    var typeName = trim(getValue("typeName"));
    var isDefault = document.getElementById("isDefault").checked;
    
    if (typeName == "")
    {
        alert(TYPE_NAME_REQUIRED);
        return;
    }
    
    var isNewType = false;
    var obj = typeMap[typeName];
    if (obj != null && typeId == "")
    {
        var result = confirm(TYPE_EXISTS);
        if (result == false)
        {
            return;
        }
        typeId = typeName;
    }
    if (typeId == "")
    {
        isNewType = true;
        typeId = typeName;
    }
    
    typeMap[typeId] = 
    [
        typeName,
        isDefault
    ];
    setValue("typeName", "");
    document.getElementById("isDefault").checked = false;
    if (isNewType == true)
    {
        addToList("typeList", typeId, typeName);
    }
}

function onChangeTypeList()
{
    var select = document.getElementById("typeList");
    var selIndex = select.selectedIndex;
    if (selIndex < 0) return;
    var value = select.options[selIndex].value;
    var obj = typeMap[value];
    
    var typeName = obj[0];
    var isDefault = obj[1];
    setValue("typeName", typeName);
    document.getElementById("isDefault").checked = isDefault;
}

/**
 * This method removes the leading and trailing spaces in the given string.
 */
function trim(str)
{
    if (str == null) return;
	str = str.replace(/^\s+/, "");
	str = str.replace(/\s+$/, "");
	return str;
}

function disable(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null)
    {
        ctrl.disabled = true;
    }
}

function enable(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null)
    {
        ctrl.disabled = false;
    }
}

function getValue(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    var value = "";
    if (ctrl != null)
    {
        value = trim(ctrl.value);
    }
    return value;
}

function setValue(ctrlId, value)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null)
    {
        ctrl.value = value;
    }
}

function setInnerHTML(ctrlId, value)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null)
    {
        ctrl.innerHTML = value;
    }
}

function getInnerHtml(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    var innerHtml = "";
    if (ctrl != null)
    {
        innerHtml = ctrl.innerHTML;
    }
    return innerHtml;
}

function setInnerText(ctrlId, value)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null)
    {
        ctrl.innerText = value;
    }
}
function getInnerText(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    var innerText = "";
    if (ctrl != null)
    {
        innerText = ctrl.innerText;
    }
    return innerText;
}

function clearSelect(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null && (ctrl.tagName == "select" || ctrl.tagName == "SELECT"))
    {
        ctrl.options.length = 0;
    }
}

function unCheck(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null)
    {
        try
        {
            ctrl.checked = false;
        }
        catch (e)
        {
        }
    }
}

function showComponents()
{
    hideAll();
    var select = document.getElementById("fieldType");
    var selIndex = select.selectedIndex;
    if (selIndex == -1) return;
    var selectedOption = select.options[selIndex].value;
    var arr = cMap[selectedOption];
    if (arr == null) return;
    for(i = 0; i < arr.length ; i++)
    {
        var element = document.getElementById(arr[i]);
        var tagName = element.tagName;
        if (tagName == "TR")
        {
            if (document.all)
            {
                element.style.display = "block";
            }
            else
            {
                element.style.display = "table-row";
            }
        }
        else
        {
            element.style.display = "inline";
        }
    }
}

function hideAll()
{
    for (i = 0; i < cList.length; i++)
    {
        var element = cList[i];
        document.getElementById(element).style.display = "none";
    }
}

function addOption(select, promptText)
{
    var str = trim(prompt(promptText, ""));
    if (str == null || str == "") return;
    var length = select.options.length ;
    select.options.length = length + 1;
    select.options[length].text = str;
}
function removeOption(select)
{
    var selIndex = select.selectedIndex;
    if (selIndex == -1) return;
    
    var length = select.options.length;
    
    var arr = new Array();
    for (i = 0; i < length; i++)
    {
        if (i != selIndex)
        {
            arr.push(select.options[i].text);
        }
    }
    select.options.length = 0;
    length = arr.length;
    for (i = 0; i < arr.length; i++)
    {
        select.options.length = i+1;
        select.options[i].text = arr[i];
    }
    return;
}

function clearAll()
{
    setValue("fieldId", "");
    setValue("fieldName", "");
    setValue("fieldLabel", "");
    setValue("toolTip", "");
    setValue("heading", "");
    clearSelect("bulletsList");
    setValue("typeName", "");
    clearSelect("typesList");
    unCheck("isDefault");
    setInnerHTML("paragraph", "");
    setValue("groupName", "");
    clearSelect("radioList", "");
}

function onAddField()
{
    document.getElementById("fieldList").value = "";
    document.getElementById("fieldType").value = "";
    hideAll();
    clearAll();
    enable("fieldType");
    setInnerHTML("contextHelp", "Select a field type and furnish the details of the field.");
}

function onRemoveField()
{
    var select = document.getElementById("fieldList");
    var selIndex = select.selectedIndex;
    if (selIndex == -1)
    {
        setInnerHTML("contextHelp", "Select a field to remove.");
        return;
    }
}

function onClickFieldType()
{
}

function onChangeFieldType()
{
    var select = document.getElementById("fieldType");
    var selIndex = select.selectedIndex;
    if (selIndex == -1) return;
    
    setValue("fieldId", "");
    setInnerHTML("contextHelp", "");
    showComponents();
    clearAll();
}

function onSaveFormDetails()
{
    for (key in fieldMap)
    {
        alert(key);
    }
}

function addToList(listName, fieldId, fieldName)
{
    var ctrl = document.getElementById(listName);
    var len = ctrl.options.length;
    ctrl.options.length = len + 1;
    ctrl.options[len].value = fieldId;
    ctrl.options[len].text = fieldName;
}
function onChangeFieldList()
{
    var select = document.getElementById("fieldList");
    var selIndex = select.selectedIndex;
    if (selIndex == -1)
    {
        return;
    }
    var value = select.options[selIndex].value;
    var obj = fieldMap[value];
    if (obj == null) return;

    // Get the field type.    
    var fieldType = obj["fieldType"];

    if (fieldType == "points")
    {
        displayBulletList(value, obj);
    }
    else if (fieldType == "checkbox" ||
             fieldType == "date" ||
             fieldType == "datetime" ||
             fieldType == "textarea" ||
             fieldType == "textbox")
    {
        displayGeneralFieldDetails(fieldId, obj);
    }
    else if (fieldType == "radio")
    {
        displayRadioList(value, obj);
    }
    else if (fieldType == "paragraph")
    {
        displayParagraph(value, obj);
    }
}
function onSaveFieldDetails()
{
    var fieldType = getValue("fieldType");
    var flag = false;
    if (fieldType == "points") 
    {
        flag = saveBulletList();
    }
    else if (fieldType == "checkbox" ||
             fieldType == "date" ||
             fieldType == "datetime" ||
             fieldType == "textarea" ||
             fieldType == "textbox")
    {
        flag = saveGeneralFieldDetails(fieldType);
    }
    else if (fieldType == "radio") 
    {
        flag = saveRadioList();
    }
    else if (fieldType == "paragraph")
    {
        flag = saveParagraph();
    }
    
    if (flag == true)
    {
        // Re initialize the form field section.
        setValue("fieldList", "");
        setValue("fieldType", "");
        showComponents();
        disable("fieldType");
    }
}

function saveParagraph()
{
    var fieldId = getValue("fieldId");
    var fieldType = "paragraph";
    var paragraph = getInnerText("paragraph");
    if (trim(paragraph) == "")
    {
        alert(PARAGRAPH_CONTENT_REQUIRED);
        return false;
    }
    
    var isNewField = false;
    if (fieldId == "")
    {
        isNewField = true;
        fieldId = "_Form_Builder_Paragraph_ID_" + fieldCtr;
        fieldCtr = fieldCtr + 1;
    }
    fieldMap[fieldId] = 
    {
        "fieldType" : fieldType,
        "paragraph" : paragraph
    };
    
    // Clear the values in the fields
    setValue("fieldId", "");
    setInnerHTML("paragraph", "");

    if (isNewField == true)
    {
        addToList("fieldList", fieldId, "Paragraph");
    }
    
    return true;
}
function displayParagraph(fieldId, obj)
{
    setValue("fieldId", fieldId);
    setValue("fieldType", "paragraph");
    disable("fieldType");
    showComponents();
    setInnerText("paragraph", obj["paragraph"]);
    return;
}

function saveBulletList()
{
    var fieldId = getValue("fieldId");
    var fieldType = "points";
    var heading = trim(getValue("heading"));
    var bulletCtrl = document.getElementById("bulletsList");
    var length = bulletCtrl.length;
    if (length == 0)
    {
        alert(HEADING_BULLETS_REQUIRED);
        return false;
    }
    var arrBullets = new Array();
    for (i = 0; i < length;i++)
    {
        arrBullets.push(bulletCtrl.options[i].text);
    }
    
    var isNewField = false;
    
    if (fieldId == "")
    {
        isNewField = true;
        fieldId = "_Form_Builder_Bullets_ID_" + fieldCtr;
        fieldCtr = fieldCtr + 1;
    }
    
    fieldMap[fieldId] = 
    {
        "fieldType" : fieldType,
        "heading"   : heading,
        "bullets"   : arrBullets
    };
    
    if (isNewField == true)
    {
        addToList("fieldList", fieldId, "Bullet Points");
    }
    
    // Clear the values in the fields
    setValue("fieldId", "");
    setValue("heading", "");
    clearSelect("bulletsList");
    
    return true;
}

function displayBulletList(fieldId, obj)
{
    setValue("fieldId", fieldId);
    setValue("fieldType", "points");
    disable("fieldType");
    showComponents();
    setValue("heading", obj["heading"]);
    
    var select = document.getElementById("bulletsList");
    select.options.length = 0;
    var arrPoints = obj["bullets"];
    for(i=0; i < arrPoints.length; i++)
    {
        select.options.length = i + 1;
        select.options[i].text = arrPoints[i];
        select.options[i].value= arrPoints[i];
    }
    return;
}

function saveGeneralFieldDetails(fieldType)
{
    var fieldId = getValue("fieldId");
    var fieldName = trim(getValue("fieldName"));
    var fieldLabel = trim(getValue("fieldLabel"));
    var toolTip = getValue("toolTip");
    
    if (fieldName == "")
    {
        alert(FIELD_NAME_MANDATORY);
        return false;
    }
    
    // Field name can contain only alphanumeric characters and underscore.
    if (fieldName.match(/^[a-zA-Z0-9_]+$/) == null)
    {
        alert(FIELD_NAME_RULE);
        return false;
    }
    
    var isNewField = false;
    // Check if there is an entry with this name in this form.
    var obj = fieldMap[fieldName];
    if (obj != null && fieldId == "")
    {
        var result = confirm(FIELD_NAME_EXISTS);
        if (result == false) return false;
        fieldId = fieldName;
    }
    
    // Set the fieldId as fieldName if fieldId is currently empty.
    if (fieldId == "")
    {
        isNewField = true;
        fieldId = fieldName;
    }
    
    if (fieldLabel == "")
    {
        alert(FIELD_LABEL_MANDATORY);
        return false;
    }
    
    fieldMap[fieldId] = 
    {
        "fieldType"     : fieldType,
        "fieldName"     : fieldName,
        "fieldLabel"    : fieldLabel,
        "toolTip"       : toolTip
    };
    
    if (isNewField == true)
    {
        addToList("fieldList", fieldId, fieldLabel);
    }
    
    setValue("fieldId", "");
    setValue("fieldType", "");
    setValue("fieldName", "");
    setValue("fieldLabel", "");
    setValue("toolTip", "");
    return true;
}
function displayGeneralFieldDetails(fieldId, obj)
{
    setValue("fieldId", fieldId);
    setValue("fieldType", obj["fieldType"]);
    disable("fieldType");
    setValue("fieldName", obj["fieldName"]);
    setValue("fieldLabel", obj["fieldLabel"]);
    setValue("toolTip", obj["toolTip"]);
    showComponents();
    return;
}

function saveRadioList()
{
    var fieldId = getValue("fieldId");
    var fieldType = "radio";
    var groupName = trim(getValue("groupName"));
    
    if (groupName == "")
    {
        alert(GROUP_NAME_REQUIRED);
        return false;
    }
    if (groupName.match(/^[a-zA-Z0-9_]+$/) == null)
    {
        alert(GROUP_NAME_RULE);
        return false;
    }
    
    var radioCtrl = document.getElementById("radioList");
    var length = radioCtrl.length;
    if (length < 2)
    {
        alert(OPTIONS_REQUIRED);
        return false;
    }
    var arrOptions = new Array();
    for (i = 0; i < length;i++)
    {
        arrOptions.push(radioCtrl.options[i].text);
    }
    
    var isNewField = false;
    
    if (fieldId == "")
    {
        isNewField = true;
        fieldId = groupName;
    }
    
    fieldMap[fieldId] = 
    {
        "fieldType" : fieldType,
        "groupName" : groupName,
        "options"   : arrOptions
    };
    
    if (isNewField == true)
    {
        addToList("fieldList", fieldId, groupName);
    }
    
    // Clear the values in the fields
    setValue("fieldId", "");
    setValue("groupName", "");
    clearSelect("radioList");
    
    return true;
}

function displayRadioList(fieldId, obj)
{
    setValue("fieldId", fieldId);
    setValue("fieldType", "radio");
    disable("fieldType");
    showComponents();
    setValue("groupName", obj["groupName"]);
    
    var select = document.getElementById("radioList");
    select.options.length = 0;
    var arrOptions = obj["options"];
    for(i=0; i < arrOptions.length; i++)
    {
        select.options.length = i + 1;
        select.options[i].text = arrOptions[i];
        select.options[i].value= arrOptions[i];
    }
    return;
}

function saveSelectFieldDetails()
{
    var fieldId = getValue("fieldId");
    var fieldType = "dropdown";
    var fieldName = trim(getValue("fieldName"));
    var fieldLabel = trim(getValue("fieldLabel"));
    var toolTip = getValue("toolTip");
    
    if (fieldName == "")
    {
        alert(FIELD_NAME_MANDATORY);
        return false;
    }
    
    // Field name can contain only alphanumeric characters and underscore.
    if (fieldName.match(/^[a-zA-Z0-9_]+$/) == null)
    {
        alert(FIELD_NAME_RULE);
        return false;
    }
    
    var isNewField = false;
    // Check if there is an entry with this name in this form.
    var obj = fieldMap[fieldName];
    if (obj != null && fieldId == "")
    {
        var result = confirm(FIELD_NAME_EXISTS);
        if (result == false) return false;
        fieldId = fieldName;
    }
    
    // Set the fieldId as fieldName if fieldId is currently empty.
    if (fieldId == "")
    {
        isNewField = true;
        fieldId = fieldName;
    }
    
    if (fieldLabel == "")
    {
        alert(FIELD_LABEL_MANDATORY);
        return false;
    }
    
    fieldMap[fieldId] = 
    {
        "fieldType" : fieldType,
        "groupName" : groupName,
        "options"   : arrOptions
    };
    
    if (isNewField == true)
    {
        addToList("fieldList", fieldId, groupName);
    }
    
    // Clear the values in the fields
    setValue("fieldId", "");
    setValue("groupName", "");
    clearSelect("radioList");
    
    return true;
}

