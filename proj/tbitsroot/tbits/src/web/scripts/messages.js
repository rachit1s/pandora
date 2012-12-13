/**********************************Add-Request,Add-Action and Transfer Request page****************************************/
// Logger required in Add-Activity page.
var LOGGER_MANDATORY = "The Logger field cannot be empty.";

// Subject Mandatory in Add Activity page.
var SUBJECT_MANDATORY = "The Subject field cannot be empty.";

// Subject Mandatory in Add Activity page.
var SELECT_TRANSFER_BA = "Select the Business Area to which this request should be transferred.";

var TRANSFER_SUCCESSFUL = "The request has been transferred successfully.";
            
var TRANSFER_FAILED = "The request could not be transferred because of a database error.\n" + 
                                  "The TBits team has been notified. Please try again later.";
     

//If an empty due-date is submitted in a BA which doesnt allow empty due-dates.
var EMPTY_DUE_DATE = "This Business Area does not allow empty due dates.";

// Date is specified in format other than mm/dd/yyyy
var INCORRECT_DATE_FORMAT = "The date specified should be in mm/dd/yyyy format.";

// Date Part specified is incorrect.
var INCORRECT_DATE = "The day of the month specified in 'Due Date' should be between 1 and 31.";

// Month Part specified is incorrect.
var INCORRECT_MONTH = "The month specified in 'Due Date' should be between 1 and 12.";

// Year Part specified is incorrect.
var INCORRECT_YEAR = "The year specified in 'Due Date' should be between 1980 and 2050.";

// Date for the month specified is incorrect.
var INCORRECT_DATE_FOR_MONTH = "The day of the month entered in 'Due Date' is invalid for the month specified.";

// Format is MM/dd/yyyy
var INVALID_DUE_DATE_FORMAT = "The format of the Due Date should be either 'mm/dd/yyyy HH:MM:SS' or 'mm/dd/yyyy'.";

// Hour [1, 12]
var INVALID_HOUR_VALUE = "The hour specified should be in the range 0 to 23."

// Minute [1, 60]
var INVALID_MINUTE_VALUE = "The minute specified should be in the range 0 to 59."

// Seconds [1, 60]
var INVALID_SECOND_VALUE = "The seconds specified should be in the range 0 to 59."

// While adding/updating a request, due date cannot occur in past.
var DUE_DATE_CANNOT_BE_EARLIER_THAN_LOGGED_DATE = "The Due Date specified cannot occur in the past.";

// While adding/updating a request, if anything other than a non-positive integer is specified in the parent-request id text box.
var INVALID_PARENT_REQUEST_ID_FORMAT = "Specify a positive integer for 'Parent #'."; 

//If invalid linked request is entered in the linked-requests text box.
var INVALID_RELATED_REQUEST_ID_FORMAT = "Specify a positive integer or a Smart Tag for 'Linked #'."; 

var INVALID_RELATED_REQUEST = "The specified request does not exist: "; 

// Message when user entered an invalid value into an extended field.
var INVALID_EXT_FIELD_VALUE = "Specify a valid <DATATYPE> in '<FIELDNAME>'.";

//Message when user tries to submit without changing the request's current state.
var EMPTY_APPEND = "You have not made any changes to this request.";

//Message when the user closes the add-request window and a copy of the draft is saved.
var AUTO_SAVE_DRAFT = "TBits has automatically saved a draft of this message. Do you want to keep it ?";


/***************************************Options Page******************************************************/

// Prompt when user navigates to a different page from a dirty Add-Request page.
var NAVIGATION_ALERT = "Changes made to this form have not been saved and will be lost.\nContinue?";

//If the user options cannot be saved because of database error.
var SAVING_OPTIONS_UNSUCCESSFUL = "Your option settings could not be saved because of a database error.\n" + 
                                  "The TBits team has been notified. Please try again.";

// Request ID field should be present in the Search Results Display header.
var REQUEST_ID_MANDATORY_IN_DISPLAY_HEADER = "The Request ID. field is mandatory and cannot be removed.";

// Confirmation whether user really wants to revert to default settings.
var REVERT_BACK_TO_DEFAULT_SETTINGS = "This will clear your custom settings on this page. \nContinue?";


/***********************************************Admin Pages**************************************************/


