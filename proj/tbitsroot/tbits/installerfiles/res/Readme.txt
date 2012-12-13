This installater wizard will install tBits 6.0, install tomcat 5.5 and create tBits database.
What is tBits?
	tBits is a document management and tracking system. 
	It was built to improve the management of documents within groups or 
	departments, as well as to manage interdependencies between groups. 
	tBits has a web-based interface and an email-based interface.

What info does the installation wizard require?
	This wizard will need inputs from your side. Before going ahead with the wizard, please
	provide the following information when prompted.

	tBits Specific:
	1. Default Business Area - tBits requires at lease one business area. 
		So, common practice is to create a business area with name "tbits"
	2. Super User - A super user account is similar to "root" in unix. It has all previleges.
		You must have an account with the same name in your domain controller.

	Database:
		1. Server Name - The Microsoft SQL database server on which you want the database 
			of tBits to be created. It can also accept the name or IP Address of the system. 
		2. Name - The name of the database. Default is tbits.
		3. Database Admin User Name / Password - This is required to create the database schema, 
			functions and stored procedures and to fill tBits's internal data.
	
	Domain Controller and LDAP:
		1. Domain Controller Name - As tBits uses the domain controller based authentication, 
			it requires the Domain Controller's server name or IP Address.
		2. LDAP Server Name - LDAP server with which the tBits users are synchronized periodically.
		3. LDAP User Name / Password - Required for users synchronization with LDAP Server.

	SMTP Server:
		1. Server Name - The SMTP server name using which the system will send mails.
		2. Login/Password - If your SMTP server uses Authentication, provide user name and password.
	
		