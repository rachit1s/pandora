Welcome to Jaguar!!

This documented in intended for a person who is new to this application, it is a general lecture, i m not explaining any code here.

If you are really new to this thing, get some background knowledge from here : 
http://code.google.com/webtoolkit/
http://www.sencha.com/products/extgwt/

We basically have three GWT modules :
1. commons.com.tbitsGlobal.utils.Utils
2. com.tbitsGlobal.jaguar.Jaguar
3. com.tbitsGlobal.admin.Admin

The latter two modules inherits the first one.

Some main components are :
1. History Management
2. Client side Cache
3. Bulk Grid Architecture
4. Request Form Architecture
5. Uploader
6. Requests Grids
7. Logger
8. Plugin Architecture
9. Event Bus

There are various other things but that you may know while you are working on the bigger ones. 
I have tried to document the Code as far as i felt necessary. 

The whole objective of writing this document is to let you know the philosophy with which each component is written.

We will go about discussing the above stated components one by one : 
1. History Management : 
	History Management means keeping the track of users actions through URL and enable him move back and forth in the application through
	Browser's "Back" and "Next" buttons. A good example is GMail.

	Though I am not pleased with the way it has been written, but it suffices. 
	You can start from the TbitsURLManager class to know what is it. 

	Basically this is how it works : In the applications URL, the string after # is used as the History String. Now browser maintains a stack
	of all these strings. Whenever we change this string by typing in the URL or programatically, an event is fired by GWT called ValueChangeEvent.
	What we do is that we listen to this event, evaluate the present History String, tokenize it and delegate tokens to other components in the
	application through another event called OnHistoryTokensChanged. Any component that is listening to this event can then act accordingly. 

2. Client Side Cache :
	CAUTION : Avoid making caches, they will make the application heavy and slow.
	Cache is basically some data that is held on Client Side and can be accessed by any component.
	A few things for which cache has been made are : 
	-> Business Areas
	-> Fields
	-> Display Groups
	-> Users

	You can start with commons.com.tbitsGlobal.utils.client.cache.AbstractCache class. It is pretty straight forward.

3. Bulk Grid Architecture
	The most used and the most well written component. Bulk Grid comes from Excel Sheets. Whenever you have to edit/add many records of same nature 
	bulk grid can be used. It provides the user with a wide range of features. Almost complete Admin module is based on this component.

	commons.com.tbitsGlobal.utils.client.bulkupdate Package contains the basic code for this module.

4. Request Form Architecture
	Forms are our traditional way of putting in and editing data in the application.
	Start with RequestFormFactory class. There is not much to explain in this but just the concept of IFieldConfig. 
	IFieldConfig is a configuration object for a every field that is to be displayed. It controls how a particular field would look, how it would 
	give values and how it would take the values from external code. Have a look at the IFieldConfig interface and its implementations.

	Correspondence module makes a lot of usage of this component. 

5. Uploader
	The current uploader is a third party code that we are using. It not well written and is not documented. It would always be a huge effort to make 
	any change in this component. You can find the code in commons.com.tbitsGlobal.utils.client.Uploader Package. 

6. Request Grids
	How is this different from Bulk grids?
	a. Request Grids are grids that are used to show requests(Remember.. to show only.. not to edit). Bulk grids can do both. 
	b. All the grids are tree based(you can show subrequests). Bulk grids are not.
	c. they work for requests only. Bulk grids can be configured for any kind of data.

	Start with RequestsViewGrid and RequestsViewGridContainer class. Read their extensions also. 
	The grids to show search results, my requests, tags are all its extensions. 
	
	There is not much that I can further explain. Work on it.. you would know.

7. Logger
	A very small component but adds a great convenience to developer as well as user. You can maintains logs of activities and errors on the client side.
	commons.com.tbitsGlobal.utils.client.log Package contains the code.

8. Plugin Architecture
	It is the core of our product. The whole objective is to enable us plug in external components written on our API.
	All modules.. DCM, Correspondence, MOM, IL, etc.. are plugins.
	
	The code is present in commons.com.tbitsGlobal.utils.client.plugins and commons.com.tbitsGlobal.utils.server.plugins Packages.
	
	Please read http://www.tbitsglobal.com/wiki/TBits_Module_Developers_Guide to know how it works.

9. Event Bus.
	An event bus is required in a scenario where a component wants to broadcast some data or information to other components.
	The code is present in commons.com.tbitsGlobal.utils.client.Events Package.

==================================================================================================================

This is almost what i had to say. Further details can be made available on On-Demand basis :P

A few more points to be kept in mind : 
1. Jaguar is only a Client side module. Don't try to do anything here that you are not suppossed to to.
2. I have tried to make components generic. Please you try to do the same(However stringent the timelines be). 
   Specifications would change before you would complete writing anything. Keeping it generic would save you a lot of redundant labor.
3. Management guys are not the ones who make a product. They would certainly try to ruin it. Fight to save your code.

------------------------
Regards.

Sourabh Agrawal

sourabh.a@tbitsglobal.com
iitr.sourabh@gmail.com