//If the Business Area name is left empty in properties page.
var EMPTY_BUSINESS_AREA_NAME = "The name of the Business Area must be specified.";

//If the display name of the Business Area is left empty in properties page.
var EMPTY_BUSINESS_AREA_DISPLAY_NAME = "The display name of the Business Area must be specified.";

//If the Business Area prefix is left empty in properties page.
var EMPTY_BUSINESS_AREA_PREFIX = "The prefix of the Business Area must be specified.";

//If the Business Area email is left empty in properties page.
var EMPTY_BUSINESS_AREA_EMAIL = "The email of the Business Area must be specified.";

//If the default due time is left empty in properties page.
var EMPTY_DUE_TIME = "The default due time for this Business Area must be specified.";

//If the display name of the field is left empty in Fields page.
var EMPTY_FIELD_DISPLAY_NAME = "The display name of the field must be specified.";

//If the display name of the type is left empty in Fields page.
var EMPTY_TYPE_DISPLAY_NAME = "The display name of the type must be specified.";

//If the BA prefix contains non alpha numeric charecters in properties page.
var INVALID_BUSINESS_AREA_PREFIX = "The Business Area Prefix may contain only alphanumeric characters.";

//The field descriptors cannot contain spaces.
var FIELD_DESCRIPTOR_SPACES = "The Field Descriptors may contain only alphabetic characters.";


/****************************************Search Page****************************************************/
// Shortcuts
// Search Criteria name is required while saving search.
var SEARCH_CRITERIA_NAME_MANDATORY = "Specify the name of the search.";

// A Search Criteria with the specified name is already present. Confirming if the user wants to overwrite it.
var DUPLICATE_NAME_OVERWRITE = "You have already saved a search with this name. \nOverwrite?";

// Save search operation failed.
var SAVE_SEARCH_FAILED = "The search could not be saved because of a database error.\n" + 
                         "The TBits team has been notified. Please try again.";

// Delete search criteria operation failed.
var SEARCH_CRITERIA_DELETE_OPERATION_FAILED = "The saved search could not be deleted because of a database error.\n" +
                                              "The TBits team has been notified. Please try again.";

// Search criteria name can contain only alphanumeric characters.
var INVALID_SEARCH_CRITERIA_NAME = "The search shorcut name may contain only alphanumeric characters, spaces and underscores.";

// Prompt when user navigates to a different page from a dirty Add-Request page.
// var NAVIGATION_ALERT = "Changes made to this form have not been saved and will be lost.\nContinue?";

// No Assignee Found for the category.
var NO_ANALYSTS_FOR_CATEGORY = "There are no analysts assigned to this category.";

//If the shortcut could not be saved as default due to database error;
var SHORTCUT_DEFAULT_FAILED = "The saved search could not be set as default due to a database error.\n" +
                              "The TBits team has been notified. Please try again.";

//If the saved search cannot be unset due to database error;
var SHORTCUT_UNSET_FAILED = "The saved search could not be removed as default due to a database error.\n" +
                            "The TBits team has been notified. Please try again.";

//If the shortcut cannot be marked private due to database error;
var SHORTCUT_PRIVATE_FAILED = "The saved search could not be marked as private due to a database error.\n" +
                              "The TBits team has been notified. Please try again.";

//If the shortcut cannot be marked public due to database error;
var SHORTCUT_PUBLIC_FAILED = "The saved search could not be marked as public due to a database error.\n" +
                              "The TBits team has been notified. Please try again.";

var NO_SHORTCUTS = "No shortcuts.";

//Messages for bulk request updates.

//If no requests are selected and the user clicks on bulk update.
var SELECT_REQUEST_FOR_GROUP_ACTION = "No requests have been selected.";

//Message displayed if the bulk update fails.
var GROUP_ACTION_FAILURE_REASON_BELOW = "The selected requests could not be marked as '<action>' because\n<message>";

//Successful bulk update.
var GROUP_ACTION_SUCCESSFUL = "The selected requests have been successfully marked as '<action>'.";

var VALID_DATE_FORMAT = "Specify date in mm/dd/yyyy format.";

var VALID_INTEGER = "Specify a valid integer";

var NUMERIC_VALUE = "Specify a numeric value.";

